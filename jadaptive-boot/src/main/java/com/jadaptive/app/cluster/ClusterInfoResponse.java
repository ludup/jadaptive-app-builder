package com.jadaptive.app.cluster;

import com.jadaptive.api.auth.oauth2.OAuth2ScopeResponse;

public class ClusterInfoResponse extends OAuth2ScopeResponse<ClusterJoinInfo> {
	
	public ClusterInfoResponse() {
		super();
	}

	public ClusterInfoResponse(ClusterJoinInfo resource) {
		super(resource);
	}

}
