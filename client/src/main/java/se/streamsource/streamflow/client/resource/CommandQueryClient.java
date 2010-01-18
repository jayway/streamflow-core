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

package se.streamsource.streamflow.client.resource;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Base class for client-side Command/Query resources
 */
public final class CommandQueryClient
{
   @Structure
   private  ValueBuilderFactory vbf;

   @Structure
   private  ObjectBuilderFactory obf;

   @Structure
   private  UnitOfWorkFactory uowf;

   @Structure
   private  Qi4jSPI spi;

   @Structure
   private  Module module;

   @Service
   private TransactionVisitor transactionVisitor;

   @Uses
   private Uniform client;

   @Uses
   private Reference reference;

   public Reference getReference()
   {
      return reference;
   }

   public Uniform getClient()
   {
      return client;
   }

   public <T extends ValueComposite> T query( String operation, Class<T> queryResult ) throws ResourceException
   {
      return query( operation, null, queryResult );
   }

   public <T extends ValueComposite> T query( String operation, ValueComposite queryValue, Class<T> queryResult ) throws ResourceException
   {
      ClientResource result = invokeQuery( operation, queryValue );

      if (result.getResponse().getStatus().isSuccess())
      {
         String jsonValue = result.getResponse().getEntityAsText();
         T returnValue = vbf.newValueFromJSON( queryResult, jsonValue );
         return returnValue;
      } else
      {
         // This will throw an exception
         handleError( result.getResponse() );
         return null;
      }
   }

   public InputStream queryStream( String operation, ValueComposite queryValue ) throws ResourceException, IOException
   {
      ClientResource result = invokeQuery( operation, queryValue );

      if (result.getResponse().getStatus().isSuccess())
      {
         return result.getResponse().getEntity().getStream();
      } else
      {
         // This will throw an exception
         handleError( result.getResponse() );
         return null;
      }
   }

   private void setQueryParameters( final Reference ref, ValueComposite queryValue )
   {
      // Value as parameter
      StateHolder holder = spi.getState( queryValue );
      final ValueDescriptor descriptor = spi.getValueDescriptor( queryValue );

      ref.setQuery( null );

      holder.visitProperties( new StateHolder.StateVisitor()
      {
         public void visitProperty( QualifiedName
               name, Object value )
         {
            if (value != null)
            {
               PropertyTypeDescriptor propertyDesc = descriptor.state().getPropertyByQualifiedName( name );
               String queryParam = propertyDesc.propertyType().type().toQueryParameter( value );
               ref.addQueryParameter( name.name(), queryParam );
            }
         }
      } );
   }

   public void postCommand( String operation ) throws ResourceException
   {
      postCommand( operation, new EmptyRepresentation() );
   }

   public void postCommand( String operation, ValueComposite command ) throws ResourceException
   {
      Representation commandRepresentation;
      commandRepresentation = new StringRepresentation( command.toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8 );

      postCommand( operation, commandRepresentation );
   }

   public void postCommand( String operation, Representation commandRepresentation )
         throws ResourceException
   {
      Reference ref = new Reference( reference ).addQueryParameter( "command", operation );
      ClientResource client = new ClientResource( ref );
      client.setNext( this.client );
      client.post( commandRepresentation );
      if (!client.getStatus().isSuccess())
      {
         throw new ResourceException( client.getStatus() );
      } else
      {
         processEvents( client.getResponse() );
      }
   }

   private Object handleError( Response response )
         throws ResourceException
   {
      if (response.getStatus().equals( Status.SERVER_ERROR_INTERNAL ))
      {
         if (response.getEntity().getMediaType().equals( MediaType.APPLICATION_JAVA_OBJECT ))
         {
            try
            {
               Object exception = new ObjectRepresentation( response.getEntity() ).getObject();
               throw new ResourceException( (Throwable) exception );
            } catch (IOException e)
            {
               throw new ResourceException( e );
            } catch (ClassNotFoundException e)
            {
               throw new ResourceException( e );
            }
         }

         throw new ResourceException( Status.SERVER_ERROR_INTERNAL, response.getEntityAsText() );
      } else
      {
         if (response.getEntity() != null)
         {
            String text = response.getEntityAsText();
            throw new ResourceException( response.getStatus(), text );
         } else
         {
            throw new ResourceException( response.getStatus() );
         }
      }
   }

