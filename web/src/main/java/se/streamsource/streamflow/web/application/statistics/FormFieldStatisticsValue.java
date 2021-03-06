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
package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Value for form data about a case in statistics. For multi-value fields
 * the same {formId,fieldId} may repeat many times in the statistics, with different values.
 */
public interface FormFieldStatisticsValue
      extends ValueComposite
{
   /**
    * Id of the form
    * @return name of the form
    */
   Property<String> formId();

   /**
    * Id of the field
    *
    * @return name of the field
    */
   Property<String> fieldId();

   /**
    * Value for the field
    *
    * @return value for field
    */
   Property<String> value();
   
   /**
    * Datatype for the field
    * @return datatype for field
    */
   Property<String> datatype();
}