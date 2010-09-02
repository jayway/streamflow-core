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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.PinstripePainter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.resource.caze.CaseValue;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Base class for all views of case lists.
 */
public class CaseTableView
      extends JPanel
{
   @Service
   protected DialogService dialogs;

   @Service
   protected StreamflowApplication application;

   protected JXTable caseTable;
   protected CasesTableModel model;
   private CasesDetailView2 detailsView;

   public void init( @Service ApplicationContext context,
                     @Uses final CasesModel casesModel,
                     @Uses final CasesTableModel model,
                     @Uses final CasesDetailView2 detailsView,
                     @Uses TableFormat tableFormat )
   {
      setLayout( new BorderLayout() );
      this.model = model;
      this.detailsView = detailsView;
      setLayout( new BorderLayout() );

      ActionMap am = context.getActionMap( CaseTableView.class, this );
      setActionMap( am );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CaseTableView.class, this ) );

      // Table
      EventJXTableModel tableModel = new EventJXTableModel( model.getEventList(), tableFormat );
      caseTable = new JXTable( tableModel );
      caseTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      caseTable.getActionMap().getParent().setParent( am );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      caseTable.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      JScrollPane caseScrollPane = new JScrollPane( caseTable );

      caseTable.getColumn( 1 ).setPreferredWidth( 70 );
      caseTable.getColumn( 1 ).setMaxWidth( 100 );
      caseTable.getColumn( 2 ).setPreferredWidth( 150 );
      caseTable.getColumn( 2 ).setMaxWidth( 150 );
      caseTable.getColumn( 3 ).setPreferredWidth( 150 );
      caseTable.getColumn( 3 ).setMaxWidth( 150 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setMaxWidth( 50 );
      caseTable.getColumn( caseTable.getColumnCount() - 1 ).setResizable( false );

      caseTable.setAutoCreateColumnsFromModel( false );

      add( caseScrollPane, BorderLayout.CENTER );

      JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
      caseTable.setDefaultEditor( Boolean.class, completableEditor );
      caseTable.setDefaultRenderer( Date.class, new DefaultTableRenderer( new StringValue()
      {
         private SimpleDateFormat format = new SimpleDateFormat();

         public String getString( Object value )
         {
            if (value == null) return "";
            Date time = (Date) value;
            return format.format( time );
         }
      } ) );
      caseTable.setDefaultRenderer( ArrayList.class, new DefaultTableRenderer()
      {

         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {
            JPanel renderer = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            //BoxLayout box = new BoxLayout( renderer, BoxLayout.X_AXIS );

            ArrayList<String> icons = (ArrayList<String>) value;
            for (String icon : icons)
            {
               ImageIcon image = i18n.icon( Icons.valueOf( icon ), 11 );
               JLabel iconLabel = image != null ? new JLabel( image, SwingConstants.LEADING ) : new JLabel( "   " );
               renderer.add( iconLabel );
            }
            if (isSelected)
               renderer.setBackground( table.getSelectionBackground() );
            return renderer;
         }
      } );
      caseTable.setDefaultRenderer( CaseStates.class, new CaseStatusTableCellRenderer() );

      caseTable.addHighlighter( HighlighterFactory.createAlternateStriping() );

      PinstripePainter p = new PinstripePainter();
      p.setAngle( 90 );
      p.setPaint( Color.LIGHT_GRAY );

      caseTable.addHighlighter( new PainterHighlighter( new HighlightPredicate()
      {
         public boolean isHighlighted( Component component, ComponentAdapter componentAdapter )
         {
            if (componentAdapter != null)
            {
               Object value = componentAdapter.getValue( componentAdapter.getColumnCount() - 1 );
               return value.equals( value.equals( CaseStates.CLOSED ) );
            } else
               return false;
         }
      }, p ) );

      caseTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         CaseValue selectedCase;

         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               try
               {
                  if (!caseTable.getSelectionModel().isSelectionEmpty())
                  {
                     CaseValue value;
                     try
                     {
                        value = getSelectedCase();
                     } catch (Exception e1)
                     {
                        // Ignore
                        return;
                     }

                     if (value == selectedCase)
                        return;

                     selectedCase = value;

                     CaseModel caseModel = casesModel.caze( value.id().get() );

                     detailsView.show( caseModel );
                  }
               } catch (Exception e1)
               {
                  throw new OperationException( CaseResources.could_not_view_details, e1 );
               }
            }
         }
      } );

      addFocusListener( new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            caseTable.requestFocusInWindow();
         }
      } );
   }

   public JXTable getCaseTable()
   {
      return caseTable;
   }

   public CasesDetailView2 getCaseDetails()
   {
      return detailsView;
   }

   public CasesTableModel getModel()
   {
      return model;
   }

   public CaseValue getSelectedCase()
   {
      int selectedRow = getCaseTable().getSelectedRow();
      if (selectedRow == -1)
         return null;
      else
         return model.getEventList().get( getCaseTable().convertRowIndexToModel( selectedRow ) );
   }
}