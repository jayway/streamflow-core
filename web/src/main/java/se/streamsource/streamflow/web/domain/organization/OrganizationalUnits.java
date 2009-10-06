/*
 * Copyright (c) 2009, Rickard �berg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.organization;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.role.RolePolicy;
import se.streamsource.streamflow.web.domain.role.Roles;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnits.OrganizationsMixin.class)
public interface OrganizationalUnits
{
    OrganizationalUnit createOrganizationalUnit(@MaxLength(50) String name);

    void addOrganizationalUnit(OrganizationalUnit ou);

    void removeOrganizationalUnit(OrganizationalUnit ou);

    interface OrganizationalUnitsState
    {
        @Aggregated
        ManyAssociation<OrganizationalUnit> organizationalUnits();

        OrganizationalUnitEntity organizationalUnitCreated(DomainEvent event, @Name("id") String id);
        void organizationalUnitRemoved(DomainEvent create, OrganizationalUnit ou);
        void organizationalUnitAdded(DomainEvent event, OrganizationalUnit ou);

        OrganizationalUnits getParent(OrganizationalUnit ou);
    }

    abstract class OrganizationsMixin
            implements OrganizationalUnits, OrganizationalUnitsState
    {
        @Service
        IdentityGenerator idGenerator;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        RolePolicy policy;

        @This
        Roles.RolesState roles;

        @This
        OrganizationalUnit.OrganizationalUnitState ouState;

        @This
        OrganizationalUnits organizationalUnits;
        
        public OrganizationalUnit createOrganizationalUnit(String name)
        {
            OrganizationalUnitEntity ou = organizationalUnitCreated(DomainEvent.CREATE, idGenerator.generate(OrganizationalUnitEntity.class));
            addOrganizationalUnit(ou);
            ou.changeDescription(name);

            // Add current user as administrator
            ou.grantAdministratorToCurrentUser();

            return ou;
        }

        public void addOrganizationalUnit(OrganizationalUnit ou)
        {
            if (organizationalUnits().contains(ou)) {
                return;
            }
            organizationalUnitAdded(DomainEvent.CREATE, ou);
        }

        public void removeOrganizationalUnit(OrganizationalUnit ou)
        {
            if (!organizationalUnits().contains(ou))
                return; // OU is not a sub-OU of this OU

            organizationalUnitRemoved(DomainEvent.CREATE, ou);
        }

        public OrganizationalUnitEntity organizationalUnitCreated(DomainEvent event, @Name("id") String id)
        {
            EntityBuilder<OrganizationalUnitEntity> ouBuilder = uowf.currentUnitOfWork().newEntityBuilder(OrganizationalUnitEntity.class, id);
            ouBuilder.instance().organization().set(ouState.organization().get());
            OrganizationalUnitEntity ou = ouBuilder.newInstance();
            return ou;
        }

        public void organizationalUnitRemoved(DomainEvent create, OrganizationalUnit ou)
        {
            organizationalUnits().remove(ou);
        }

        public void organizationalUnitAdded(DomainEvent event, OrganizationalUnit ou)
        {
            organizationalUnits().add(organizationalUnits().count(), ou);
        }


        public OrganizationalUnits getParent(OrganizationalUnit ou)
        {
            if (organizationalUnits().contains(ou))
            {
                return organizationalUnits;
            } else
            {
                for (OrganizationalUnit organizationalUnit : organizationalUnits())
                {
                    OrganizationalUnitsState state = (OrganizationalUnitsState) organizationalUnit;
                    OrganizationalUnits parent = state.getParent(ou);
                    if (parent != null)
                    {
                        return parent;
                    }
                }
            }
            return null;
        }
    }
}
