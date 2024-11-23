package com.jadaptive.api.auth.oauth2;

import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;

@Service
public class OAuth2ApplicationServiceImpl extends AbstractUUIDObjectServceImpl<OAuth2Application>
		implements OAuth2ApplicationService {

	@Override
	protected Class<OAuth2Application> getResourceClass() {
		return OAuth2Application.class;
	}

	@Override
	public OAuth2Application byName(String name) {
		return objectDatabase.get(OAuth2Application.class, SearchField.eq("name", name));
	}

}
