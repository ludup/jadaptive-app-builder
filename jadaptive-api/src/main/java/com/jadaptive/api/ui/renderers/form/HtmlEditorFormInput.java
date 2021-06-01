package com.jadaptive.api.ui.renderers.form;

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
	public HtmlEditorFormInput(ObjectTemplate template, OrderedField field, Document document) {
		super(template, field);
		this.document = document;
	}

	@Override
	public void renderInput(OrderedView panel, Element rootElement, String value) {

		PageHelper.appendScript(document, "/app/content/codemirror/lib/codemirror.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/xml/xml.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/javascript/javascript.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/css/css.js");
		PageHelper.appendScript(document, "/app/content/codemirror/mode/htmlmixed/htmlmixed.js");
		PageHelper.appendStylesheet(document, "/app/content/codemirror/lib/codemirror.css");
				

		rootElement.appendChild(new Element("div")
				.addClass("form-group")
				.addClass("w-100")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("col-form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("textarea")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.addClass("form-control")
						.val(value))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))));

		rootElement.appendChild(new Element("script")
							.attr("type", "application/javascript")
							.text("CodeMirror.fromTextArea(document.getElementById('" + field.getResourceKey() + "'), {\n"
								+ "    lineNumbers: true,\n"
								+ "    lineWrapping: true,\n"
								+ "    mode:  'htmlmixed'\n"
								+ "  });"));
	}

}