   private ClientResource invokeQuery( String operation, ValueComposite queryValue )
         throws ResourceException
   {
      Reference ref = new Reference( reference );
      if (queryValue != null)
         setQueryParameters( ref, queryValue );
      ref.addQueryParameter( "query", operation );

      ClientResource client = new ClientResource( ref );
      client.setNext( this.client );

      client.get( MediaType.APPLICATION_JSON );

      return client;
   }

   public void create() throws ResourceException
   {
      putCommand( null );
   }

   public void putCommand( String operation ) throws ResourceException
   {
      putCommand( operation, null );
   }

   public void putCommand( String operation, ValueComposite command ) throws ResourceException
   {
      Representation commandRepresentation;
      if (command != null)
         commandRepresentation = new StringRepresentation( command.toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8 );
      else
         commandRepresentation = new EmptyRepresentation();

      Reference ref = new Reference( reference );
      if (operation != null)
      {
         ref = ref.addQueryParameter( "command", operation );
      }

      ClientResource client = new ClientResource( ref );
      client.setNext( this.client );
      int tries = 3;
      while (true)
      {
         try
         {
            client.put( commandRepresentation );
            if (!client.getStatus().isSuccess())
            {
               throw new ResourceException( client.getStatus() );
            } else
            {
               processEvents( client.getResponse() );
            }
            break;
         } catch (ResourceException e)
         {
            if (e.getStatus().equals( Status.CONNECTOR_ERROR_COMMUNICATION ) ||
                  e.getStatus().equals( Status.CONNECTOR_ERROR_CONNECTION ))
            {
               if (tries == 0)
                  throw e; // Give up
               else
               {
                  // Try again
                  tries--;
                  continue;
               }
            } else
            {
               // Abort
               throw e;
            }
         }
      }
   }

   public void deleteCommand() throws ResourceException
   {

      ClientResource client = new ClientResource( new Reference( reference ) );
      client.setNext( this.client );

      int tries = 3;
      while (true)
      {
         try
         {
            client.delete();
            if (!client.getStatus().isSuccess())
            {
               throw new ResourceException( client.getStatus() );
            } else
            {
               processEvents( client.getResponse() );
            }

            break;
         } catch (ResourceException e)
         {
            if (e.getStatus().equals( Status.CONNECTOR_ERROR_COMMUNICATION ) ||
                  e.getStatus().equals( Status.CONNECTOR_ERROR_CONNECTION ))
            {
               if (tries == 0)
                  throw e; // Give up
               else
               {
                  // Try again
                  tries--;
                  continue;
               }
            } else
            {
               // Abort
               throw e;
            }
         }
      }
   }

   public <T extends ClientResource> T getSubResource( String pathSegment, Class<T> clientResource )
   {
      T resource = getResource( reference.clone().addSegment( pathSegment ), clientResource );
      resource.setNext( client );
      return resource;
   }

   public <T extends ClientResource> T getResource( Reference ref, Class<T> clientResource )
   {
      T resource = obf.newObjectBuilder( clientResource ).use( client, new Context(), ref ).newInstance();
      return resource;
   }

   public CommandQueryClient getSubClient( String pathSegment )
   {
      Reference subReference = reference.clone().addSegment( pathSegment );
      return obf.newObjectBuilder( CommandQueryClient.class ).use( client, new Context(), subReference ).newInstance();
   }

   private void processEvents( Response response )
   {
      if (response.getStatus().isSuccess() &&
            (response.getRequest().getMethod().equals( Method.POST ) ||
                  response.getRequest().getMethod().equals( Method.DELETE ) ||
                  response.getRequest().getMethod().equals( Method.PUT )))
      {
         try
         {
            Representation entity = response.getEntity();
            if (entity != null && !(entity instanceof EmptyRepresentation))
            {
               String source = entity.getText();

               final TransactionEvents transactionEvents = vbf.newValueFromJSON( TransactionEvents.class, source );

               transactionVisitor.visit( transactionEvents );
            }
         } catch (Exception e)
         {
            throw new OperationException( StreamFlowResources.could_not_process_events, e );
         }
      }
   }
}