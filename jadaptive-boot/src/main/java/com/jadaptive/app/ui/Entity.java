package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.ParentView;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.app.SecurityScope;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.ui.renderers.form.BooleanFormInput;
import com.jadaptive.app.ui.renderers.form.DropdownFormInput;
import com.jadaptive.app.ui.renderers.form.FieldInputRender;
import com.jadaptive.app.ui.renderers.form.NumberFormInput;
import com.jadaptive.app.ui.renderers.form.PasswordFormInput;
import com.jadaptive.app.ui.renderers.form.TextAreaFormInput;
import com.jadaptive.app.ui.renderers.form.TextFormInput;
import com.jadaptive.app.ui.renderers.form.TimestampFormInput;
import com.jadaptive.utils.Utils;

@Widget({ PageResources.class, PageResourcesElement.class })
@View(contentType = "text/html")
@Resource
public class Entity {

	static Logger log = LoggerFactory.getLogger(Entity.class);
	
	@Autowired
	private SecurityPropertyService propertyService; 
	
	@Autowired
	private ClassLoaderService classLoader;
	
	@Autowired
	private TemplateService templateService; 
	
	@PostConstruct
	private void postConstruct() {
	
	}
	
    @Out
    public Elements service(@In Elements contents, @ParentView TemplatePage page) throws IOException {
    	
    	try {
			Properties properties = propertyService.getOverrideProperties(
					SecurityScope.TENANT, 
					page.getTemplate().getResourceKey() + ".properties");
			
			FieldView view = page.getScope();
			
			AbstractObject object = null;
			if(page instanceof ObjectPage) {
				object = ((ObjectPage)page).getObject();
			}
			renderObject(contents, page.getTemplate(), object, properties, view);
			return contents;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage(), e);
		}
    	
	
    }

	private void renderObject(Elements contents, ObjectTemplate template, AbstractObject obj, Properties properties, FieldView view) {
		
		contents.append("<form id=\"entity\"><div class=\"row\"><ul class=\"nav nav-tabs pt-4\"></ul><div class=\"tab-content panel col-12 py-4\"></div></div></form>");
		
		Element element = contents.select(".row").first();
		orderFields(element, template, obj, properties, view);
	}

	private void renderField(ObjectTemplate template, Element element, AbstractObject obj, FieldTemplate field, Properties properties, FieldView view) {
		
		if(field.getCollection()) {
//			renderColletion(template, element, obj, field, properties, view); 
		} else {
			switch(field.getFieldType()) {
			case OBJECT_EMBEDDED:
				
				break;
			case OBJECT_REFERENCE:
				
				break;
			default:
				renderFormField(template, element, obj, field, properties, view);
			}
			
		}
	}
	
	private void renderFormField(ObjectTemplate template, Element element, AbstractObject obj, FieldTemplate field,
			Properties properties, FieldView view) {
		
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
		
		FieldInputRender render;
		switch(field.getFieldType()) {
		case TEXT:
			render = new TextFormInput(template, field);
			break;
		case TEXT_AREA:
			render = new TextAreaFormInput(template, field);
			break;
		case PASSWORD:
			render = new PasswordFormInput(template, field);
			break;
		case TIMESTAMP:
			render = new TimestampFormInput(template, field);
			break;
		case BOOL:
			render = new BooleanFormInput(template, field);
			break;
		case ENUM:
		{
			Class<?> values;
			try {
				values = classLoader.findClass(field.getValidationValue(ValidationType.OBJECT_TYPE));
				render = new DropdownFormInput(template, field, (Enum<?>[])values.getEnumConstants());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
			
			break;
		}
		case DECIMAL:
		case INTEGER:
		case LONG:
			render = new NumberFormInput(template, field);
			break;
		case OBJECT_EMBEDDED:
		case OBJECT_REFERENCE:
			throw new IllegalStateException("Object cannot be rendered by renderField");
		default:
			throw new IllegalStateException("Missing field type " + field.getFieldType().name());
		}
		
		render.renderInput(element, getDefaultValue(field, obj));
		
		Elements thisElement = element.select("#" + field.getResourceKey());
		if(field.isRequired()) {
			thisElement.attr("required", "required");
		}
		if(field.isReadOnly() || view == FieldView.READ) {
			thisElement.attr("readonly", "readonly");
		}
	}

	private String getDefaultValue(FieldTemplate field, AbstractObject obj) {
		String defaultValue = field.getDefaultValue();
		if(Objects.nonNull(obj)) {
			defaultValue = String.valueOf(obj.getValue(field));
		}
		return defaultValue;
	}

	private void orderFields(Element rootElement, ObjectTemplate template, AbstractObject obj, Properties properties, FieldView currentView) {
		
		List<OrderedView> views = templateService.getViews(template);
		
		boolean first = true;
		for(OrderedView view : views) {
			
			Element viewElement = createViewElement(view, rootElement, template, first && !view.isRoot());
			
			if(!view.isRoot()) {
				first = false;
			}
			
			for(OrderedField orderedField : view.getFields()) {
				FieldTemplate field = orderedField.getField();
				if(field.isHidden()) {
					continue;
				}
				switch(field.getFieldType()) {
				default:
					renderField(template, viewElement, obj, field, properties, currentView);
					break;
				}
			}
		}
		
		
	}

	private Element createViewElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
		if(view.isRoot()) {
			return rootElement.prepend("<div id=\"rootView\" class=\"col-12 py-1\"></div>").select("#rootView").first();
		}
		
		switch(view.getType()) {
		case ACCORDION:
			
			break;
		default:
			return createTabElement(view, rootElement, template, first);
		}
		return rootElement;
	}

	private Element createTabElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
		Element list = rootElement.select("ul").first();
		
		list.append(String.format(
				"<li class=\"nav-item\"><a class=\"nav-link\" data-toggle=\"tab\" href=\"#%s\" webbits:bundle=\"i18n/%s\" webbits:i18n=\"%s.name\"></a></li>",
						view.getResourceKey(),
						template.getResourceKey(),
						view.getResourceKey()));

		rootElement.select(".tab-content").append(String.format(
				"<div id=\"%s\" class=\"tab-pane panel-body fade in\"></div>", view.getResourceKey()));
		
		Element tabPane = rootElement.select(".tab-pane").last();
		
		if(first) {
			list.select("li").last().addClass("active");
			list.select(".nav-link").first().addClass("active");
			tabPane.addClass("active");
		}
		
		return tabPane;
	}
	
	
}
