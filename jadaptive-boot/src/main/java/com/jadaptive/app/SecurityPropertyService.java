package com.jadaptive.app;

import java.io.FileNotFoundException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

public interface SecurityPropertyService {

	Properties resolveSecurityProperties(HttpServletRequest request, String resourceUri) throws FileNotFoundException;

}
