package com.jadaptive.api.ui.pages.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.SearchUtils;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.pages.TemplatePage;
import com.jadaptive.api.ui.pages.ext.TableRenderer;
import com.jadaptive.api.ui.pages.objects.AbstractSearchPage.SearchForm;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.api.ui.renderers.form.DateFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownFormInput;
import com.jadaptive.api.ui.renderers.form.FieldSearchFormInput;
import com.jadaptive.api.ui.renderers.form.SwitchFormInput;

public abstract class AbstractSearchPage extends TemplatePage implements FormProcessor<SearchForm> {

	static Logger log = LoggerFactory.getLogger(AbstractSearchPage.class);
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private InternationalService internationalService; 
	
	@Autowired
	private ClassLoaderService classService; 
	
	protected Integer start = 0;
	protected Integer length = 10;
	protected String searchField;
	protected String searchValue;
	protected String searchValueText;
	protected String searchModifier;
	protected String sortColumn;
	protected SortOrder sortOrder;
	
	public final void processForm(Document document, SearchForm form) throws IOException {
		
		searchField = form.getSearchColumn();
		setCachedValue("searchField", searchField);
		
		searchValue = form.getSearchValue();
		setCachedValue("searchValue", searchValue);
		
		searchValueText = form.getSearchValueText();
		setCachedValue("searchValueText", searchValueText);
		
		searchModifier = form.getSearchModifier();
		setCachedValue("searchModifier", searchModifier);
		
		start = form.getStart();
		setCachedValue("start", String.valueOf(start));
		
		length = form.getLength();
		length =  Math.max(length, 10);
		
		setCachedValue("length", String.valueOf(length));
		
		sortColumn = StringUtils.defaultString(form.getSortColumn(), form.getSearchColumn());
		setCachedValue("sortColumn", sortColumn);
		
		sortOrder = SortOrder.valueOf(StringUtils.defaultString(form.getSortOrder(), "ASC"));
		setCachedValue("sortOrder", sortOrder.name());
		
		generateTable(document);
	}

	@Override
	public String getJsResource() {
		return String.format("%s.js", AbstractSearchPage.class.getSimpleName());
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
		
		searchValueText = Request.get().getParameter("searchValueText");
		if(Objects.isNull(searchValueText)) {
			searchValueText = getCachedValue("searchValueText", StringUtils.defaultString(Request.get().getParameter("searchValueText"), ""));
			if(StringUtils.isBlank(searchValueText)) {
				searchValueText = null;
			}
		}
		
		searchModifier = Request.get().getParameter("searchModifier");
		if(Objects.isNull(searchModifier)) {
			searchModifier = getCachedValue("searchModifier", StringUtils.defaultString(Request.get().getParameter("searchModifier"), ""));
			if(StringUtils.isBlank(searchModifier)) {
				searchModifier = null;
			}
		}
		
		sortColumn = Request.get().getParameter("sortColumn");
		if(Objects.isNull(sortColumn)) {
			sortColumn = getCachedValue("sortColumn", StringUtils.defaultString(Request.get().getParameter("sortColumn"), template.getDefaultColumn()));
			if(StringUtils.isBlank(sortColumn)) {
				sortColumn = null;
			}
		}
		
		String order = Request.get().getParameter("sortOrder");
		if(Objects.isNull(order)) {
			order = getCachedValue("sortOrder", StringUtils.defaultString(Request.get().getParameter("sortOrder"), "ASC"));
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
		
		generateTable(document);
	
	}

	protected void generateTable(Document document) throws IOException {
		
		
		DropdownInput searchColumns = new DropdownInput("searchColumn", "default");
		searchColumns.disableIDAttribute();
		document.selectFirst(".searchDropdownHolder").appendChild(searchColumns.renderInput());
		
		if(StringUtils.isBlank(searchField)) {
			searchField = template.getDefaultColumn();
		}
		
		Map<String,FieldTemplate> searchFieldTemplates = new HashMap<>();
		generateSearchColumns(template, searchColumns, document, "", searchField, searchFieldTemplates);
		document.selectFirst(".searchColumn").val(searchField);
		
		Element table = document.selectFirst("#tableholder");
		
		if(StringUtils.isNotBlank(searchModifier)) {
			searchValue = searchModifier + searchValue;
		}
		
		if(log.isInfoEnabled()) {
			if(StringUtils.isNotBlank(searchValue))
				log.info("Searching for {} {}", searchField, searchValue);
		}
		
		SearchField[] search = SearchUtils.generateSearch(searchField, searchValue, searchFieldTemplates);
		long totalObjects = generateCount(template, search);
		
		if(start > 0 && totalObjects <= start) {
			start -= length;
		}
		
		Collection<AbstractObject> objects = generateTable(template, start, length, searchFieldTemplates, search);
		
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
		renderer.setSortColumn(sortColumn);
		renderer.setSortOrder(sortOrder);
		
		table.insertChildren(0, renderer.render());

		renderPagination(totalObjects, table.selectFirst("#pagnation"));
		
	}
	
	private void generateSearchColumns(ObjectTemplate template, DropdownInput input, Document document, String parentPrefix, String searchField, Map<String,FieldTemplate> processedFields) {
		
		for(FieldTemplate field : template.getFields()) {
			if(processedFields.containsKey(field.getResourceKey())) {
				continue;
			}
			processedFields.put(field.getResourceKey(), field);
			if(field.isSearchable()) {
				input.addInputValue(field.getResourceKey(), String.format("%s.name", field.getResourceKey()), true, template.getBundle()).attr("data-formvar", parentPrefix + field.getFormVariable());
				if(searchField.equals(parentPrefix + field.getFormVariable())) {
					input.setDefaultValue(String.format("%s.name", field.getResourceKey()), 
							field.getResourceKey(), true, template.getBundle()).attr("data-formvar", parentPrefix + field.getFormVariable());
				}
				addSearchValueField(template, parentPrefix, field, document, searchField.equals(parentPrefix + field.getFormVariable()));
			}
			if(field.getFieldType() == FieldType.OBJECT_EMBEDDED) {
				generateSearchColumns(templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY)), input, document, parentPrefix + field.getResourceKey() + ".", searchField, processedFields);
			}
		}
		
