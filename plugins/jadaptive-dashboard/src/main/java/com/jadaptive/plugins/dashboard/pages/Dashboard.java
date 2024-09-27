package com.jadaptive.plugins.dashboard.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.BasicDashboardTypes;
import com.jadaptive.api.ui.DashboardType;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.pages.ext.EnableBootstrapTheme;
import com.jadaptive.plugins.dashboard.DashboardInitialiser;
import com.jadaptive.plugins.dashboard.DashboardWidget;
import com.jadaptive.plugins.dashboard.WidgetPlacement;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "help", "i18n"} )
@EnableBootstrapTheme(path = "bootstrap")
public class Dashboard extends AuthenticatedPage {

	static Logger log = LoggerFactory.getLogger(Dashboard.class);
	
	@Autowired
	private ApplicationService applicationService; 

	@Autowired
	private I18nService i18nService;

	@Autowired
	private PermissionService permissionService;
	
	@Override
	public String getUri() {
		return "dashboard";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		
		permissionService.assertPermission(DashboardInitialiser.DASHBOARD_PERMISSION);
		
		List<DashboardWidget> widgets = new ArrayList<>(applicationService.getBeans(DashboardWidget.class));
		
		Collections.sort(widgets, (o1,o2) ->  o1.weight().compareTo(o2.weight()));
		
		List<DashboardType> sortedTypes = getTypes(widgets);
		List<DashboardType> remainingTypes = new ArrayList<>(sortedTypes);
		
		Element tabContent = document.getElementById("nav-tabContent");
		Element tabButtons = document.getElementById("nav-tab");
		int tabs = 0, tabCount = 0;
		
		try(var ctx = permissionService.userContext()) {
			
			
			for(DashboardType type : sortedTypes) {
			
				int count = 0;
				Element left;
				Element right;
				Element root;
				
				String tabId = type.cssId();
				
				Element tabEl = tabContent.appendElement("div").
							addClass("tab-pane").
							addClass("fade").
							attr("role", "tabpanel").
							attr("aria-labelledby", "nav-" + tabId + "-tab").
							attr("id", "nav-" + tabId);
				
				if(tabs == 0) {
					tabEl.addClass("show active");
				}
				
				Element tabButton = tabButtons.appendElement("button").
							addClass("nav-link").
							attr("id", "nav-" + tabId + "-tab").
							attr("data-bs-toggle", "tab").
							attr("data-bs-target", "#nav-" + tabId).
							attr("type", "button").
							attr("role", "tab").
							attr("aria-controls", "nav-" + tabId);
				
				if(tabs == 0) {
					tabButton.addClass("active");
				}
				tabButton.attr("aria-selected", tabs == 0);
				tabButton.html(i18nService.format(type.bundle(), Locale.getDefault(), type.resourceKey() + ".name"));
				
				Element element = tabEl.appendElement("div").addClass("mt-5").attr("jad:type", type.name());
				
				List<DashboardWidget> topWidgets = widgets.stream().filter(e -> e.getPlacement() == WidgetPlacement.EXPANDED_TOP).collect(Collectors.toList());
				List<DashboardWidget> bottomWidgets = widgets.stream().filter(e -> e.getPlacement() == WidgetPlacement.EXPANDED_BOTTOM).collect(Collectors.toList());
				List<DashboardWidget> standardWidgets = widgets.stream().filter(e -> e.getPlacement() == WidgetPlacement.STANDARD).collect(Collectors.toList());
				
				if(!topWidgets.isEmpty()) {
					
					for(DashboardWidget widget : topWidgets) {
						if(widget.getType()==type && widget.wantsDisplay()) {
							
							remainingTypes.remove(widget.getType());
							renderWidget(element, widget, document);
						}
					}
				}
				
				element.appendChild(root = new Element("div").addClass("row"));
				root.appendChild(left = new Element("div").addClass("col-md-6"));
				root.appendChild(right = new Element("div").addClass("col-md-6"));;
				
				int noWidgets = 0;
				for(DashboardWidget widget : standardWidgets) {
					
					if(widget.getType()==type && widget.wantsDisplay()) {
						
						noWidgets++;
						remainingTypes.remove(widget.getType());
						Element row = count % 2 == 0 ? left : right;
						if(renderWidget(row, widget, document)) {
							count++;
						}
					}
				}
				
				if(!bottomWidgets.isEmpty()) {
					
					for(DashboardWidget widget : bottomWidgets) {
						if(widget.getType()==type && widget.wantsDisplay()) {
							
							remainingTypes.remove(widget.getType());
							renderWidget(element, widget, document);
						}
					}
				}
				
				if(noWidgets > 0)
					tabCount++;
				
				tabs++;
			}
			
			for(var t : remainingTypes) {
				document.selectFirst("#nav-" + t.cssId() +  "-tab").addClass("d-none");
				document.selectFirst("#nav-" + t.cssId()).addClass("d-none");
			} 
		}
		
		if(tabCount < 2) {
			document.getElementById("nav-tab").addClass("d-none");
		}
	}

	private boolean renderWidget(Element row, DashboardWidget widget, Document document) throws IOException {
		
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
		
		try {
			widget.renderWidget(document, w);
			row.appendChild(e);
			
			URL stylesheet = widget.getClass().getResource(widget.getClass().getSimpleName() + ".css");
			if(Objects.nonNull(stylesheet)) {
				PageHelper.appendStylesheet(document,"/app/style/" + widget.getClass().getPackageName().replace('.', '/') + "/" + widget.getClass().getSimpleName() + ".css");
			}
			
			URL script = widget.getClass().getResource(widget.getClass().getSimpleName() + ".js");
			if(Objects.nonNull(script)) {
				PageHelper.appendHeadScript(document, "/app/script/" + widget.getClass().getPackageName().replace('.', '/') + "/" + widget.getClass().getSimpleName() + ".js");
			}

			return true;
		} catch(Throwable ex) {
			log.error("Consumed exception whilst processing dashboard widget {}", widget.getName(), ex);
		}
		
		return false;
	}

	static List<DashboardType> getTypes(List<DashboardWidget> widgets) {
		Set<DashboardType> allTypes = new LinkedHashSet<>();
		allTypes.addAll(Arrays.asList(BasicDashboardTypes.values()));
		widgets.forEach(w -> allTypes.add(w.getType()));
		List<DashboardType> sortedTypes = new ArrayList<>(allTypes);
		Collections.sort(sortedTypes, (s1,s2) -> Integer.valueOf(s1.weight()).compareTo(s2.weight()));
		return sortedTypes;
	}

	
}
