package com.jadaptive.api.ui.pages.objects;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.App;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.pages.TemplatePage;
import com.jadaptive.api.ui.pages.ext.TableRenderer;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;

public abstract class BaseSearchPage<T extends BaseSearchForm> extends TemplatePage implements FormProcessor<T> {

	static Logger log = LoggerFactory.getLogger(AbstractSearchPage.class);

	@Autowired
	protected PermissionService permissionService; 
	
	@Autowired
	protected App applicationService; 
	
	@Autowired
	protected InternationalService internationalService; 
	
	@Autowired
	protected ClassLoaderService classService;
	
	@Autowired
	protected ObjectService objectService;
	
	protected Integer start = 0;
	protected Integer length = 10;
	protected String sortColumn;
	protected SortOrder sortOrder;
	protected TableView tableView;
	
	@Override
	protected void beforeGenerateContent(Document document) {
		
		super.beforeGenerateContent(document);
		
		Class<?> tmp = templateClazz;
		do {
			tableView = tmp.getAnnotation(TableView.class);
			tmp = tmp.getSuperclass();
		} while(tableView == null && !tmp.equals(Object.class));
		
		if(tableView == null) {
			throw new IllegalStateException(MessageFormat.format("{0} has no {1} annotation.", templateClazz.getName(), TableView.class.getName()));
		}
	}
	
	public void processForm(Document document, T form) throws IOException {
		
		document.selectFirst("#form").attr("action", generateSearchPostURI());
		
		start = form.getStart();
		setCachedValue("start", String.valueOf(start));
		
		length = form.getLength();
		length =  Math.max(length, 10);
		
		setCachedValue("length", String.valueOf(length));

		sortColumn = Objects.toString(form.getSortColumn(), tableView.sortField());
		setCachedValue("sortColumn", sortColumn);
		
		sortOrder = SortOrder.valueOf(Objects.toString(form.getSortOrder(), tableView.sortOrder().name()));
		setCachedValue("sortOrder", sortOrder.name());
		
		generateTable(document, generateSearchFields(document));
	}
	
	@Override
	public FieldView getScope() {
		return FieldView.TABLE;
	}

	@Override
	protected void doGenerateTemplateContent(Document document) throws IOException {
		
		document.selectFirst("#form").attr("action", generateSearchPostURI());
		
		sortColumn = Request.get().getParameter("sortColumn");
		if(Objects.isNull(sortColumn)) {
			sortColumn = getCachedValue("sortColumn", Objects.toString(Request.get().getParameter("sortColumn"),tableView.sortField()));
			if(StringUtils.isBlank(sortColumn)) {
				sortColumn = null;
			}
		}
		
		String order = Request.get().getParameter("sortOrder");
		if(Objects.isNull(order)) {
			order = getCachedValue("sortOrder", Objects.toString(Request.get().getParameter("sortOrder"), tableView.sortOrder().name()));
			if(StringUtils.isBlank(order)) {
				order = null;
			}
		}
		
		sortOrder = SortOrder.ASC;
		if(Objects.nonNull(order)) {
			sortOrder = SortOrder.valueOf(order.toUpperCase());
		}
		
		start = getCachedInt("start", (String) Request.get().getParameter("start"), 0);
		length = getCachedInt("length", (String) Request.get().getParameter("length"), 10);
		
		generateTable(document, generateSearchFields(document));
	}
	
	
	protected abstract SearchField[] generateSearchFields(Document document) throws IOException;
	
	protected String generateSearchPostURI() {
		return String.format("/app/ui/%s/%s", getUri(), getResourceKey());
	}
	
