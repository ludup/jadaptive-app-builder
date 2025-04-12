package com.jadaptive.app.tomcat;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.KeyManager;

import org.apache.tomcat.util.net.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.apache.tomcat.util.net.jsse.JSSEUtil;

import com.jadaptive.api.app.App;

public class CustomSSLImplementation extends JSSEImplementation {

	@Override
    public SSLUtil getSSLUtil(SSLHostConfigCertificate certificate) {
        return new JSSEUtil(certificate) {
        	@Override
            public SSLContext createSSLContextInternal(List<String> negotiableProtocols)
                    throws NoSuchAlgorithmException {
                try {
					return createCustomJavaSSLContext(certificate);
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
            }
        };
    }

    private SSLContext createCustomJavaSSLContext(SSLHostConfigCertificate certificate) throws Exception {
    	
        SniKeyManager customKm = App.wire(new SniKeyManager(certificate));
        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
        sslContext.init(new KeyManager[]{customKm}, null, null);

        return new SniSSLContext(sslContext, customKm);
    }
}
