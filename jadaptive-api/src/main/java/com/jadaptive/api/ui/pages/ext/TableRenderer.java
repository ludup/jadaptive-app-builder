package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.CreateURL;
import com.jadaptive.api.template.DynamicColumn;
import com.jadaptive.api.template.DynamicColumnService;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.SortOrder;
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

@TableView(defaultColumns = { "uuid" })
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
	
	@Autowired
	private I18nService i18nService;
	
	@Autowired
	private ApplicationService appService;
	
	private int start;
	private int length;
	
	private long totalObjects;
	private Collection<AbstractObject> objects;
	private String sortColumn;
	private SortOrder sortOrder;
	
	private ObjectTemplate template;
	private Class<?> templateClazz;
	
	private TableView view;
	private AbstractObject parentObject = null;
	private FieldTemplate field;
	private boolean readOnly;

	private RenderScope formRenderer;
	private String formHandler;
	private String stashURL = null;
	
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
			view = templateClazz.getAnnotation(TableView.class);
	
			if(Objects.isNull(view)) {
				view = getClass().getAnnotation(TableView.class);
			}
			
			Map<String,DynamicColumn> dynamicColumns = generateDynamicColumns();
			Map<String,ObjectTemplate> columns = new LinkedHashMap<>();
			Collection<TableAction> tableActions = generateActions(template.getCollectionKey());
			boolean hasMultipleSelection = checkMultipleSelectionActions(tableActions) || view.multipleDelete();
			
			
			if(hasMultipleSelection && totalObjects > 0) {
				Element ae;
				tableholder.add(Html.div("row")
						.appendChild(ae = Html.div("col-12")));
			
				if(view.multipleDelete()) {
					
					try {
						permissionService.assertWrite(template.getResourceKey());
						
						ae.appendChild(Html.a("#")
								.attr("data-url", "/app/api/objects/" + template.getResourceKey() + "/delete")
								.addClass("btn btn-primary selectionAction")
								.appendChild(Html.i("fa-solid", "fa-trash", "me-2"))
								.appendChild(Html.i18n("userInterface","multipleDelete.text")));
					
					} catch(AccessDeniedException e) { }
				}
				
				for(TableAction action : tableActions) {
					if(action.target() == Target.SELECTION) {

						if(action.permissions().length > 0) {
							try {
								if(action.matchAllPermissions()) {
									permissionService.assertAllPermission(action.permissions());
								} else {
									permissionService.assertAnyPermission(action.permissions());
								}
								
								ae.appendChild(Html.a("#")
										.attr("data-url", action.url())
										.addClass("btn btn-primary selectionAction")
										.appendChild(Html.i(action.iconGroup(), action.icon(), "me-2"))
										.appendChild(Html.i18n(action.bundle(), action.resourceKey() + ".name")));
								
							} catch(AccessDeniedException e) {
							}
						}
						
				
					}
				}
			}
			
			Element el;
			if(totalObjects > 0) {
				Element table = Html.table("table").attr("data-toggle", "table");
				tableholder.add(table);
				
				table.appendChild(Html.thead().appendChild(el = Html.tr()));
				
				if(hasMultipleSelection) {
					el.appendChild(Html.td());
				}
				
				ObjectTemplate tmp = template;
				while(tmp.hasParent()) {
					ObjectTemplate t = templateService.get(tmp.getParentTemplate());
					TableView v = templateService.getTemplateClass(tmp.getParentTemplate()).getAnnotation(TableView.class);
					if(Objects.nonNull(v)) {
						renderTableColumns(v, el, t, columns, dynamicColumns);
					}
					tmp = t;
				}
				
				renderTableColumns(view, el, template, columns, dynamicColumns);
				
				for(String childTemplate : template.getChildTemplates()) {
					ObjectTemplate t = templateService.get(childTemplate);
					Class<?> clz = templateService.getTemplateClass(childTemplate);
					if(Objects.nonNull(clz)) {
						TableView v = clz.getAnnotation(TableView.class);
						if(Objects.nonNull(v)) {
							renderTableColumns(v, el, t, columns, dynamicColumns);
						}
					}
				}
				// Actions
				el.appendChild(Html.td());
	
				
				table.appendChild(el = Html.tbody());
				
				ObjectMapper json = new ObjectMapper();
				
				if(objects.size() > 0) {
					for(AbstractObject obj : objects) {
						
						ObjectTemplate rowTemplate = template;
						if(!obj.getResourceKey().equals(template.getResourceKey())) {
							rowTemplate = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(obj.getResourceKey());
						}
						Element row = Html.tr();
						
						if(hasMultipleSelection) {
							row.appendChild(Html.td().appendChild(Html.input("checkbox", "selectedUUID", obj.getUuid())));
						}
						
						if(Objects.nonNull(parentObject)) {
							String c = json.writeValueAsString(obj);
							row.appendChild(new Element("input").attr("type", "hidden")
								.attr("name", this.field.getResourceKey())
								.val(Base64.getUrlEncoder().encodeToString(c.getBytes("UTF-8"))));
						}
						
						
						
						for(String column : columns.keySet()) {
							if(dynamicColumns.containsKey(column)) {
								DynamicColumn dc = dynamicColumns.get(column);
								DynamicColumnService service = ApplicationServiceImpl.getInstance().getBean(dc.service());
								Element col = service.renderColumn(column, obj, rowTemplate);
								row.appendChild(Html.td().appendChild(col == null ? Html.span("") : col));
							} else {
								FieldTemplate t = columns.get(column).getField(column);
								if(t == null) {
									row.appendChild(Html.td().appendChild(Html.span("<missing column: " + column + ">")));
								}
								else {
									row.appendChild(Html.td().appendChild(renderElement(obj, rowTemplate, t)));
								}
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
			}
			
			tableholder.add(Html.div("row", "mb-3").appendChild(
						Html.div("col-md-9 float-start text-start")
							.attr("id", "pagnation"))
					.appendChild(Html.div("col-md-3 float-start text-end")
							.attr("id", "pagesize")));
			
			tableholder.add(Html.div("row", "mb-3").appendChild(
					Html.div("col-md-12 float-start text-start")
						.attr("id", "objectActions")));
			
			generateTableActions(tableholder.select("#objectActions").first(), tableActions);
			
			if(readOnly) {
				tableholder.select(".readWrite").remove();
			}
			
			return tableholder;
		
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
	}
	
	private boolean checkMultipleSelectionActions(Collection<TableAction> tableActions) {
		for(TableAction action : tableActions) {
			if(action.target() == Target.SELECTION) {
				if(action.permissions().length > 0) {
					try {
						if(action.matchAllPermissions()) {
							permissionService.assertAllPermission(action.permissions());
						} else {
							permissionService.assertAnyPermission(action.permissions());
						}
					} catch(AccessDeniedException e) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private void renderTableColumns(TableView view, Element el, ObjectTemplate template, 
			Map<String,ObjectTemplate> columns, Map<String,DynamicColumn> dynamicColumns) {
		
		for(String column : view.defaultColumns()) {
			
			boolean isSortedBy = column.equals(sortColumn);
			
			DynamicColumn dyn = dynamicColumns.get(column);
			if(Objects.nonNull(dyn)) {
				//if(StringUtils.isBlank(dyn.sortColumn())) {
					el.appendChild(Html.td().appendChild(
							Html.i18n(template.getBundle(),String.format("%s.name", column))));
					columns.put(column, template);
					continue;
				//}
				// Too late to change the search
				// sortColumn = dyn.sortColumn();
			}
			
			FieldTemplate t = template.getField(column);
			
			Element e;
			if(Objects.nonNull(t)) {
				el.appendChild(Html.td().appendChild(
						e = Html.a("#")
							.addClass("sortColumn text-decoration-none")
							.attr("data-column", column)
							.appendChild(Html.i18n(template.getBundle(),String.format("%s.name", t.getResourceKey())))));
			} else {
				el.appendChild(Html.td().appendChild(
						e = Html.a("#")
							.addClass("sortColumn text-decoration-none")
							.attr("data-column", column)
							.appendChild(Html.i18n(template.getBundle(),String.format("%s.name", column)))));
			}
			
			if(isSortedBy) {
				switch(sortOrder) {
				case ASC:
					e.appendChild(Html.i("fa-solid", "fa-caret-down", "ms-1"));
					break;
				default:
					e.appendChild(Html.i("fa-solid", "fa-caret-up", "ms-1"));
					break;
				}
			}
			
			columns.put(column, template);
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
		el.appendChild(dropdown.renderInput().addClass("mb-3"));
		
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
				
				try {
					if(!appService.autowire(action.filter().getConstructor().newInstance()).showAction(obj)) {
						continue;
					}
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				}
				
				Object val = obj.getValue(template.getDefaultColumn());
				if(action.confirmationRequired()) {
					Element iel;
					if(StringUtils.isNotBlank(template.getDefaultColumn())) {
						iel = dropdown.addI18nAnchorWithIconValue(action.bundle(), action.resourceKey() + ".name", "#", action.iconGroup(), action.icon(), action.deleteAction() ? "deleteAction" : "confirmAction")
							.attr("data-name", val == null ? "" : val.toString())
							.attr("data-url", replaceVariables(action.url(), obj))
							.attr("target", action.window() == Window.BLANK ? "_blank" : "_self");
					} else {
						iel = dropdown.addI18nAnchorWithIconValue(action.bundle(), action.resourceKey() + ".name", "#", action.iconGroup(), action.icon(), action.deleteAction() ? "deleteAction" : "confirmAction")
							.attr("data-name", obj.getUuid())
							.attr("data-url", replaceVariables(action.url(), obj))
							.attr("target", action.window() == Window.BLANK ? "_blank" : "_self");
					}
					if(StringUtils.isNotBlank(action.confirmationBundle())) {
						var vargs = Arrays.asList(action.confirmationArgs()).stream().map(a -> replaceVariables(a, obj)).toArray();
						if(StringUtils.isNotBlank(action.confirmationKey()))
							iel.dataset().put("confirm-text", i18nService.format(action.confirmationBundle(), Locale.getDefault(), action.confirmationKey(), vargs));
						else
							iel.dataset().put("confirm-text", i18nService.format(action.confirmationBundle(), Locale.getDefault(), action.resourceKey() + ".confirm", vargs));
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
				createMultipleCreate(element, template);
			} else {
				createSingleCreate(element, template);
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
	
	private void createSingleCreate(Element element, ObjectTemplate t) {
		
		if(Objects.isNull(parentObject)) {
			
			Class<?> clz = templateService.getTemplateClass(t.getResourceKey());
			CreateURL[] urls = clz.getAnnotationsByType(CreateURL.class);
			if(Objects.nonNull(urls) && urls.length > 0) {
				for(CreateURL url : urls) {
					createTableAction(element, url.value(), 
							StringUtils.defaultIfEmpty(url.i18n(), template.getCollectionKey()), "fa-plus", "fa-solid",
							"primary", "create", "readWrite");
				}
			} else {
				createTableAction(element, String.format("/app/ui/create/%s", t.getResourceKey()), 
						template.getCollectionKey(), "fa-plus", "fa-solid",
						"primary", "create", "readWrite");
			}
			
			
		} else {
			createStashAction(element, 
					template.getCollectionKey(), "fa-plus", "fa-solid",
					"primary", "create", "readWrite");	
		}
	}

	private void createMultipleCreate(Element element, ObjectTemplate t) {
		
		List<ObjectTemplate> creatableTemplates = new ArrayList<>();
		
		ObjectTemplate collectionTemplate = templateRepository.get(t.getCollectionKey());
		
		for(String template : collectionTemplate.getChildTemplates()) {
			ObjectTemplate childTemplate = templateRepository.get(template);
			if(childTemplate.isCreatable()) {
				creatableTemplates.add(childTemplate);
			}
		}
	
		if(creatableTemplates.size() > 1) {
			createMultipleOptionAction(element, creatableTemplates, template.getCollectionKey());
		} else if(creatableTemplates.size() == 1) {
			ObjectTemplate singleTemplate = creatableTemplates.get(0);
			createSingleCreate(element, singleTemplate);
		}
	}

	private void createTableAction(Element element, String url, String bundle, String icon, String iconGroup, String buttonClass, String resourceKey, String... additionalClasses) {
		
		if(!url.startsWith("/")) {
			url = "/app/ui/" + url;
		}
		
		List<String> classes = new ArrayList<>(Arrays.asList(additionalClasses));
		classes.add("btn"); 
		classes.add("btn-" + buttonClass);
		classes.add("me-3");
		element.appendChild(
				new Element("a").attr("href", replaceParameters(url))
				.addClass(StringUtils.join(classes, " "))
				.appendChild(Html.i(iconGroup, icon, "me-1"))
				.appendChild(new Element("span")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey))));
	}
	
	private void createStashAction(Element element, String bundle, String icon, String iconGroup, String buttonClass, String resourceKey, String... additionalClasses) {
		
		List<String> classes = new ArrayList<>(Arrays.asList(additionalClasses));
		classes.add("stash");
		classes.add("btn"); 
		classes.add("btn-" + buttonClass);
		classes.add("me-3");
		element.appendChild(
				new Element("a").attr("href", "#")
				.attr("data-action", 
						replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
				.attr("data-url",
						replaceVariables("/app/ui/object-" + resourceKey + "/{resourceKey}", parentObject) + "/" + field.getResourceKey() + "/" + field.getValidationValue(ValidationType.RESOURCE_KEY))
				.addClass(StringUtils.join(classes, " "))
				.appendChild(Html.i(iconGroup, icon, "me-1"))
				.appendChild(new Element("span")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey))));
	}
	
	private void createMultipleOptionAction(Element element,
			Collection<ObjectTemplate> actions, String bundle) {
		
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
							.attr("id", "create")
							.appendChild(new Element("i")
								.addClass("fa-solid fa-plus me-1"))
							.appendChild(new Element("span")
								.attr("jad:bundle", bundle)
								.attr("jad:i18n", String.format("%s.name", "create"))))
					.appendChild(menu = new Element("div")
							.addClass("dropdown-menu")
							.attr("aria-labelledby", "create")));
		
		for(ObjectTemplate action : actions) {
			if(Objects.nonNull(parentObject)) {

				menu.appendChild(new Element("a")
						.addClass("dropdown-item stash")
						.attr("href", "#")
						.attr("jad:bundle", action.getBundle())
						.attr("data-action", 
								replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
						.attr("data-url",
								replaceVariables("/app/ui/object-create/{resourceKey}", parentObject) 
								+ "/" + field.getResourceKey() + "/" + action.getResourceKey())
						.attr("jad:i18n", String.format("%s.name", action.getResourceKey())));
				
			} else {
				
				Class<?> clz = templateService.getTemplateClass(action.getResourceKey());
				if(Objects.nonNull(clz)) {
					CreateURL[] urls = clz.getAnnotationsByType(CreateURL.class);
					if(Objects.nonNull(urls) && urls.length > 0) {
						for(CreateURL url : urls) {
							menu.appendChild(new Element("a")
									.addClass("dropdown-item")
									.attr("href", url.value())
									.attr("jad:bundle", action.getBundle())
									.attr("jad:i18n", String.format("%s.name", url.i18n())));
						}
					} else {
						menu.appendChild(new Element("a")
								.addClass("dropdown-item")
								.attr("href",  String.format("/app/ui/create/%s", action.getResourceKey()))
								.attr("jad:bundle", action.getBundle())
								.attr("jad:i18n", String.format("%s.name", action.getResourceKey())));
					}
				}
			}
		}
	}
	
	private String replaceParameters(String str) {
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
//
//		if(StringUtils.isNotBlank(field.getParentKey()) && !rootObject.getResourceKey().equals(field.getParentKey())) {
//			AbstractObject obj = rootObject.getChild(field.getParentField());
//			if(Objects.nonNull(obj)) {
//				return safeCast(obj.getValue(field.getResourceKey()));
//			} 
//			return "";
//		} else {
			return safeCast(rootObject.getValue(field.getResourceKey()));
//		}
		
	}
	
	AbstractObject getReferenceValue(FieldTemplate field, AbstractObject rootObject) {

		AbstractObject obj = null;
		if(StringUtils.isNotBlank(field.getParentKey()) && !rootObject.getResourceKey().equals(field.getParentKey())) {
			obj = rootObject.getChild(field.getParentField());
			if(Objects.nonNull(obj)) {
				obj = obj.getChild(field.getResourceKey());
			} 
		} else {
			obj = rootObject.getChild(field.getResourceKey());
		}
		
		return obj;
		
	}
	
	Object getObjectValue(FieldTemplate field, AbstractObject rootObject) {

		if(StringUtils.isNotBlank(field.getParentKey()) && !rootObject.getResourceKey().equals(field.getParentKey())) {
			AbstractObject obj = rootObject.getChild(field.getParentField());
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

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	
}
