package com.jadaptive.app.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.permissions.FeatureGroup;
import com.jadaptive.api.permissions.LicensedFeature;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.HtmlContentService;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageResources;
import com.jadaptive.utils.FileUtils;

@Service
@LicensedFeature(group = FeatureGroup.ENTERPRISE, value = HtmlContentService.DEVELOPER_TOOLS, includedWithPAYG = false)
public class HtmlContentServiceImpl implements HtmlContentService {

	@Autowired
	private ClassLoaderService classService; 
	
	@Override
	public Document resolveDocument(Class<?> clz, PageResources page, String resource, boolean canFail) throws IOException {
		
		if(Objects.nonNull(clz)) {
			
			URL url = clz.getResource(resource);
			
			if(Objects.isNull(url)) {
				url = page.getResourceClass().getResource(resource);
			}
			if(Objects.isNull(url)) {
				url = classService.getResource(resource);
			}
			if(resource.startsWith("/")) {
				resource = FileUtils.checkStartsWithNoSlash(resource);
				return resolveDocument(clz, page, resource, canFail);
			}
			if(Objects.nonNull(url)) {
				return loadDocument(url);
			} 
			
			if(canFail) {
				throw new IOException("Missing document for " + resource);
			}
		}
		Document doc = new Document(Request.get().getRequestURI());
		doc.appendChild(new Element("body"));
		return doc;
	}
	
	@Override
	public void resolveStylesheet(String uri, Document document, PageResources ext) {
		URL url = ext.getResourceClass().getResource(ext.getCssResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(document, "/app/css/" + uri + ".css");
		} else {
			url = classService.getResource(ext.getCssResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendStylesheet(document, "/app/style/" + uri + ".css");
			} 
		}
	}

	@Override
	public void resolveScript(String uri, Document document, PageResources ext) {
		URL url = ext.getResourceClass().getResource(ext.getJsResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendBodyScript(document, "/app/js/" + uri + ".js");
		} else {
			url = classService.getResource(ext.getJsResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendBodyScript(document, "/app/script/" + ext.getJsResource());
			}
		}
	}
	
	protected Document loadDocument(URL url) throws IOException {
		try(InputStream in = url.openStream()) {
			return Jsoup.parse(in, "UTF-8", url.toExternalForm());
		}
	}
}
