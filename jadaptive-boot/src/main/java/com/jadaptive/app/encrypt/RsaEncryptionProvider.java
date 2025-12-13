package com.jadaptive.app.encrypt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;

@Component
public class RsaEncryptionProvider extends AbstractEncryptionProvider {

	private static Logger LOG = LoggerFactory.getLogger(RsaEncryptionProvider.class);

	private Path privateFolder;
	private Path prvFile;
	private Path pubFile;
	private boolean enabled;

	@Override
	public int priority() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void init() throws Exception {
		enabled = ApplicationProperties.getValue("private.enabled", true);
		if(!enabled) {
			throw new IllegalArgumentException("Local private key disabled.");
		}
		
		privateFolder = Paths.get(ApplicationProperties.getValue("private.dir", "conf")).
				resolve(ApplicationProperties.getValue("private.conf", "private"));

		prvFile = privateFolder.resolve(ApplicationProperties.getValue("private.filename", "secrets"));
		pubFile = privateFolder.resolve(ApplicationProperties.getValue("private.filename", prvFile.getFileName().toString() + ".pub"));
		
		
		try {
			loadKeys();
		} catch(FileNotFoundException e) {
			generateKeys();
		}
	}

	public Path getPrvFile() {
		return prvFile;
	}

	public Path getPubFile() {
		return pubFile;
	}

	private void generateKeys() throws Exception {

		Files.createDirectories(privateFolder);

		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair key = gen.generateKeyPair();
		privateKey = key.getPrivate();
		publicKey = key.getPublic();

		try(var pvt = Files.newOutputStream(prvFile)) {
		    pvt.write(privateKey.getEncoded());
		    pvt.flush();
		}

		try(var pub = Files.newOutputStream(pubFile)) {
		    pub.write(publicKey.getEncoded());
		    pub.flush();
		}

		setOwnerPermissions(prvFile);
		setOwnerPermissions(pubFile);
		setOwnerPermissions(privateFolder);
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
//				if(LOG.isInfoEnabled()) {
//					LOG.info("Set strict permissions on {}", path.toAbsolutePath());
//				}
//			} else {

				Set<PosixFilePermission> ownerWritable;
				if(Files.isDirectory(path)) {
					ownerWritable = PosixFilePermissions.fromString("rwx------");
				} else {
					ownerWritable = PosixFilePermissions.fromString("rw-------");
				}
				Files.setPosixFilePermissions(path, ownerWritable);

				if(LOG.isInfoEnabled()) {
					LOG.info("Set strict permissions on {}", path.toAbsolutePath());
				}
			}

		} catch (Throwable e) {
			LOG.warn("Could not set strict permissions on private keys", e);
		}
	}

	private void loadKeys() throws Exception {

		KeyFactory kf = KeyFactory.getInstance("RSA");

		try(var in = Files.newInputStream(prvFile)) {
			
			var prvBytes = IOUtils.toByteArray(in);
			var privateKeySpec = new PKCS8EncodedKeySpec(prvBytes);
			
			privateKey = kf.generatePrivate(privateKeySpec);
		}

		try(var in = Files.newInputStream(pubFile)) {
			
			var pubBytes = IOUtils.toByteArray(in);
			var publicKeySpec = new X509EncodedKeySpec(pubBytes);
			
			publicKey = kf.generatePublic(publicKeySpec);
		}
	}

	@Override
	public int getLength() {
		return 128;
	}

	public boolean isEnabled() {
		return enabled;
	}
}