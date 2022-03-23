package com.jadaptive.api.user;

import java.io.Serializable;

import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectServiceBean;

@ObjectServiceBean(bean = UserService.class)
@AuditedObject
public interface User extends Serializable, UUIDDocument, NamedDocument {
	
	String getUuid();
	
	String getUsername();
	
	void setName(String value);

	String getSystemName();
}
