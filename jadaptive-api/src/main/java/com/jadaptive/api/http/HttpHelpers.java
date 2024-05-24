package com.jadaptive.api.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpHelpers {

	private final static class LazyInitContext {
		private final static SSLContext DEFAULT;

		static {
			try {
				DEFAULT = SSLContext.getInstance("TLS");
				DEFAULT.init(null, new TrustManager[] { new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
				} }, new SecureRandom());
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				throw new IllegalStateException(e);
			}
		}

	}

	public static SSLContext insecureContext() {
		return LazyInitContext.DEFAULT;
	}
}
