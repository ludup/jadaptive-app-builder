package com.jadaptive.app.tomcat;

import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

import org.apache.tomcat.util.net.SSLContext;

public class SniSSLContext implements SSLContext {

	javax.net.ssl.SSLContext context;
	SniKeyManager km;
	TrustManager[] tms;
	
	SniSSLContext(javax.net.ssl.SSLContext context, SniKeyManager km) {
		this.km = km;
		this.context = context;
	}
	
	@Override
	public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {

	}

	@Override
	public void destroy() {
	}

	@Override
	public SSLSessionContext getServerSessionContext() {
		return context.getServerSessionContext();
	}

	@Override
	public SSLEngine createSSLEngine() {
		return context.createSSLEngine();
	}

	@Override
	public SSLServerSocketFactory getServerSocketFactory() {
		return context.getServerSocketFactory();
	}

	@Override
	public SSLParameters getSupportedSSLParameters() {
		return context.getSupportedSSLParameters();
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return  km.getCertificateChain(alias);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		throw new UnsupportedOperationException();
	}

}
