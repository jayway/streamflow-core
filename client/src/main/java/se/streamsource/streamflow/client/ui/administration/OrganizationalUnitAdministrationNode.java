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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitClientResource;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * JAVADOC
 */
public class OrganizationalUnitAdministrationNode
        extends DefaultMutableTreeNode implements Transferable, EventListener
{
    @Structure
    ObjectBuilderFactory obf;

    WeakModelMap<TreeNodeValue, OrganizationalUnitAdministrationNode> models = new WeakModelMap<TreeNodeValue, OrganizationalUnitAdministrationNode>()
    {
        @Override
        protected OrganizationalUnitAdministrationNode newModel(TreeNodeValue key)
        {
            OrganizationalUnitClientResource resource = orgResource.organizationalUnits().organizationalUnit( key.entity().get().identity() );
            return obf.newObjectBuilder( OrganizationalUnitAdministrationNode.class).use( OrganizationalUnitAdministrationNode.this, key, resource).newInstance();
        }
    };

    OrganizationalUnitAdministrationModel model;

    OrganizationalUnitClientResource orgResource;

    public OrganizationalUnitAdministrationNode(@Uses TreeNode parent, @Uses TreeNodeValue ou, @Uses OrganizationalUnitClientResource ouResource, @Structure ObjectBuilderFactory obf) throws ResourceException
    {
        super(ou.buildWith().prototype());
        this.orgResource = ouResource;

        model = obf.newObjectBuilder(OrganizationalUnitAdministrationModel.class).use(ouResource).newInstance();

        for (TreeNodeValue treeNodeValue : ou.children().get())
        {
            OrganizationalUnitClientResource resource = orgResource.organizationalUnits().organizationalUnit( treeNodeValue.entity().get().identity() );
            add(obf.newObjectBuilder( OrganizationalUnitAdministrationNode.class).use(this, treeNodeValue, resource).newInstance());
        }
    }

    @Override
    public String toString()
    {
        return ou().description().get();
    }

    public TreeNodeValue ou()
    {
        return (TreeNodeValue) getUserObject();
    }

    @Override
    public void setUserObject(Object userObject)
    {
        model.changeDescription(userObject.toString());
        ou().description().set(userObject.toString());
    }

    public OrganizationalUnitAdministrationModel model()
    {
        return model;
    }

    public DataFlavor[] getTransferDataFlavors() {

        DataFlavor[] result = {new DataFlavor( OrganizationalUnitAdministrationNode.class,"OrganizationalStructureNode")};
        return result;
    }

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return "OrganizationalStructureNode".equals(dataFlavor.getHumanPresentableName());
    }

    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {

        return ((OrganizationalUnitAdministrationNode)parent).ou().entity().get();
    }

    public void notifyEvent( DomainEvent event )
    {
        model.notifyEvent(event);

        for (OrganizationalUnitAdministrationNode organizationalUnitAdministrationNode : models)
        {
            organizationalUnitAdministrationNode.notifyEvent( event );
        }
    }
}