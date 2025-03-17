package com.jadaptive.app.tomcat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.app.certificates.KeyHolder;
import com.jadaptive.api.app.certificates.KeyType;
import com.jadaptive.api.app.certificates.Keyring;
import com.jadaptive.api.x509.FileFormatException;
import com.jadaptive.api.x509.InvalidPassphraseException;
import com.jadaptive.api.x509.MismatchedCertificateException;
import com.jadaptive.api.x509.X509CertificateUtils;

@Component
public class KeytoolKeyring implements Keyring, StartupAware {
	private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";
	private static Logger LOG = LoggerFactory.getLogger(KeytoolKeyring.class);
	
	private KeyHolder defaultKeyHolder = null;
	private Map<String,Set<KeyHolder>> keyEntries = new HashMap<>();

	@PostConstruct
	private void setup() throws IOException {
		reload();
	}
	
	@Override
	public void onApplicationStartup() {
	}

	@Override
	public Optional<KeyHolder> find(String name, KeyType type) {
		if("default".equals(name)) {
			return Optional.of(defaultKeyHolder);
		}
		if(keyEntries.containsKey(name)) {
			return keyEntries.get(name).stream().filter(e -> e.type().equals(type)).findFirst();
		} else {
			return Optional.empty();
		}
	}

	public final static X509Certificate decodeCertificate(byte[] encoded) {
		try {
			return (X509Certificate) CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME).generateCertificate(new ByteArrayInputStream(encoded));
		} catch (CertificateException | NoSuchProviderException e) {
			throw new IllegalArgumentException("Failed to decode certifcate.", e);
		}
	}

	public final static X509Certificate decodeCertificate(Object cert) {
		if (cert instanceof X509Certificate x509) {
			return x509;
		} else {
			throw new IllegalArgumentException("Unkown Certificate format " + cert.getClass());
		}
	}
	
	@Override
	public void reload() throws IOException {
		
		defaultKeyHolder = null;
		String[] certs = ApplicationProperties.getValue("jadaptive.bundles", "default").split(",");
		for(String cert : certs) {
			reload(cert);
		}
		
		// TODO load others from application properties
	}
	
	public void reload(String name) throws IOException {
		var entries = new LinkedHashSet<KeyHolder>();
				
		KeyStore serverKeyStore = null, caKeyStore = getCertificateAuthorities();

		String pemPath = ApplicationProperties.getValue(String.format("spring.ssl.bundle.pem.%s.keystore.private-key", name), "");
		String certPath = ApplicationProperties.getValue(String.format("spring.ssl.bundle.pem.%s.keystore.certificate", name), "");
		if(StringUtils.isNotBlank(pemPath) && StringUtils.isNotBlank(certPath)) {
			Path key = Paths.get(pemPath);
			Path cert = Paths.get(certPath);
			if(Files.exists(key) && Files.exists(cert)) {
				try {
					serverKeyStore = X509CertificateUtils.loadKeystoreFromPEM(Files.newInputStream(key),
							Files.newInputStream(cert), 
							null,
							DEFAULT_KEYSTORE_PASSWORD.toCharArray());
					
					LOG.info("Loaded PEM file for {} certificate", name);
				} catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException
						| InvalidPassphraseException | FileFormatException | MismatchedCertificateException e) {
					LOG.error("Failed to load PEM key/certificate {}", name, e);
				}
			}
		}
		
		
		if(Objects.isNull(serverKeyStore)) {
			LOG.info("Cannot find default PEM certificate! Looking for PKCS12 keystore");
			
			var serverKeystoreFile = Paths.get(ApplicationProperties.getValue(String.format("spring.ssl.bundle.jks.%s.keystore.location", name), 
					String.format("conf.d/%s/cert.p12", name)));

			try(var in = Files.newInputStream(serverKeystoreFile)) {
				try {
					serverKeyStore = KeyStore.getInstance("PKCS12");
					serverKeyStore.load(in, ApplicationProperties.getValue(
							String.format("spring.ssl.bundle.jks.%s.keystore.password", name),
							"changeit").toCharArray());
					
					LOG.info("Loaded PKCS12 file for {} certificate", name);
					
				} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
					throw new IOException(e.getMessage(), e);
				}
			} 
		}


		try {
			// Add the server key store entries
			Enumeration<String> aliasEnum = serverKeyStore.aliases();
			while (aliasEnum.hasMoreElements()) {
				var alias = aliasEnum.nextElement();
				var key = serverKeyStore.getKey(alias, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
				var cert = decodeCertificate(serverKeyStore.getCertificate(alias).getEncoded());
				var certChain = decodeCertificates(serverKeyStore.getCertificateChain(alias));
				
				KeyHolder entry = null;
				if (key == null) {
					if (cert == null) {
						LOG.warn("Not a key or a cert.");
					} else {
						if(certChain == null)
							entry = new KeyHolder(KeyType.CERTIFICATE, alias, key, cert);
						else
							entry = new KeyHolder(KeyType.CERTIFICATE, alias, key, certChain);
					}
				} else {
					if (cert == null) {
						entry = new KeyHolder(KeyType.CERTIFICATE, alias, key);
					} else {
						if(certChain == null)
							entry = new KeyHolder(KeyType.PRIVATE_KEY_AND_CERTIFICATE, alias, key, cert);
						else
							entry = new KeyHolder(KeyType.PRIVATE_KEY_AND_CERTIFICATE, alias, key, certChain);
					}
				}
				if (entry != null) {
					if(Objects.isNull(defaultKeyHolder)) {
						defaultKeyHolder = entry;
					}
					entries.add(entry);
				}
			}

			// Add the CA certs
			aliasEnum = caKeyStore.aliases();
			while (aliasEnum.hasMoreElements()) {
				var alias = aliasEnum.nextElement();
				var cert =decodeCertificate(caKeyStore.getCertificate(alias).getEncoded());
				var entry = new KeyHolder(KeyType.CA, alias, null, cert);
				entries.add(entry);
			}
			
			keyEntries.put(name, entries);
			
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateEncodingException e) {
			throw new IOException(e.getMessage(), e);
		}

	}

	private X509Certificate[] decodeCertificates(Certificate[] certificateChain) throws CertificateEncodingException {
		X509Certificate[] results = new X509Certificate[certificateChain.length];
		int idx = 0;
		for(Certificate cert : certificateChain) {
			results[idx++] = decodeCertificate(cert.getEncoded());
		}
		return results;
	}

	private KeyStore getCertificateAuthorities() throws IOException {
		
		try {
			var secDir = Paths.get(System.getProperty("java.home"), "lib", "security");
			KeyStore caKeyStore = KeyStore.getInstance("JKS");
			var caKeyStoreFile = secDir.resolve("cacerts");
			try(var in = Files.newInputStream(caKeyStoreFile)) {
				caKeyStore.load(in, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
			}
			return caKeyStore;
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		Optional<KeyHolder> key = find(alias, KeyType.PRIVATE_KEY_AND_CERTIFICATE);
		if(key.isPresent()) {
			return (X509Certificate[]) key.get().chain();
		}
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		Optional<KeyHolder> key = find(alias, KeyType.PRIVATE_KEY_AND_CERTIFICATE);
		if(key.isPresent()) {
			return (PrivateKey) key.get().key();
		}
		return null;
	}

	@Override
	public String[] getAliases() {
		return keyEntries.keySet().toArray(new String[0]);
	}
}
