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
package se.streamsource.streamflow.web.context.workspace.cases.general;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.CasePriorityDTO;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresCasePriorityVisible;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.casetype.CasePrioritySetting;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.CasePriority;
import se.streamsource.streamflow.web.domain.structure.organization.CasePriorityDefinitions;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

import java.util.Date;

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.*;

/**
 * Commands for the General view of a Case. They all require the "write" permission
 */
@Mixins(CaseGeneralCommandsContext.Mixin.class)
@RequiresPermission(PermissionType.write)
public interface CaseGeneralCommandsContext
      extends
      Context
{
   @RequiresStatus({DRAFT, OPEN})
   void changedueon( @Name("date") Date dueOnValue );

   @RequiresStatus({DRAFT, OPEN})
   void casetype( EntityValue dto );

   @RequiresStatus({DRAFT, OPEN})
   void changedescription( @Optional @MaxLength(50) @Name("description") String stringValue );

   LinksValue possiblecasetypes();
   
   @RequiresStatus({DRAFT, OPEN})
   @RequiresCasePriorityVisible
   void changepriority( @Optional CasePriorityValue priority );

   @RequiresCasePriorityVisible
   LinksValue casepriorities();

   abstract class Mixin
         implements CaseGeneralCommandsContext
   {
      @Structure
      Module module;

      @Uses
      CasePriority casePriority;

      public void changedescription( String stringValue )
      {
         Describable describable = RoleMap.role( Describable.class );
         describable.changeDescription( stringValue );
      }

      public void changedueon( Date newDueOn )
      {
         DueOn dueOn = RoleMap.role( DueOn.class );
         dueOn.dueOn( newDueOn );
      }

      public LinksValue possiblecasetypes()
      {
         CaseTypeQueries aCase = RoleMap.role( CaseTypeQueries.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "casetype" );

         aCase.possibleCaseTypes( builder );

         return builder.newLinks();
      }

      public void casetype( EntityValue dto )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         TypedCase aCase = RoleMap.role( TypedCase.class );

         String entityReference = dto.entity().get();
         if (entityReference != null)
         {
            CaseType caseType = uow.get( CaseType.class, entityReference );
            aCase.changeCaseType( caseType );
         } else
            aCase.changeCaseType( null );
      }
      
      public LinksValue casepriorities()
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Organization org = orgs.organization().get();
         //Organization org = ( (OwningOrganization) ((OwningOrganizationalUnit.Data) RoleMap.role( Ownable.Data.class ).owner().get()).organizationalUnit().get() ).organization().get();
         RoleMap.current().set( org );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "changepriority" );
         ValueBuilder<CasePriorityDTO> linkBuilder = module.valueBuilderFactory().newValueBuilder( CasePriorityDTO.class );
         CasePrioritySetting.Data casePriority = (CasePrioritySetting.Data)((TypedCase.Data)RoleMap.role( TypedCase.class )).caseType().get();
         // if not mandatory add an empty option
         if( !casePriority.mandatory().get() )
         {
            linkBuilder.prototype().text().set( "-" );
            linkBuilder.prototype().id().set( "-1" );
            linkBuilder.prototype().href().set( "" );
            builder.addLink( linkBuilder.newInstance() );
         }

         int count = 0;
         for( CasePriorityValue priority : RoleMap.role( CasePriorityDefinitions.Data.class ).prioritys().get() )
         {
            linkBuilder.prototype().priority().set( priority );
            linkBuilder.prototype().text().set( priority.name().get() );
            linkBuilder.prototype().id().set( ""+count );
            linkBuilder.prototype().rel().set( "priority" );
            linkBuilder.prototype().href().set( "" );

            builder.addLink( linkBuilder.newInstance() );
            count++;
         }
         
         return builder.newLinks();
      }
      
      public void changepriority( CasePriorityValue priority )
      {
         if(Strings.empty( priority.name().get() ) )
            casePriority.changePriority( null );
         else
            casePriority.changePriority( priority );
      }
   }
}
