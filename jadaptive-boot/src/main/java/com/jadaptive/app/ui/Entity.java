package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.jsoup.select.Elements;
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
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.FieldDefinition;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.ui.renderers.form.DropdownFormInput;
import com.jadaptive.app.ui.renderers.form.SearchFormInput;

@Widget({ PageResources.class, PageResourcesElement.class })
@View(contentType = "text/html")
@Resource
public class Entity {

	@Autowired
	private SecurityPropertyService propertyService; 
	
	@Autowired
	private ClassLoaderService classLoader;
	
	Map<FieldType,String> DEFAULT_TEMPLATES = new HashMap<>();
	String COLLECTION_TEMPLATE;
	
	@PostConstruct
	private void postConstruct() {
		
		DEFAULT_TEMPLATES.put(FieldType.TEXT, loadTemplate("text"));
		DEFAULT_TEMPLATES.put(FieldType.TEXT_AREA, loadTemplate("textarea"));
		DEFAULT_TEMPLATES.put(FieldType.PASSWORD, loadTemplate("password"));
		DEFAULT_TEMPLATES.put(FieldType.BOOL, loadTemplate("bool"));
		DEFAULT_TEMPLATES.put(FieldType.DECIMAL, loadTemplate("decimal"));
		DEFAULT_TEMPLATES.put(FieldType.ENUM, loadTemplate("enum"));
		DEFAULT_TEMPLATES.put(FieldType.INTEGER, loadTemplate("integer"));
		DEFAULT_TEMPLATES.put(FieldType.LONG, loadTemplate("long"));
		DEFAULT_TEMPLATES.put(FieldType.TIMESTAMP, loadTemplate("timestamp"));

		COLLECTION_TEMPLATE = loadTemplate("collection");
	}
	
	private String loadTemplate(String name) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(
						String.format("/com/jadaptive/app/ui/templates/%s.html", name)), "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
    @Out
    public Elements service(@In Elements contents, @ParentView TemplatePage page) throws IOException {
    	
    	try {
			Properties properties = propertyService.getOverrideProperties(
					SecurityScope.TENANT, 
					page.getTemplate().getResourceKey() + ".properties");
			
			boolean readOnly = page.isReadOnly();
			
			AbstractObject object = null;
			if(page instanceof ObjectPage) {
				object = ((ObjectPage)page).getObject();
			}
			renderObject(contents, page.getTemplate(), object, properties, readOnly);
			return contents;
		} catch (IOException e) {
			throw new IOException(e.getMessage(), e);
		}
    	
	
    }

	private void renderObject(Elements contents, ObjectTemplate template, AbstractObject entity, Properties properties, boolean readOnly) {
		
		contents.append("<form id=\"entityForm\" class=\"row\"></form>");
		Elements parentElement = contents.select("#entityForm");
		
		List<FieldDefinition> objects = new ArrayList<>();
		List<FieldDefinition> fields = new ArrayList<>();
		orderFields(objects, fields, template, properties);
		
		renderFields(fields, entity, parentElement, properties, readOnly);
		renderObjects(objects, entity, parentElement, properties, readOnly);
	}