		for(String childTemplate : template.getChildTemplates()) {
			ObjectTemplate t = templateService.get(childTemplate);
			generateSearchColumns(t, input, document, parentPrefix, searchField, processedFields);
		}
		
		document.selectFirst("#searchForm").appendChild(
				Html.input("hidden", "sortColumn", sortColumn)
						.attr("id", "sortColumn")
						.attr("data-column", template.getDefaultColumn()));
		document.selectFirst("#searchForm").appendChild(Html.input("hidden", "sortOrder", sortOrder.name())
				.attr("id", "sortOrder"));
		
		
	}

	private void addSearchValueField(ObjectTemplate template, String parentPrefix, FieldTemplate field, Document document, boolean initial) {
		
		if(Objects.isNull(field)) {
			return;
		}
		Element holder = document.selectFirst("#searchValueHolder");
		switch(field.getFieldType()) {
		case BOOL:
		{
			SwitchFormInput input = new SwitchFormInput(field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			
			input.disableDecoration();
			input.disableIDAttribute();
			
			if(initial) {
				input.renderInput(holder, searchValue, "searchValueField");
			} else {
				input.renderInput(holder, "", "d-none", "searchValueField");
			}
			break;
		}
		case COUNTRY:
		{
			Element e;
			holder.appendChild(e = Html.div());
			if(initial) {
				e.addClass("searchValueField row");
			} else {
				e.addClass("d-none searchValueField row");
			}
			
			DropdownFormInput dropdown = new DropdownFormInput(field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			dropdown.disableDecoration();
			dropdown.disableIDAttribute();
			dropdown.renderInput(e, "");
			for(Country country : internationalService.getCountries()) {
				dropdown.addInputValue(country.getCode(), country.getName());
			}
			
			if(StringUtils.isNotBlank(searchValue) && initial) {
				dropdown.setSelectedValue(searchValue, internationalService.getCountryName(searchValue));
			}
			break;
		}
		case DATE:
		case TIMESTAMP:
		{
			Element e;
			holder.appendChild(e = Html.div());
			
			if(initial) {
				e.addClass("searchValueField row");
			} else {
				e.addClass("d-none searchValueField row");
			}
			
			
			DropdownInput modifier = new DropdownInput(initial ? "searchModifier" : "unusedModifier", "userInterface");
			modifier.disableIDAttribute();
			Collection<I18nOption> modifiers = new ArrayList<>();
			modifiers.add(new I18nOption("userInterface","equals.name", ""));
			modifiers.add(new I18nOption("userInterface","gt.name", ">"));
			modifiers.add(new I18nOption("userInterface","gte.name", ">="));
			modifiers.add(new I18nOption("userInterface","lt.name", "<"));
			modifiers.add(new I18nOption("userInterface","lte.name", "<="));
			
			e.appendChild(Html.div("col-3").appendChild(modifier.renderInput()));
			modifier.renderValues(modifiers, StringUtils.defaultString(searchModifier));
			
			Element valueElement = Html.div("col-9");
			e.appendChild(valueElement);
			DateFormInput input = new DateFormInput(template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			input.disableDecoration();
			input.disableIDAttribute();
			if(initial) {
				input.renderInput(valueElement, searchValue);
			} else {
				input.renderInput(valueElement, "");
			}

			break;
		}
		case ENUM:
		{
			Element e;
			holder.appendChild(e = Html.div());
			if(initial) {
				e.addClass("searchValueField row");
			} else {
				e.addClass("d-none searchValueField row");
			}
			
			DropdownFormInput dropdown = new DropdownFormInput(field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			dropdown.disableDecoration();
			dropdown.disableIDAttribute();
			dropdown.renderInput(e, "");

			try {
				Class<?> clz = classService.findClass(field.getValidationValue(ValidationType.OBJECT_TYPE));
				
				for(Object c : clz.getEnumConstants()) {
					dropdown.addInputValue(c.toString(), c.toString());
				}
				
				if(StringUtils.isNotBlank(searchValue) && initial) {
					dropdown.setSelectedValue(searchValue, searchValue);
				}
			} catch (ClassNotFoundException e1) {
				log.error("Expected enum constants", e1);
			}
		
			break;
		}
		case OBJECT_REFERENCE:
		{
			ObjectTemplate referenceTemplate = templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
			FieldSearchFormInput input = new FieldSearchFormInput(
					template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle(), 
					String.format("/app/api/objects/%s/table", field.getValidationValue(ValidationType.RESOURCE_KEY)),
						referenceTemplate.getNameField(), field.getResourceKey(), "uuid");
			input.diableDecoration();
			input.disableIDAttribute();
			if(!initial) {
				Element e = input.renderInput(holder,  "",  "", true, false);
				e.addClass("d-none searchValueField");
			} else {
				Element e = input.renderInput(holder,  searchValue,  searchValueText, true, false);
				e.addClass("searchValueField");
			}
			
			break;
		}
		case DECIMAL:
		case INTEGER:
		case LONG:
		{
			Element e;
			holder.appendChild(e = Html.div());
			
			if(initial) {
				e.addClass("searchValueField row");
			} else {
				e.addClass("d-none searchValueField row");
			}
			
			
			DropdownInput modifier = new DropdownInput(initial ? "searchModifier" : "unusedModifier", "userInterface");
			modifier.disableIDAttribute();
			
			Collection<I18nOption> modifiers = new ArrayList<>();
			modifiers.add(new I18nOption("userInterface","equals.name", ""));
			modifiers.add(new I18nOption("userInterface","gt.name", ">"));
			modifiers.add(new I18nOption("userInterface","gte.name", ">="));
			modifiers.add(new I18nOption("userInterface","lt.name", "<"));
			modifiers.add(new I18nOption("userInterface","lte.name", "<="));
			
			e.appendChild(Html.div("col-3").appendChild(modifier.renderInput()));
			modifier.renderValues(modifiers, searchModifier);
			
			Element valueElement = Html.div("col-9");
			e.appendChild(valueElement);
			
			Element input = new Element("input")
					.attr("type", "text")
					.attr("name",  initial ? "searchValue" : "unused")
					.addClass(field.getResourceKey() + " form-control");
			if(initial) {
				input.val(searchValue);
			} 
			valueElement.appendChild(input);
			break;
		}
		case TEXT:
		case TEXT_AREA:
		default:
		{
			Element input;
			Element div = new Element("div")
					.appendChild(input = new Element("input")
							.attr("type", "text")
							.attr("name",  initial ? "searchValue" : "unused")
							.addClass(field.getResourceKey() + " form-control"));
			if(initial) {
				input.val(searchValue);
				div.addClass("searchValueField");
			} else {
				div.addClass("d-none searchValueField");
			}
			holder.appendChild(div);
			break;
		}
		}
		
	}

	protected abstract Collection<AbstractObject> generateTable(ObjectTemplate template, 
			Integer start, Integer length, Map<String, FieldTemplate> searchFieldTemplates, SearchField... fields);
	
	protected abstract long generateCount(ObjectTemplate template, SearchField... fields);

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
		String getSortOrder();
		String getSortColumn();
		String getSearchValueText();
		String getSearchValue();
		String getSearchModifier();
		int getStart();
		int getLength();
		
	}

	@Override
	public Class<SearchForm> getFormClass() {
		return SearchForm.class;
	}

}
