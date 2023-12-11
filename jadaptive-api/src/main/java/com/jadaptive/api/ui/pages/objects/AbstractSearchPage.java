package com.jadaptive.api.ui.pages.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.pages.TemplatePage;
import com.jadaptive.api.ui.pages.ext.TableRenderer;
import com.jadaptive.api.ui.pages.objects.AbstractSearchPage.SearchForm;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;

public abstract class AbstractSearchPage extends TemplatePage implements FormProcessor<SearchForm> {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	protected Integer start = 0;
	protected Integer length = 10;
	protected String searchField;
	protected String searchValue;
	
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
		
		long totalObjects = generateCount(template, searchField, searchValue);
		
		if(start > 0 && totalObjects <= start) {
			start -= length;
		}
		
		Collection<AbstractObject> objects = generateTable(template, searchField, searchValue, start, length);
		
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
	
	protected abstract Collection<AbstractObject> generateTable(ObjectTemplate template, 
			String searchField, String searchValue,
			Integer start, Integer length);
	
	protected abstract long generateCount(ObjectTemplate template, String searchField, String searchValue);

	private Element renderPagination(long totalObjects, Element pagnation) {
		
		long pages = totalObjects / length;
		if(totalObjects % length > 0) {
			pages++;
		}
		
		long currentPage = 0;
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

		int totalPages = 0;
		long firstPage = Math.max(currentPage-5, 0);
		
		for(long i=firstPage;i<currentPage;i++) {
			pageList.appendChild(Html.li("page-item")
						.appendChild(Html.a("#", "page-link searchTable")
								.attr("data-start", String.valueOf(i*length))
								.text(String.valueOf(i+1))));
			totalPages++;
		}
		
		pageList.appendChild(Html.li("page-item", "active")
				.appendChild(Html.a("#", "page-link searchTable")
						.attr("data-start", String.valueOf(currentPage*length))
						.text(String.valueOf(currentPage+1))));
		long endPage = currentPage + 1;
		
		while(totalPages < 9 && endPage < pages) {
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", String.valueOf(endPage*length))
							.text(String.valueOf(endPage+1))));
			totalPages++;
			endPage++;
		}
	
		if(endPage < pages - 1) {
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
