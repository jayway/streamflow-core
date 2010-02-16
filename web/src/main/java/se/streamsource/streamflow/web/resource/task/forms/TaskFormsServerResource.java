/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.task.forms;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionsQueries;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /tasks/{task}/forms
 */
public class TaskFormsServerResource
      extends CommandQueryServerResource
{
   public TaskFormsServerResource()
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   public SubmittedFormsListDTO listsubmittedforms()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      SubmittedFormsQueries forms = 
            uow.get( SubmittedFormsQueries.class, getRequest().getAttributes().get( "task" ).toString() );

      return forms.getSubmittedForms();
   }

   public EffectiveFieldsDTO effectivefields()
   {
      String formsQueryId = getRequest().getAttributes().get( "task" ).toString();

      SubmittedFormsQueries fields = uowf.currentUnitOfWork().get( SubmittedFormsQueries.class, formsQueryId );

      return fields.effectiveFields();
   }

   public SubmittedFormDTO submittedform( IntegerDTO index) throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      SubmittedFormsQueries forms =
            uow.get( SubmittedFormsQueries.class, getRequest().getAttributes().get( "task" ).toString() );

      return forms.getSubmittedForm( index.integer().get() );
   }


   public void createformsubmission( EntityReferenceDTO formDTO )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      FormSubmissions formSubmissions =
            uow.get( FormSubmissions.class, getRequest().getAttributes().get( "task" ).toString() );

      Form form = uow.get( Form.class, formDTO.entity().get().identity() );

      formSubmissions.createFormSubmission( form );
   }

   public void discard( EntityReferenceDTO formDTO )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      FormSubmissions formSubmissions =
            uow.get( FormSubmissions.class, getRequest().getAttributes().get( "task" ).toString() );

      Form form = uow.get( Form.class, formDTO.entity().get().identity() );

      formSubmissions.discardFormSubmission( form );
   }

   public EntityReferenceDTO formsubmission( EntityReferenceDTO formDTO ) 
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      FormSubmissionsQueries formSubmissions =
            uow.get( FormSubmissionsQueries.class, getRequest().getAttributes().get( "task" ).toString() );

      return formSubmissions.getFormSubmission( formDTO.entity().get() );
   }

   public void submit( EntityReferenceDTO formDTO ) throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      TaskEntity task = uow.get( TaskEntity.class,
            getRequest().getAttributes().get( "task" ).toString() );

      EntityReferenceDTO dto = task.getFormSubmission( formDTO.entity().get() );

      if ( dto == null )
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }

      FormSubmission formSubmission =
            uow.get( FormSubmission.class, dto.entity().get().identity() );

      Submitter submitter = uow.get( Submitter.class, getClientInfo().getUser().getIdentifier() );

      task.submitForm( formSubmission, submitter );
   }

   @Override
   protected String getConditionalIdentityAttribute()
   {
      return "task";
   }
}