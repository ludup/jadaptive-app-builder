package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public abstract class FormInputRender extends FieldInputRender {
	
	public FormInputRender(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}
	
	public final void renderInput(OrderedView panel, Element rootElement, String value) {
		

		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("input")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.addClass("form-control")
						.attr("value", value)
						.attr("type", getInputType()))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));
//		
//		
//		rootElement.append(replaceResourceKey("<div id=\"${resourceKey}Group\" class=\"row col-12\"></div>")); 
//		Element div = rootElement.select(replaceResourceKey("#${resourceKey}Group")).first();
//		div.append(replaceResourceKey("<label for=\"${resourceKey}\" class=\"form-label\" ${i18nName}></label>"));
//		div.append(replaceResourceKey("<input type=\"${inputType}\" id=\"${resourceKey}\" name=\"${resourceKey}\" class=\"form-control\" value=\"" + value + "\">"));
//		div.append(replaceResourceKey("<small class=\"form-text text-muted\" ${i18nDesc}></small>"));

	}

	public abstract String getInputType();
}
