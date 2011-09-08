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

package se.streamsource.streamflow.client.ui.administration.filters;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;

/**
 * Email filter action
 */
public class EmailActionView
   extends JPanel
{
   public EmailActionView(@Service ApplicationContext context, @Uses EmailActionModel model)
   {
      JLabel label = new JLabel(i18n.text(AdministrationResources.send_email_to, model.getIndex().getFirstValue("recipient")), JLabel.LEFT);
      add(label);
   }
}