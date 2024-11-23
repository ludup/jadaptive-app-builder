/*******************************************************************************
 * Copyright (c) 2019 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package com.jadaptive.api.auth.oauth2;

import java.util.Optional;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.entity.ObjectNotFoundException;

public interface OAuth2ApplicationService  extends AbstractUUIDObjectService<OAuth2Application> {

	OAuth2Application byName(String name);

	
	default OAuth2Application find(String nameOrUuid) {
		try {
			return getObjectByUUID(nameOrUuid);
		}
		catch(ObjectNotFoundException nfe) {
			return byName(nameOrUuid);
		}	
	}
	
	default Optional<OAuth2Application> findOr(String nameOrUuid) {
		try {
			return Optional.of(find(nameOrUuid));
		}
		catch(ObjectNotFoundException nfe) {
			return Optional.empty();
		}	
	}
}
