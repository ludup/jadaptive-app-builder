package com.jadaptive.plugins.web.ui.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;

public abstract class SetupSection extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		super.process(document, element, page);
	}
}
