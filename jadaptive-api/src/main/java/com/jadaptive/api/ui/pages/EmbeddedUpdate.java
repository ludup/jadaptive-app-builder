package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Component
@RequestPage(path = "object-update/{resourceKey}/{uuid}/{fieldName}/{childUuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = {"freemarker", "i18n"})
@ModalPage
public class EmbeddedUpdate extends EmbeddedObjectPage {
	
	@Override
	public FieldView getScope() {
		return FieldView.UPDATE;
	}

	@Override
	public String getUri() {
		return "object-update";
	}
	
	@Override
	protected void doGenerateTemplateContent(Document document) throws FileNotFoundException, IOException {
		
		Element element = document.selectFirst("#saveButton");
		if(Objects.nonNull(element)) {
			element.attr("data-url", String.format("/app/ui/update/%s/%s", template.getResourceKey(), getUuid()))
				.attr("data-action", String.format("/app/api/form/stash/%s/%s/%s", template.getResourceKey(), childResourceKey, fieldName));
		}
		
		element = document.selectFirst("#cancelButton");
		
		if(Objects.nonNull(element)) {
			element.attr("href", String.format("/app/ui/update/%s/%s", template.getResourceKey(), getUuid()));
		}
	}
}
