package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.auth.AuthenticationStep;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class AuthenticationModules extends AuthenticatedPage {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public String getUri() {
		return "authentication-modules";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		
		Element el;
		document.selectFirst("#steps").appendChild(el = Html.div("row", "text-center"));
		for(AuthenticationStep step : applicationService.getBeans(AuthenticationStep.class)) {
			
			el.appendChild(Html.div("col-md-3", "mt-5")
					.appendChild(Html.div().appendChild(Html.i("far fa-2x fa-" + step.getIcon())))
					.appendChild(new Element("a")
						.attr("href", step.getConfigurationPage())
						.appendChild(Html.i18n(step.getBundle(), step.getResourceKey() + ".name")))
					.appendChild(new Element("p")
							.addClass("text-muted")
							.appendChild(Html.i18n(step.getBundle(), step.getResourceKey() + ".desc"))));
		}
	}

}