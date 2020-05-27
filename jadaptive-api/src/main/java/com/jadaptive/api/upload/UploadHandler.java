package com.jadaptive.api.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.pf4j.ExtensionPoint;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;

public interface UploadHandler extends ExtensionPoint {

	void handleUpload(String handlerName, String uri, Map<String,String> parameters,
			String filename, InputStream in) throws IOException, 
				SessionTimeoutException, UnauthorizedException;

	boolean isSessionRequired();
	
	String getURIName();

	default void sendSuccessfulResponse(HttpServletResponse resp, String handlerName, String uri) throws IOException {
		resp.setStatus(HttpStatus.OK.value());
	};

	default void sendFailedResponse(HttpServletResponse resp, String handlerName, String uri, Exception e) throws IOException {
		resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
	}
}
