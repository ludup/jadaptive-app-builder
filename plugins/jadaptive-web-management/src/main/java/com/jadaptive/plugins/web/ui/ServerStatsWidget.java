package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.stats.ResourceService;
import com.jadaptive.api.ui.DashboardWidget;

@Extension
public class ServerStatsWidget implements DashboardWidget {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public String getIcon() {
		return "server";
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
		element.appendChild(e = new Element("p"));
		
		for(ResourceService rs : applicationService.getBeans(ResourceService.class)) {
			long count = rs.getTotalResources();
			e.appendChild(new Element("a")
					.attr("href", String.format("/app/ui/table/%s", rs.getResourceKey()))
						.appendChild(new Element("span")
							.html(String.format("%s&nbsp;", String.valueOf(count)))) 
						.appendChild(new Element("span")
								.attr("jad:bundle", rs.getResourceKey())
								.attr("jad:i18n", rs.getResourceKey() + (count > 1 || count == 0 ? ".names" : ".name")))
						.appendChild(new Element("br")));
		}
	}

	@Override
	public Integer weight() {
		return 0;
	}

	@Override
	public boolean wantsDisplay() {
		return true;
	}

}