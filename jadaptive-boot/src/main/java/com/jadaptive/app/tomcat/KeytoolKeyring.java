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

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.certificates.CertificateProvider;
import com.jadaptive.api.app.certificates.KeyHolder;
import com.jadaptive.api.app.certificates.KeyType;
import com.jadaptive.api.app.certificates.Keyring;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.x509.FileFormatException;
import com.jadaptive.api.x509.InvalidPassphraseException;
import com.jadaptive.api.x509.MismatchedCertificateException;
import com.jadaptive.api.x509.X509CertificateUtils;




@Component
public class KeytoolKeyring implements Keyring, TenantAware {
	private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";
	private static Logger LOG = LoggerFactory.getLogger(KeytoolKeyring.class);
	
	private KeyHolder defaultKeyHolder = null;
	private Map<String,Set<KeyHolder>> keyEntries = new HashMap<>();
	
	@PostConstruct
	private void postConstruct() throws IOException { 
		reload("default");
	}
	
	@Override
	public void initializeSystem(boolean newSchema) {
		try {
			reload();
		} catch (IOException e) {
		}
	}

	@Override
	public Optional<KeyHolder> find(String name, KeyType type) {
		if("default".equals(name)) {
			if(defaultKeyHolder == null) {
				return Optional.empty();
			}
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
		reload("default");
		String[] certs = ApplicationProperties.getValue("jadaptive.sni.bundles", "").split(",");
		for(String cert : certs) {
			reload(cert);
		}
	}
	
	public void reload(String name) throws IOException {
		var entries = new LinkedHashSet<KeyHolder>();
//		String alias = ApplicationProperties.getValue(String.format("spring.ssl.bundle.jks.%s.keystore.alias", name), "server");
		String password = ApplicationProperties.getValue(String.format("spring.ssl.bundle.jks.%s.keystore.password", name), DEFAULT_KEYSTORE_PASSWORD);
		
		KeyStore serverKeyStore = null, caKeyStore = getCertificateAuthorities();
		
		if(!"default".equals(name)) {
			serverKeyStore = loadCustomCertificates(name, name, password.toCharArray());
		}
		if(Objects.isNull(serverKeyStore)) {
			serverKeyStore = loadPEMCertificates(name);
		}
		
		if(Objects.isNull(serverKeyStore)) {
			serverKeyStore = loadPKSC12Certificates(name, password);
		}
		
		try {
	
			// Add the server key store entries
			Enumeration<String> aliasEnum = serverKeyStore.aliases();
			while (aliasEnum.hasMoreElements()) {
				var aliasName = aliasEnum.nextElement();
				var key = serverKeyStore.getKey(aliasName, password.toCharArray());
				var cert = decodeCertificate(serverKeyStore.getCertificate(aliasName).getEncoded());
				var certChain = decodeCertificates(serverKeyStore.getCertificateChain(aliasName));
				
				KeyHolder entry = null;
				if (key == null) {
					if (cert == null) {
						LOG.warn("Not a key or a cert.");
					} else {
						if(certChain == null)
							entry = new KeyHolder(KeyType.CERTIFICATE, aliasName, key, cert);
						else
							entry = new KeyHolder(KeyType.CERTIFICATE, aliasName, key, certChain);
					}
				} else {
					if (cert == null) {
						entry = new KeyHolder(KeyType.CERTIFICATE, aliasName, key);
					} else {
						if(certChain == null)
							entry = new KeyHolder(KeyType.PRIVATE_KEY_AND_CERTIFICATE, aliasName, key, cert);
						else
							entry = new KeyHolder(KeyType.PRIVATE_KEY_AND_CERTIFICATE, aliasName, key, certChain);
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
				var aliasName = aliasEnum.nextElement();
				var cert =decodeCertificate(caKeyStore.getCertificate(aliasName).getEncoded());
				var entry = new KeyHolder(KeyType.CA, aliasName, null, cert);
				entries.add(entry);
			}
			
			keyEntries.put(name, entries);
			
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateEncodingException e) {
			throw new IOException(e.getMessage(), e);
		}

	}

	private KeyStore loadCustomCertificates(String name, String aliasRequired, char[] passwordRequired) {
		
		for(CertificateProvider provider : App.beans(CertificateProvider.class)) {
			KeyStore ret = provider.loadCertificate(name, aliasRequired, passwordRequired);
			if(Objects.nonNull(ret)) {
				LOG.info("Loaded custom certificate source for {}", name);
				return ret;
			}
		}
		return null;
	}

	private KeyStore loadPKSC12Certificates(String name, String password) throws IOException {
		

		LOG.info("Cannot find default PEM certificate! Looking for PKCS12 keystore");
		
		var serverKeystoreFile = Paths.get(ApplicationProperties.getValue(String.format("spring.ssl.bundle.jks.%s.keystore.location", name), 
				String.format("conf.d/%s/cert.p12", name)));

		try(var in = Files.newInputStream(serverKeystoreFile)) {
			try {
				KeyStore ret = KeyStore.getInstance("PKCS12");
				ret.load(in, password.toCharArray());
				
				LOG.info("Loaded PKCS12 file for {} certificate", name);
				
				return ret;
			} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
				throw new IOException(e.getMessage(), e);
			}
		} 
	}

	private KeyStore loadPEMCertificates(String name) {
		
		String alias = ApplicationProperties.getValue(String.format("jadaptive.sni.%s.keystore.alias", name), name);
		String password = ApplicationProperties.getValue(String.format("jadaptive.sni..%s.keystore.password", name), DEFAULT_KEYSTORE_PASSWORD);
		String pemPath = ApplicationProperties.getValue(String.format("jadaptive.sni.%s.pem.private-key", name), "");
		String pemPassword = ApplicationProperties.getValue(String.format("jadaptive.sni.%s.pem.password", name), "");
		String certPath = ApplicationProperties.getValue(String.format("jadaptive.sni.%s.pem.certificate", name), "");
		if(StringUtils.isNotBlank(pemPath) && StringUtils.isNotBlank(certPath)) {
			Path key = Paths.get(pemPath);
			Path cert = Paths.get(certPath);
			if(Files.exists(key) && Files.exists(cert)) {
				try {
					 KeyStore ret = X509CertificateUtils.loadKeystoreFromPEM(Files.newInputStream(key),
							Files.newInputStream(cert), 
							pemPassword.toCharArray(),
							password.toCharArray(),
							alias);
					 LOG.info("Loaded PEM file for {} certificate", name);
					 return ret;
					
				} catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException
						| InvalidPassphraseException | FileFormatException | MismatchedCertificateException e) {
					LOG.error("Failed to load PEM key/certificate {}", name, e);
				}
			}
		}
		
		return null;
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
