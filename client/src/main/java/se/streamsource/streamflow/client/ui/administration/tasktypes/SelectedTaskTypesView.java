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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemComparator;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class SelectedTaskTypesView
        extends JPanel
{
    @Service
    DialogService dialogs;

    @Uses
    ObjectBuilder<SelectTaskTypesDialog> taskTypesDialogs;

    public JList taskTypeList;

    private SelectedTaskTypesModel modelSelected;

    public SelectedTaskTypesView(@Service ApplicationContext context, @Uses SelectedTaskTypesModel modelSelected )
    {
        super(new BorderLayout());
        this.modelSelected = modelSelected;

        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        taskTypeList = new JList( new EventListModel<ListItemValue>(new SortedList<ListItemValue>(modelSelected.getTaskTypeList(), new ListItemComparator())));

        taskTypeList.setCellRenderer(new ListItemListCellRenderer());

        add(new JScrollPane( taskTypeList ), BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);
        taskTypeList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));

        addAncestorListener( new RefreshWhenVisible( modelSelected, this) );
    }

    @Action
    public void add()
    {
        SelectTaskTypesDialog dialog = taskTypesDialogs.use( modelSelected.getPossibleTaskTypes() ).newInstance();

        dialogs.showOkCancelHelpDialog( this, dialog );

        if (dialog.getSelectedTaskTypes() != null)
        {
            for (ListItemValue listItemValue : dialog.getSelectedTaskTypes())
            {
                modelSelected.addTaskType( listItemValue.entity().get() );
            }
            modelSelected.refresh();
        }

    }

    @Action
    public void remove()
    {
        ListItemValue selected = (ListItemValue) taskTypeList.getSelectedValue();
        modelSelected.removeTaskType( selected.entity().get() );
        modelSelected.refresh();
    }
}