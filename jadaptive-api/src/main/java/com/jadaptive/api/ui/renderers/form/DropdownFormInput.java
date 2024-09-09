package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.ui.PageHelper;

public class DropdownFormInput extends FieldInputRender {
	
	Element dropdownMenu;
	Element dropdownInput;
	Element nameElement;
	Element valueElement;
	
	public DropdownFormInput(TemplateViewField field) {
		super(field);
	}
	
	public DropdownFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	@Override
	public void renderInput(Element rootElement, String defaultValue, String... classes) {

		
		Element e;
		rootElement.appendChild( e =new Element("div"));
		e.addClass("dropdownInput");
		
		if(decorate) {
				
				e.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		}
		
		e.appendChild(dropdownInput = new Element("div")
				.addClass(String.format("%sDropdown", getResourceKey()) + " input-group position-relative dropdown")
			.appendChild(valueElement = new Element("input")
					.addClass(resourceKey)
					.attr("name", getFormVariableWithParents())
					.attr("type", "hidden"))
			.appendChild(nameElement = new Element("input")
					.attr("data-display", "static")
					.addClass(String.format("%sText", getResourceKey()) + " form-control dropdown-toggle filter-dropdown")
					.attr("type", "text")
					.attr("data-bs-toggle", "dropdown")
					.attr("aria-haspopup", "true")
					.attr("aria-expanded", "false"))
			.appendChild(new Element("span")
								.attr("class", "jdropdown input-group-text")
							.appendChild(new Element("i")
									.attr("class", "fa-solid fa-chevron-down"))));
		
		if(decorate) {
			e.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}
		
		if(!disableIDAttribute) {
			dropdownInput.attr("id", String.format("%sDropdown", getResourceKey()));
			valueElement.attr("id", resourceKey);
			nameElement.attr("id", String.format("%sText", getResourceKey()));
		}

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
	
	public void renderValues(Map<String,String> values, String defaultValue) {
		
		Map.Entry<String,String> selected = null;
		for(Map.Entry<String,String> value : values.entrySet()) {
			
			addInputValue(value.getKey(), value.getValue());
			if(value.getKey().equals(defaultValue)) {
				selected = value;
			}
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getValue());
			valueElement.val(selected.getKey());
		}
	}
	
	public void renderCollectionValues(Collection<? extends NamedDocument> values, String defaultValue) {
		
		NamedDocument selected = null;
		for(NamedDocument value : values) {
			addInputValue(value.getUuid(), value.getName());
			if(value.getUuid().equals(defaultValue)) {
				selected = value;
			}
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getName());
			valueElement.val(selected.getUuid());
		}
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

	public void renderNamePairValues(Collection<NamePairValue> values, String defaultValue) {
		
		NamePairValue selected = null;
		for(NamePairValue value : values) {
			addInputValue(value.getValue(), value.getName());
			if(value.getValue().equals(defaultValue)) {
				selected = value;
			}
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getName());
			valueElement.val(selected.getValue());
		}
	}

	
}
