package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.app.SecurityScope;
import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.ExtensionRegistration;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.ViewType;
import com.jadaptive.api.templates.ObjectDynamicField;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;
import com.jadaptive.api.ui.renderers.ReplacementDropdown;
import com.jadaptive.api.ui.renderers.Widget;
import com.jadaptive.api.ui.renderers.form.BooleanFormInput;
import com.jadaptive.api.ui.renderers.form.BootstrapBadgeRender;
import com.jadaptive.api.ui.renderers.form.CollectionSearchFormInput;
import com.jadaptive.api.ui.renderers.form.CollectionTextFormInput;
import com.jadaptive.api.ui.renderers.form.CssEditorFormInput;
import com.jadaptive.api.ui.renderers.form.DateFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownMenu;
import com.jadaptive.api.ui.renderers.form.FieldSearchFormInput;
import com.jadaptive.api.ui.renderers.form.HtmlEditorFormInput;
import com.jadaptive.api.ui.renderers.form.ImageFormInput;
import com.jadaptive.api.ui.renderers.form.JavascriptEditorFormInput;
import com.jadaptive.api.ui.renderers.form.MarkdownEditorInput;
import com.jadaptive.api.ui.renderers.form.MultipleAttachmentInput;
import com.jadaptive.api.ui.renderers.form.MultipleSelectionFormInput;
import com.jadaptive.api.ui.renderers.form.MultipleTagsFormInput;
import com.jadaptive.api.ui.renderers.form.NumberFormInput;
import com.jadaptive.api.ui.renderers.form.OptionsFormInput;
import com.jadaptive.api.ui.renderers.form.PasswordFormInput;
import com.jadaptive.api.ui.renderers.form.RadioFormInput;
import com.jadaptive.api.ui.renderers.form.RichTextEditorInput;
import com.jadaptive.api.ui.renderers.form.SetPasswordFormInput;
import com.jadaptive.api.ui.renderers.form.SingleAttachmentInput;
import com.jadaptive.api.ui.renderers.form.SwitchFormInput;
import com.jadaptive.api.ui.renderers.form.TextAreaFormInput;
import com.jadaptive.api.ui.renderers.form.TextEditorInput;
import com.jadaptive.api.ui.renderers.form.TextFormInput;
import com.jadaptive.api.ui.renderers.form.TimeFormInput;
import com.jadaptive.api.ui.renderers.form.TimestampFormInput;
import com.jadaptive.utils.Utils;

public abstract class AbstractObjectRenderer extends AbstractPageExtension {

	static Logger log = LoggerFactory.getLogger(AbstractObjectRenderer.class);
	
	@Autowired
	private SecurityPropertyService propertyService; 
	
	@Autowired
	private ClassLoaderService classLoader;
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private I18nService i18nService; 
	
	@Autowired
	private EncryptionService encryptionService;
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private InternationalService internationalService; 
	
	@Autowired
	private App applicationService;
	
	@Autowired
	private TenantService tenantService; 

	protected ThreadLocal<Document> currentDocument = new ThreadLocal<>();
	protected ThreadLocal<ObjectTemplate> currentTemplate = new ThreadLocal<>();
	private ThreadLocal<Properties> securityProperties = new ThreadLocal<>();
	private ThreadLocal<Map<String,AbstractObject>> childObjects = new ThreadLocal<>();

	protected ThreadLocal<Boolean> disableViews = new ThreadLocal<>();
	protected ThreadLocal<Set<String>> ignoreResources = new ThreadLocal<>();
	protected ThreadLocal<List<String>> replacementVariables = new ThreadLocal<>();
	
	protected ThreadLocal<RenderScope> formRenderer = new ThreadLocal<>();
	protected ThreadLocal<String> formHandler = new ThreadLocal<>();
	protected ThreadLocal<Element> currentElement = new ThreadLocal<>(); 
	protected ThreadLocal<Page> currentPage = new ThreadLocal<>(); 
	
	protected Map<String,Class<? extends Widget>> fieldRenderers = new HashMap<>();
	
	public ObjectTemplate getCurrentTemplate() {
		return currentTemplate.get();
	}
	
	public Page getCurrentPage() {
		return currentPage.get();
	}
	
	protected void process(Document contents, Page page, ObjectTemplate template, AbstractObject object, FieldView scope) throws IOException {

		currentDocument.set(contents);
		currentTemplate.set(template);
		currentPage.set(page);
    	try {
    		securityProperties.set(propertyService.getOverrideProperties(
					SecurityScope.TENANT, 
					template.getResourceKey() + ".properties"));
			
				
			if(Objects.nonNull(object)) {
				Map<String,AbstractObject> children = new HashMap<>();
				children.put(object.getResourceKey(), object);
				extractChildObjects(object, children, template);
				childObjects.set(children);
			} else {
				object = objectService.createNew(template);
			}
			 
			
			Element row;
			Element form = getForm(contents);
			
			form.addClass("jadaptiveForm")
				.attr("method", "POST")
				.id("formFor" + page.getClass().getSimpleName())
				.attr("autocomplete", "off")
				.attr("data-resourcekey", template.getResourceKey())
				.attr("enctype", "multipart/form-data")
				.attr("action", getActionURL())
				.appendChild(new Element("input")
					.attr("type", "hidden")
					.attr("name", "uuid")
					.val(Objects.nonNull(object) ? object.getUuid() : ""))
				.appendChild(new Element("input")
						.attr("type", "hidden")
						.attr("name", "system")
						.val(Objects.nonNull(object) ? String.valueOf(object.isSystem()) : "false"))
				.appendChild(new Element("input")
						.attr("type", "hidden")
						.attr("name", "resourceKey")
						.val(Objects.nonNull(object) ? object.getResourceKey() : template.getResourceKey())
//				.appendChild(new Element("input")
//						.attr("type", "hidden")
//			 			.attr("name", "hidden")
//						.val(Objects.nonNull(object) ?  String.valueOf(object.isHidden()) : "false"))
				);
				
			Session.getOr().ifPresent(session -> {
				sessionUtils.setupFormCSRFToken(Request.get(), template, form);
			});
			
			sessionUtils.addContentSecurityPolicy(Request.response(), "form-action", "self");
			
			form.appendChild(row = new Element("div").addClass("row"));
			
			Boolean disable = disableViews.get();
			if(Objects.isNull(disable))
			{
				disable = Boolean.FALSE;
			}
			
			Collection<ExtensionRegistration> extensions = templateService.getTemplateExtensions(template);
			if(!extensions.isEmpty()) {
				
				ObjectTemplate parent = templateService.getBaseTemplate(template);
				
				IconWithDropdownInput dropdown = new IconWithDropdownInput("extendObject", "default").up().icon("fa-code");
				row.appendChild(Html.div("col-12 text-end")
						.appendChild(dropdown.renderInput().addClass("mb-3")));
				List<I18nOption> options = new ArrayList<>();
				for(ExtensionRegistration extension : extensions) {
					options.add(new I18nOption(extension.bundle(), 
							String.format("%s.name", extension.resourceKey()), 
							extension.resourceKey()));
				}
				dropdown.renderValues(options, "");
				dropdown.setName(parent.getBundle(), "extendWith.name");		
			}
			
			renderResources(contents, templateService.getTemplateClass(template.getResourceKey()));
			
			List<TemplateView> views = templateService.getViews(template, disable);
			if(Objects.isNull(views)) {
				views = new ArrayList<>();
				TemplateView v = new TemplateView("dynamic");
				for(FieldTemplate t : template.getFields()) {
					v.getFields().add(new TemplateViewField(null, v, t, null, false));
				}
				views.add(v);
			}
			createViews(views, row, object, scope);

		} catch (IOException e) {
			log.error("Error processing entity", e);
			throw new IOException(e.getMessage(), e);
		} finally {
			currentDocument.remove();
			currentTemplate.remove();
			currentPage.remove();
			securityProperties.remove();
			childObjects.remove();
		}
    }

