package com.jadaptive.plugins.web.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.DashboardWidget;
import com.jadaptive.api.ui.Page;

@Extension
public class DashboardWidgets extends AbstractPageExtension {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public String getName() {
		return "dashboardWidgets";
	}

	@Override
	public void process(Document document, Element element, Page page) throws IOException {

		super.process(document, element, page);
		
		List<DashboardWidget> widgets = new ArrayList<>(applicationService.getBeans(DashboardWidget.class));
		
		Collections.sort(widgets, new Comparator<DashboardWidget>() {

			@Override
			public int compare(DashboardWidget o1, DashboardWidget o2) {
				return o1.weight().compareTo(o2.weight());
			}
			
		});
		
		int count = 0;
		
		Element left;
		Element right;
		Element root;
		element.appendChild(root = new Element("div").addClass("row"));
		root.appendChild(left = new Element("div").addClass("col-md-6"));
		root.appendChild(right = new Element("div").addClass("col-md-6"));;
		
		for(DashboardWidget widget : widgets) {
			
			if(widget.wantsDisplay()) {
				Element row = count % 2 == 0 ? left : right;
				Element w;
				row.appendChild(new Element("div").addClass("row").appendChild(new Element("div")
						.addClass("col-md-12 mb-3 h-100")
						.appendChild(new Element("div")
								.addClass("card")
								.appendChild(new Element("div")
										.addClass("card-header")
										.appendChild(new Element("i")
												.addClass("fas fa-" + widget.getIcon()))
										.appendChild(new Element("span")
												.attr("jad:bundle", widget.getBundle())
												.attr("jad:i18n",String.format("%s.name", widget.getName()) )))
								.appendChild(w = new Element("div")
										.addClass("card-body")))));
				widget.renderWidget(document, w);

				count++;
			}
		}
		
	}
	
	

}
