package com.jadaptive.api.ui.pages.ext;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.session.Session;
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
			
			var sorted = new HashMap<String,List<ApplicationMenu>>();
			var parents = new ArrayList<ApplicationMenu>();
			for(var menu : menuService.getMenus()) {
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
			
			var user = permissionService.getCurrentUser();
			var menuItems = new LinkedHashMap<ApplicationMenu, List<ApplicationMenu>>();
			for(var parent :  parents.stream().
					filter(p -> menuService.checkPermission(p)).
					sorted((o1, o2) -> o1.weight().compareTo(o2.weight())).toList()) {
				var items = new ArrayList<ApplicationMenu>(sorted.get(parent.getUuid()).stream().
					peek(c -> {
						if(log.isDebugEnabled()) {
							log.debug("Checking {} menu access to {}", user.getUsername(), c.getI18n());
						}
					}).
					filter(c -> {
						if(!menuService.checkPermission(c)) {
							if(log.isDebugEnabled()) {
								log.debug("{} does not have access to menu {}", permissionService.getCurrentUser().getUsername(), c.getI18n());
							}
							return false;
						}
						return true;
					}).
					sorted((o1,o2) -> o1.weight().compareTo(o2.weight())).toList());
				if(items.size() > 0) {
					menuItems.put(parent, items);
				}
			}
			
			var topMenu = document.selectFirst("#topMenu");
			
			if(menuItems.size() == 1) {
				var parent = menuItems.keySet().iterator().next();
				for(var child : menuItems.values().iterator().next()) {
					Element link;
					topMenu.appendChild(new Element("li")
							.addClass("nav-item")
							.appendChild(link = new Element("a")
									.addClass("nav-link")
									.attr("href", child.getPath())
									.appendChild(new Element("i")
											.addClass(String.format("nav-icon me-2 fa-solid fa-fw %s", child.getIcon())))
									.appendChild(new Element("span")
											.attr("jad:bundle", child.getBundle())
											.attr("jad:i18n", child.getI18n()))));
					
					if(!child.isEnabled() || !parent.isEnabled()) {
						link.addClass("disabled");
						link.attr("href", "#");
					}
				}

			}
			else {
				for(var en : menuItems.entrySet()) {
					var parent = en.getKey();
					
					var parentElement = new Element("div")
							.addClass("dropdown-menu")
							.attr("aria-labelledby", "navbarDropdown");
							
					for(var child : en.getValue()) {
						
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
		
		if(isNotBlank(productService.getLogoResource())) {
			var e = document.selectFirst("#logo");
			if(Objects.nonNull(e)) {
				e.attr("src", productService.getLogoResource());
			}
		}
		
		if(isNotBlank(productService.getFaviconResource())) {
			document.selectFirst("head")
				.appendElement("link")
					.attr("href", productService.getFaviconResource())
					.attr("rel", "icon");
		}
		
		Session.getOr().ifPresent(session -> {
			document.selectFirst("#headerActions").appendChild(new Element("div")
					.addClass("float-end me-3")
					.appendChild(new Element("div")
							.attr("id", "logoff")
							.appendChild(new Element("a")
								.attr("href", "/app/ui/logoff")
								.addClass("text-light fa-solid fa-sign-out-alt text-decoration-none"))));
			
			if(session.isImpersontating()) {
				document.selectFirst("#headerActions").appendChild(new Element("div")
						.addClass("float-end me-3")
						.appendChild(new Element("div")
								.attr("id", "revert")
								.appendChild(new Element("a")
									.attr("href", "/app/ui/revert-impersonation")
									.addClass("text-light fa-solid fa-person text-decoration-none"))));
			}
		});
	}

	@Override
	public String getName() {
		return "header";
	}

}
