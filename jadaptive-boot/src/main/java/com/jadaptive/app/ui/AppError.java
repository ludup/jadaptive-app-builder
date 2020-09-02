package com.jadaptive.app.ui;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.Created;
import com.codesmith.webbits.ExceptionHandler;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.codesmith.webbits.extensions.Bind;
import com.codesmith.webbits.freemarker.FreeMarker;

@Page({ FreeMarker.class, Bind.class })
@View(contentType = "text/html", paths = "/error")
@ClasspathResource
@ExceptionHandler
public class AppError {

	@Created
	void created(Exception exception) {
		String msg = exception.getMessage();
		if (msg != null && msg.contains("Not in expected state"))
			throw new Redirect(Dashboard.class);
	}
}
