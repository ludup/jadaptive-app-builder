package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.renderers.DropdownInput;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "bootstrap-tree", "bootstrapTable", "jadaptive-utils", "i18n"} )
@PageProcessors(extensions = { "i18n"} )
public class Tree extends AuthenticatedPage {

	
	@Override
	public String getUri() {
		return "tree";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException {
		super.generateAuthenticatedContent(document);
		
		Map<String,String> depths = new HashMap<>();
		depths.put("0", "flat.name");
		depths.put("99", "maximum.name");
		DropdownInput searchDepth = new DropdownInput("searchDepth", "virtualFolder");
		
		document.select("#searchDepthDropdown").first().appendChild(searchDepth.renderInput());
		searchDepth.renderValues(depths, "0", true);
	}
	
	

}