	protected void generateTable(Document document, SearchField[] search) throws IOException {
		
		Element table = document.selectFirst("#tableholder");
		
		long totalObjects = generateCount(template, search);
		
		if(start > 0 && totalObjects <= start) {
			start -= length;
		}
		
		Collection<AbstractObject> objects = generateTable(template, start, length, search);
		
		boolean readOnly = false;
		if(template.getScope()!=ObjectScope.PERSONAL && template.getPermissionProtected()) {
			try {
				permissionService.assertWrite(template.getResourceKey());
			} catch(AccessDeniedException e) {
				readOnly = true;
			}
		}

		boolean filtered = search.length > 0;

		TableRenderer renderer = applicationService.autowire(createTableRenderer(readOnly, template));
		renderer.setObjects(objects);
		renderer.setTotalObjects(totalObjects);
		renderer.setTemplateClazz(templateClazz);
		renderer.setSortColumn(sortColumn);
		renderer.setSortOrder(sortOrder);
		
		table.insertChildren(0, renderer.render());

		Element pagnation = table.selectFirst("#pagnation");
		if(getNumberOfPages(totalObjects) < 2)
			pagnation.parent().remove();
		else {
			pagnation.dataset().put("jad-filtered", String.valueOf(filtered));
			renderPagination(totalObjects, pagnation);
		}
		
		if (totalObjects == 0) {

			var div = Html.div("mb-3");
			if (filtered) {
				div.appendChild(Html.i18nWithFallback("userInterface", "search.noMatch", template.getBundle(),
						template.getResourceKey() + ".noMatch"));
			} else {
				/* No point in showing search form if there is nothing to search */
				document.selectFirst("#searchForm").remove();
				document.selectFirst("#simpleSearch").remove();
				
				if (table.getElementById("create") == null) {
					div.appendChild(Html.i18nWithFallback("userInterface", "search.noResults", template.getBundle(),
							template.getResourceKey() + ".noResults"));
				} else {
					div.appendChild(Html.i18nWithFallback("userInterface", "search.noResults.creatable",
							template.getBundle(), template.getResourceKey() + ".noResults.createable"));
				}
			}
			table.insertChildren(0, div);
		}
		
		Element form = document.selectFirst("form");
		form.appendChild(
				Html.input("hidden", "sortColumn", sortColumn)
						.attr("id", "sortColumn")
						.attr("data-column", template.getDefaultColumn()));
		form.appendChild(Html.input("hidden", "sortOrder", sortOrder.name())
				.attr("id", "sortOrder"));
	}
	
	protected TableRenderer createTableRenderer(boolean readOnly, ObjectTemplate template) {
		return new TableRenderer(readOnly, template);
	}

	protected void generateAdditionalColumns(Element searchInputs, DropdownInput input, String searchField) {
		
	}
	
	protected Collection<AbstractObject> generateTable(ObjectTemplate template,
			Integer start, Integer length, SearchField... fields) {
		return objectService.tableObjects(template.getResourceKey(), start, length, sortColumn, sortOrder, fields);
	}

	protected long generateCount(ObjectTemplate template, SearchField... fields) {
		return  objectService.countObjects(template.getCollectionKey(), fields);
	}

	private Element renderPagination(long totalObjects, Element pagnation) {
		
		long pages = getNumberOfPages(totalObjects);
		
		long currentPage = 0;
		if(start > 0) {
			currentPage = start / length;
		}

		Element pageList;
		pagnation.appendChild(Html.nav().appendChild(pageList = Html.ul("pagination")));
		
		pageList.dataset().put("jad-page-number", String.valueOf(currentPage));
		pageList.dataset().put("jad-pages", String.valueOf(pages));
		
		Element pageSize = pagnation.nextElementSibling();
		DropdownInput searchPage = new DropdownInput("length", "default");
		pageSize.appendChild(searchPage.renderInput());
		
		var pageResults = new ArrayList<I18nOption>();
		pageResults.add(new I18nOption("default", "10.items", "10"));
		pageResults.add(new I18nOption("default", "25.items", "25"));
		pageResults.add(new I18nOption("default", "50.items", "50"));
		pageResults.add(new I18nOption("default", "100.items", "100"));
		pageResults.add(new I18nOption("default", "250.items", "250"));
		searchPage.renderValues(pageResults, String.valueOf(length));
		
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
	
	private long getNumberOfPages(long totalObjects) {
		long pages = totalObjects / length;
		if(totalObjects % length > 0) {
			pages++;
		}
		return pages;
	}
	
	protected String getCachedValue(String key, String defaultValue) {
		String cachedValue = (String) Request.get().getSession().getAttribute(resourceKey + "." + key);
		if(Objects.isNull(cachedValue)) {
			return defaultValue;
		}
		return cachedValue;
	}
	
	protected int getCachedInt(String key, String sessionValue, int defaultValue) {
		String value = getCachedValue(key, sessionValue);
		if(Objects.nonNull(value)) {
			try {
			return Integer.parseInt(value);
			} catch(NumberFormatException e) { }
		}
		return defaultValue;
	}
	
	protected void setCachedValue(String key, String value) {
		setCachedValue(resourceKey, key, value);
	}
	
	public static void setCachedValue(String resourceKey, String key, String value) {
		Request.get().getSession().setAttribute(resourceKey + "." + key, value);
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
