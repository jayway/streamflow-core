/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Role for maintaining descriptions of entities.
 */
@Mixins(Describable.Mixin.class)
public interface Describable
{
   void changeDescription( @Optional String newDescription );

   String getDescription();

   interface Data
   {
      @UseDefaults @Optional
      Property<String> description();

      void changedDescription( @Optional DomainEvent event, String description );
   }

   public abstract class Mixin
         implements Describable, Data
   {
      public void changeDescription( String newDescription )
      {
         if (!newDescription.equals( description().get() ))
            changedDescription( null, newDescription );
      }

      public String getDescription()
      {
         return description().get();
      }

      // State

      public void changedDescription( @Optional DomainEvent event, String description )
      {
         description().set( description );
      }
   }
}