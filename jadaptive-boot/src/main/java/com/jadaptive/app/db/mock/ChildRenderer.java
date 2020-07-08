package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = ChildRenderer.RESOURCE_KEY)
public class ChildRenderer extends AbstractUUIDEntity {

	private static final long serialVersionUID = -1022570617998580427L;

	public static final String RESOURCE_KEY = "childRenderer";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.TEXT)
	String childText;

	public String getChildText() {
		return childText;
	}

	public void setChildText(String childText) {
		this.childText = childText;
	}
	
	
}
