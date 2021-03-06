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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * JAVADOC
 */
public class GroupedFilteredList
        extends JPanel implements PropertyChangeListener
{
   private JTextField filterField;
   private JList list;
   public JScrollPane pane = new JScrollPane();

   public GroupedFilteredList()
   {
      setLayout(new BorderLayout());

      filterField = new JTextField(20);

      FilterLinkListCellRenderer filterCellRenderer = new FilterLinkListCellRenderer();
      filterCellRenderer.addPropertyChangeListener(this);
      filterField.getDocument().addDocumentListener(filterCellRenderer);

      list = new JList();

      list.setCellRenderer(new SeparatorListCellRenderer(filterCellRenderer));
      list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (list.getSelectedValue() instanceof SeparatorList.Separator)
               list.clearSelection();
         }
      });
      pane.setViewportView(list);

      add(filterField, BorderLayout.NORTH);
      add(pane, BorderLayout.CENTER);
   }

   public JTextField getFilterField()
   {
      return filterField;
   }

   public JList getList()
   {
      return list;
   }

   public JScrollPane getPane()
   {
      return pane;
   }

   public void setEventList(EventList<TitledLinkValue> eventList)
   {
      setEventList(eventList, "");
   }

   public void setEventList(EventList<TitledLinkValue> eventList, String topValue )
   {
      setEventList( eventList, topValue, true );
   }
   
   public void setEventList(EventList<TitledLinkValue> eventList, String topValue, final boolean selectMatchAlways )
   {
      SortedList<TitledLinkValue> sortedIssues = new SortedList<TitledLinkValue>(eventList, new LinkComparator());
      TextComponentMatcherEditor editor = new TextComponentMatcherEditor(filterField, new LinkFilterator());
      editor.addMatcherEditorListener(new MatcherEditor.Listener()
      {
         public void changedMatcher(MatcherEditor.Event event)
         {
            for (int i = 0; i < list.getModel().getSize(); i++)
            {
               if (list.getModel().getElementAt(i) != null && list.getModel().getElementAt(i) instanceof LinkValue)
               {
                  final int idx = i;

                  if( selectMatchAlways )
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           list.setSelectedIndex(idx);
                        }
                     });
                  }

                  break;
               }
            }
         }
      });
      final FilterList<TitledLinkValue> textFilteredIssues = new FilterList<TitledLinkValue>(sortedIssues, editor);

      EventListModel listModel = new EventListModel<TitledLinkValue>(new SeparatorList<TitledLinkValue>(textFilteredIssues, new TitledLinkGroupingComparator(topValue), 1, 10000));

      list.setModel(listModel);

      filterField.setText("");
   }

   public void propertyChange(PropertyChangeEvent evt)
   {
      if (evt.getPropertyName().equals("text"))
      {
         list.repaint();
      }
   }
}