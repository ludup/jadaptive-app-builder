package com.jadaptive.api.ui;

public class UpdateRedirect extends UriRedirect {

	private static final long serialVersionUID = 5500634777740778373L;

	public UpdateRedirect(String resourceKey, String uuid) {
		super("/app/ui/update/" + resourceKey + "/" + uuid);

	}

}
