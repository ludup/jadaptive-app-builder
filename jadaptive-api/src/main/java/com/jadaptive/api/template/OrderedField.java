package com.jadaptive.api.template;

public class OrderedField {

	ObjectView view;
	FieldTemplate field;
	public OrderedField(ObjectView view, FieldTemplate field) {
		super();
		this.view = view;
		this.field = field;
	}
	
	public Integer getWeight() {
		return new Integer(view==null ? Integer.MIN_VALUE : view.weight());
	}
	
	public FieldTemplate getField() {
		return field;
	}
}
