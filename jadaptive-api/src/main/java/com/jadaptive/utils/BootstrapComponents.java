package com.jadaptive.utils;

import org.bouncycastle.util.Arrays;
import org.jsoup.nodes.Element;

public class BootstrapComponents {

	public static Element progressBar(long max, long val, String background, String... classes) {

		var bar = new Element("div").
				addClass("progress-bar auto-progress-bar").
				attr("role", "progressbar").
				attr("aria-valuenow", String.valueOf(val)).
				attr("aria-valuemax", String.valueOf(max)).
				attr("role", "progressbar");

		bar.addClass("bg-" + background);
		
		var el = new Element("div").
				addClass(Utils.csv(" ", 
						Arrays.append(classes, "progress")));
		el.appendChild(bar);
		
		return el;
	}
	
	public static Element progressBarWithText(long max, long val, String background, Element textElement, String... classes) {

		var bar = new Element("div").
				addClass("progress-bar auto-progress-bar").
				attr("role", "progressbar").
				attr("aria-valuenow", String.valueOf(val)).
				attr("aria-valuemax", String.valueOf(max)).
				attr("role", "progressbar");

		bar.addClass("bg-" + background);
		bar.appendChild(textElement);
		
		var el = new Element("div").
				addClass(Utils.csv(" ", Arrays.append(classes, "progress")));
		el.appendChild(bar);
		
		return el;
	}
}
