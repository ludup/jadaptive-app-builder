package com.jadaptive.plugins.dashboard.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.ui.ContextHelpProcessor;
import com.jadaptive.api.ui.Page;
import com.jadaptive.plugins.dashboard.DashboardWidget;

@Component
public class DashboardHelp implements ContextHelpProcessor {

	@Autowired
	private ApplicationService applicationService; 
	@Autowired
	private I18nService i18nService;

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		var widgets = new ArrayList<>(applicationService.getBeans(DashboardWidget.class));
		Collections.sort(widgets, (o1,o2) ->  o1.weight().compareTo(o2.weight()));
		var sortedTypes = Dashboard.getTypes(widgets);
		
		var el = document.getElementById("helpSections");
		for (var type : sortedTypes) {
			if (widgets.stream().filter(w -> w.getType().equals(type) && w.wantsDisplay()).findFirst().isPresent()) {
				var div = el.appendElement("div").attr("id", "help-" + type.resourceKey());
				div.appendElement("h5")
						.html(i18nService.format(type.bundle(), Locale.getDefault(), type.resourceKey() + "Tab.title"));
				div.appendElement("p")
						.html(i18nService.format(type.bundle(), Locale.getDefault(), type.resourceKey() + "Tab.text"));

			}
		}
	}


}
