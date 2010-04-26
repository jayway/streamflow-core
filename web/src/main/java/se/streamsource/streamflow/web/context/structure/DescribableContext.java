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

package se.streamsource.streamflow.web.context.structure;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.dci.api.InteractionsMixin;

/**
 * JAVADOC
 */
@Mixins(DescribableContext.Mixin.class)
public interface DescribableContext
{
   public void changedescription( StringValue stringValue );

   abstract class Mixin
      extends InteractionsMixin
      implements DescribableContext
   {
      public void changedescription( StringValue stringValue )
      {
         Describable describable = context.get( Describable.class );
         describable.changeDescription( stringValue.string().get() );
      }
   }
}
