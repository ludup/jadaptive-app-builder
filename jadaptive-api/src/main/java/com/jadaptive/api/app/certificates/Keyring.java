package com.jadaptive.api.app.certificates;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

public interface Keyring {

	void reload() throws IOException;

	Optional<KeyHolder> find(String name, KeyType type);

	X509Certificate[] getCertificateChain(String alias);

	PrivateKey getPrivateKey(String alias);

}
