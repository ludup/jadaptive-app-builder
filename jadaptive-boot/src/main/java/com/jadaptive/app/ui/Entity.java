package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.app.SecurityScope;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.ViewType;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.ui.Page;
import com.jadaptive.app.ui.renderers.form.BooleanFormInput;
import com.jadaptive.app.ui.renderers.form.DateFormInput;
import com.jadaptive.app.ui.renderers.form.DropdownFormInput;
import com.jadaptive.app.ui.renderers.form.HiddenFormInput;
import com.jadaptive.app.ui.renderers.form.HtmlEditorFormInput;
import com.jadaptive.app.ui.renderers.form.MultipleSearchFormInput;
import com.jadaptive.app.ui.renderers.form.MultipleSelectionFormInput;
import com.jadaptive.app.ui.renderers.form.NumberFormInput;
import com.jadaptive.app.ui.renderers.form.PasswordFormInput;
import com.jadaptive.app.ui.renderers.form.TextAreaFormInput;
import com.jadaptive.app.ui.renderers.form.TextFormInput;
import com.jadaptive.app.ui.renderers.form.TimestampFormInput;
import com.jadaptive.utils.Utils;

@Extension
public class Entity extends AbstractPageExtension {

	static Logger log = LoggerFactory.getLogger(Entity.class);
	
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
	
	private ThreadLocal<Document> currentDocument = new ThreadLocal<>();
	
	@Override
	public String getName() {
		return "objectRenderer";
	}

