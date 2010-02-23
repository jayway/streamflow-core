/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.users.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(WorkspaceProjectsContext.Mixin.class)
public interface WorkspaceProjectsContext
   extends SubContexts<WorkspaceProjectContext>, Context
{
   LinksValue projects();

   abstract class Mixin
      extends ContextMixin
      implements WorkspaceProjectsContext, SubContexts<WorkspaceProjectContext>
   {
      @Structure
      Module module;

      public LinksValue projects()
      {
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         ProjectQueries projectQueries = context.role( ProjectQueries.class);
         return linksBuilder.addDescribables( projectQueries.allProjects()).newLinks();
      }

      public WorkspaceProjectContext context( String id )
      {
         Project project = module.unitOfWorkFactory().currentUnitOfWork().get( Project.class, id );
         context.playRoles( project, Project.class );

         return subContext( WorkspaceProjectContext.class );
      }
   }
}