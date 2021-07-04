package com.jadaptive.api.template;

import java.util.LinkedList;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.jadaptive.api.entity.AbstractObject;

public class OrderedField {

	ObjectView view;
	FieldTemplate field;
	OrderedView panel;
	LinkedList<FieldTemplate> objectPath;
	
	public OrderedField(ObjectView view, OrderedView panel, FieldTemplate field, LinkedList<FieldTemplate> objectPath) {
		super();
		this.view = view;
		this.field = field;
		this.panel = panel;
		this.objectPath = objectPath;
	}
	
	public Integer getWeight() {
		return Integer.valueOf(view==null ? Integer.MIN_VALUE : view.weight());
	}
	
	public FieldTemplate getField() {
		return field;
	}
	
	public FieldTemplate getParentField() {
		if(Objects.isNull(objectPath) || objectPath.isEmpty()) {
			return null;
		} else {
			return objectPath.getLast();
		}
	}

	public String getFormVariable() {
		return field.getFormVariable();
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
	
	public String getFieldValue(AbstractObject obj) {
		if(Objects.isNull(obj)) {
			return field.getDefaultValue();
		}
		if(Objects.nonNull(objectPath) && !objectPath.isEmpty()) {
			for(FieldTemplate objectField : objectPath) {
				obj = obj.getChild(objectField);
			}
		}
		Object val = obj.getValue(field);
		if(Objects.isNull(val)) {
			return field.getDefaultValue();
		}
		return String.valueOf(val);
	}
}
