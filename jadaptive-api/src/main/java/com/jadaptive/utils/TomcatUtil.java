package com.jadaptive.utils;

/**
 * NOTE: This will not be needed when we update to Spring Boot 3.2+, it can
 * do it's own SSL configuration reloading
 */
@Deprecated
public interface TomcatUtil {

	void reloadSSLHostConfig();

}