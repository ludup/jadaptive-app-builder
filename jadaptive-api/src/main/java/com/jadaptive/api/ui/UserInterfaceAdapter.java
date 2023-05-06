package com.jadaptive.api.ui;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.template.ObjectTemplate;

public interface UserInterfaceAdapter extends ExtensionPoint{

	boolean canCreate(ObjectTemplate template);

	boolean canUpdate(ObjectTemplate template);

	boolean isController(ObjectTemplate template);


}
