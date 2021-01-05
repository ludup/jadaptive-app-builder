package com.jadaptive.api.template;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

public class OrderedField {

	ObjectView view;
	FieldTemplate field;
	OrderedView panel;
	public OrderedField(ObjectView view, OrderedView panel, FieldTemplate field) {
		super();
		this.view = view;
		this.field = field;
		this.panel = panel;
	}
	
	public Integer getWeight() {
		return new Integer(view==null ? Integer.MIN_VALUE : view.weight());
	}
	
	public FieldTemplate getField() {
		return field;
	}

	public String getResourceKey() {
		return field.getResourceKey();
	}
	
	public String getBundle() {
		return (Objects.nonNull(view) && StringUtils.isNotBlank(view.bundle())) ? view.bundle() : panel.getBundle();
	}

	public FieldRenderer getRenderer() {
		return Objects.nonNull(view) ? view.renderer() : FieldRenderer.DEFAULT;
	}
}
