package com.jadaptive.api.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.api.user.User;

public class AuthenticationState {

	User user;
	List<Class<?>> authenticationPages = new ArrayList<>();
	int currentPageIndex = 0;
	String remoteAddress;
	String userAgent;
	Map<String,Object> attrs = new HashMap<>();
	boolean decorateWindow = true;
	String resetURL = "/app/ui/login-reset";
	String resetText = "Restart Authentication";
	int failedAttempts = 0;
	
	public Class<?> getCurrentPage() {
		return authenticationPages.get(currentPageIndex);
	}
	
	public boolean isComplete() {
		return currentPageIndex == authenticationPages.size();
	}
	
	public boolean hasMorePages() {
		return currentPageIndex < authenticationPages.size() - 1;
	}
	
	public void completePage() {
		currentPageIndex++;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Class<?>> getAuthenticationPages() {
		return authenticationPages;
	}
	
	public void setAuthenticationPages(List<Class<?>> authenticationPages) {
		this.authenticationPages = authenticationPages;
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean hasUser() {
		return Objects.nonNull(user);
	}

	public void verifyUser(User user) {
		if(!this.user.getUuid().equals(user.getUuid())) {
			throw new IllegalStateException("User cannot change during authentication");
		}
	}
	
	public Object getAttribute(String name) {
		return attrs.get(name);
	}
	
	public void setAttribute(String name, Object value) {
		attrs.put(name, value);
	}
	
	public void removeAttribute(String name) {
		attrs.remove(name);
	}

	public boolean isDecorateWindow() {
		return decorateWindow;
	}

	public void setDecorateWindow(boolean decorateWindow) {
		this.decorateWindow = decorateWindow;
	}
	
	public String getResetURL() {
		return resetURL;
	}
	
	public void setResetURL(String resetURL) {
		this.resetURL = resetURL;
	}

	public boolean canReset() {
		return failedAttempts > 0 || currentPageIndex > 0;
	}

	public String getResetText() {
		return resetText;
	}
	
	public void setResetText(String resetText) {
		this.resetText = resetText;
	}

	public void incrementFailedAttempts() {
		failedAttempts++;
	}
}
