package com.jadaptive.plugins.web.ui.jobs;

import org.pf4j.Extension;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Extension
@RequestPage(path = "runjob/{instanceId}")
@PageDependencies(extensions = { "jquery", "jquery-ui" } )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class RunJob extends HtmlPage {

	String instanceId;
	
	@Override
	public String getUri() {
		return "runjob";
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
