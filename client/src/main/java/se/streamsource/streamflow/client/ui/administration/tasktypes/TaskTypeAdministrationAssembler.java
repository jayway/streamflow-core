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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FormsView;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FormModel;
import se.streamsource.streamflow.client.ui.administration.projects.forms.FormView;

/**
 * JAVADOC
 */
public class TaskTypeAdministrationAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        UIAssemblers.addViews(module, TaskTypesAdminView.class);

        UIAssemblers.addDialogs( module, SelectTaskTypesDialog.class );

        UIAssemblers.addMV( module, SelectedTaskTypesModel.class, SelectedTaskTypesView.class );

       UIAssemblers.addMV(module, FormsModel.class, FormsView.class);

       UIAssemblers.addMV(module, FormModel.class, FormView.class);

        UIAssemblers.addMV(module, TaskTypesModel.class,
                TaskTypesView.class);

        UIAssemblers.addMV(module, TaskTypeModel.class, TaskTypeView.class);
    }
}