	private Element getForm(Document contents) {
		Element form = contents.selectFirst("form");
		if(Objects.isNull(form)) {
			contents.selectFirst("body").appendChild(form = new Element("form"));
		}
		return form;
	}

	protected abstract String getActionURL();

	private void extractChildObjects(AbstractObject object, Map<String,AbstractObject> children, ObjectTemplate template) {

		for(FieldTemplate field : template.getFields()) {
			if(!field.getCollection()) {
				switch(field.getFieldType()) {
				case OBJECT_EMBEDDED:
					AbstractObject child = object.getChild(field);
					if(Objects.nonNull(child)) {
						children.put(field.getResourceKey(), child);
						ObjectTemplate childTemplate = templateService.get((String)child.getValue("resourceKey"));
						extractChildObjects(child, children, childTemplate);
					}
					break;
				default:
				}
			}
		}
	}

	private void createViews(List<TemplateView> views, Element element, AbstractObject obj, FieldView scope) throws IOException {
		
		int tabIndex = hasTabbedView(views);
		int acdIndex = hasAccordionView(views);
		
		boolean first = true;
		for(TemplateView view : views) {
			
			if(!view.isRoot() && view.getFields().isEmpty()) {
				continue;
			}
			
			Element viewElement = createViewElement(view, element, first && !view.isRoot());
			
			boolean firstField = true;
			Element lastRow = Html.div("row mb-3 fields");
			viewElement.appendChild(lastRow);
			
			int usedCols = 0;
			for(TemplateViewField fieldView : view.getFields()) {
				FieldTemplate field = fieldView.getField();

				if(field.getResourceKey().equals("lastLogin")) {
					System.out.println();
				}
				switch(field.getFieldType()) {
				default:
					
					Element f = Html.div("field");
					f.attr("id", String.format("%sField", field.getResourceKey()));
					lastRow.appendChild(f);
					int cols = Math.min(field.getMetaValueInt("cols", 12), 12);
					int size = Math.min(field.getMetaValueInt("size", 12), 12);
					boolean eor = true;
					
					if(size < 12) {
						f.addClass("col-md-" + size + " col-sm-12");
					} else if(cols < 12) {
						usedCols += cols;
						eor = usedCols >= 12;
						f.addClass("col-md-" + cols + " col-sm-12");
					}

					renderField(f, obj, fieldView, scope, view);
					
					//if(!field.isHidden()) {
					if(eor) {
						boolean visible = false;
						for(Element e : lastRow.select(".field")) {
							if(!e.hasClass("d-none")) {
								visible = true;
								break;
							}
						}
						if(!visible) {
							lastRow.addClass("d-none");
						}
						viewElement.appendChild(lastRow = Html.div("row mb-3 fields"));
						usedCols = 0;
					}
					//}
					break;
				}

				if(firstField) {
					var firstInput = viewElement.select("input").first();
					if(firstInput != null) {
						firstInput.attr("autofocus", "true");
					}
				}
				
				firstField = false;
			}
			
			if(lastRow.childNodeSize() == 0) {
				lastRow.remove();
			}
			
			lastRow.removeClass("mb-3");
			
			if(!view.getChildViews().isEmpty()) {
				createViews(view.getChildViews(), viewElement, obj, scope);
			}

			if(!view.isRoot()) {
				first = false;
			} else {
				if(tabIndex > -1) {
					createTabOutline(viewElement);
				}
				
				if(acdIndex > -1) {
					createAccordionOutline(viewElement, tabIndex < acdIndex);
				}
			}
		}
		
		checkTabVisibility(element);
	}
	
	private void checkTabVisibility(Element element) {
		
		for(Element tab : element.select(".tab-pane")) {
			String target = tab.attr("id");

			boolean visible = false;
			for(Element e : tab.select(".fields")) {
				if(!e.hasClass("d-none")) {
					visible = true;
					break;
				}
			}
			if(!visible) {
				tab.addClass("d-none");
				element.ownerDocument().selectFirst(String.format("a[href=\"#%s\"]" ,target)).addClass("d-none");
			}
			
		};
		
	}

	private void createTabOutline(Element element) {
		element.appendChild(new Element("ul").attr("class", "nav nav-tabs pt-4"));
		element.appendChild(new Element("div").attr("class", "tab-content panel col-12 py-4"));
	}
	
	private void createAccordionOutline(Element element, boolean append) {
		
		if(append) {
			element.appendChild(new Element("div")
					.attr("class", "row")
					.appendChild(new Element("div")
						.attr("class", "accordion col-12")
						.attr("id", String.format("%sAccordion", currentTemplate.get().getResourceKey()))));
		} else {
			element.prependChild(new Element("div")
					.attr("class", "row")
					.appendChild(new Element("div")
						.attr("class", "accordion col-12")
						.attr("id", String.format("%sAccordion", currentTemplate.get().getResourceKey()))));
		}
	}
	
