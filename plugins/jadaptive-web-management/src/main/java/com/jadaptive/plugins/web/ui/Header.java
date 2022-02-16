package com.jadaptive.plugins.web.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
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
				
				if(!parent.isEnabled()) {
					continue;
				}
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
					
					if(!child.isEnabled()) {
						continue;
					}
					
					if(Objects.nonNull(child.getPermissions()) && !child.getPermissions().isEmpty()) {
						try {
							permissionService.assertAnyPermission(child.getPermissions().toArray(new String[0]));
						} catch(AccessDeniedException e) {
							continue;
						}
					}
					parentElement.appendChild(new Element("a")
							.addClass("dropdown-item me-3")
							.attr("href", child.getPath())
							.appendChild(new Element("i")
									.addClass("nav-icon me-2 far fa-fw fa-" + child.getIcon()))
							.appendChild(new Element("span")
									.attr("jad:bundle", child.getBundle())
										.attr("jad:i18n", child.getResourceKey())));
				}
				
				if(!parentElement.children().isEmpty()) {
					topMenu.appendChild(new Element("li")
							.addClass("nav-item dropdown me-3")
							.appendChild(new Element("a")
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
				}
			}
		}
		
//		var menus = [];
//		var top = [];
//		$.each(data.resource, function(idx, obj) {
//			if(obj.parent != '') {
//				if(!menus[obj.parent]) {
//					menus[obj.parent] = [];
//				}
//				menus[obj.parent].push(obj);
//			} else {
//				if(!menus[obj.uuid]) {
//					menus[obj.uuid] = [];
//				}
//				top.push(obj);
//			}
//		});
//		$('#topMenu').empty();
		
//		$('#topMenu').append('<li class="nav-item dropdown me-3">'
//				+ '<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'
//				+ obj.title + '</a><div id="' + obj.uuid + '" class="dropdown-menu" aria-labelledby="navbarDropdown"></div></li>');
//		
//		$.each(menus[obj.uuid], function(idx, child) {
//			
//			if(child.hidden) {
//				return;
//			}
//			$('#' + child.parent).append('<a class="dropdown-item me-3" href="' 
//					+ child.path + '"><i class="' + child.icon + ' nav-icon"></i>&nbsp;' + child.title + '</a>');
//	
//		});
	}

	@Override
	public String getName() {
		return "header";
	}

}
