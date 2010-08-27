/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.dci.value;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 * Value representing a whole context in a URL path. Allows listing of available
 * queries, commands, subcontexts and an index.
 */
public interface ContextValue
   extends ValueComposite
{
   @UseDefaults
   Property<List<String>> queries();

   @UseDefaults
   Property<List<String>> commands();

   @UseDefaults
   Property<List<String>> contexts();
   
   @Optional
   Property<ValueComposite> index();
}