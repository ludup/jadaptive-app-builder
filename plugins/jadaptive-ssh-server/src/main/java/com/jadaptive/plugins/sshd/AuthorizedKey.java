package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;
import com.jadaptive.api.template.UniqueIndex;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;

@Template(name = "Authorized Key", resourceKey = AuthorizedKey.RESOURCE_KEY, aliases = { "userPrivateKeys" },
     type = EntityType.COLLECTION, scope = EntityScope.PERSONAL)
@UniqueIndex(columns = { "ownerUUID", "name" })
public class AuthorizedKey extends PersonalUUIDEntity {

	public static final String RESOURCE_KEY = "authorizedKeys";

	@Column(name = "Name", description = "A name to identify this public key", type = FieldType.TEXT)
	String name;
	
	@Column(name = "Public Key", description = "The formatted public key", type = FieldType.TEXT_AREA)
	String publicKey;
	
	@Column(name = "Fingerprint", description = "The SHA256 fingerprint of the public key", type = FieldType.TEXT, required = false)
	String fingerprint;
	
	@Column(name = "Tags", description = "Tags determine how and when keys can be used", type = FieldType.TEXT, searchable = true, hidden = true)
	Set<String> tags = new HashSet<>();
	
	@Column(name = "Type", description = "The type of key", type = FieldType.ENUM)
	KeyType type;
	
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
		
		try {
			SshPublicKey key = SshKeyUtils.getPublicKey(publicKey);
			this.fingerprint = SshKeyUtils.getFingerprint(key);
			this.publicKey = publicKey;
			this.type = KeyType.fromAlgorithm(key.getAlgorithm());
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

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Collection<String> tags) {
		this.tags = new HashSet<>(tags);
	}

	public KeyType getType() {
		return type;
	}

	public void setType(KeyType type) {
		this.type = type;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
