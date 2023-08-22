package com.jadaptive.app.saml.idp.config;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml.saml2.attribute.Attribute;

import com.jadaptive.api.user.User;


public class IDPUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;
	private List<Attribute> samlAttributesToSendToSP;
    private List<Authority> authorities;
    private User user;

    public IDPUserDetails(User user, List<Attribute> attributes) {
        this.user = user;
    	this.authorities = Collections.singletonList(new Authority("USER"));
        this.samlAttributesToSendToSP = attributes;
    }

    @Override
    public Collection<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "{noop}notused"; // no password encoder
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
		return "USER";
	}

	public void setRole(String role) {
	
	}

	public void setUsername(String username) {
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public String toString() {
		return "IDPUserDetails [samlAttributesToSendToSP=" + samlAttributesToSendToSP + ", username=" + user.getUsername()
				+ ", role=USER, authorities=" + authorities + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(user.getUsername());
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
		return Objects.equals(user.getUsername(), other.user.getUsername());
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