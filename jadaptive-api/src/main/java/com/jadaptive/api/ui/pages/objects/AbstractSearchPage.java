package com.jadaptive.api.ui.pages.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.pages.TemplatePage;
import com.jadaptive.api.ui.pages.ext.TableRenderer;
import com.jadaptive.api.ui.pages.objects.AbstractSearchPage.SearchForm;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.api.ui.renderers.form.BooleanFormInput;
import com.jadaptive.api.ui.renderers.form.DateFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownFormInput;
import com.jadaptive.api.ui.renderers.form.FieldSearchFormInput;

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
		
		start = getCachedInt("start", (String) Request.get().getParameter("start"), 0);
		length = getCachedInt("length", (String) Request.get().getParameter("length"), 10);
		
		generateTable(document);
	
	}

	protected void generateTable(Document document) throws IOException {
		
		
		DropdownInput searchColumns = new DropdownInput("searchColumn", "default");
		document.selectFirst("#searchDropdownHolder").appendChild(searchColumns.renderInput());
		
		if(StringUtils.isBlank(searchField)) {
			searchField = template.getDefaultColumn();
		}
		
		searchColumns.addInputValue(template.getDefaultColumn(), String.format("%s.name", template.getDefaultColumn()), true, template.getDefaultColumn().equals("uuid") ? "default" : template.getBundle());

		if(template.getDefaultColumn().equals("uuid")) {
			
			Element e = Html.text("uuid", "searchValue", searchValue, "form-control");
			Element holder = document.selectFirst("#searchValueHolder");
			if(!searchField.equals(template.getDefaultColumn())) {
				e.addClass("d-none searchValueField");
			} else {
				e.addClass("searchValueField");
			}
			holder.appendChild(e);
		} else {
			addSearchValueField(template, "", template.getField(template.getDefaultColumn()), document, searchField.equals(template.getDefaultColumn()));
		}
		
		generateSearchFields(template, searchColumns, document, "", searchField);
		document.selectFirst("#searchColumn").val(searchField);
		
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
		
		String searchModifier = Request.get().getParameter("searchModifier");
		if(StringUtils.isNotBlank(searchModifier)) {
			searchValue = searchModifier + searchValue;
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
	
	private void generateSearchFields(ObjectTemplate template, DropdownInput input, Document document, String parentPrefix, String searchField) {
		
		for(FieldTemplate field : template.getFields()) {
			if(field.isSearchable() && !field.getResourceKey().equals(template.getDefaultColumn())) {
				input.addInputValue(field.getResourceKey(), String.format("%s.name", field.getResourceKey()), true, template.getBundle()).attr("data-formvar", parentPrefix + field.getFormVariable());
				if(searchField.equals(parentPrefix + field.getFormVariable())) {
					input.setDefaultValue(String.format("%s.name", field.getResourceKey()), 
							field.getResourceKey(), true, template.getBundle()).attr("data-formvar", parentPrefix + field.getFormVariable());
				}
				addSearchValueField(template, parentPrefix, field, document, searchField.equals(parentPrefix + field.getFormVariable()));
			}
			if(field.getFieldType() == FieldType.OBJECT_EMBEDDED) {
				generateSearchFields(templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY)), input, document, parentPrefix + field.getResourceKey() + ".", searchField);
			}
		}
	}

	private void addSearchValueField(ObjectTemplate template, String parentPrefix, FieldTemplate field, Document document, boolean initial) {
		
		if(Objects.isNull(field)) {
			return;
		}
		Element holder = document.selectFirst("#searchValueHolder");
		switch(field.getFieldType()) {
		case BOOL:
		{
			BooleanFormInput input = new BooleanFormInput(template,field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			
			input.disableDecoration();
			if(initial) {
				input.renderInput(holder, searchValue, "searchValueField");
			} else {
				input.renderInput(holder, "", "searchValueField", "d-none");
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
			
			DropdownFormInput dropdown = new DropdownFormInput(template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			dropdown.disableDecoration();
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
			
			
//			DropdownInput modifier = new DropdownInput(initial ? "searchModifier" : "unusedModifier", "userInterface");
//			Collection<I18nOption> modifiers = new ArrayList<>();
//			modifiers.add(new I18nOption("userInterface","equals.name", ""));
//			modifiers.add(new I18nOption("userInterface","gt.name", ">"));
//			modifiers.add(new I18nOption("userInterface","gte.name", ">="));
//			modifiers.add(new I18nOption("userInterface","lt.name", "<"));
//			modifiers.add(new I18nOption("userInterface","lte.name", "<="));
			
//			e.appendChild(Html.div("col-3").appendChild(modifier.renderInput()));
//			modifier.renderValues(modifiers, StringUtils.defaultString(Request.get().getParameter("searchModifier"),""));
			
			Element valueElement = Html.div("col-9");
			e.appendChild(valueElement);
			DateFormInput input = new DateFormInput(template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			input.disableDecoration();
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
			
			DropdownFormInput dropdown = new DropdownFormInput(template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			dropdown.disableDecoration();
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
			String searchText = Request.get().getParameter("searchValueText");
			
			ObjectTemplate referenceTemplate = templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
			FieldSearchFormInput input = new FieldSearchFormInput(
					template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle(), 
					String.format("/app/api/objects/%s/table", field.getValidationValue(ValidationType.RESOURCE_KEY)),
						referenceTemplate.getNameField(), field.getResourceKey(), "uuid");
			input.diableDecoration();
			
			if(!initial) {
				Element e = input.renderInput(holder,  "",  "", true, false);
				e.addClass("d-none searchValueField");
			} else {
				Element e = input.renderInput(holder,  searchValue,  searchText, true, false);
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
			
			
//			DropdownInput modifier = new DropdownInput(initial ? "searchModifier" : "unusedModifier", "userInterface");
//			Collection<I18nOption> modifiers = new ArrayList<>();
//			modifiers.add(new I18nOption("userInterface","equals.name", ""));
//			modifiers.add(new I18nOption("userInterface","gt.name", ">"));
//			modifiers.add(new I18nOption("userInterface","gte.name", ">="));
//			modifiers.add(new I18nOption("userInterface","lt.name", "<"));
//			modifiers.add(new I18nOption("userInterface","lte.name", "<="));
//			
//			e.appendChild(Html.div("col-3").appendChild(modifier.renderInput()));
//			modifier.renderValues(modifiers, StringUtils.defaultString(Request.get().getParameter("searchModifier"),""));
			
			Element valueElement = Html.div("col-9");
			e.appendChild(valueElement);
			valueElement.appendChild(Html.text(field.getResourceKey(), initial ? "searchValue" : "unused", initial ? searchValue : "", "form-control"));
			break;
		}
		case TEXT:
		case TEXT_AREA:
		default:
		{
			Element e = Html.text(field.getResourceKey(), initial ? "searchValue" : "unused", initial ? searchValue : "", "form-control");
			if(!initial) {
				e.addClass("d-none searchValueField");
			} else {
				e.addClass("searchValueField");
			}
			holder.appendChild(e);
			break;
		}
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
