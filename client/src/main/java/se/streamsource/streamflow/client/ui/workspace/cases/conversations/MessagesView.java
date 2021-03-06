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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import ca.odell.glazedlists.swing.EventTableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.font.TextAttribute;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class MessagesView extends JPanel implements TransactionListener
{
   private static final long serialVersionUID = -4508473068931275932L;

   @Structure
   Module module;

   @Service
   DialogService dialogs;

   private MessagesModel model;

   private JXTable messageTable;
   private JPanel detailMessagePanel;
   private JPanel sendPanel = new JPanel( new BorderLayout(  ) );

   private JPanel messageViewPanel = new JPanel( new BorderLayout(  ) );
   private MessageView messageView;
   private MessageDraftView messageDraftView;

   private StreamflowButton writeMessage;

   private javax.swing.Action  createMessage;

   public MessagesView(@Service ApplicationContext context, @Uses MessagesModel model )
   {
      setActionMap(context.getActionMap(this));
      MacOsUIWrapper.convertAccelerators(getActionMap());

      setLayout(new BorderLayout());

      this.model = model;

      // Add proxy actions
      ApplicationAction closeMessageDetailsAction = (ApplicationAction)getActionMap().get("closeMessageDetails");
      javax.swing.Action closeMessageDetails = context.getActionMap().get("closeMessageDetails");
      closeMessageDetails.putValue("proxy", closeMessageDetailsAction);

      ApplicationAction createMessageAction = (ApplicationAction)getActionMap().get("createMessage");
      createMessage = context.getActionMap().get("createMessage");
      createMessage.putValue("proxy", createMessageAction);

      ApplicationAction cancelNewMessageAction = (ApplicationAction)getActionMap().get("cancelNewMessage");
      javax.swing.Action cancelNewMessage = context.getActionMap().get("cancelNewMessage");
      cancelNewMessage.putValue("proxy", cancelNewMessageAction);

      messageTable = new JXTable(new EventJXTableModel<MessageDTO>(model.messages(), new TableFormat<MessageDTO>()
      {
         String[] columnNames = new String[]
         { "", text(sender_column_header), text(message_column_header), text(created_column_header), "" };

         public int getColumnCount()
         {
            return columnNames.length;
         }

         public String getColumnName(int i)
         {
            return columnNames[i];
         }

         public Object getColumnValue(MessageDTO o, int i)
         {
            switch (i)
            {
            case 0:
               return o.hasAttachments().get();
            case 1:
               return o.sender().get();
            case 2:
               return o.text().get();
            case 3:
               return o.createdOn().get();
            case 4:
               return o.id().get();
            }

            return null;
         }
      })){
         public Component prepareRenderer(
               TableCellRenderer renderer, int row, int column)
         {
            Component c = super.prepareRenderer(renderer, row, column);

            //  add custom rendering here
            EventTableModel model = (EventTableModel) getModel();
            if( model.getElementAt( row ) instanceof MessageDTO)
            {

               Map attributes = c.getFont().getAttributes();
               if ( ((MessageDTO) model.getElementAt( row )).unread().get() )
               {
                  attributes.put( TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD );
               }
               c.setFont( new Font(attributes) );
            }

            return c;
         }
      };

      messageTable.getColumnExt( messageTable.getColumnCount()-1 ).setVisible( false );

      messageTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      messageTable
            .setFocusTraversalKeys(
                  KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                  KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
                        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
      messageTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

      messageTable.getColumn(3).setCellRenderer(new DefaultTableRenderer(new StringValue()
      {
         private static final long serialVersionUID = -6677363096055906298L;

         public String getString(Object value)
         {
            return DateFormats.getProgressiveDateTimeValue((Date) value, Locale.getDefault());
         }
      }));
      messageTable.getColumn(0).setCellRenderer( new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {

            ImageIcon icon = i18n.icon( Icons.attachments, 11 );
            JLabel iconLabel = (Boolean) value ? new JLabel( icon ) : new JLabel( " " );
            iconLabel.setOpaque( true );

            if (isSelected)
               iconLabel.setBackground( messageTable.getSelectionBackground() );
            return iconLabel;
         }
      } );

      ListSelectionModel selectionModel = messageTable.getSelectionModel();
      selectionModel.addListSelectionListener(new MessageListSelectionHandler());

      messageTable.addHighlighter(HighlighterFactory.createAlternateStriping());

      messageTable.getColumn(0).setPreferredWidth( 20 );
      messageTable.getColumn( 0 ).setWidth( 20 );
      messageTable.getColumn(0).setMaxWidth( 20 );
      messageTable.getColumn(0).setResizable( false );
      messageTable.getColumn( 1 ).setPreferredWidth(250);
      messageTable.getColumn( 1 ).setMaxWidth( 250 );
      messageTable.getColumn(2).setPreferredWidth(300);
      messageTable.getColumn(3).setPreferredWidth(60);
      messageTable.getColumn(3).setMaxWidth(100);

      detailMessagePanel = new JPanel(new CardLayout());

      JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      javax.swing.Action writeMessageAction = getActionMap().get("writeMessage");
      writeMessage = new StreamflowButton(writeMessageAction);
      writeMessage.registerKeyboardAction(writeMessageAction,
            (KeyStroke) writeMessageAction.getValue(javax.swing.Action.ACCELERATOR_KEY),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      createPanel.add(writeMessage);

      detailMessagePanel.add( createPanel, "INITIAL");
      detailMessagePanel.add( sendPanel, "NEW_MESSAGE" );
      detailMessagePanel.add( messageViewPanel, "SHOW_MESSAGE" );

      ((CardLayout) detailMessagePanel.getLayout()).show( detailMessagePanel, "INITIAL" );

      JScrollPane scrollMessages = new JScrollPane(messageTable);

      add(scrollMessages, BorderLayout.CENTER);
      add(detailMessagePanel, BorderLayout.SOUTH);

      model.addObserver( new RefreshComponents().visibleOn( "createmessagefromdraft", writeMessage ) );

      new RefreshWhenShowing(this, model);
   }

   @Action
   public void writeMessage()
   {
      sendPanel.removeAll();
      messageDraftView = module.objectBuilderFactory().newObjectBuilder( MessageDraftView.class ).use( model.newMessageDraftModel() ).newInstance();
      sendPanel.add( messageDraftView, BorderLayout.CENTER );
      ((CardLayout) detailMessagePanel.getLayout()).show( detailMessagePanel, "NEW_MESSAGE" );

      messageDraftView.requestFocusInWindow();
   }


   @Action
   public Task createMessage()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog, text( StreamflowResources.confirmation ) );
      if( dialog.isConfirmed(  ) )
      {
         sendPanel.removeAll();
         ((CardLayout) detailMessagePanel.getLayout()).show( detailMessagePanel, "INITIAL" );
         return new CommandTask()
         {
            @Override
            public void command() throws Exception
            {
               model.createMessageFromDraft();
            }
         };
      }
      return null;
   }

   @Action
   public void cancelNewMessage()
   {
      sendPanel.removeAll();
      ((CardLayout) detailMessagePanel.getLayout()).show( detailMessagePanel, "INITIAL" );
   }

   @Action
   public void closeMessageDetails()
   {
      messageTable.getSelectionModel().clearSelection();
      messageViewPanel.removeAll();
      ((CardLayout) detailMessagePanel.getLayout()).show( detailMessagePanel, "INITIAL" );
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if ( matches(Events.withNames("createdMessage", "createdMessageFromDraft", "setUnread"), transactions)
            || matches( withUsecases( "resolve", "reopen" ),transactions ))
      {
         model.refresh();
      }
   }

   class MessageListSelectionHandler implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {

         if (!e.getValueIsAdjusting())
         {
            int index = messageTable.getSelectedRow();
            if (index >= 0)
            {
               Object selectedValue = messageTable.getModel().getValueAt( messageTable.convertRowIndexToModel( index ), messageTable.getColumnCount() );
               String href = (String) selectedValue;

               ((CardLayout) detailMessagePanel.getLayout()).show( detailMessagePanel, "INITIAL" );
               messageViewPanel.removeAll();
               messageView = module.objectBuilderFactory().newObjectBuilder( MessageView.class ).use( model.newMessageModel( href ) ).newInstance();
               EventTableModel model = (EventTableModel) messageTable.getModel();
               MessageDTO messageDTO = (MessageDTO)model.getElementAt( index );

               if( messageDTO.unread().get() )
               {
                  messageView.read();
               }

               messageViewPanel.add( messageView, BorderLayout.CENTER );
               ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "SHOW_MESSAGE");
            }
         }
      }
   }
}