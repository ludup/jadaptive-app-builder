package com.jadaptive.app.saml.idp.config;
import org.springframework.security.core.Authentication;

public final class UserUtils {

    private UserUtils() {
    }

    public static IDPUserDetails getCurrentUserDetails(Authentication authentication) {
        IDPUserDetails userDetails = null;
        if (authentication != null && authentication.getPrincipal() instanceof IDPUserDetails) {
            userDetails = (IDPUserDetails) authentication.getPrincipal();
        }
        return userDetails;
    }

}