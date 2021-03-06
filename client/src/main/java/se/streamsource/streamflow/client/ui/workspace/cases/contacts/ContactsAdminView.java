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
package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.util.RefreshComponents;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;


/**
 * JAVADOC
 */
public class ContactsAdminView
      extends JPanel
{
   public ContactsAdminView( @Uses final ContactsModel model, @Structure Module module)
   {
      super( new BorderLayout() );

      final ContactsView contactsView = module.objectBuilderFactory().newObjectBuilder(ContactsView.class).use( model ).newInstance();
      final ContactView contactView = module.objectBuilderFactory().newObject(ContactView.class);

      contactsView.getModel().addObserver( new RefreshComponents().enabledOn( "add", contactView ) );

      add( contactsView, BorderLayout.WEST );
      add( contactView, BorderLayout.CENTER );

      final JList list = contactsView.getContactsList();
      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();
               if (idx != -1)
               {
                  ContactModel contactModel = model.newContactModel(idx);
                  contactView.setModel( contactModel );

                  contactView.setFocusOnName();
               } else
               {
                  contactView.setModel( null );
               }
            }
         }
      } );

   }
}