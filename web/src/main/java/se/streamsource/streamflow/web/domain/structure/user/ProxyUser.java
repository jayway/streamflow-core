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
package se.streamsource.streamflow.web.domain.structure.user;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;

/**
 * A ProxyUser represents an entire external system. Actual users
 * are federated under this user. This means that only the ProxyUser
 * needs to have an actual login to the system. It will then provide
 * the identity of the user in the external system, which can then be an Actor
 * in this system.
 */
public interface ProxyUser
   extends Describable, UserAuthentication, EndUsers, OwningOrganization
{
}
