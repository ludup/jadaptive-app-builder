package com.jadaptive.api.ui.pages.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.config.ConfigurationPageItem;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class SystemConfigurationPage extends AuthenticatedPage {

	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Override
	public String getUri() {
		return "systemConfiguration";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {

		permissionService.assertAdministrator();
		
		Element el;
		document.selectFirst("#optionPages").appendChild(el = Html.div("row", "text-center"));
		
		for(ConfigurationPageItem optionPage : applicationService.getBeans(ConfigurationPageItem.class)) {
			
			if(optionPage.isSystem() && optionPage.isVisible()) {
				el.appendChild(Html.div("col-md-3", "mt-5")
						.appendChild(Html.div().appendChild(Html.i(optionPage.getIconGroup(), "fa-2x", optionPage.getIcon())))
						.appendChild(new Element("a")
							.attr("href", optionPage.getPath())
							.appendChild(Html.i18n(optionPage.getBundle(), optionPage.getResourceKey() + ".name")))
						.appendChild(new Element("p")
								.addClass("text-muted")
								.appendChild(Html.i18n(optionPage.getBundle(), optionPage.getResourceKey() + ".desc"))));
			}
		}
	}

}
