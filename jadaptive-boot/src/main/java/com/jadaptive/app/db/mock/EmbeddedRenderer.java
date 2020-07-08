package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = EmbeddedRenderer.RESOURCE_KEY)
public class EmbeddedRenderer extends AbstractUUIDEntity {

	private static final long serialVersionUID = -1022570617998580427L;

	public static final String RESOURCE_KEY = "embeddedRenderer";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.TEXT)
	String embeddedText;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	ChildRenderer child;

	public String getEmbeddedText() {
		return embeddedText;
	}

	public void setEmbeddedText(String embeddedText) {
		this.embeddedText = embeddedText;
	}

	public ChildRenderer getChild() {
		return child;
	}

	public void setChild(ChildRenderer child) {
		this.child = child;
	}
	
	
}
