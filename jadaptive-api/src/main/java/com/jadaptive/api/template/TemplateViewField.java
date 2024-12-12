package com.jadaptive.api.template;

import java.util.LinkedList;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.jadaptive.api.entity.AbstractObject;

public class TemplateViewField {

	FieldTemplate field;
	TemplateView panel;
	LinkedList<FieldTemplate> objectPath;
	int weight = 0;
	String bundle = null;
	boolean disableEncoding;
	FieldRenderer renderer = null;
	ObjectView view;
	boolean parentIsHidden;
	
	public TemplateViewField(ObjectView view, TemplateView panel, FieldTemplate field, LinkedList<FieldTemplate> objectPath, boolean parentIsHidden) {
		super();
		this.view = view;
		this.field = field;
		this.panel = panel;
		this.objectPath = objectPath;
		this.weight = view != null ? view.weight() : 0;
		this.disableEncoding = Objects.nonNull(view) && view.disableEncoding();
		this.bundle = Objects.nonNull(view) && StringUtils.isNotBlank(view.bundle()) ? view.bundle() : panel.getBundle();
		this.renderer = view != null ? view.renderer() : FieldRenderer.DEFAULT;
		this.parentIsHidden = parentIsHidden;
	}
	
	public Integer getWeight() {
		return weight;
	}
	
	public FieldTemplate getField() {
		return field;
	}
	
	public LinkedList<FieldTemplate> getParentFields() {
		if(Objects.isNull(objectPath) || objectPath.isEmpty()) {
			return null;
		} else {
			return objectPath;
		}
	}
	
	public String getVariable() {
		if(Objects.isNull(objectPath) || objectPath.isEmpty()) {
			return field.getFormVariable();
		} 
		
		StringBuffer buf = new StringBuffer();
		for(FieldTemplate t : objectPath) {
			buf.append(t.getFormVariable());
			buf.append(".");
		}
		buf.append(field.getFormVariable());
		return buf.toString();
	}

	public boolean isDisableEncoding() {
		return disableEncoding;
	}
	public String getFormVariable() {
		return field.getFormVariable();
	}
	
	public String getResourceKey() {
		return field.getResourceKey();
	}
	
	public String getBundle() {
		return bundle;
	}

	public FieldRenderer getRenderer() {
		return renderer;
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
		if(Objects.isNull(obj)) {
			return field.getDefaultValue();
		}
		Object val = obj.getValue(field);
		if(Objects.isNull(val)) {
			return field.getDefaultValue();
		}
		return String.valueOf(val);
	}

	public boolean requiresDecryption() {
		if(field.getFieldType()==FieldType.PASSWORD) {
			return false;
		}
		return field.isManuallyEncrypted() || field.isAutomaticallyEncrypted();
	}
	
//	public boolean isOptional() {
//		return Objects.nonNull(view) && StringUtils.isNotBlank(view.dependsOn());
//	}
//	
//	public String getDependsOn() {
//		return view.dependsOn();
//	}
//	
//	public String getDependsValue() {
//		return view.dependsValue();
//	}
	
	public boolean isAutoSave() {
		return Objects.nonNull(view) && view.autosave();
	}

	public boolean isSystemOnly() {
		return Objects.nonNull(view) && view.systemOnly();
	}
	
	public boolean isHidden() {
		return field.isHidden() || parentIsHidden;
	}
}
