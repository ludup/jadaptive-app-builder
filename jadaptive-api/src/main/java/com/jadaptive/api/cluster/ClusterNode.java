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

@ObjectDefinition(resourceKey = ClusterNode.RESOURCE_KEY, type = ObjectType.COLLECTION, creatable = false, updatable = false, deletable = false, system = true)
@TableView(defaultColumns = {"status", "uuid", "hostname", "lastHeartbeat" }, 
otherColumns = {
		  @DynamicColumn(resourceKey = "uuid", service = ClusterManager.class),
		  @DynamicColumn(resourceKey = "status", service = ClusterManager.class),
		  @DynamicColumn(resourceKey = "lastHeartbeat", service = ClusterManager.class),
		  @DynamicColumn(resourceKey = "hostname", service = ClusterManager.class),
}, multipleDelete = false)
@PageMenu(
		parent = ApplicationMenuService.REPORTING_MENU_UUID, 
		icon = "fa-stars", 
		weight = 2000, 
		withPermission = "system.read")
public final class ClusterNode extends UUIDEntity {

	private static final long serialVersionUID = -7912946488135263747L;

	public static final String RESOURCE_KEY = "clusterNode";
	
	public enum ClusterNodeStatus {
		ONLINE, OFFLINE
	}
	
	@ObjectField(type = FieldType.ENUM)
	private ClusterNodeStatus status;

	@ObjectField(type = FieldType.TEXT)
	private String hostname;

	@ObjectField(type = FieldType.LONG)
	private long lastHeartbeat;

	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	private List<ClusterService> services = new ArrayList<>();

	public String getResourceKey() {
		return RESOURCE_KEY;
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

	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void populate(ClusterNode other) {
		this.hostname = other.hostname;
		this.status = other.status;
		this.lastHeartbeat = other.lastHeartbeat;
		this.services.clear();
		this.services.addAll(other.getServices());
	}

	
}
