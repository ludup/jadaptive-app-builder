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

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.ParentView;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
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
import com.jadaptive.api.template.ViewType;
import com.jadaptive.app.ui.renderers.form.BooleanFormInput;
import com.jadaptive.app.ui.renderers.form.DropdownFormInput;
import com.jadaptive.app.ui.renderers.form.FieldInputRender;
import com.jadaptive.app.ui.renderers.form.NumberFormInput;
import com.jadaptive.app.ui.renderers.form.PasswordFormInput;
import com.jadaptive.app.ui.renderers.form.TextAreaFormInput;
import com.jadaptive.app.ui.renderers.form.TextFormInput;
import com.jadaptive.app.ui.renderers.form.TimestampFormInput;
import com.jadaptive.utils.Utils;

@Widget
@View(contentType = "text/html")
@ClasspathResource
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
    public Element service(@In Element contents, @ParentView TemplatePage page, Request<?> request) throws IOException {
    	
    	try {
			Properties properties = propertyService.getOverrideProperties(
					SecurityScope.TENANT, 
					page.getTemplate().getResourceKey() + ".properties");
			
			AbstractObject object = null;
			if(page instanceof ObjectPage) {
				object = ((ObjectPage)page).getObject();
			}
			
			contents.append("<form id=\"entity\"><div class=\"row\"></div></form>");
			List<OrderedView> views = templateService.getViews(page.getTemplate());
			
			
			contents.append("<p>\n" + 
					"  <a class=\"btn btn-primary\" data-toggle=\"collapse\" href=\"#collapseExample\" role=\"button\" aria-expanded=\"false\" aria-controls=\"collapseExample\">\n" + 
					"    Link with href\n" + 
					"  </a>\n" + 
					"  <button class=\"btn btn-primary\" type=\"button\" data-toggle=\"collapse\" data-target=\"#collapseExample\" aria-expanded=\"false\" aria-controls=\"collapseExample\">\n" + 
					"    Button with data-target\n" + 
					"  </button>\n" + 
					"</p>\n" + 
					"<div class=\"collapse\" id=\"collapseExample\">\n" + 
					"  <div class=\"card card-body\">\n" + 
					"    Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.\n" + 
					"  </div>\n" + 
					"</div>");
			createViews(views, contents.select(".row").first(), page.getTemplate(), object, properties, page.getScope());
			return contents;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage(), e);
		}
    }

	private void createViews(List<OrderedView> views, Element element, ObjectTemplate template, AbstractObject obj, Properties properties, FieldView currentView) {
		
		int tabIndex = hasTabbedView(views);
		int acdIndex = hasAccordionView(views);
		
		if(tabIndex > -1) {
			createTabOutline(element);
		}
		
		if(acdIndex > -1) {
			createAccordionOutline(element, template, tabIndex < acdIndex);
		}

		boolean first = true;
		for(OrderedView view : views) {
			
			Element viewElement = createViewElement(view, element, template, first && !view.isRoot());
			
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
			
			if(!view.getChildViews().isEmpty()) {
				createViews(view.getChildViews(), element, template, obj, properties, currentView);
			}
		}
	}
	
	private void createTabOutline(Element element) {
		element.append("<ul class=\"nav nav-tabs pt-4\"></ul><div class=\"tab-content panel col-12 py-4\"></div>");
	}
	
	private void createAccordionOutline(Element element, ObjectTemplate template, boolean append) {
		String outline = "<div class=\"accordion col-12\" id=\"" + template.getResourceKey() + "Accordion\"></div>";
		if(append) {
			element.append(outline);
		} else {
			element.prepend(outline);
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

	private void renderField(ObjectTemplate template, Element element, AbstractObject obj, FieldTemplate field, Properties properties, FieldView view) {
		
		if(field.getCollection()) {
//			renderColletion(template, element, obj, field, properties, view); 
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

	private Element createViewElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
		if(view.isRoot()) {
			return rootElement.prepend("<div id=\"rootView\" class=\"col-12 py-1\"></div>").select("#rootView").first();
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
			.attr("webbits:bundle", "i18n/" + template.getResourceKey())
			.attr("webbits:i18n", view.getResourceKey());

		/* Collapsible Body Container */
		Element outer = card.appendElement("div")
			.attr("id", "collapse" + view.getResourceKey())
			.attr("aria-labelledby", view.getResourceKey())
			.attr("data-parent", "#" + template.getResourceKey() + "Accordion")
			.addClass("collapse");
		if (first)
			outer.addClass("show");
		
		/* Card Body */
		return outer.appendElement("div")
			.addClass("card-body");

	}

	private Element createTabElement(OrderedView view, Element rootElement, ObjectTemplate template, boolean first) {
		
		Element list = rootElement.selectFirst("ul");
		
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
