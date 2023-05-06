package com.jadaptive.api.ui.pages.ext;

import java.util.Collection;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class Fontawesome extends AbstractPageExtension {

	final String free = "/app/content/fontawesome-free-6.4.0-web/css/all.css";
	final String pro = "/app/content/fontawesome-pro-6.4.0-web/css/all.css";
	
	@Autowired
	private ClassLoaderService classService; 
	
	String runtimePath = null;
	
	@Override
	public void process(Document document, Element element, Page page) {
	
		if(Objects.isNull(runtimePath)) {
			Collection<?> classes = classService.resolveAnnotatedClasses(EnableFontAwesomePro.class);
			if(classes.isEmpty()) {
				runtimePath = free;
			} else {
				runtimePath = pro;
			}
		}
		PageHelper.appendStylesheet(document, runtimePath);
	}
	
	@Override
	public String getName() {
		return "fontawesome";
	}

}