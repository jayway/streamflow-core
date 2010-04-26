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

package se.streamsource.streamflow.web.context.structure.labels;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.Specification;
import se.streamsource.streamflow.web.domain.entity.label.PossibleLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(SelectedLabelsContext.Mixin.class)
public interface SelectedLabelsContext
   extends SubContexts<SelectedLabelContext>, IndexInteraction<LinksValue>, Interactions
{
   public LinksValue possiblelabels();
   public void createlabel( StringValue name );
   public void addlabel( EntityReferenceDTO labelDTO );

   abstract class Mixin
         extends InteractionsMixin
         implements SelectedLabelsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         SelectedLabels.Data labels = context.get(SelectedLabels.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "label" ).addDescribables( labels.selectedLabels() ).newLinks();
      }

      public LinksValue possiblelabels()
      {
         PossibleLabelsQueries possibleLabelsQueries = context.get(PossibleLabelsQueries.class);
         final SelectedLabels.Data selectedLabels = context.get(SelectedLabels.Data.class);

         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command( "addlabel" );
         possibleLabelsQueries.possibleLabels( builder, new Specification<Label>()
         {
            public boolean valid( Label instance )
            {
               return !selectedLabels.selectedLabels().contains( instance );
            }
         });
         return builder.newLinks();
      }

      public void createlabel( StringValue name )
      {
         Labels labels = context.get(Labels.class);
         SelectedLabels selectedLabels = context.get(SelectedLabels.class);

         Label label = labels.createLabel( name.string().get() );
         selectedLabels.addSelectedLabel( label );
      }

      public void addlabel( EntityReferenceDTO labelDTO )
      {
         SelectedLabels labels = context.get( SelectedLabels.class);
         Label label = module.unitOfWorkFactory().currentUnitOfWork().get( Label.class, labelDTO.entity().get().identity() );

         labels.addSelectedLabel( label );
      }

      public SelectedLabelContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get(Label.class, id ));
         return subContext( SelectedLabelContext.class );
      }
   }
}
