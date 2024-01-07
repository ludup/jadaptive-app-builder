package com.jadaptive.api.template;

import com.jadaptive.api.entity.AbstractObject;

public class NoActionFilter implements ActionFilter {

	@Override
	public boolean showAction(AbstractObject object) {
		return true;
	}

}
