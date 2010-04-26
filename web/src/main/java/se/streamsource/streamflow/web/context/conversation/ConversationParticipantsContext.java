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

package se.streamsource.streamflow.web.context.conversation;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationParticipantsQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ConversationParticipantsContext.Mixin.class)
public interface ConversationParticipantsContext
   extends SubContexts<ConversationParticipantContext>, IndexInteraction<LinksValue>, Interactions
{
   public void addparticipant( EntityReferenceDTO participantId);
   public LinksValue possibleparticipants();

   abstract class Mixin
      extends InteractionsMixin
      implements ConversationParticipantsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public LinksValue index()
      {
         return new LinksBuilder(module.valueBuilderFactory()).rel( "participant" ).addDescribables( context.get( ConversationParticipants.Data.class ).participants()).newLinks();
      }

      public void addparticipant( EntityReferenceDTO participantId)
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ConversationParticipant participant = uow.get( ConversationParticipant.class, participantId.entity().get().identity() );

         ConversationParticipants participants = context.get(ConversationParticipants.class);

         participants.addParticipant( participant );
      }

      public LinksValue possibleparticipants()
      {
         Ownable.Data ownable = context.get(Ownable.Data.class);
         Owner owner = ownable.owner().get();
         List<ConversationParticipant> possibleParticipants = context.get( ConversationParticipantsQueries.class).possibleParticipants(owner);
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addparticipant" );

         for (ConversationParticipant possibleParticipant : possibleParticipants)
         {
            String group = "" + Character.toUpperCase( possibleParticipant.getDescription().charAt( 0 ) );
            linksBuilder.addDescribable( possibleParticipant, group );
         }

         return linksBuilder.newLinks();
      }


      public ConversationParticipantContext context( String id )
      {
         context.set(uowf.currentUnitOfWork().get( ConversationParticipant.class, id ));
         return subContext( ConversationParticipantContext.class );
      }
   }
}