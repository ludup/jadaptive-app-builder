package com.jadaptive.plugins.dashboard.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.DashboardType;
import com.jadaptive.api.ui.HomePage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageResources;
import com.jadaptive.api.ui.PageResourcesHelper;
import com.jadaptive.api.ui.pages.ext.EnableBootstrapTheme;
import com.jadaptive.plugins.dashboard.DashboardWidget;

@Component
@HomePage
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "help", "i18n"} )
@EnableBootstrapTheme(path = "bootstrap")
public class Dashboard extends AuthenticatedPage {

	static Logger log = LoggerFactory.getLogger(Dashboard.class);
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public String getUri() {
		return "dashboard";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		
		super.generateAuthenticatedContent(document);
		
		List<DashboardWidget> widgets = new ArrayList<>(applicationService.getBeans(DashboardWidget.class));
		
		Collections.sort(widgets, new Comparator<DashboardWidget>() {
			@Override
			public int compare(DashboardWidget o1, DashboardWidget o2) {
				return o1.weight().compareTo(o2.weight());
			}
		});
		
		int home = 0;
		int insights = 0;
		for(DashboardType type : DashboardType.values()) {
		
			int count = 0;
			Element left;
			Element right;
			Element root;
			Element element = document.selectFirst("div[jad:type='" + type.name() + "']");
			element.appendChild(root = new Element("div").addClass("row"));
			root.appendChild(left = new Element("div").addClass("col-md-6"));
			root.appendChild(right = new Element("div").addClass("col-md-6"));;
			
			for(DashboardWidget widget : widgets) {
				
				if(widget.getType()==type && widget.wantsDisplay()) {
					
					switch(type) {
					case INSIGHTS:
						insights++;
						break;
					default:
						home++;
						break;
					}
					Element row = count % 2 == 0 ? left : right;
					Element w;
					Element help;
					Element e = new Element("div").addClass("row").appendChild(new Element("div")
							.addClass("col-md-12 mb-3 h-100")
							.appendChild(new Element("div")
									.addClass("card")
									.appendChild(new Element("div")
											.addClass("card-header")
											.appendChild(Html.div("w-75 float-start")
												.appendChild(new Element("i")
														.addClass(widget.getIconGroup() + " " + widget.getIcon()))
												.appendChild(new Element("span")
														.attr("jad:bundle", widget.getBundle())
														.attr("jad:i18n",String.format("%s.name", widget.getName()) )))
												.appendChild(help = Html.div("w-25 float-end text-end")))
									.appendChild(w = new Element("div")
											.addClass("card-body"))));
					
					if(widget.hasHelp()) {
						help.appendChild(new Element("a")
								.attr("jad:help", widget.getName() + "Help")
								.attr("jad:html", widget.getClass().getName().replace(".", "/") + "Help.html")
								.attr("jad:bundle", widget.getBundle())
								.appendChild(Html.i("fa-solid", "fa-question-circle")));
					}
					
					
					URL html = widget.getClass().getResource(widget.getClass().getSimpleName() + ".html");
					if(Objects.nonNull(html)) {
						try(InputStream in = html.openStream()) {
							Document doc = Jsoup.parse(IOUtils.toString(in, "UTF-8"));
							Elements children = doc.selectFirst("body").children();
							if(Objects.nonNull(children)) {
								for(Element elem : children) {
									elem.appendTo(w);
								}
							}
						}
					}
					URL stylesheet = widget.getClass().getResource(widget.getClass().getSimpleName() + ".css");
					if(Objects.nonNull(stylesheet)) {
						PageHelper.appendStylesheet(document,"/app/style/" + widget.getClass().getPackageName().replace('.', '/') + "/" + widget.getClass().getSimpleName() + ".css");
					}
					URL script = widget.getClass().getResource(widget.getClass().getSimpleName() + ".js");
					if(Objects.nonNull(script)) {
						PageHelper.appendHeadScript(document, "/app/script/" + widget.getClass().getPackageName().replace('.', '/') + "/" + widget.getClass().getSimpleName() + ".js");
					}
					
					
					try {
						widget.renderWidget(document, w);
						row.appendChild(e);
						count++;
					} catch(Throwable ex) {
						log.error("Consumed exception whilst processing dashboard widget {}", widget.getName(), ex);
					}
				}
			}
		}
		
		if(home==0) {
			document.selectFirst("#nav-home-tab").addClass("d-none");
			document.selectFirst("#nav-home").addClass("d-none");
		} 
		if(insights==0) {
			document.selectFirst("#nav-insights-tab").addClass("d-none");
			document.selectFirst("#nav-insights").addClass("d-none");		
		} 
	}

	
}
