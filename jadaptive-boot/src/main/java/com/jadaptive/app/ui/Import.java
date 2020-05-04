package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.bootstrap.Bootstrapify;
import com.codesmith.webbits.extensions.Absolutify;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.fontawesome.FontAwesomeify;

@Page({ BootstrapTable.class, Bootstrapify.class, FontAwesomeify.class, 
	PageResources.class, 
	PageResourcesElement.class, Absolutify.class, 
	Enablement.class, I18N.class })
@View(contentType = "text/html", paths = { "/import/{resourceKey}"})
@Component
@Resource
public class Import extends TemplatePage {
    
	@Out(methods = HTTPMethod.POST)
    Document service(@In Document content) {
    	return content;
    }
}
