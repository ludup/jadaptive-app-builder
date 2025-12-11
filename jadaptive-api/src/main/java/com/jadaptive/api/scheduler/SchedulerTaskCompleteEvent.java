package com.jadaptive.api.scheduler;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = SchedulerTaskCompleteEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = SchedulerTask.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(bundle = SchedulerTask.RESOURCE_KEY, value = ObjectEvent.OBJECT_VIEW)})
public class SchedulerTaskCompleteEvent extends ObjectEvent<SchedulerTask> {
	
	private static final long serialVersionUID = 6612104802338158304L;
	
	public static final String RESOURCE_KEY = "schedulerTaskCompleted";
	private SchedulerTask object;

	public SchedulerTaskCompleteEvent(SchedulerTask object) {
		super(RESOURCE_KEY, "schedulerTask");
		this.object = object;
		setName(object.getDisplayName());
	}
	
	public SchedulerTaskCompleteEvent(SchedulerTask object, Throwable error) {
		super(RESOURCE_KEY, "schedulerTask", error);
		this.object = object;
		setName(object.getDisplayName());
	}

	@Override
	public SchedulerTask getObject() {
		return object;
	}
	
}
