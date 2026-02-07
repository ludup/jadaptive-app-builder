package com.jadaptive.api.ui.pages.ext;

import static com.jadaptive.utils.Npm.scripts;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.utils.FileUtils;

@Component
public class Bootstrap extends BootstrapOnly {

	static Logger log = LoggerFactory.getLogger(Bootstrap.class);

	@Override
	public void process(Document document, Element element, Page page) {
	
		super.process(document, element, page);
		
		scripts(document, "bootbox", "dist/bootbox.all.min.js");

	}

	@Override
	public String getName() {
		return "bootstrap";
	}

}
