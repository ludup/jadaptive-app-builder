package com.jadaptive.api.ui;

import java.util.Collection;

import com.jadaptive.api.template.ObjectTemplate;

public interface UserInterfaceService {

	boolean canCreate(ObjectTemplate template);
	
	boolean canUpdate(ObjectTemplate template);

	Collection<HtmlPageExtender> getExtenders(HtmlPage htmlPage);

}
