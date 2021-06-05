package com.jadaptive.api.ui.renderers;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.WordUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.ui.PageHelper;

public class DropdownInput extends InputRender {
	
	Element dropdownMenu;
	Element valueElement;
	Element nameElement;
	
	String bundle;
	public DropdownInput(String resourceKey, String bundle) {
		super(resourceKey);
		this.bundle = bundle;
	}

	@Override
	public Element renderInput() {

		return new Element("div").attr("class", "form-group")
				.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", resourceKey))
						.attr("style", "position: relative")
						.addClass("input-group")
						.addClass("dropdown")
					.appendChild(valueElement = new Element("input")
							.attr("id", resourceKey)
							.attr("name", resourceKey)
							.attr("type", "hidden"))
					.appendChild(nameElement = new Element("input")
							.attr("id", String.format("%sText", resourceKey))
							.attr("data-display", "static")
							.attr("class", "dropdown-toggle form-control")
							.attr("readonly", "readonly")
							.attr("type", "text")
							.attr("data-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false"))
					.appendChild(new Element("div")
							.attr("class", "input-group-append")
								.appendChild(new Element("span")
										.attr("class", ".jdropdown input-group-text")
									.appendChild(new Element("i")
											.attr("class", "fas fa-chevron-down"))))
					.appendChild(dropdownMenu = new Element("div")
							.attr("class", "dropdown-menu")
							.attr("aria-labelledby", String.format("%sDropdown", resourceKey))));
	}

	public void renderValues(Enum<?>[] values, String defaultValue, boolean i18n) {
		
		Enum<?> selected = null;
		for(Enum<?> value : values) {
			if(Objects.isNull(selected)) {
				selected = value;
			}
			addInputValue(String.valueOf(value.ordinal()), processEnumName(value.name()), i18n);
			if(value.name().equals(defaultValue)) {
				selected = value;
			}

		}
//		
		nameElement.val(processEnumName(selected.name()));
		valueElement.val(String.valueOf(selected.ordinal()));
	}
	
	private String processEnumName(String name) {
		return WordUtils.capitalizeFully(name.replace('_', ' '));
	}
	
	public void renderValues(Map<String,String> values, String defaultValue, boolean i18n) {
		
		for(Map.Entry<String,String> entry : values.entrySet()) {
			addInputValue(entry.getKey(), entry.getValue(), i18n);
			if(defaultValue.equals(entry.getKey())) {
				nameElement.attr("jad:bundle", bundle);
				nameElement.attr("jad:i18n", entry.getValue());
				valueElement.val(entry.getKey());
			}
		}
	}
	
	private void addInputValue(String key, String value, boolean i18n) {
		Element el = PageHelper.createAnchor("#", value)
				.attr("data-resourcekey", key)
				.addClass("jdropdown-item dropdown-item");
		if(i18n) {
			el.attr("jad:bundle", bundle);
			el.attr("jad:i18n", value);
		}
		dropdownMenu.appendChild(el);
	}

	private void renderValues(Iterable<? extends NamedUUIDEntity> fields, boolean i18n) {
		NamedUUIDEntity selected = null;
		for(NamedUUIDEntity field : fields) {
			if(Objects.isNull(selected)) {
				selected = field;
			}

			addInputValue(field.getResourceKey(), field.getName(), i18n);
			if(field.getResourceKey().equals(defaultValue)) {
				selected = field;
			}
			
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getName());
			valueElement.val(selected.getResourceKey());
		}
	}

	public Element renderInputWithValues(Iterable<? extends NamedUUIDEntity> children, boolean i18n) {
		Element el = renderInput();
		renderValues(children, i18n);
		return el;
	}
	
	public Element renderInputWithTemplateFields(Collection<FieldTemplate> fields) {
		Element el = renderInput();
		renderTemplateFields(fields);
		return el;
	}
	
	private void renderTemplateFields(Collection<FieldTemplate> fields) {
		
		FieldTemplate selected = null;
		for(FieldTemplate field : fields) {
			if(Objects.isNull(field)) {
				selected = field;
			}
			if(field.isSearchable()) {
				addInputValue(resourceKey, field.getResourceKey(), true);
				if(resourceKey.equals(defaultValue)) {
					selected = field;
				}
			}
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getResourceKey());
			valueElement.val(selected.getResourceKey());
		}
	}

	
}
