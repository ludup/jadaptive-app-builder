package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Base64;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;

@PageDependencies(extensions = {"codemirror"})
public class HtmlEditorFormInput extends FieldInputRender {

	private Document document;
	private boolean readOnly;

	public HtmlEditorFormInput(ObjectTemplate template, OrderedField field, Document document, boolean readOnly) {
		super(template, field);
		this.document = document;
		this.readOnly = readOnly;
	}

	@Override
	public void renderInput(OrderedView panel, Element rootElement, String value) throws IOException {

		PageHelper.appendScript(document, "/app/content/codemirror/lib/codemirror.js");
		PageHelper.appendScript(document, "/app/content/codemirror/addon/display/autorefresh.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/xml/xml.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/javascript/javascript.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/css/css.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/htmlmixed/htmlmixed.js");
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
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.addClass("form-control")
						.val(Base64.getEncoder().encodeToString(value.getBytes("UTF-8"))))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));

		String script = "$(function() {\n$('#" + field.getResourceKey() + "').val(window.atob($('#" + field.getResourceKey() + "').val()));\r\n"
								+ "var " + field.getFormVariable() + "Editor = CodeMirror.fromTextArea(document.getElementById('" + field.getResourceKey() + "'), {\r\n"
								+ "    lineNumbers: true,\r\n"
								+ "    lineWrapping: true,\r\n"
								+ "    readOnly: " + String.valueOf(readOnly) + ",\r\n"
								+ "    mode:  'htmlmixed'\r\n"
								+ "  });\r\n"
								+ field.getResourceKey() + "Editor.refresh();\r\n"
								+ field.getFormVariable() + "Editor.on('change', function(e) {\r\n"
								+ "  const text = e.doc.getValue();\r\n"
								+ "  $('#"  +field.getFormVariable() + "').val(text);\r\n"
								+ "});\r\n});";
		
		PageHelper.appendScriptSnippet(document, script);
	}

}
