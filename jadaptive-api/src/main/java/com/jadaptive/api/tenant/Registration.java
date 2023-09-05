package com.jadaptive.api.tenant;

import com.jadaptive.api.repository.UUIDEntity;

public class Registration extends UUIDEntity {

	private static final long serialVersionUID = 6453613448531239814L;
	
	String company;
	String name;
	String email;
	
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String getResourceKey() {
		return "registration";
	}

	
}
