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

package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
@Mixins(EndUsers.Mixin.class)
public interface EndUsers
{
   AnonymousEndUser createAnonymousEndUser( );

   boolean removeAnonymousEndUser( AnonymousEndUser user );

   void addAnonymousEndUser( AnonymousEndUser user);

   interface Data
   {
      @Aggregated
      ManyAssociation<AnonymousEndUser> anonymousEndUsers();

      AnonymousEndUser createdAnonymousEndUser( DomainEvent event, String id );

      void removedAnonymousEndUser( DomainEvent event, AnonymousEndUser user );

      void addedAnonymousEndUser( DomainEvent event, AnonymousEndUser user );
   }

   abstract class Mixin
         implements EndUsers, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idgen;

      public AnonymousEndUser createAnonymousEndUser( )
      {
         String id = idgen.generate( Identity.class );

         AnonymousEndUser anonymousEndUser = createdAnonymousEndUser( DomainEvent.CREATE, id );
         addedAnonymousEndUser( DomainEvent.CREATE, anonymousEndUser );

         return anonymousEndUser;
      }

      public AnonymousEndUser createdAnonymousEndUser( DomainEvent event, String id )
      {
         EntityBuilder<AnonymousEndUser> builder = uowf.currentUnitOfWork().newEntityBuilder( AnonymousEndUser.class, id );
         return builder.newInstance();
      }

      public void addAnonymousEndUser( AnonymousEndUser user )
      {

         if (anonymousEndUsers().contains( user ))
         {
            return;
         }
         addedAnonymousEndUser( DomainEvent.CREATE, user );
      }

      public boolean removeAnonymousEndUser( AnonymousEndUser user )
      {
         if (!anonymousEndUsers().contains( user ))
            return false;

         removedAnonymousEndUser( DomainEvent.CREATE, user );
         user.removeEntity();
         return true;
      }
   }
}