package com.jadaptive.api.ui.forms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageResources;

public interface InputField extends PageResources, ExtensionPoint {

	default public String getHtmlResource() {
		return String.format("%s.html", getResourceClass().getSimpleName());
	}

	default public String getJsResource() {
		return String.format("%s.js", getResourceClass().getSimpleName());
	}

	default public String getCssResource() {
		return String.format("%s.css",getResourceClass().getSimpleName());
	}
	
	default public Class<?> getResourceClass() {
		return getClass();
	}
	
	default public void render(Document document, Element e, String fieldValue) throws IOException {
		
		URL url = getClass().getResource(getHtmlResource());
		if(Objects.isNull(url)) {
			throw new IOException("Missing resource file " + getHtmlResource());
		}
		try(InputStream in = url.openStream()) {
			Document doc = Jsoup.parse(IOUtils.toString(in, "UTF-8"));
			Element body = doc.selectFirst("body");
			for(Element child : body.children()) {
				e.appendChild(child);
			}
		}
		
		url = getClass().getResource(getJsResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendBodyScript(document, "/app/script/" + getResourceClass().getPackageName().replace('.', '/') + "/" + getJsResource());
		}
		
		url = getClass().getResource(getCssResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(document,  "/app/style/" + getResourceClass().getPackageName().replace('.', '/') + "/" + getCssResource());
		}
		
	}
}
