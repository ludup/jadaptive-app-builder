package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	private I18nService i18nService; 
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		/**
		 * Yes this does it twice, once to insert i18n from file, and
		 * another to process any i18n inserted within the i18n itself.
		 */
		replaceAttributes(document, false);
		replaceAttributes(document, true);
	}

	void replaceAttributes(Document document, boolean remove) {
		for(Element e : document.getElementsByAttribute("jad:i18n")) {
			String bundle = e.attr("jad:bundle");
			List<Object> args = new ArrayList<>();
			int arg = 0;
			String attr;
			while(e.hasAttr(attr = String.format("jad:arg%d", arg++))) {
				args.add(e.attr(attr));
			}
			switch(e.tag().getName()) {
			case "input":
				e.val(i18nService.format(bundle, Locale.getDefault(), e.attr("jad:i18n"), args.toArray(new Object[0])));
				break;
			default:
				e.html(i18nService.format(bundle, Locale.getDefault(), e.attr("jad:i18n"), args.toArray(new Object[0])));
				break;
			}
			
			e.removeAttr("jad:i18n");
			e.removeAttr("jad:bundle");
			
		}
	}
	
	@Override
	public String getName() {
		return "i18n";
	}

}
