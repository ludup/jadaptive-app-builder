package com.jadaptive.api.auth.oauth2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method that requires a particular OAuth scope. By default, the
 * method will only be accessible via OAuth and will require all specified scopes, 
 * unless {@link OAuth2Requirement#required()} is <code>false</code>. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OAuth2Requirement  {

	/**
	 * Scopes required.
	 *  
	 * @return scopes
	 */
	String value() default "";

	/**
	 * Set whether OAuth authentication is required for this method. When <code>false</code>,
	 * non-OAuth authenticated sessions may also access it.
	 * 
	 * @return oauth required
	 */
	boolean required() default true;

	/**
	 * When <code>true</code>, a 401 response will be sent along with a <code>WWW-Authenticate</code>
	 * Bearer header (with the scopes specified by this annotation). When <code>false</code>, if un-authenticated,
	 * an exception will be thrown. In both cases, the annotated method will NOT be executed.
	 * 
	 * @return send HTTP response
	 */
	boolean response() default true;
	
	/*
	 * Whether or not run the annotated method in the context of the user attached to the 
	 * token.
	 */
	boolean asUser() default true;
}
