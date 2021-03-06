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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Locale;

public class ConversationsListCellRenderer implements ListCellRenderer
{
   DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

   public Component getListCellRendererComponent(final JList list, Object value, int index, boolean isSelected,
         boolean cellHasFocus)
   {
      // Tweak to make it possible to have different heights of list elements.
      list.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent lse)
         {
            if (!lse.getValueIsAdjusting())
               list.setCellRenderer(new ConversationsListCellRenderer());
         }
      });

      if (list instanceof JXList)
      {
         ((JXList) list).addHighlighter(HighlighterFactory.createAlternateStriping());
      }

      ConversationDTO conversation = (ConversationDTO) value;

      JPanel renderer = new JPanel(new BorderLayout());
      renderer.setLayout(new BorderLayout());
      renderer.setBorder(new EmptyBorder(3, 3, 3, 3));

      // Layout and form for the header panel
      FormLayout headerLayout = new FormLayout("40dlu:grow, pref, 3dlu, pref", "pref");
      JPanel headerPanel = new JPanel(headerLayout);
      headerPanel.setFocusable(false);
      DefaultFormBuilder headerBuilder = new DefaultFormBuilder(headerLayout, headerPanel);

      // Title
      JLabel title = new JLabel(conversation.text().get());
      title.setFont( title.getFont().deriveFont( Font.BOLD ) );
      headerBuilder.add( title );
      headerBuilder.nextColumn();

      // Participants
      JLabel participants = new JLabel(String.valueOf(conversation.participants().get()), i18n.icon(
            Icons.participants, 16), JLabel.LEADING);
      headerBuilder.add(participants);
      headerBuilder.nextColumn(2);

      // Conversations
      JLabel conversationsLabel = new JLabel(String.valueOf(conversation.messages().get()), i18n.icon(
            conversation.unread().get() ? Icons.unreadconversations : Icons.conversations, 16), JLabel.LEADING);
      headerBuilder.add(conversationsLabel);
      renderer.add(headerPanel, BorderLayout.NORTH);

      if (isSelected)
      {
         // Layout and form for the content panel
         JPanel contentPanel = new JPanel(new BorderLayout());
         contentPanel.setFocusable(false);

         JPanel ingressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

         // Conversation creation date
         JLabel labelDate = new JLabel(DateFormats.getFullDateTimeValue( conversation.creationDate().get(),
               Locale.getDefault() ));
         labelDate.setFont(labelDate.getFont().deriveFont(Font.ITALIC));
         ingressPanel.add( labelDate );

         // Creator
         JLabel labelCreator = new JLabel(conversation.creator().get());
         ingressPanel.add(labelCreator);
         contentPanel.add(ingressPanel, BorderLayout.NORTH);
         renderer.add(contentPanel, BorderLayout.CENTER);
      } else
      {
         renderer.setPreferredSize(headerPanel.getPreferredSize());
         renderer.setBackground(Color.WHITE);
         headerPanel.setBackground(Color.WHITE);
      }

      if (list.getSelectedIndex() == index)
      {
         renderer.setPreferredSize(new Dimension(200, 40));
      } else
      {
         renderer.setPreferredSize(new Dimension(200, 20));
      }

      return renderer;
   }
}
