package com.jadaptive.api.setup;

import java.io.IOException;

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
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.utils.Utils;

public class ObjectSection extends SetupSection {

	public ObjectSection(String bundle, int weight) {
		super(bundle, bundle, weight);
	}
	
	public ObjectSection(String bundle, String name, int weight) {
		super(bundle, name, weight);
	}
	
	public boolean isSystem() {
		return false;
	}

	@Override
	public void processReview(Document document, WizardState state) {
		
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
		for(FieldTemplate field : template.getFields()) { 
		
			if(field.isHidden()) {
				continue;
			}
			
			String value = object.getValue(field).toString();
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
								.text(Utils.maskingString(value, 2, "*")))));
				break;
			case OBJECT_REFERENCE:
				break;
			case OPTIONS:
				// TODO option
				break;
			case COUNTRY:
				value = ApplicationServiceImpl.getInstance().getBean(InternationalService.class).getCountryName(value);
				// Purposely NOT breaking to render value below
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
								.text(value))));
				break;
			}
			
		}
	}
	
	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
		document.selectFirst("body").appendChild(Html.div("col-12")
				.attr("id", "feedback"))
			.appendChild(Html.div("col-12")
					.attr("jad:id", "objectRenderer")
					.attr("jad:handler", "setup")
					.attr("jad:resourceKey", getName()));

	}

}
