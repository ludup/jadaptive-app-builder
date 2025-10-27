package com.jadaptive.api.ui.pages.ext;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class Fontawesome extends AbstractPageExtension {
	
	String iconset = "fa-solid";
	
	@Override
	public void process(Document document, Element element, Page page) {
		
		if(StringUtils.isBlank(document.selectFirst("body").attr("data-iconset"))) {
			document.selectFirst("body").attr("data-iconset", "fa-solid");
			PageHelper.appendStylesheet(document, 
					"/app/content/npm2mvn/npm.fortawesome/fontawesome-free/current/css/all.css",
					"fontawesomeCss");
		}
	}
	
	@Override
	public String getName() {
		return "fontawesome";
	}

	public String iconset() {
		return iconset;
	}
	
	public void setIconSet(String iconset) {
		this.iconset = iconset;
	}

}