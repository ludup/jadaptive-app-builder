package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;

public abstract class FormInputRender extends FieldInputRender {
	
	private boolean decorate = true;
	Element input;
	
	public FormInputRender(ObjectTemplate template, TemplateViewField field) {
		super(field);
	}
	
	public FormInputRender(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}
	
	public FormInputRender(String resourceKey,String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}
	
	public void disableDecoration() {
		this.decorate = false;
	}
	
	public final void renderInput(Element rootElement, String value, String... classes) {
		
		if(decorate) {
			
			rootElement
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		}
		
		rootElement.appendChild(Html.div("input-group").appendChild(input = createInputElement(value)));
		if(decorate) {
			createHelpElement(rootElement, value);
		}
		
		if(!disableIDAttribute) {
			input.attr("id", resourceKey);
		}
		
		onRender(rootElement, value);

	}
	
	protected void createHelpElement(Element parent, String value) {
		
		parent.appendChild(new Element("small")
				.addClass("form-text")
				.addClass("text-muted")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
	}

	public Element getInputElement() {
		return input;
	}

	protected Element createInputElement(String value) {
		return new Element("input")
						.attr("name", getFormVariableWithParents())
						.addClass(resourceKey + " form-control")
						.attr("value", value)
						.attr("autocomplete", getAutocomplete())
						.attr("type", getInputType());
	}

	protected String getAutocomplete() {
		return "off";
	}

	protected void onRender(Element rootElement, String value) { }
	
	public abstract String getInputType();
}
