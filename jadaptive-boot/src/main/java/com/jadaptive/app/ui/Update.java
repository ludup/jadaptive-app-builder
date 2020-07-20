package com.jadaptive.app.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootBox;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.bootstrap.BootstrapToggle;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.template.FieldView;

@Page({ BootstrapTable.class, BootBox.class, BootstrapToggle.class, Widgets.class, FreeMarker.class })
@View(contentType = "text/html", paths = { "/update/{resourceKey}/{uuid}", "/update/{resourceKey}" })
@ClasspathResource
public class Update extends ObjectPage {


    @Out
    public Document service(@In Document contents) throws IOException {
    	return contents;
    }

	@Override
	public FieldView getScope() {
		return FieldView.UPDATE;
	}
}
