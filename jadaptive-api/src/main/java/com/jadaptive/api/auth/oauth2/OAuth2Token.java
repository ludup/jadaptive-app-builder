package com.jadaptive.api.auth.oauth2;

import java.util.Collection;
import java.util.Optional;

import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.user.User;

@ObjectDefinition(bundle = OAuth2Token.RESOURCE_KEY, resourceKey = OAuth2Token.RESOURCE_KEY)
@TableView(defaultColumns = { "name" })
@ObjectViewDefinition(value = OAuth2Token.VIEW_TOKEN, weight = 100)
@ObjectServiceBean(bean = OAuth2ApplicationService.class)
@GenerateEventTemplates
public class OAuth2Token extends NamedUUIDEntity {
	
	private final static ThreadLocal<OAuth2Token> token = new ThreadLocal<>();
	
	public final static OAuth2Token get() {
		return getOr().orElseThrow(() -> new IllegalStateException("Not an OAuth request."));
	}
	
	public final static Optional<OAuth2Token> getOr() {
		return Optional.ofNullable(token.get());
	}
	
	public final static void set(OAuth2Token token) {
		OAuth2Token.token.set(token);
	}

	public final static void clear() {
		OAuth2Token.token.remove();
	}

	private static final long serialVersionUID = -7288600405384404678L;

	public static final String RESOURCE_KEY = "oauth2Tokens";
	public static final String VIEW_TOKEN = "tokenView";

	@ObjectField(type = FieldType.TIMESTAMP)
	@ObjectView(VIEW_TOKEN)
	private long expires;

	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = User.RESOURCE_KEY, readOnly = true)
	@ObjectView(value = VIEW_TOKEN)
	private User owner;

	@ObjectField(type = FieldType.TEXT, defaultValue = "", hidden = true, readOnly = true)
	@ObjectView(value = VIEW_TOKEN)
	private String tenant;

	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = OAuth2Application.RESOURCE_KEY, readOnly = true)
	@ObjectView(value = VIEW_TOKEN)
	private OAuth2Application application;

	@ObjectField(type = FieldType.TEXT, defaultValue = "", hidden = true)
	@ObjectView(VIEW_TOKEN)
	private String nonce;

	@ObjectField(type = FieldType.TEXT, defaultValue = "", hidden = true)
	@ObjectView(VIEW_TOKEN)
	private String refreshToken;
	
	@ObjectField(type = FieldType.TEXT, readOnly = true)
	@ObjectView(VIEW_TOKEN)
	@Validator(type = ValidationType.CIDR_V4)
	@Validator(type = ValidationType.CIDR_V6)
	private Collection<String> scopes;
	
	public OAuth2Application getApplication() {
		return application;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public Collection<String> getScopes() {
		return scopes;
	}

	public void setScopes(Collection<String> scopes) {
		this.scopes = scopes;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setApplication(OAuth2Application application) {
		this.application = application;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() >= expires;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
