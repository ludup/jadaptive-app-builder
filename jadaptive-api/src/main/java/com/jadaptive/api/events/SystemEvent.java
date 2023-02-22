package com.jadaptive.api.events;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.JadaptiveIgnore;
import com.jadaptive.api.repository.UUIDEvent;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
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
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = SystemEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, 
     creatable = false, updatable = false, defaultColumn = "eventKey")
@ObjectViews({@ObjectViewDefinition(value = "event", bundle = SystemEvent.RESOURCE_KEY, weight = Integer.MIN_VALUE)})
@TableView(defaultColumns = { "state", "timestamp", "username", "eventKey", "eventDescription", "ipAddress"}, 
				sortOrder = SortOrder.DESC, sortField = "timestamp", requiresView = false,
				actions = { @TableAction(bundle = SystemEvent.RESOURCE_KEY, icon = "fa-magnifying-glass", resourceKey = "inspect", target = Target.ROW, url = "/app/ui/event/{resourceKey}/{uuid}" )})
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
	@ObjectView(value = "", renderer = FieldRenderer.BOOTSTRAP_BADGE, weight = Integer.MIN_VALUE)
	EventState state = EventState.SUCCESS;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = EVENT_VIEW, weight = 9997, bundle = Session.RESOURCE_KEY, renderer = FieldRenderer.OPTIONAL)
	String username;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = EVENT_VIEW, weight = 9998, bundle = Session.RESOURCE_KEY, renderer = FieldRenderer.OPTIONAL)
	String name;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = EVENT_VIEW, weight = 9998, renderer = FieldRenderer.OPTIONAL)
	String eventDescription;
	
	public SystemEvent() {
		
	}

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
		attachSession();
	}
	
	public void setEventDescription(String objectName) {
		this.eventDescription = objectName;
	}
	
	public String getEventDescription() {
		return eventDescription;
	}
	
	private void attachSession() {
		if(Request.isAvailable()) {
			this.ipAddress = Request.isAvailable() ? Request.getRemoteAddress() : null;
			Session session = ApplicationServiceImpl.getInstance().getBean(SessionUtils.class).getActiveSession(Request.get(), false);
			if(Objects.nonNull(session)) {
				this.name = session.getUser().getName();
				this.username = session.getUser().getUsername();
			} else {
				PermissionService permissionService = ApplicationServiceImpl.getInstance().getBean(PermissionService.class);
				if(permissionService.hasUserContext()) {
					User user = permissionService.getCurrentUser();
					this.name = user.getName();
					this.username = user.getUsername();
				}
				
			}
		}
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
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public void setEventGroup(String eventGroup) {
		this.eventGroup = eventGroup;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setExtendedInformation(String extendedInformation) {
		this.extendedInformation = extendedInformation;
	}

	public void setState(EventState state) {
		this.state = state;
	}

}
