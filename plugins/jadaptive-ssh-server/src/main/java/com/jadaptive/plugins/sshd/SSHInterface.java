package com.jadaptive.plugins.sshd;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.ViewType;
import com.sshtools.common.ssh.SecurityLevel;

@ObjectDefinition(resourceKey = SSHInterface.RESOURCE_KEY, aliases = "sshInterfaces", defaultColumn = "name", system = true)
@ObjectServiceBean(bean = SSHInterfaceService.class)
@ObjectViews({
	@ObjectViewDefinition(value=SSHInterface.VIEW_INTERFACE, bundle = SSHInterface.RESOURCE_KEY, type = ViewType.ACCORDION)})
@TableView(defaultColumns = { "name", "addressToBind", "portToBind", "securityLevel" })
public abstract class SSHInterface extends NamedUUIDEntity {

	public static final String RESOURCE_KEY = "sshInterface";
	public static final String VIEW_INTERFACE = "interface";
	
	private static final long serialVersionUID = 3220349031259390699L;

	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	@ObjectView(SSHInterface.VIEW_INTERFACE)
	String addressToBind;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "22")
	@Validator(type = ValidationType.RANGE, value = "1-65535", bundle = RESOURCE_KEY, i18n = "port.invalid")
	@ObjectView(SSHInterface.VIEW_INTERFACE)
	int portToBind;
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "STRONG")
	@ObjectView(SSHInterface.VIEW_INTERFACE)
	SecurityLevel securityLevel;
	
	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
	}

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

	public abstract Class<? extends SSHInterfaceFactory<?,?>> getInterfaceFactory();

	public String getInterface() { 
		return String.format("%s:%d", getAddressToBind(), getPortToBind()); 
	}

}
