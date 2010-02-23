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

package se.streamsource.streamflow.web.context.users;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersQueries;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.Users;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContexts;
import se.streamsource.streamflow.web.resource.organizations.OrganizationsServerResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * JAVADOC
 */
@Mixins(UsersContext.Mixin.class)
public interface UsersContext
   extends SubContexts<UserContext>, Context
{
   // Queries
   UserEntityListDTO users();
   
   // Commands
   UserContext createuser( NewUserCommand command);

   abstract class Mixin
      extends ContextMixin
      implements UsersContext
   {
      public UserEntityListDTO users()
      {
         UsersQueries orgs = module.unitOfWorkFactory().currentUnitOfWork().get( UsersQueries.class, UsersEntity.USERS_ID );

         return orgs.users();
      }

      public UserContext createuser( NewUserCommand command )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         Users users = uow.get( UsersEntity.class, UsersEntity.USERS_ID );
         User user = users.createUser( command.username().get(), command.password().get() );
         return context( user.toString() );
      }

      public void importusers( Representation representation ) throws ResourceException
      {
         boolean badRequest = false;
         String errors = "<html>";
         Locale locale = context.role(Locale.class);

         ResourceBundle bundle = ResourceBundle.getBundle(
               OrganizationsServerResource.class.getName(), locale );

         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         Users organizations = uow.get( Users.class, UsersEntity.USERS_ID );

         try
         {
            List<String> users = new ArrayList<String>();

            if (representation.getMediaType().equals( MediaType.APPLICATION_EXCEL ))
            {
               HSSFWorkbook workbook = new HSSFWorkbook( representation.getStream() );

               //extract a user list
               Sheet sheet1 = workbook.getSheetAt( 0 );
               StringBuilder builder;
               for (Row row : sheet1)
               {
                  builder = new StringBuilder();
                  builder.append( row.getCell( 0 ).getStringCellValue() );
                  builder.append( "," );
                  builder.append( row.getCell( 1 ).getStringCellValue() );

                  ((List<String>) users).add( builder.toString() );
               }

            } else if (representation.getMediaType().equals( MediaType.TEXT_CSV ))
            {
               StringReader reader = new StringReader( representation.getText() );
               BufferedReader bufReader = new BufferedReader( reader );
               String line = null;
               while ((line = bufReader.readLine()) != null)
               {
                  users.add( line );
               }
            } else
            {
               throw new ResourceException( Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE );
            }

            for (String userNamePwd : users)
            {
               if (userNamePwd.startsWith( "#" ))
               {
                  continue;
               }
               Pattern pattern = Pattern.compile( "\\t|," );
               String[] usrPwdPair = userNamePwd.split( pattern.pattern() );

               if (usrPwdPair.length < 2)
               {
                  badRequest = true;
                  errors += userNamePwd + " - " + bundle.getString( "missing_user_password" ) + "<br></br>";
                  continue;
               }

               String name = usrPwdPair[0].trim();
               String pwd = usrPwdPair[1].trim();

               // Check for empty pwd!!! and log an error for that
               if ("".equals( pwd.trim() ))
               {
                  badRequest = true;
                  errors += name + " - " + bundle.getString( "missing_password" ) + "<br></br>";
               }

               try
               {   // Check if user already exists
                  UserEntity existingUser = uow.get( UserEntity.class, name );
                  if (existingUser.isCorrectPassword( pwd ))
                  {
                     //nothing to do here
                     continue;
                  } else
                  {
                     existingUser.resetPassword( pwd );
                     continue;
                  }

               } catch (NoSuchEntityException e)
               {
                  //Ok user doesnt exist
               }

               try
               {
                  organizations.createUser( name, pwd );

               } catch (ConstraintViolationException e)
               {
                  // catch constraint violation and collect errors for the entire transaction
                  badRequest = true;
                  errors += name + " - " + bundle.getString( "user_name_not_valid" ) + "<br></br>";
               }
            }
         } catch (IOException ioe)
         {
            throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
         }

         // Check for errors and rollback
         if (badRequest)
         {
            errors += "</html>";
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, errors );
         }

      }


      public UserContext context( String id )
      {
         UserEntity user = module.unitOfWorkFactory().currentUnitOfWork().get( UserEntity.class, id );
         context.playRoles( user, UserEntity.class );

         return subContext( UserContext.class);
      }
   }
}
