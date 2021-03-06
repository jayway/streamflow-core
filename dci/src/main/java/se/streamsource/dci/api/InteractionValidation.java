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
package se.streamsource.dci.api;

/**
 * Interface that contexts can implement to achieve custom validation
 * of whether an interaction is valid or not. If the logic only applies for one
 * method it is usually better to use this instead of creating a new annotation for it.
 * <p/>
 * This is triggered by annotating the method that should be validated like so:
 *
 * @RequiresValid("xyz") public void xyz()
 * {...}
 * <p/>
 * This causes the context to be instantiated and isValid("xyz") is called. The isValid()
 * method can use the name to determine which set of logic is to be applied. Typically the provided
 * string will correspond to the name of the interaction, but this is not strictly necessary. It is
 * possible to combine several annotations on one method, if desired:
 * @RequiresValid("allowed") @RequiresValid("officehours")
 * public void xyz()
 * {...}
 * <p/>
 * The validation occurs both when a ResourceValue is computed for the resource as a whole, and when an actual
 * invocation of an interaction is made.
 */
public interface InteractionValidation
{
   boolean isValid(String name);
}
