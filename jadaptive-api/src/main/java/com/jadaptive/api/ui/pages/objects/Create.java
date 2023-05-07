package com.jadaptive.api.ui.pages.objects;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.MessagePage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UserInterfaceService;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Component
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

		if(Objects.isNull(object)) {
			if(!uiService.canCreate(template)) {
				throw new PageRedirect(new MessagePage("default",
						"title.createNotAllowed", 
						"message.createNotAllowed",
						"fa-exclamation-square",
						String.format("/app/ui/search/%s", template.getResourceKey())));
			}
		}
	}
}
