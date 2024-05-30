package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@ObjectDefinition(resourceKey = AuthenticationDevice.RESOURCE_KEY, scope = ObjectScope.PERSONAL, creatable = true, updatable = false, deletable = true)
@TableView( defaultColumns = "name")
@PageMenu(bundle = AuthenticationDevice.RESOURCE_KEY, parent = ApplicationMenuService.HOME_MENU_UUID, i18n = "2faDevices.names", icon = "fa-mobile-phone", path = "/app/ui/search/authenticationDevices")
public abstract class AuthenticationDevice extends PersonalUUIDEntity {

	public static final String RESOURCE_KEY = "authenticationDevices";
	
	private static final long serialVersionUID = -5802081948452215574L;
	
	@ObjectField(type = FieldType.LONG, hidden = true, unique = true)
	protected Long uniqueId;
	
	@ObjectField(type = FieldType.TEXT, nameField = true)
	protected String name;

	public Long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(Long uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
