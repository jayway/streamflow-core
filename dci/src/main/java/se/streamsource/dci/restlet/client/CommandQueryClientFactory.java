/*
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.restlet.client;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.*;

import java.util.Collections;
import java.util.Locale;

/**
 * Builder for CommandQueryClient
 */
public class CommandQueryClientFactory
{
   @Structure
   private Qi4jSPI spi;

   @Structure
   private Module module;

   @Uses @Optional
   private ClientCache cache ;

   @Uses
   @Optional
   private ResponseHandler handler = new NullResponseHandler();

   @Uses
   private ResponseReaderDelegator readerDelegator;

   @Uses
   private Uniform client;

   public CommandQueryClient newClient( Reference reference )
   {
      return module.objectBuilderFactory().newObjectBuilder( CommandQueryClient.class ).use( this, reference ).newInstance();
   }

   ResponseHandler getHandler()
   {
      return handler;
   }

   void updateCommandRequest( Request request)
   {
      ClientInfo info = new ClientInfo();
      info.setAgent( "Streamflow" ); // TODO Set versions correctly
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      info.setAcceptedLanguages( Collections.singletonList( new Preference<Language>(new Language( Locale.getDefault().getLanguage()) )));
      request.setClientInfo( info );

      if (cache != null)
         cache.updateCommandConditions( request );
   }

   void updateQueryRequest( Request request)
   {
      ClientInfo info = new ClientInfo();
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      info.setAcceptedLanguages( Collections.singletonList( new Preference<Language>( new Language( Locale.getDefault().getLanguage() ) ) ) );
      request.setClientInfo( info );
   }

   void updateCache( Response response)
   {
      if (cache != null)
         cache.updateCache( response );
   }

   Uniform getClient()
   {
      return client;
   }

   <T> T readResponse( Response response, Class<T> queryResult)
   {
      readerDelegator.readResponse( response, queryResult );
      String jsonValue = response.getEntityAsText();

      return module.valueBuilderFactory().newValueFromJSON( queryResult, jsonValue);
   }

   public void writeRequest( Request request, Object queryRequest )
   {
      if (queryRequest != null)
         setQueryParameters( request.getResourceRef(), (ValueComposite) queryRequest );
   }

   private void setQueryParameters( final Reference ref, ValueComposite queryValue )
   {
      // Value as parameter
      StateHolder holder = spi.getState( queryValue );
      final ValueDescriptor descriptor = spi.getValueDescriptor( queryValue );

      ref.setQuery( null );

      holder.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
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

}
