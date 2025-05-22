package com.jadaptive.api.ui;

public class CreateRedirect extends UriRedirect {

	private static final long serialVersionUID = -883867384789694539L;

	public CreateRedirect(String resourceKey) {
		super("/app/ui/create/" + resourceKey);

	}

}
