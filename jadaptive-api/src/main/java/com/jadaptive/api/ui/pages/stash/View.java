package com.jadaptive.api.ui.pages.stash;

import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Component("stashView")
@RequestPage(path = "object-view/{resourceKey}/{uuid}/{fieldName}/{childUuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
@ModalPage
public class View extends StashedObjectPage {
	
	@Override
	public FieldView getScope() {
		return FieldView.READ;
	}

	@Override
	public String getUri() {
		return "object-view";
	}
}
