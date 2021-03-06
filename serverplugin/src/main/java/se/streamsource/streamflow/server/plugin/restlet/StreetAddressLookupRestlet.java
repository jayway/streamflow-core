/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.server.plugin.restlet;

import org.json.JSONException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.StringRepresentation;

import se.streamsource.streamflow.server.plugin.address.StreetAddressLookup;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;

/**
 * Delegate Restlet calls to the StreetAddress service.
 */
public class StreetAddressLookupRestlet
      extends Restlet
{
   @Optional
   @Service
   StreetAddressLookup streetAddressLookup;

   @Structure
   private Qi4jSPI spi;

   @Structure
   private ModuleSPI module;

   @Override
   public void handle( Request request, Response response )
   {
      super.handle( request, response );

      try
      {
         if (streetAddressLookup == null)
         {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
            return;
         }

         if (request.getMethod().equals( Method.GET ))
         {
            if (request.getResourceRef().getQuery() == null || request.getResourceRef().getQuery().isEmpty())
            {
               response.setEntity( new InputRepresentation( getClass().getResourceAsStream( "streetform.html" ) ) );
               response.setStatus( Status.SUCCESS_OK );
            } else
            {
               // Parse request
               StreetValue contactTemplate;

               contactTemplate = (StreetValue) getValueFromForm( StreetValue.class, request.getResourceRef().getQueryAsForm() );

               // Call plugin
               StreetList lookups = streetAddressLookup.lookup( contactTemplate );

               // Send response
               String json = lookups.toJSON();

               StringRepresentation result = new StringRepresentation( json, MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8 );
               response.setStatus( Status.SUCCESS_OK );
               response.setEntity( result );
            }
         } else
         {
            response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
         }
      } finally
      {
         request.release();
      }
   }

   private ValueComposite getValueFromForm( Class<? extends ValueComposite> valueType, final Form asForm )
   {
      ValueBuilder<? extends ValueComposite> builder = module.valueBuilderFactory().newValueBuilder(valueType);
      final ValueDescriptor descriptor = spi.getValueDescriptor( builder.prototype() );
      builder.withState( new StateHolder()
      {
         public <T> Property<T> getProperty( QualifiedName name )
         {
            return null;
         }

         public <T> Property<T> getProperty( java.lang.reflect.Method propertyMethod )
         {
            return null;
         }

          public <ThrowableType extends Throwable> void visitProperties( StateVisitor<ThrowableType> visitor )
              throws ThrowableType
         {
            for (PropertyType propertyType : descriptor.valueType().types())
            {
               Parameter param = asForm.getFirst( propertyType.qualifiedName().name() );
               if (param != null)
               {
                  String value = param.getValue();
                  if (value == null)
                     value = "";
                  try
                  {
                     Object valueObject = propertyType.type().fromQueryParameter( value, module );
                     visitor.visitProperty( propertyType.qualifiedName(), valueObject );
                  } catch (JSONException e)
                  {
                     throw new IllegalArgumentException( "Query parameter has invalid JSON format", e );
                  }
               }
            }
         }
      } );
      return builder.newInstance();
   }
}
