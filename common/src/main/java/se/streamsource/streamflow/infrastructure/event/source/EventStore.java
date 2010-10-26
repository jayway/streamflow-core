/*
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

package se.streamsource.streamflow.infrastructure.event.source;

import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;

import java.io.IOException;

/**
 * Store of domain-events. Internally it stores TransactionEvents, which is a timestamped
 * list of DomainEvents. The timestamp is when the TransactionEvents was created. Moving
 * events from one store to another will create new TransactionEvents with new timestamps.
 */
public interface EventStore
{
   TransactionEvents storeEvents(Iterable<DomainEvent> events) throws IOException;
}
