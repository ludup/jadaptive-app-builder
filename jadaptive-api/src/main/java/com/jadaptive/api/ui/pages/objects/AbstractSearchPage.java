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

import com.jadaptive.api.countries.Country;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.SearchUtils;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldOptions;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.pages.objects.AbstractSearchPage.SearchForm;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.api.ui.renderers.form.DateFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownFormInput;
import com.jadaptive.api.ui.renderers.form.FieldSearchFormInput;
import com.jadaptive.api.ui.renderers.form.SwitchFormInput;

public abstract class AbstractSearchPage extends BaseSearchPage<SearchForm> {

	static Logger log = LoggerFactory.getLogger(AbstractSearchPage.class);
	
	protected String searchField;
	protected String searchValue;
	protected String searchValueText;
	protected String searchModifier;
	protected boolean useRegex;

	@Override
	public final void processForm(Document document, SearchForm form) throws IOException {
		
		searchField = form.getSearchColumn();
		setCachedValue("searchField", searchField);
		
		searchValue = form.getSearchValue();
		setCachedValue("searchValue", searchValue);
		
		searchValueText = form.getSearchValueText();
		setCachedValue("searchValueText", searchValueText);
		
		searchModifier = form.getSearchModifier();
		setCachedValue("searchModifier", searchModifier);
		
		useRegex = form.getUseRegex();
		setCachedValue("useRegex", String.valueOf(useRegex));
		
		super.processForm(document, form);
	}

	@Override
	public final String getJsResource() {
		return String.format("%s.js", AbstractSearchPage.class.getSimpleName());
	}
	
	@Override
	public final String getHtmlResource() {
		return String.format("%s.html", AbstractSearchPage.class.getSimpleName());
	}
	
	@Override
	public final Class<?> getResourceClass() {
		return AbstractSearchPage.class;
	}
	
	public interface SearchForm  extends BaseSearchForm {
		String getSearchColumn();
		String getSearchValueText();
		String getSearchModifier();
		boolean getUseRegex();
	}

	@Override
	public Class<SearchForm> getFormClass() {
		return SearchForm.class;
	}

	@Override
	protected void doGenerateTemplateContent(Document document) throws IOException {
		
		document.selectFirst("#form").attr("action", generateSearchPostURI());
		searchField = Request.get().getParameter("column");
		if(Objects.isNull(searchField)) {
			searchField = getCachedValue("searchField", StringUtils.defaultIfBlank(Request.get().getParameter("column"), template.getDefaultColumn()));
		}
		
		searchValue = Request.get().getParameter("filter");
		if(Objects.isNull(searchValue)) {
			searchValue = getCachedValue("searchValue", StringUtils.defaultIfBlank(Request.get().getParameter("filter"), template.getDefaultFilter()));
			if(StringUtils.isBlank(searchValue)) {
				searchValue = null;
			}
		}
		
		searchValueText = Request.get().getParameter("searchValueText");
		if(Objects.isNull(searchValueText)) {
			searchValueText = getCachedValue("searchValueText", StringUtils.defaultIfBlank(Request.get().getParameter("searchValueText"), ""));
			if(StringUtils.isBlank(searchValueText)) {
				searchValueText = null;
			}
		}
		
		searchModifier = Request.get().getParameter("searchModifier");
		if(Objects.isNull(searchModifier)) {
			searchModifier = getCachedValue("searchModifier", StringUtils.defaultIfBlank(Request.get().getParameter("searchModifier"), ""));
			if(StringUtils.isBlank(searchModifier)) {
				searchModifier = null;
			}
		}
		
		String tmp = Request.get().getParameter("useRegex");
		if(Objects.isNull(tmp)) {
			tmp = getCachedValue("useRegex", "false");
			if(StringUtils.isBlank(tmp)) {
				useRegex = false;
			} else {
				useRegex = Boolean.valueOf(tmp);
			}
		} else {
			useRegex = Boolean.valueOf(tmp);
		}
		
		
		super.doGenerateTemplateContent(document);
	
	}

