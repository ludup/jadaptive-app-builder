package com.jadaptive.app.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.Bootstrap;
import com.codesmith.webbits.extensions.Client;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.template.FieldView;

@Page({ Bootstrap.class, Widgets.class, FreeMarker.class, Client.class })
@View(contentType = "text/html", paths = { "/create/{resourceKey}" })
@ClasspathResource
public class Create extends TemplatePage {
	
    @Out
    public Document service(@In Document contents) throws IOException {
    	return contents;
    }

	@Override
	public FieldView getScope() {
		return FieldView.CREATE;
	}

}
