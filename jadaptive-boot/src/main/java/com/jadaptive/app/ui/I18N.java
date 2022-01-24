package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;

@Extension
public class I18N extends AbstractPageExtension {

	@Autowired
	I18nService i18nService; 
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		for(Element e : document.getElementsByAttribute("jad:i18n")) {
			String bundle = e.attr("jad:bundle");
			switch(e.tag().getName()) {
			case "input":
				e.val(i18nService.format(bundle, Locale.getDefault(), e.attr("jad:i18n")));
				break;
			default:
				e.html(i18nService.format(bundle, Locale.getDefault(), e.attr("jad:i18n")));
				break;
			}
		}
	}

	@Override
	public String getName() {
		return "i18n";
	}

}
