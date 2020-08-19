package com.jadaptive.plugins.keys;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.View;
import com.jadaptive.api.ui.AuthenticatedPage;

@Page
@View(contentType = "text/html", paths = { "import/public-key", "import/public-key/{uuid}" })
@ClasspathResource
public class PublicKeyUpload extends AuthenticatedPage {
	
	String uuid;
	
	public String getUuid() {
		return uuid;
	}
	
	@Out(methods = HTTPMethod.GET)
    public Document serviceAnonymous(@In Document contents) throws IOException {
    	
    	if(StringUtils.isBlank(uuid)) {
    		uuid = getCurrentUser().getUuid();
    	}
    	
    	contents.selectFirst("form").prepend("<input type=\"hidden\" name=\"uuid\" value=\"" + uuid + "\">");
		return contents;
	}
}
