package com.jadaptive.api.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;

public class AuthenticationState {

	User user;
	List<Class<? extends Page>> authenticationPages = new ArrayList<>();
	List<Class<? extends Page>> postAuthenticationPages = new ArrayList<>();
	int currentPageIndex = 0;
	String remoteAddress;
	String userAgent;
	Map<String,Object> attrs = new HashMap<>();
	boolean decorateWindow = true;
	String resetURL = "/app/ui/login-reset";
	String resetText = "Restart Authentication";
	int failedAttempts = 0;
	int currentPostAuthenticationIndex = 0;
	Redirect homePage = new UriRedirect("/app/ui/dashboard");
	String attemptedUsername;
	
	public Class<? extends Page> getCurrentPage() {
		if(!isAuthenticationComplete()) {
			return authenticationPages.get(currentPageIndex);
		} else {
			if(currentPostAuthenticationIndex >= postAuthenticationPages.size()) {
				Request.get().getSession().setAttribute(AuthenticationService.AUTHENTICATION_STATE_ATTR, null);
				if(Objects.nonNull(homePage)) {
					throw homePage;
				}
				throw new IllegalStateException("No home page set in authentication state");
			}
			return postAuthenticationPages.get(currentPostAuthenticationIndex);
		}
	}
	
	public boolean isAuthenticationComplete() {
		return currentPageIndex == authenticationPages.size();
	}
	
	public boolean hasMorePages() {
		return currentPageIndex < authenticationPages.size() - 1;
	}
	
	public boolean completePage() {
		if(isAuthenticationComplete()) {
			currentPostAuthenticationIndex++;
		} else {
			currentPageIndex++;
			if(isAuthenticationComplete()) {
				return true;
			}
		}
		return false;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Class<? extends Page>> getAuthenticationPages() {
		return authenticationPages;
	}
	
	public void setAuthenticationPages(List<Class<? extends Page>> authenticationPages) {
		this.authenticationPages = authenticationPages;
	}
	
	public List<Class<? extends Page>> getPostAuthenticationPages() {
		return postAuthenticationPages;
	}

	public void setPostAuthenticationPages(List<Class<? extends Page>> postAuthenticationPages) {
		this.postAuthenticationPages = postAuthenticationPages;
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

	public Redirect getHomePage() {
		return homePage;
	}

	public void setHomePage(Page homePage) {
		this.homePage = new PageRedirect(homePage);
	}
	
	public void setHomePage(String uri) {
		this.homePage = new UriRedirect(uri);
	}

	public String getAttemptedUsername() {
		return attemptedUsername;
	}

	public void setAttemptedUsername(String username) {
		this.attemptedUsername = username;
	}
}
