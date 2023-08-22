package com.jadaptive.app.saml.idp.config;
import static java.util.Arrays.asList;
import static org.springframework.security.saml.saml2.metadata.NameId.EMAIL;
import static org.springframework.security.saml.saml2.metadata.NameId.PERSISTENT;
import static org.springframework.security.saml.saml2.metadata.NameId.UNSPECIFIED;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.key.KeyType;
import org.springframework.security.saml.key.SimpleKey;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.config.NetworkConfiguration;
import org.springframework.security.saml.provider.config.RotatingKeys;
import org.springframework.security.saml.provider.identity.config.ExternalServiceProviderConfiguration;
import org.springframework.security.saml.provider.identity.config.LocalIdentityProviderConfiguration;
import org.springframework.security.saml.saml2.encrypt.DataEncryptionMethod;
import org.springframework.security.saml.saml2.encrypt.KeyEncryptionMethod;
import org.springframework.security.saml.saml2.signature.AlgorithmMethod;
import org.springframework.security.saml.saml2.signature.DigestMethod;

import com.jadaptive.api.servlet.Request;

@Configuration(value = "samlProperties")
public class SAMLProperties extends SamlServerConfiguration {

	static Logger log = LoggerFactory.getLogger(SAMLProperties.class);
	
	public SAMLProperties() {
		setIdentityProvider(generateIdentityProviderConfiguration());
		setNetwork(generateNetworkConfiguration());
	}

	private NetworkConfiguration generateNetworkConfiguration() {
		NetworkConfiguration config = new NetworkConfiguration();
		config.setConnectTimeout(30000);
		config.setReadTimeout(30000);
		return config;
	}

	@Override
	public LocalIdentityProviderConfiguration getIdentityProvider() {
		return super.getIdentityProvider();
	}


