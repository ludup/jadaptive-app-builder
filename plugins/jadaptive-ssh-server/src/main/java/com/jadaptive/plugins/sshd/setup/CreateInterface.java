package com.jadaptive.plugins.sshd.setup;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = CreateInterface.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class CreateInterface extends UUIDEntity {

	private static final long serialVersionUID = -4229613403580844237L;

	public static final String RESOURCE_KEY = "createInterface";
	
	@ObjectField(required = true, defaultValue = "::", type = FieldType.TEXT)
	String addressToBind;
	
	@ObjectField(required = true, defaultValue = "2222", type = FieldType.INTEGER)
	@Validator(type = ValidationType.RANGE, value = "1-65535", bundle = RESOURCE_KEY, i18n = "port.invalid")
	Integer port;
	

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}


	public String getAddressToBind() {
		return addressToBind;
	}


	public void setAddressToBind(String addressToBind) {
		this.addressToBind = addressToBind;
	}


	public Integer getPort() {
		return port;
	}


	public void setPort(Integer port) {
		this.port = port;
	}
	
	
	
}
