package com.jadaptive.api.upload;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.PageDependencies;

@Component
@PageDependencies(extensions = { "i18n"})
public class UploadWidget extends AbstractPageExtension {

	@Override
	public String getName() {
		return "uploadWidget";
	}
}
