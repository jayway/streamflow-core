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
package se.streamsource.streamflow.web.application.attachment;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.replay.EventReplayException;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventRouter;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTracker;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.util.Date;

/**
 * Takes care of Attachement and AttachedFile removal.
 */
@Mixins(RemoveAttachmentsService.Mixin.class)
public interface RemoveAttachmentsService
      extends ServiceComposite, Activatable, Configuration<RemoveAttachmentsConfiguration>
{

   abstract class Mixin
         implements Activatable, AttachedFile.Data//Removable.Data
   {
      final Logger logger = LoggerFactory.getLogger( RemoveAttachmentsService.class.getName() );

      @Service
      private EventSource eventSource;

      @Service
      private EventStream stream;

      @Structure
      private Module module;

      @This
      private Configuration<RemoveAttachmentsConfiguration> config;

      private TransactionTracker tracker;

      @Service
      DomainEventPlayer player;

      @Service
      AttachmentStore store;

      public void activate() throws Exception
      {
         EventRouter router = new EventRouter();
         router.route( Specifications.and( Events.onEntityTypes( AttachmentEntity.class.getName() ), /*Events.withNames( Removable.Data.class ),*/ Events.withNames( "deletedFile" ) ), Events.playEvents( player, this, module.unitOfWorkFactory(), UsecaseBuilder.newUsecase( "Delete attachment files" ) ) );

         tracker = new TransactionTracker( stream, eventSource, config, Events.adapter( router ) );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public void deletedFile( @Optional DomainEvent event, String uri )
      {
         try
         {
            if (uri.startsWith( "store:" ))
            {
               String id = uri.substring( "store:".length() );
               store.deleteAttachment( id );
               logger.info( "Removed file: " + uri );

            } else
            {
               // Handle external storage of file
            }

         } catch (IOException ioe)
         {
            logger.error( "Failed to remove attachment:" + uri, ioe );
            throw new EventReplayException( event, ioe );
         }
      }
      public void changedName( @Optional DomainEvent event, String newName){}
      public void changedMimeType( @Optional DomainEvent event, String newMimeType){}
      public void changedUri( @Optional DomainEvent event, String newMimeType){}
      public void changedModificationDate( @Optional DomainEvent event, Date newModificationDate){}
      public void changedSize(@Optional DomainEvent event, long size){}
   }
}
