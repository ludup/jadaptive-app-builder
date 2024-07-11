package com.jadaptive.api.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public interface TemplatedPageExtender extends HtmlPageExtender {

	default public Elements template() {
		URL html = getClass().getResource(getClass().getSimpleName() + ".html");
		try(InputStream in = html.openStream()) {
			Document doc = Jsoup.parse(IOUtils.toString(in, "UTF-8"));
			return doc.body().children();
		}
		catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

}
