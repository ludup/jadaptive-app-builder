package com.jadaptive.api.user;

import java.io.Serializable;

import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectServiceBean;

@ObjectServiceBean(bean = UserService.class)
public interface User extends Serializable, UUIDDocument {
	
	String getUuid();
	
	String getUsername();
	
	String getName();

	void setName(String value);
}
