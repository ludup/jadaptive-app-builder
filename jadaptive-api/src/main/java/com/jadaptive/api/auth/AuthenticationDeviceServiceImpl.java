package com.jadaptive.api.auth;

import java.util.Objects;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.Html;

@Service
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
