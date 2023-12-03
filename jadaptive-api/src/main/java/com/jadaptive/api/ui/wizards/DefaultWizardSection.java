package com.jadaptive.api.ui.wizards;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.Html;
import com.jadaptive.utils.Utils;

public class DefaultWizardSection extends WizardSection {

	public DefaultWizardSection(String bundle, Integer weight) {
		super(bundle, weight);
	}
	
	public DefaultWizardSection(String bundle, String name, Integer weight) {
		super(bundle, name, weight);
	}
	
	public DefaultWizardSection(String bundle, String name, String resource, Integer weight) {
		super(bundle, name, resource, weight);
	}
	
	protected Element renderObjectSection(Document document, String resourceKey) {
		
		Element object;
		document.selectFirst("body").appendChild(object = Html.div("row")
				.appendChild(Html.div("col-12")
				.attr("id", "feedback"))
			.appendChild(Html.div("col-12")
					.attr("jad:id", "objectRenderer")
					.attr("jad:renderer", "WIZARD")
					.attr("jad:handler", Wizard.getCurrentState().getResourceKey())
					.attr("jad:resourceKey", resourceKey)));
		
		return object;

	}
	
	protected void renderObjectReview(Document document, WizardState state) {
		
		Element content = document.selectFirst("#wizardContent");
		UUIDDocument uuidObject = state.getObject(this);
		ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(uuidObject.getResourceKey());
		
		Element row;
		content.appendChild(new Element("div")
				.addClass("col-12 w-100 my-3")
				.appendChild(new Element("h4")
					.attr("jad:i18n", String.format("review.%s.header", template.getResourceKey()))
					.attr("jad:bundle", template.getBundle()))
				.appendChild(new Element("p")
						.attr("jad:bundle", template.getBundle())
						.attr("jad:i18n", String.format("review.%s.desc", template.getResourceKey())))
				.appendChild(row = new Element("div")
					.addClass("row")));
		
		AbstractObject object = ApplicationServiceImpl.getInstance().getBean(ObjectService.class).toAbstractObject(uuidObject);
		renderObject(object, template, row);
		
		
	}
	
	private void renderObject(AbstractObject object, ObjectTemplate template, Element row) {
		for(FieldTemplate field : template.getFields()) { 
			
			if(field.isHidden()) {
				continue;
			}
			
			switch(field.getFieldType()) {
			case OBJECT_EMBEDDED:
			case OBJECT_REFERENCE:
				if(!field.getCollection()) {
					AbstractObject o = object.getChild(field);
					if(Objects.isNull(o)) {
						continue;
					}
				}
				break;
			default:
				
				Object v  = object.getValue(field);
				if(Objects.isNull(v)) {
					continue;
				}
				
				String value = v.toString();
				
				if(StringUtils.isBlank(value)) {
					continue;
				}
			}
			
			
			switch(field.getFieldType()) {
			case PASSWORD:
				row.appendChild(new Element("div")
						.addClass("col-3")
						.appendChild(new Element("span")
										.attr("jad:bundle", template.getBundle())
										.attr("jad:i18n", String.format("%s.name", field.getResourceKey()))))
					.appendChild(new Element("div")
						.addClass("col-9")
						.appendChild(new Element("span")
								.appendChild(new Element("strong")
								.text(Utils.maskingString(object.getValue(field).toString(), 2, "*")))));
				break;
			case OBJECT_REFERENCE:
			case OBJECT_EMBEDDED:
			case OPTIONS:
				
				if(field.getCollection()) {
					Collection<AbstractObject> values = object.getObjectCollection(field.getResourceKey());
					
					row.appendChild(new Element("div")
							.addClass("col-3")
							.appendChild(new Element("span")
											.attr("jad:bundle", template.getBundle())
											.attr("jad:i18n", String.format("%s.name", field.getResourceKey()))))
						.appendChild(new Element("div")
							.addClass("col-9")
							.appendChild(new Element("span")
									.appendChild(new Element("strong")
									.text(createObjectCSVString(values, field)))));
				} else {
					
					AbstractObject obj = object.getChild(field);
					if(Objects.nonNull(obj)) {
						renderObject(obj, 
								ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(obj.getResourceKey()), 
								row);
					}
				}
				break;
			case COUNTRY:

				row.appendChild(new Element("div")
						.addClass("col-3")
						.appendChild(new Element("span")
										.attr("jad:bundle", template.getBundle())
										.attr("jad:i18n", String.format("%s.name", field.getResourceKey()))))
					.appendChild(new Element("div")
						.addClass("col-9")
						.appendChild(new Element("span")
								.appendChild(new Element("strong")
								.text(ApplicationServiceImpl.getInstance().getBean(InternationalService.class)
										.getCountryName(object.getValue(field).toString())))));
				break;
				
			default:
				row.appendChild(new Element("div")
						.addClass("col-3")
						.appendChild(new Element("span")
										.attr("jad:bundle", template.getBundle())
										.attr("jad:i18n", String.format("%s.name", field.getResourceKey()))))
					.appendChild(new Element("div")
						.addClass("col-9")
						.appendChild(new Element("span")
								.appendChild(new Element("strong")
								.text(object.getValue(field).toString()))));
				break;
			}
			
		}
	}

	private String createObjectCSVString(Collection<AbstractObject> values, FieldTemplate field) {
		
		StringBuffer buf = new StringBuffer();
		for(AbstractObject value : values) {
			if(buf.length() > 0) {
				buf.append(", ");
			}
			buf.append(value.getValue("name"));
		}
		return buf.toString();
	}

	

}
