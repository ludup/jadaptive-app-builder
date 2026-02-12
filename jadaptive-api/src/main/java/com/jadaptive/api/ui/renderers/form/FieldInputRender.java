package com.jadaptive.api.ui.renderers.form;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageResources;

public abstract class FieldInputRender implements PageResources {

	String resourceKey;
	String formVariable;
	String bundle;
	String formVariableWithParents;
	boolean decorate = true;
	boolean labelOnly = false;
	boolean disableIDAttribute = false;
	
	protected FieldInputRender() {

	}

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
	
	public void init(String resourceKey,String formVariable, String bundle) {
		this.resourceKey = resourceKey;
		this.formVariable = formVariable;
		this.bundle = bundle;
		this.formVariableWithParents = formVariable;
	}
	
	public Element elementForRole(Element parent, String role) {
		return elementForRoleOr(parent, role).orElseThrow(() -> new IllegalStateException("No element with role:" + role + " in template."));
	}

	public Optional<Element> elementForRoleOr(Element parent, String role) {
		return ofNullable(parent.getElementsByAttributeValue("jad:role", role).first());
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
	
	public boolean isLabelOnly() {
		return labelOnly;
	}
	
	public void disableDescription() {
		this.labelOnly = true;
	}
	
	public final void renderInput(Element rootElement, String value, boolean readOnly, String... classes) {
		load(rootElement);
		onRender(rootElement, value, readOnly, classes);
	}
	
	protected abstract void onRender(Element rootElement, String value, boolean readOnly, String... classes);
	
	public void load(Element e) {
		
		URL url = getClass().getResource(getClass().getSimpleName()+ ".html");
		if(Objects.nonNull(url)) {
			try(InputStream in = url.openStream()) {
				Document doc = Jsoup.parse(IOUtils.toString(in, "UTF-8"));
				Element body = doc.selectFirst("body");
				for(Element child : body.children()) {
					e.appendChild(child);
				}
			} catch(IOException ex) {
				throw new IllegalStateException("Unable to load resource file " + getClass().getSimpleName() + ".html");
			}
		}
		url = getClass().getResource(getJsResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendBodyScript(e.ownerDocument(), "/app/script/" + getResourceClass().getPackageName().replace('.', '/') + "/" + getJsResource());
		}
		
		url = getClass().getResource(getCssResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(e.ownerDocument(),  "/app/style/" + getResourceClass().getPackageName().replace('.', '/') + "/" + getCssResource());
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
