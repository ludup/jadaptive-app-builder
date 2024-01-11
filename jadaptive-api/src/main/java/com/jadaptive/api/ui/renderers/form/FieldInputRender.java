package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.TemplateViewField;

public abstract class FieldInputRender {

	String resourceKey;
	String formVariable;
	String bundle;
	String formVariableWithParents;
	boolean decorate = true;
	boolean disableIDAttribute = false;
	
	public FieldInputRender(TemplateViewField field) {
		this.resourceKey = field.getResourceKey();
		this.formVariable = field.getFormVariable();
		this.bundle = field.getBundle();
		
		StringBuffer formVariable = new StringBuffer();
		
		if(Objects.nonNull(field.getParentFields())) {
			for(FieldTemplate t : field.getParentFields()) {
				formVariable.append(t.getResourceKey());
				formVariable.append(".");
			}
		}
		
		formVariable.append(field.getFormVariable());
		formVariableWithParents = formVariable.toString();
	}
	
	public FieldInputRender(String resourceKey,String formVariable, String bundle) {
		this.resourceKey = resourceKey;
		this.formVariable = formVariable;
		this.bundle = bundle;
		this.formVariableWithParents = formVariable;
	}
	
	public void disableDecoration() {
		decorate = false;
	}
	
	public void disableIDAttribute() {
		this.disableIDAttribute = true;
	}
	
	protected String getFormVariableWithParents() {
		return formVariableWithParents;
	}
	
	protected String getFormVariable() {
		return formVariable;
	}
	protected String getBundle() {
		return bundle;
	}
	protected String getResourceKey() {
		return resourceKey;
	}
	
	public abstract void renderInput(Element rootElement, String value, String... classes) throws IOException;

}
