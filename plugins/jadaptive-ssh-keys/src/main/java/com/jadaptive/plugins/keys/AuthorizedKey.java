package com.jadaptive.plugins.keys;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;
import com.jadaptive.api.template.UniqueIndex;


@Template(name = "Authorized Key", resourceKey = AuthorizedKey.RESOURCE_KEY, aliases = { "userPrivateKeys" },
     type = EntityType.COLLECTION, scope = EntityScope.PERSONAL)
@UniqueIndex(columns = { "ownerUUID", "name" })
public class AuthorizedKey extends PersonalUUIDEntity {

	public static final String RESOURCE_KEY = "authorizedKeys";
	
	@Column(name = "Key ID", 
			description = "The ID of this key", 
			searchable = true, 
			hidden = true, type = FieldType.LONG)
	Long id;
	
	@Column(name = "Name", description = "A name to identify this public key", type = FieldType.TEXT)
	String name;
	
	@Column(name = "Public Key", description = "The formatted public key", readOnly = true, type = FieldType.TEXT_AREA)
	String publicKey;
	
	@Column(name = "Fingerprint", description = "The SHA256 fingerprint of the public key", readOnly = true,  type = FieldType.TEXT, required = false)
	String fingerprint;
	
	@Column(name = "Tags", description = "Tags determine how and when keys can be used", readOnly = true, type = FieldType.TEXT, searchable = true)
	Set<String> tags = new HashSet<>();
	
	@Column(name = "Type", description = "The type of key", readOnly = true, type = FieldType.TEXT)
	String type;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setUuid(String uuid) {
		super.setUuid(uuid);
		this.id = UUID.fromString(getUuid()).getMostSignificantBits() & Long.MAX_VALUE;
	}
	
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public String getKeyType() {
		if(tags.contains(AuthorizedKeyService.SYSTEM_TAG)) {
			return AuthorizedKeyService.SYSTEM_TAG;
		}
		if(tags.contains(AuthorizedKeyService.WEBAUTHN_TAG)) {
			return AuthorizedKeyService.WEBAUTHN_TAG;
		}
		return AuthorizedKeyService.SSH_TAG;
 	}
	
}