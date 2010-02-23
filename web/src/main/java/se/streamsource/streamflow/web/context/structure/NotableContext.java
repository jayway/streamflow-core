/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.structure;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;

/**
 * JAVADOC
 */
@Mixins(NotableContext.Mixin.class)
public interface NotableContext
{
   public void changenote( StringDTO noteValue );

   abstract class Mixin
      extends ContextMixin
      implements NotableContext
   {
      public void changenote( StringDTO noteValue )
      {
         Notable notable = context.role( Notable.class );
         notable.changeNote( noteValue.string().get() );
      }
   }
}