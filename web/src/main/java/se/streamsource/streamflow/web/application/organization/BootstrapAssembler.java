/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.service.ServiceSelector;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;

/**
 * Bootstrap and testdata assembler.
 */
public class BootstrapAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addServices( BootstrapDataService.class ).instantiateOnStartup();

      if (module.layerAssembly().applicationAssembly().mode() == Application.Mode.development)
      {
         module.importServices( NamedEntityFinder.class ).importedBy( ServiceSelectorImporter.class ).setMetaInfo( ServiceSelector.withId("solr" ));
         module.addServices( TestDataService.class ).instantiateOnStartup();
      }
   }
}
