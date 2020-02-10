package com.jadaptive.api.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;

public interface UploadHandler extends ExtensionPoint {

	void handleUpload(String handlerName, String uri, Map<String,String> parameters,
			String filename, InputStream in) throws IOException, 
				SessionTimeoutException, UnauthorizedException;

	boolean isSessionRequired();
	
	String getURIName();
}
