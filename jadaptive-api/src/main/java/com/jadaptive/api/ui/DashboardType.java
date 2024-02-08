package com.jadaptive.api.ui;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;

public interface DashboardType {

	String name();
	
	String bundle();
	
	int weight();
	
	default String resourceKey() {
		var cased = StringUtils.remove(WordUtils.capitalizeFully(name(), new char[] { '_' }), "_");
		return cased.substring(0, 1).toLowerCase() + cased.substring(1);
	}

	default String cssId() {
		return name().toLowerCase().replace('_', '-');
	}
}
