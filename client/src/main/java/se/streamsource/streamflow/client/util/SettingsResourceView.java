/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseArchivalSettingView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseDefaultDaysToCompleteView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseSettingsView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesView;
import se.streamsource.streamflow.client.ui.administration.filters.FiltersView;
import se.streamsource.streamflow.client.ui.administration.forms.FormsView;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormElementsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsView;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsView;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersView;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsView;
import se.streamsource.streamflow.client.ui.administration.projects.MembersView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersView;
import se.streamsource.streamflow.client.ui.administration.templates.TemplatesView;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static se.streamsource.dci.value.link.Links.withRel;
import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * Show settings for a REST resource, with each part as its own panel with a separator. To determine panels, do a query
 * to the resources directory URL ("/") to get a ResourceValue. Then iterate through the registered views and check if
 * they are in ResourceValue.resources(). By using xzm
 * the order of the registered views we ensure that the UI order of the tabs is always the same, regardless of the order
 * returned from the server.
 */
public class SettingsResourceView
      extends JPanel
{
   private static final Map<String, Class<? extends JComponent>> views = new LinkedHashMap<String, Class<? extends JComponent>>();
   private static final Map<String, Enum> settingsNames = new LinkedHashMap<String, Enum>();

   static
   {
      addSettings("caseaccessdefaults", AdministrationResources.caseaccessdefaults_separator, CaseAccessDefaultsView.class);

      addSettings("defaultdaystocomplete", AdministrationResources.default_days_to_complete_separator, CaseDefaultDaysToCompleteView.class);

      addSettings("archival", AdministrationResources.archival_settings_separator, CaseArchivalSettingView.class);
   }

   private static void addSettings(String name, Enum tabName, Class<? extends JComponent> viewClass)
   {
      settingsNames.put( name, tabName );
      views.put( name, viewClass );
   }

   public SettingsResourceView(@Uses ResourceModel model, @Structure Module module)
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      model.refresh();
      EventList<LinkValue> resources = model.getResources();
      for (Map.Entry<String, Class<? extends JComponent>> stringClassEntry : views.entrySet())
      {
         LinkValue linkedResource = Iterables.first(Iterables.filter(withRel(stringClassEntry.getKey()), resources));
         if (linkedResource != null)
         {
            String separatorText = text( settingsNames.get( stringClassEntry.getKey() ) );
            Class<? extends JComponent> tabClass = stringClassEntry.getValue();
            try
            {
               Object resourceModel = model.newResourceModel(linkedResource);

               JLabel jLabel = new JLabel(separatorText, JLabel.LEFT);
               jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
               jLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
               add(jLabel);
               JComponent view = module.objectBuilderFactory().newObjectBuilder(tabClass).use(resourceModel).newInstance();
               view.setAlignmentX(JComponent.LEFT_ALIGNMENT);
               add(view);
               view.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }

      add(Box.createVerticalGlue());
   }
}
