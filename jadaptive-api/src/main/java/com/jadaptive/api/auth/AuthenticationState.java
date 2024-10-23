package com.jadaptive.api.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;

public class AuthenticationState {
	private final static Logger LOG = LoggerFactory.getLogger(AuthenticationState.class);

	private User user;
	private Map<Class<? extends Page>,AuthenticationModule> requiredAuthenticationModulez = new HashMap<>();
	private List<Class<? extends Page>> requiredAuthenticationPages = new ArrayList<>();
	private Map<Class<? extends Page>,AuthenticationModule> optionalAuthentications = new HashMap<>();
	private List<PostAuthenticatorPage> postAuthenticationPages = null;
	private int currentPageIndex = 0;
	private String remoteAddress;
	private String userAgent;
	private Map<String,Object> attrs = new HashMap<>();
	private boolean decorateWindow = true;
	private String resetURL = "/app/api/reset-login";
	private String resetText = "Reset";
	private int failedAttempts = 0;
	private int currentPostAuthenticationIndex = 0;
	private Redirect homePage;
	private String attemptedUsername;
	private Class<? extends Page> optionalSelectionPage;
	private Class<? extends Page> selectedPage = null;
	private AuthenticationModule selectedAuthenticator = null;
	private List<Class<? extends Page>> completedOptionsPages = new ArrayList<>();
	private boolean passwordEnabled;
	private int optionalCompleted = 0;
	private int optionalRequired = 0;
	private int optionalAvailable = 0;
	
	private AuthenticationPolicy policy;
	
	public AuthenticationState(AuthenticationPolicy policy) {
		this.policy = policy;
	}
	
	public boolean hasSetupPostAuthentication() {
		return postAuthenticationPages != null;
	}

	public AuthenticationPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(AuthenticationPolicy policy) {
		this.policy = policy;
	}

	public int getOptionalAvailable() {
		return optionalAvailable;
	}

	public void setOptionalAvailable(int optionalAvailable) {
		this.optionalAvailable = optionalAvailable;
	}
	
	public Redirect nextRedirectOrFinish(PageCache pageCache) throws Redirect {
		if(!isRequiredAuthenticationComplete()) {
			return new PageRedirect(pageCache.getPage(requiredAuthenticationPages.get(currentPageIndex)));
		} if(!isOptionalComplete()) { 
			if(Objects.nonNull(selectedPage)) {
				return new PageRedirect(pageCache.getPage(selectedPage));
			} else {
				return new PageRedirect(pageCache.getPage(optionalSelectionPage));
			}
			
		} else {
			if(!hasPostAuthentication()) {
				LOG.info("Clearing authentication state from session.");
				Request.get().getSession().removeAttribute(AuthenticationService.AUTHENTICATION_STATE_ATTR);
				
				if(Objects.nonNull(homePage)) {
					return homePage;
				}
				else
					return new PageRedirect(pageCache.getPage(pageCache.getHomeClass()));
			}
			
			return new PageRedirect(pageCache.getPage(postAuthenticationPages.get(currentPostAuthenticationIndex).getClass()));
		}
	}

	public Optional<Class<? extends Page>> getCurrentPage() {
		if(!isRequiredAuthenticationComplete()) {
			return Optional.of(requiredAuthenticationPages.get(currentPageIndex));
		} if(!isOptionalComplete()) { 
			if(Objects.nonNull(selectedPage)) {
				return Optional.of(selectedPage);
			} else {
				return Optional.of(optionalSelectionPage);
			}
			
		} else {
			if(!hasPostAuthentication()) {
				if(Objects.nonNull(homePage)) {
					throw homePage;
				}
				else
					return Optional.empty();
			}
			return Optional.of(postAuthenticationPages.get(currentPostAuthenticationIndex).getClass());
		}
	}
	
