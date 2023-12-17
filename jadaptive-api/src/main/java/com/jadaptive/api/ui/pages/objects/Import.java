package com.jadaptive.api.ui.pages.objects;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Component
@RequestPage(path = "import/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils" })
@PageProcessors(extensions = { "freemarker", "i18n" })
public class Import extends ObjectTemplatePage {

	@Override
	public String getUri() {
		return "import";
	}

	@Override
	public FieldView getScope() {
		return FieldView.IMPORT;
	}

	@Override
	protected void doGenerateTemplateContent(Document document) throws FileNotFoundException, IOException {
		
		super.doGenerateTemplateContent(document);
		
		Element e = document.selectFirst("#csvColumns");
		Element select;
		e.appendChild(Html.div("row", "mt-3", "csvColumn")
				.appendChild(Html.div("col-10")
						.appendChild(select = new Element("select")
								.addClass("orderedFields form-control")
								.attr("id", "templateFields1")))
				.appendChild(Html.div("col-2")
						.appendChild(Html.a("#", "deleteColumn")
						.appendChild(Html.i("fa-solid", "fa-trash", "")))));
		
		select.appendChild(Html.option("", "")
				.attr("jad:bundle", "userInterface")
				.attr("jad:i18n", "notImported.name"));
		
		select.appendChild(Html.option("uuid", "uuid")
				.attr("jad:bundle", "userInterface")
				.attr("jad:i18n", "uuid.name"));
		
		
		iterateFields(template, select, "");
	}

	private void iterateFields(ObjectTemplate template, Element select, String formVariablePrefix) {
		
		Collection<FieldTemplate> deferred = new ArrayList<>();
		for(FieldTemplate field : template.getFields()) {
			switch(field.getResourceKey()) {
			case "created":
			case "lastModified":
				continue;
			default:
				if(field.getFieldType()==FieldType.OBJECT_EMBEDDED) {
					deferred.add(field);
				} else {
					addField(select, field, template, formVariablePrefix);
				}
			}
		}
		
		for(FieldTemplate field : deferred) {
			addField(select, field, template, formVariablePrefix);
		}
	}
	private void addField(Element e, FieldTemplate field, ObjectTemplate template, String formVariablePrefix) {
		
		switch(field.getFieldType()) {
		case OBJECT_EMBEDDED:
			
			String resourceKey = field.getValidationValue(ValidationType.RESOURCE_KEY);
			ObjectTemplate embeddedTemplate = templateService.get(resourceKey);
			
			StringBuffer tmp = new StringBuffer();
			tmp.append(formVariablePrefix);
			tmp.append(field.getFormVariable());
			tmp.append(".");
			
			e.appendChild(Html.option(formVariablePrefix + field.getFormVariable(), field.getResourceKey())
					.attr("disabled", "true")
					.attr("jad:bundle", template.getBundle())
					.attr("jad:i18n", field.getResourceKey() + ".name"));
			
			iterateFields(embeddedTemplate, e, tmp.toString());

			break;
		case OBJECT_REFERENCE:
			
			e.appendChild(Html.option(formVariablePrefix + field.getFormVariable() + "Text", "")
					.text(I18N.getResource(template.getBundle(), field.getResourceKey() + ".name") + " Name"));
			
			e.appendChild(Html.option(formVariablePrefix + field.getFormVariable(), "")
					.text(I18N.getResource(template.getBundle(), field.getResourceKey() + ".name") + " UUID"));

			break;
		default:
		
			e.appendChild(Html.option(formVariablePrefix + field.getFormVariable(), field.getResourceKey())
					.attr("jad:bundle", template.getBundle())
					.attr("jad:i18n", field.getResourceKey() + ".name"));
		}
		
	}
	
	@Override
	protected String getCancelURI() {
		return "/app/ui/search/" + resourceKey;
	}
}
