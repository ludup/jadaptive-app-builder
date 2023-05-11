package com.jadaptive.plugins.keys;

import org.pf4j.Extension;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
public class PublicKeyUpload extends AuthenticatedPage {

	@Override
	public String getUri() {
		return "upload-key";
	}
}
