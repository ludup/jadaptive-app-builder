package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;

@PageDependencies(extensions = {"codemirror"})
public class CssEditorFormInput extends FieldInputRender {

	private Document document;
	private boolean readOnly;

	public CssEditorFormInput(ObjectTemplate template, TemplateViewField field, Document document, boolean readOnly) {
		super(template, field);
		this.document = document;
		this.readOnly = readOnly;
	}

	@Override
	public void renderInput(Element rootElement, String value) throws IOException {

		StringBuffer formVariable = new StringBuffer();
		
		if(Objects.nonNull(field.getParentFields())) {
			for(FieldTemplate t : field.getParentFields()) {
				formVariable.append(t.getResourceKey());
				formVariable.append(".");
			}
		}
		
		formVariable.append(field.getFormVariable());
		
		PageHelper.appendHeadScript(document, "/app/content/codemirror/lib/codemirror.js");
		PageHelper.appendHeadScript(document, "/app/content/codemirror/addon/display/autorefresh.js");
		PageHelper.appendHeadScript(document, "/app/content/codemirror/mode/css/css.js");
		PageHelper.appendStylesheet(document, "/app/content/codemirror/lib/codemirror.css");
				

		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.addClass("w-100")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("textarea")
						.attr("id", formVariable.toString())
						.attr("name", formVariable.toString())
						.addClass("form-control")
						.val(value))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));

		String script = "$(function() {\nvar " + field.getFormVariable() + "Editor = CodeMirror.fromTextArea(document.getElementById('" + field.getResourceKey() + "'), {\r\n"
				+ "    lineNumbers: true,\r\n"
				+ "    autoRefresh:true,\r\n"
				+ "    lineWrapping: true,\r\n"
				+ "    readOnly: " + String.valueOf(readOnly) + ",\r\n"
				+ "    mode:  'css'\r\n"
				+ "  });\r\n"
				+ field.getResourceKey() + "Editor.refresh();\r\n"
				+ field.getFormVariable() + "Editor.on('change', function(e) {\r\n"
				+ "  const text = e.doc.getValue();\r\n"
				+ "  $('#"  +field.getFormVariable() + "').val(text);\r\n"
				+ "});\r\n});";
		
		PageHelper.appendBodyScriptSnippet(document, script);
		
	}
}
