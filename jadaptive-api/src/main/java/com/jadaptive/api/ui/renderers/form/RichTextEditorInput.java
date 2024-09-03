package com.jadaptive.api.ui.renderers.form;

import static com.jadaptive.utils.Npm.scripts;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.PageHelper;

public class RichTextEditorInput extends FieldInputRender {

	private Document document;

	public RichTextEditorInput(TemplateViewField field, Document document) {
		super(field);
		this.document = document;
	}
	
	public RichTextEditorInput(Document document, String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
		this.document = document;
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
		
		rootElement.appendChild(new Element("label")
				.attr("for", getFormVariable())
				.addClass("form-label")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		
		rootElement.appendChild(
				new Element("textarea")
					.val(value)
					.attr("name", resourceKey)
					.attr("id", resourceKey)
					.addClass("row mb-3 mceEditor form-control"));
		
		rootElement.appendChild(new Element("small")
				.addClass("form-text")
				.addClass("text-muted")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		
		rootElement.addClass("mb-3");
		
		scripts(document, "tinymce", 
				"tinymce.min.js"
		);
		
		String tinyMCEScript = "$(function() { \n" 
		+ " tinymce.init({\n"
		+ "   mode : '" + resourceKey + "',"
		+ "	  selector: '#" + resourceKey + "', \n"
		+ "	  plugins: 'autosave',\n"
		+ "	  license_key: 'gpl',\n"
		+ "	  promotion: false,\n"
		+ "	  branding: false,\n"
		+ "	  menubar: 'edit insert view format table'\n"
		+ "	}); \n"
		+ "});";
		
		PageHelper.addContentSecurityPolicy("style-src", SessionUtils.UNSAFE_INLINE);
		PageHelper.appendBodyScriptSnippet(document, tinyMCEScript);
	}


}
