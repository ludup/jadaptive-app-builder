package com.jadaptive.plugins.keys;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.IncludeView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.Table;
import com.jadaptive.api.template.UniqueIndex;


@ObjectDefinition(
	resourceKey = AuthorizedKey.RESOURCE_KEY, 
	 aliases = { "userPrivateKeys" },
     type = ObjectType.COLLECTION, 
     scope = ObjectScope.PERSONAL)
@UniqueIndex(columns = { "ownerUUID", "name" })
@Table(defaultColumns = { "name", "fingerprint", "type" }, optionalColumns = { "id" })
public class AuthorizedKey extends PersonalUUIDEntity {

	private static final long serialVersionUID = 9215617764035887442L;

	public static final String RESOURCE_KEY = "authorizedKeys";
	
	@ObjectField(name = "Key ID", 
			description = "The ID of this key", 
			searchable = true, 
			hidden = true, type = FieldType.LONG)
	@ExcludeView(values = { FieldView.CREATE })
	Long id;
	
	@ObjectField(name = "Name", description = "A name to identify this public key", type = FieldType.TEXT)
	String name;
	
	@ObjectField(name = "Public Key", description = "The formatted public key", readOnly = true, type = FieldType.TEXT_AREA)
	@ExcludeView(values = { FieldView.TABLE })
	String publicKey;
	
	@ObjectField(name = "Fingerprint", description = "The SHA256 fingerprint of the public key", readOnly = true,  type = FieldType.TEXT, required = false)
	@ExcludeView(values = { FieldView.CREATE })
	String fingerprint;
	
	@ObjectField(name = "Tags", description = "Tags determine how and when keys can be used", readOnly = true, type = FieldType.TEXT, searchable = true)
	@IncludeView(values = { FieldView.UPDATE, FieldView.READ })
	Set<String> tags = new HashSet<>();
	
	@ObjectField(name = "Type", description = "The type of key", readOnly = true, type = FieldType.TEXT)
	@ExcludeView(values = { FieldView.CREATE })
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
		
		/**
		 * TODO change this to store
		 */
		if(tags.contains(AuthorizedKeyService.SYSTEM_TAG)) {
			return AuthorizedKeyService.SYSTEM_TAG;
		}
		if(tags.contains(AuthorizedKeyService.WEBAUTHN_TAG)) {
			return AuthorizedKeyService.WEBAUTHN_TAG;
		}
		return AuthorizedKeyService.SSH_TAG;
 	}
	
}
