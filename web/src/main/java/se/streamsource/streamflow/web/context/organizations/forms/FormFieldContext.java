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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldTemplate;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.DeleteContext;
import se.streamsource.streamflow.web.infrastructure.web.context.RequiresRoles;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;

/**
 * JAVADOC
 */
@Mixins(FormFieldContext.Mixin.class)
public interface FormFieldContext
      extends DeleteContext, DescribableContext, NotableContext, Context
{
   abstract class Mixin
         extends ContextMixin
         implements FormFieldContext
   {
      @Structure
      Module module;

      public FieldDefinitionValue field() throws ResourceException
      {
         FieldEntity fieldEntity = context.role( FieldEntity.class );

         ValueBuilder<FieldDefinitionValue> builder = module.valueBuilderFactory().newValueBuilder( FieldDefinitionValue.class );
         builder.prototype().field().set( EntityReference.getEntityReference( fieldEntity ) );
         builder.prototype().note().set( fieldEntity.note().get() );
         builder.prototype().description().set( fieldEntity.description().get() );
         builder.prototype().fieldValue().set( fieldEntity.fieldValue().get() );

         return builder.newInstance();
      }

      public void updatemandatory( BooleanDTO mandatory ) throws ResourceException
      {
         FieldTemplate fieldEntity = context.role( FieldTemplate.class );

         fieldEntity.changeMandatory( mandatory.bool().get() );
      }

      @RequiresRoles(TextFieldValue.class)
      public void changewidth( IntegerDTO newWidth ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         TextFieldValue value = context.role( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().width().set( newWidth.integer().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      @RequiresRoles(TextFieldValue.class)
      public void changerows( IntegerDTO newRows ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         TextFieldValue value = context.role( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().rows().set( newRows.integer().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      @RequiresRoles(SelectionFieldValue.class)
      public void changemultiple( BooleanDTO multiple ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         SelectionFieldValue value = context.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().multiple().set( multiple.bool().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      @RequiresRoles(SelectionFieldValue.class)
      public void addselectionelement( StringDTO name ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         SelectionFieldValue value = context.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().values().get().add( name.string().get() );
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      @RequiresRoles(SelectionFieldValue.class)
      public void removeselectionelement( IntegerDTO index ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         SelectionFieldValue value = context.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         if (builder.prototype().values().get().size() > index.integer().get())
         {
            builder.prototype().values().get().remove( index.integer().get().intValue() );
            fieldValueDefinition.changeFieldValue( builder.newInstance() );
         }
      }

      @RequiresRoles(SelectionFieldValue.class)
      public void moveselectionelement( NamedIndexDTO moveElement ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         SelectionFieldValue value = context.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         String element = builder.prototype().values().get().remove( moveElement.index().get().intValue() );
         if ("up".equals( moveElement.name().get() ))
         {
            builder.prototype().values().get().add( moveElement.index().get() - 1, element );
         } else
         {
            builder.prototype().values().get().add( moveElement.index().get() + 1, element );
         }
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      @RequiresRoles(SelectionFieldValue.class)
      public void changeselectionelementname( NamedIndexDTO newNameDTO ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         SelectionFieldValue value = context.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().values().get().set( newNameDTO.index().get(), newNameDTO.name().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

/*
      @RequiresRoles(CommentFieldValue.class)
      public void changecomment( StringDTO newComment ) throws ResourceException
      {
         FieldValueDefinition fieldValueDefinition = context.role( FieldValueDefinition.class );
         CommentFieldValue value = context.role( CommentFieldValue.class );
         ValueBuilder<CommentFieldValue> builder = value.buildWith();
         builder.prototype().comment().set( newComment.string().get() );
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

*/
      public void move( StringDTO direction ) throws ResourceException
      {
         Field field = context.role( Field.class );
         Fields fields = context.role( Fields.class );
         Fields.Data fieldsData = context.role( Fields.Data.class );

         int index = fieldsData.fields().toList().indexOf( field );
         if (direction.string().get().equalsIgnoreCase( "up" ))
         {
            try
            {
               fields.moveField( field, index - 1 );
            } catch (ConstraintViolationException e)
            {
            }
         } else
         {
            fields.moveField( field, index + 1 );
         }
      }

      public void delete()
      {
         Field field = context.role(Field.class);
         Fields fields = context.role(Fields.class);

         fields.removeField( field );
      }
   }
}
