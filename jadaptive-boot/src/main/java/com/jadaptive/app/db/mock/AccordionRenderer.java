package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.ViewType;

@ObjectDefinition(resourceKey = AccordionRenderer.RESOURCE_KEY)
@ObjectViews({ @ObjectViewDefinition(value = AccordionRenderer.VIEW_OBJECT1, type = ViewType.ACCORDION),
	@ObjectViewDefinition(value = AccordionRenderer.VIEW_OBJECT2, type = ViewType.ACCORDION, weight = 100)})
public class AccordionRenderer extends NamedUUIDEntity {

	private static final long serialVersionUID = -4142337313881674158L;

	public static final String RESOURCE_KEY = "accordionRenderer";

	public static final String VIEW_OBJECT1 = "obj1";
	public static final String VIEW_OBJECT2 = "obj2";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = "")
	@ObjectView(VIEW_OBJECT1)
	Object1 obj1;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = "")
	@ObjectView(VIEW_OBJECT2)
	Object2 obj2;

	public Object1 getObj1() {
		return obj1;
	}

	public void setObj1(Object1 obj1) {
		this.obj1 = obj1;
	}

	public Object2 getObj2() {
		return obj2;
	}

	public void setObj2(Object2 obj2) {
		this.obj2 = obj2;
	}

	
}
