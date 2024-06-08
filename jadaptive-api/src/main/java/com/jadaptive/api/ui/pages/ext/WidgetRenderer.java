package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.ObjectPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageExtension;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;
import com.jadaptive.api.ui.renderers.form.BooleanFormInput;
import com.jadaptive.api.ui.renderers.form.DateFormInput;
import com.jadaptive.api.ui.renderers.form.DropdownFormInput;
import com.jadaptive.api.ui.renderers.form.NumberFormInput;
import com.jadaptive.api.ui.renderers.form.PasswordFormInput;
import com.jadaptive.api.ui.renderers.form.SwitchFormInput;
import com.jadaptive.api.ui.renderers.form.TextAreaFormInput;
import com.jadaptive.api.ui.renderers.form.TextFormInput;
import com.jadaptive.api.ui.renderers.form.TimestampFormInput;

@Component
public class WidgetRenderer implements PageExtension {

	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private InternationalService internationalService;
	
	@Autowired
	private ClassLoaderService classService; 
	
	@Override
	public String getHtmlResource() {
		return null;
	}

	@Override
	public String getCssResource() {
		return null;
	}

	@Override
	public String getJsResource() {
		return null;
	}

	@Override
	public void process(Document document, Element extensionElement, Page page) throws IOException {
		
		
		for(Element e : document.getElementsByAttribute("jad:widget")) {
			
			String widgetName = e.attr("jad:widget");
			String name = e.attr("jad:name");
			String formVar = Objects.toString(e.attr("jad:formVar"), name);
			String bundle = Objects.toString( e.attr("bundle"), getBundle(page));
			String value  = getValue(page, e, name, formVar);
			boolean readOnly = getReadOnly(e);
		
			switch(widgetName) {
			case "text":
				new TextFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "textarea":
				new TextAreaFormInput(name, formVar, bundle, Integer.parseInt(e.attr("jad:rows"))).renderInput(e, value);
				break;
			case "number":
				new NumberFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "switch":
				new SwitchFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "checkbox":
				new BooleanFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "date":
				new DateFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "timestamp":
				new TimestampFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "password":
				new PasswordFormInput(name, formVar, bundle).renderInput(e, value);
				break;
			case "enum":
				Class<?> values;
				try {
					values = classService.findClass(e.attr("jad:class"));
					DropdownFormInput render = new DropdownFormInput(name, formVar, bundle);
					render.renderInput(e, value);
					render.renderValues((Enum<?>[])values.getEnumConstants(), value, readOnly);
				} catch (ClassNotFoundException e2) {
					throw new IllegalStateException(e2.getMessage(), e2);
				}
				break;
			case "country":
				DropdownFormInput dropdown = new DropdownFormInput(name, formVar, bundle);
				dropdown.renderInput(e, "");
				for(Country country : internationalService.getCountries()) {
					dropdown.addInputValue(country.getCode(), country.getName());
				}
				String code = getValue(page, e, name, formVar);
				if(StringUtils.isNotBlank(code)) {
					dropdown.setSelectedValue(code, internationalService.getCountryName(code));
				}
				break;
			default:
				throw new IllegalStateException("Unsupported widget " + widgetName);
			}
		}
	}
	
	private boolean getReadOnly(Element e) {
		
		String v = e.attr("jad:readOnly");
		if(Objects.nonNull(v)) {
			return "true".equals(v);
		}
		return false;
	}

	String getBundle(Page page) {
		if(page instanceof ObjectTemplatePage) {
			return ((ObjectTemplatePage)page).getTemplate().getBundle();
		}
		return "default";
	}
	
	String getValue(Page page, Element e, String name, String formVar) {
		
		if(page instanceof ObjectPage) {
			ObjectPage objectPage = (ObjectPage) page;
			ObjectTemplate template = templateService.get(objectPage.getObject().getResourceKey());
			FieldTemplate field = template.getField(name);
			if(Objects.isNull(field)) {
				return (String) objectPage.getObject().getValue(formVar);
			} else {
				switch(field.getFieldType()) {
				case BOOL:
				case DATE:
				case DECIMAL:
				case ENUM:
				case INTEGER:
				case LONG:
				case PASSWORD:
				case TEXT:
				case TEXT_AREA:
				case TIME:
				case TIMESTAMP:
				case COUNTRY:
					return (String) objectPage.getObject().getValue(formVar);
				case OBJECT_REFERENCE:
				case FILE:
				case ATTACHMENT:
				case IMAGE:
				case OBJECT_EMBEDDED:
				case OPTIONS:
				case PERMISSION:
				default:
					return "";
				}
			}
		} else {
			return StringUtils.defaultIfBlank((String)e.attr("jad:value"), "");
		}
	}

	@Override
	public String getName() {
		return "widgets";
	}

}
