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
		
		PageHelper.appendBodyScript(document, "/app/content/jadaptive-forms.js");
		document.selectFirst("body").append("""
				<!-- Modal -->
				<div class="modal fade" id="progressModal" data-bs-backdrop="static"
				      data-bs-keyboard="false" tabindex="-1"
				      aria-labelledby="staticBackdropLabel" aria-hidden="true">
				  <div class="modal-dialog modal-dialog-centered">
				    <div class="modal-content">
				      <div class="modal-body">
							<div id="uploadProgress">
					   			<div class="progress mx-auto my-1 w-100 " role="progressbar" aria-label="Basic example" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
									<div id="progressBar" class="progress-bar auto-progress-bar"></div>
								</div>
						      </div>
						     <div class="mt-1">
						        <span class="form-text text-muted" jad:bundle="userInterface" jad:i18n="uploadingFiles.text"></span>
						     </div>
				      </div>
				    </div>
				  </div>
				</div>
				""");
	}

	@Override
	public String getName() {
		return "jadaptive-forms";
	}
}
