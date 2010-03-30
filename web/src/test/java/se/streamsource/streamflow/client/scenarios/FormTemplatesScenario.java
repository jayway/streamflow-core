/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.scenarios;

import se.streamsource.streamflow.client.application.shared.steps.FieldDefinitionsSteps;
import se.streamsource.streamflow.client.application.shared.steps.FormsSteps;
import se.streamsource.streamflow.client.application.shared.steps.GroupsSteps;
import se.streamsource.streamflow.client.application.shared.steps.MembersSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationalUnitsSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ParticipantsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ProjectsSteps;
import se.streamsource.streamflow.client.application.shared.steps.TaskTypesSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.TestSetupSteps;
import se.streamsource.streamflow.test.AbstractWebDomainApplicationScenario;

/**
 * JAVADOC
 */
public class FormTemplatesScenario
      extends AbstractWebDomainApplicationScenario
{
   public FormTemplatesScenario()
   {
      this( Thread.currentThread().getContextClassLoader() );
   }

   public FormTemplatesScenario( ClassLoader classLoader )
   {
      super( classLoader, new TestSetupSteps(),
            new OrganizationsSteps(),
            new OrganizationalUnitsSteps(),
            new FieldDefinitionsSteps(),
            new ParticipantsSteps(),
            new ProjectsSteps(),
            new GroupsSteps(),
            new MembersSteps(),
            new TaskTypesSteps(),
            new FormsSteps());
   }
}