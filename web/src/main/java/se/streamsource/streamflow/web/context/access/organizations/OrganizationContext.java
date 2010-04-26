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

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.context.access.projects.ProjectsContext;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;

/**
 * JAVADOC
 */
@Mixins(OrganizationContext.Mixin.class)
public interface OrganizationContext
   extends Interactions, IndexInteraction<StringValue>
{
   @SubContext
   ProjectsContext projects();

   @SubContext
   AccessPointsContext accesspoints();

   @SubContext
   ProxyUsersContext proxyusers();

   abstract class Mixin
      extends InteractionsMixin
      implements OrganizationContext
   {

      public AccessPointsContext accesspoints()
      {
         return subContext( AccessPointsContext.class );
      }

      public ProjectsContext projects( )
      {
         return subContext( ProjectsContext.class);
      }

      public ProxyUsersContext proxyusers()
      {
         return subContext( ProxyUsersContext.class );
      }

      public StringValue index()
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );

         builder.prototype().string().set( context.get( Organization.class ).getDescription() );

         return builder.newInstance();
      }
   }
}