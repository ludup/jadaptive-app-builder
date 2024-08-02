package com.jadaptive.app.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.commons.lang3.StringUtils;
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
		for(var e : document.getElementsByAttribute("jad:i18n")) {
			
			var bundle = StringUtils.defaultIfEmpty(e.attr("jad:bundle"), "missing");
			var fallbackBundle = e.attr("jad:fallback-bundle");
			var optional = e.hasAttr("jad:optional");
			var key = e.attr("jad:i18n");
			var fallbackKey = e.attr("jad:fallback-i18n");
			
			formatElement(e, bundle, fallbackBundle, optional, key, fallbackKey);
			e.removeAttr("jad:i18n");
			e.removeAttr("jad:bundle");
			if(optional) {
				e.removeAttr("jad:optional");
			}
		}
	}

	void formatElement(Element e, String bundle, String fallbackBundle, boolean optional, String key, String fallbackKey) {
		var args = new ArrayList<Object>();
		var arg = 0;
		String attr;
		while(e.hasAttr(attr = String.format("jad:arg%d", arg++))) {
			args.add(e.attr(attr));
		}
		switch(e.tag().getName()) {
		case "input":
			if(fallbackBundle.equals("")) {
				if(optional) {
					e.val(i18nService.formatNoDefault(bundle, Locale.getDefault(), key, args.toArray(new Object[0])));
				} else {
					e.val(i18nService.format(bundle, Locale.getDefault(), key, args.toArray(new Object[0])));
				}
			}
			else {
				try {
					e.val(i18nService.formatOrException(bundle, Locale.getDefault(), key, args.toArray(new Object[0])));
				}
				catch(MissingResourceException | IllegalArgumentException mre) {
					formatElement(e, fallbackBundle, "", optional, fallbackKey, "");
				}
			}
			break;
		default:
			if(fallbackBundle.equals("")) {
				if(optional) {
					e.html(i18nService.formatNoDefault(bundle, Locale.getDefault(), key, args.toArray(new Object[0])));
				} else {
					e.html(i18nService.format(bundle, Locale.getDefault(), key, args.toArray(new Object[0])));
				}
			}
			else {
				try {
					e.html(i18nService.formatOrException(bundle, Locale.getDefault(), key, args.toArray(new Object[0])));
				}
				catch(MissingResourceException | IllegalArgumentException mre) {
					formatElement(e, fallbackBundle, "", optional, fallbackKey, "");
				}
			}
			break;
		}
	}
	
	@Override
	public String getName() {
		return "i18n";
	}

}
