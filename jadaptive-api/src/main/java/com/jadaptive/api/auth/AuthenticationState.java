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
	List<Class<? extends Page>> requiredAuthenticationPages = new ArrayList<>();
	List<AuthenticationModule> optionalAuthentications = new ArrayList<>();
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
	Class<? extends Page> optionalSelectionPage;
	Class<? extends Page> selectedPage = null;
	List<Class<? extends Page>> completedOptionsPages = new ArrayList<>();
	
	int optionalCompleted = 0;
	int optionalRequired = 0;
	
	public Class<? extends Page> getCurrentPage() {
		if(!isAuthenticationComplete()) {
			return requiredAuthenticationPages.get(currentPageIndex);
		} if(!isOptionalComplete()) { 
			if(Objects.nonNull(selectedPage)) {
				return selectedPage;
			} else {
				return optionalSelectionPage;
			}
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
		return currentPageIndex >= requiredAuthenticationPages.size();
	}
	
	public boolean isOptionalComplete() {
		return optionalCompleted == optionalRequired;
	}
	
	public boolean hasMorePages() {
		return currentPageIndex < requiredAuthenticationPages.size() - 1;
	}
	
	public boolean completePage() {
		if(isAuthenticationComplete() && isOptionalComplete()) {
			currentPostAuthenticationIndex++;
		} else {
			currentPageIndex++;
			if(isAuthenticationComplete()) {
				if(Objects.nonNull(selectedPage)) {
					optionalCompleted++;
					completedOptionsPages.add(selectedPage);
					selectedPage = null;
				}
				return isOptionalComplete();
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
	
	public void clear() {
		currentPageIndex = 0;
		requiredAuthenticationPages.clear();
		optionalAuthentications.clear();
		optionalCompleted = 0;
		optionalRequired = 0;
	}
	
	public List<Class<? extends Page>> getRequiredPages() {
		return requiredAuthenticationPages;
	}
	
	public List<AuthenticationModule> getOptionalAuthentications() {
		return optionalAuthentications;
	}
	
	public List<Class<? extends Page>> getPostAuthenticationPages() {
		return postAuthenticationPages;
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

	public int getOptionalCompleted() {
		return optionalCompleted;
	}

	public void setOptionalCompleted(int optionalCompleted) {
		this.optionalCompleted = optionalCompleted;
	}

	public int getOptionalRequired() {
		return optionalRequired;
	}

	public void setOptionalRequired(int optionalRequired) {
		this.optionalRequired = optionalRequired;
	}

	public Class<? extends Page> getOptionalSelectionPage() {
		return optionalSelectionPage;
	}

	public void setOptionalSelectionPage(Class<? extends Page> optionalSelectionPage) {
		this.optionalSelectionPage = optionalSelectionPage;
	}
	
	public void setSelectedPagE(Class<? extends Page> selectedPage) {
		this.selectedPage = selectedPage;
	}

	public boolean hasCompleted(Class<? extends Page> page) {
		return completedOptionsPages.contains(page);
	}
}