    @Override
	public void process(Document contents, Page page) throws IOException {

		if(!(page instanceof TemplatePage)) {
			throw new IllegalStateException();
		}
		
		currentDocument.set(contents);
		
		TemplatePage templatePage = (TemplatePage) page;
		
    	try {
			Properties properties = propertyService.getOverrideProperties(
					SecurityScope.TENANT, 
					templatePage.getTemplate().getResourceKey() + ".properties");
			
			AbstractObject object = null;
			if(page instanceof ObjectPage) {
				object = ((ObjectPage)page).getObject();
			}
			
			Element row;
			Element form;
			contents.selectFirst("body").appendChild(
					form = new Element("form")
						.attr("id", "entity")
						.attr("method", "POST")
						.attr("data-resourcekey", templatePage.getResourceKey())
						.attr("enctype", "application/x-www-form-urlencoded")
						.attr("action", String.format("/app/api/%s", templatePage.getResourceKey()))
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
								.val(Objects.nonNull(object) ? object.getResourceKey() : templatePage.getResourceKey())
						.appendChild(new Element("input")
								.attr("type", "hidden")
								.attr("name", "hidden")
								.val(Objects.nonNull(object) ?  String.valueOf(object.isHidden()) : "false"))));
						
//			
//			if(Objects.nonNull(object)) {
//				String clz = (String) object.getValue("_clz");
//				if(Objects.nonNull(clz)) {
//					form.appendChild(new Element("input")
//							.attr("type", "hidden")
//							.attr("name", "_clz")
//							.val(clz));
//				}
//			} else {
//				form.appendChild(new Element("input")
//						.attr("type", "hidden")
//						.attr("name", "_clz")
//						.val(templatePage.getTemplate().getTemplateClass()));
//			}
			
			form.appendChild(row = new Element("div").addClass("row"));
			
			List<OrderedView> views = templateService.getViews(templatePage.getTemplate());

			createViews(views, row, templatePage.getTemplate(), object, properties, templatePage.getScope());

		} catch (IOException e) {
			log.error("Error processing entity", e);
			throw new IOException(e.getMessage(), e);
		} finally {
			currentDocument.remove();
		}
    }

	private void createViews(List<OrderedView> views, Element element, ObjectTemplate template, AbstractObject obj, Properties properties, FieldView currentView) {
		
		int tabIndex = hasTabbedView(views);
		int acdIndex = hasAccordionView(views);
		
		boolean first = true;
		for(OrderedView view : views) {
			
			if(view.getFields().isEmpty()) {
				continue;
			}
			
			Element viewElement = createViewElement(view, element, template, first && !view.isRoot());
			
			for(OrderedField orderedField : view.getFields()) {
				FieldTemplate field = orderedField.getField();
				if(field.isHidden()) {
					continue;
				}
				switch(field.getFieldType()) {
				default:
					renderField(template, viewElement, obj, orderedField, properties, currentView, view);
					break;
				}
			}
			
			if(!view.getChildViews().isEmpty()) {
				createViews(view.getChildViews(), viewElement, template, obj, properties, currentView);
			}

			if(!view.isRoot()) {
				first = false;
			} else {
				if(tabIndex > -1) {
					createTabOutline(viewElement);
				}
				
				if(acdIndex > -1) {
					createAccordionOutline(viewElement, template, tabIndex < acdIndex);
				}
			}
		}
	}
	
	private void createTabOutline(Element element) {
		element.appendChild(new Element("ul").attr("class", "nav nav-tabs pt-4"));
		element.appendChild(new Element("div").attr("class", "tab-content panel col-12 py-4"));
	}
	
	private void createAccordionOutline(Element element, ObjectTemplate template, boolean append) {
		
		if(append) {
			element.appendChild(new Element("div")
					.attr("class", "accordion col-12")
					.attr("id", String.format("%sAccordion", template.getResourceKey())));
		} else {
			element.prependChild(new Element("div")
					.attr("class", "accordion col-12")
					.attr("id", String.format("%sAccordion", template.getResourceKey())));
		}
	}
	
	private int hasTabbedView(List<OrderedView> views) {
		int idx = 0;
		for(OrderedView v : views) {
			if(!v.isRoot() && v.getType()==ViewType.TAB) {
				return idx;
			}
			idx++;
		}
		return -1;
	}
	
	private int hasAccordionView(List<OrderedView> views) {
		int idx = 0;
		for(OrderedView v : views) {
			if(!v.isRoot() && v.getType()==ViewType.ACCORDION) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	private void renderField(ObjectTemplate template, Element element, AbstractObject obj, OrderedField orderedField, Properties properties, FieldView view, OrderedView panel) {
		
		FieldTemplate field = orderedField.getField();
		if(!field.getViews().isEmpty()) {
			if(!field.getViews().contains(view)) {
				if(log.isDebugEnabled()) {
					log.debug("Skipping field {} as its view scopes {} are not in the current scope {}",
							field.getResourceKey(),
							Utils.csv(field.getViews()),
							view.name());
				}
				return;
			}
		}
		
		if(field.getCollection()) {
			renderColletion(template, element, obj, orderedField, properties, view, panel); 
		} else {
			switch(field.getFieldType()) {
			case OBJECT_REFERENCE:
				/**
				 * TODO this should be a form field, ensure no all objects are references, not links,
				 * as links require different processing.
				 */
				break;
			case OBJECT_EMBEDDED:
//				renderFormField(template, element, Objects.nonNull(obj) ? obj.getChild(field) : null, field, properties, view);
				throw new IllegalStateException("Embedded object field should not be processed here");
			default:
				
				renderFormField(template, element, obj, orderedField, properties, view, panel);
			}
			
		}
	}
	
	private void renderColletion(ObjectTemplate template, Element element, AbstractObject obj, OrderedField orderedField,
			Properties properties, FieldView view, OrderedView panel) {
		
		FieldTemplate field = orderedField.getField();
		
		switch(field.getFieldType()) {
		case BOOL:
			break;
		case DATE:
			break;
		case DECIMAL:
			break;
		case ENUM:
			break;
		case INTEGER:
			break;
		case LONG:
			break;
		case OBJECT_EMBEDDED:
			
			break;
		case OBJECT_REFERENCE:
		{
			String objectType = field.getValidationValue(ValidationType.RESOURCE_KEY);
			ObjectTemplate objectTemplate = templateService.get(objectType);
			List<NamePairValue> values = new ArrayList<>();
			if(Objects.nonNull(obj)) {
				for(String uuid : obj.getCollection(objectType)) {
					AbstractObject referencedObject = objectService.get(objectType, uuid);
					values.add(new NamePairValue(referencedObject.getValue(objectTemplate.getNameField()).toString(), uuid));
				}
			}
			MultipleSearchFormInput render = new MultipleSearchFormInput(
					template, orderedField, String.format("/app/api/%s/table", objectType),
					objectTemplate.getNameField(), "uuid");
			render.renderInput(panel, element, values, false);
			break;
		}
		case PASSWORD:
			break;
		case TEXT:
			break;
		case TEXT_AREA:
			break;
		case HIDDEN:
			break;
		case TIMESTAMP:
			break;
		case PERMISSION:
		{
			MultipleSelectionFormInput render = new MultipleSelectionFormInput(template, orderedField);
			render.renderInput(panel, element, permissionService.getAllPermissions(), 
					Objects.nonNull(obj) ? obj.getCollection(field.getResourceKey()) : Collections.emptyList(), true);
			break;
		}
		default:
			break;
		
		}
	}
	
	private void renderFormField(ObjectTemplate template, Element element, AbstractObject obj, OrderedField orderedField,
			Properties properties, FieldView view, OrderedView panel) {
		
		FieldTemplate field = orderedField.getField();

		switch(field.getFieldType()) {
		case HIDDEN:
		{
			HiddenFormInput render = new HiddenFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			break;
		}
		case TEXT:
		{
			TextFormInput render = new TextFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			break;
		}
		case TEXT_AREA:
		{
			switch(orderedField.getRenderer()) {
			case HTML_EDITOR:
			{
				HtmlEditorFormInput render = new HtmlEditorFormInput(template, orderedField, currentDocument.get());
				render.renderInput(panel, element, getDefaultValue(orderedField, obj));
				break;
			}
			default:
			{
				TextAreaFormInput render = new TextAreaFormInput(template, orderedField);
				render.renderInput(panel, element, getDefaultValue(orderedField, obj));
				break;
			}
			}
			break;
		}
		case PASSWORD:
		{
			PasswordFormInput render = new PasswordFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			break;
		}
		case TIMESTAMP:
		{
			TimestampFormInput render = new TimestampFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			break;
		}
		case DATE:
		{
			DateFormInput render = new DateFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			break;
		}
		case BOOL:
		{
			BooleanFormInput render = new BooleanFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			break;
		}
		case PERMISSION:
		{
			DropdownFormInput render = new DropdownFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
			render.renderValues(permissionService.getAllPermissions(), getDefaultValue(orderedField, obj));
		
			break;
		}
		case ENUM:
		{
			Class<?> values;
			try {
				values = classLoader.findClass(field.getValidationValue(ValidationType.OBJECT_TYPE));
				DropdownFormInput render = new DropdownFormInput(template, orderedField);
				render.renderInput(panel, element, getDefaultValue(orderedField, obj));
				render.renderValues((Enum<?>[])values.getEnumConstants(), getDefaultValue(orderedField, obj), view == FieldView.READ);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
			
			break;
		}
		case DECIMAL:
		case INTEGER:
		case LONG:
		{
			NumberFormInput render = new NumberFormInput(template, orderedField);
			render.renderInput(panel, element, getDefaultValue(orderedField, obj));
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

	private String getDefaultValue(OrderedField field, AbstractObject obj) {
		return field.getFieldValue(obj);
	}

	private Element createViewElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
		if(view.isRoot()) {
			Element root;
			rootElement.prependChild(root = new Element("div")
					.attr("id", "rootView")
					.attr("class", "col-12 py-1"));
			return root;
//			return rootElement.prepend("<div id=\"rootView\" class=\"col-12 py-1\"></div>").select("#rootView").first();
		}

		switch(view.getType()) {
		case ACCORDION:
			return createAccordionElement(view, rootElement, template, first);
		default:
			return createTabElement(view, rootElement, template, first);
		}
	}

	private Element createAccordionElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
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
				.attr("data-toggle", "collapse")
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
			.attr("data-parent",  String.format("#%sAccordion", template.getResourceKey()))
			.addClass("collapse");
		if (first)
			outer.addClass("show");
		
		/* Card Body */
		return outer.appendElement("div")
			.addClass("card-body");

	}

	private Element createTabElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
		Element list = rootElement.selectFirst("ul");
		
		list.appendChild(new Element("li")
				.addClass("nav-item")
					.appendChild(new Element("a")
						.addClass("nav-link")
						.attr("data-toggle", "tab")
						.attr("href", String.format("#%s", view.getResourceKey()))
						.attr("jad:bundle", view.getBundle())
						.attr("jad:i18n", String.format("%s.name", view.getResourceKey()))));

		rootElement.selectFirst(".tab-content").appendChild(new Element("div")
														.attr("id", view.getResourceKey())
														.addClass("tab-pane")
														.addClass("panel-body")
														.addClass("fade")
														.addClass("in"));
		
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
