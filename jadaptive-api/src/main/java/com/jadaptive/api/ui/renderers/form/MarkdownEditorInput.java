package com.jadaptive.api.ui.renderers.form;

import static com.jadaptive.utils.Npm.scripts;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.app.App;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.ObjectPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeService;
import com.jadaptive.api.ui.pages.ext.ObjectRenderer;
import com.jadaptive.utils.Npm;

public class MarkdownEditorInput extends FieldInputRender {

	private Document document;

	public MarkdownEditorInput(TemplateViewField field, Document document) {
		super(field);
		this.document = document;
	}
	
	public MarkdownEditorInput(Document document, String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
		this.document = document;
	}

	@Override
	public void renderInput(Element rootElement, String value, boolean readOnly, String... classes) throws IOException {
		
		if(decorate) {
			rootElement.appendChild(new Element("label")
					.attr("for", getFormVariable())
					.addClass("form-label")
					.attr("jad:bundle", getBundle())
					.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		}
		rootElement.appendChild(
				new Element("textarea")
					.val(value)
					.attr("name", resourceKey)
					.attr("id", resourceKey)
					.addClass("row mb-3 mdeEditor form-control"));
		
		if(decorate) {
			rootElement.appendChild(new Element("small")
					.addClass("form-text")
					.addClass("text-muted")
					.attr("jad:bundle", getBundle())
					.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}
		
		rootElement.addClass("mb-3");
		
		scripts(document, "marked", 
				"marked.min.js"
		);
		
		scripts(document, "europa", 
				"lib/umd/europa.js"
		);
		
		
//		Npm.stylesheets(document, "highlight.js", "styles/default.min.css");
		
//		scripts(document, "highlight.js", 
//				"highlight.min.js"
//		);
//		
//		scripts(document, "highlight.js", 
//				"languages/javascript.min.js"
//		);
		
		scripts(document, "easymde", 
				"dist/easymde.min.js"
		);
		
		

		ObjectRenderer renderer = App.bean(ObjectRenderer.class);
		Page page = renderer.getCurrentPage();
		String uniqueId = (page != null && page instanceof ObjectPage ? ((ObjectPage)page).getObject().getUuid() : "__") + "_" + renderer.getCurrentTemplate().getResourceKey() + "_" + resourceKey;
		
		String tinyMCEScript = "$(function() { debugger;\n" 
	    + "  const europa = new Europa();\n"
	    + "  const html = document.getElementById('" + resourceKey + "');\n"
        + "  $('#" + resourceKey + "').val(europa.convert(html.value));\n"
		+ "  var mde = new EasyMDE({\n"
		+ "      element: $('#" + resourceKey + "')[0],\n"
	    + "      forceSync: true,"
	    + "      imageUploadEndpoint: '/app/api/forms/image/upload',"
	    + "      uploadImage: true,"
	    + "      imagePathAbsolute: false,"
	    + "      previewImagesInEditor: true,"
		+ "      autosave: { enabled: true, uniqueId: '" + uniqueId + "', delay: 1000 },"
		+ "      spellChecker: true,"
//		+ "      renderingConfig: {"
//		+ "          codeSyntaxHighlighting: true"
//		+ "      }"
		+ "  });\n"
        + "  mde.togglePreview();"
		+ "  $('#" + resourceKey + "').data('mde', mde);\n"
		+ "});";
		
		PageHelper.addContentSecurityPolicy("style-src", SessionUtils.UNSAFE_INLINE);
		PageHelper.addContentSecurityPolicy("style-src", "https://maxcdn.bootstrapcdn.com");
		PageHelper.addContentSecurityPolicy("font-src", "https://maxcdn.bootstrapcdn.com");
		PageHelper.addContentSecurityPolicy("connect-src", "https://cdn.jsdelivr.net");
		
		PageHelper.appendBodyScriptSnippet(document, tinyMCEScript);
		Npm.stylesheets(document, "easymde", "dist/easymde.min.css");
		if(App.bean(BootstrapThemeService.class).getTheme().isDark()) {
			PageHelper.appendStylesheet(document, "/app/content/easymde/easymde.bootstrap.min.css");
		} 
	}


}
