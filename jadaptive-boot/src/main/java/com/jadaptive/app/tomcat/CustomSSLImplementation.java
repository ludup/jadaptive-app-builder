package com.jadaptive.app.tomcat;
import java.io.File;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

import org.apache.tomcat.util.net.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.apache.tomcat.util.net.jsse.JSSEUtil;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;

public class CustomSSLImplementation extends JSSEImplementation {

	@Override
    public SSLUtil getSSLUtil(SSLHostConfigCertificate certificate) {
        return new JSSEUtil(certificate) {
        	@Override
            public SSLContext createSSLContextInternal(List<String> negotiableProtocols)
                    throws NoSuchAlgorithmException {
                try {
					return createCustomJavaSSLContext();
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
            }
        };
    }

    private SSLContext createCustomJavaSSLContext() throws Exception {
        KeyStore ks = KeyStore.getInstance(
        		new File(ApplicationProperties.getValue("spring.ssl.bundle.jks.default.keystore.location", "conf.d/default/cert.p12")),
        		ApplicationProperties.getValue("spring.ssl.bundle.jks.default.keystore.password", "changeit").toCharArray());
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, ApplicationProperties.getValue("spring.ssl.bundle.jks.default.keystore.password", "changeit").toCharArray());

        X509ExtendedKeyManager originalKm = null;
        for (KeyManager km : kmf.getKeyManagers()) {
            if (km instanceof X509ExtendedKeyManager) {
                originalKm = (X509ExtendedKeyManager) km;
                break;
            }
        }
        if (originalKm == null) {
            throw new IllegalStateException("No X509ExtendedKeyManager found");
        }

        SniKeyManager customKm = App.wire(new SniKeyManager(originalKm));
        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
        sslContext.init(new KeyManager[]{customKm}, null, null);

        return new SniSSLContext(sslContext, customKm);
    }
}
