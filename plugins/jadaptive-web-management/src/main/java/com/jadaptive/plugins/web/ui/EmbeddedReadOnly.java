package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Extension
@RequestPage(path = "object-view/{resourceKey}/{uuid}/{fieldName}/{childUuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
@ModalPage
public class EmbeddedReadOnly extends EmbeddedObjectPage {
	
	@Override
	public FieldView getScope() {
		return FieldView.READ;
	}

	@Override
	public String getUri() {
		return "object-view";
	}
	
	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		super.generateAuthenticatedContent(document);
		
		Element element = document.selectFirst("#cancelButton");
		if(Objects.nonNull(element)) {
			element.attr("href", String.format("/app/ui/view/%s/%s", template.getResourceKey(), getUuid()));
		}
	}
}
