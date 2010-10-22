/*
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

package se.streamsource.streamflow.web.rest;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Language;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.security.User;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryRestlet2;
import se.streamsource.streamflow.web.application.security.ProxyUserPrincipal;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.resource.RootResource;

import javax.security.auth.Subject;
import java.util.List;
import java.util.Locale;

/**
 * JAVADOC
 */
public class StreamflowRestlet
   extends CommandQueryRestlet2
{
   @Override
   protected Uniform createRoot( Request request, Response response )
   {
      initRoleMap( request, RoleMap.current() );

      return module.objectBuilderFactory().newObjectBuilder( RootResource.class ).newInstance();
   }

   private void initRoleMap( Request request, RoleMap roleMap )
   {
      roleMap.set( resolveRequestLocale( request ), Locale.class );
      roleMap.set( request.getResourceRef(), Reference.class );
      roleMap.set( getApplication(), Application.class );

      // TODO Should we really store user AND subject in role map?
      Subject subject = new Subject();
      subject.getPrincipals().addAll( request.getClientInfo().getPrincipals() );

      User user = request.getClientInfo().getUser();
      if (user != null)
      {
         subject.getPrivateCredentials().add( user.getSecret() );
      }

      roleMap.set( subject );

      String name = subject.getPrincipals().iterator().next().getName();
      UserAuthentication authentication = module.unitOfWorkFactory().currentUnitOfWork().get( UserAuthentication.class, name );
      roleMap.set(authentication);

      if ( authentication instanceof ProxyUser)
      {
         subject.getPrincipals().add( new ProxyUserPrincipal( name ));
      }
   }

   protected Locale resolveRequestLocale( Request request )
   {
      List<Preference<Language>> preferenceList = request.getClientInfo().getAcceptedLanguages();

      if (preferenceList.isEmpty())
         return Locale.getDefault();

      Language language = preferenceList
            .get( 0 ).getMetadata();
      String[] localeStr = language.getName().split( "_" );

      Locale locale;
      switch (localeStr.length)
      {
         case 1:
            locale = new Locale( localeStr[0] );
            break;
         case 2:
            locale = new Locale( localeStr[0], localeStr[1] );
            break;
         case 3:
            locale = new Locale( localeStr[0], localeStr[1], localeStr[2] );
            break;
         default:
            locale = Locale.getDefault();
      }
      return locale;
   }}