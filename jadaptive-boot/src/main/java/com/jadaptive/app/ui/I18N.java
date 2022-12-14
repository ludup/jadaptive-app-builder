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
			boolean optional = e.hasAttr("jad:optional");
			List<Object> args = new ArrayList<>();
			int arg = 0;
			String attr;
			while(e.hasAttr(attr = String.format("jad:arg%d", arg++))) {
				args.add(e.attr(attr));
			}
			switch(e.tag().getName()) {
			case "input":
				if(optional) {
					e.val(i18nService.formatNoDefault(bundle, Locale.getDefault(), e.attr("jad:i18n"), args.toArray(new Object[0])));
				} else {
					e.val(i18nService.format(bundle, Locale.getDefault(), e.attr("jad:i18n"), args.toArray(new Object[0])));
				}
				break;
			default:
				if(optional) {
					e.html(i18nService.formatNoDefault(bundle, Locale.getDefault(), e.attr("jad:i18n"), args.toArray(new Object[0])));
				} else {
					e.html(i18nService.format(bundle, Locale.getDefault(), e.attr("jad:i18n"), args.toArray(new Object[0])));
				}
				break;
			}
			
			e.removeAttr("jad:i18n");
			e.removeAttr("jad:bundle");
			if(optional) {
				e.removeAttr("jad:optional");
			}
		}
	}
	
	@Override
	public String getName() {
		return "i18n";
	}

}
