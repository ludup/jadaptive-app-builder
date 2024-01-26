package com.jadaptive.api.permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.session.Session;

/**
 * Used to elevate a methods invocation current {@link Session}. When no options are present,
 * the currently authenticated user will be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthenticatedContext  {

	/**
	 * Create a new 
	 * @return
	 */
	boolean system() default false;

	/**
	 * Prefer the currently active session, but fallback to a system session if not available.
	 * 
	 * @return prefer active
	 */
	boolean preferActive() default false;

	/**
	 * Requests that the session be elevated to use obtained from the current {@link AuthenticationState}, i.e.
	 * the user that is actually logged in.
	 * 
	 * @return use current principal
	 */
	boolean user() default false;
}
