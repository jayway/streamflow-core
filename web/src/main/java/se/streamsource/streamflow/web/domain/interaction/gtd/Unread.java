/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * This interface is used to determine if an implementor has been read( acknowledged ) or not.
 */
@Mixins(Unread.Mixin.class)
public interface Unread
{
   public void setUnread( boolean unread );
   public boolean isUnread();

   interface Data
   {
      @UseDefaults
      Property<Boolean> unread();

      void setUnread( @Optional DomainEvent event, boolean unread );
   }

   abstract class Mixin
      implements Unread, Data

   {
      @This
      Data data;

      public void setUnread( boolean unread )
      {
         setUnread( null, unread );
      }

      public boolean isUnread()
      {
         return data.unread().get();
      }

      public void setUnread( DomainEvent event, boolean unread )
      {
         data.unread().set( unread );
      }
   }
}
