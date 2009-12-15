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

package se.streamsource.streamflow.domain;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import se.streamsource.streamflow.domain.contact.ContactAssembler;
import se.streamsource.streamflow.domain.form.FormAssembler;
import se.streamsource.streamflow.domain.task.TaskActions;

/**
 * JAVADOC
 */
public class CommonDomainAssembler
{
   public void assemble( LayerAssembly domainLayer ) throws AssemblyException
   {
      new ContactAssembler().assemble( domainLayer.moduleAssembly( "Contact" ) );
      new FormAssembler().assemble( domainLayer.moduleAssembly( "Form" ) );

      domainLayer.moduleAssembly( "Task" ).addValues( TaskActions.class ).visibleIn( Visibility.application );
   }
}
