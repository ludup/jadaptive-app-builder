package com.jadaptive.app.ui;

import org.jsoup.select.Elements;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.ParentView;


public abstract class TemplateDropdown {

    @Out
    public Elements service(@In Elements contents, @In TemplatePage page, @ParentView TemplatePage view) {
    	onService(contents, page);
    	return contents;
    }

	protected abstract void onService(Elements contents, TemplatePage page);
}
