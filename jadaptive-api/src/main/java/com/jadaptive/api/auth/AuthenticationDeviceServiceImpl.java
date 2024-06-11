package com.jadaptive.api.auth;

import java.util.Objects;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@Service
@PageMenu(bundle = AuthenticationDevice.RESOURCE_KEY, parent = ApplicationMenuService.HOME_MENU_UUID, i18n = "2faDevices.names", icon = "fa-mobile-phone", path = "/app/ui/search/authenticationDevices")
public class AuthenticationDeviceServiceImpl implements AuthenticationDeviceService {

	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {
		FieldTemplate type = rowTemplate.getField("deviceType");
		if(Objects.nonNull(type)) {
			return Html.i18n(rowTemplate.getBundle(), obj.getValue(type) + ".name");
		}
		return Html.i18n(rowTemplate.getBundle(), rowTemplate.getResourceKey() + ".name");
	}

	
}
