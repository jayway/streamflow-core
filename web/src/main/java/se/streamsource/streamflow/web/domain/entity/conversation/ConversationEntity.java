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
package se.streamsource.streamflow.web.domain.entity.conversation;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageDraft;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * JAVADOC
 */
@Concerns({ConversationEntity.CaseLogMessageConcern.class, ConversationEntity.RemovableConcern.class})
public interface ConversationEntity
   extends
      Conversation,
      ConversationParticipants.Data,
      Describable.Data,
      Messages.Data,
      MessageDraft.Data,
      Attachments.Data,
      Removable.Data,

      ConversationParticipantsQueries,

      DomainEntity
{
   

   abstract class CaseLogMessageConcern
   extends ConcernOf<Messages>
   implements Messages
   {
      @This
      Conversation conversation;
      
      public Message createMessage(String body, MessageType messageType, ConversationParticipant participant)
      {
         CaseLoggable.Data caseLoggable = RoleMap.role( CaseLoggable.Data.class );
         caseLoggable.caselog().get().addTypedEntry( "{createMessage,topic=" + conversation.getDescription() + "}" , CaseLogEntryTypes.conversation);
         return next.createMessage( body, messageType, participant );
      }

      public Message createMessage( String body, MessageType messageType, ConversationParticipant participant, boolean unread )
      {
         CaseLoggable.Data caseLoggable = RoleMap.role( CaseLoggable.Data.class );
         caseLoggable.caselog().get().addTypedEntry( "{createMessage,topic=" + conversation.getDescription() + "}" , CaseLogEntryTypes.conversation);
         return next.createMessage( body, messageType, participant, unread );
      }

      public void createMessageFromDraft( ConversationParticipant participant )
      {
         CaseLoggable.Data caseLoggable = RoleMap.role( CaseLoggable.Data.class );
         caseLoggable.caselog().get().addTypedEntry( "{createMessage,topic=" + conversation.getDescription() + "}" , CaseLogEntryTypes.conversation);
         next.createMessageFromDraft( participant );
      }

   }

   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @This
      Messages.Data messages;

      public void deleteEntity()
      {
         for( Message message : messages.messages() )
         {
            message.deleteEntity();
         }
         next.deleteEntity();
      }

      public boolean removeEntity()
      {
         for( Message message : messages.messages() )
         {
            message.removeEntity();
         }
         return next.removeEntity();
      }
   }
}
