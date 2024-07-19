package com.jadaptive.utils;

import org.jsoup.nodes.Element;

public class BootstrapComponents {

	public static Element progressBar(long max, long val, String background) {

		var bar = new Element("div").
				addClass("progress-bar").
				addClass("auto-progress-bar").
				attr("role", "progressbar").
//				attr("style", "width: " +  (long)(((double)val / (double)max ) * 100d) + "%;").
				attr("aria-valuenow", String.valueOf(val)).
				attr("aria-valuemax", String.valueOf(max)).
				attr("role", "progressbar");

		bar.addClass("bg-" + background);
		
		var el = new Element("div").
				addClass("progress");
		el.appendChild(bar);
		
		return el;
	}
}
