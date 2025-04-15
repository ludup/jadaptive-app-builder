package com.jadaptive.api.cluster;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = LeadershipLostEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = ClusterNode.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(bundle = ClusterNode.RESOURCE_KEY, value = ObjectEvent.OBJECT_VIEW)})
public class LeadershipLostEvent extends ObjectEvent<ClusterNode> {
	
	private static final long serialVersionUID = 6612104802338158304L;
	
	public static final String RESOURCE_KEY = "leadershipLost";
	private ClusterNode object;

	public LeadershipLostEvent(ClusterNode object) {
		super(RESOURCE_KEY, "networks");
		this.object = object;
		setName(object.getHostname());
	}
	
	public LeadershipLostEvent(ClusterNode object, Throwable error) {
		super(RESOURCE_KEY, "networks", error);
		this.object = object;
		setName(object.getHostname());
	}

	@Override
	public ClusterNode getObject() {
		return object;
	}
	
}
