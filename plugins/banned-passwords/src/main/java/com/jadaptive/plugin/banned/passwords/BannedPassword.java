package com.jadaptive.plugin.banned.passwords;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class BannedPassword extends AbstractUUIDEntity {

	String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
