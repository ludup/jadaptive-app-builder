package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.utils.Utils;

public abstract class FormInputRender extends FieldInputRender {
	
	private boolean decorate = true;
	
	public FormInputRender(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public FormInputRender(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}
	
	public void disableDecoration() {
		this.decorate = false;
	}
	
	public final void renderInput(Element rootElement, String value, String... classes) {
		
		Element myElement;
		Element input;
		rootElement.appendChild(myElement = 
				new Element("div").addClass(Utils.csv(" ", classes) + " row mb-3"));
		
		if(decorate) {
			
			myElement.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey()))));
		}
		
		myElement.appendChild(input = new Element("input")
						.attr("name", getFormVariableWithParents())
						.addClass(resourceKey + " form-control")
						.attr("value", value)
						.attr("autocomplete", "off")
						.attr("type", getInputType()));
		if(decorate) {
			myElement.appendChild(new Element("small")
					.addClass("form-text")
					.addClass("text-muted")
					.attr("jad:bundle", getBundle())
					.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}
		
		if(!disableIDAttribute) {
			input.attr("id", resourceKey);
		}
		
		onRender(myElement, value);

	}

	protected void onRender(Element rootElement, String value) { }
	
	public abstract String getInputType();
}
