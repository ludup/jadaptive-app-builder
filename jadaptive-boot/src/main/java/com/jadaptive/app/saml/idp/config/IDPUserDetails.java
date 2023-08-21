package com.jadaptive.app.saml.idp.config;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml.saml2.attribute.Attribute;


public class IDPUserDetails implements UserDetails {


    private static final long serialVersionUID = 1L;
	private List<Attribute> samlAttributesToSendToSP;
    private String username;
    private String role;
    private List<Authority> authorities;


    public IDPUserDetails(String username, String role, List<Attribute> attributes) {
        this.username = username;
        this.authorities = Collections.singletonList(new Authority(role));
        this.samlAttributesToSendToSP = attributes;
    }

    @Override
    public Collection<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "{noop}pass"; // no password encoder
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

	public List<Attribute> getSamlAttributesToSendToSP() {
		return samlAttributesToSendToSP;
	}

	public void setSamlAttributesToSendToSP(List<Attribute> samlAttributesToSendToSP) {
		this.samlAttributesToSendToSP = samlAttributesToSendToSP;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public String toString() {
		return "IDPUserDetails [samlAttributesToSendToSP=" + samlAttributesToSendToSP + ", username=" + username
				+ ", role=" + role + ", authorities=" + authorities + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IDPUserDetails other = (IDPUserDetails) obj;
		return Objects.equals(username, other.username);
	}
    
	
}

class Authority implements GrantedAuthority {
    private String authority;

    Authority(String name) {
        this.authority = name;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}