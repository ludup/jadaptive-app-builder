package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = TestRenderer.RESOURCE_KEY)
public class TestRenderer extends NamedUUIDEntity {

	private static final long serialVersionUID = -4142337313881674158L;

	public static final String RESOURCE_KEY = "testRenderer";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	EmbeddedRenderer embedded;

	public EmbeddedRenderer getEmbedded() {
		return embedded;
	}

	public void setEmbedded(EmbeddedRenderer embedded) {
		this.embedded = embedded;
	}
	
	
}
