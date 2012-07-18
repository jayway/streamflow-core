/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.api.administration.form;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.util.MultiFieldHelper;

/**
 * JAVADOC
 */
@Mixins( CheckboxesFieldValue.Mixin.class )
public interface CheckboxesFieldValue
      extends SelectionFieldValue
{

   abstract class Mixin
      implements FieldValue
   {
      @This CheckboxesFieldValue definition;

      public Boolean validate( String value )
      {
         if ("".equals( value )) return true;
         for (String selection : MultiFieldHelper.options( value ))
         {
            if (!definition.values().get().contains( selection ))
               return false;
         }
         return true;
      }
   }
}
