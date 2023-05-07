package com.jadaptive.api.ui.pages.objects;

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
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Extension
@RequestPage(path = "update/{resourceKey}/{uuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Update extends ObjectTemplatePage {
	
	@Autowired
	private UserInterfaceService uiService; 
	
	@Override
	public FieldView getScope() {
		return FieldView.UPDATE;
	}

	@Override
	public String getUri() {
		return "update";
	}
	
	protected void beforeGenerateContent(Document document) {

		if(!uiService.canUpdate(template)) {
			throw new PageRedirect(new MessagePage("default",
					"title.updateNotAllowed", 
					"message.updateNotAllowed",
					"fa-exclamation-square",
					String.format("/app/ui/search/%s", template.getResourceKey())));
		}
	}
}
