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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;

import java.lang.reflect.Method;

/**
 * Change ownership of first argument to "this".
 */
@AppliesTo(ChangesOwner.class)
public class ChangeOwnerSideEffect
   extends GenericSideEffect
{
   @This
   Owner owner;

   @Override
   protected void invoke( Method method, Object[] args ) throws Throwable
   {
      Ownable ownable = (Ownable) args[0];

      ownable.changeOwner( owner );
   }
}