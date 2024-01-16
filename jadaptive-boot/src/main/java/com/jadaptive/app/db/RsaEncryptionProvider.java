package com.jadaptive.app.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.crypto.Cipher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RsaEncryptionProvider {

	static public RsaEncryptionProvider instance;
	
	static Logger log = LoggerFactory.getLogger(RsaEncryptionProvider.class);
	
	private File privateFolder = new File("conf", "private");
	private File prvFile = new File(privateFolder, "secrets");
	private File pubFile = new File(privateFolder, "secrets.pub");
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	public RsaEncryptionProvider() throws Exception {
		
		try {
			loadKeys();
		} catch(Exception e) {
			generateKeys();
		}
	}
	
	public RsaEncryptionProvider(File prvFile, File pubFile) throws Exception {
		this.prvFile = prvFile;
		this.pubFile = pubFile;
		loadKeys();
	}
	
	private void generateKeys() throws Exception {
		
		privateFolder.mkdirs();
		
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair key = gen.generateKeyPair();
		privateKey = key.getPrivate();
		publicKey = key.getPublic();

		try(FileOutputStream pvt = new FileOutputStream(prvFile)) {
		    pvt.write(privateKey.getEncoded());
		    pvt.flush();
		}
	
		try(FileOutputStream pub = new FileOutputStream(pubFile)) {
		    pub.write(publicKey.getEncoded());
		    pub.flush();
		}

		setOwnerPermissions(prvFile.toPath());
		setOwnerPermissions(pubFile.toPath());
		setOwnerPermissions(privateFolder.toPath());
	}
	
	private void setOwnerPermissions(Path path) throws IOException {
		
		try {
			if(!SystemUtils.IS_OS_WINDOWS) {
				
//				AclFileAttributeView aclAttr = Files.getFileAttributeView(path, AclFileAttributeView.class);
//				
//				UserPrincipalLookupService upls = path.getFileSystem().getUserPrincipalLookupService();
//				UserPrincipal user = 
//				AclEntry.Builder builder = AclEntry.newBuilder();       
//				builder.setPermissions( EnumSet.of(AclEntryPermission.READ_DATA, AclEntryPermission.EXECUTE, 
//				        AclEntryPermission.READ_ACL, AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.READ_NAMED_ATTRS,
//				        AclEntryPermission.WRITE_ACL, AclEntryPermission.DELETE
//				));
//				builder.setPrincipal(user);
//				builder.setType(AclEntryType.ALLOW);
//				aclAttr.setAcl(Collections.singletonList(builder.build()));
//				
//				if(log.isInfoEnabled()) {
//					log.info("Set strict permissions on {}", path.toAbsolutePath());
//				}
//			} else {
				
				Set<PosixFilePermission> ownerWritable;
				if(Files.isDirectory(path)) {
					ownerWritable = PosixFilePermissions.fromString("rwx------");
				} else {
					ownerWritable = PosixFilePermissions.fromString("rw-------");
				}
				Files.setPosixFilePermissions(path, ownerWritable);
				
				if(log.isInfoEnabled()) {
					log.info("Set strict permissions on {}", path.toAbsolutePath());
				}
			}
			
		} catch (Throwable e) {
			log.warn("Could not set strict permissions on private keys", e);
		}
	}
	
	private void loadKeys() throws Exception {
		
		KeyFactory kf = KeyFactory.getInstance("RSA");
		
		try(InputStream in = new FileInputStream(prvFile)) {
			byte[] prvBytes = IOUtils.toByteArray(in);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(prvBytes);
			privateKey = kf.generatePrivate(privateKeySpec);
		}

		try(InputStream in = new FileInputStream(pubFile)) {
			byte[] pubBytes = IOUtils.toByteArray(in);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubBytes);
			publicKey = kf.generatePublic(publicKeySpec);
		}
	}
	
	
	public static RsaEncryptionProvider getInstance() throws Exception {
		return instance==null ? instance = new RsaEncryptionProvider() : instance;
	}
	
	public int getLength() {
		return 128;
	}
	
	public String encrypt(String toEncrypt) throws Exception {
		
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
	
	public String decrypt(String toDecrypt) throws Exception {
		
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