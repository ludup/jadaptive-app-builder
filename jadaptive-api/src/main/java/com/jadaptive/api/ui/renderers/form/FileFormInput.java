package com.jadaptive.api.ui.renderers.form;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.utils.Utils;

public class FileFormInput extends FormInputRender {

	String filename;
	long size;
	String contentType;
	boolean resettable;
	public FileFormInput(ObjectTemplate template, TemplateViewField field,
			String filename, 
			long size,
			String contentType) {
		super(template, field);
		this.filename = filename;
		this.size = size;
		this.contentType = contentType;
		this.resettable = field.getField().isResettable();
	}

	@Override
	public String getInputType() {
		return "file";
	}
	
	protected void onRender(TemplateView panel, Element rootElement, String value) { 
		
		rootElement.selectFirst("input").val("");
		rootElement.appendChild(new Element("input")
							.attr("name", getFormVariable() + "_previous")
							.attr("type", "hidden")
							.val(value));
		
//		Element inputEl = rootElement.selectFirst("input");
//		if(resettable && StringUtils.isNotBlank(value)) {
//			inputEl.after(new Element("div").addClass("input-group-text").appendChild(
//				new Element("a").
//				  attr("href", "#").
//				  addClass("input-resetter").
//				  attr("data-reset-type", "image").
//				  attr("data-reset-for", getFormVariable()).
//				  appendChild(
//					new Element("i").addClass("fa fa-broom-wide")
//				)
//			));
//		}
	}
	
	protected void createHelpElement(Element parent, String value) {
		
		if(StringUtils.isNotBlank(filename)) {
			parent.appendChild(new Element("small")
					.addClass("form-text")
					.addClass("text-muted")
					.attr("jad:bundle", "default")
					.attr("jad:i18n", "fileInput.help")
					.attr("jad:arg0", filename)
					.attr("jad:arg1", Utils.toByteSize(size, 0))
					.attr("jad:arg2", contentType));
		} else {
			super.createHelpElement(parent, value);
		}
	}

}
