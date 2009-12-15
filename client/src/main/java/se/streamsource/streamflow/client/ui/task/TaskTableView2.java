/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.task.TaskDTO;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for all views of task lists.
 */
public class TaskTableView2
      extends JPanel
{
   @Service
   protected DialogService dialogs;

   @Service
   protected StreamFlowApplication application;

   protected JXTable taskTable;
   protected TaskCreationNode taskCreation;
   protected TaskTableModel2 model;
   private TasksDetailView detailsView;
   protected EntityReference dialogSelection;

   public void init( @Service ApplicationContext context,
                     @Uses @Optional TaskCreationNode node,
                     @Uses final TasksModel tasksModel,
                     @Uses final TaskTableModel2 model,
                     @Uses final TasksDetailView detailsView,
                     @Uses TableFormat tableFormat)
   {
      this.taskCreation = node;
      this.model = model;
      this.detailsView = detailsView;
      setLayout( new BorderLayout() );
      final JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
      splitPane.setOneTouchExpandable( true );
      add( splitPane, BorderLayout.CENTER );

      ActionMap am = context.getActionMap( TaskTableView2.class, this );
      setActionMap( am );

      // Toolbar
      JPanel toolbar = new JPanel();

      // Table
      EventJXTableModel tableModel = new EventJXTableModel(model.getEventList(), tableFormat);
      taskTable = new JXTable( tableModel );
      taskTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      taskTable.getActionMap().getParent().setParent( am );
      taskTable.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      taskTable.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      JScrollPane taskScrollPane = new JScrollPane( taskTable );

      add( toolbar, BorderLayout.NORTH );

      taskTable.getColumn( 1 ).setPreferredWidth( 150 );
      taskTable.getColumn( 1 ).setMaxWidth( 150 );
      taskTable.getColumn( 2 ).setPreferredWidth( 150 );
      taskTable.getColumn( 2 ).setMaxWidth( 150 );
      taskTable.getColumn( taskTable.getColumnCount() - 1 ).setCellRenderer( new TaskStatusTableCellRenderer() );
      taskTable.getColumn( taskTable.getColumnCount() - 1 ).setMaxWidth( 40 );
      taskTable.getColumn( taskTable.getColumnCount() - 1 ).setResizable( false );

      TableColumn column = taskTable.getColumnModel().getColumn( taskTable.getColumnCount()-1 );
      column.setCellRenderer( new TaskStatusTableCellRenderer() );

      taskTable.setAutoCreateColumnsFromModel( false );

      splitPane.setTopComponent( taskScrollPane );
      splitPane.setBottomComponent( detailsView );
      splitPane.setResizeWeight( 0.3D );

      JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
      taskTable.setDefaultEditor( Boolean.class, completableEditor );
      taskTable.setDefaultRenderer( Date.class, new DefaultTableRenderer( new StringValue()
      {
         private SimpleDateFormat format = new SimpleDateFormat();

         public String getString( Object value )
         {
            if (value == null) return "";
            Date time = (Date) value;
            return format.format( time );
         }
      } ) );
      taskTable.setDefaultRenderer( ImageIcon.class, new TaskStatusTableCellRenderer() );

      taskTable.addHighlighter( HighlighterFactory.createAlternateStriping() );
      taskTable.addHighlighter( new ColorHighlighter( new HighlightPredicate()
      {
         public boolean isHighlighted( Component component, ComponentAdapter componentAdapter )
         {
            return componentAdapter != null && componentAdapter.getValue( 2 ).equals( TaskStates.DROPPED);
         }
      }, Color.black, Color.lightGray ) );
      taskTable.setEditable( true );

      buildToolbar( toolbar );

      taskTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         TaskDTO selectedTask;

         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               try
               {
                  if (taskTable.getSelectionModel().isSelectionEmpty())
                  {
                     detailsView.removeCurrent();
                  } else
                  {
                     TaskDTO dto = null;
                     try
                     {
                        dto = getSelectedTask();
                     } catch (Exception e1)
                     {
                        // Ignore
                        return;
                     }

                     if (dto == selectedTask)
                        return;

                     selectedTask = dto;

                     TaskModel taskModel = tasksModel.task( dto.task().get().identity() );

                     detailsView.show( taskModel );
                  }
               } catch (Exception e1)
               {
                  throw new OperationException( TaskResources.could_not_view_details, e1 );
               }
            }
         }
      } );
      splitPane.setDividerLocation( 1D );

      addFocusListener( new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            taskTable.requestFocusInWindow();
         }
      } );
   }

   protected void buildToolbar( JPanel toolbar )
   {
      if (taskCreation != null)
         addToolbarButton( toolbar, "createTask" );
      addToolbarButton( toolbar, "refresh" );
   }

   protected Action addToolbarButton( JPanel toolbar, String name )
   {
      ActionMap am = getActionMap();
      Action action = am.get( name );
      action.putValue( Action.SMALL_ICON, i18n.icon( (ImageIcon) action.getValue( Action.SMALL_ICON ), 16 ) );
      toolbar.add( new JButton( action ) );
      return action;
   }

   public JXTable getTaskTable()
   {
      return taskTable;
   }

   public TasksDetailView getTaskDetails()
   {
      return detailsView;
   }

   public TaskDTO getSelectedTask()
   {
      int selectedRow = getTaskTable().getSelectedRow();
      if (selectedRow == -1)
         return null;
      else
         return model.getEventList().get( getTaskTable().convertRowIndexToModel( selectedRow ) );
   }

   @org.jdesktop.application.Action()
   public void createTask() throws ResourceException
   {
      taskCreation.createTask();
      model.refresh();

/*
      JXTable table = getTaskTable();
      int index = model.getRowCount() - 1;
      table.getSelectionModel().setSelectionInterval( index, index );
      table.scrollRowToVisible( index );

      TaskDetailView taskDetail = (TaskDetailView) detailsView.getComponentAt( 0 );
      taskDetail.setSelectedIndex( 0 );
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            Component component1 = detailsView.getSelectedComponent();
            if (component1 != null)
               component1.requestFocusInWindow();
         }
      } );
*/
   }

   @org.jdesktop.application.Action
   public void refresh() throws ResourceException
   {
      model.refresh();
   }
}