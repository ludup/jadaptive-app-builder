package com.jadaptive.app.ui;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.nodes.Document;

import com.codesmith.webbits.Extension;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.View;
import com.codesmith.webbits.util.Html;

@Extension(extendsPatterns = ".*", appliesTo = Page.class)
@View(contentType = "text/html")
public class JadaptiveUtils {
	
	String path = "/app/ui/js/jadaptive-utils.js";
	
	@Out
	Document service(@In Document template, HttpServletRequest request) {
		Html.addScript(template, request, path);
		return template;
	}
}
