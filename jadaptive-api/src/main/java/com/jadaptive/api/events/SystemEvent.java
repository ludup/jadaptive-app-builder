package com.jadaptive.api.events;

import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.JadaptiveIgnore;
import com.jadaptive.api.repository.UUIDEvent;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = SystemEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, 
     creatable = false, updatable = false, system = true)
@ObjectViews({@ObjectViewDefinition(value = "event", bundle = SystemEvent.RESOURCE_KEY)})
public class SystemEvent extends UUIDEvent {

	private static final long serialVersionUID = 4068966863055480029L;

	public static final String RESOURCE_KEY = "systemEvent";
	public static final String EVENT_VIEW = "event";
	
	@ObjectField(type = FieldType.TIMESTAMP)
	@ObjectView(value = SystemEvent.EVENT_VIEW)
	Date timestamp;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.I18N)
	String resourceKey;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.I18N)
	String group;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW)
	String message;
	
	/**
	 * TODO: We want the ability to hide fields from the UI if they 
	 * have no value
	 */
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = SystemEvent.EVENT_VIEW)
	String extendedInformation;
	
	@ObjectField(type = FieldType.ENUM)
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.BOOTSTRAP_BADGE)
	EventState state = EventState.SUCCESS;
	
	public SystemEvent(String resourceKey, String group) {
		this(resourceKey, group, "", Utils.now());
	}
	
	public SystemEvent(String resourceKey, String group, Throwable e) {
		this(resourceKey, group, e.getMessage(), Utils.now());
		this.state = EventState.ERROR;
		this.extendedInformation = ExceptionUtils.getFullStackTrace(e);
	}
	
	public SystemEvent(String resourceKey, String group, String message, Date timestamp) {
		this.timestamp = timestamp;
		this.resourceKey = resourceKey;
		this.message = message;
		this.group = group;
		setSystem(true);
	}

	public String getResourceKey() {
		return resourceKey;
	}
	
	public String getEventGroup() {
		return group;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getGroup() {
		return group;
	}
	
	public String getMessage() {
		return message;
	}
	
	public EventState getState() {
		return state;
	}
	
	public String getExtendedInformation() {
		return extendedInformation;
	}
	
	public SystemEvent flagFailed() {
		this.state = EventState.FAILURE;
		return this;
	}
	
	public SystemEvent flagWarning() {
		this.state = EventState.WARNING;
		return this;
	}
	
	@JadaptiveIgnore
	public boolean async() { return true; };
}
