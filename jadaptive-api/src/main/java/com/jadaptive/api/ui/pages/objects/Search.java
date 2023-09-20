package com.jadaptive.api.ui.pages.objects;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.ui.pages.TemplatePage;
import com.jadaptive.api.ui.pages.ext.TableRenderer;
import com.jadaptive.api.ui.pages.objects.Search.SearchForm;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;

@Component
@RequestPage(path="search/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "help", "i18n"} )
public class Search extends TemplatePage implements FormProcessor<SearchForm> {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ObjectService objectService;;
	
	@Autowired
	private ApplicationService applicationService; 
	
	Integer start = 0;
	Integer length = 10;
	String searchField;
	String searchValue;
	
	@Override
	public String getUri() {
		return "search";
	}

	@Override
	public void onCreate() throws FileNotFoundException {
		super.onCreate();
		
		if(template.getPermissionProtected()) {
			permissionService.assertRead(template.getResourceKey());
		}

		if(!template.getCollectionKey().equals(resourceKey)) {
			throw new UriRedirect(String.format("/app/ui/search/%s", template.getCollectionKey()));
		}
		
		if(template.isSingleton()) {
			throw new UriRedirect(String.format("/app/ui/config/%s", template.getCollectionKey()));
		}
	}	
	
	public final void processForm(Document document, SearchForm form) throws IOException {
		
		searchField = form.getSearchColumn();
		setCachedValue("searchField", searchField);
		
		searchValue = form.getSearchValue();
		setCachedValue("searchValue", searchValue);
		
		start = form.getStart();
		setCachedValue("start", String.valueOf(start));
		
		length = form.getLength();
		setCachedValue("length", String.valueOf(length));
		
		generateTable(document);
	}
	
	private String getCachedValue(String key, String defaultValue) {
		String cachedValue = (String) Request.get().getSession().getAttribute(resourceKey + "." + key);
		if(Objects.isNull(cachedValue)) {
			return defaultValue;
		}
		return cachedValue;
	}
	
	private int getCachedInt(String key, String sessionValue, int defaultValue) {
		String value = getCachedValue(key, sessionValue);
		if(Objects.nonNull(value)) {
			return Integer.parseInt(value);
		}
		return defaultValue;
	}
	
	private void setCachedValue(String key, String value) {
		Request.get().getSession().setAttribute(resourceKey + "." + key, value);
	}

	
	
	@Override
	protected void doGenerateTemplateContent(Document document) throws IOException {
		

		searchField = Request.get().getParameter("column");
		if(Objects.isNull(searchField)) {
			searchField = getCachedValue("searchField", StringUtils.defaultString(Request.get().getParameter("column"), template.getDefaultColumn()));
		}
		
		searchValue = Request.get().getParameter("filter");
		if(Objects.isNull(searchValue)) {
			searchValue = getCachedValue("searchValue", StringUtils.defaultString(Request.get().getParameter("filter"), template.getDefaultFilter()));
			if(StringUtils.isBlank(searchValue)) {
				searchValue = null;
			}
		}
		
		start = getCachedInt("start", (String) Request.get().getParameter("start"), 0);
		length = getCachedInt("length", (String) Request.get().getParameter("length"), 10);
		
		generateTable(document);
	
	}

	protected void generateTable(Document document) throws IOException {
		
		
		DropdownInput searchColumns = new DropdownInput("searchColumn", "default");
		document.selectFirst("#searchDropdownHolder").appendChild(searchColumns.renderInput());
		List<I18nOption> columns = new ArrayList<>();
		
		if(StringUtils.isBlank(searchField)) {
			searchField = template.getDefaultColumn();
		}
		
		columns.add(new I18nOption(template.getDefaultColumn().equals("uuid") ? "default" : template.getBundle(), String.format("%s.name", template.getDefaultColumn()), template.getDefaultColumn()));
		
		for(FieldTemplate field : template.getFields()) {
			if(field.isSearchable() && !field.getResourceKey().equals(template.getDefaultColumn())) {
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
		
		if(start > 0 && totalObjects <= start) {
			start -= length;
		}
		
		Collection<AbstractObject> objects = objectService.table(template.getResourceKey(), searchField, searchValue, start, length);
		
		boolean readOnly = false;
		if(template.getScope()!=ObjectScope.PERSONAL && template.getPermissionProtected()) {
			try {
				permissionService.assertWrite(template.getResourceKey());
			} catch(AccessDeniedException e) {
				readOnly = true;
			}
		}
		
		TableRenderer renderer = applicationService.autowire(new TableRenderer(readOnly));
		renderer.setLength(length);
		renderer.setStart(start);
		renderer.setObjects(objects);
		renderer.setTotalObjects(totalObjects);
		renderer.setTemplate(template);
		renderer.setTemplateClazz(templateClazz);
		
		table.insertChildren(0, renderer.render());

		if(totalObjects > length) {
			renderPagination(totalObjects, table);
		}
		
	}
	
	private Element renderPagination(long totalObjects, Element pagnation) {
		
		long pages = totalObjects / length;
		if(totalObjects % length > 0) {
			pages++;
		}
		
		int currentPage = 0;
		if(start > 0) {
			currentPage = start / length;
		}
		
		Element pageList;

		pagnation.appendChild(Html.nav().appendChild(pageList = Html.ul("pagination")));
		
		if(currentPage > 0) {
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", "0")
							.appendChild(Html.i("fa-solid fa-chevron-double-left"))));
			
			pageList.appendChild(Html.li("page-item")
						.appendChild(Html.a("#", "page-link searchTable")
								.attr("data-start", String.valueOf((currentPage-1)*length))
								.appendChild(Html.i("fa-solid fa-chevron-left"))));
		} else {
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("fa-solid fa-chevron-double-left"))));
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("fa-solid fa-chevron-left"))));
		}

		for(int i=0;i<pages;i++) {
			pageList.appendChild(Html.li("page-item", currentPage == i ? "active" : "")
						.appendChild(Html.a("#", "page-link searchTable")
								.attr("data-start", String.valueOf(i*length))
								.text(String.valueOf(i+1))));
			if(i== 4)
				break;
		}
		
		if(currentPage < pages - 1) {
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", String.valueOf((currentPage+1)*length))
							.appendChild(Html.i("fa-solid fa-chevron-right"))));
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", String.valueOf((pages-1)*length))
							.appendChild(Html.i("fa-solid fa-chevron-double-right"))));
		} else {
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("fa-solid fa-chevron-right"))));
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("fa-solid fa-chevron-double-right"))));
		}
		
		return pagnation;
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
