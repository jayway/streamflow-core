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

package se.streamsource.streamflow.web.application.management;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.rest.MBeanServerImporter;

import javax.management.MBeanServer;

/**
 * JAVADOC
 */
public class ManagementAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        if (module.layerAssembly().applicationAssembly().mode().equals(Application.Mode.production))
        {
            module.addObjects(CompositeMBean.class);
            module.addTransients(ManagerComposite.class);

//            module.addServices(CustomJMXConnectorService.class);
            module.importServices(MBeanServer.class).importedBy(MBeanServerImporter.class);
            module.addServices(ManagerService.class).instantiateOnStartup();
            
            module.addServices(ReindexerService.class).identifiedBy("reindexer");
            module.addServices(ReindexOnStartupService.class).instantiateOnStartup();

            module.addServices(EventManagerService.class).instantiateOnStartup();
            module.addServices(ErrorLogService.class).instantiateOnStartup();
        }
    }
}
