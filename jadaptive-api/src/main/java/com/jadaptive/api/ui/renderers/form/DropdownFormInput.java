package com.jadaptive.api.ui.renderers.form;

import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.NamePairValue;

public class DropdownFormInput extends FieldInputRender {

	private Element componentElement;
	private Element dropdownMenu;
	private Element dropdownInput;
	private Element nameElement;
	private Element valueElement;

	public DropdownFormInput(TemplateViewField field) {
		super(field);
	}

	public DropdownFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	@Override
	protected void onRender(Element rootElement, String defaultValue, boolean readOnly, String... classes) {

		componentElement = rootElement.getElementsByClass("dropdown-form-field").last();

		ofNullable(componentElement.getElementsByAttributeValue("jad:role", "label").first()).ifPresent(lbl -> {
			if(decorate) {
				lbl.removeAttr("jad:role");
				lbl.attr("for", getFormVariable());
				lbl.attr("jad:bundle", getBundle());
				lbl.attr("jad:i18n", String.format("%s.name", getResourceKey()));
			}
			else {
				lbl.remove();
			}
		});
		
		dropdownInput = elementForRole(componentElement, "component");
		dropdownInput.addClass(String.format("%sDropdown", getResourceKey()));

		valueElement = elementForRole(componentElement, "reference");
		valueElement.attr("name", getFormVariableWithParents());
		valueElement.addClass(resourceKey);

		nameElement = elementForRole(componentElement, "input");
		nameElement.attr("name", getFormVariableWithParents() + "Text");
		nameElement.addClass(resourceKey + "Text");

		elementForRoleOr(componentElement, "help").ifPresent(dsc -> {
			if(decorate) {
				dsc.removeAttr("jad:role");
				dsc.attr("jad:bundle", getBundle());
				dsc.attr("jad:i18n", String.format("%s.desc", getResourceKey()));
			}
			else {
				dsc.remove();
			}
		});

		if(!disableIDAttribute) {
			dropdownInput.attr("id", String.format("%sDropdown", getResourceKey()));
			valueElement.attr("id", resourceKey);
			nameElement.attr("id", String.format("%sText", getResourceKey()));
		}

		if(readOnly) {
			nameElement.attr("disabled", "disabled");
		}

	}

	public void renderValues(Enum<?>[] values, String defaultValue, boolean readOnly) {

		setupValues(readOnly);

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

		setupValues(false);
		
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

		setupValues(false);
		
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

		setupValues(false);
		
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
		setupValues(false);
		
		var anchor = elementForRole(componentElement, "value").firstElementChild().clone();
		anchor.attr("data-resourcekey", value);
		anchor.text(name);
		
		dropdownMenu.appendChild(anchor);
	}

	public void setSelectedValue(String value, String name) {
		nameElement.val(name);
		valueElement.val(value);
	}

	public void addI18nValue(String value, String i18n) {
		
		setupValues(false);

		var anchor = elementForRole(componentElement, "value").firstElementChild().clone();
		anchor.attr("data-resourcekey", value);
		anchor.attr("jad:bundle", bundle);
		anchor.attr("jad:i18n", i18n);
		
		dropdownMenu.appendChild(anchor);
	}

	public void setSelectedI18nValue(String key, String value) {
		nameElement.val(I18N.getResource(bundle, key));
		valueElement.val(value);
    }

	public void renderNamePairValues(Collection<NamePairValue> values, String defaultValue) {

		setupValues(false);
		
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

	private void setupValues(boolean readOnly) {
		if(dropdownMenu == null) {
			dropdownMenu = elementForRole(componentElement, "values");
			if(readOnly) {
				dropdownMenu.remove();
			}
			else {
				dropdownMenu.attr("aria-labelledby", String.format("%sDropdown", getResourceKey()));
			}
		}
	}

}
