/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.web.context.conversation.ConversationContext;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

/**
 * JAVADOC
 */
@Mixins(ConversationsContext.Mixin.class)
public interface ConversationsContext
   extends
      SubContexts<ConversationContext>, IndexContext<LinksValue>, Context
{
   public void create( StringValue topic );

   abstract class Mixin
      extends ContextMixin
      implements ConversationsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<ConversationDTO> builder = module.valueBuilderFactory().newValueBuilder( ConversationDTO.class );

         Conversations.Data conversations = context.role( Conversations.Data.class );

         for (Conversation conversation : conversations.conversations())
         {
            builder.prototype().creationDate().set( conversation.createdOn().get() );
            builder.prototype().creator().set( ((Describable)conversation.createdBy().get() ).getDescription() );
            builder.prototype().messages().set( ((Messages.Data)conversation).messages().count() );
            builder.prototype().participants().set( ((ConversationParticipants.Data)conversation).participants().count() );
            builder.prototype().href().set( EntityReference.getEntityReference( conversation ).identity() );
            builder.prototype().text().set( conversation.getDescription() );
            builder.prototype().id().set( EntityReference.getEntityReference( conversation ).identity() );

            links.addLink( builder.newInstance() );
         }
         return links.newLinks();
      }

      public void create( StringValue topic )
      {
         Conversations conversations = context.role(Conversations.class);
         Conversation conversation = conversations.createConversation( topic.string().get(), context.role( Creator.class) );
         ((ConversationEntity)conversation).addParticipant( context.role( ConversationParticipant.class ) );

      }

      public ConversationContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get( Conversation.class, id ) );

         return subContext( ConversationContext.class );
      }
   }
}