package com.jadaptive.api.ui.renderers.form;

import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageHelper;

public class ReplacementFormInput extends FieldInputRender {
	
	Element dropdownMenu;
	Element dropdownInput;
	Element valueElement;
	
	public ReplacementFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public void renderInput(Element rootElement, String defaultValue) {

		StringBuffer formVariable = new StringBuffer();
		
		if(Objects.nonNull(field.getParentFields())) {
			for(FieldTemplate t : field.getParentFields()) {
				formVariable.append(t.getResourceKey());
				formVariable.append(".");
			}
		}
		
		formVariable.append(field.getFormVariable());
		
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
											.attr("class", "fa-solid fa-code-simple"))))
				.appendChild(new Element("small")
							.addClass("form-text")
							.addClass("text-muted")
							.attr("jad:bundle", getBundle())
							.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));

	}
	
	public void addInputValue(String value, String bundle, String name) {
		
		if(Objects.isNull(dropdownMenu)) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey())));
		}
		dropdownMenu.appendChild(new Element("a").attr("href", "#")
				.appendChild(Html.i18n(bundle, name + ".name"))
				.attr("data-resourcekey", value)
				.addClass("replacement-item dropdown-item"));
	}
	
	public void addSection(String name) {
		
		if(Objects.isNull(dropdownMenu)) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey())));
		}
		dropdownMenu.appendChild(PageHelper.createAnchor("#", name)
				.addClass("dropdown-item small text-muted")
				.attr("disabled", "disabled"));
	}
	
	public void setSelectedValue(String value, String name) {
		valueElement.val(value);
	}

	public void renderTemplateReplacements(List<ObjectTemplate> replacementVars) {
		
		int index = 0;
		for(ObjectTemplate template : replacementVars) {
			addSection(String.valueOf(index));
			for(FieldTemplate field : template.getFields()) {
				switch(field.getFieldType()) {
				case TEXT:
					if(!field.getCollection()) {
						addInputValue(String.format("${%s:%s}", index, field.getResourceKey()), 
							template.getBundle(),
							field.getResourceKey());
					}
					break;
				default:
				}
			}
			
			
			index++;
		}
		
	}

	
}
