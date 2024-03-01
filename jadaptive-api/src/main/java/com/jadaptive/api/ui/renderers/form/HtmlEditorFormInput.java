package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Base64;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;

@PageDependencies(extensions = {"codemirror"})
public class HtmlEditorFormInput extends FieldInputRender {

	private Document document;
	private boolean readOnly;

	public HtmlEditorFormInput(TemplateViewField field, Document document, boolean readOnly) {
		super(field);
		this.document = document;
		this.readOnly = readOnly;
	}
	
	public HtmlEditorFormInput(Document document, String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
		this.document = document;
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
	
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/lib/codemirror.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/addon/display/autorefresh.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/mode/xml/xml.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/mode/javascript/javascript.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/mode/css/css.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/mode/htmlmixed/htmlmixed.js");
		PageHelper.appendStylesheet(document, "/app/content/npm2mvn/npm/codemirror/current/lib/codemirror.css");
				
		Element input;
		
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.addClass("w-100")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey)))
				.appendChild(input = new Element("textarea")
						.attr("name", getFormVariableWithParents())
						.addClass(getResourceKey() + " form-control")
						.val(Base64.getEncoder().encodeToString(value.getBytes("UTF-8"))))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.desc", resourceKey)))));

		if(!disableIDAttribute) {
			input.attr("id", getResourceKey());
		}
		String script = "$(function() {\n$('#" + resourceKey + "').val(window.atob($('#" + resourceKey + "').val()));\r\n"
								+ "var " + getFormVariable() + "Editor = CodeMirror.fromTextArea(document.getElementById('" + resourceKey + "'), {\r\n"
								+ "    lineNumbers: true,\r\n"
								+ "    lineWrapping: true,\r\n"
								+ "    readOnly: " + String.valueOf(readOnly) + ",\r\n"
								+ "    mode:  'htmlmixed'\r\n"
								+ "  });\r\n"
								+ resourceKey + "Editor.refresh();\r\n"
								+ getFormVariable() + "Editor.on('change', function(e) {\r\n"
								+ "  const text = e.doc.getValue();\r\n"
								+ "  $('#"  + getFormVariable() + "').val(text);\r\n"
								+ "});\r\n});";
		
		PageHelper.appendBodyScriptSnippet(document, script);
	}

}
