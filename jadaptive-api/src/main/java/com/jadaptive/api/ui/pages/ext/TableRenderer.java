package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDReference;
import com.jadaptive.api.template.DynamicColumn;
import com.jadaptive.api.template.DynamicColumnService;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableAction.Window;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.UserInterfaceService;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;
import com.jadaptive.api.ui.renderers.form.BootstrapBadgeRender;
import com.jadaptive.utils.Utils;

public class TableRenderer {

	@Autowired
	private ObjectTemplateRepository templateRepository; 
	
	@Autowired
	private UserInterfaceService uiService;
	
	@Autowired
	private TemplateService templateService;
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private InternationalService internationalService; 
	
	int start;
	int length;
	
	long totalObjects;
	Collection<AbstractObject> objects;
	
	ObjectTemplate template;
	Class<?> templateClazz;
	
	TableView view;
	AbstractObject parentObject = null;
	FieldTemplate field;
	private boolean readOnly;

	RenderScope formRenderer;
	String formHandler;
	String stashURL = null;
	
	public TableRenderer(boolean readOnly, AbstractObject parentObject, FieldTemplate field,
			RenderScope formRenderer, String formHandler) {
		this.parentObject = parentObject;
		this.field = field;
		this.readOnly = readOnly;
		this.formRenderer = formRenderer;
		this.formHandler = formHandler;
	}
	
