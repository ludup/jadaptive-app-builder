package com.jadaptive.plugins.web.ui;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.menu.ApplicationMenu;

public interface UserInterfaceAdapter extends ExtensionPoint{

	boolean canCreate(ObjectTemplate template);

	boolean canUpdate(ObjectTemplate template);

	boolean isController(ObjectTemplate template);

	boolean isController(ApplicationMenu menu);

	boolean isEnabled(ApplicationMenu menu);

//	Feedback getFeedback(HttpServletRequest request, Page page);

}
