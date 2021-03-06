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
package se.streamsource.streamflow.infrastructure.event.domain.replay;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;

/**
 * Service that can replay transactions and individual domain events.
 */
public interface DomainEventPlayer
{
   public void playTransaction( TransactionDomainEvents transactionDomain )
         throws EventReplayException;

   /**
    * Invoke a domain event on a particular object. The object could
    * be the original object, but could also be a service that wants
    * to be invoked to handle the event.
    *
    * @param domainEvent
    * @param object
    * @throws EventReplayException
    */
   public void playEvent( DomainEvent domainEvent, Object object )
         throws EventReplayException;
}