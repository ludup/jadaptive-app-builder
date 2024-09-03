package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.utils.Utils;

public class BooleanFormInput extends FieldInputRender {

	Element input;
	
	public BooleanFormInput(TemplateViewField field) {
		super(field);
	}

	public BooleanFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) {

		renderTop(rootElement);
		
		Element container = createContainer();
		
		rootElement.appendChild(new Element("div")
				.addClass("col-12 " + Utils.csv(" ", classes))
					.appendChild(container));
		
		if(decorate) {
			rootElement.appendChild(new Element("div")
						.addClass("col-12"))
				.appendChild(new Element("p")
				.addClass("form-text text-muted text-small")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}
		
		if(!disableIDAttribute) {
			input.attr("id", resourceKey);
		}

		if("true".equalsIgnoreCase(value)) {
			input.attr("checked", "checked");
		}
	}

	public void disable() {
		input.attr("disabled", "disabled");
	}

	protected void renderTop(Element e) {
	}

	protected final Element createLabel() {
		return new Element("label")
			.attr("for", getFormVariable())
			.addClass("form-label")
			.attr("jad:bundle", getBundle())
			.attr("jad:i18n", String.format("%s.name", getResourceKey()));
	}

	protected Element createContainer() {
		return createContainerElement().appendChild(createLabel());
	}

	protected final Element createContainerElement() {
		return new Element("div")
			.addClass("col-12 form-check")
			.appendChild(input = new Element("input")
				.attr("name", getFormVariable())
				.attr("type", "checkbox")
				.addClass(resourceKey + " form-check-input")
				.val("true"));
	}

}
