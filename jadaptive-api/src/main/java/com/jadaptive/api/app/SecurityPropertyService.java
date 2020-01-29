package com.jadaptive.api.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public interface SecurityPropertyService {

	Properties resolveSecurityProperties(String resourceUri) throws FileNotFoundException;

	Properties getOverrideProperties(SecurityScope scope, String resourceUri) throws FileNotFoundException, IOException;

	Properties resolveSecurityProperties(String resourceUri, boolean uriOnly) throws FileNotFoundException;

	void saveProperty(SecurityScope scope, String path, String key, String value) throws IOException;

	void deleteProperty(SecurityScope scope, String path, String key) throws IOException;

}
