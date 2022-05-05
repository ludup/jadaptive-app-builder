package com.jadaptive.api.events;

import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.JadaptiveIgnore;
import com.jadaptive.api.repository.UUIDEvent;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TableView;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = SystemEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, 
     creatable = false, updatable = false, system = true, defaultColumn = "eventKey")
@ObjectViews({@ObjectViewDefinition(value = "event", bundle = SystemEvent.RESOURCE_KEY)})
@TableView(defaultColumns = { "state", "timestamp", "eventKey", "eventGroup", "ipAddress"}, sortOrder = SortOrder.DESC, sortField = "timestamp")
public class SystemEvent extends UUIDEvent {

	private static final long serialVersionUID = 4068966863055480029L;

	public static final String RESOURCE_KEY = "systemEvent";
	public static final String EVENT_VIEW = "event";
	
	@ObjectField(type = FieldType.TIMESTAMP)
	@ObjectView(value = SystemEvent.EVENT_VIEW, weight = 200)
	Date timestamp;
	
	@ObjectField(type = FieldType.TEXT, searchable = true)
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.I18N, weight = 100)
	String eventKey;
	
	@ObjectField(type = FieldType.TEXT, searchable = true)
	@ExcludeView(values = { FieldView.READ, FieldView.TABLE })
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.I18N)
	String eventGroup;
	
	@ObjectField(type = FieldType.TEXT, searchable = true)
	@ObjectView(value = SystemEvent.EVENT_VIEW, weight = 300)
	String ipAddress;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.OPTIONAL, weight = 9999)
	@ExcludeView(values = FieldView.TABLE)
	String extendedInformation;
	
	@ObjectField(type = FieldType.ENUM)
	@ObjectView(value = SystemEvent.EVENT_VIEW, renderer = FieldRenderer.BOOTSTRAP_BADGE, weight = 0)
	EventState state = EventState.SUCCESS;
	
	public SystemEvent(String resourceKey, String eventGroup) {
		this(resourceKey, eventGroup, Utils.now());
	}
	
	public SystemEvent(String eventKey, String eventGroup, Throwable e) {
		this(eventKey, eventGroup, Utils.now());
		this.state = EventState.ERROR;
		this.extendedInformation = ExceptionUtils.getFullStackTrace(e);
	}
	
	public SystemEvent(String eventKey, String eventGroup, Date timestamp) {
		this.timestamp = timestamp;
		this.eventKey = eventKey;
		this.eventGroup = eventGroup;
		this.ipAddress = Request.isAvailable() ? Request.get().getRemoteAddr() : null;
		setSystem(true);
	}
	
	public String getResourceKey() {
		return eventKey;
	}

	public String getEventKey() {
		return eventKey;
	}
	
	public String getEventGroup() {
		return eventGroup;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getIpAddress() {
		return ipAddress;
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
