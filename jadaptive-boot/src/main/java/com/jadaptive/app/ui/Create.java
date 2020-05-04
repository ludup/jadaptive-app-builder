package com.jadaptive.app.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootBox;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;

@Page({ BootstrapTable.class, BootBox.class, Widgets.class, FreeMarker.class, PageResources.class, PageResourcesElement.class })
@View(contentType = "text/html", paths = { "/create/{resourceKey}" })
@Component
@Resource
public class Create extends ObjectPage {
	
    @Out
    public Document service(@In Document contents) throws IOException {
    	return contents;
    }
}