package com.jadaptive.plugins.keys;

import org.pf4j.Extension;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"})
@ModalPage
public class DownloadPublicKey extends AuthenticatedPage {

	@Override
	public String getUri() {
		return "download-key";
	}
	

	
}
