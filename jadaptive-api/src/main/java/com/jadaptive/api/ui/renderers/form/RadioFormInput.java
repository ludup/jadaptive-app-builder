package com.jadaptive.api.ui.renderers.form;

import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;

public class RadioFormInput extends FieldInputRender {

	private Element nameElement;
	private Element valueElement;
	private Element e;

	public RadioFormInput(TemplateViewField field) {
		super(field);
	}

	public RadioFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	@Override
	public void renderInput(Element rootElement, String defaultValue, String... classes) {

		rootElement.appendChild(
				e =new Element("div").
				addClass("row mb-3"));

		Element div;
		e.appendChild(div = new Element("div")
				.addClass("col-12"));

		if(decorate) {
			div.appendChild(new Element("label").
				attr("for", getFormVariable()).
				addClass("form-label").
				attr("jad:bundle", getBundle()).
				attr("jad:i18n", String.format("%s.name", getResourceKey())));
			
			rootElement.appendElement("div").
				addClass("col-12").
				appendElement("p").
					addClass("form-text text-muted text-small mt-3").
					attr("jad:bundle", getBundle()).
					attr("jad:i18n", String.format("%s.desc", getResourceKey()));
		}
	}

	public void renderValues(Enum<?>[] values, String defaultValue, boolean readOnly) {

		Enum<?> selected = null;

		for(Enum<?> value : values) {
			if(value.name().equals(defaultValue) || String.valueOf(value.ordinal()).equals(defaultValue)) {
				selected = value;
			}
		}
		
		if(Objects.isNull(selected)) {
			selected = values[0];
		}
		
		for(Enum<?> value : values) {

			var col = e.appendElement("div").
				addClass("col-12");
			
			var div = col.appendElement("div").
				addClass("form-check");
			
			var input = div.appendElement("input").
				addClass("form-check-input").
				attr("name", getFormVariable()).
				attr("type", "radio").
				attr("value", value.name());
			
			if(value.equals(selected)) {
				input.attr("checked", "checked");
			}
			
			if(readOnly) {
				input.attr("disabled", "disabled");
			}
			
			var label = div.appendElement("label").
				addClass("form-check-label").
				attr("type", "radio").html(processEnumName(value.name()));
			
			if(!disableIDAttribute) {
				input.attr("id", getResourceKey() + value.name());
				label.attr("for", getResourceKey() + value.name());
			}
		}
	}

	private String processEnumName(String name) {
		return name.replace('_', ' ');
	}

	public void setSelectedValue(String value, String name) {
		nameElement.val(name);
		valueElement.val(value);
	}


}
