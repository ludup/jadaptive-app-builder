package com.jadaptive.plugins.keys;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@RequestPage(path = "generate-user-key/{uuid}")
@ModalPage
public class GenerateUserKey extends GeneratePublicKey {

	@Autowired
	UserService userService; 
	
	String uuid;
	
	@Override
	protected User getUser() {
		return getCurrentSession().getUser();
	}

	@Override
	public String getUri() {
		return "generate-user-key";
	}

	@Override
	protected String getAction() {
		// TODO Auto-generated method stub
		return "/generate/user/key";
	}

}
