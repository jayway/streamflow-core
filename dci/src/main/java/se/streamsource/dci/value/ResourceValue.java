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
package se.streamsource.dci.value;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.dci.value.link.LinkValue;

import java.util.List;

/**
 * Value representing a whole resource in a URL path. Allows listing of available
 * queries, commands, sub-resources and an index.
 */
public interface ResourceValue
   extends ValueComposite
{
   @UseDefaults
   Property<List<LinkValue>> queries();

   @UseDefaults
   Property<List<LinkValue>> commands();

   @UseDefaults
   Property<List<LinkValue>> resources();
   
   @Optional
   Property<ValueComposite> index();
}
