/*******************************************************************************
 * Copyright (c) 2019 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package com.jadaptive.app.auth.oauth2;

import java.util.Set;

import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.user.User;

public interface OAuth2Service {
	
	DeviceCode requestDevice(OAuth2Request request, String verificationUri);

	String requestAuthCode(OAuth2AuthCodeRequest authCodeRequest);
	
	Set<OAuth2Scope> getScopes(String... scopes);
	
	OAuth2AuthCodeRequest getAuthCodeRequest(String code);
	
	OAuth2AuthCodeRequest popAuthCodeRequest(String code);

	OAuth2Scope getScope(String scope);

	PendingDevice getPendingDevice(String userCode);

	PendingDevice getPendingDeviceByDeviceCode(String deviceCode);

	void removeDevice(String deviceCode);

	void rejectUserCode(String userCode);

	void approveUserCode(String userCode, User user);

	boolean isDeviceCodePending(String deviceCode);
}
