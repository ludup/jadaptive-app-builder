package com.jadaptive.api.app.certificates;

import java.security.KeyStore;

import org.pf4j.ExtensionPoint;

public interface CertificateProvider extends ExtensionPoint{

	KeyStore loadCertificate(String name, String alias, char[] password);
}