	public TableRenderer(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Elements render() throws IOException {
		
		try {
			Elements tableholder = new Elements();
			Element table;
			tableholder.add(table = Html.table("table").attr("data-toggle", "table"));
			
			Element el;
			table.appendChild(Html.thead().appendChild(el = Html.tr()));
			
			view = templateClazz.getAnnotation(TableView.class);
			int columns = 0;
			if(Objects.isNull(view)) {
				throw new IllegalStateException(templateClazz.getSimpleName() + " requires @TableView annotation to render this page");
			}
			for(String column : view.defaultColumns()) {
				FieldTemplate t = template.getField(column);
				if(Objects.nonNull(t)) {
					el.appendChild(Html.td().appendChild(Html.i18n(template.getBundle(),String.format("%s.name", t.getResourceKey()))));
				} else {
					el.appendChild(Html.td().appendChild(Html.i18n(template.getBundle(),String.format("%s.name", column))));
				}
				columns++;
			}
			
			el.appendChild(Html.td());
			columns++;
			
			table.appendChild(el = Html.tbody());
			
			ObjectMapper json = new ObjectMapper();
			
			if(objects.size() > 0) {
				for(AbstractObject obj : objects) {
					
					ObjectTemplate rowTemplate = template;
					if(!obj.getResourceKey().equals(template.getResourceKey())) {
						rowTemplate = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(obj.getResourceKey());
					}
					Element row = Html.tr();
					
					if(Objects.nonNull(parentObject)) {
						String c = json.writeValueAsString(obj);
						row.appendChild(new Element("input").attr("type", "hidden")
							.attr("name", this.field.getResourceKey())
							.val(Base64.getUrlEncoder().encodeToString(c.getBytes("UTF-8"))));
					}
					
					Map<String,DynamicColumn> dynamicColumns = generateDynamicColumns();
					
					for(String column : view.defaultColumns()) {
						if(dynamicColumns.containsKey(column)) {
							DynamicColumn dc = dynamicColumns.get(column);
							DynamicColumnService service = ApplicationServiceImpl.getInstance().getBean(dc.service());
							row.appendChild(Html.td().appendChild(service.renderColumn(column, obj, rowTemplate)));
						} else {
							FieldTemplate t = template.getField(column);
							row.appendChild(Html.td().appendChild(renderElement(obj, rowTemplate, t)));
						}
					}
					
				
					renderRowActions(row, obj, view, rowTemplate, generateActions(rowTemplate.getResourceKey()));
					
					el.appendChild(row);
				}
			} else {
				el.appendChild(Html.tr().appendChild(Html.td().attr("colspan", String.valueOf(columns))
						.addClass("text-center")
						.appendChild(Html.i18n("default", "noResults.text"))));
			}
			
			tableholder.add(new Element("hr").addClass("mt-5"));
			tableholder.add(Html.div("row", "mb-3").appendChild(
						Html.div("col-md-6 float-start text-start")
							.attr("id", "pagnation"))
					.appendChild(Html.div("col-md-6 float-start text-end")
							.attr("id", "pagesize")));
			
			tableholder.add(Html.div("row", "mb-3").appendChild(
					Html.div("col-md-12 float-start text-start")
						.attr("id", "objectActions")));
			
			generateTableActions(tableholder.select("#objectActions").first(), generateActions(template.getCollectionKey()));
			
			if(readOnly) {
				tableholder.select(".readWrite").remove();
			}
			
			return tableholder;
		
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
	}
	
	private Collection<TableAction> generateActions(String resourceKey) {
		var t = templateService.getTableActions(resourceKey);
		if(Objects.nonNull(t)) {
			return t;
		}
		return Collections.emptySet();
	}

	private Map<String, DynamicColumn> generateDynamicColumns() {
		Map<String, DynamicColumn> results = new HashMap<>();
		for(DynamicColumn column : view.otherColumns()) {
			results.put(column.resourceKey(), column);
		}
		return results;
	}
	
	private void renderRowActions(Element row, AbstractObject obj, TableView view, ObjectTemplate template, Collection<TableAction> allActions) {
		
		Element el = Html.td("text-end");
		
		boolean canUpdate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canUpdate(template);
		boolean canCreate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canCreate(template);
		
		IconWithDropdownInput dropdown = new IconWithDropdownInput("options", "default");
		dropdown.icon("fa-ellipsis");
		el.appendChild(dropdown.renderInput());
		
		if(canUpdate && !readOnly) {
			if(Objects.isNull(parentObject)) {
				dropdown.addI18nAnchorWithIconValue("default", "edit.name", replaceVariables("/app/ui/update/{resourceKey}/{uuid}", obj), "fa-solid", "fa-edit");
			} else {
				dropdown.addI18nAnchorWithIconValue("default", "edit.name", replaceVariables("/app/ui/update/{resourceKey}/{uuid}", obj), "fa-solid", "fa-edit", "stash")
					.attr("data-action", replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
					.attr("data-url", replaceVariables("/app/ui/object-update/{resourceKey}/{uuid}", parentObject) + "/" + field.getResourceKey() + "/" + obj.getUuid());	
			}
		}
		
		if(view.requiresView()) {
			if(Objects.isNull(parentObject)) {
				dropdown.addI18nAnchorWithIconValue("default", "view.name", replaceVariables("/app/ui/view/{resourceKey}/{uuid}", obj), "fa-solid", "fa-eye");
			} else {
				dropdown.addI18nAnchorWithIconValue("default", "view.name", replaceVariables("/app/ui/object-view/{resourceKey}/{uuid}", parentObject)
						+ "/" + field.getResourceKey() + "/" + obj.getUuid(), "fa-solid", "fa-eye");
			}
		}
				
		if(canCreate && !readOnly) {
			dropdown.addI18nAnchorWithIconValue("default", "copy.name", replaceVariables("/app/api/objects/{resourceKey}/copy/{uuid}", obj), "fa-solid", "fa-copy");
		} 
		
		for(TableAction action : allActions) {
			if(action.target()==Target.ROW) {
				
				if(action.permissions().length > 0) {
					try {
					permissionService.assertAnyPermission(action.permissions());
					} catch(AccessDeniedException e) {
						continue;
					}
				}
				
				Object val = obj.getValue(template.getDefaultColumn());
				if(action.confirmationRequired()) {
					if(StringUtils.isNotBlank(template.getDefaultColumn())) {
						dropdown.addI18nAnchorWithIconValue(action.bundle(), action.resourceKey() + ".name", "#", action.iconGroup(), action.icon(), "deleteAction")
							.attr("data-name", val == null ? "" : val.toString())
							.attr("data-url", replaceVariables(action.url(), obj))
							.attr("target", action.window() == Window.BLANK ? "_blank" : "_self");
					} else {
						dropdown.addI18nAnchorWithIconValue(action.bundle(), action.resourceKey() + ".name", "#", action.iconGroup(), action.icon(), "deleteAction")
							.attr("data-name", obj.getUuid())
							.attr("data-url", replaceVariables(action.url(), obj))
							.attr("target", action.window() == Window.BLANK ? "_blank" : "_self");
					}
				} else {
					dropdown.addI18nAnchorWithIconValue(action.bundle(), action.resourceKey() + ".name", replaceVariables(action.url(), obj), action.iconGroup(), action.icon())
						.attr("target", action.window() == Window.BLANK ? "_blank" : "_self");
				}
			} 
		}
		
		if(template.isDeletable()) {
			if(!obj.isSystem() && !readOnly) {
				if(Objects.nonNull(parentObject)) {
					dropdown.addI18nAnchorWithIconValue("default", "delete.name", "#", "fa-solid", "fa-trash", "removeAction", "readWrite")
							.attr("data-name", checkNull(obj.getValue(template.getDefaultColumn())));
				} else {
					dropdown.addI18nAnchorWithIconValue("default", "delete.name", "#", "fa-solid", "fa-trash", "deleteAction", "readWrite")
						.attr("data-name", checkNull(obj.getValue(template.getDefaultColumn())))
						.attr("data-url", replaceVariables("/app/api/objects/{resourceKey}/{uuid}", obj));
				}
			} 
		} 
		
		if(el.children().size() > 0) {
			row.appendChild(el);
		}
		
	}
	
	private void generateTableActions(Element element, Collection<TableAction> allActions) {
		
		if(uiService.canCreate(template)) {
			
			if(!template.getChildTemplates().isEmpty()) {
				
				List<ObjectTemplate> creatableTemplates = new ArrayList<>();
				
				ObjectTemplate collectionTemplate = templateRepository.get(template.getCollectionKey());
				
				for(String template : collectionTemplate.getChildTemplates()) {
					ObjectTemplate childTemplate = templateRepository.get(template);
					if(childTemplate.isCreatable()) {
						creatableTemplates.add(childTemplate);
					}
				}
			
				if(creatableTemplates.size() > 1) {
					createMultipleOptionAction(element, "create", creatableTemplates, template.getCollectionKey());
				} else if(creatableTemplates.size() == 1) {
					
					ObjectTemplate singleTemplate = creatableTemplates.get(0);
					if(Objects.isNull(parentObject)) {
						createTableAction(element, String.format("create/%s", singleTemplate.getResourceKey()), 
								template.getCollectionKey(), "fa-plus", "fa-solid",
								"primary", "create", "readWrite");					
					} else {

						createStashAction(element, String.format("create/%s", singleTemplate.getResourceKey()), 
								template.getCollectionKey(), "fa-plus", "fa-solid",
								"create", "readWrite");					
					}
				}
			} else {
				
				if(Objects.isNull(parentObject)) {
					createTableAction(element, String.format("create/%s", template.getResourceKey()), 
						template.getCollectionKey(), "fa-plus", "fa-solid",
						"primary", "create", "readWrite");
				} else {
					createStashAction(element, String.format("create/%s", template.getResourceKey()), 
							template.getCollectionKey(), "fa-plus", "fa-solid",
							"primary", "create", "readWrite");	
				}
			}
		}

		if(view != null) {
			if(Objects.nonNull(allActions)) {
				for(TableAction action : allActions) {
					if(action.target()==Target.TABLE) {
						createTableAction(element, action.url(), action.bundle(), action.icon(), action.iconGroup(), action.buttonClass(), action.resourceKey());
					}
				}
			}
		}
		
	}
	
	private void createTableAction(Element element, String url, String bundle, String icon, String iconGroup, String buttonClass, String resourceKey, String... additionalClasses) {
		
		List<String> classes = new ArrayList<>(Arrays.asList(additionalClasses));
		classes.add("btn"); 
		classes.add("btn-" + buttonClass);
		classes.add("me-3");
		element.appendChild(
				new Element("a").attr("href", String.format("/app/ui/%s", replaceParameters(url)))
				.addClass(StringUtils.join(classes, " "))
				.appendChild(Html.i(iconGroup, icon, "me-1"))
				.appendChild(new Element("span")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey))));
	}
	
	private void createStashAction(Element element, String url, String bundle, String icon, String iconGroup, String buttonClass, String resourceKey, String... additionalClasses) {
		
		List<String> classes = new ArrayList<>(Arrays.asList(additionalClasses));
		classes.add("stash");
		classes.add("btn"); 
		classes.add("btn-" + buttonClass);
		classes.add("me-3");
		element.appendChild(
				new Element("a").attr("href", "#")
				.attr("data-action", 
						//Objects.nonNull(stashURL) ? stashURL : 
						replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
				.attr("data-url",
						replaceVariables("/app/ui/object-" + resourceKey + "/{resourceKey}", parentObject) + "/" + field.getResourceKey() + "/" + field.getValidationValue(ValidationType.RESOURCE_KEY))
				.addClass(StringUtils.join(classes, " "))
				.appendChild(Html.i(iconGroup, icon, "me-1"))
				.appendChild(new Element("span")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey))));
	}
	
