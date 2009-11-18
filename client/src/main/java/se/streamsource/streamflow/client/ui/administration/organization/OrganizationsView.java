/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.organization;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;

import javax.swing.*;
import java.awt.*;

public class OrganizationsView
        extends JPanel
{
    private OrganizationsModel model;

    JList organizationsList;
    @Structure
    ObjectBuilderFactory obf;

    public OrganizationsView(@Service ApplicationContext context, @Uses OrganizationsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        organizationsList = new JList(model);

        organizationsList.setCellRenderer(new ListItemListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(organizationsList);
        add(scrollPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(250,600));
    }

    public JList getOrganizationsList()
    {
        return organizationsList; 
    }

    public OrganizationsModel getModel()
    {
        return model;
    }
}
