package com.jadaptive.api.ui;

import java.util.Collection;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.menu.ApplicationMenu;

public interface UserInterfaceService {

	boolean canCreate(ObjectTemplate template);
	
	boolean canUpdate(ObjectTemplate template);

	boolean isEnabled(ApplicationMenu menu);

	Collection<HtmlPageExtender> getExtenders(HtmlPage htmlPage);

//	Feedback getFeedback(HttpServletRequest request, Page page);
}
