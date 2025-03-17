package com.jadaptive.app.tomcat;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.StandardConstants;
import javax.net.ssl.X509ExtendedKeyManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.certificates.Keyring;
import com.jadaptive.utils.Utils;

public final class SniKeyManager extends X509ExtendedKeyManager {

	private final String defaultAlias = "default";

	@Autowired
	private Keyring keyring;
	
	public SniKeyManager() {
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		throw new UnsupportedOperationException(); // we don't use client mode
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		throw new UnsupportedOperationException(); // as above
	}

	@Override
	public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
		throw new UnsupportedOperationException(); // as above
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return keyring.getAliases();
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		throw new UnsupportedOperationException(); // Netty does not use SSLSocket
	}

	@Override
	public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
		ExtendedSSLSession session = (ExtendedSSLSession) engine.getHandshakeSession();

		// Pick first SNIHostName in the list of SNI names.
		String hostname = null;
		for (SNIServerName name : session.getRequestedServerNames()) {
			if (name.getType() == StandardConstants.SNI_HOST_NAME) {
				hostname = ((SNIHostName) name).getAsciiName();
				break;
			}
		}

		// If we got given a hostname over SNI, check if we have a cert and key for that hostname. If so, we use it.
		// Otherwise, we fall back to the default certificate.
		if (hostname != null && isMatchingAlias(hostname)) {
			return hostname;
		} else  if (hostname != null) {
			if(!Utils.isIPAddress(hostname)) {
				int idx = hostname.indexOf('.');
				if(idx > -1) {
					hostname = "*" + hostname.substring(idx);
					if(isMatchingAlias(hostname)) {
						return hostname;
					}
				}
			}
		}
		
		return defaultAlias;
	}

	protected boolean isMatchingAlias(String hostname) {
		return getCertificateChain(hostname) != null && getPrivateKey(hostname) != null;
	}
	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return keyring.getCertificateChain(alias);
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		return keyring.getPrivateKey(alias);
	}
}