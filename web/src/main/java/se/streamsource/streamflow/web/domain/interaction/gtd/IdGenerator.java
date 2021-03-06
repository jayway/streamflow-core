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
package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Generator for id sequences. Format is: yyyyMMdd-nnnn.
 */
@Mixins(IdGenerator.IdGeneratorMixin.class)
public interface IdGenerator
{
   void assignId( CaseId aCase );

   void setCounter( Long current );
   Long getCounter();

   void changeDate( Long timeInMillis);
   Long getDate();

   interface Data
   {
      @UseDefaults
      Property<Long> current();

      @Optional
      Property<Long> lastIdDate();

      void setCounter( @Optional DomainEvent event, long counter );

      void changedDate( @Optional DomainEvent create, long timeInMillis );
   }

   abstract class IdGeneratorMixin
         implements IdGenerator, Data
   {
      @This
      Data state;

      // Commands

      public void assignId( CaseId aCase )
      {
         // Check if we should reset the counter
         Calendar now = Calendar.getInstance();
         if (state.lastIdDate().get() != null)
         {
            Calendar lastDate = Calendar.getInstance();
            lastDate.setTimeInMillis( state.lastIdDate().get() );

            // Day has changed - reset counter
            if (now.get( Calendar.DAY_OF_YEAR ) != lastDate.get( Calendar.DAY_OF_YEAR ))
            {
               state.setCounter( null, 0 );
            }
         }
         // Save current date
         state.changedDate( null, now.getTimeInMillis() );

         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );

         long current = state.current().get();
         current++;
         setCounter( null, current );

         String date = format.format( now.getTime() );

         String caseId = date + "-" + current;
         aCase.assignId( caseId );
      }

      public void setCounter( Long current )
      {
          this.setCounter( null, current );
      }

      public Long getCounter()
      {
          return state.current().get();
      }

      public void changeDate( Long timeInMillis )
      {
          this.changedDate( null, timeInMillis );
      }

      public Long getDate()
      {
          return state.lastIdDate().get();
      }
      // Events

      public void changedDate( @Optional DomainEvent create, long timeInMillis )
      {
         state.lastIdDate().set( timeInMillis );
      }

      public void setCounter( @Optional DomainEvent event, long counter )
      {
         state.current().set( counter );
      }
   }
}
