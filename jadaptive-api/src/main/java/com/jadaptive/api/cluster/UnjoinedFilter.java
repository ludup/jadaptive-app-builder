package com.jadaptive.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.PageMenuFilter;

public class UnjoinedFilter implements PageMenuFilter {
	
	@Autowired
	private ClusterManager clusterManager;

	@Override
	public boolean isVisible(ApplicationMenu menu) {
		return !clusterManager.isJoined();
	}

}
