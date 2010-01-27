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

package se.streamsource.streamflow.client.ui.task;

import java.util.Date;
import java.util.Observable;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel extends Observable implements Refreshable,
		EventListener, EventVisitor

{
	@Structure
	ValueBuilderFactory vbf;

	@Structure
	ObjectBuilderFactory obf;

	EventVisitorFilter eventFilter;

	private CommandQueryClient client;

	TaskGeneralDTO general;

	@Uses
	TaskLabelsModel taskLabelsModel;

	public TaskGeneralModel(@Uses CommandQueryClient client)
	{
		this.client = client;
		eventFilter = new EventVisitorFilter(client.getReference()
				.getParentRef().getLastSegment(), this, "addedLabel",
				"removedLabel", "changedOwner", "changedTaskType");
	}

	public TaskGeneralDTO getGeneral()
	{
		if (general == null)
			refresh();

		return general;
	}

	public void changeDescription(String newDescription)
	{
		try
		{
			ValueBuilder<StringDTO> builder = vbf
					.newValueBuilder(StringDTO.class);
			builder.prototype().string().set(newDescription);
			client.putCommand("changedescription", builder.newInstance());
		} catch (ResourceException e)
		{
			throw new OperationException(
					TaskResources.could_not_change_description, e);
		}
	}

	public void changeNote(String newNote)
	{
		try
		{
			ValueBuilder<StringDTO> builder = vbf
					.newValueBuilder(StringDTO.class);
			builder.prototype().string().set(newNote);
			client.putCommand("changenote", builder.newInstance());
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_change_note, e);
		}
	}

	public void changeDueOn(Date newDueOn)
	{
		try
		{
			ValueBuilder<DateDTO> builder = vbf.newValueBuilder(DateDTO.class);
			builder.prototype().date().set(newDueOn);
			client.putCommand("changedueon", builder.newInstance());
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_change_due_on,
					e);
		}
	}

	public void changeTaskType(EntityReference taskType)
	{
		try
		{
			ValueBuilder<EntityReferenceDTO> builder = vbf
					.newValueBuilder(EntityReferenceDTO.class);
			builder.prototype().entity().set(taskType);
			client.postCommand("changetasktype", builder.newInstance());
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_remove_label,
					e);
		}
	}

	public TaskLabelsModel labelsModel()
	{
		return taskLabelsModel;
	}

	public FormsListModel formsModel()
	{
		return obf.newObjectBuilder(FormsListModel.class).use(
				client.getSubClient("forms"), getPossibleForms()).newInstance();
	}

	public EventList<ListItemValue> getPossibleTaskTypes()
	{
		try
		{
			BasicEventList<ListItemValue> list = new BasicEventList<ListItemValue>();

			ListValue listValue = client.query("possibletasktypes",
					ListValue.class);
			list.addAll(listValue.items().get());

			return list;
		} catch (ResourceException e)
		{
			throw new OperationException(WorkspaceResources.could_not_refresh,
					e);
		}
	}

	public EventList<ListItemValue> getPossibleLabels()
	{
		try
		{
			BasicEventList<ListItemValue> list = new BasicEventList<ListItemValue>();

			ListValue listValue = client.query("possiblelabels",
					ListValue.class);
			list.addAll(listValue.items().get());

			return list;
		} catch (ResourceException e)
		{
			throw new OperationException(WorkspaceResources.could_not_refresh,
					e);
		}
	}

	public EventList<ListItemValue> getPossibleForms()
	{
		try
		{
			BasicEventList<ListItemValue> list = new BasicEventList<ListItemValue>();

			ListValue listValue = client
					.query("possibleforms", ListValue.class);
			list.addAll(listValue.items().get());

			return list;
		} catch (ResourceException e)
		{
			throw new OperationException(WorkspaceResources.could_not_refresh,
					e);
		}
	}

	public void refresh()
	{
		try
		{
			general = (TaskGeneralDTO) client.query("general",
					TaskGeneralDTO.class).buildWith().prototype();

			taskLabelsModel.setLabels(general.labels().get());

			// formsListModel.refresh();

			setChanged();
			notifyObservers();

		} catch (Exception e)
		{
			throw new OperationException(TaskResources.could_not_refresh, e);
		}
	}

	public void notifyEvent(DomainEvent event)
	{
		eventFilter.visit(event);

		taskLabelsModel.notifyEvent(event);
	}

	public boolean visit(DomainEvent event)
	{
		refresh();
		return true;
	}

	public void taskType(EntityReference selected)
	{
		try
		{
			ValueBuilder<EntityReferenceDTO> builder = vbf
					.newValueBuilder(EntityReferenceDTO.class);
			builder.prototype().entity().set(selected);
			client.putCommand("tasktype", builder.newInstance());
		} catch (ResourceException e)
		{
			throw new OperationException(
					WorkspaceResources.could_not_perform_operation, e);
		}
	}

	public void addLabel(EntityReference entityReference)
	{
		try
		{
			ValueBuilder<EntityReferenceDTO> builder = vbf
					.newValueBuilder(EntityReferenceDTO.class);
			builder.prototype().entity().set(entityReference);
			client.putCommand("label", builder.newInstance());
		} catch (ResourceException e)
		{
			throw new OperationException(
					WorkspaceResources.could_not_perform_operation, e);
		}
	}

	public Actions actions()
	{
		try
		{
			return client.query("actions", Actions.class);
		} catch (ResourceException e)
		{
			throw new OperationException(
					WorkspaceResources.could_not_perform_operation, e);
		}
	}

}