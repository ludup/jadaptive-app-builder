package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.PostConstruct;

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
		
		contents.append("<form id=\"entityForm\" class=\"row\"></form>");
		Elements element = contents.select("#entityForm");
		
		List<FieldTemplate> objects = new ArrayList<>();
		List<FieldTemplate> fields = new ArrayList<>();
		orderFields(objects, fields, template, properties);
		
		renderFields(template, fields, obj, element, properties, view);
		renderObjects(template, objects, obj, element, properties, view);
	}

	private void renderObjects(ObjectTemplate template, List<FieldTemplate> objects, AbstractObject obj, Elements element, Properties properties, FieldView view) {
		
		for(FieldTemplate object : objects) {
			renderObject(element, obj, object, view);
		}
	}

	private void renderFields(ObjectTemplate template, List<FieldTemplate> fields, AbstractObject obj, Elements element, Properties properties, FieldView view) {
		
		for(FieldTemplate field : fields) {
			renderField(template, element, obj, field, properties, view);
		}
	}

	private void renderField(ObjectTemplate template, Elements element, AbstractObject obj, FieldTemplate field, Properties properties, FieldView view) {
		
		if(field.getCollection()) {
//			renderColletion(template, element, obj, field, properties, view); 
		} else {
			renderFormField(template, element, obj, field, properties, view);
		}
	}
	
	private void renderFormField(ObjectTemplate template, Elements element, AbstractObject obj, FieldTemplate field,
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

	private void renderObject(Elements element, AbstractObject obj, FieldTemplate field, FieldView view) {
		
		
	}

	private void orderFields(List<FieldTemplate> objects, List<FieldTemplate> fields, ObjectTemplate template, Properties properties) {
		
		for(FieldTemplate field : template.getFields()) {
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
