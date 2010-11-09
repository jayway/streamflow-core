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

package se.streamsource.dci.restlet.server.resultwriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.restlet.server.ResultWriter;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * JAVADOC
 */
public class ResourceTemplateResultWriter
      implements ResultWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_ATOM );

   @Service VelocityEngine velocity;

   @Service MetadataService metadataService;

   public boolean write( final Object result, final Response response ) throws ResourceException
   {
      MediaType type = response.getRequest().getClientInfo().getPreferredMediaType( supportedMediaTypes );
      if (type != null)
      {
         // Try to find template for this specific resource
         StringBuilder templateBuilder = (StringBuilder) response.getRequest().getAttributes().get( "template" );
         String templateName = templateBuilder.toString();

         if (result instanceof ValueDescriptor)
            templateName += "_form";

         final String extension = metadataService.getExtension( type );
         templateName += "."+extension;


         try
         {
            final Template template = velocity.getTemplate( templateName );
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  Object res = result;
                  if (res instanceof ValueComposite)
                  {
                     res = new ValueCompositeContext( (ValueComposite) res );
                  }

                  context.put( "result", res );
                  template.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;

         } catch (Exception e)
         {
            // Ignore
         }
      }

      return false;
   }
}