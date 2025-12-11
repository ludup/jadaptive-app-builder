package com.jadaptive.app.encrypt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.encrypt.EncryptionProvider;

public abstract class AbstractEncryptionProvider implements EncryptionProvider {

	static Logger log = LoggerFactory.getLogger(AbstractEncryptionProvider.class);

	protected PrivateKey privateKey;
	protected PublicKey publicKey;

	public abstract int getLength();

	@Override
	public final String encrypt(String toEncrypt) throws Exception {

		int pos = 0;
		StringBuffer ret = new StringBuffer();
		while(pos < toEncrypt.length()) {
			int count = Math.min(toEncrypt.length() - pos, getLength());
			ret.append(doEncrypt(toEncrypt.substring(pos, pos+count)));
			ret.append('|');
			pos += count;
		}
		return ret.toString();
	}

	private String doEncrypt(String toEncrypt) throws Exception {

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
		c.init(Cipher.ENCRYPT_MODE, privateKey);
		return Base64.getEncoder().encodeToString(c.doFinal(toEncrypt.getBytes("UTF-8")));

	}

	@Override
	public final String decrypt(String toDecrypt) throws Exception {

		StringBuffer ret = new StringBuffer();
		StringTokenizer t = new StringTokenizer(toDecrypt, "|");

		while(t.hasMoreTokens()) {

			String data = t.nextToken();
			ret.append(doDecrypt(data));
		}

		return ret.toString();
	}

	private String doDecrypt(String toDecrypt) throws Exception {

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
		c.init(Cipher.DECRYPT_MODE, publicKey);
		return new String(c.doFinal(Base64.getDecoder().decode(toDecrypt)), "UTF-8");
	}
}