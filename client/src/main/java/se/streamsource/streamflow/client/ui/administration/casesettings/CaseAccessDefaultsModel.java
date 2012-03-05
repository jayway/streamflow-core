/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.casesettings;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * JAVADOC
 */
public class CaseAccessDefaultsModel
      extends ResourceModel<LinksValue>
{
   public EventList<LinkValue> getPossibleDefaultAccessTypes( LinkValue linkValue )
   {
      BasicEventList<LinkValue> possibleLinks = new BasicEventList<LinkValue>();
      possibleLinks.addAll( client.queryLink( linkValue, LinksValue.class ).links().get() );
      return possibleLinks;
   }

   public void changeDefaultAccessType( LinkValue selectedLink )
   {
      client.postLink( selectedLink );
   }
}
