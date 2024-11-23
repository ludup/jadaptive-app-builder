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
	String[] value();

	/**
	 * Set whether OAuth authentication is required for this method. When <code>false</code>,
	 * non-OAuth authenticated sessions may also access it.
	 * 
	 * @return oauth required
	 */
	boolean required() default true;
}
