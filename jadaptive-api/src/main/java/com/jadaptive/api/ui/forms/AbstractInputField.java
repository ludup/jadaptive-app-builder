package com.jadaptive.api.ui.forms;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Html;

public abstract class AbstractInputField implements InputField {

	String resourceKey;
	String bundle;
	String formVariable;
	boolean readonly; 
	
	protected AbstractInputField(String bundle, String resourceKey, String formVariable, boolean readonly) {
		this.bundle = bundle;
		this.resourceKey = resourceKey;
		this.formVariable = formVariable;
		this.readonly = readonly;
	}
	
	protected void onRender(Element e, String value) {
		
		field(e);
		label(e.selectFirst("jad\\:label"));
		doInput(e, value);
		description(e.selectFirst("jad\\:desc"));
	}
	
	protected void field(Element e) {
		if(Objects.nonNull(e)) {
			e.addClass("jfield mb-3");
		}
	}
	
	protected void label(Element e) {
		if(Objects.nonNull(e)) {
			e.replaceWith(Html.div("jlabel")
					.appendChild(Html.label(bundle, String.format("%s.name", resourceKey))
							.addClass("form-label")
							.attr("for", formVariable)));
		}
	}
	
	protected abstract void doInput(Element e, String value);
	
	protected void input(Element e, String type, String value) {
		e.replaceWith(Html.div("input-group")
				.appendChild(Html.input(type, formVariable, value)
						.addClass("form-control")
						.attr("autocomplete", "off")));
	}
	
	protected void description(Element e) {
		if(Objects.nonNull(e)) {
			e.replaceWith(Html.div("jdesc")
					.appendChild(new Element("small")
							.attr("jad:bundle", bundle)
							.attr("jad:i18n", String.format("%s.desc", resourceKey))
							.addClass("form-text text-muted")));
		}
	}

	@Override
	public void render(Document document, Element e, String value) throws IOException {
		InputField.super.render(document, e, value);
		onRender(e, value);
	}
	
	
}
