package com.jadaptive.app.encrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jadaptive.api.encrypt.EncryptionProvider;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.repository.RepositoryException;

@Service
public class EncryptionServiceImpl implements EncryptionService {

	private static Logger log = LoggerFactory.getLogger(EncryptionServiceImpl.class);
	
	public static final String ENCRYPTION_MARKER = "!!ENC!!";
	public static final String ENCRYPTION_MARKER_EXTERNAL_KEY = "!!EWK!!";
	
	private final List<EncryptionProvider> encryptionProviders = new ArrayList<>();
	
	@Autowired
	private ApplicationContext context;
	
	@PostConstruct
	private void postConstruct() {
		log.info("Looking for encryption providers ..");
		for(var provider : context.getBeansOfType(EncryptionProvider.class).values().stream().sorted((p1,p2) -> {
			return Integer.valueOf(p1.priority()).compareTo(p2.priority());
		}).toList()) {
			try {
				provider.init();
				encryptionProviders.add(provider);
				log.info("  {} - Activated.", provider.getClass().getName());
				break;
			}
			catch(IllegalArgumentException e) {
				log.error("  {} - Ignoring. {}", provider.getClass().getName(), e.getMessage());
				if(log.isDebugEnabled()) {
					log.error("Failed to init provider.", e);
				}
			}
			catch(RuntimeException re) {
				throw re;
			}
			catch(Exception e) {
				throw new IllegalStateException("Failed to configure encryption service. As a precaution, the service will be halted.", e);
			}
		}
		
		if(encryptionProviders.isEmpty()) {
			throw new IllegalStateException("No encryption provider. Cannot continue.");
		}
	}
	
	@Override
	public boolean isEncrypted(String value) {
		return value.startsWith(ENCRYPTION_MARKER);
	}
	
	@Override
	public boolean isEncryptedWithPassword(String value) {
		return value.startsWith(ENCRYPTION_MARKER_EXTERNAL_KEY);
	}
	
	@Override
	public String encryptString(String value, String base64Key, String base64Iv) {
		
    	try {
			byte[] rawkey = Base64.getDecoder().decode(base64Key);
			byte[] rawiv = Base64.getDecoder().decode(base64Iv);
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			SecretKeySpec kspec = new SecretKeySpec(rawkey, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, kspec, new IvParameterSpec(rawiv));
			
			return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes("UTF-8")));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			throw new RepositoryException(e);
		}
	}
	
	@Override
	public String encrypt(String value) {

		if(isEncrypted(value)) {
			return value;
		}
		
		try {
			int keyLength = Math.min(Cipher.getMaxAllowedKeyLength("AES"), 256) / 8;
			
			SecureRandom rnd = new SecureRandom();
			byte[] rawkey = new byte[keyLength];
			rnd.nextBytes(rawkey);

			byte[] iv = new byte[16];
			rnd.nextBytes(iv);

			StringBuffer buffer = new StringBuffer();
			buffer.append(Base64.getEncoder().encodeToString(rawkey));
			buffer.append("|");
			buffer.append(Base64.getEncoder().encodeToString(iv));
			buffer.append("|");
			buffer.append(Base64.getEncoder().encodeToString(encryptAES(value, rawkey, iv)));
			
			return ENCRYPTION_MARKER.concat(encryptionProviders.get(0).encrypt(buffer.toString()));
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	@Override
	public String encrypt(String value, String keydata) {

		if(isEncryptedWithPassword(value)) {
			return value;
		}
		
		try {
			int keyLength = Math.min(Cipher.getMaxAllowedKeyLength("AES"), 256) / 8;
			
			byte[] data = DigestUtils.sha3_512(keydata);
			
			byte[] rawkey = new byte[keyLength];
			System.arraycopy(data, 0, rawkey, 0, rawkey.length);

			byte[] iv = new byte[16];
			System.arraycopy(data, rawkey.length, iv, 0, iv.length);

			StringBuffer buffer = new StringBuffer();
			buffer.append(Base64.getEncoder().encodeToString(encryptAES(value, rawkey, iv)));
			
			return ENCRYPTION_MARKER_EXTERNAL_KEY.concat(encryptionProviders.get(0).encrypt(buffer.toString()));
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	@Override
	public String decrypt(String value, String keydata) {
		
		if(!isEncryptedWithPassword(value)) {
			return value;
		}
		
		RepositoryException  exception = null;
		
		for(var encryptionProvider : encryptionProviders) {
			try {
				String encodedData = encryptionProvider.decrypt(value.substring(ENCRYPTION_MARKER_EXTERNAL_KEY.length()));
	
				byte[] encrypted = Base64.getDecoder().decode(encodedData);
				byte[] data = DigestUtils.sha3_512(keydata);
				
				int keyLength = Math.min(Cipher.getMaxAllowedKeyLength("AES"), 256) / 8;
				byte[] rawkey = new byte[keyLength];
				System.arraycopy(data, 0, rawkey, 0, rawkey.length);
	
				byte[] iv = new byte[16];
				System.arraycopy(data, rawkey.length, iv, 0, iv.length);
				
				String tmp = new String(decryptAES(encrypted, rawkey, iv), "UTF-8");
				return tmp;
			} catch (Exception e) {
				exception = new RepositoryException(e.getMessage(), e);
			}
		}
		
		throw exception;
	}
	
	@Override
	public String decrypt(String value) {
		
		if(!isEncrypted(value)) {
			return value;
		}
		
		RepositoryException  exception = null;
		
		for(var encryptionProvider : encryptionProviders) {
			try {
				String data = encryptionProvider.decrypt(value.substring(ENCRYPTION_MARKER.length()));
				String[] elements = data.split("\\|");
				byte[] key = Base64.getDecoder().decode(elements[0]);
				byte[] iv = Base64.getDecoder().decode(elements[1]);
				byte[] encrypted = Base64.getDecoder().decode(elements[2]);
				
				String tmp = new String(decryptAES(encrypted, key, iv), "UTF-8");
				return tmp;
			} catch (Exception e) {
				exception = new RepositoryException(e.getMessage(), e);
			}
		}
		
		throw exception;
	}
	
	private byte[] encryptAES(String value, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		
		Cipher aesCipherForEncryption = Cipher
				.getInstance("AES/CTR/PKCS7PADDING", "BC");

		SecretKey secretKeySpec = new SecretKeySpec(key, "AES");
		aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKeySpec,
				new IvParameterSpec(iv));

		byte[] byteDataToEncrypt = value.getBytes("UTF-8");
		return aesCipherForEncryption.doFinal(byteDataToEncrypt);
	}
	
	private byte[] decryptAES(byte[] value, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		
		Cipher aesCipherForEncryption = Cipher
				.getInstance("AES/CTR/PKCS7PADDING", "BC");

		SecretKey secretKeySpec = new SecretKeySpec(key, "AES");
		aesCipherForEncryption.init(Cipher.DECRYPT_MODE, secretKeySpec,
				new IvParameterSpec(iv));

		return aesCipherForEncryption.doFinal(value);
	}

	
	public static void main(String[] args) throws IOException {
		
		Security.addProvider(new BouncyCastleProvider());
		
		EncryptionServiceImpl service = new EncryptionServiceImpl();
		
		String encrypted = service.encrypt("The old brown cow looked over at the sly fox and said how now brown cow");
	
		System.out.println(encrypted);
		
		System.out.println(service.decrypt(encrypted));
	}

}
