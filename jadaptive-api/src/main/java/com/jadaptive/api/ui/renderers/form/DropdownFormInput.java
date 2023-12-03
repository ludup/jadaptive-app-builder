package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageHelper;

public class DropdownFormInput extends FieldInputRender {
	
	Element dropdownMenu;
	Element dropdownInput;
	Element nameElement;
	Element valueElement;
	
	public DropdownFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public void renderInput(Element rootElement, String defaultValue) {

		
		rootElement.appendChild(new Element("div").addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
						.addClass("dropdownInput")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())))
				.appendChild(dropdownInput = new Element("div")
						.attr("id", String.format("%sDropdown", getResourceKey()))
						.addClass("input-group position-relative dropdown")
					.appendChild(valueElement = new Element("input")
							.attr("id", getFormVariable())
							.attr("name", getFormVariableWithParents())
							.attr("type", "hidden"))
					.appendChild(nameElement = new Element("input")
							.attr("id", String.format("%sText", getResourceKey()))
							.attr("data-display", "static")
							.addClass("form-control dropdown-toggle filter-dropdown")
							.attr("type", "text")
							.attr("data-bs-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false"))
					.appendChild(new Element("span")
										.attr("class", "jdropdown input-group-text")
									.appendChild(new Element("i")
											.attr("class", "fa-solid fa-chevron-down"))))
				.appendChild(new Element("small")
							.addClass("form-text")
							.addClass("text-muted")
							.attr("jad:bundle", getBundle())
							.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));

	}

	public void renderValues(Enum<?>[] values, String defaultValue, boolean readOnly) {
		
		if(!readOnly) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", getResourceKey())));
			
		}
		
		Enum<?> selected = null;
		for(Enum<?> value : values) {
			if(Objects.isNull(selected)) {
				selected = value;
			}
			if(!readOnly) {
				addInputValue(value.name(), processEnumName(value.name()));
			}
			if(value.name().equals(defaultValue) || String.valueOf(value.ordinal()).equals(defaultValue)) {
				selected = value;
			}
		}
		
		if(readOnly) {
			nameElement.attr("disabled", "disabled");
		}
		nameElement.val(processEnumName(selected.name()));
		valueElement.val(selected.name());
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
	
	public void addInputValue(String value, String name) {
		
		if(Objects.isNull(dropdownMenu)) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", getResourceKey())));
		}
		dropdownMenu.appendChild(PageHelper.createAnchor("#", name)
				.attr("data-resourcekey", value)
				.addClass("jdropdown-item dropdown-item"));
	}
	
	public void setSelectedValue(String value, String name) {
		nameElement.val(name);
		valueElement.val(value);
	}

	
}
