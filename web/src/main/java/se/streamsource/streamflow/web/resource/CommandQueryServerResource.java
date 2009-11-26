/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource;

import org.json.JSONException;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.infrastructure.event.CommandEvents;
import se.streamsource.streamflow.web.infrastructure.web.TemplateUtil;

import javax.security.auth.Subject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for command/query resources.
 * <p/>
 * GET: if has ?operation=name then show XHTML form
 * for invoking operation with name "name". Otherwise
 * return whatever makes sense (listing, query form(s), etc.)
 * <p/>
 * POST: post of form must include names "operation" and "command",
 * where "operation" is the name of the method to invoke and "command
 * is the JSON-serialized command value.
 * <p/>
 * PUT: put of form must include names "operation" and "command",
 * where "operation" is the name of the method to invoke and "command
 * is the JSON-serialized command value. Must be an idempotent operation.
 * <p/>
 * DELETE: resources implement this on their own.
 */
public class CommandQueryServerResource
        extends BaseServerResource
{
    @Structure
    protected ValueBuilderFactory vbf;

    @Structure
    protected ModuleSPI module;

    @Service
    CommandEvents commandEvents;

    public CommandQueryServerResource()
    {
        getVariants().addAll(Arrays.asList(new Variant(MediaType.TEXT_HTML), new Variant(MediaType.APPLICATION_JSON)));

        setNegotiated(true);
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        String operation = getQueryOperation();
        if (operation == null)
        {
            return listOperations();
        } else
        {
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase(operation));

            try
            {
                Method method = getResourceMethod(operation);

                if (getRequest().getResourceRef().hasQuery() || method.getParameterTypes().length == 0)
                {
                    // Invoke query
                    Object[] args = getQueryArguments(method);
                    return returnRepresentation(invoke(method, args), variant);
                } else
                {
                    // Show form
                    return new StringRepresentation(""); // TODO
                }
            } finally
            {
                uow.discard();
            }
        }
    }

    @Override
    final protected Representation delete(Variant variant) throws ResourceException
    {
        return post(null, variant);
    }

    @Override
    final protected Representation post(Representation entity, Variant variant) throws ResourceException
    {
        String operation = getCommandOperation();
        UnitOfWork uow = null;
        try
        {
            Method method = getResourceMethod(operation);
            Object[] args = getCommandArguments(method);

            int tries = 0;
            while (tries < 10)
            {
                uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase(operation));

                commandEvents.reset();
                Representation rep = returnRepresentation(invoke(method, args), variant);
                try
                {
                    uow.complete();

                    if (rep instanceof EmptyRepresentation)
                    {
                        rep = new WriterRepresentation(MediaType.APPLICATION_JSON)
                        {
                            public void write( Writer writer) throws IOException
                            {
                                writer.write(commandEvents.commandEvents().toJSON());
                            }
                        };
                        rep.setCharacterSet( CharacterSet.UTF_8 );
                    }

                    return rep;
                } catch (ConcurrentEntityModificationException e)
                {
                    // Try again
                } catch (Exception ex)
                {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex);
                }
            }

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not invoke command");

        } catch (ResourceException ex)
        {
            if (uow != null)
                uow.discard();

            if (ex.getMessage().startsWith( "<html>" ))
            {
                getResponse().setStatus( ex.getStatus() );
                return new StringRepresentation( ex.getMessage());
            } else
            {
                if (ex.getCause() instanceof ConstraintViolationException)
                {
                    throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
                }
                throw ex;
            }
        } catch (Exception ex)
        {
            Logger.getLogger("command").log(Level.SEVERE, "Could not process command:" + operation, ex);

            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new ObjectRepresentation(ex, MediaType.APPLICATION_JAVA_OBJECT);
        }
    }

    protected String getQueryOperation()
    {
        String operation = getRequest().getResourceRef().getQueryAsForm().getFirstValue("query");
        if (operation == null && !getRequest().getMethod().getName().equals("GET"))
        {
            operation = getRequest().getMethod().getName().toLowerCase() + "Operation";
        }
        return operation;
    }

    protected String getCommandOperation()
    {
        String operation = getRequest().getResourceRef().getQueryAsForm().getFirstValue("command");
        if (operation == null)
        {
            operation = getRequest().getMethod().getName().toLowerCase() + "Operation";
        }
        return operation;
    }

    protected Representation listOperations() throws ResourceException
    {
        // List methods
        Method[] methods = getClass().getDeclaredMethods();
        StringBuilder queries = new StringBuilder("");
        for (Method method : methods)
        {
            if (!Modifier.isPublic( method.getModifiers() ))
                continue;

            if (isQueryMethod(method))
                queries.append("<li><a href=\"?query=").append(
                        method.getName()).append("\" rel=\"").append(
                        method.getName()).append("\">")
                        .append(method.getName()).append("</a></li>\n");
        }

        StringBuilder commands = new StringBuilder("");
        for (Method method : methods)
        {
            if (!Modifier.isPublic( method.getModifiers() ))
                continue;

            if (isCommandMethod(method))
                commands.append("<li><a href=\"?command=").append(
                        method.getName()).append("\" rel=\"").append(
                        method.getName()).append("\">")
                        .append(method.getName()).append("</a></li>\n");
        }

        try
        {
            String template = TemplateUtil.getTemplate("resources/links.html",
                    CompositeCommandQueryServerResource.class);
            String content = TemplateUtil.eval(template,
                    "$queries", queries.toString(),
                    "$commands", commands.toString(),
                    "$title", getRequest().getResourceRef().getLastSegment());
            return new StringRepresentation(content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8);
        } catch (IOException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    private Representation returnRepresentation(Object returnValue, Variant variant) throws ResourceException
    {
        if (returnValue != null)
        {
            if (returnValue instanceof ValueComposite)
            {
                if (variant.getMediaType().equals(MediaType.APPLICATION_JSON))
                {
                    return new StringRepresentation(((Value) returnValue).toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8);
                } else if (variant.getMediaType().equals(MediaType.TEXT_HTML))
                {
                    try
                    {
                        String template = TemplateUtil.getTemplate("resources/value.html", CommandQueryServerResource.class);
                        String content = TemplateUtil.eval(template, "$content", ((Value) returnValue).toJSON());
                        return new StringRepresentation(content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8);
                    } catch (IOException e)
                    {
                        throw new ResourceException(e);
                    }
                } else
                {
                    return new EmptyRepresentation();
                }
            } else if (returnValue instanceof Representation)
            {
                return (Representation) returnValue;
            } else
            {
                Logger.getLogger(getClass().getName()).warning("Unknown result type:" + returnValue.getClass().getName());
                return new EmptyRepresentation();
            }
        } else
            return new EmptyRepresentation();
    }

    @Override
    final protected Representation put(Representation representation, Variant variant) throws ResourceException
    {
        return post(representation, variant);
    }

    private Method getResourceMethod(String operation)
            throws ResourceException
    {
        for (Method method : getClass().getMethods())
        {
            if (method.getName().equals(operation))
            {
                return method;
            }
        }
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }

    private Object[] getCommandArguments(Method method) throws ResourceException
    {
        if (method.getParameterTypes().length > 0)
        {
            Class<? extends Value> commandType = (Class<? extends Value>) method.getParameterTypes()[0];

            if (Representation.class.isAssignableFrom( commandType))
            {
                return new Object[]{getRequest().getEntity()};
            } else if (getRequest().getEntity().getMediaType().equals(MediaType.APPLICATION_JSON))
            {
                String json = getRequest().getEntityAsText();
                if (json == null)
                {
                    StringBuffer fileData = null;
                    try
                    {
                        fileData = new StringBuffer(1000);
                        BufferedReader reader = new BufferedReader(getRequest().getEntity().getReader());
                        char[] buf = new char[1024];
                        int numRead = 0;
                        while ((numRead = reader.read(buf)) != -1)
                        {
                            fileData.append(buf, 0, numRead);
                        } 
                        reader.close();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers! available:" + getRequest().getEntity().isAvailable() + " size:" + getRequest().getEntity().getSize()+" stream:"+fileData.toString());
                }

                Object command = vbf.newValueFromJSON(commandType, json);

                return new Object[]{command};
            } else if (getRequest().getEntity().getMediaType().equals(MediaType.TEXT_PLAIN))
            {
                String text = getRequest().getEntityAsText();
                if (text == null)
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");
                return new Object[]{text};
            } else
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Command has to be in JSON format");
        } else
        {
            return new Object[0];
        }
    }

    private Object[] getQueryArguments(Method method)
            throws ResourceException
    {
        Object[] args = new Object[method.getParameterTypes().length];
        int idx = 0;

        Representation representation = getRequest().getEntity();
        if (representation != null && MediaType.APPLICATION_JSON.equals(representation.getMediaType()))
        {
            Class<?> valueType = method.getParameterTypes()[0];
            String json = getRequest().getEntityAsText();
            if (json == null)
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");
            Object requestValue = vbf.newValueFromJSON(valueType, json);
            args[idx++] = requestValue;
        } else
        {
            final Form asForm = getRequest().getResourceRef().getQueryAsForm();

            if (args.length > 0 && ValueComposite.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
                Class<?> valueType = method.getParameterTypes()[0];
                args[idx++] = getValueFromQuery((Class<ValueComposite>) valueType);
            } else
            {
                for (Annotation[] annotations : method.getParameterAnnotations())
                {
                    Name name = Annotations.getAnnotationOfType(annotations, Name.class);

                    if (name != null)
                    {
                        Object arg = asForm.getFirstValue(name.value());

                        // Parameter conversion
                        if (method.getParameterTypes()[idx].equals(EntityReference.class))
                        {
                            arg = EntityReference.parseEntityReference(arg.toString());
                        }

                        args[idx++] = arg;
                    }
                }
            }
        }

        return args;
    }

    private Object invoke(final Method method, final Object[] args)
            throws ResourceException
    {
        try
        {
            Subject subject = new Subject();
            subject.getPrincipals().addAll( getRequest().getClientInfo().getPrincipals() );
            final Object commandObject = this;
            try
            {
                Object returnValue = Subject.doAs(subject, new PrivilegedExceptionAction()
                {
                    public Object run() throws Exception
                    {
                        return method.invoke(commandObject, args);
                    }
                });

                return returnValue;
            } catch (PrivilegedActionException e)
            {
                throw e.getCause();
            }
        } catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof ResourceException)
            {
                throw (ResourceException) e.getTargetException();
            } else if (e.getTargetException() instanceof AccessControlException)
            {
                // Operation not allowed - return 403
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
            }

            Logger.getLogger("command").log(Level.SEVERE, "Could not invoke command:" + method.getName(), e.getTargetException());

            getResponse().setEntity(new ObjectRepresentation(e));

            throw new ResourceException(e.getTargetException());
        } catch (Throwable e)
        {
            throw new ResourceException(e);
        }
    }

    private ValueComposite getValueFromQuery(Class<ValueComposite> valueType)
    {
        final Form asForm = getRequest().getResourceRef().getQueryAsForm();
        ValueBuilder<ValueComposite> builder = vbf.newValueBuilder(valueType);
        final ValueDescriptor descriptor = spi.getValueDescriptor(builder.prototype());
        builder.withState(new StateHolder()
        {
            public <T> Property<T> getProperty(QualifiedName name)
            {
                return null;
            }

            public <T> Property<T> getProperty(Method propertyMethod)
            {
                return null;
            }

            public void visitProperties(StateVisitor visitor)
            {
                for (PropertyType propertyType : descriptor.valueType().types())
                {
                    Parameter param = asForm.getFirst(propertyType.qualifiedName().name());
                    if (param != null)
                    {
                        String value = param.getValue();
                        if (value == null)
                            value = "";
                        try
                        {
                            Object valueObject = propertyType.type().fromQueryParameter(value, module);
                            visitor.visitProperty(propertyType.qualifiedName(), valueObject);
                        } catch (JSONException e)
                        {
                            throw new IllegalArgumentException("Query parameter has invalid JSON format", e);
                        }
                    }
                }
            }
        });
        return builder.newInstance();
    }

    /**
     * A query method has the following attributes
     * - Does not return void
     *
     * @param method
     * @return
     */
    private boolean isQueryMethod(Method method)
    {
        return !Void.TYPE.equals(method.getReturnType());
    }

    /**
     * A command method has the following attributes:
     * - Returns void
     * - Has a Value as the first parameter
     *
     * @param method
     * @return
     */
    private boolean isCommandMethod(Method method)
    {
        return method.getReturnType().equals(Void.TYPE) && method.getParameterTypes().length > 0 && Value.class.isAssignableFrom(method.getParameterTypes()[0]);
    }
}