	private void renderObjects(List<FieldDefinition> objects, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {
		
		for(FieldDefinition object : objects) {
			renderObject(parentElement, entity, object, readOnly);
		}
	}

	private void renderFields(List<FieldDefinition> fields, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {
		
		for(FieldDefinition field : fields) {
			renderField(parentElement, entity, field, properties, readOnly);
		}
	}

	private void renderField(Elements parentElement, AbstractObject entity, FieldDefinition fieldTemplate, Properties properties, boolean readOnly) {
		
		if(fieldTemplate.getCollection()) {
			renderColletion(parentElement, entity, fieldTemplate, properties, readOnly); 
		} else {
			renderFormField(parentElement, entity, fieldTemplate, properties, readOnly);
		}
	}
	
	private void renderFormField(Elements parentElement, AbstractObject entity, FieldDefinition fieldTemplate,
			Properties properties, boolean readOnly) {
		
		switch(fieldTemplate.getFieldType()) {
		case TEXT:
			renderTextBox(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case TEXT_AREA:
			renderTextArea(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case PASSWORD:
			renderPassword(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case TIMESTAMP:
			renderDate(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case BOOL:
			renderBool(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case ENUM:
			renderEnum(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case DECIMAL:
		case INTEGER:
		case LONG:
			renderNumber(fieldTemplate, entity, parentElement, properties, readOnly);
			break;
		case OBJECT_EMBEDDED:
		case OBJECT_REFERENCE:
			throw new IllegalStateException("Object cannot be rendered by renderField");
		default:
			throw new IllegalStateException("Missing field type " + fieldTemplate.getFieldType().name());
		}
	}

	private void renderColletion(Elements parentElement, AbstractObject entity, FieldDefinition fieldTemplate,
			Properties properties, boolean readOnly) {
		
		new SearchFormInput(parentElement, fieldTemplate, getDefaultValue(fieldTemplate, entity));
	}

	private void renderTextBox(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		String templateHtml = getTemplate(fieldTemplate, properties);
		addElement(templateHtml, fieldTemplate, entity, parentElement, readOnly);
	}
	
	private void renderPassword(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		String templateHtml = getTemplate(fieldTemplate, properties);
		addElement(templateHtml, fieldTemplate, entity, parentElement, readOnly);
	}
	
	private void renderEnum(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		try {
			Class<?> values = classLoader.resolveClass(
					fieldTemplate.getValidationValue(ValidationType.OBJECT_TYPE));
			
			new DropdownFormInput(parentElement, fieldTemplate, getDefaultValue(fieldTemplate, entity))
						.renderValues((Enum<?>[])values.getEnumConstants());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private String getDefaultValue(FieldDefinition fieldTemplate, AbstractObject entity) {
		String defaultValue = fieldTemplate.getDefaultValue();
		if(Objects.nonNull(entity)) {
			defaultValue = String.valueOf(entity.getValue(fieldTemplate));
		}
		return defaultValue;
	}

	private void renderBool(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		String templateHtml = getTemplate(fieldTemplate, properties);
		Elements elements = addElement(templateHtml, fieldTemplate, entity, parentElement, readOnly);
		if(Objects.nonNull(entity)) {
			if("true".equalsIgnoreCase(entity.getValue(fieldTemplate).toString())) {
				elements.select("#" + fieldTemplate.getResourceKey()).attr("checked", "checked");
			}
		}
	}
	
	private void renderNumber(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		String templateHtml = getTemplate(fieldTemplate, properties);
		addElement(templateHtml, fieldTemplate, entity, parentElement, readOnly);
	}
	
	private void renderDate(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		String templateHtml = getTemplate(fieldTemplate, properties);
		addElement(templateHtml, fieldTemplate, entity, parentElement, readOnly);
	}
	
	private void renderTextArea(FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, Properties properties, boolean readOnly) {

		String templateHtml = getTemplate(fieldTemplate, properties);
		addElement(templateHtml, fieldTemplate, entity, parentElement, readOnly);
	}
	
	private String getTemplate(FieldDefinition fieldTemplate, Properties properties) {
		return DEFAULT_TEMPLATES.get(fieldTemplate.getFieldType());
	}

	private Elements addElement(String templateHtml, FieldDefinition fieldTemplate, AbstractObject entity, Elements parentElement, boolean readOnly) {
		
		if(Objects.isNull(templateHtml)) {
			return null;
		}
		
		templateHtml = templateHtml.replace("${resourceKey}", fieldTemplate.getResourceKey());
		templateHtml = templateHtml.replace("${name}", fieldTemplate.getName());
		templateHtml = templateHtml.replace("${description}", fieldTemplate.getDescription());
		if(Objects.nonNull(entity)) {
			templateHtml = templateHtml.replace("${value}", entity.getValue(fieldTemplate).toString());
		} else {
			templateHtml = templateHtml.replace("${value}", fieldTemplate.getDefaultValue());
		}
		
		parentElement.append(templateHtml);
		
		Elements thisElement = parentElement.select("#" + fieldTemplate.getResourceKey());
		if(fieldTemplate.isRequired()) {
			thisElement.attr("required", "required");
		}
		if(fieldTemplate.isReadOnly() || readOnly) {
			thisElement.attr("readonly", "readonly");
		}
		
		return thisElement;
	}

	private void renderObject(Elements parentElement, AbstractObject entity, FieldDefinition object, boolean readOnly) {
		
		
	}

	private void orderFields(List<FieldDefinition> objects, List<FieldDefinition> fields, ObjectTemplate template, Properties properties) {
		
		for(FieldDefinition field : template.getFields()) {
			if(field.isHidden()) {
				continue;
			}
			switch(field.getFieldType()) {
			case OBJECT_REFERENCE:
				if(field.getCollection()) {
					fields.add(field);
				} else {
					objects.add(field);
				}
				break;
			case OBJECT_EMBEDDED:
				objects.add(field);
				break;
			default:
				fields.add(field);
				break;
			}
		}
	}
}
