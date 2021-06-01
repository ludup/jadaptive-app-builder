package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.stats.ResourceService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.renderers.DropdownInput;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Dashboard extends AuthenticatedPage {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException {

		super.generateAuthenticatedContent(document);
		
		Element element = document.selectFirst("#resources");
		for(ResourceService rs : applicationService.getBeans(ResourceService.class)) {
			long count = rs.getTotalResources();
			element.appendChild(new Element("span")
						.html(String.format("%s&nbsp;", String.valueOf(count)))) 
					.appendChild(new Element("span")
							.attr("jad:bundle", rs.getI18NKey())
							.attr("jad:i18n", rs.getI18NKey() + (count > 1 || count == 0 ? ".names" : ".name")))
					.appendChild(new Element("br"));
		}
		
		element = document.selectFirst("#setupTasks");
		
		
		DropdownInput input = new DropdownInput("setupTasks", "default");
		element.appendChild(input.renderInput());
		
		Map<String,String> values = new HashMap<>();
		values.put("publicFolder", "a public URL to receive files over the web anonymously");
		values.put("sftpPartner", "credentials for a partner to send you files via SFTP");
		input.renderValues(values, "", false);
		
		
		
	}

	@Override
	public String getUri() {
		return "dashboard";
	}

}
