/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import se.streamsource.streamflow.web.domain.structure.casetype.CasePrioritySetting;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Check if case priority should be visible.
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(RequiresCasePriorityVisible.Constraint.class)
public @interface RequiresCasePriorityVisible
{
   public class Constraint
         implements org.qi4j.api.constraint.Constraint<RequiresCasePriorityVisible, TypedCase.Data >
   {
      public boolean isValid( RequiresCasePriorityVisible visible, TypedCase.Data value )
      {
         if( value.caseType().get() == null )
            return false;
         else
            return ((CasePrioritySetting.Data)value.caseType().get()).visible().get();
      }
   }
}