package com.jadaptive.plugins.dashboard.pages;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.stats.ResourceService;
import com.jadaptive.plugins.dashboard.DashboardWidget;

@Component
public class ServerStatsWidget implements DashboardWidget {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public String getIcon() {
		return "fa-server";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getName() {
		return "serverStats";
	}

	@Override
	public void renderWidget(Document document, Element element) {

		element.appendChild(new Element("h6").addClass("card-title")
						.appendChild(new Element("span")
								.attr("jad:bundle", getBundle())
								.attr("jad:i18n", "serverStats.desc")));
		Element e;
		element.appendChild(e = new Element("div").addClass("row"));
		
		for(ResourceService rs : applicationService.getBeans(ResourceService.class)) {
			if(rs.isEnabled()) {
			long count = rs.getTotalResources();
				e.appendChild(
					new Element("div")
						.addClass("col-6")
						.appendChild(
					new Element("a")
						.attr("href", String.format("/app/ui/search/%s", rs.getResourceKey()))
							.appendChild(new Element("span")
								.html(String.format("%s&nbsp;", String.valueOf(count)))) 
							.appendChild(new Element("span")
									.attr("jad:bundle", rs.getResourceKey())
									.attr("jad:i18n", rs.getResourceKey() + (count > 1 || count == 0 ? ".names" : ".name")))
							.appendChild(new Element("br"))));
			}
		}
	}

	@Override
	public Integer weight() {
		return 0;
	}

	@Override
	public boolean wantsDisplay() {
//		try {
//			permissionService.assertAdministrator();
//			return true;
//		} catch(AccessDeniedException e) {
//			return false;
//		}
		return false;
	}

}