	private LocalIdentityProviderConfiguration generateIdentityProviderConfiguration() {
		LocalIdentityProviderConfiguration config = new LocalIdentityProviderConfiguration();
		return config;
//		config.setAlias("my-identity-provider-app");
//		config.setBasePath("https://dev.jadaptive.com");
//		config.setDataEncryptionAlgorithm(DataEncryptionMethod.AES128_CBC);
//		config.setDefaultDigest(DigestMethod.SHA256);
//		config.setDefaultSigningAlgorithm(AlgorithmMethod.RSA_SHA256);
//		config.setEncryptAssertions(false);
//		config.setEntityId("my-identity-provider-app");
//		config.setKeyEncryptionAlgorithm(KeyEncryptionMethod.RSA_1_5);
//		
//		String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n"
//				+ "Proc-Type: 4,ENCRYPTED\n"
//				+ "DEK-Info: DES-EDE3-CBC,DD358F733FD89EA1\n"
//				+ "\n"
//				+ "e/vEctkYs/saPsrQ57djWbW9YZRQFVVAYH9i9yX9DjxmDuAZGjGVxwS4GkdYqiUs\n"
//				+ "f3jdeT96HJPKBVwj88dYaFFO8g4L6CP+ZRN3uiKXGvb606ONp1BtJBvN0b94xGaQ\n"
//				+ "K9q2MlqZgCLAXJZJ7Z5k7aQ2NWE7u+1GZchQSVo308ynsIptxpgqlpMZsh9oS21m\n"
//				+ "V5SKs03mNyk2h+VdJtch8nWwfIHYcHn9c0pDphbaN3eosnvtWxPfSLjo274R+zhw\n"
//				+ "RA3KNp2bdyfidluTXj40GOYObjfcm1g3sSMgZZqpY3EQUc8DEokfXQZghfBvoEe/\n"
//				+ "GB0k/+StrFNl0qAdOrA6PBndlySp6STwQVAsKsKlJneRO3nAHMlZ7kenHgPunACI\n"
//				+ "IYKIPqPKGVTm1k2FuEPDuwsneEStiThtlvQ4Nu+k6hbuplaKlZ8C2xsubzVQ3rFU\n"
//				+ "KNEhU65DagDH9wR9FzEXpTYUgwrr2vNRyd0TqcSxUpUx4Ra0f3gp5/kojufD8i1y\n"
//				+ "Fs88e8L3g1to1hCsz8yIYIiFjYNf8CuH8myDd2KjqJlyL8svKi+M2pPYl9vY1m8L\n"
//				+ "u4/3ZPMrGUvtAKixBZNzj95HPX0UtmC2kPMAvdvgzaPlDeH5Ee0rzPxnHI21lmyd\n"
//				+ "O6Sb3tc/DM9xbCCQVN8OKy/pgv1PpHMKwEE7ELpDRoVWS8DzZ43Xfy1Rm8afADAv\n"
//				+ "39oj4Gs08FblaHnOSP8WOr4r9SZbF1qmlMw7QkHeaF+MJzmG3d0t2XsDzKfc510m\n"
//				+ "gEbiD/L3Z8czwXM5g2HciAMOEVhZQJvK62KwMyOmNqBnEThBN+apsQ==\n"
//				+ "-----END RSA PRIVATE KEY-----";
//		String certificate = "-----BEGIN CERTIFICATE-----\n"
//				+ "MIIChTCCAe4CCQDo0wjPUK8sMDANBgkqhkiG9w0BAQsFADCBhjELMAkGA1UEBhMC\n"
//				+ "VVMxEzARBgNVBAgMCldhc2hpbmd0b24xEjAQBgNVBAcMCVZhbmNvdXZlcjEdMBsG\n"
//				+ "A1UECgwUU3ByaW5nIFNlY3VyaXR5IFNBTUwxDDAKBgNVBAsMA2lkcDEhMB8GA1UE\n"
//				+ "AwwYaWRwLnNwcmluZy5zZWN1cml0eS5zYW1sMB4XDTE4MDUxNDE0NTUyMVoXDTI4\n"
//				+ "MDUxMTE0NTUyMVowgYYxCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApXYXNoaW5ndG9u\n"
//				+ "MRIwEAYDVQQHDAlWYW5jb3V2ZXIxHTAbBgNVBAoMFFNwcmluZyBTZWN1cml0eSBT\n"
//				+ "QU1MMQwwCgYDVQQLDANpZHAxITAfBgNVBAMMGGlkcC5zcHJpbmcuc2VjdXJpdHku\n"
//				+ "c2FtbDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA2EuygAucRBWtYifgEH/E\n"
//				+ "rVUive4dZdqo72Bze4MbkPuTKLrMCLB6IXxt1p5lu+tr0JxOiRO3KFVOO3D0l+j9\n"
//				+ "zOow4g+JdoMQsjSzA6HtL/D9ZjXP6iUxFCYx+qmnVl3X9ipBD/HVKOBlzIqeXTSa\n"
//				+ "5D17uxPQVxK64UDOI3CyY4cCAwEAATANBgkqhkiG9w0BAQsFAAOBgQAj+6b6dlA6\n"
//				+ "SitTfz44LdnFSW9mYaeimwPP8ZtU7/3EJCzLd5eq7N/0kYPNVclZvB45I0UMT77A\n"
//				+ "HWrNyScm56MTcEpSuHhJHAqRAgJKbciCTNsFI928EqiWSmu//w0ASBN3bVa8nv8/\n"
//				+ "rafuutCq3RskTkHVZnbT5Xa6ITEZxSncow==\n"
//				+ "-----END CERTIFICATE-----";
//		String passphrase = "idppassword";
//		
//		RotatingKeys keys = new RotatingKeys();
//		keys.setActive(new SimpleKey("default", privateKey, certificate, passphrase, KeyType.SIGNING));
//		config.setKeys(keys);
//		
//		config.setNameIds(
//					asList(
//						PERSISTENT,
//						EMAIL,
//						UNSPECIFIED
//					)
//		);
//		config.setPrefix("/saml/idp");
//
//		config.setSignAssertions(true);
//		config.setSignMetadata(true);
//		config.setSingleLogoutEnabled(true);
//		config.setWantRequestsSigned(true);
//
//		ExternalServiceProviderConfiguration provider = new ExternalServiceProviderConfiguration();
//		provider.setAlias("local-sp1");
//		provider.setMetadata("http://localhost:8082/this-is-sp/saml/sp/metadata");
//		provider.setLinktext("Test Service Provider Application");
//		
//		config.setProviders(Arrays.asList(provider));
//		
//		return config;
	}
}