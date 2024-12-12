package com.jadaptive.api.user;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.ActionFilter;

public class EnabledAccountAction implements ActionFilter {

	@Override
	public boolean showAction(AbstractObject user) {
		return user.getValue("enabled") != Boolean.FALSE;
	}

}
