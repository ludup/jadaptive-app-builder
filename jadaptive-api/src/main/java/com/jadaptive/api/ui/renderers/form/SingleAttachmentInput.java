package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;
import com.jadaptive.utils.Utils;

public class SingleAttachmentInput extends FormInputRender {

	String templateKey; 
	
	public SingleAttachmentInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
		this.templateKey = template.getResourceKey();
	}

	@Override
	public String getInputType() {
		return "file";
	}
	
	protected void afterInput(Element rootElement, String value) throws IOException {
		
		FileAttachment att = null;
		if(StringUtils.isNotBlank(value)) {
			att = ApplicationServiceImpl.getInstance().getBean(FileAttachmentService.class).getAttachment(value);
		}
		
		rootElement.selectFirst("input")
			.val("")
			.addClass(Objects.nonNull(att) ? "d-none" : "");
		
		rootElement.appendChild(new Element("input")
							.attr("id", getFormVariable() + "_previousUUID")
							.attr("name", getFormVariable() + "_previousUUID")
							.attr("type", "hidden")
							.val(Objects.nonNull(att) ? att.getUuid() : ""));
		
		rootElement.appendChild(new Element("input")
				.attr("id", getFormVariable() + "_previousName")
				.attr("name", getFormVariable() + "_previousName")
				.attr("type", "hidden")
				.val(Objects.nonNull(att) ? att.getFilename() : ""));
		
		if(Objects.nonNull(att)) {
			rootElement.appendChild(Html.div("float-start", "me-2")
					.attr("id", getFormVariable() + "Holder")
					.appendChild(Html.div()
							.appendChild(Html.i("fa-solid", "fa-file", "float-start", "me-2", "mb-3", "fa-4x"))
							.appendChild(Html.span(att.getFilename())))
					.appendChild(
							Html.div().appendChild(
							new Element("small")
								.attr("jad:bundle", FileAttachment.RESOURCE_KEY)
								.attr("jad:i18n", "contentLength.text")
								.attr("jad:arg0", Utils.toByteSize(att.getSize().doubleValue(), 2)).addClass("text-muted")))
					.appendChild(Html.div().appendChild(
							new Element("small").attr("jad:bundle", FileAttachment.RESOURCE_KEY)
							.attr("jad:i18n", "contentType.text")
							.attr("jad:arg0", att.getContentType()).addClass("text-muted")))
					.appendChild(Html.a("#")
							.addClass("attachment-download")
							.attr("data-url", String.format("/app/api/objects/attachment/%s/%s/%s", templateKey, att.getUuid(), att.getFilename()))
							.attr("data-attachment-uuid", att.getUuid())
							.appendChild(Html.i("fa-solid", "fa-download", "me-2"))
					.appendChild(Html.a("#").addClass("attachment-delete write")
							.attr("data-filename", att.getFilename())
							.attr("data-attachment-uuid", att.getUuid())
							.attr("data-target", getFormVariable())
							.appendChild(Html.i("fa-solid", "fa-trash", "me-2")))));
		}

	}
}
