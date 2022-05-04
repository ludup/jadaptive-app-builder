package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.ui.UserInterfaceService;

@Extension
@RequestPage(path="table/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils", "i18n"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class TablePage extends TemplatePage {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ObjectTemplateRepository templateRepository; 
	
	@Autowired
	private UserInterfaceService uiService;
	
	@Override
	public String getUri() {
		return "table";
	}

	@Override
	public void onCreate() throws FileNotFoundException {
		
		super.onCreate();
		
		if(!template.getCollectionKey().equals(resourceKey)) {
			throw new UriRedirect(String.format("/app/ui/table/%s", template.getCollectionKey()));
		}
	}	

	@Override
	protected void generateAuthenticatedContent(Document document) {
		
		
		
		if(uiService.canCreate(template)) {
			
			List<ObjectTemplate> creatableTemplates = new ArrayList<>();
			
			if(!template.getChildTemplates().isEmpty()) {
				ObjectTemplate collectionTemplate = templateRepository.get(template.getCollectionKey());
				
				for(String template : collectionTemplate.getChildTemplates()) {
					ObjectTemplate childTemplate = templateRepository.get(template);
					if(childTemplate.isCreatable()) {
						creatableTemplates.add(childTemplate);
					}
				}
			} 
			
			creatableTemplates.add(template);
			if(creatableTemplates.size() > 1) {
				createMultipleOptionAction(document, "create", creatableTemplates, template.getCollectionKey());
			} else if(creatableTemplates.size() == 1) {
				ObjectTemplate singleTemplate = creatableTemplates.get(0);
				createTableAction(document, String.format("create/%s", singleTemplate.getResourceKey()), 
						template.getCollectionKey(), "far fa-plus",
						"primary", "create");
			}
		}
		
//		DropdownInput searchColumns = new DropdownInput("searchColumn", "default");
//		document.selectFirst("#searchDropdownHolder").appendChild(searchColumns.renderInput());
//		List<I18nOption> columns = new ArrayList<>();
//		columns.add(new I18nOption("default", "default.value", ""));
//		
//		for(FieldTemplate field : template.getFields()) {
//			if(field.isSearchable()) {
//				columns.add(new I18nOption(template.getBundle(), String.format("%s.name", field.getResourceKey()), field.getResourceKey()));
//			}
//		}
//		
//		searchColumns.renderValues(columns, "");
		
		
		TableView view = templateClazz.getAnnotation(TableView.class);
		Element rowActions = document.selectFirst("#rowActions");
		
		if(view != null) {
			for(TableAction action : view.actions()) {
				if(action.target()==Target.TABLE) {
					createTableAction(document, action.url(), action.bundle(), action.icon(), action.buttonClass(), action.resourceKey());
				} else {
					rowActions.appendChild( new Element("div")
						.addClass("tableAction")
						.attr("data-url", action.url())
						.attr("data-bundle", action.bundle())
						.attr("data-icon", action.icon())
						.attr("data-resourcekey", action.resourceKey())
						.attr("data-window", action.window().name()));
				}
			}
		}
		
		try {
			permissionService.assertReadWrite(template.getResourceKey());
		} catch(AccessDeniedException e) {
			document.select(".readWrite").remove();
		}
	}

	private void createMultipleOptionAction(Document document, String id, Collection<ObjectTemplate> actions,
			String bundle) {
		
		Element menu;
		document.selectFirst("#objectActions").appendChild(
				new Element("div")
				    .addClass("dropdown")
					.appendChild(new Element("button")
							.addClass("btn btn-primary dropdown-toggle")
							.attr("type", "button")
							.attr("data-bs-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false")
							.attr("id", id)
							.appendChild(new Element("i")
								.addClass("far fa-plus me-1"))
							.appendChild(new Element("span")
								.attr("jad:bundle", bundle)
								.attr("jad:i18n", String.format("%s.name", id))))
					.appendChild(menu = new Element("div")
							.addClass("dropdown-menu")
							.attr("aria-labelledby", id)));
		
		for(ObjectTemplate action : actions) {
			menu.appendChild(new Element("a")
					.addClass("dropdown-item")
					.attr("href",String.format("/app/ui/%s/%s", id, action.getResourceKey()))
					.attr("jad:bundle", action.getBundle())
					.attr("jad:i18n", String.format("%s.name", action.getResourceKey())));
		}
				 
	}
	
	private void createTableAction(Document document, String url, String bundle, String icon, String buttonClass, String resourceKey) {
		document.selectFirst("#objectActions").appendChild(
				new Element("a").attr("href", String.format("/app/ui/%s", replaceParameters(url)))
				.attr("class", String.format("btn btn-%s", buttonClass))
				.appendChild(new Element("i")
						.attr("class", icon + " me-1"))
				.appendChild(new Element("span")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey))));
	}
	
	private Object replaceParameters(String str) {
		return str.replace("${resourceKey}", template.getResourceKey());
	}

	@Override
	public FieldView getScope() {
		return FieldView.TABLE;
	}
	
	

}
