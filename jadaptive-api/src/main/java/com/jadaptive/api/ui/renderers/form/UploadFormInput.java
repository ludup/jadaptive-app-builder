package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;

public class UploadFormInput extends FieldInputRender {

	Element e;
	
	public UploadFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	public UploadFormInput(TemplateViewField field) {
		super(field);
	}

	@Override
	public void renderInput(Element rootElement, String value, boolean readOnly, String... classes) throws IOException {
		
		this.e = rootElement;
		Element e;
		Element form;
		rootElement.appendChild(e = new Element("div")
						.addClass("col-12"));
		if(decorate) {
			e.appendChild(new Element("label")
					.attr("for", getFormVariable())
					.addClass("form-label")
					.attr("jad:bundle", getBundle())
					.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		}
		
		e.append("<div class=\"float-end text-end mt-1\">\n"
				+ " 		<a class=\"uploadToggle\" data-bs-toggle=\"collapse\" href=\"#attachmentHolder\" role=\"button\" aria-expanded=\"false\" aria-controls=\"attachmentHolder\">\n"
				+ " 			<i class=\"fa-solid fa-chevron-up\" data-next=\"fa-chevron-down\" data-prev=\"fa-chevron-up\"></i>\n"
				+ "  		</a>\n"
				+ " 	</div>").appendChild(form = Html.div("uploadForm")
					.attr("data-resourcekey", resourceKey)
					.attr("data-variable", formVariable));
		
		if(decorate) {
			e.appendChild(new Element("small")
							.addClass("form-text")
							.addClass("text-muted")
							.attr("jad:bundle", getBundle())
							.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}
		
		load(form);
		
		form.select("input[type='file']")
			.addClass("mfiles")
			.attr("name", formVariable);
	}

	public void renderExistingFile(String uuid, String filename, String size) {
		
		var row = e.selectFirst(".jfiles").select("tr").last();
		row.parent().appendChild(row.clone());
		row.attr("data-filename", filename);
		row.attr("data-uuid", uuid);
		row.addClass("file-index");
		row.selectFirst(".filename").text(filename);
		row.selectFirst(".size").text(size);
		row.appendChild(Html.input("hidden", formVariable, uuid));
		row.appendChild(Html.input("hidden", formVariable + "_name", filename));
		row.removeClass("hidden");
		
		 e.selectFirst(".jfiles").removeClass("d-none");
	}
	

}
