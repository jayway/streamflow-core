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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormView
    extends JPanel
{
    private AdministrationView adminView;
    private FormValue form;


    public FormView(@Service ApplicationContext context,
                    @Uses FormValue form,
                    @Uses AdministrationView adminView)
    {
        super(new BorderLayout());

        ActionMap am = context.getActionMap(this);

        this.adminView = adminView;
        this.form = form;

        JTextArea textArea = new JTextArea(form.note().get());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(textArea, BorderLayout.CENTER);
        add(new JButton(am.get("edit")), BorderLayout.SOUTH);

    }

    @org.jdesktop.application.Action
    public void edit()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JLabel(form.description().get()), BorderLayout.NORTH);
        panel.add(new JTextArea(form.note().get()), BorderLayout.CENTER);

        for (ListItemValue value : form.fields().get().items().get())
        {
        }

        adminView.show( panel );
    }

}
