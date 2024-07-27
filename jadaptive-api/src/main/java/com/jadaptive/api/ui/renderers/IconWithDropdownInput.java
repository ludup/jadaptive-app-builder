package com.jadaptive.api.ui.renderers;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.WordUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.utils.Utils;

public class IconWithDropdownInput extends InputRender {
	
	Element dropdownMenu;
	Element valueElement;
	Element nameElement;
	boolean up;
	boolean dark;
	String bundle;
	String icon = "fa-theater-masks";
	String iconGroup = "fa-solid";

	public IconWithDropdownInput(String resourceKey, String bundle) {
		super(resourceKey);
		this.bundle = bundle;
	}

	public IconWithDropdownInput icon(String icon) {
		this.icon = icon;
		return this;
	}
	
	public IconWithDropdownInput group(String icon) {
		this.icon = icon;
		return this;
	}
	
	public IconWithDropdownInput up() {
		this.up = true;
		return this;
	}
	public IconWithDropdownInput down() {
		this.up = false;
		return this;
	}
	
	public IconWithDropdownInput dark() {
		this.dark = true;
		return this;
	}
	
	@Override
	public Element renderInput() {

		return new Element("div").attr("class", "row")
				.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", resourceKey))
						.addClass("position-relative dropdown" + (up ? " dropup" : ""))
					.appendChild(valueElement = new Element("input")
							.attr("name", resourceKey)
							.attr("type", "hidden"))
					.appendChild(new Element("a")
							.attr("id", String.format("%sText", resourceKey))
							.attr("href", "#")
							.attr("role", "button")
							.addClass("h-100 text-decoration-none" + (dark ? " text-light" : ""))
							.attr("data-bs-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false")
					.appendChild(new Element("i")
							.addClass(String.format("%s %s me-1 text-decoration-none", iconGroup, icon)))
					.appendChild(nameElement = new Element("span")
								.addClass("jdropdown-text")
								.html("")))
					.appendChild(dropdownMenu = new Element("div")
							.addClass("dropdown-menu dropdown-size" + (dark ? " dropdown-menu-dark" : ""))
							.attr("aria-labelledby", String.format("%sDropdown", resourceKey))));
	}

	public void renderValues(Enum<?>[] values, String defaultValue, boolean i18n, boolean valueIsName) {
		
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
		nameElement.html(processEnumName(selected.name()));
		if(valueIsName) {
			valueElement.val(String.valueOf(selected.name()));
		} else {
			valueElement.val(String.valueOf(selected.ordinal()));
		}
	}
	
	private String processEnumName(String name) {
		return WordUtils.capitalizeFully(name.replace('_', ' '));
	}
	
	public void renderValues(Map<String,String> values, String defaultValue, boolean i18n) {
		
		for(Map.Entry<String,String> entry : values.entrySet()) {
			addInputValue(entry.getKey(), entry.getValue(), i18n);
			if(defaultValue.equals(entry.getKey())) {
				if(!i18n) {
					nameElement.html(entry.getValue());
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
			if(defaultValue.equals(value.getValue())) {
				nameElement.attr("jad:bundle", value.getBundle());
				nameElement.attr("jad:i18n", value.getI18n());
				valueElement.val(value.getValue());
			}
		}
	}
	
	public void setName(String bundle, String i18n) {
		nameElement.attr("jad:bundle", bundle);
		nameElement.attr("jad:i18n", i18n);
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
		Element el = PageHelper.createAnchor("#", value)
				.attr("data-resourcekey", key)
				.addClass("jdropdown-item dropdown-item");
		if(i18n) {
			el.attr("jad:bundle", bundle);
			el.attr("jad:i18n", value);
		}
		dropdownMenu.appendChild(el);
	}
	
	public void addAnchorValue(String key, String value, boolean i18n, String url) {
		Element el = PageHelper.createAnchor(url, value)
				.attr("data-resourcekey", key)
				.addClass("jdropdown-item dropdown-item");
		if(i18n) {
			el.attr("jad:bundle", bundle);
			el.attr("jad:i18n", value);
		}
		dropdownMenu.appendChild(el);
	}
	
	public Element addI18nAnchorWithIconValue(String bundle, String key, String url, String iconGroup, String icon, String... classes) {
		Element el = PageHelper.createAnchor(url)
				.addClass("dropdown-item " + Utils.csv(" ", classes))
				.appendChild(Html.i(iconGroup, icon, "fa-fw", "me-1"))
				.appendChild(Html.i18n(bundle, key));

		dropdownMenu.appendChild(el);
		return el;
	}

	private void renderValues(Iterable<? extends NamedUUIDEntity> fields, String defaultValue, boolean i18n) {
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
			nameElement.html(selected.getName());
			valueElement.val(selected.getResourceKey());
		}
	}

	public Element renderInputWithValues(Iterable<? extends NamedUUIDEntity> children, String defaultValue, boolean i18n) {
		Element el = renderInput();
		renderValues(children, defaultValue, i18n);
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
			nameElement.html(selected.getResourceKey());
			valueElement.val(selected.getResourceKey());
		}
	}

	
}
