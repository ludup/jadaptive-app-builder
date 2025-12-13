package com.jadaptive.api.cluster;

import java.util.ArrayList;
import java.util.List;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.DynamicColumn;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@ObjectDefinition(resourceKey = ClusterNode.RESOURCE_KEY, type = ObjectType.COLLECTION, creatable = false, updatable = false, deletable = true, system = true)
@TableView(defaultColumns = {"status", "uuid", "hostname" }, 
otherColumns = {
		  @DynamicColumn(resourceKey = "uuid", service = ClusterManager.class),
		  @DynamicColumn(resourceKey = "status", service = ClusterManager.class),
		  @DynamicColumn(resourceKey = "hostname", service = ClusterManager.class)
}, multipleDelete = false)
@PageMenu(
		parent = ApplicationMenuService.REPORTING_MENU_UUID, 
		icon = "fa-stars", 
		weight = 2000, 
		withPermission = "system.read",
		filter =  JoinedFilter.class)
public final class ClusterNode extends UUIDEntity {

	private static final long serialVersionUID = -7912946488135263747L;

	public static final String RESOURCE_KEY = "clusterNode";
	
	public enum ClusterNodeStatus {
		ONLINE, UNAUTHORIZED, OFFLINE
	}
	
	@ObjectField(type = FieldType.ENUM)
	private ClusterNodeStatus status;

	@ObjectField(type = FieldType.TEXT)
	private String hostname;

	@ObjectField(type = FieldType.TEXT)
	private String groupAddress;

	@ObjectField(type = FieldType.TEXT)
	private String version;

	@ObjectField(type = FieldType.TEXT)
	private String timeZone;

	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	private List<ClusterService> services = new ArrayList<>();

	@ObjectField(type = FieldType.TEXT, hidden = true)
	private String authenticationToken;

	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public ClusterNodeStatus getStatus() {
		return status;
	}

	public void setStatus(ClusterNodeStatus status) {
		this.status = status;
	}

	public void setServices(List<ClusterService> services) {
		this.services = services;
	}

	public List<ClusterService> getServices() {
		return services;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getGroupAddress() {
		return groupAddress;
	}

	public void setGroupAddress(String groupAddress) {
		this.groupAddress = groupAddress;
	}

	public void populate(ClusterNode other) {
		this.hostname = other.hostname;
		this.status = other.status;
		this.services.clear();
		this.services.addAll(other.getServices());
		this.groupAddress = other.groupAddress;
	}

	
}
