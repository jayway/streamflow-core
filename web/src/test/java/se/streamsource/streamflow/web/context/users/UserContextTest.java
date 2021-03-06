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
package se.streamsource.streamflow.web.context.users;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.account.AccountContext;
import se.streamsource.streamflow.web.context.account.ProfileContext;
import se.streamsource.streamflow.web.context.administration.OrganizationUserContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.WrongPasswordException;

/**
 * JAVADOC
 */
public class UserContextTest
   extends ContextTest
{
   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      UsersContextTest.createUser( "testing" );
      clearEvents();
   }

   @AfterClass
   public static void after() throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      uow.remove( uow.get( User.class, "testing" ));
      uow.complete();
   }

   @Test
   public void testDisabled() throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();
      playRole(User.class, "testing");

      Organizations.Data data = (Organizations.Data) uow.get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );
      OrganizationEntity organization = (OrganizationEntity) data.organization().get();
      playRole( Organization.class, organization.identity().get() );

      context(OrganizationUserContext.class).setdisabled();
      uow.complete();
      eventsOccurred( "changedEnabled" );
   }

   @Test
   public void testChangePassword() throws UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "testing");
         try
         {
            context( AccountContext.class).changepassword( "testing", "testing2");
         } catch (WrongPasswordException e)
         {
            Assert.fail( "Should have been able to change password" );
         }
         uow.complete();

         eventsOccurred( "changedPassword" );
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "testing");
         try
         {
            context(AccountContext.class).changepassword( "testing", "testing3");
            Assert.fail( "Should not have been able to change password" );
         } catch (WrongPasswordException e)
         {
            // Ok
         }
         uow.complete();
         clearEvents();
      }

   }

   @Test
   public void testDeliveryType() throws UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "testing");

         context(ProfileContext.class).changemessagedeliverytype(MessageRecipient.MessageDeliveryTypes.none);
         uow.complete();
         eventsOccurred( "changedMessageDeliveryType" );
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(User.class, "testing");

         context( ProfileContext.class).changemessagedeliverytype( MessageRecipient.MessageDeliveryTypes.email );
         uow.complete();
         eventsOccurred( );
      }

   }
}