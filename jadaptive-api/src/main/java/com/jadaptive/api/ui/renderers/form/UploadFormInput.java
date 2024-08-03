package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageHelper;

public class UploadFormInput extends FieldInputRender {

	public UploadFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	public UploadFormInput(TemplateViewField field) {
		super(field);
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
		
		Element e;
		
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())))
				.appendChild(e = Html.div("uploadForm")
						.attr("data-resourcekey", resourceKey)
						.attr("data-variable", formVariable))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));
		
		load(e);
		
		e.select("input[type='file']")
			.addClass("mfiles")
			.attr("name", formVariable);
	}


	

}
