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
package se.streamsource.streamflow.web.context.surface.customers.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.entity.RequiresRemoved;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * JAVADOC
 */
public class MyCasesMessagesContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;
   
   public LinksValue index()
   {
      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder( MessageDTO.class );
      ConversationEntity conversation = RoleMap.role( ConversationEntity.class );

      ResourceBundle bundle = ResourceBundle.getBundle( MessagesContext.class.getName(), RoleMap.role( Locale.class ) );
      Map<String, String> translations = new HashMap<String, String>();
      for (String key : bundle.keySet())
      {
         translations.put(key, bundle.getString(key));
      }

      for (Message message : conversation.messages())
      {
         Contactable contact = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, EntityReference.getEntityReference( ((MessageEntity) message).sender().get() ).identity() );
         String sender = contact.getContact().name().get();
         builder.prototype().sender().set( !"".equals( sender )
               ? sender
               : EntityReference.getEntityReference( ((MessageEntity) message).sender().get() ).identity() );
         builder.prototype().createdOn().set( ((MessageEntity) message).createdOn().get() );

         builder.prototype().text().set( message.translateBody(translations));
         builder.prototype().href().set( EntityReference.getEntityReference( message ).identity() + "/");
         builder.prototype().id().set( EntityReference.getEntityReference( message ).identity() );
         builder.prototype(  ).hasAttachments().set( message.hasAttachments() );

         links.addLink( builder.newInstance() );
      }
      return links.newLinks();
   }

   @RequiresRemoved(false)
   public void createmessage( StringValue message )
   {
      Messages messages = RoleMap.role( Messages.class );
      messages.createMessage( message.string().get(), MessageType.PLAIN, RoleMap.role( ConversationParticipant.class ) );
   }

}