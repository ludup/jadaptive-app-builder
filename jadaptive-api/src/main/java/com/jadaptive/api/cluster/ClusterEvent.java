package com.jadaptive.api.cluster;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = ClusterEvent.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class ClusterEvent extends UUIDEntity {

	private static final long serialVersionUID = -7912946488135263747L;

	public static final String RESOURCE_KEY = "clusterEvent";
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	private SystemEvent event;

	@ObjectField(type = FieldType.TEXT)
	private String clusterNode;

	@ObjectField(type = FieldType.TEXT)
	private String tenant;

	@ObjectField(type = FieldType.TEXT)
	private String user;
	
	public ClusterEvent() {
	}

	public ClusterEvent(SystemEvent event, String clusterNode, String tenant, String user) {
		super();
		this.event = event;
		this.clusterNode = clusterNode;
		this.tenant = tenant;
		this.user = user;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getResourceKey() {
		return RESOURCE_KEY;
	}


	public SystemEvent getEvent() {
		return event;
	}


	public void setEvent(SystemEvent event) {
		this.event = event;
	}

	public String getClusterNode() {
		return clusterNode;
	}

	public void setClusterNode(String clusterNode) {
		this.clusterNode = clusterNode;
	}
	
	
}

