package com.jadaptive.api.user;

import org.pf4j.Extension;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.ActionFilter;

public class DisabledAccountAction implements ActionFilter {

	@Override
	public boolean showAction(AbstractObject user) {
		return user.getValue("enabled") == Boolean.FALSE;
	}

}
