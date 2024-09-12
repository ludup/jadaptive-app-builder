package com.jadaptive.api.ui.pages.ext;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.avatar.AvatarRequest;
import com.jadaptive.api.avatar.AvatarService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.CustomizablePage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Instrumentation;

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
	private AvatarService avatarService;
	
	@Override
	public void process(Document document, Element element, Page page) {

		var parents = new ArrayList<ApplicationMenu>();
		var sorted = new HashMap<String,List<ApplicationMenu>>();
		var modal = Objects.nonNull(page.getClass().getAnnotation(ModalPage.class));
		var hasUser = permissionService.hasUserContext();
		var isAdmin = hasUser && permissionService.isAdministrator();
		var user = hasUser ? permissionService.getCurrentUser() : null;
		var resolvedPermissions = hasUser ? permissionService.resolvePermissions(user) : PermissionService.NO_PERMISSIONS;
		
		if(!hasUser) {
			document.select("#searchForm").remove();
			document.select("#topMenu").remove();
			document.select("#logoff").remove();
			document.select("nav button").remove();
		} else if(modal) {
			document.select("#topMenu").remove();
		} else {
			
			try(var timed = Instrumentation.timed("Header#sort")) {
				sortMenus(parents, sorted);
			}
			
			var menuItems = new LinkedHashMap<ApplicationMenu, List<ApplicationMenu>>();

			try(var timed = Instrumentation.timed("Header#filter")) {
				filterMenus(isAdmin, resolvedPermissions, parents, sorted, user, menuItems);
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
		
		if(modal) {
			var umenu = document.getElementById("userMenu");
			if(umenu != null) {
				umenu.remove();
			}
		}
		else {
			Session.getOr().ifPresentOrElse(session -> {
				try(var timed = Instrumentation.timed("Header#usermenu")) {
					userMenu( isAdmin, resolvedPermissions, document, parents, sorted, session);
				}
			}, () -> {
				var umenu = document.getElementById("userMenu");
				if(umenu != null) {
					umenu.remove();
				}
			});
		}
	}

	private void userMenu(boolean isAdmin, Set<String> resolvedPermissions, Document document, ArrayList<ApplicationMenu> parents,
			HashMap<String, List<ApplicationMenu>> sorted, Session session) {
		var umenuLink = document.getElementById("userMenuLink");
		if(umenuLink != null) {
			
			var user = session.getUser();
			var txt = Html.span(user.getUsername());
			txt.addClass("fw-bold");
			txt.addClass("me-3");
			
			Element icn;
			try(var timer = Instrumentation.timed("Header#userMenu.avatar")) {
				icn = avatarService.avatar(new AvatarRequest.Builder().
				forUser(user).
				build()).
					render(); 
			}
					
			umenuLink.appendChild(txt);
			umenuLink.appendChild(icn);
			
			var itemTemplate = document.getElementById("userMenuItem");
			itemTemplate.remove();
			
			var userMenuList = document.getElementById("userMenuList");
			
			for(var parent :  parents.stream().
					filter(p -> p.getUuid() ==  ApplicationMenuService.USER_MENU).
					filter(p -> isAdmin || menuService.checkPermission(p, resolvedPermissions, isAdmin)).
					sorted((o1, o2) -> o1.weight().compareTo(o2.weight())).toList()) {
				
				var items = new ArrayList<ApplicationMenu>(sorted.get(parent.getUuid()).stream().
					filter(c -> {
						if(!menuService.checkPermission(c)) {
							if(log.isDebugEnabled()) {
								log.debug("{} does not have access to menu {}", user.getUsername(), c.getI18n());
							}
							return false;
						}
						return true;
					}).
					sorted((o1,o2) -> o1.weight().compareTo(o2.weight())).toList());

				var lastGroup = 0;
				for(var item : items) {
					
					var group = item.weight() / 100000;
					if(group != lastGroup) {
						userMenuList.appendChild(Html.hr());
						lastGroup = group;
					}
					
					var userMenuItem = itemTemplate.clone();
					
					userMenuItem.getElementById("userMenuItemIcon").
						addClass("fa-solid").
						addClass("me-2").
						addClass(item.getIcon());
					
					userMenuItem.getElementById("userMenuItemText")
						.attr("jad:bundle", item.getBundle())
						.attr("jad:i18n", item.getI18n());

					userMenuItem.getElementById("userMenuItemLink")
						.attr("href", item.getPath());
					
					userMenuList.appendChild(userMenuItem);
				}
				
			}
			
		}
	}

	private void filterMenus(boolean isAdmin, Set<String> resolvedPermissions, ArrayList<ApplicationMenu> parents, HashMap<String, List<ApplicationMenu>> sorted,
			User user, LinkedHashMap<ApplicationMenu, List<ApplicationMenu>> menuItems) {
		for(var parent :  parents.stream().
				filter(p -> p.getUuid() !=  ApplicationMenuService.USER_MENU).
				filter(p -> menuService.checkPermission(p, resolvedPermissions, isAdmin)).
				sorted((o1, o2) -> o1.weight().compareTo(o2.weight())).toList()) {
			var items = new ArrayList<ApplicationMenu>(sorted.get(parent.getUuid()).stream().
				peek(c -> {
					if(log.isDebugEnabled()) {
						log.debug("Checking {} menu access to {}", user.getUsername(), c.getI18n());
					}
				}).
				filter(c -> {
					if(!menuService.checkPermission(c, resolvedPermissions, isAdmin)) {
						if(log.isDebugEnabled()) {
							log.debug("{} does not have access to menu {}", user.getUsername(), c.getI18n());
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
	}

	private void sortMenus(ArrayList<ApplicationMenu> parents, HashMap<String, List<ApplicationMenu>> sorted) {
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
	}

	@Override
	public String getName() {
		return "header";
	}

}
