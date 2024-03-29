package com.jadaptive.plugins.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public interface HttpUtilsImpl {
	
	void setVerifier(HostnameVerifier verifier);

	CloseableHttpResponse doHttpGet(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException;

	InputStream doHttpGet(String uri, boolean allowSelfSigned) throws IOException;

	CloseableHttpClient createHttpClient(boolean allowSelfSigned) throws IOException;

	String doHttpPost(String url, Map<String, String> parameters, 
			boolean allowSelfSigned)
					throws IOException;
	
	String doHttpPost(String url, Map<String, String> parameters, 
			boolean allowSelfSigned, Map<String,String> additionalHeaders)
					throws IOException;

	String doHttpGetContent(String uri, boolean allowSelfSigned, Map<String, String> headers) throws IOException;

	CloseableHttpResponse execute(HttpUriRequest request, boolean allowSelfSigned) throws IOException;

	InputStream doHttpGetInputStream(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException;

	String doHttpDelete(String uri, boolean allowSelfSigned, Map<String, String> headers) throws IOException;

}
