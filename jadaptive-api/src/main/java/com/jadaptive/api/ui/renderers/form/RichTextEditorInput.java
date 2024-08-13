package com.jadaptive.api.ui.renderers.form;

import static com.jadaptive.utils.Npm.scripts;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageHelper;

public class RichTextEditorInput extends FieldInputRender {

	private Document document;
	private boolean readOnly;

	public RichTextEditorInput(TemplateViewField field, Document document, boolean readOnly) {
		super(field);
		this.document = document;
		this.readOnly = readOnly;
	}
	
	public RichTextEditorInput(Document document, String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
		this.document = document;
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
				
		rootElement.appendChild(
				new Element("textarea")
					.attr("name", resourceKey)
					.attr("id", resourceKey)
					.addClass("row mb-3 mceEditor"));
		
		rootElement.addClass("mb-3");
		
		scripts(document, "tinymce", 
				"tinymce.min.js"
		);
		
		String tinyMCEScript = "$(function() { \n" 
		+ " tinymce.init({\n"
		+ "   mode : '" + resourceKey + "',"
		+ "	  selector: '#" + resourceKey + "', \n"
		+ "	  plugins: '',\n"
		+ "	  license_key: 'gpl',\n"
		+ "	  promotion: false,\n"
		+ "	  branding: false,\n"
		+ "	  menubar: 'edit insert view format table tools'\n"
		+ "	}); \n"
		+ "});";
		
		
		PageHelper.appendBodyScriptSnippet(document, tinyMCEScript);
	}


}
