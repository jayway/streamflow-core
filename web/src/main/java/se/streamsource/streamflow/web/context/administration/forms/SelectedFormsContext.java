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
package se.streamsource.streamflow.web.context.administration.forms;

import static se.streamsource.dci.api.RoleMap.role;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
public class SelectedFormsContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      SelectedForms.Data forms = role( SelectedForms.Data.class );

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "selectedform" ).addDescribables( forms.selectedForms() ).newLinks();
   }

   public LinksValue possibleforms()
   {
      OrganizationQueries organizationQueries = role( OrganizationQueries.class );

      final SelectedForms.Data selectedForms = role( SelectedForms.Data.class );

      final LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addform" );

      organizationQueries.visitOrganization( new OrganizationVisitor()
      {

         Describable owner;

         @Override
         public boolean visitOrganization( Organization org )
         {
            owner = org;

            return super.visitOrganization( org );
         }

         @Override
         public boolean visitOrganizationalUnit( OrganizationalUnit ou )
         {
            owner = ou;

            return super.visitOrganizationalUnit( ou );
         }

         @Override
         public boolean visitProject( Project project )
         {
            owner = project;

            return super.visitProject( project );
         }

         @Override
         public boolean visitCaseType( CaseType caseType )
         {
            owner = caseType;

            return super.visitCaseType( caseType );
         }

         @Override
         public boolean visitForm( Form form )
         {
            if (!selectedForms.selectedForms().contains( form ))
               builder.addDescribable( form, owner );

            return true;
         }
      }, new OrganizationQueries.ClassSpecification(
            Organization.class,
            OrganizationalUnits.class,
            OrganizationalUnit.class,
            Projects.class,
            Project.class,
            CaseTypes.class,
            CaseType.class,
            Forms.class ) );

      return builder.newLinks();
   }

   public LinksValue possibleformsforcasetype()
   {
      AccessPointSettings.Data accessPoint = RoleMap.role( AccessPointSettings.Data.class );
      SelectedForms.Data selected = RoleMap.role( SelectedForms.Data.class );
      CaseType caseType = accessPoint.caseType().get();

      List<Form> possibleForms = new ArrayList<Form>();

      if (caseType != null)
      {

         List<Form> forms = ((SelectedForms.Data) caseType).selectedForms().toList();
         for (Form f : forms)
         {
            if (!selected.selectedForms().contains( f ))
            {
               possibleForms.add( f );
            }
         }
      }

      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).
            command( "addform" );
      for(Form form : possibleForms )
      {
         builder.addDescribable( form, ((Describable)((Ownable.Data)form).owner().get()).getDescription() );
      }
      return builder.newLinks();
   }

   public void addform( EntityValue formDTO )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      SelectedForms selectedForms = role( SelectedForms.class );
      Form form = uow.get( Form.class, formDTO.entity().get() );

      selectedForms.addSelectedForm( form );
   }
}