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
package se.streamsource.streamflow.client.ui;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.util.GroupedFilteredList;
import se.streamsource.streamflow.client.util.i18n;
import ca.odell.glazedlists.EventList;

/**
 * JAVADOC
 */
public class SelectUsersAndGroupsDialog
      extends JPanel
{
   private GroupedFilteredList groupList;
   private GroupedFilteredList userList;

   private Set<LinkValue> selectedEntities;

   public SelectUsersAndGroupsDialog( @Service ApplicationContext context,
                                      @Uses UsersAndGroupsModel model)
   {
      super( new GridLayout(1, 2) );
      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      setName( i18n.text( AdministrationResources.search_users_or_groups) );

      selectedEntities = new HashSet<LinkValue>();
      EventList<TitledLinkValue> groups = model.getPossibleGroups();

      groupList = new GroupedFilteredList();
      groupList.setEventList( groups );
      groupList.setBorder( BorderFactory.createTitledBorder( i18n.text( AdministrationResources.group_title ) ));

      add( groupList );

      EventList<TitledLinkValue> users = model.getPossibleUsers();

      userList = new GroupedFilteredList();
      userList.setEventList(users);
      userList.setBorder( BorderFactory.createTitledBorder( i18n.text( AdministrationResources.user_title ) ));

      add( userList );
   }

   public Set<LinkValue> getSelectedEntities()
   {
      return selectedEntities;
   }

   @Action
   public void execute()
   {
      for (Object value : groupList.getList().getSelectedValues())
      {
         selectedEntities.add( (LinkValue) value );
      }

      for (Object value : userList.getList().getSelectedValues())
      {
         selectedEntities.add( (LinkValue) value );
      }
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}