	private void createMultipleOptionAction(Element element, String id, Collection<ObjectTemplate> actions,
			String bundle) {
		
		Element menu;
		element.appendChild(
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
								.addClass("fa-solid fa-plus me-1"))
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
	
	private Object replaceParameters(String str) {
		return str.replace("${resourceKey}", template.getResourceKey());
	}

	
	private String checkNull(Object obj) {
		if(Objects.isNull(obj)) {
			return "";
		}
		return obj.toString();
	}

	private String replaceVariables(String url, AbstractObject obj) {
		for(String var : Utils.extractVariables(url)) {
			url = url.replace(String.format("{%s}", var), StringUtils.defaultString((String)obj.getValue(var)));
		}
		return url;
	}

	private Node renderElement(AbstractObject obj, ObjectTemplate template, FieldTemplate field) throws UnsupportedEncodingException {
		
		boolean isDefault = StringUtils.defaultString(template.getDefaultColumn()).equals(field.getResourceKey());
		boolean canUpdate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canUpdate(template);
		
		if(isDefault) {
			if(canUpdate && !readOnly) {
				if(Objects.isNull(parentObject)) {
					return Html.a(replaceVariables("/app/ui/update/{resourceKey}/{uuid}", obj), "underline").appendChild(processFieldValue(obj, template, field));
				} else {
					return Html.a("#", "underline", "stash")
							.attr("data-action", replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
							.attr("data-url", replaceVariables("/app/ui/object-update/{resourceKey}/{uuid}", parentObject) + "/" + this.field.getResourceKey() + "/" + obj.getUuid())
							.appendChild(processFieldValue(obj, template, field));
				}
			} else {
				if(Objects.isNull(parentObject)) {
					return Html.a(String.format("/app/ui/view/%s/%s", template.getCollectionKey(), obj.getUuid()) , "underline").appendChild(processFieldValue(obj, template, field));
				} else {
					return Html.a(replaceVariables("/app/ui/object-view/{resourceKey}/{uuid}", parentObject)  + "/" + this.field.getResourceKey() + "/" + obj.getUuid(), "underline").appendChild(processFieldValue(obj, template, field));
				}
				
			}
			
		}
		
		return processFieldValue(obj, template, field);
		
	}
	
	private Element processFieldValue(AbstractObject obj, ObjectTemplate template, FieldTemplate field) {
		switch(field.getFieldType()) {
		case BOOL:
			return Html.i("fa-solid", Boolean.parseBoolean(getStringValue(field, obj)) ? "text-success fa-check fa-fw" : "text-danger fa-times fa-fw");
		case OBJECT_REFERENCE: 
			AbstractObject value = getReferenceValue(field, obj);
			if(Objects.nonNull(value)) {
				if(value instanceof AbstractObject) {
					return Html.span(StringUtils.defaultIfEmpty((String)((AbstractObject)value).getValue("name"), "-"));
				} 
			}
			return Html.span("-");
		case TEXT:
		case ENUM:
		{
			return renderText(field, obj, template);
		}
		case DATE:
			Date date = (Date) obj.getValue(field);
			if(Objects.nonNull(date)) {
				return  Html.span(Utils.formatDate(date));  
			} else {
				return Html.span("-");
			}
		case COUNTRY:
			String code = getStringValue(field, obj);
			if(StringUtils.isNotBlank(code)) {
				return Html.span(internationalService.getCountryName(code));
			} else {
				return Html.span("-");
			}
		default:
			return Html.span(StringUtils.defaultString(getStringValue(field, obj)));
		}
	}
	
	String getStringValue(FieldTemplate field, AbstractObject rootObject) {

		if(StringUtils.isNotBlank(field.getParentKey())) {
			AbstractObject obj = rootObject.getChild(field.getParentKey());
			if(Objects.nonNull(obj)) {
				return safeCast(obj.getValue(field.getResourceKey()));
			} 
			return "";
		} else {
			return safeCast(rootObject.getValue(field.getResourceKey()));
		}
		
	}
	
	AbstractObject getReferenceValue(FieldTemplate field, AbstractObject rootObject) {

		AbstractObject obj = null;
		if(StringUtils.isNotBlank(field.getParentKey())) {
			obj = rootObject.getChild(field.getParentKey());
			if(Objects.nonNull(obj)) {
				obj = obj.getChild(field.getResourceKey());
			} 
		} else {
			obj = rootObject.getChild(field.getResourceKey());
		}
		
		return obj;
		
	}
	
	Object getObjectValue(FieldTemplate field, AbstractObject rootObject) {

		if(StringUtils.isNotBlank(field.getParentKey())) {
			AbstractObject obj = rootObject.getChild(field.getParentKey());
			if(Objects.nonNull(obj)) {
				return obj.getValue(field.getResourceKey());
			} 
			return null;
		} else {
			return rootObject.getValue(field.getResourceKey());
		}
		
	}

	private String safeCast(Object value) {
		if(Objects.isNull(value)) {
			return null;
		}
		return value.toString();
	}

	private Element renderText(FieldTemplate field, AbstractObject obj, ObjectTemplate template) {
		FieldRenderer renderer = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).getRenderer(field, template);
		switch(renderer) {
		case BOOTSTRAP_BADGE:
		{
			return BootstrapBadgeRender.generateBadge(Utils.checkNullToString(getStringValue(field, obj)));
		}
		case I18N:
		{
			return Html.i18n(template.getBundle(), Utils.checkNullToString(getStringValue(field, obj)));
		}
		default:
		{
			return Html.span(Utils.checkNullToString(getStringValue(field, obj)), "UTF-8");
		}
		}
	}
	
	public void setStart(int start) {
		this.start = start;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setTotalObjects(long totalObjects) {
		this.totalObjects = totalObjects;
	}

	public void setObjects(Collection<AbstractObject> objects) {
		this.objects = objects;
	}

	public void setTemplate(ObjectTemplate template) {
		this.template = template;
	}

	public void setTemplateClazz(Class<?> templateClazz) {
		this.templateClazz = templateClazz;
	}

	public TableView getView() {
		return view;
	}

	public void setStashURL(String stashURL) {
		this.stashURL = stashURL;
	}
}
