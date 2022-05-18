package com.jadaptive.plugins.web.ui;

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
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class Header extends AbstractPageExtension {

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
				
				if(Objects.nonNull(parent.getPermissions()) && !parent.getPermissions().isEmpty()) {
					try {
						permissionService.assertAnyPermission(parent.getPermissions().toArray(new String[0]));
					} catch(AccessDeniedException e) {
						continue;
					}
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
					
					if(Objects.nonNull(child.getPermissions()) && !child.getPermissions().isEmpty()) {
						try {
							permissionService.assertAnyPermission(child.getPermissions().toArray(new String[0]));
						} catch(AccessDeniedException e) {
							continue;
						}
					}
					
					Element link;
					parentElement.appendChild(link = new Element("a")
							.addClass("dropdown-item me-3")
							.attr("href", child.getPath())
							.appendChild(new Element("i")
									.addClass("nav-icon me-2 far fa-fw fa-" + child.getIcon()))
							.appendChild(new Element("span")
									.attr("jad:bundle", child.getBundle())
										.attr("jad:i18n", child.getResourceKey())));
					
					if(!child.isEnabled()) {
						link.addClass("disabled");
						link.attr("href", "#");
					}
				}
				
				if(!parentElement.children().isEmpty()) {
					
					Element link;
					topMenu.appendChild(new Element("li")
							.addClass("nav-item dropdown me-3")
							.appendChild(link = new Element("a")
									.addClass("nav-link dropdown-toggle")
									.attr("href", "#")
									.attr("data-bs-toggle", "dropdown")
									.attr("aria-haspopup", "true")
									.attr("aria-expanded", "false")
									.appendChild(new Element("i")
											.addClass("nav-icon me-2 far fa-fw fa-" + parent.getIcon()))
									.appendChild(new Element("span")
											.attr("jad:bundle", parent.getBundle())
											.attr("jad:i18n", parent.getResourceKey())))
							.appendChild(parentElement));
					
					if(!parent.isEnabled()) {
						link.addClass("disabled");
						link.attr("href", "#");
					}
				}
			}
		}
		
		if(StringUtils.isNotBlank(productService.getLogoResource())) {
			document.selectFirst("#logo").attr("src", productService.getLogoResource());
		}
		
		if(StringUtils.isNotBlank(productService.getFaviconResource())) {
			document.selectFirst("head")
				.appendElement("link")
					.attr("href", productService.getFaviconResource())
					.attr("rel", "icon");
		}

	}

	@Override
	public String getName() {
		return "header";
	}

}
