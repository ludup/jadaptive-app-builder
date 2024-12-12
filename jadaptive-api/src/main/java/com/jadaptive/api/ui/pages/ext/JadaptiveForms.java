package com.jadaptive.api.ui.pages.ext;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class JadaptiveForms extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		
		PageHelper.appendHeadScript(document, "/app/content/jadaptive-forms.js");
		document.selectFirst("body").append("<!-- Modal -->\n"
				+ "<div class=\"modal fade\" id=\"progressModal\" data-bs-backdrop=\"static\" \n"
				+ "      data-bs-keyboard=\"false\" tabindex=\"-1\" \n"
				+ "      aria-labelledby=\"staticBackdropLabel\" aria-hidden=\"true\"> \n"
				+ "  <div class=\"modal-dialog modal-dialog-centered\"> \n"
				+ "    <div class=\"modal-content\"> \n"
				+ "      <div class=\"modal-body\"> \n"
				+ "			<div id=\"uploadProgress\"> \n"
				+ "	   			<div class=\"progress mx-auto my-1 w-100 \" role=\"progressbar\" aria-label=\"Basic example\" aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\">\n"
				+ "					<div id=\"progressBar\" class=\"progress-bar auto-progress-bar\"></div> \n"
				+ "				</div> \n"
				+ "		      </div> \n"
				+ "		     <div class=\"mt-1\"> \n"
				+ "		        <span class=\"form-text text-muted\" jad:bundle=\"userInterface\" jad:i18n=\"uploadingFiles.text\"></span> \n"
				+ "		     </div> \n"
				+ "      </div> \n"
				+ "    </div> \n"
				+ "  </div> \n"
				+ "</div>");
	}

	@Override
	public String getName() {
		return "jadaptive-forms";
	}
}
