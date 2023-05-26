package com.jadaptive.api.upload;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;

@Component
public class UploadWidget extends AbstractPageExtension {

	@Override
	public String getName() {
		return "uploadWidget";
	}
}
