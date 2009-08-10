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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.util.Annotations;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.helpers.json.JSONException;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.infrastructure.web.TemplateUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    protected UnitOfWorkFactory uowf;

    @Structure
    protected ValueBuilderFactory vbf;

    @Structure
    protected Qi4jSPI spi;

    @Structure
    protected Module module;

    public CommandQueryServerResource()
    {
        getVariants().put(org.restlet.data.Method.ALL, Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_JSON));

        setNegotiated(true);
    }


    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        String operation = getOperation();
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

    private String getOperation()
    {
        return getRequest().getResourceRef().getQueryAsForm().getFirstValue("operation");
    }

    protected Representation listOperations() throws ResourceException
    {
        // List methods
        String links = "";
        Method[] methods = getClass().getMethods();
        for (Method method : methods)
        {
            if (isQueryMethod(method))
                links += "<li><a href=\"?operation=" + method.getName() + "\" rel=\"" + method.getName() + "\">" + method.getName() + "</a></li>\n";
        }

        try
        {
            String template = TemplateUtil.getTemplate("resources/links.html", CommandQueryServerResource.class);
            String content = TemplateUtil.eval(template, "$content", links, "$title", getRequest().getResourceRef().getLastSegment() + " operations");
            return new StringRepresentation(content, MediaType.TEXT_HTML);
        } catch (IOException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    @Override
    protected Representation post(Representation entity, Variant variant) throws ResourceException
    {
        String operation = getOperation();
        UnitOfWork uow = null;
        try
        {
            Method method = getResourceMethod(operation);
            Object[] args = getCommandArguments(method);

            int tries = 0;
            while (tries < 10)
            {
                uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase(getRequest().getResourceRef().getLastSegment()));

                Representation rep = returnRepresentation(invoke(method, args), variant);
                try
                {
                    uow.complete();
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

            throw ex;
        } catch (Exception ex)
        {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new ObjectRepresentation(ex, MediaType.APPLICATION_JAVA_OBJECT);
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
                    return new StringRepresentation(((Value) returnValue).toJSON(), MediaType.APPLICATION_JSON);
                } else if (variant.getMediaType().equals(MediaType.TEXT_HTML))
                {
                    try
                    {
                        String template = TemplateUtil.getTemplate("resources/value.html", CommandQueryServerResource.class);
                        String content = TemplateUtil.eval(template, "$content", ((Value) returnValue).toJSON());
                        return new StringRepresentation(content, MediaType.TEXT_HTML);
                    } catch (IOException e)
                    {
                        throw new ResourceException(e);
                    }
                } else
                {
                    return new EmptyRepresentation();
                }
            } else
            {
                Logger.getLogger(getClass().getName()).warning("Unknown result type:" + returnValue.getClass().getName());
                return new EmptyRepresentation();
            }
        } else
            return new EmptyRepresentation();
    }

    @Override
    protected Representation put(Representation representation, Variant variant) throws ResourceException
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

    private boolean isQueryMethod(Method method)
    {
        return Value.class.isAssignableFrom(method.getReturnType());
    }

    private Object[] getCommandArguments(Method method) throws ResourceException
    {
        if (method.getParameterTypes().length > 0)
        {
            Class<? extends Value> commandType = (Class<? extends Value>) method.getParameterTypes()[0];

            if (getRequest().getEntity().getMediaType().equals(MediaType.APPLICATION_JSON))
            {
                String json = getRequest().getEntityAsText();

                Object command = vbf.newValueFromJSON(commandType, json);

                return new Object[]{command};
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
            Object requestValue = vbf.newValueFromJSON(valueType, getRequest().getEntityAsText());
            args[0] = requestValue;
        } else
        {
            final Form asForm = getRequest().getResourceRef().getQueryAsForm();

            if (args.length == 1 && ValueComposite.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
                Class<?> valueType = method.getParameterTypes()[0];
                ValueBuilder builder = vbf.newValueBuilder(valueType);
                final ValueDescriptor descriptor = spi.getValueDescriptor((ValueComposite) builder.prototype());
                builder.withState(new StateHolder()
                {
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
                args[0] = builder.newInstance();
            } else
            {
                for (Annotation[] annotations : method.getParameterAnnotations())
                {
                    Name name = Annotations.getAnnotationOfType(annotations, Name.class);
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

        return args;
    }

    private Object invoke(Method method, Object[] args)
            throws ResourceException
    {
        try
        {
            Object returnValue = method.invoke(this, args);
            return returnValue;
        } catch (InvocationTargetException e)
        {
            getResponse().setEntity(new ObjectRepresentation(e));

            throw new ResourceException(e.getTargetException());
        } catch (Exception e)
        {
            throw new ResourceException(e);
        }
    }
}
