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
package se.streamsource.streamflow.web.context.administration;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.role.Roles;

/**
 * JAVADOC
 */
public class AdministratorContext
   implements DeleteContext
{
   public void delete()
   {
      RolePolicy role = RoleMap.role(RolePolicy.class);

      Participant participant = RoleMap.role(Participant.class);

      Roles roles = RoleMap.role(Roles.class);
      Role adminRole = roles.getAdministratorRole();

      role.revokeRole( participant, adminRole );
   }
}