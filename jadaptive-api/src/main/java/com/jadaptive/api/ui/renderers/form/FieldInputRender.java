package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageResources;

public abstract class FieldInputRender implements PageResources {

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
	
	protected void load(Element e) throws IOException {
		
		URL url = getClass().getResource(getClass().getSimpleName()+ ".html");
		if(Objects.isNull(url)) {
			throw new IOException("Missing resource file " + getClass().getSimpleName() + ".html");
		}
		try(InputStream in = url.openStream()) {
			Document doc = Jsoup.parse(IOUtils.toString(in, "UTF-8"));
			Element body = doc.selectFirst("body");
			for(Element child : body.children()) {
				e.appendChild(child);
			}
		}
	}
	
	public String getHtmlResource() {
		return String.format("%s.html", getResourceClass().getSimpleName());
	}
	
	public String getJsResource() {
		return String.format("%s.js", getResourceClass().getSimpleName());
	}
	
	public String getCssResource() {
		return String.format("%s.css", getResourceClass().getSimpleName());
	}

}
