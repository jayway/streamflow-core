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
package se.streamsource.streamflow.web.domain;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Generic interface for removing objects. They are not
 * physically removed, but are instead only marked as removed.
 * All state still exists and can be queried.
 * <p/>
 * Queries for entities that include this one should ensure that the removed flag is set to false
 * before allowing it to be included.
 */
@Mixins(Removable.Mixin.class)
public interface Removable
{
   /**
    * Mark the entity as removed
    *
    * @return true if the entity was removed. False if the entity was already marked as removed
    */
   boolean removeEntity();

   /**
    * Mark the entity as not-removed
    *
    * @return true if the entity was reinstate. False if the entity was already active.
    */
   boolean reinstate();

   void deleteEntity();

   interface Data
   {
      @UseDefaults
      Property<Boolean> removed();

      void changedRemoved( @Optional DomainEvent event, boolean isRemoved );

      void deletedEntity( @Optional DomainEvent event);
   }

   abstract class Mixin
         implements Removable, Data
   {
      @This
      Data state;

      @Structure
      Module module;

      public boolean removeEntity()
      {
         if (!state.removed().get())
         {
            state.changedRemoved( null, true );
            return true;
         } else
         {
            return false;
         }
      }

      public void changedRemoved( DomainEvent event, boolean isRemoved )
      {
         removed().set( isRemoved );
      }

      public boolean reinstate()
      {
         if (state.removed().get())
         {
            state.changedRemoved( null, false );
            return true;
         } else
         {
            return false;
         }
      }

      public void deleteEntity()
      {
         state.deletedEntity( null );
      }

      public void deletedEntity( DomainEvent event )
      {
         module.unitOfWorkFactory().currentUnitOfWork().remove( state );
      }
   }
}
