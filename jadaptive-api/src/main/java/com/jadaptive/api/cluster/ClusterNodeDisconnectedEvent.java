package com.jadaptive.api.cluster;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = ClusterNodeDisconnectedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = ClusterNode.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(bundle = ClusterNode.RESOURCE_KEY, value = ObjectEvent.OBJECT_VIEW)})
public class ClusterNodeDisconnectedEvent extends ObjectEvent<ClusterNode> {
	
	private static final long serialVersionUID = 6612104802338158304L;
	
	public static final String RESOURCE_KEY = "clusterNodeDisonnected";
	private ClusterNode object;

	public ClusterNodeDisconnectedEvent(ClusterNode object) {
		super(RESOURCE_KEY, "networks");
		this.object = object;
		setName(object.getHostname());
	}
	
	public ClusterNodeDisconnectedEvent(ClusterNode object, Throwable error) {
		super(RESOURCE_KEY, "networks", error);
		this.object = object;
		setName(object.getHostname());
	}

	@Override
	public ClusterNode getObject() {
		return object;
	}
	
}