	private void generateSearchColumns(ObjectTemplate template, DropdownInput input, Document document, String parentPrefix, String searchField, Map<String,FieldTemplate> processedFields) throws IOException {
		
		for(FieldTemplate field : template.getFields()) {
			if(processedFields.containsKey(field.getResourceKey())) {
				continue;
			}
			processedFields.put(field.getResourceKey(), field);
			if(field.isSearchable()) {
				if(field.getFieldType()==FieldType.OBJECT_REFERENCE && field.getOptions().contains(FieldOptions.SEARCH_REQUIRE_REFERENCE_READ)) {
					String resourceKey = field.getValidationValue(ValidationType.RESOURCE_KEY);
					try {
						permissionService.assertRead(resourceKey);
					} catch(AccessDeniedException e) {
						continue;
					}
				}
				
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
		
		generateAdditionalColumns(document.selectFirst("#searchValueHolder"), input, searchField);
		
		Element form = document.selectFirst("#searchForm");
		form.appendChild(
				Html.input("hidden", "sortColumn", sortColumn)
						.attr("id", "sortColumn")
						.attr("data-column", template.getDefaultColumn()));
		form.appendChild(Html.input("hidden", "sortOrder", sortOrder.name())
				.attr("id", "sortOrder"));
		
		
	}
	
	private void addSearchValueField(ObjectTemplate template, String parentPrefix, FieldTemplate field, Document document, boolean initial) throws IOException {
		
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
				input.renderInput(holder, searchValue, false, "searchValueField");
			} else {
				input.renderInput(holder, "", false, "d-none", "searchValueField");
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
			dropdown.renderInput(e, "", false);
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
			modifier.renderValues(modifiers, Objects.toString(searchModifier));
			
			Element valueElement = Html.div("col-9");
			e.appendChild(valueElement);
			DateFormInput input = new DateFormInput(template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle());
			input.disableDecoration();
			input.disableIDAttribute();
			if(initial) {
				input.renderInput(valueElement, searchValue,true);
			} else {
				input.renderInput(valueElement, "", true);
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
			dropdown.renderInput(e, "", false);

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
			addReferenceSearchInput(holder, field.getResourceKey(), initial, template.getBundle(), 
					field.getMetaValue("url", 
							String.format("/app/api/%s/%s/table", 
									template.getScope() == ObjectScope.PERSONAL ? "personal" : "references",
									field.getValidationValue(ValidationType.RESOURCE_KEY))));

//			FieldSearchFormInput input = new FieldSearchFormInput(
//					template, field.getResourceKey(), initial ? "searchValue" : "unused", template.getBundle(), 
//						String.format("/app/api/references/%s/table", field.getValidationValue(ValidationType.RESOURCE_KEY)),
//						"name", field.getResourceKey(), "uuid");
//			input.diableDecoration();
//			input.disableIDAttribute();
//			if(!initial) {
//				Element e = input.renderInput(holder,  "",  "", true, false);
//				e.addClass("d-none searchValueField");
//			} else {
//				Element e = input.renderInput(holder,  searchValue,  searchValueText, true, false);
//				e.addClass("searchValueField");
//			}
			
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
			Element i = Html.input("checkbox", "useRegex", "true");
			if(useRegex) {
				i.attr("checked", "checked");
			}
			div.appendChild(i);
			div.appendChild(Html.label("userInterface", "regex.name").addClass("ms-2 small text-muted"));
			holder.appendChild(div);
			
			break;
		}
		}
		
	}
	
	protected void addReferenceSearchInput(Element searchInputs, String searchField, boolean initial, String bundle, String url) {
		
		FieldSearchFormInput input = new FieldSearchFormInput(
				resourceKey, initial ? "searchValue" : "unused",bundle, 
				url, "name", searchField, "uuid");
		input.diableDecoration();
		input.disableIDAttribute();
		if(!initial) {
			Element e = input.renderInput(searchInputs,  "",  "", true, false);
			e.addClass("d-none searchValueField");
		} else {
			Element e = input.renderInput(searchInputs,  searchValue,  searchValueText, true, false);
			e.addClass("searchValueField");
		}
	}

	@Override
	protected SearchField[] generateSearchFields(Document document) throws IOException {
		
		DropdownInput searchColumns = new DropdownInput("searchColumn", "default");
		searchColumns.disableIDAttribute();
		document.selectFirst(".searchDropdownHolder").appendChild(searchColumns.renderInput());
		
		if(StringUtils.isBlank(searchField)) {
			searchField = template.getDefaultColumn();
		}
		
		Map<String,FieldTemplate> searchFieldTemplates = new HashMap<>();
		generateSearchColumns(template, searchColumns, document, "", searchField, searchFieldTemplates);
		
		Element srchCol = document.selectFirst(".searchColumn");
		srchCol.val(searchField);
		srchCol.dataset().put("default-search-column", template.getDefaultColumn());
		
		if(StringUtils.isNotBlank(searchModifier)) {
			searchValue = searchModifier + searchValue;
		}
		
		if(log.isInfoEnabled()) {
			if(StringUtils.isNotBlank(searchValue))
				log.info("Searching for {} {}", searchField, searchValue);
		}
		return SearchUtils.generateSearch(searchField, searchValue, template);
	}

}
