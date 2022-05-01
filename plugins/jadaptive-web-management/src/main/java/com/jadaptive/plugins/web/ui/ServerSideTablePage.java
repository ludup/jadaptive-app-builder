package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.ui.UserInterfaceService;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.plugins.web.ui.ServerSideTablePage.SearchForm;
import com.jadaptive.plugins.web.ui.renderers.TableRenderer;

@Extension
@RequestPage(path="search/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils", "i18n"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class ServerSideTablePage extends TemplatePage implements FormProcessor<SearchForm> {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ObjectTemplateRepository templateRepository; 
	
	@Autowired
	private ObjectService objectService;;
	
	@Autowired
	private UserInterfaceService uiService;
	
	Integer start = 0;
	Integer length = 10;
	String searchField;
	String searchValue;
	
	@Override
	public String getUri() {
		return "search";
	}

	@Override
	public void created() throws FileNotFoundException {
		super.created();
		
		if(!template.getCollectionKey().equals(resourceKey)) {
			throw new UriRedirect(String.format("/app/ui/search/%s", template.getCollectionKey()));
		}
	}	
	
	public final void processForm(Document document, SearchForm form) throws IOException {
		
		searchField = form.getSearchColumn();
		searchValue = form.getSearchValue();
		start = form.getStart();
		length = form.getLength();

		generateTable(document);
	}
	
	protected void processPost(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(uri, request, response);
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws IOException {
		
		searchField = StringUtils.defaultString(Request.get().getParameter("column"), template.getDefaultColumn());
		searchValue = StringUtils.defaultString(Request.get().getParameter("filter"), template.getDefaultFilter());
		
		generateTable(document);
	
	}
	
	private void generateTableActions(Document document, TableView view) {
		
		if(view != null) {
			for(TableAction action : view.actions()) {
				if(action.target()==Target.TABLE) {
					createTableAction(document, action.url(), action.bundle(), action.icon(), action.buttonClass(), action.resourceKey());
				}
			}
		}
		
	}

	protected void generateTable(Document document) throws IOException {
		
		
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
			
			if(creatableTemplates.size() > 1) {
				createMultipleOptionAction(document, "create", creatableTemplates, template.getCollectionKey());
			} else if(creatableTemplates.size() == 1) {
				ObjectTemplate singleTemplate = creatableTemplates.get(0);
				createTableAction(document, String.format("create/%s", singleTemplate.getResourceKey()), 
						template.getCollectionKey(), "far fa-plus",
						"primary", "create");
			} else {
				createTableAction(document, String.format("create/%s", template.getResourceKey()), 
						template.getCollectionKey(), "far fa-plus",
						"primary", "create");
			}
		}
		
		DropdownInput searchColumns = new DropdownInput("searchColumn", "default");
		document.selectFirst("#searchDropdownHolder").appendChild(searchColumns.renderInput());
		List<I18nOption> columns = new ArrayList<>();
		
		for(FieldTemplate field : template.getFields()) {
			if(field.isSearchable()) {
				columns.add(new I18nOption(template.getBundle(), String.format("%s.name", field.getResourceKey()), field.getResourceKey()));
			}
		}
		
		searchColumns.renderValues(columns, searchField);
		
		document.selectFirst("#searchValueHolder").appendChild(Html.text("searchValue", "searchValue", searchValue, "form-control"));
		
		
		DropdownInput searchPage = new DropdownInput("length", "default");
		document.selectFirst("#searchPageHolder").appendChild(searchPage.renderInput());
		
		var pageResults = new ArrayList<I18nOption>();
		pageResults.add(new I18nOption("default", "5.items", "5"));
		pageResults.add(new I18nOption("default", "10.items", "10"));
		pageResults.add(new I18nOption("default", "25.items", "25"));
		pageResults.add(new I18nOption("default", "50.items", "50"));
		pageResults.add(new I18nOption("default", "100.items", "100"));
		pageResults.add(new I18nOption("default", "250.items", "250"));
		searchPage.renderValues(pageResults, String.valueOf(length));
		
		Element table = document.selectFirst("#tableholder");
		
		long totalObjects = objectService.count(template.getCollectionKey(), searchField, searchValue);
		Collection<AbstractObject> objects = objectService.table(template.getResourceKey(), searchField, searchValue, start, length);
		
		TableRenderer renderer = new TableRenderer(false);
		renderer.setLength(length);
		renderer.setStart(start);
		renderer.setObjects(objects);
		renderer.setTotalObjects(totalObjects);
		renderer.setTemplate(template);
		renderer.setTemplateClazz(templateClazz);
		
		table.insertChildren(0, renderer.render());
	
		generateTableActions(document, renderer.getView());
		
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
				.attr("class", String.format("btn btn-%s me-3", buttonClass))
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

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}
	
	public interface SearchForm {
		String getSearchColumn();
		String getSearchValue();
		int getStart();
		int getLength();
		
	}

	@Override
	public Class<SearchForm> getFormClass() {
		return SearchForm.class;
	}

}
