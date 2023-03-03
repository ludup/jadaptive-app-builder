package com.jadaptive.app.encrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

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
import org.springframework.stereotype.Service;

import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.app.db.RsaEncryptionProvider;

@Service
public class EncryptionServiceImpl implements EncryptionService {

	static Logger log = LoggerFactory.getLogger(EncryptionServiceImpl.class);
	
	public static final String ENCRYPTION_MARKER = "!!ENC!!";
	public static final String ENCRYPTION_MARKER_EXTERNAL_KEY = "!!EWK!!";
	
	public static EncryptionService instance;
	
	@PostConstruct
	private void postConstruct() {
		instance = this;
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
			
			return ENCRYPTION_MARKER.concat(RsaEncryptionProvider.getInstance().encrypt(buffer.toString()));
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
			
			return ENCRYPTION_MARKER_EXTERNAL_KEY.concat(RsaEncryptionProvider.getInstance().encrypt(buffer.toString()));
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	@Override
	public String decrypt(String value, String keydata) {
		
		if(!isEncryptedWithPassword(value)) {
			return value;
		}
		
		try {
			String encodedData = RsaEncryptionProvider.getInstance().decrypt(value.substring(ENCRYPTION_MARKER_EXTERNAL_KEY.length()));

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
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	@Override
	public String decrypt(String value) {
		
		if(!isEncrypted(value)) {
			return value;
		}
		
		try {
			String data = RsaEncryptionProvider.getInstance().decrypt(value.substring(ENCRYPTION_MARKER.length()));
			String[] elements = data.split("\\|");
			byte[] key = Base64.getDecoder().decode(elements[0]);
			byte[] iv = Base64.getDecoder().decode(elements[1]);
			byte[] encrypted = Base64.getDecoder().decode(elements[2]);
			
			String tmp = new String(decryptAES(encrypted, key, iv), "UTF-8");
			return tmp;
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(), e);
		}
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

	public static EncryptionService getInstance() {
		return instance;
	}
}
