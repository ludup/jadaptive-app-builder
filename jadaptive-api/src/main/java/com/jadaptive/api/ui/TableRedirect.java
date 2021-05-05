package com.jadaptive.api.ui;

public class TableRedirect extends UriRedirect {

	private static final long serialVersionUID = 7653033767303709747L;

	public TableRedirect(String resourceKey) {
		super("/app/ui/table/" + resourceKey);

	}

}
