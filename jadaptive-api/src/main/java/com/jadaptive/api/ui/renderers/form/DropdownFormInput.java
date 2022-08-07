package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.PageHelper;

public class DropdownFormInput extends FieldInputRender {
	
	Element dropdownMenu;
	Element dropdownInput;
	Element nameElement;
	Element valueElement;
	
	public DropdownFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public void renderInput(OrderedView panel, Element rootElement, String defaultValue) {

		rootElement.appendChild(new Element("div").addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
						.addClass("dropdownInput")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(dropdownInput = new Element("div")
						.attr("id", String.format("%sDropdown", field.getResourceKey()))
						.attr("style", "position: relative")
						.addClass("input-group")
						.addClass("dropdown")
					.appendChild(valueElement = new Element("input")
							.attr("id", field.getFormVariable())
							.attr("name", field.getFormVariable())
							.attr("type", "hidden"))
					.appendChild(nameElement = new Element("input")
							.attr("id", String.format("%sText", field.getResourceKey()))
							.attr("data-display", "static")
							.addClass("form-control")
							.addClass("dropdown-toggle")
							.attr("type", "text")
							.attr("data-bs-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false"))
					.appendChild(new Element("span")
										.attr("class", "jdropdown input-group-text")
									.appendChild(new Element("i")
											.attr("class", "fas fa-chevron-down"))))
				.appendChild(new Element("small")
							.addClass("form-text")
							.addClass("text-muted")
							.attr("jad:bundle", field.getBundle())
							.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));

	}

	public void renderValues(Enum<?>[] values, String defaultValue, boolean readOnly) {
		
		if(!readOnly) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu")
					.attr("style", "max-height: 300px; overflow-y: scroll;")
					.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey())));
			
		}
		
		Enum<?> selected = null;
		for(Enum<?> value : values) {
			if(Objects.isNull(selected)) {
				selected = value;
			}
			if(!readOnly) {
				addInputValue(String.valueOf(value.ordinal()), processEnumName(value.name()));
			}
			if(value.name().equals(defaultValue)) {
				selected = value;
			}
		}
		
		if(readOnly) {
			nameElement.attr("disabled", "disabled");
		}
		nameElement.val(processEnumName(selected.name()));
		valueElement.val(String.valueOf(selected.ordinal()));
	}
	
	public void renderValues(Collection<String> values, String defaultValue) {
		
		String selected = null;
		for(String value : values) {
			if(Objects.isNull(selected)) {
				selected = value;
			}
			addInputValue(value, value);
			if(value.equals(defaultValue)) {
				selected = value;
			}
		}
		
		nameElement.val(processEnumName(selected));
		valueElement.val(String.valueOf(selected));
	}
	
	private String processEnumName(String name) {
		return name.replace('_', ' ');
	}
	
	private void addInputValue(String key, String value) {
		dropdownMenu.appendChild(PageHelper.createAnchor("#", value)
				.attr("data-resourcekey", key)
				.addClass("jdropdown-item dropdown-item"));
	}

	
}
