package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.MessagePage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UserInterfaceService;

@Extension
@RequestPage(path = "create/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Create extends ObjectTemplatePage {
	
	@Autowired
	private UserInterfaceService uiService; 
	
	@Override
	public FieldView getScope() {
		return FieldView.CREATE;
	}

	@Override
	public String getUri() {
		return "create";
	}
	
	protected void beforeGenerateContent(Document document) {

		if(!uiService.canCreate(template)) {
			throw new PageRedirect(new MessagePage("default",
					"title.createNotAllowed", 
					"message.createNotAllowed",
					"fa-exclamation-square",
					String.format("/app/ui/search/%s", template.getResourceKey())));
		}
	}
}
