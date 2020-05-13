package com.jadaptive.app.ui.renderers;

import java.util.Collection;
import java.util.Map;

import org.jsoup.select.Elements;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldTemplate;

public class DropdownInput extends InputRender {

	Elements inputElements;
	public DropdownInput(Elements rootElement, FieldTemplate template) {
		super(rootElement, template);
	}

	@Override
	protected void renderInput(Elements rootElement, FieldTemplate template) {

		rootElement.append(replaceResourceKey("<div class=\"form-group col-12\"><label for=\"${resourceKey}\" class=\"col-form-label\">" + template.getName() + "</label><div id=\"${resourceKey}Dropdown\" style=\"position: relative\" class=\"input-group dropdown\"></div></div>"));
		Elements dropdown = rootElement.select(replaceResourceKey("#${resourceKey}Dropdown"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}\" name=\"${resourceKey}\" type=\"hidden\">"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}Text\" data-display=\"static\" class=\"dropdown-toggle form-control\" readonly=\"readonly\" type=\"text\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">"));
		dropdown.append(replaceResourceKey("<div class=\"input-group-append\"><span class=\"input-group-text\"><i class=\"fas fa-chevron-down\"></i></span></div>"));
		dropdown.append(replaceResourceKey("<div class=\"dropdown-menu\" aria-labelledby=\"${resourceKey}Dropdown\"></div>"));
		dropdown.parents().first().append("<small class=\"form-text text-muted\">" + template.getDescription() + "</small>");
		inputElements = dropdown.select(".dropdown-menu");
	}

	public void renderValues(Enum<?>[] values) {
		for(Enum<?> value : values) {
			addInputValue(String.valueOf(value.ordinal()), value.name());
		}
	}
	
	public void renderValues(Map<String,String> values) {
		
		for(Map.Entry<String,String> entry : values.entrySet()) {
			addInputValue(entry.getKey(), entry.getValue());
		}
	}
	
public void renderValues(Collection<NamedUUIDEntity> values) {
		
		for(NamedUUIDEntity entry : values) {
			addInputValue(entry.getUuid(), entry.getName());
		}
	}
	
	private void addInputValue(String key, String value) {
		inputElements.append("<a data-resourcekey=\"" + key + "\" class=\"" 
				+ "dropdown-item\" href=\"#\">" + value + "</a>");
	}

	
}
