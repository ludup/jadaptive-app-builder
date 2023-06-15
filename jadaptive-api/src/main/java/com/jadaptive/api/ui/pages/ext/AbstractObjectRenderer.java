package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.app.SecurityScope;
import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.entity.AbstractObject;
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
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.ViewType;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.I18nOption;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;
import com.jadaptive.api.ui.renderers.form.BooleanFormInput;
import com.jadaptive.api.ui.renderers.form.BootstrapBadgeRender;
import com.jadaptive.api.ui.renderers.form.CollectionSearchFormInput;
import com.jadaptive.api.ui.renderers.form.CollectionTextFormInput;
import com.jadaptive.api.ui.renderers.form.CssEditorFormInput;
import com.jadaptive.api.ui.renderers.form.DateFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownFormInput;
import com.jadaptive.api.ui.renderers.form.FieldSearchFormInput;
import com.jadaptive.api.ui.renderers.form.FileFormInput;
import com.jadaptive.api.ui.renderers.form.HiddenFormInput;
import com.jadaptive.api.ui.renderers.form.HtmlEditorFormInput;
import com.jadaptive.api.ui.renderers.form.ImageFormInput;
import com.jadaptive.api.ui.renderers.form.MultipleSelectionFormInput;
import com.jadaptive.api.ui.renderers.form.NumberFormInput;
import com.jadaptive.api.ui.renderers.form.PasswordFormInput;
import com.jadaptive.api.ui.renderers.form.TextAreaFormInput;
import com.jadaptive.api.ui.renderers.form.TextFormInput;
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
	private ApplicationService applicationService;
	
	protected ThreadLocal<Document> currentDocument = new ThreadLocal<>();
	protected ThreadLocal<ObjectTemplate> currentTemplate = new ThreadLocal<>();
	private ThreadLocal<Properties> securityProperties = new ThreadLocal<>();
	private ThreadLocal<Map<String,AbstractObject>> childObjects = new ThreadLocal<>();

	protected ThreadLocal<Boolean> disableViews = new ThreadLocal<>();
	protected ThreadLocal<Set<String>> ignoreResources = new ThreadLocal<>();
	
	protected void process(Document contents, Page page, ObjectTemplate template, AbstractObject object, FieldView scope) throws IOException {

		currentDocument.set(contents);
		currentTemplate.set(template);
		
    	try {
    		securityProperties.set(propertyService.getOverrideProperties(
					SecurityScope.TENANT, 
					template.getResourceKey() + ".properties"));
			
				
			if(Objects.nonNull(object)) {
				Map<String,AbstractObject> children = new HashMap<>();
				children.put(object.getResourceKey(), object);
				extractChildObjects(object, children, template);
				childObjects.set(children);
			}
			 
			
			Element row;
			Element form;
			contents.selectFirst("body").appendChild(
					form = new Element("form")
						.addClass("jadaptiveForm")
						.attr("id", "objectForm")
						.attr("method", "POST")
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
						.appendChild(new Element("input")
								.attr("type", "hidden")
					 			.attr("name", "hidden")
								.val(Objects.nonNull(object) ?  String.valueOf(object.isHidden()) : "false"))));
				
			Session session = sessionUtils.getActiveSession(Request.get());
			if(Objects.nonNull(session)) {
				form.appendChild(Html.input("hidden", 
						SessionUtils.CSRF_TOKEN_ATTRIBUTE, 
							sessionUtils.setupCSRFToken(Request.get()))
							.attr("id", "csrftoken"));
						
			}
			
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
						.appendChild(dropdown.renderInput()));
				List<I18nOption> options = new ArrayList<>();
				for(ExtensionRegistration extension : extensions) {
					options.add(new I18nOption(extension.bundle(), 
							String.format("%s.name", extension.resourceKey()), 
							extension.resourceKey()));
				}
				dropdown.renderValues(options, "");
				dropdown.setName(parent.getBundle(), "extendWith.name");		
			}
			
			List<TemplateView> views = templateService.getViews(template, disable);
			
			createViews(views, row, object, scope);

		} catch (IOException e) {
			log.error("Error processing entity", e);
			throw new IOException(e.getMessage(), e);
		} finally {
			currentDocument.remove();
			securityProperties.remove();
			childObjects.remove();
		}
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
			
			for(TemplateViewField orderedField : view.getFields()) {
				FieldTemplate field = orderedField.getField();
				if(field.isHidden()) {
					HiddenFormInput render = new HiddenFormInput(currentTemplate.get(), orderedField);
					render.renderInput(element, getFieldValue(orderedField, obj));
					continue;
				}
				switch(field.getFieldType()) {
				default:
					renderField(viewElement, obj, orderedField, scope, view);
					break;
				}
			}
			
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

	private void renderField(Element element, AbstractObject obj, TemplateViewField orderedField,  FieldView scope, TemplateView panel) throws IOException {
		
		FieldTemplate field = orderedField.getField();
		
		Set<String> ignores = ignoreResources.get();
		if(Objects.nonNull(ignores) && ignores.contains(field.getResourceKey())) {
			return;
		}
		
		if(!field.getViews().isEmpty()) {
			if(!field.getViews().contains(scope)) {
				HiddenFormInput render = new HiddenFormInput(currentTemplate.get(), orderedField);
				render.renderInput(element, getFieldValue(orderedField, obj));
				return;
			}
		}
		
		if(field.getCollection()) {
			renderCollection(element, obj, orderedField, scope, panel); 
		} else {
			switch(field.getFieldType()) {
			case OBJECT_REFERENCE:
				String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
				ObjectTemplate objectTemplate = templateService.get(objectType);
				String uuid = null;
				String name = null;
				if(Objects.nonNull(obj)) {
					AbstractObject ref = obj.getChild(orderedField.getField());
					if(Objects.nonNull(ref)) {
						uuid = ref.getUuid();
						name = (String) ref.getValue("name");
					}
				}

				FieldSearchFormInput input = new FieldSearchFormInput(currentTemplate.get(), orderedField, 
						String.format("/app/api/objects/%s/table", objectType),
						objectTemplate.getNameField(), "uuid");
				input.renderInput(element, uuid, name, false, scope == FieldView.READ);
				break;
			case OBJECT_EMBEDDED:
//				renderFormField(template, element, Objects.nonNull(obj) ? obj.getChild(field) : null, field, properties, view);
				throw new IllegalStateException("Embedded object field should not be processed here");
			default:	
				renderFormField(element, obj, orderedField, scope, panel);
			}
			
		}
	}
	
	private void renderCollection(Element element, AbstractObject obj, TemplateViewField orderedField,
			FieldView view, TemplateView panel) throws IOException {
		
		FieldTemplate field = orderedField.getField();
		
		List<FieldTemplate> parents = orderedField.getParentFields();
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
			
			List<String> available = new ArrayList<>();

			String enumType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			try {
				Class<?> cls = classLoader.findClass(enumType);
				for(Object enumObj : cls.getEnumConstants()) {
					available.add(enumObj.toString());
				}

				MultipleSelectionFormInput render = new MultipleSelectionFormInput(currentTemplate.get(), orderedField);
				render.renderInput(panel, element, available, values, false);
			} catch (ClassNotFoundException e) {
				log.error("Cannot render enum as the class could not be found", e);
			}

			
			break;
		}
		case INTEGER:
			break;
		case LONG:
			break;
		case OBJECT_EMBEDDED:
		{
			TableRenderer table = applicationService.autowire(new TableRenderer(view == FieldView.READ, obj, field));
			String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			ObjectTemplate objectTemplate = templateService.get(objectType);
			
			Collection<AbstractObject> objects;
			
			if(Objects.nonNull(obj)) {
				objects = obj.getObjectCollection(field.getResourceKey());
			} else {
				objects = new ArrayList<>();
			}
			
			table.setLength(objects.size());
			table.setStart(0);
			table.setTotalObjects(objects.size());
			table.setObjects(objects);
			table.setTemplate(objectTemplate);
			table.setTemplateClazz(templateService.getTemplateClass(objectTemplate.getResourceKey()));
			
			element.insertChildren(0, table.render());
			break;
		}
		case OBJECT_REFERENCE:
		{
			String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			ObjectTemplate objectTemplate = templateService.get(objectType);
			List<NamePairValue> values = new ArrayList<>();
			if(Objects.nonNull(obj)) {
				for(Object o : obj.getObjectCollection(field.getResourceKey())) {
					if(o instanceof String) {
						AbstractObject referencedObject = objectService.get(objectType, (String)o);
						values.add(new NamePairValue(referencedObject.getValue(objectTemplate.getNameField()).toString(), (String)o));
					} else if(o instanceof AbstractObject) {
						AbstractObject ref = (AbstractObject) o;
						values.add(new NamePairValue((String)ref.getValue("name"), ref.getUuid()));
					}
				}
			}
			CollectionSearchFormInput render = new CollectionSearchFormInput(
					currentTemplate.get(), orderedField, String.format("/app/api/objects/%s/table", objectType),
					objectTemplate.getNameField(), "uuid");
			render.renderInput(panel, element, values, false, 
					(view == FieldView.READ || orderedField.getField().isReadOnly()));
			break;
		}
		case PASSWORD:
			break;
		case TEXT:
		{
			switch(orderedField.getRenderer()) {
//			case TAGS:
//			{
//				MultipleTagsFormInput render = new MultipleTagsFormInput(currentTemplate.get(), orderedField);
//				render.renderInput(panel, element, Objects.nonNull(obj) ? 
//						obj.getCollection(field.getResourceKey()) 
//						: Collections.emptyList());
//				break;
//			}
			default:
			{
				CollectionTextFormInput render = new CollectionTextFormInput(currentTemplate.get(), orderedField);
				render.renderInput(panel, element, 
						Objects.nonNull(obj) ? 
								obj.getCollection(field.getResourceKey()) 
								: Collections.emptyList(), (view == FieldView.READ || orderedField.getField().isReadOnly()));
				break;
			}
			}
			

			break;
		}
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
					currentTemplate.get(), orderedField, "/app/api/permissions/table",
					"name", "value");
			render.renderInput(panel, element, values, false, (view == FieldView.READ || orderedField.getField().isReadOnly()));
			
			break;
		}
		default:
			break;
		
		}
	}
	
	private void renderFormField(Element element, AbstractObject obj, TemplateViewField orderedField,
			FieldView view, TemplateView panel) throws IOException {
		
		FieldTemplate field = orderedField.getField();

		switch(field.getFieldType()) {
		case TEXT:
		{
			switch(orderedField.getRenderer()) {
			case BOOTSTRAP_BADGE:
			{
				BootstrapBadgeRender render = new BootstrapBadgeRender(currentTemplate.get(), orderedField);
				render.renderInput(element, getFieldValue(orderedField, obj));
				break;
			}
			case I18N:
			{
				String i18nValue = i18nService.format(currentTemplate.get().getBundle(), Locale.getDefault(), getFieldValue(orderedField, obj));
				TextFormInput render = new TextFormInput(currentTemplate.get(), orderedField);
				render.renderInput(element, i18nValue);
				break;
			}
			case COUNTRY:
			{
				DropdownFormInput dropdown = new DropdownFormInput(currentTemplate.get(), orderedField);
				dropdown.renderInput(element, "");
				for(Country country : internationalService.getCountries()) {
					dropdown.addInputValue(country.getCode(), country.getName());
				}
				String code = getFieldValue(orderedField, obj);
				if(StringUtils.isNotBlank(code)) {
					dropdown.setSelectedValue(code, internationalService.getCountryName(code));
				}
				break; 
			}
			case OPTIONAL:
			{
				String value = getFieldValue(orderedField, obj); 
				if(StringUtils.isNotBlank(value) || (!orderedField.getField().isReadOnly() && view !=FieldView.READ)) {
					TextFormInput render = new TextFormInput(currentTemplate.get(), orderedField);
					render.renderInput(element, value);
				}
				break;
			}
			default:
			{
				TextFormInput render = new TextFormInput(currentTemplate.get(), orderedField);
				render.renderInput(element, getFieldValue(orderedField, obj));
				break;
			}
			}
			break;
		}
		case IMAGE:
		{
			String val = getFieldValue(orderedField, obj);
			ImageFormInput render = new ImageFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, val);
			break;
		}
		case FILE:
		{
			FileFormInput render = new FileFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, getFieldValue(orderedField, obj));
			break;
		}
		case TEXT_AREA:
		{
			switch(orderedField.getRenderer()) {
			case CSS_EDITOR:
			{
				CssEditorFormInput render = new CssEditorFormInput(currentTemplate.get(), orderedField, currentDocument.get(), view == FieldView.READ);
				render.renderInput(element, getFieldValue(orderedField, obj));
				break;
			}
			case HTML_EDITOR:
			{
				HtmlEditorFormInput render = new HtmlEditorFormInput(currentTemplate.get(), orderedField, currentDocument.get(), view == FieldView.READ);
				render.renderInput(element, getFieldValue(orderedField, obj));
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
						.attr("srcdoc", getFieldValue(orderedField, obj)));
				break;
			}
			case I18N:
			{
				String i18nValue = i18nService.format(panel.getBundle(), Locale.getDefault(), getFieldValue(orderedField, obj));
				TextAreaFormInput render = new TextAreaFormInput(currentTemplate.get(), orderedField, field.getMetaValueInt("rows", 15));
				render.renderInput(element, i18nValue);
				break;
			}
			case OPTIONAL:
			{
				String value = getFieldValue(orderedField, obj);
				if(StringUtils.isNotBlank(value) || view!=FieldView.READ) {
					TextAreaFormInput render = new TextAreaFormInput(currentTemplate.get(), orderedField, field.getMetaValueInt("rows", 15));
					render.renderInput(element, value);
				}
				break;
			}
			default:
			{
				TextAreaFormInput render = new TextAreaFormInput(currentTemplate.get(), orderedField, field.getMetaValueInt("rows", 10));
				render.renderInput(element, getFieldValue(orderedField, obj));
				break;
			}
			}
			break;
		}
		case PASSWORD:
		{
			PasswordFormInput render = new PasswordFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, getFieldValue(orderedField, obj));
			break;
		}
		case TIMESTAMP:
		{
			TimestampFormInput render = new TimestampFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, getFieldValue(orderedField, obj));
			break;
		}
		case DATE:
		{
			String dateValue = getFieldValue(orderedField, obj);
			if(orderedField.getRenderer()==FieldRenderer.OPTIONAL) {
				if(StringUtils.isBlank(dateValue)) {
					break;
				}
			}

			DateFormInput render = new DateFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, dateValue);
			break;
			
		}
		case BOOL:
		{
			BooleanFormInput render = new BooleanFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, getFieldValue(orderedField, obj));
			if(field.isReadOnly() || view == FieldView.READ) {
				render.disable();
			}
			break;
		}
		case PERMISSION:
		{
			DropdownFormInput render = new DropdownFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, getFieldValue(orderedField, obj));
			render.renderValues(permissionService.getAllPermissions(), getFieldValue(orderedField, obj));
		
			break;
		}
		case ENUM:
		{
			switch(orderedField.getRenderer()) {
			case BOOTSTRAP_BADGE:
			{
				BootstrapBadgeRender render = new BootstrapBadgeRender(currentTemplate.get(), orderedField);
				render.renderInput(element, getFieldValue(orderedField, obj));
				break;
			}
			default:
				Class<?> values;
				try {
					values = classLoader.findClass(field.getValidationValue(ValidationType.OBJECT_TYPE));
					DropdownFormInput render = new DropdownFormInput(currentTemplate.get(), orderedField);
					render.renderInput(element, getFieldValue(orderedField, obj));
					render.renderValues((Enum<?>[])values.getEnumConstants(), getFieldValue(orderedField, obj), view == FieldView.READ);
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
			NumberFormInput render = new NumberFormInput(currentTemplate.get(), orderedField);
			render.renderInput(element, getFieldValue(orderedField, obj));
			break;
		}
		case OBJECT_EMBEDDED:
		case OBJECT_REFERENCE:
			throw new IllegalStateException("Object cannot be rendered by renderField");
		default:
			throw new IllegalStateException("Missing field type " + field.getFieldType().name());
		}
		
		
		Elements thisElement = element.select("#" + field.getResourceKey());
		if(field.isRequired()) {
			thisElement.attr("required", "required");
		}
		if(field.isReadOnly() || view == FieldView.READ) {
			thisElement.attr("readonly", "readonly");
		}
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
		
		Element list = rootElement.selectFirst("ul");
		
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
							Html.i18n(view.getExtensionBundle(), String.format("%s.remove", view.getExtension())))))));
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


	
	
}