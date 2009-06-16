/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Individual;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

/**
 * JAVADOC
 */
public class SharedUserNode
        extends DefaultMutableTreeTableNode
{
    private SharedUserAllInboxesNode allInboxes;
    private SharedUserAllAssignmentsNode allAssignments;
    private SharedUserAllDelegationsNode allDelegations;
    private SharedUserAllWaitingForNode allWaitingFor;

    public SharedUserNode(@Uses Individual individual,
                          @Structure ObjectBuilderFactory obf,
                          @Uses SharedUserAllInboxesNode allInboxes,
                          @Uses SharedUserAllAssignmentsNode allAssignments,
                          @Uses SharedUserAllDelegationsNode allDelegations,
                          @Uses SharedUserAllWaitingForNode allWaitingFor)
    {
        super(individual);

        this.allInboxes = allInboxes;
        this.allAssignments = allAssignments;
        this.allDelegations = allDelegations;
        this.allWaitingFor = allWaitingFor;
        add(allInboxes);
        add(allAssignments);
        add(allDelegations);
        add(allWaitingFor);
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(SharedResources.user_node);
    }

    public void refresh() 
    {
        allInboxes.refresh();
        allAssignments.refresh();
        allDelegations.refresh();
        allWaitingFor.refresh();
    }
}