	public AuthenticationModule getCurrentAuthenticator() {
		if(!isRequiredAuthenticationComplete()) {
			return  requiredAuthenticationModulez.get(requiredAuthenticationPages.get(currentPageIndex));
		} if(!isOptionalComplete()) { 
			if(Objects.nonNull(selectedPage)) {
				return selectedAuthenticator;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public boolean hasFinished() {
		return isRequiredAuthenticationComplete() 
				&& isOptionalComplete()
				&& !hasPostAuthentication();
	}
	
	public boolean isNew() {
		return currentPageIndex == 0 && optionalCompleted == 0 && currentPostAuthenticationIndex == 0 && failedAttempts == 0;
	}
	
	public boolean isRequiredAuthenticationComplete() {
		return currentPageIndex >= requiredAuthenticationPages.size();
	}
	
	public boolean isOptionalComplete() {
		return optionalCompleted == optionalRequired;
	}
	
	public boolean hasPostAuthentication() {
		return currentPostAuthenticationIndex < postAuthenticationPages.size();
	}
	
	public boolean completePage() {
		if(isRequiredAuthenticationComplete() && isOptionalComplete()) {
			currentPostAuthenticationIndex++;
			return !hasPostAuthentication();
		} else {
			currentPageIndex++;
			if(isRequiredAuthenticationComplete()) {
				if(Objects.nonNull(selectedPage)) {
					optionalCompleted++;
					completedOptionsPages.add(selectedPage);
					selectedPage = null;
					selectedAuthenticator = null;
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
	
	public Collection<AuthenticationModule> getOptionalAuthentications() {
		return optionalAuthentications.values();
	}
	
	public List<PostAuthenticatorPage> getPostAuthenticationPages() {
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
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Class<T> key) {
		return (T)attrs.get(key.getName());
	}
	
	public <T> void setAttribute(Class<T> key, T value) {
		attrs.put(key.getName(), value);
	}
	
	public void removeAttribute(Class<?> key) {
		attrs.remove(key.getName());
	}
	
	public Object removeAttribute(String name) {
		return attrs.remove(name);
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
	
	public void setHomePage(Redirect homePage) {
		this.homePage = homePage;
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
	
	public void setSelectedPage(Class<? extends Page> selectedPage) {
		this.selectedPage = selectedPage;
		this.selectedAuthenticator = optionalAuthentications.get(selectedPage);
	}

	public boolean hasCompleted(Class<? extends Page> page) {
		return completedOptionsPages.contains(page);
	}

	public boolean isFirstPage() {
		return currentPageIndex == 0;
	}

	public boolean isPasswordEnabled() {
		return passwordEnabled;
	}

	public void setPasswordEnabled(boolean passwordEnabled) {
		this.passwordEnabled = passwordEnabled;
	}

	public void insertNextPostAuthentication(PostAuthenticatorPage page) {
		this.postAuthenticationPages.add(currentPostAuthenticationIndex+1, page);
	}
	
	public void insertPostAuthentication(PostAuthenticatorPage page) {
		this.postAuthenticationPages.add(page);
	}
	
	public Map<String,Object> getAttributes() {
		return attrs;
	}

	public void clearRequiredAuthentications() {
		requiredAuthenticationModulez.clear();
		requiredAuthenticationPages.clear();
	}

	public void addRequiredAuthentication(Class<? extends Page> clz, AuthenticationModule authenticationModule) {
		requiredAuthenticationModulez.put(clz, authenticationModule);
		requiredAuthenticationPages.add(clz);
	}

	public void clearOptionalAuthentications() {
		optionalAuthentications.clear();
	}
	
	public void addOptionalAuthentication(Class<? extends Page> clz, AuthenticationModule authenticationModule) {
		optionalAuthentications.put(clz, authenticationModule);
	}

	public void setPostAuthenticationPages(List<PostAuthenticatorPage> postAuthenticationPages) {
		this.postAuthenticationPages = postAuthenticationPages;
	}
}
