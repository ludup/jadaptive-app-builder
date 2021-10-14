package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.stats.ResourceService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.QuickSetupItem;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Dashboard extends AuthenticatedPage {

	@Autowired
	private ApplicationService applicationService; 

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private PageCache pageCache;
	
	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException {

		try {
			permissionService.assertAdministrator();
		} catch(AccessDeniedException ex) {
			throw new PageRedirect(pageCache.getHomePage());
		}
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
		Element parent = document.selectFirst("#quickSetup");
		
		DropdownInput input = new DropdownInput("setupTasks", "default");
		element.appendChild(input.renderInput());
		
		List<I18nOption> options = new ArrayList<>();

		for(QuickSetupItem item : applicationService.getBeans(QuickSetupItem.class)) {
			options.add(new I18nOption(item.getBundle(), item.getI18n(), item.getLink()));
		}
		
		if(options.size() > 0) {
			input.renderValues(options, "");
		} else {
			parent.remove();
		}
	}

	@Override
	public String getUri() {
		return "dashboard";
	}

}
