package com.jadaptive.app.ui.renderers.form;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldDefinition;

public class DropdownFormInput extends FormInputRender {

	Elements inputElements;
	Element valueElement;
	Element nameElement;
	public DropdownFormInput(Elements rootElement, FieldDefinition template, String defaultValue) {
		super(rootElement, template, defaultValue);
	}

	@Override
	protected void renderInput(Elements rootElement, FieldDefinition template, String value) {

		rootElement.append(replaceResourceKey("<div class=\"form-group col-12\"><label for=\"${resourceKey}\" class=\"col-form-label\">" 
				+ template.getName() + "</label><div id=\"${resourceKey}Dropdown\" style=\"position: relative\" class=\"input-group dropdown\"></div></div>"));
		Elements dropdown = rootElement.select(replaceResourceKey("#${resourceKey}Dropdown"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}\" name=\"${resourceKey}\" type=\"hidden\">"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}Text\" data-display=\"static\" class=\"dropdown-toggle form-control\" readonly=\"readonly\" type=\"text\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">"));
		dropdown.append(replaceResourceKey("<div class=\"input-group-append\"><span class=\"jdropdown input-group-text\"><i class=\"fas fa-chevron-down\"></i></span></div>"));
		dropdown.append(replaceResourceKey("<div class=\"dropdown-menu\" aria-labelledby=\"${resourceKey}Dropdown\"></div>"));
		dropdown.parents().first().append("<small class=\"form-text text-muted\">" + template.getDescription() + "</small>");
		inputElements = dropdown.select(".dropdown-menu");
		nameElement = dropdown.select(replaceResourceKey("#${resourceKey}Text")).first();
		valueElement = dropdown.select(replaceResourceKey("#${resourceKey}")).first();
	}

	public void renderValues(Enum<?>[] values) {
		
		Enum<?> selected = null;
		for(Enum<?> value : values) {
			if(Objects.isNull(selected)) {
				selected = value;
			}
			addInputValue(String.valueOf(value.ordinal()), processEnumName(value.name()));
			if(value.name().equals(defaultValue)) {
				selected = value;
			}

		}
		
		nameElement.val(processEnumName(selected.name()));
		valueElement.val(String.valueOf(selected.ordinal()));
	}
	
	private String processEnumName(String name) {
		return name.replace('_', ' ');
	}
	
	public void renderValues(Map<String,String> values) {
		
		for(Map.Entry<String,String> entry : values.entrySet()) {
			addInputValue(entry.getKey(), entry.getValue());
		}
	}
	
	private void addInputValue(String key, String value) {
		inputElements.append("<a data-resourcekey=\"" + key + "\" class=\"" 
				+ "dropdown-item\" href=\"#\">" + value + "</a>");
	}

	public void renderValues(Collection<? extends NamedUUIDEntity> fields) {
		NamedUUIDEntity selected = null;
		for(NamedUUIDEntity field : fields) {
			if(Objects.isNull(field)) {
				selected = field;
			}

			addInputValue(field.getResourceKey(), field.getName());
			if(field.getResourceKey().equals(defaultValue)) {
				selected = field;
			}
			
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getName());
			valueElement.val(selected.getResourceKey());
		}
	}
	
	public void renderTemplateFields(Collection<FieldDefinition> fields) {
		
		FieldDefinition selected = null;
		for(FieldDefinition field : fields) {
			if(Objects.isNull(field)) {
				selected = field;
			}
			if(field.isSearchable()) {
				addInputValue(field.getResourceKey(), field.getName());
				if(field.getResourceKey().equals(defaultValue)) {
					selected = field;
				}
			}
		}
		
		if(Objects.nonNull(selected)) {
			nameElement.val(selected.getName());
			valueElement.val(selected.getResourceKey());
		}
	}

	
}
