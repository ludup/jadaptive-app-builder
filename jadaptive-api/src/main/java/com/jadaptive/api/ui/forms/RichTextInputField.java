package com.jadaptive.api.ui.forms;

import static com.jadaptive.utils.Npm.scripts;
import static com.jadaptive.utils.Npm.stylesheets;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class RichTextInputField extends AbstractInputField implements InputField {

	public RichTextInputField(String bundle, String resourceKey, String formVariable, boolean readonly) {
		super(bundle, resourceKey, formVariable, readonly);
	}

	@Override
	protected void doInput(Element e, String value) {
		e.selectFirst(".htmlEditor textarea")
			.attr("name", formVariable + "HtmlEditor")
			.attr("id", resourceKey + "HtmlEditor");
		
		e.selectFirst(".richEditor textarea")
		.attr("name", formVariable + "RichText")
		.attr("id", resourceKey + "RichText");

	}

	@Override
	public void render(Document document, Element e, String value) throws IOException {
		configureDocument(document);
		super.render(document, e, value);
	}

	public void configureDocument(Document document) {
		
		scripts(document, "codemirror", 
				"lib/codemirror.js",
				"addon/display/autorefresh.js",
				"mode/xml/xml.js",
				"mode/javascript/javascript.js",
				"mode/css/css.js",
				"mode/htmlmixed/htmlmixed.js"
		);
		
		stylesheets(document, "codemirror", "lib/codemirror.css");
		
		scripts(document, "tinymce", 
				"tinymce.min.js"
		);
		
		PageHelper.addContentSecurityPolicy("style-src", SessionUtils.UNSAFE_INLINE);
		
		String tinyMCEScript = "$(function() { \n" 
				+ " tinymce.init({\n"
				+ "   mode : '" + resourceKey + "',"
				+ "	  selector: '#" + resourceKey + "', \n"
				+ "	  plugins: '',\n"
				+ "   toolbar: 'undo redo print spellcheckdialog formatpainter | blocks fontfamily fontsize | bold italic underline forecolor backcolor | link image | alignleft aligncenter alignright alignjustify',\n"
				+ "	  license_key: 'gpl',\n"
				+ "	  promotion: false,\n"
				+ "	  branding: false,\n"
				+ "   advcode_inline: true\n"
				+ "	}); \n"
				+ "	}); \n";
		
		PageHelper.appendBodyScriptSnippet(document, tinyMCEScript);
		
		String script = "$(function() {\n$('#" + resourceKey + "').val(window.atob($('#" + resourceKey + "').val()));\r\n"
				+ "var " + formVariable + "Editor = CodeMirror.fromTextArea(document.getElementById('" + resourceKey + "'), {\r\n"
				+ "    lineNumbers: true,\r\n"
				+ "    lineWrapping: true,\r\n"
				+ "    readOnly: " + String.valueOf(readonly) + ",\r\n"
				+ "    mode:  'htmlmixed'\r\n"
				+ "});\r\n"
				+ resourceKey + "Editor.refresh();\r\n"
				+ formVariable + "Editor.on('change', function(e) {\r\n"
				+ "  const text = e.doc.getValue();\r\n"
				+ "  $('#"  + formVariable + "').val(text);\r\n"
				+ "});\r\n"
		+ "});\r\n";

		PageHelper.appendBodyScriptSnippet(document, script);
	}


}
