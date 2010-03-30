/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.logging.Logger;

/**
 * JAVADOC
 */
@Mixins(IndividualRepositoryService.Mixin.class)
public interface IndividualRepositoryService
      extends IndividualRepository, ServiceComposite, Activatable
{
   class Mixin
         implements IndividualRepository, Activatable
   {
      @Structure
      UnitOfWorkFactory uowf;

      public Individual individual()
      {
         UnitOfWork unitOfWork = uowf.currentUnitOfWork();
         Individual individual = unitOfWork.get( Individual.class, "1" );
         return individual;
      }

      public void activate() throws Exception
      {
         UnitOfWork uow = uowf.newUnitOfWork();

         try
         {
            uow.get( Individual.class, "1" );
         } catch (NoSuchEntityException e)
         {
            // Create Individual
            uow.newEntity( Individual.class, "1" );

            Logger.getLogger( IndividualRepository.class.getName() ).info( "Created invidual" );
         }

         uow.complete();
      }

      public void passivate() throws Exception
      {
      }
   }
}
