package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;

@Extension
@RequestPage(path="table2/{resourceKey}/{start}/{length}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils", "freemarker", "i18n"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class ServerSideTablePage extends TemplatePage {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ObjectTemplateRepository templateRepository; 
	
	@Autowired
	private ObjectService objectService;;
	
	Integer start;
	
	Integer length;
	
	String searchField = "resourceKey";
	String searchValue = "";
	
	@Override
	public String getUri() {
		return "table2";
	}

	@Override
	public void created() throws FileNotFoundException {
		super.created();
		
		if(!template.getCollectionKey().equals(resourceKey)) {
			throw new UriRedirect(String.format("/app/ui/table2/%s", template.getCollectionKey()));
		}
	}	

	@Override
	protected void generateAuthenticatedContent(Document document) {
		
		searchField = StringUtils.defaultString(Request.get().getParameter("column"), "resourceKey");
		searchValue = StringUtils.defaultString(Request.get().getParameter("filter"), "");
		
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
		
		if(template.isCreatable()) {
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
		
		Element table = document.selectFirst("#tableholder");
		table.appendChild(table = new Element("table").attr("data-toggle", "table").addClass("table"));
		Element el;
		table.appendChild(new Element("thead").appendChild(el = new Element("tr")));
		
		TableView view = templateClazz.getAnnotation(TableView.class);

		for(String column : view.defaultColumns()) {
			el.appendChild(new Element("td").appendChild(new Element("span")
					.attr("jad:bundle", template.getBundle())
					.attr("jad:i18n", String.format("%s.name", column))));
		}
		
		table.appendChild(el = new Element("tbody"));
		
		long totalObjects = objectService.count(template.getCollectionKey(), searchField, searchValue);
		for(AbstractObject obj : objectService.table(template.getResourceKey(), searchField, searchValue, start, length)) {
			Element row = new Element("tr");
			
			for(String column : view.defaultColumns()) {
				row.appendChild(new Element("td").appendChild(new Element("span").text(StringUtils.defaultString(obj.getValue(column).toString()))));
			}
			
			el.appendChild(row);
		}
		
		long pages = totalObjects / length;
		if(totalObjects % length > 0) {
			pages++;
		}
		
		int currentPage = 0;
		if(start > 0) {
			currentPage = start / length;
		}
		
		if(pages > 1) {
			
			
			Element pageList;
			Element pagnation = document.selectFirst("#pagnation");
			pagnation.appendChild(Html.nav().appendChild(pageList = Html.ul("pagination")));
			
			if(currentPage > 0) {
				pageList.appendChild(Html.li("page-item")
						.appendChild(Html.a(generateTableURL(0, length), "page-link searchTable")
								.appendChild(Html.i("far fa-chevron-double-left"))));
				
				pageList.appendChild(Html.li("page-item")
							.appendChild(Html.a(generateTableURL((currentPage-1)*length, length), "page-link searchTable")
									.appendChild(Html.i("far fa-chevron-left"))));
			} else {
				pageList.appendChild(Html.li("page-item disabled")
						.appendChild(Html.a("#", "page-link")
								.appendChild(Html.i("far fa-chevron-double-left"))));
				pageList.appendChild(Html.li("page-item disabled")
						.appendChild(Html.a("#", "page-link")
								.appendChild(Html.i("far fa-chevron-left"))));
			}
	
			for(int i=0;i<pages;i++) {
				pageList.appendChild(Html.li("page-item", currentPage == i ? "active" : "")
							.appendChild(Html.a(generateTableURL(i*length, length), "page-link searchTable")
									.text(String.valueOf(i+1))));
				if(i== 4)
					break;
			}
			
			if(currentPage < pages - 1) {
				pageList.appendChild(Html.li("page-item")
						.appendChild(Html.a(generateTableURL((currentPage+1)*length, length), "page-link searchTable")
								.appendChild(Html.i("far fa-chevron-right"))));
				pageList.appendChild(Html.li("page-item")
						.appendChild(Html.a(generateTableURL((int) (pages-1)*length, length), "page-link searchTable")
								.appendChild(Html.i("far fa-chevron-double-right"))));
			} else {
				pageList.appendChild(Html.li("page-item disabled")
						.appendChild(Html.a("#", "page-link")
								.appendChild(Html.i("far fa-chevron-right"))));
				pageList.appendChild(Html.li("page-item disabled")
						.appendChild(Html.a("#", "page-link")
								.appendChild(Html.i("far fa-chevron-double-right"))));
			}
		}
		
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

	private String generateTableURL(int start, int length) {
		return String.format("/app/ui/table2/%s/%d/%d", template.getCollectionKey(), start, length);
	}

	private Element renderElement(Element element, FieldTemplate fieldTemplate) {
		return element;
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
	
	

}
