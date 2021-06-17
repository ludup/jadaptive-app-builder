package com.jadaptive.plugins.sshd;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.ViewType;

@ObjectDefinition(resourceKey = SSHInterface.RESOURCE_KEY, aliases = "sshInterfaces")
@ObjectServiceBean(bean = SSHInterfaceService.class)
@ObjectViews({
	@ObjectViewDefinition(value=SSHInterface.VIEW_INTERFACE, bundle = SSHInterface.RESOURCE_KEY, type = ViewType.ACCORDION)})
@TableView(defaultColumns = { "name", "addressToBind", "portToBind" })
public abstract class SSHInterface extends NamedUUIDEntity {

	public static final String RESOURCE_KEY = "sshInterface";
	public static final String VIEW_INTERFACE = "interface";
	
	private static final long serialVersionUID = 3220349031259390699L;

	@ObjectField(required = true, type = FieldType.TEXT)
	String addressToBind;
	
	@ObjectField(required = true, type = FieldType.INTEGER, defaultValue = "22")
	@Validator(type = ValidationType.RANGE, value = "1-65535", bundle = RESOURCE_KEY, i18n = "port.invalid")
	int portToBind;
	
	public String getAddressToBind() {
		return addressToBind;
	}

	public void setAddressToBind(String addressToBind) {
		this.addressToBind = addressToBind;
	}

	public int getPortToBind() {
		return portToBind;
	}

	public void setPortToBind(int portToBind) {
		this.portToBind = portToBind;
	}

	public abstract Class<? extends SSHInterfaceFactory> getInterfaceFactory();

	public String getInterface() { 
		return String.format("%s:%d", getAddressToBind(), getPortToBind()); 
	}

}
