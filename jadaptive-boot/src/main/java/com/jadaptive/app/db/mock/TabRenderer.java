package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;

//@ObjectDefinition(resourceKey = TabRenderer.RESOURCE_KEY)
//@ObjectViews({ @ObjectViewDefinition(value = TabRenderer.VIEW_OBJECT1_TAB, bundle = TabRenderer.RESOURCE_KEY),
//	@ObjectViewDefinition(value = TabRenderer.VIEW_OBJECT2_TAB, bundle = TabRenderer.RESOURCE_KEY, weight = 100)})
//@TableView(defaultColumns = { "name", "field1", "field2" })
public class TabRenderer extends NamedUUIDEntity {

	private static final long serialVersionUID = -4142337313881674158L;

	public static final String RESOURCE_KEY = "tabRenderer";

	public static final String VIEW_OBJECT1_TAB = "obj1";
	public static final String VIEW_OBJECT2_TAB = "obj2";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = "")
	@ObjectView(VIEW_OBJECT1_TAB)
	Object1 obj1;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = "")
	@ObjectView(VIEW_OBJECT2_TAB)
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
