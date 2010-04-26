/**
 *
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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;

/**
 * JAVADOC
 */
public class OverviewAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( OverviewWindow.class ).visibleIn( Visibility.layer );

      module.addObjects(
            OverviewNode.class,
            OverviewProjectsNode.class,
            OverviewProjectNode.class,
            OverviewProjectAssignmentsNode.class).visibleIn( Visibility.layer );

      UIAssemblers.addMV( module,
            OverviewModel.class,
            OverviewView.class );

      UIAssemblers.addMV( module,
            OverviewSummaryModel.class,
            OverviewSummaryView.class );
   }
}