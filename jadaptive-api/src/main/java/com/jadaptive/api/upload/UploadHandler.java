package com.jadaptive.api.upload;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.pf4j.ExtensionPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;

public interface UploadHandler extends ExtensionPoint {

	void handleUpload(String handlerName, String uri, Map<String,String[]> parameters,
			UploadIterator uploads) throws IOException, 
				SessionTimeoutException, UnauthorizedException;

	boolean isSessionRequired();
	
	String getURIName();

	default void onUploadsComplete(Map<String,String[]> params) { };
	
	default void onUploadsFailure(Map<String,String[]> params, Throwable e) { };
	
	default void sendSuccessfulResponse(HttpServletResponse resp, String handlerName, String uri, Map<String,String[]> params) throws IOException {
		RequestStatus status = new RequestStatusImpl(true);
		byte[] data = new ObjectMapper().writeValueAsBytes(status);
		resp.setStatus(200);
		resp.getOutputStream().write(data);
		resp.setContentLength(data.length);
		resp.setContentType("application/json");
		
		onUploadsComplete(params);
	}
	
	default void sendFailedResponse(HttpServletResponse resp, String handlerName, String uri, Map<String,String[]> params, Throwable e) throws IOException {
		RequestStatus status = new RequestStatusImpl(false, e.getMessage());
		byte[] data = new ObjectMapper().writeValueAsBytes(status);
		resp.setStatus(200);
		resp.getOutputStream().write(data);
		resp.setContentLength(data.length);
		resp.setContentType("application/json");
		
		onUploadsFailure(params, e);
	}
}