	private int hasTabbedView(List<TemplateView> views) {
		int idx = 0;
		for(TemplateView v : views) {
			if(!v.isRoot() && v.getType()==ViewType.TAB) {
				return idx;
			}
			idx++;
		}
		return -1;
	}
	
	private int hasAccordionView(List<TemplateView> views) {
		int idx = 0;
		for(TemplateView v : views) {
			if(!v.isRoot() && v.getType()==ViewType.ACCORDION) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	private void renderField(Element element, AbstractObject obj, TemplateViewField fieldView,  FieldView scope, TemplateView panel) throws IOException {
		
		currentElement.set(element);
		
		try {
			FieldTemplate field = fieldView.getField();
			boolean decorate = field.getMetaValueBool("decorate", true);
			
			if(StringUtils.isNotBlank(field.getWidget())) {
				Widget render = lookupWidget(field.getWidget());
				if(Objects.nonNull(render)) {
					render.init(fieldView.getResourceKey(), fieldView.getFormVariable(), fieldView.getBundle());
					render.renderInput(element,getFieldValue(fieldView, obj), field.isReadOnly()); 
					return;
				} else {
					log.warn("Cannot find custom widget {} for field {}", field.getWidget(), field.getResourceKey());
				}
			} else if(field.getCollection()) {
				renderCollection(element, obj, fieldView, scope, panel); 
			} else {
				switch(field.getFieldType()) {
				case TEMPLATE_REFERENCE:
				{
					String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
					ObjectTemplate objectTemplate = templateService.get(objectType);
					String uuid = null;
					String name = null;
					if(Objects.nonNull(obj)) {
						AbstractObject ref = obj.getChild(fieldView.getField());
						if(Objects.nonNull(ref)) {
							uuid = ref.getUuid();
							name = (String) ref.getValue("name");
						}
					}
					
					AbstractObject ref = obj.getChild(field);
					if(fieldView.getRenderer() == FieldRenderer.DROPDOWN) {
						DropdownFormInput dropdown = new DropdownFormInput(fieldView);
						dropdown.renderInput(element, "", scope == FieldView.READ);
						for(String resourceKey : objectTemplate.getChildTemplates()) {
							dropdown.addI18nValue(resourceKey, resourceKey + ".name");
						}
						if(Objects.nonNull(ref)) {
							dropdown.setSelectedValue(ref.getUuid(), (String) ref.getValue("name"));
						}
					} else if(fieldView.getRenderer() == FieldRenderer.DROPDOWN_MENU) {
						DropdownMenu dropdown = new DropdownMenu(fieldView);
						dropdown.renderInput(element, "", scope == FieldView.READ);
						for(String resourceKey : objectTemplate.getChildTemplates()) {
							dropdown.addI18nValue(resourceKey, resourceKey + ".name");
						}
						if(Objects.nonNull(ref)) {
							dropdown.setSelectedValue(ref.getUuid(), (String) ref.getValue("name"));
						}
					} else {
						FieldSearchFormInput input = new FieldSearchFormInput(fieldView, 
								String.format("/app/api/templates/%s/table", objectType),
								"name",fieldView.getFormVariable(), "uuid");
						if(!decorate) {
							input.diableDecoration();
						}
						input.renderInput(element, uuid, name, false, scope == FieldView.READ);
					}
	
					break;
				}
				case OBJECT_REFERENCE:
				{
					String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
					ObjectTemplate objectTemplate = templateService.get(objectType);
					String uuid = null;
					String name = null;
					if(Objects.nonNull(obj)) {
						AbstractObject ref = obj.getChild(fieldView.getField());
						if(Objects.nonNull(ref)) {
							uuid = ref.getUuid();
							name = (String) ref.getValue("name");
						}
					}
					
					AbstractObject ref = obj.getChild(field);
					if(fieldView.getRenderer() == FieldRenderer.DROPDOWN) {
						DropdownFormInput dropdown = new DropdownFormInput(fieldView);
						if(!decorate) {
							dropdown.disableDecoration();
						}
						dropdown.renderInput(element, "", scope == FieldView.READ);
						if(scope!=FieldView.READ) {
							for(AbstractObject o : objectService.list(objectType)) {
								dropdown.addInputValue(o.getUuid(), (String) o.getValue(objectTemplate.getNameField()));
							}
						}
						if(Objects.nonNull(ref)) {
							dropdown.setSelectedValue(ref.getUuid(), (String) ref.getValue("name"));
						}
					} else if(fieldView.getRenderer() == FieldRenderer.DROPDOWN_MENU) {
						DropdownMenu dropdown = new DropdownMenu(fieldView);
						if(!decorate) {
							dropdown.disableDecoration();
						}
						dropdown.renderInput(element, "", scope == FieldView.READ);
						if(scope!=FieldView.READ) {
							for(AbstractObject o : objectService.list(objectType)) {
								dropdown.addInputValue(o.getUuid(), (String) o.getValue(objectTemplate.getNameField()));
							}
						}
						if(Objects.nonNull(ref)) {
							dropdown.setSelectedValue(ref.getUuid(), (String) ref.getValue("name"));
						}
					} else {
						String url = fieldView.getField().getMetaValue("url", String.format("/app/api/%s/%s/table", 
								currentTemplate.get().getScope() == ObjectScope.PERSONAL ? "personal" : "references",
								objectType));
						url = url.replace("{uuid}", Objects.nonNull(obj) && StringUtils.isNotBlank(obj.getUuid()) ? obj.getUuid() : "");
						FieldSearchFormInput input = new FieldSearchFormInput(fieldView, 
								url,
								"name", fieldView.getFormVariable(), "uuid");
						if(!decorate) {
							input.diableDecoration();
						}
						input.renderInput(element, uuid, name, false, scope == FieldView.READ);
					}
	
					break;
				}
				case OBJECT_EMBEDDED:
	//				renderFormField(template, element, Objects.nonNull(obj) ? obj.getChild(field) : null, field, properties, view);
					throw new IllegalStateException("Embedded object field should not be processed here");
				default:	
					renderFormField(element, obj, fieldView, scope, panel);
				}
				
				Elements thisElement = element.select("#" + field.getResourceKey());
				if(field.isRequired()) {
					thisElement.attr("required", "true");
				}
				if(field.isReadOnly() || scope == FieldView.READ) {
					thisElement.attr("readonly", "readonly");
				}
			}
			
			
			Elements thisElement = element.select("#" + field.getResourceKey());
			processDynamicElements(thisElement, fieldView, obj);
			
			if(!field.getViews().isEmpty()) {
				if(!field.getViews().contains(scope)) {
					if(scope != FieldView.READ) {
						/**
						 * Edit will need hidden fields 
						 * 
						 * TODO encrypt these
						 */
						element.addClass("d-none");
						return;
					}
				}
			}
			
			if(fieldView.isHidden()) {
				element.addClass("d-none");
			} if(!field.getViews().contains(scope)) {
				switch(scope) {
				case UPDATE:
					element.addClass("d-none");
					break;
				default:
					element.empty();
				}
			} else if(Objects.isNull(obj) && 
					field.isReadOnly() &&
					fieldView.getRenderer() == FieldRenderer.OPTIONAL) {
				element.addClass("d-none");
			}
			
			Set<String> ignores = ignoreResources.get();
			if(Objects.nonNull(ignores) && ignores.contains(field.getResourceKey())) {
				element.addClass("d-none");
			} else if(!tenantService.getCurrentTenant().isSystem() && fieldView.isSystemOnly()) {
				element.addClass("d-none");
			}
		} finally {
			currentElement.remove();
		}
	}
	
	private Widget lookupWidget(String widget) {
		
			try {
				Class<? extends Widget> cls = fieldRenderers.get(widget);
				if(Objects.isNull(cls)) {
					applicationService.getBeans(Widget.class).stream().filter(b -> b.getClass().getSimpleName().equals(widget)).findFirst().ifPresent(b -> {
						fieldRenderers.put(widget, b.getClass());
					});
				} 
				if(fieldRenderers.containsKey(widget)) {
					cls = fieldRenderers.get(widget);
					return (Widget) applicationService.autowire(cls.getConstructors()[0].newInstance());
				}
			} catch (Exception e) {
				log.error("Error loading custom widget class", e);
			}
		throw new IllegalStateException(String.format("Cannot find widget for %s", widget));
	}

	private void renderCollection(Element element, AbstractObject obj, TemplateViewField fieldView,
			FieldView view, TemplateView panel) throws IOException {
		
		FieldTemplate field = fieldView.getField();
		boolean decorate = fieldView.getField().getMetaValueBool("decorate", true);
		
		List<FieldTemplate> parents = fieldView.getParentFields();
		if(Objects.nonNull(parents)) {
			for(FieldTemplate parentField : parents) {
				obj = obj.getChild(parentField);
			}
		}
		
		switch(field.getFieldType()) {
		case BOOL:
			break;
		case DATE:
			break;
		case DECIMAL:
			break;
		case ENUM:
		{
			List<String> values = new ArrayList<>();
			if(Objects.nonNull(obj)) {
				for(String val : obj.getCollection(field.getResourceKey())) {
					values.add(val);
				}
			}
			
			if(values.isEmpty() && 
					field.isReadOnly() &&
					fieldView.getRenderer() == FieldRenderer.OPTIONAL) {
				return;
			}
			
			List<String> available = new ArrayList<>();

			String enumType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			try {
				Class<?> cls = classLoader.findClass(enumType);
				for(Object enumObj : cls.getEnumConstants()) {
					available.add(enumObj.toString());
				}

				MultipleSelectionFormInput render = new MultipleSelectionFormInput(currentTemplate.get(), fieldView);
				render.renderInput(panel, element, available, values, false);
			} catch (ClassNotFoundException e) {
				log.error("Cannot render enum as the class could not be found", e);
			}

			
			break;
		}
		case INTEGER:
		case LONG:
			break;
		case OBJECT_EMBEDDED:
		{
			
			String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			ObjectTemplate objectTemplate = templateService.get(objectType);
			
			TableRenderer table = applicationService.autowire(new TableRenderer(view == FieldView.READ, 
					obj, field,
					formRenderer.get(), formHandler.get(), objectTemplate));

			Collection<AbstractObject> objects;
			
			if(Objects.nonNull(obj)) {
				objects = obj.getObjectCollection(field.getResourceKey());
			} else {
				objects = new ArrayList<>();
			}
			
			if(objects.isEmpty() && 
					field.isReadOnly() &&
					fieldView.getRenderer() == FieldRenderer.OPTIONAL) {
				return;
			}
			
			table.setObjects(objects);
			table.setTemplateClazz(templateService.getTemplateClass(objectTemplate.getResourceKey()));
			table.setSortColumn(objectTemplate.getDefaultColumn());
			table.setSortOrder(SortOrder.ASC);
			
			element.appendChild(table.render());
			break;
		}
//		case FILE:
//		{
//			element.appendChild(Html.div()).attr("jad-id", "uploadWidget");
//		}
		case OBJECT_REFERENCE:
		{
			String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			ObjectTemplate objectTemplate = templateService.get(objectType);
			List<NamePairValue> values = new ArrayList<>();
			
			String filterTo = fieldView.getField().getMetaValue("filterTo", "");
			if(StringUtils.isNotBlank(filterTo)) {
				objectType = filterTo;
			}
			
			if(Objects.nonNull(obj)) {
				for(Object o : obj.getObjectCollection(field.getResourceKey())) {
					if(o instanceof String) {
						AbstractObject referencedObject = objectService.get(objectType, (String)o);
						values.add(new NamePairValue(referencedObject.getValue(objectTemplate.getNameField()).toString(), (String)o));
					} else if(o instanceof AbstractObject) {
						
						AbstractObject ref = (AbstractObject) o;
						if(StringUtils.isBlank(filterTo) || ref.getResourceKey().equals(filterTo)) {
							values.add(new NamePairValue((String)ref.getValue("name"), ref.getUuid()));
						}
					}
				}
			}
			
			if(values.isEmpty() && 
					field.isReadOnly() &&
					fieldView.getRenderer() == FieldRenderer.OPTIONAL) {
				return;
			}
			
			String url;
			if(StringUtils.isNotBlank(filterTo)) {
				url = fieldView.getField().getMetaValue("url", 
						String.format("/app/api/filteredReferences/%s/table", 
						objectType));
			} else {
				url = fieldView.getField().getMetaValue("url", String.format("/app/api/%s/%s/table", 
					currentTemplate.get().getScope() == ObjectScope.PERSONAL ? "personal" : "references",
					objectType));
			}
			url = url.replace("{uuid}", Objects.nonNull(obj) && StringUtils.isNotBlank(obj.getUuid()) ? obj.getUuid() : "");

			CollectionSearchFormInput render = new CollectionSearchFormInput(
					currentTemplate.get(), fieldView, 
					url,
					"name", "uuid");
			render.renderInput(element, values, false, 
					(view == FieldView.READ || fieldView.getField().isReadOnly()));
			break;
		}
		case ATTACHMENT:
		{
			MultipleAttachmentInput input = new MultipleAttachmentInput(fieldView);
			if(!decorate ) {
				input.disableDecoration();
			}
			input.renderInput(element, "", view == FieldView.READ);
			input.renderAttachments(obj.getObjectCollection(fieldView.getResourceKey()));
			break;
			
		}
		case PASSWORD:
			break;
		case OPTIONS:
			
			new OptionsFormInput(fieldView).renderInput(element, 
					obj.getObjectCollection(field.getResourceKey()),
					objectService.list(field.getValidationValue(ValidationType.RESOURCE_KEY)),
					field.getMetaValue("ignoreUUIDs", "").split(","));
			
			break;
		case COUNTRY:
		{
			List<NamePairValue> values = new ArrayList<>();
			if(Objects.nonNull(obj)) {
				for(String permission : obj.getCollection(field.getResourceKey())) {
					values.add(new NamePairValue(internationalService.getCountryName(permission), permission));
				}
			}
			
			CollectionSearchFormInput render = new CollectionSearchFormInput(
					currentTemplate.get(), fieldView, "/app/api/countries/table",
					"name", "code");
			render.renderInput(element, values, false, (view == FieldView.READ || fieldView.getField().isReadOnly()));
			
			break;
		}
		case TEXT:
		{
			switch(fieldView.getRenderer()) {
			case TAGS:
			{
				MultipleTagsFormInput render = new MultipleTagsFormInput(
						fieldView.getResourceKey(),
						fieldView.getBundle(), 
						fieldView.getFormVariable());
				render.renderInput(panel, element, Objects.nonNull(obj) ? 
						obj.getCollection(field.getResourceKey()) 
						: Collections.emptyList());
				break;
			}
 			case COLLECTION:
			{
				Collection<NamePairValue> values = 
						( Objects.nonNull(obj) ? obj.getCollection(field.getResourceKey())
						: Collections.emptyList() ).stream().map(o -> new NamePairValue(o.toString(), o.toString())).toList();
				
				if(values.isEmpty() && 
						field.isReadOnly() &&
						(fieldView.getRenderer() == FieldRenderer.OPTIONAL)) {
					// TODO Hidden encrypted
					return;
				}
				
				CollectionSearchFormInput render = new CollectionSearchFormInput(
						currentTemplate.get(), fieldView, field.getMeta(),
						"name", "value");
				render.renderInput(element, values, false, (view == FieldView.READ || fieldView.getField().isReadOnly()));

				break;
			}
			default:
			{
				
				Collection<String> values = Objects.nonNull(obj) ? obj.getCollection(field.getResourceKey())
						: Collections.emptyList();
				
				if(values.isEmpty() && 
						field.isReadOnly() &&
						(fieldView.getRenderer() == FieldRenderer.OPTIONAL)) {
					// TODO Hidden encrypted
					return;
				}
				
				if(fieldView.requiresDecryption()) {
					Collection<String> tmp = new ArrayList<>();
					for(String value : values) {
						tmp.add(encryptionService.decrypt(value));
					}
					values = tmp;
				}
				
				
				CollectionTextFormInput render = new CollectionTextFormInput(currentTemplate.get(), fieldView);
				render.renderInput(panel, element, 
						values, (view == FieldView.READ || fieldView.getField().isReadOnly()));
				
				List<String> replacementVars = replacementVariables.get();
				if(Objects.nonNull(replacementVars) && replacementVars.size() > 0) {
					ReplacementDropdown replacement = new ReplacementDropdown("replacements", "");
					render.getInputElement().before(replacement.renderInput());
					replacement.renderTemplateReplacements(replacementVars);
				} 
				break;
			}
			}
			

			break;
		}
		case TIME:
			break;
		case TEXT_AREA:
			break;
		case TIMESTAMP:
			break;
		case PERMISSION:
		{
			List<NamePairValue> values = new ArrayList<>();
			if(Objects.nonNull(obj)) {
				for(String permission : obj.getCollection(field.getResourceKey())) {
					values.add(new NamePairValue(permission, permission));
				}
			}
			
			CollectionSearchFormInput render = new CollectionSearchFormInput(
					currentTemplate.get(), fieldView, "/app/api/permissions/table",
					"name", "value");
			render.renderInput(element, values, false, (view == FieldView.READ || fieldView.getField().isReadOnly()));
			
			break;
		}
		default:
			break;
		
		}
	}
	
	private void renderFormField(Element element, AbstractObject obj, TemplateViewField fieldView,
			FieldView view, TemplateView panel) throws IOException {
		
		FieldTemplate field = fieldView.getField();
		boolean decorate = fieldView.getField().getMetaValueBool("decorate", true);
		
		switch(field.getFieldType()) {
		case ISO_CURRENCY:
		{
			DropdownFormInput dropdown = new DropdownFormInput(fieldView);
			dropdown.renderInput(element, "", view == FieldView.READ);
			if(!decorate) {
				dropdown.disableDecoration();
			}
			for(Currency currency : Currency.getAvailableCurrencies()) {
				dropdown.addInputValue(currency.getCurrencyCode(), currency.getCurrencyCode());
			}
			String code = getFieldValue(fieldView, obj);
			if(StringUtils.isNotBlank(code)) {
				dropdown.setSelectedValue(code, code);
			}
			break; 
		}
		case COUNTRY:
		{
			DropdownFormInput dropdown = new DropdownFormInput(fieldView);
			dropdown.renderInput(element, "", view == FieldView.READ);
			if(!decorate) {
				dropdown.disableDecoration();
			}
			for(Country country : internationalService.getCountries()) {
				dropdown.addInputValue(country.getCode(), country.getName());
			}
			String code = getFieldValue(fieldView, obj);
			if(StringUtils.isNotBlank(code)) {
				dropdown.setSelectedValue(code, internationalService.getCountryName(code));
			}
			break; 
		}
		case HTML:
			
			element.append(i18nService.format(currentTemplate.get().getBundle(), Locale.getDefault(), fieldView.getResourceKey() + ".html"));
			element.child(0).attr("id", fieldView.getFormVariable());
			break;
		case TEXT:
		{
			switch(fieldView.getRenderer()) {
			case BOOTSTRAP_BADGE:
			{
				BootstrapBadgeRender render = new BootstrapBadgeRender(fieldView);
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case I18N:
			{
				String i18nValue = i18nService.format(currentTemplate.get().getBundle(), Locale.getDefault(), getFieldValue(fieldView, obj));
				TextFormInput render = new TextFormInput(currentTemplate.get(), fieldView);
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, i18nValue, view == FieldView.READ);
				break;
			}
			case OPTIONAL:
			{
				String value = getFieldValue(fieldView, obj); 
				if(StringUtils.isNotBlank(value) || (!fieldView.getField().isReadOnly() && view !=FieldView.READ)) {
					TextFormInput render = new TextFormInput(currentTemplate.get(), fieldView);
					if(!decorate) {
						render.disableDecoration();
					}
					render.renderInput(element, value, view == FieldView.READ);
				}
				break;
			}
			case COLLECTION:
			{
				FieldSearchFormInput input = new FieldSearchFormInput(fieldView.getResourceKey(), 
						fieldView.getFormVariable(),
						fieldView.getBundle(),
						fieldView.getField().getMetaValue("url", ""),
						"name",
						"name",
						"value");
				if(!decorate) {
					input.diableDecoration();
				}
				input.renderInput(element, getFieldValue(fieldView, obj), 
						getFieldValue(fieldView, obj), false, view == FieldView.READ);
				break;
			}
			default:
			{
				List<String> replacementVars = replacementVariables.get();
				
				TextFormInput render = new TextFormInput(currentTemplate.get(), fieldView) {

					@Override
					public String getInputType() {
						switch(fieldView.getRenderer()) {
						case COLOR_CHOOSER:
							return "color";
						default:
							return super.getInputType();
						}
					}

					@Override
					protected String getAutocomplete() {
						if(fieldView.getRenderer() == FieldRenderer.SET_PASSWORD) {
							return "new-password";
						}
						else {
							return "off";
						}
					}
				};
				
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				
				if(Objects.nonNull(replacementVars) && replacementVars.size() > 0) {
					ReplacementDropdown replacement = new ReplacementDropdown("replacements", "");
					render.getInputElement().before(replacement.renderInput());
					replacement.renderTemplateReplacements(replacementVars);
				} 
				
				
				break;
			}
			}
			break;
		}
		case IMAGE:
		{
			String val = getFieldValue(fieldView, obj);
			ImageFormInput render = new ImageFormInput(currentTemplate.get(), fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, val, view == FieldView.READ);
			break;
		}
//		case FILE:
//		{
//			String value = getFieldValue(fieldView, obj);
//			FileFormInput render = new FileFormInput(currentTemplate.get(), fieldView,
//					documentService.getFileTypeFilename(value),
//					documentService.getFileTypeContentLength(value),
//					documentService.getFileTypeContentType(value));
//			render.renderInput(element, value);
//			break;
//		}
		case ATTACHMENT:
		{
			SingleAttachmentInput render = new SingleAttachmentInput(currentTemplate.get(),fieldView);
			AbstractObject att = obj.getChild(field);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, Objects.nonNull(att) ? att.getUuid() : "", view == FieldView.READ);
			break;
		}
		case TEXT_AREA:
		{
			switch(fieldView.getRenderer()) {
			case CSS_EDITOR:
			{
				CssEditorFormInput render = new CssEditorFormInput(fieldView, currentDocument.get(), view == FieldView.READ);
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case HTML_EDITOR:
			{
				HtmlEditorFormInput render = new HtmlEditorFormInput(fieldView, currentDocument.get(), view == FieldView.READ);
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case JAVA_EDITOR:
			{
				JavascriptEditorFormInput render = new JavascriptEditorFormInput(fieldView, currentDocument.get(), view == FieldView.READ);
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case TEXT_EDITOR:
			{
				TextEditorInput render = new TextEditorInput(fieldView, currentDocument.get());
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case RICH_EDITOR:
			{
				RichTextEditorInput render = new RichTextEditorInput(fieldView, currentDocument.get());
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case MARKDOWN_EDITOR:
			{
				MarkdownEditorInput render = new MarkdownEditorInput(fieldView, currentDocument.get());
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case HTML_VIEW:
			{
				/**
				 * This is required because Firefox does not support csp attribute so
				 * we have to set the parent page to the required policy.
				 */
				PageHelper.addContentSecurityPolicy("style-src", SessionUtils.UNSAFE_INLINE);
				element.appendChild(new Element("iframe")
						.addClass("w-100")
						.attr("height", "600")
						.attr("srcdoc", getFieldValue(fieldView, obj)));
				break;
			}
			case I18N:
			{
				String i18nValue = i18nService.format(panel.getBundle(), Locale.getDefault(), getFieldValue(fieldView, obj));
				TextAreaFormInput render = new TextAreaFormInput(fieldView, field.getMetaValueInt("rows", 15));
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, i18nValue, view == FieldView.READ);
				break;
			}
			case OPTIONAL:
			{
				String value = getFieldValue(fieldView, obj);
				if(StringUtils.isNotBlank(value) || view!=FieldView.READ) {
					TextAreaFormInput render = new TextAreaFormInput(fieldView, field.getMetaValueInt("rows", 15));
					if(!decorate) {
						render.disableDecoration();
					}
					render.renderInput(element, value, view == FieldView.READ);
				}
				break;
			}
			default:
			{
				TextAreaFormInput render = new TextAreaFormInput(fieldView, field.getMetaValueInt("rows", 10));
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				
				List<String> replacementVars = replacementVariables.get();
				if(Objects.nonNull(replacementVars) && replacementVars.size() > 0) {
					ReplacementDropdown replacement = new ReplacementDropdown("replacements", "");
					render.getInputElement().before(replacement.renderInput());
					replacement.renderTemplateReplacements(replacementVars);
				} 
				break;
			}
			}
			break;
		}
		case PASSWORD:
		{
			PasswordFormInput render = fieldView.getRenderer() == FieldRenderer.SET_PASSWORD ? 
					new SetPasswordFormInput(currentTemplate.get(), fieldView) :
					new PasswordFormInput(currentTemplate.get(), fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
			List<String> replacementVars = replacementVariables.get();
			if(Objects.nonNull(replacementVars) && replacementVars.size() > 0) {
				ReplacementDropdown replacement = new ReplacementDropdown("replacements", "");
				render.getInputElement().before(replacement.renderInput());
				replacement.renderTemplateReplacements(replacementVars);
			} 
			break;
		}
		case TIMESTAMP:
		{
			TimestampFormInput render = new TimestampFormInput(fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
			break;
		}
		case TIME:
		{
			TimeFormInput render = new TimeFormInput(currentTemplate.get(), fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
			break;
		}
		case DATE:
		{
			String dateValue = getFieldValue(fieldView, obj);
			if(fieldView.getRenderer()==FieldRenderer.OPTIONAL) {
				if(StringUtils.isBlank(dateValue)) {
					break;
				}
			}

			DateFormInput render = new DateFormInput(currentTemplate.get(), fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, dateValue, view == FieldView.READ);
			break;
			
		}
		case BOOL:
		{
			BooleanFormInput render;
			switch(fieldView.getRenderer()) {
			case CHECKBOX:
				render = new BooleanFormInput(fieldView);
				break;
			default:
				render = new SwitchFormInput(fieldView);
				break;
			}
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
			if(field.isReadOnly() || view == FieldView.READ) {
				render.disable();
			}
			break;
		}
		case PERMISSION:
		{
			DropdownFormInput render = new DropdownFormInput(fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
			render.renderValues(permissionService.getAllPermissions(), getFieldValue(fieldView, obj));
		
			break;
		}
		case ENUM:
		{
			switch(fieldView.getRenderer()) {
			case BOOTSTRAP_BADGE:
			{
				BootstrapBadgeRender render = new BootstrapBadgeRender(fieldView);
				if(!decorate) {
					render.disableDecoration();
				}
				render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
				break;
			}
			case RADIO_BUTTON:
			{

				Class<?> values;
				try {
					values = classLoader.findClass(field.getValidationValue(ValidationType.OBJECT_TYPE));
					RadioFormInput render = new RadioFormInput(fieldView);
					if(!decorate) {
						render.disableDecoration();
					}
					render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
					render.renderValues(filterEnums((Enum<?>[])values.getEnumConstants(), field), getFieldValue(fieldView, obj), view == FieldView.READ);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
				break;
			}
			default:
				Class<?> values;
				try {
					values = classLoader.findClass(field.getValidationValue(ValidationType.OBJECT_TYPE));
					DropdownFormInput render = new DropdownFormInput(fieldView);
					if(!decorate) {
						render.disableDecoration();
					}
					render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);
					render.renderValues(filterEnums((Enum<?>[])values.getEnumConstants(), field), getFieldValue(fieldView, obj), view == FieldView.READ);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			break;
		}
		case DECIMAL:
		case INTEGER:
		case LONG:
		{
			NumberFormInput render = new NumberFormInput(currentTemplate.get(), fieldView);
			if(!decorate) {
				render.disableDecoration();
			}
			render.renderInput(element, getFieldValue(fieldView, obj), view == FieldView.READ);

			break;
		}
		case OBJECT_EMBEDDED:
		case OBJECT_REFERENCE:
		case OPTIONS:
			throw new IllegalStateException("Object cannot be rendered by renderField");
		default:
			throw new IllegalStateException("Missing field type " + field.getFieldType().name());
		}
		
	}
	
	private Enum<?>[] filterEnums(Enum<?>[] enums, FieldTemplate fieldTemplate) {
		var str = fieldTemplate.getMetaValue("include", "");
		var includes = StringUtils.isBlank(str) ? Collections.emptyList() : Arrays.asList(str.split(":"));
		
		str = fieldTemplate.getMetaValue("exclude", "");
		var excludes = StringUtils.isBlank(str) ? Collections.emptyList() : Arrays.asList(str.split(":"));
		
		return Arrays.asList(enums).stream().filter(f-> {
			return ( includes.size() == 0 || includes.contains(f.name()) ) &&
				     !excludes.contains(f.name());
		}).toList().toArray(new Enum<?>[0]);
	}

	private void processDynamicElements(Elements thisElement, TemplateViewField fieldView, AbstractObject obj) {

		Map<String,ObjectDynamicField> optionalFields = generateDynamicFields(currentTemplate.get());
		Element row = thisElement.parents().select(".field").first();

		if(Objects.nonNull(row)) {
			
			if(optionalFields.containsKey(fieldView.getVariable())) {
				
				String dependsOn = optionalFields.get(fieldView.getVariable()).dependsOn();
				String dependsValue = optionalFields.get(fieldView.getVariable()).dependsValue();
				row.attr("data-depends-on", dependsOn);
				row.attr("data-depends-value", dependsValue);
				row.attr("data-resourcekey", fieldView.getResourceKey());
				row.addClass("processDepends");
				String[] matchValues = dependsValue.split(",");
				boolean matches = false;
				for(String matchValue : matchValues) {
					boolean expectedResult = !matchValue.startsWith("!");
					if(!expectedResult) {
						matchValue = matchValue.substring(1);
					}
					FieldTemplate depends = currentTemplate.get().getField(dependsOn);
					if(Objects.nonNull(obj)) {
						if(depends == null)
							throw new IllegalArgumentException("The field '" + dependsOn + "' that '" + fieldView.getResourceKey() + "' depends on does not exist.");
						Object value = obj.getValue(depends);
						if(Objects.isNull(value)) {
							value = depends.getDefaultValue();
						}
						if(value.toString().equals(matchValue)) {
							matches = true;
							break;
						} else if(!expectedResult) {
							matches = true;
							break;
						}
					}
				}
				
				if(!matches) {
					row.addClass("d-none");
				}
			}
			
			if(fieldView.isAutoSave()) {
				row.addClass("processAutosave");
				row.attr("data-action", String.format("/app/api/form/stash/%s", obj.getResourceKey()));
			}
		}
	}
	
	private Map<String, ObjectDynamicField> generateDynamicFields(ObjectTemplate objectTemplate) {
		
		Map<String,ObjectDynamicField> tmp = new HashMap<>();
		
		Class<?> clz = templateService.getTemplateClass(objectTemplate.getResourceKey());
		while(Objects.nonNull(clz)) {
			ObjectDynamicField[] fields = clz.getAnnotationsByType(ObjectDynamicField.class);
			if(Objects.nonNull(fields)) {
				for(ObjectDynamicField field : fields) {
					tmp.put(field.field(), field);
				}
			}
			clz = clz.getSuperclass();
		}
		return tmp;
	}

	private String getDefaultValue(TemplateViewField field) {
		return encodeValue(field, field.getField().getDefaultValue());
	}
	
	private String encodeValue(TemplateViewField field, String value) {
//		switch(field.getField().getFieldType()) {
//		case TEXT_AREA:
//			if(field.getRenderer()==FieldRenderer.HTML_EDITOR || field.getRenderer()==FieldRenderer.HTML_VIEW) {
//				return value;
//			}
//			return Encode.forHtml(value);
//		default:
//			return Encode.forHtml(value);
//		}
		return value;
	}
	
	private String getFieldValue(TemplateViewField field, AbstractObject obj) {
		if(Objects.isNull(obj)) {
			return getDefaultValue(field);
		}
	
		List<FieldTemplate> parents = field.getParentFields();
		if(Objects.nonNull(parents)) {
			for(FieldTemplate parentField : parents) {
				obj = obj.getChild(parentField);
				if(Objects.isNull(obj)) {
					return getDefaultValue(field);
				}
			}
		}
		if(Objects.isNull(obj)) {
			return getDefaultValue(field);
		}
		
		Object val = obj.getValue(field.getField());
		if(Objects.isNull(val)) {
			return getDefaultValue(field);
		}
		
		if(field.requiresDecryption()) {
			val = encryptionService.decrypt(val.toString());
		}
		
		switch(field.getField().getFieldType()) {
		case DATE:
			if(val instanceof Date) {
				return Utils.formatISODate((Date)val);
			}
			break;
		case TIMESTAMP:
			if(val instanceof Date) {
				return Utils.formatTimestamp((Date)val);
			}
			break;
		case TEXT_AREA:
			switch(field.getRenderer()) {
			case HTML_VIEW:
			case HTML_EDITOR:
				return val.toString();
			default:
				break;
			}
			break;
		case ATTACHMENT:
		case OBJECT_REFERENCE:
		{
			AbstractObject ref = obj.getChild(field.getField());
			if(Objects.nonNull(ref)) {
				return ref.getUuid();
			}
			return "";
		}
		default:
			break;
		}
		
		
		return encodeValue(field, val.toString()); 	
	}

	private Element createViewElement(TemplateView view, Element rootElement, boolean first) {
		
		if(view.isRoot()) {
			Element root;
			rootElement.prependChild(root = new Element("div")
					.attr("id", "rootView")
					.attr("class", "col-12 py-1"));
			return root;
		}

		switch(view.getType()) {
		case ACCORDION:
			return createAccordionElement(view, rootElement, first);
		default:
			return createTabElement(view, rootElement, first);
		}
	}

	private Element createAccordionElement(TemplateView view, Element rootElement, boolean first) {
		
		Element accord = rootElement.selectFirst(".accordion");

		/* Card */
		Element card = accord.appendElement("div")
			.addClass("card");

		/* Header */
		Element cardHeader = card.appendElement("div")
			.addClass("card-header")
			.attr("id", view.getResourceKey());
		
		Element cardHeaderLink = cardHeader.appendElement("h2")
			.addClass("mb-0")
			.appendElement("a")
				.addClass("btn")
				.addClass("btn-link")
				.attr("data-bs-toggle", "collapse")
				.attr("data-target", "#collapse" + view.getResourceKey())
				.attr("href", "#collapse" + view.getResourceKey())
				.attr("aria-expanded", String.valueOf(first))
				.attr("aria-controls", "collapse" + view.getResourceKey())
				;

		cardHeaderLink.appendElement("span")
			.attr("jad:bundle", view.getBundle())
			.attr("jad:i18n",  String.format("%s.name", view.getResourceKey()));

		/* Collapsible Body Container */
		Element outer = card.appendElement("div")
			.attr("id", "collapse" + view.getResourceKey())
			.attr("aria-labelledby", view.getResourceKey())
			.attr("data-parent",  String.format("#%sAccordion", currentTemplate.get().getResourceKey()))
			.addClass("collapse");
		if (first)
			outer.addClass("show");
		
		/* Card Body */
		return outer.appendElement("div")
			.addClass("card-body");

	}

	private Element createTabElement(TemplateView view, Element rootElement, boolean first) {
		
		Element list = rootElement.selectFirst("ul.nav");
		
		list.appendChild(new Element("li")
				.addClass("nav-item")
					.appendChild(Html.div()
					.appendChild(new Element("a")
						.addClass("nav-link")
						.attr("data-bs-toggle", "tab")
						.attr("href", String.format("#%s", view.getResourceKey()))
						.attr("jad:bundle", view.getBundle())
						.attr("jad:i18n", String.format("%s.name", view.getResourceKey())))));
		Element div;
		
		rootElement.selectFirst(".tab-content").appendChild(div = new Element("div")
														.attr("id", view.getResourceKey())
														.addClass("px-3")
														.addClass("tab-pane")
														.addClass("panel-body")
														.addClass("fade")
														.addClass("in"));
		
		if(view.isExtension()) {
			div.appendChild(Html.div("row")
					.appendChild(Html.div("col-12")
						.appendChild(Html.a("#")
							.addClass("removeExtension")
							.attr("data-ext", view.getExtension())
							.appendChild(
							new Element("sup")
								.addClass("float-end")
								.appendChild(Html.i("fa-solid", "fa-times", "me-1"))
								.appendChild(
							Html.i18n(view.getBundle(), String.format("%s.remove", view.getExtension())))))));
		}
		Element tabPane = rootElement.select(".tab-pane").last();
		
		if(first) {
			list.select("li").last().addClass("active");
			list.select(".nav-link").first().addClass("active");
			tabPane.addClass("active");
			tabPane.addClass("active show");
		}
		
		return tabPane;
	}


	public void renderResources(Document document, Class<?> templateClz) throws IOException {
		
		String jsResource = templateClz.getSimpleName() + ".js";
		URL url = templateClz.getResource(jsResource);
		if(Objects.nonNull(url)) {
			PageHelper.appendBodyScript(document, "/app/script/" + templateClz.getPackageName().replace('.', '/') + "/" + jsResource);
		}
		
		String cssResource = templateClz.getSimpleName() + ".css";
		url = templateClz.getResource(cssResource);
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(document,  "/app/style/" + templateClz.getPackageName().replace('.', '/') + "/" + cssResource);
		}
		
	}
	
	
}
