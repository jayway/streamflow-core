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

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;


public class CaseLogEntryContext
{
   @Structure
   ValueBuilderFactory vbf;

   public CaseLogEntryDTO index()
   {
      CaseLog.Data caseLog = (CaseLog.Data)RoleMap.role( CaseLoggable.Data.class ).caselog().get();
      Integer index = RoleMap.role( Integer.class );

      CaseLogEntryValue caseLogValue = caseLog.entries().get().get( index );

      ValueBuilder<CaseLogEntryDTO> valueBuilder = vbf.newValueBuilder( CaseLogEntryDTO.class );

      valueBuilder.prototype().caseLogType().set( caseLogValue.entryType().get() );
      valueBuilder.prototype().creationDate().set( caseLogValue.createdOn().get() );
      valueBuilder.prototype().creator().set( caseLogValue.createdBy().get().identity() );
      valueBuilder.prototype().message().set( caseLogValue.message().get() );
      valueBuilder.prototype().myPagesVisibility().set(  caseLogValue.availableOnMypages().get() );
      valueBuilder.prototype().href().set( "" );
      valueBuilder.prototype().id().set( caseLogValue.entity().toString() );
      valueBuilder.prototype().text().set( caseLogValue.message().get() );

      return valueBuilder.newInstance();
   }

   public void setpublish( @Name("publish") boolean publish )
   {
      CaseLog caseLog = RoleMap.role( CaseLoggable.Data.class ).caselog().get();
      Integer index = RoleMap.role( Integer.class );

      caseLog.modifyMyPagesVisibility( index, publish );
   }
}
