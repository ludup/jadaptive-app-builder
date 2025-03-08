package com.jadaptive.api.auth.oauth2;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jadaptive.api.app.App;

/**
 * https://www.rfc-editor.org/rfc/rfc8414
 */
public class OAuth2Discovery {

	private final String issuer;
	private final String authorizationEndpoint;
	private final String tokenEndpoint;
	private final String registrationEndpoint;
	private final String jwksUri;
	private final List<String> scopesSupported;
	private final List<String> responseTypesSupported;
	private final List<String> responseModesSupported;
	private final List<String> grantTypesSupported;
	private final List<String> tokenEndpointAuthMethodsSupported;
	private final List<String> tokenEndpointAuthSigningAlgValuesSupported;
	private final String serviceDocumentation;
	private final List<String> uiLocalesSupported;
	private final String opPolicyUri;
	private final String opTosUri;
	private final String revocationEndpoint;
	private final List<String> revocationEndpointAuthMethodsSupported;
	private final List<String> revocationEndpointAuthSigningAlgValuesSupported;
	private final String introspectionEndpoint;
	private final List<String> introspectionEndpointAuthMethodsSupported;
	private final List<String> introspectionEndpointAuthSigningAlgValuesSupported;
	private final List<String> codeChallengeMethodsSupported;

	public OAuth2Discovery(String hostname, App applicationService) {
		this.issuer = "https://" + hostname;
		this.authorizationEndpoint = this.issuer + "/oauth2-start";
		this.tokenEndpoint = this.issuer + "/oauth2/token";
		this.grantTypesSupported = asList(GrantType.values()).stream().map(GrantType::toGrantType).toList();
		this.scopesSupported = applicationService.getBeans(OAuth2Scope.class).stream().map(OAuth2Scope::getId).toList();
		this.codeChallengeMethodsSupported = Arrays.asList("plain", "S26");
		
		// TODO
		this.registrationEndpoint = null;
		this.jwksUri = null;
		this.responseTypesSupported = null;
		this.responseModesSupported = null;
		this.tokenEndpointAuthMethodsSupported = null;
		this.tokenEndpointAuthSigningAlgValuesSupported = null;
		this.serviceDocumentation = null;
		this.uiLocalesSupported = null;
		this.opPolicyUri = null;
		this.opTosUri = null;
		this.revocationEndpoint = null;
		this.revocationEndpointAuthMethodsSupported = null;
		this.revocationEndpointAuthSigningAlgValuesSupported = null;
		this.introspectionEndpoint = null;
		this.introspectionEndpointAuthMethodsSupported = null;
		this.introspectionEndpointAuthSigningAlgValuesSupported = null;
		
	}

	public String getIssuer() {
		return issuer;
	}

    @JsonProperty("authorization_endpoint")
	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

    @JsonProperty("token_endpoint")
	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

    @JsonProperty("registration_endpoint")
	public String getRegistrationEndpoint() {
		return registrationEndpoint;
	}

    @JsonProperty("jwks_uri")
	public String getJwksUri() {
		return jwksUri;
	}

    @JsonProperty("scopes_supported")
	public List<String> getScopesSupported() {
		return scopesSupported;
	}

    @JsonProperty("response_types_supported")
	public List<String> getResponseTypesSupported() {
		return responseTypesSupported;
	}

    @JsonProperty("response_modes_supported")
	public List<String> getResponseModesSupported() {
		return responseModesSupported;
	}

    @JsonProperty("grant_types_supported")
	public List<String> getGrantTypesSupported() {
		return grantTypesSupported;
	}

    @JsonProperty("token_endpoint_auth_methods_supported")
	public List<String> getTokenEndpointAuthMethodsSupported() {
		return tokenEndpointAuthMethodsSupported;
	}

    @JsonProperty("token_endpoint_auth_signing_alg_values_supported")
	public List<String> getTokenEndpointAuthSigningAlgValuesSupported() {
		return tokenEndpointAuthSigningAlgValuesSupported;
	}

    @JsonProperty("service_documentation")
	public String getServiceDocumentation() {
		return serviceDocumentation;
	}

    @JsonProperty("ui_locales_supported")
	public List<String> getUiLocalesSupported() {
		return uiLocalesSupported;
	}

    @JsonProperty("op_policy_uri")
	public String getOpPolicyUri() {
		return opPolicyUri;
	}

    @JsonProperty("op_tos_uri")
	public String getOpTosUri() {
		return opTosUri;
	}

    @JsonProperty("revocation_endpoint")
	public String getRevocationEndpoint() {
		return revocationEndpoint;
	}

    @JsonProperty("revocation_endpoint_auth_methods_supported")
	public List<String> getRevocationEndpointAuthMethodsSupported() {
		return revocationEndpointAuthMethodsSupported;
	}

    @JsonProperty("revocation_endpoint_auth_signing_alg_values_supported")
	public List<String> getRevocationEndpointAuthSigningAlgValuesSupported() {
		return revocationEndpointAuthSigningAlgValuesSupported;
	}

    @JsonProperty("introspection_endpoint")
	public String getIntrospectionEndpoint() {
		return introspectionEndpoint;
	}

    @JsonProperty("introspection_endpoint_auth_methods_supported")
	public List<String> getIntrospectionEndpointAuthMethodsSupported() {
		return introspectionEndpointAuthMethodsSupported;
	}

    @JsonProperty("introspection_endpoint_auth_signing_alg_values_supported")
	public List<String> getIntrospectionEndpointAuthSigningAlgValuesSupported() {
		return introspectionEndpointAuthSigningAlgValuesSupported;
	}

    @JsonProperty("code_challenge_methods_supported")
	public List<String> getCodeChallengeMethodsSupported() {
		return codeChallengeMethodsSupported;
	}
}
