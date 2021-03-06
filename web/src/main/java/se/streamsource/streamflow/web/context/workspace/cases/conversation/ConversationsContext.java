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
package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.RequiresRemoved;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.*;

/**
 * JAVADOC
 */
public class ConversationsContext
      implements IndexContext<LinksValue>, CreateContext<String, Conversation>
{


   @Structure
   Module module;

   public LinksValue index()
   {
      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<ConversationDTO> builder = module.valueBuilderFactory().newValueBuilder( ConversationDTO.class );

      Conversations.Data conversations = RoleMap.role( Conversations.Data.class );

      for (Conversation conversation : conversations.conversations())
      {
         builder.prototype().creationDate().set( conversation.createdOn().get() );
         builder.prototype().creator().set( ((Describable) conversation.createdBy().get()).getDescription() );
         builder.prototype().messages().set( ((Messages.Data) conversation).messages().count() );
         builder.prototype().participants().set( ((ConversationParticipants.Data) conversation).participants().count() );
         builder.prototype().href().set( EntityReference.getEntityReference( conversation ).identity() );
         builder.prototype().text().set( conversation.getDescription() );
         builder.prototype().id().set( EntityReference.getEntityReference( conversation ).identity() );
         builder.prototype().unread().set( conversation.hasUnreadMessage() );

         links.addLink( builder.newInstance() );
      }
      return links.newLinks();
   }

   @RequiresRemoved(false)
   @RequiresStatus( OPEN )
   @RequiresPermission(PermissionType.write)
   public Conversation create( @MaxLength(50) @Name("topic") String topic )
   {
      Conversations conversations = RoleMap.role( Conversations.class );
      Conversation conversation = conversations.createConversation( topic, RoleMap.role( Creator.class ) );
      ((ConversationEntity) conversation).addParticipant( RoleMap.role( ConversationParticipant.class ) );
      return conversation;
   }

}