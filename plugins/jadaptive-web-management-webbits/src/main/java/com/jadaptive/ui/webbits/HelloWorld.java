package com.jadaptive.ui.webbits;

import com.codesmith.webbits.View;
import com.codesmith.webbits.Page;

@Page
@View(contentType = "text/html")
public class HelloWorld {

	@Override
	public String toString() {
		/* If we simply override toString(), we are expected to return all content ourselves */
		return "<html><body><h1>Hello World!</h1></body></html>";
	}
}
