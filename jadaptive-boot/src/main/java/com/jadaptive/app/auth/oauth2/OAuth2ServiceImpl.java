package com.jadaptive.app.auth.oauth2;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService;
import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.app.auth.oauth2.PendingDevice.Status;

@Service
public class OAuth2ServiceImpl implements OAuth2Service, StartupAware {

	private static String genUserCode(OAuth2Configuration config) {
		var pattern = config.getDeviceCodePattern();
		if(StringUtils.isBlank(pattern)) {
			pattern = "AAAAAA";
		}
		var b = new StringBuffer();
		for(char ch : pattern.toCharArray()) {
			if(ch == '?') {
				switch((int)(Math.random() * 3)) {
				case 0:
					ch = 'A';
					break;
				case 1:
					ch = '0';
					break;
				default:
					ch = '!';
					break;
				}
			}
			switch(ch) {
			case 'A':
				b.append((char) ('A' + ((int) (Math.random() * 26))));
				break;
			case 'a':
				b.append((char) ('a' + ((int) (Math.random() * 26))));
				break;
			case '0':
			case '9':
				b.append((char) ('0' + ((int) (Math.random() * 26))));
				break;
			case '!':
				b.append((char) ('!' + ((int) (Math.random() * 15))));
				break;
			default:
				b.append(ch);
				break;
			}
		}
		return b.toString();
	}
	
	@Autowired
	private App applicationService;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private TenantService tenantService;

	@Autowired
	private SingletonObjectDatabase<OAuth2Configuration> config;

	private final ScheduledExecutorService scheduler;

	public OAuth2ServiceImpl() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void onApplicationStartup() {
	}

	private Map<String, PendingDevice> getPendingDevicesByDeviceCode() {
		return cacheService.clusteredCacheOrCreate("pendingDevicesByDeviceCode", String.class, PendingDevice.class);
	}

	private Map<String, PendingDevice> getPendingDevicesByUserCode() {
		return cacheService.clusteredCacheOrCreate("pendingDevicesByUserCode", String.class, PendingDevice.class);
	}

	private Map<String, OAuth2AuthCodeRequest> getOauth2AuthCodeRequests() {
		return cacheService.clusteredCacheOrCreate("oauth2AuthCodeRequests", String.class, OAuth2AuthCodeRequest.class);
	}

	@Override
	public OAuth2AuthCodeRequest getAuthCodeRequest(String code) {
		OAuth2AuthCodeRequest req = getOauth2AuthCodeRequests().get(code);
		if (req == null)
			throw new IllegalArgumentException(String.format("No auth code request with code of %s", code));
		return req;
	}

	@Override
	public PendingDevice getPendingDevice(String userCode) {
		return getPendingDevicesByUserCode().get(userCode);
	}

	@Override
	public OAuth2Scope getScope(String scope) {
		for (var a : applicationService.getBeans(OAuth2Scope.class)) {
			if (a.getId().equals(scope))
				return a;
		}
		throw new IllegalArgumentException(String.format("No Scope %s registered.", scope));
	}

	@Override
	public Set<OAuth2Scope> getScopes(String... scopes) {
		Set<OAuth2Scope> scopeSet = new LinkedHashSet<>();
		for (String scope : scopes) {
			scopeSet.add(getScope(scope));
		}
		return scopeSet;
	}

	@Override
	public boolean isDeviceCodePending(String deviceCode) {
		return getPendingDevicesByDeviceCode().containsKey(deviceCode);
	}

	@Override
	public OAuth2AuthCodeRequest popAuthCodeRequest(String code) {
		OAuth2AuthCodeRequest req = getOauth2AuthCodeRequests().remove(code);
		if (req == null)
			throw new IllegalArgumentException(String.format("No auth code request with code of %s", code));
		return req;
	}

	@Override
	public void removeDevice(String deviceId) {
		var pc = getPendingDevicesByDeviceCode().remove(deviceId);
		if (pc != null) {
			getPendingDevicesByUserCode().remove(pc.getUserCode());
		}
	}

	@Override
	public String requestAuthCode(OAuth2AuthCodeRequest authCodeRequest) {
		getOauth2AuthCodeRequests().put(authCodeRequest.getAuthCode(), authCodeRequest);
		return authCodeRequest.getAuthCode();

	}

	@Override
	public DeviceCode requestDevice(OAuth2Request request, String verificationUri) {
		var oauth2Config = config.getObject(OAuth2Configuration.class);

		var deviceCode = OAuth2AuthorizationService.genToken();
		var userCode = genUniqueUserCode(oauth2Config);
		var expiry = oauth2Config.getDeviceCodeExpiryTime();
		var interval = oauth2Config.getDeviceCodeInterval();
		var tenant = tenantService.getCurrentTenant();
		var pdevc = getPendingDevicesByDeviceCode();
		var pending = new PendingDevice(deviceCode, userCode, request, tenant);
		var pudevc = getPendingDevicesByUserCode();
		pudevc.put(userCode, pending);
		pdevc.put(deviceCode, pending);

		String verificationCompleteUri;
		if (verificationUri.contains("?"))
			verificationCompleteUri = verificationUri + "&user_code=" + userCode;
		else
			verificationCompleteUri = verificationUri + "?user_code=" + userCode;

		scheduler.schedule(() -> {
			synchronized (pdevc) {
				pdevc.remove(userCode);
				pudevc.remove(deviceCode);
			}
		}, expiry, TimeUnit.SECONDS);

		return new DeviceCode(deviceCode, expiry, userCode, verificationUri, interval, verificationCompleteUri);
	}

	private String genUniqueUserCode(OAuth2Configuration config) {
		/* Unlikely, but just in case */
		for (int i = 0; i < 1000; i++) {
			var code = genUserCode(config);
			if (!getPendingDevicesByUserCode().containsKey(code)) {
				return code;
			}
		}
		throw new IllegalStateException("Could not obtain a unique user code.");
	}

	@Override
	public PendingDevice getPendingDeviceByDeviceCode(String deviceCode) {
		return getPendingDevicesByDeviceCode().get(deviceCode);
	}

	@Override
	public void rejectUserCode(String userCode) {
		var puc = getPendingDevicesByUserCode();
		var pending = puc.get(userCode);
		if (pending == null)
			throw new IllegalArgumentException("No pending device for user code.");
		else {
			pending.setStatus(Status.REJECTED);
			puc.put(userCode, pending);
		}
	}

	@Override
	public void approveUserCode(String userCode, User user) {
		var puc = getPendingDevicesByUserCode();
		var pending = puc.get(userCode);
		if (pending == null)
			throw new IllegalArgumentException("No pending device for user code.");
		else {
			pending.setUser(user.getUsername());
			pending.setStatus(Status.APPROVED);
			puc.put(userCode, pending);
		}
	}
}
