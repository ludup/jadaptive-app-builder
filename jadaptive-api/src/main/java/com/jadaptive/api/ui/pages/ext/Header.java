package com.jadaptive.api.ui.pages.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.CustomizablePage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Component
@CustomizablePage
public class Header extends AbstractPageExtension {

	static Logger log = LoggerFactory.getLogger(Header.class);
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private ApplicationMenuService menuService;
	
	@Autowired
	private ProductService productService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Override
	public void process(Document document, Element element, Page page) {
		
		
		if(!permissionService.hasUserContext()) {
			document.select("#searchForm").remove();
			document.select("#topMenu").remove();
			document.select("#logoff").remove();
			document.select("nav button").remove();
		} else if(Objects.nonNull(page.getClass().getAnnotation(ModalPage.class))) {
			document.select("#topMenu").remove();
		} else {
			
			Map<String,List<ApplicationMenu>> sorted = new HashMap<>();
			List<ApplicationMenu> parents = new ArrayList<>();
			for(ApplicationMenu menu : menuService.getMenus()) {
				if(Objects.nonNull(menu.getParent()) && !sorted.containsKey(menu.getParent())) {
					sorted.put(menu.getParent(), new ArrayList<>());
				}
				if(Objects.isNull(menu.getParent())) {
					if(!sorted.containsKey(menu.getUuid())) {
						sorted.put(menu.getUuid(), new ArrayList<>());
					}
					parents.add(menu);
				}
				if(Objects.nonNull(menu.getParent())) {
					sorted.get(menu.getParent()).add(menu);
				}
			}
			
			Collections.sort(parents, new Comparator<ApplicationMenu>() {
				@Override
				public int compare(ApplicationMenu o1, ApplicationMenu o2) {
					return o1.weight().compareTo(o2.weight());
				}
			});
			
			Element topMenu = document.selectFirst("#topMenu");
			
			for(ApplicationMenu parent : parents) {
				
				if(!menuService.checkPermission(parent)) {
					continue;
				}
				
				Element parentElement = new Element("div")
						.addClass("dropdown-menu")
						.attr("aria-labelledby", "navbarDropdown");
						
				List<ApplicationMenu> children = sorted.get(parent.getUuid());
				Collections.sort(children, new Comparator<ApplicationMenu>() {
					@Override
					public int compare(ApplicationMenu o1, ApplicationMenu o2) {
						return o1.weight().compareTo(o2.weight());
					}
				});

				for(ApplicationMenu child : children) {
					
					if(log.isDebugEnabled()) {
						log.debug("Checking {} menu access to {}", permissionService.getCurrentUser().getUsername(), child.getI18n());
					}
					
					if(!menuService.checkPermission(child)) {
						if(log.isDebugEnabled()) {
							log.debug("{} does not have access to menu {}", permissionService.getCurrentUser().getUsername(), child.getI18n());
						}
						continue;
					}

					Element link;
					parentElement.appendChild(link = new Element("a")
							.addClass("dropdown-item me-3")
							.attr("href", child.getPath())
							.appendChild(new Element("i")
									.addClass(String.format("nav-icon me-2 %s fa-fw %s", child.getIconGroup(), child.getIcon())))
							.appendChild(new Element("span")
									.attr("jad:bundle", child.getBundle())
										.attr("jad:i18n", child.getI18n())));
					
					if(!child.isEnabled()) {
						link.addClass("disabled");
						link.attr("href", "#");
					}
				}
				
				if(!parentElement.children().isEmpty()) {
					
					Element link;
					topMenu.appendChild(new Element("li")
							.addClass("nav-item dropdown")
							.appendChild(link = new Element("a")
									.addClass("nav-link dropdown-toggle")
									.attr("href", "#")
									.attr("data-bs-toggle", "dropdown")
									.attr("aria-haspopup", "true")
									.attr("aria-expanded", "false")
									.appendChild(new Element("i")
											.addClass(String.format("nav-icon me-2 fa-solid fa-fw %s", parent.getIcon())))
									.appendChild(new Element("span")
											.attr("jad:bundle", parent.getBundle())
											.attr("jad:i18n", parent.getI18n())))
							.appendChild(parentElement));
					
					if(!parent.isEnabled()) {
						link.addClass("disabled");
						link.attr("href", "#");
					}
				}
			}
		}
		
		if(StringUtils.isNotBlank(productService.getLogoResource())) {
			Element e = document.selectFirst("#logo");
			if(Objects.nonNull(e)) {
				e.attr("src", productService.getLogoResource());
			}
		}
		
		if(StringUtils.isNotBlank(productService.getFaviconResource())) {
			document.selectFirst("head")
				.appendElement("link")
					.attr("href", productService.getFaviconResource())
					.attr("rel", "icon");
		}
		
		boolean loggedIn = sessionUtils.hasActiveSession(Request.get());
		if(loggedIn) {
			
			document.selectFirst("#headerActions").appendChild(new Element("div")
					.addClass("float-end me-3")
					.appendChild(new Element("div")
							.attr("id", "logoff")
							.appendChild(new Element("a")
								.attr("href", "/app/ui/logoff")
								.addClass("text-light fa-solid fa-sign-out-alt text-decoration-none"))));
			
			Session session = sessionUtils.getActiveSession(Request.get());
			if(session.isImpersontating()) {
				document.selectFirst("#headerActions").appendChild(new Element("div")
						.addClass("float-end me-3")
						.appendChild(new Element("div")
								.attr("id", "revert")
								.appendChild(new Element("a")
									.attr("href", "/app/ui/revert-impersonation")
									.addClass("text-light fa-solid fa-person text-decoration-none"))));
			}
		}

	}

	@Override
	public String getName() {
		return "header";
	}

}
