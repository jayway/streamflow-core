/**
 *
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

package se.streamsource.streamflow.web.context.structure;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.SelectedTemplate;


/**
 * The context that handles selection/deselection of an attachment as some form of template.
 */
@Mixins(SelectedTemplateContext.Mixin.class)
public interface SelectedTemplateContext
      extends DeleteContext, Context
{
   void settemplate( StringValue id );

   LinksValue possibletemplates( StringValue extensionFilter );

   abstract class Mixin
         extends ContextMixin
         implements SelectedTemplateContext
   {
      public void settemplate( StringValue id )
      {
         SelectedTemplate template = roleMap.get( SelectedTemplate.class );
         template.addSelectedTemplate( module.unitOfWorkFactory().currentUnitOfWork().get( Attachment.class, id.string().get() ) );
      }

      public LinksValue possibletemplates( StringValue extensionFilter )
      {
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         Attachments.Data attachments = (Attachments.Data) roleMap.get( Attachments.Data.class );
         SelectedTemplate.Data template = roleMap.get( SelectedTemplate.Data.class );

         for (Attachment attachment : attachments.attachments())
         {
            if (!attachment.equals( template.selectedTemplate().get() )
                  && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
            {
               linksBuilder.addLink( ((AttachedFile.Data) attachment).name().get(), EntityReference.getEntityReference( attachment ) );
            }
         }
         return linksBuilder.newLinks();
      }

      public void delete()
      {
         SelectedTemplate template = roleMap.get( SelectedTemplate.class );
         template.removeSelectedTemplate( ((SelectedTemplate.Data) template).selectedTemplate().get() );
      }
   }
}