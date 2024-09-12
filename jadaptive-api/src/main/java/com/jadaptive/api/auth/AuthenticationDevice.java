package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.DynamicColumn;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.user.User;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = AuthenticationDevice.RESOURCE_KEY, scope = ObjectScope.PERSONAL, creatable = true, updatable = false, deletable = true, defaultColumn = "name")
@TableView( defaultColumns = {"name", "device.type", "lastModified"}, otherColumns = { @DynamicColumn(resourceKey = "device.type", service = AuthenticationDeviceService.class)})
public abstract class AuthenticationDevice extends PersonalUUIDEntity {

	public static final String RESOURCE_KEY = "authenticationDevices";
	
	private static final long serialVersionUID = -5802081948452215574L;
	
	@ObjectField(type = FieldType.TEXT, nameField = true, searchable = true)
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
