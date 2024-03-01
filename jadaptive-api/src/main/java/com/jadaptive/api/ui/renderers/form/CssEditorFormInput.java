package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;

@PageDependencies(extensions = {"codemirror"})
public class CssEditorFormInput extends FieldInputRender {

	private Document document;
	private boolean readOnly;

	public CssEditorFormInput(TemplateViewField field, Document document, boolean readOnly) {
		super(field);
		this.document = document;
		this.readOnly = readOnly;
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {


		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/lib/codemirror.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/addon/display/autorefresh.js");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/mode/css/css.js");
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
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())))
				.appendChild(input = new Element("textarea")
						.attr("name", getFormVariableWithParents())
						.addClass(getResourceKey() + " form-control")
						.val(value))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));

		if(!disableIDAttribute) {
			input.attr("id", getResourceKey());
		}
		
		String script = "$(function() {\nvar " + getFormVariable() + "Editor = CodeMirror.fromTextArea(document.getElementById('" + getResourceKey() + "'), {\r\n"
				+ "    lineNumbers: true,\r\n"
				+ "    autoRefresh:true,\r\n"
				+ "    lineWrapping: true,\r\n"
				+ "    readOnly: " + String.valueOf(readOnly) + ",\r\n"
				+ "    mode:  'css'\r\n"
				+ "  });\r\n"
				+ getResourceKey() + "Editor.refresh();\r\n"
				+ getFormVariable() + "Editor.on('change', function(e) {\r\n"
				+ "  const text = e.doc.getValue();\r\n"
				+ "  $('#"  + getFormVariable() + "').val(text);\r\n"
				+ "});\r\n});";
		
		PageHelper.appendBodyScriptSnippet(document, script);
		
	}
}
