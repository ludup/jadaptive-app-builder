package com.jadaptive.api.ui.renderers;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.ui.PageHelper;

public class DropdownInput extends InputRender {
	
	Element dropdownMenu;
	Element valueElement;
	Element nameElement;
	boolean up;
	boolean dark;
	String bundle;
	
	public DropdownInput(String resourceKey, String bundle) {
		super(resourceKey);
		this.bundle = bundle;
	}

	public DropdownInput up() {
		this.up = true;
		return this;
	}
	public DropdownInput down() {
		this.up = false;
		return this;
	}
	
	public DropdownInput dark() {
		this.dark = true;
		return this;
	}
	
	@Override
	public Element renderInput() {

		return new Element("div").attr("class", "row")
				.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", resourceKey))
						.addClass("input-group position-relative dropdown" + (up ? " dropup" : ""))
					.appendChild(valueElement = new Element("input")
							.attr("id", resourceKey)
							.attr("name", resourceKey)
							.attr("type", "hidden"))
					.appendChild(nameElement = new Element("input")
							.attr("id", String.format("%sText", resourceKey))
							.attr("data-display", "static")
							.addClass("dropdown-toggle filter-dropdown form-control" + (dark ? " text-light" : ""))
							.attr("readonly", "readonly")
							.attr("type", "text")
							.attr("autocomplete", "off")
							.attr("data-bs-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false"))
					.appendChild(new Element("a")
							.attr("role", "button")
							.attr("class", "jdropdown input-group-text text-decoration-none")
						.appendChild(new Element("i")
								.attr("class", "fa-solid fa-chevron-down")))
					.appendChild(dropdownMenu = new Element("div")
							.addClass("dropdown-menu dropdown-size" + (dark ? " dropdown-menu-dark" : ""))
							.attr("aria-labelledby", String.format("%sDropdown", resourceKey))));
	}
	
	public void setDefaultValue(String name, String value, boolean i18n, String bundle) {
		
		if(i18n) {
			nameElement.attr("jad:bundle", bundle);
			nameElement.attr("jad:i18n", name);
		} else {
			nameElement.val(name);

		}
		
		valueElement.val(value);
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
		return name.replace('_', ' ');
	}
	
	public void renderValues(Map<String,String> values, String defaultValue, boolean i18n) {
		
		for(Map.Entry<String,String> entry : values.entrySet()) {
			addInputValue(entry.getKey(), entry.getValue(), i18n);
			if(defaultValue.equals(entry.getKey())) {
				if(!i18n) {
					nameElement.val(entry.getValue());
				} else {
					nameElement.attr("jad:bundle", bundle);
					nameElement.attr("jad:i18n", entry.getValue());
				}
				valueElement.val(entry.getKey());
			}
		}
	}
	
	public void renderValues(Collection<I18nOption> values, String defaultValue) {
		
		for(I18nOption value : values) {
			addInputValue(value);
			if(Objects.nonNull(defaultValue) && defaultValue.equals(value.getValue())) {
				nameElement.attr("jad:bundle", value.getBundle());
				nameElement.attr("jad:i18n", value.getI18n());
				valueElement.val(value.getValue());
			}
		}
	}
	
	public void addEmptyValue() {
		Element el = PageHelper.createAnchor("#", " ")
				.attr("data-resourcekey", "")
				.addClass("jdropdown-item dropdown-item");
	
		dropdownMenu.appendChild(el);
	}
	
	private void addInputValue(I18nOption value) {
		Element el = PageHelper.createAnchor("#", value.getValue())
				.attr("data-resourcekey", value.getValue())
				.addClass("jdropdown-item dropdown-item");

		el.attr("jad:bundle", value.getBundle());
		el.attr("jad:i18n", value.getI18n());
	
		dropdownMenu.appendChild(el);
	}
	
	public void addInputValue(String key, String value, boolean i18n) {
		addInputValue(key, value, i18n, bundle);
	}
	
	public void addInputValue(String key, String value, boolean i18n, String bundle) {
		Element el = PageHelper.createAnchor("#", value)
				.attr("data-resourcekey", key)
				.addClass("jdropdown-item dropdown-item");
		if(i18n) {
			el.attr("jad:bundle", bundle);
			el.attr("jad:i18n", value);
		}
		dropdownMenu.appendChild(el);
	}

	private void renderValues(Iterable<? extends NamedDocument> fields, String defaultValue, boolean i18n, String append) {
		NamedDocument selected = null;
		for(NamedDocument field : fields) {
			if(Objects.isNull(selected)) {
				selected = field;
			}

			addInputValue(field.getUuid(), field.getName() + append, i18n);
			if(field.getUuid().equals(defaultValue)) {
				selected = field;
			}
			
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getName());
			valueElement.val(selected.getUuid());
		}
	}

	public Element renderInputWithValues(Iterable<? extends NamedDocument> children, String defaultValue, boolean i18n) {
		return renderInputWithValues(children, defaultValue, i18n, "");
	}
	
	public Element renderInputWithValues(Iterable<? extends NamedDocument> children, String defaultValue, boolean i18n, String append) {
		Element el = renderInput();
		renderValues(children, defaultValue, i18n, append);
		return el;
	}
	
	public Element renderInputWithTemplateFields(Collection<FieldTemplate> fields, String defaultValue) {
		Element el = renderInput();
		renderTemplateFields(fields, defaultValue);
		return el;
	}
	
	private void renderTemplateFields(Collection<FieldTemplate> fields, String defaultValue) {
		
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
