package com.jadaptive.plugins.dashboard.pages;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.QuickSetupItem;
import com.jadaptive.api.ui.QuickSetupItem.Scope;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.plugins.dashboard.DashboardWidget;

@Component
public class QuickSetupWidget implements DashboardWidget {

	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public String getIcon() {
		return "fa-bolt-lightning";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getName() {
		return "quickSetup";
	}

	@Override
	public void renderWidget(Document document, Element element) {
		

		element.appendChild(new Element("h6").addClass("card-title")
				.appendChild(new Element("span")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", "quickSetup.desc")));


		DropdownInput input = new DropdownInput("setupTasks", "default");
		element.appendChild(input.renderInput());
		
		var isAdmin = permissionService.isAdministrator();
		
		input.renderValues(applicationService.getBeans(QuickSetupItem.class).
			stream().
			filter(item -> isShow(isAdmin, item)).
			map(item -> new I18nOption(item.getBundle(), item.getI18n(), item.getLink())).
			toList(), ""
		);
		
		PageHelper.appendBodyScriptSnippet(document, "$('input[name=\"setupTasks\"]').change(function(e) {\r\n"
				+ "			if($(this).val()!=='') {\r\n"
				+ "				window.location = $(this).val();\r\n"
				+ "			}\r\n"
				+ "		});\r\n\r\n");
	}

	@Override
	public Integer weight() {
		return 1;
	}

	@Override
	public boolean wantsDisplay() {
		try {
			var isAdmin = permissionService.isAdministrator();
			for(QuickSetupItem item : applicationService.getBeans(QuickSetupItem.class)) {
				if( isShow(isAdmin, item)) {
					return true;
				}
			}
		} catch(AccessDeniedException e) {
		}
		return false;
	}

	private boolean isShow(boolean isAdmin, QuickSetupItem item) {
		return ( ( item.scope() == Scope.ADMINISTRATOR && isAdmin ) || 
			  ( item.scope() == Scope.USERS && !isAdmin ) || 
			  ( item.scope() == Scope.ANY ) ) &&
			item.isEnabled();
	}

}
