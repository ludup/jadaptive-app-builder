package com.jadaptive.api.sshd;

import java.io.IOException;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.Entity;
import com.jadaptive.api.template.Member;
import com.jadaptive.api.template.FieldType;
import com.sshtools.common.publickey.SshKeyUtils;

@Entity(name = "Authorized Key", resourceKey = "authorizedKeys", type = EntityType.COLLECTION, scope = EntityScope.PERSONAL)
public class AuthorizedKey extends PersonalUUIDEntity {

	@Member(name = "Name", description = "A name to identify this public key", type = FieldType.TEXT)
	String name;
	
	@Member(name = "Public Key", description = "The formatted public key", type = FieldType.TEXT_AREA)
	String publicKey;
	
	@Member(name = "Fingerprint", description = "The SHA256 fingerprint of the public key", type = FieldType.TEXT, required = false)
	String fingerprint;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
		try {
			this.fingerprint = SshKeyUtils.getFingerprint(SshKeyUtils.getPublicKey(publicKey));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	public String getFingerprint() {
		return fingerprint;
	}
	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	
}
