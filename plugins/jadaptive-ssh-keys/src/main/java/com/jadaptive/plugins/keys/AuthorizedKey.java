package com.jadaptive.plugins.keys;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.DisableStandardActions;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.IncludeView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.UniqueIndex;


@ObjectDefinition(
	resourceKey = AuthorizedKey.RESOURCE_KEY, 
	 aliases = { "userPrivateKeys" },
     type = ObjectType.COLLECTION, 
     scope = ObjectScope.PERSONAL)
@UniqueIndex(columns = { "ownerUUID", "name" })
@ObjectServiceBean(bean = AuthorizedKeyService.class)
@DisableStandardActions
@TableView(defaultColumns = { "name", "fingerprint", "type" }, optionalColumns = { "id" },
			actions = {
				@TableAction(resourceKey = "generateKey", url = "generate/public-key"),
				@TableAction(resourceKey = "uploadPublicKey", url = "import/public-key")})
public class AuthorizedKey extends PersonalUUIDEntity {

	private static final long serialVersionUID = 9215617764035887442L;

	public static final String RESOURCE_KEY = "authorizedKeys";
	
	@ObjectField(searchable = true, alternativeId = true,
			hidden = true, type = FieldType.LONG)
	@ExcludeView(values = { FieldView.CREATE, FieldView.TABLE, FieldView.IMPORT })
	Long id;
	
	@ObjectField(type = FieldType.TEXT)
	String name;
	
	@ObjectField(type = FieldType.TEXT_AREA, unique = true)
	@ExcludeView(values = { FieldView.TABLE })
	String publicKey;
	
	@ObjectField(readOnly = true,  type = FieldType.TEXT, required = false, unique = true)
	@ExcludeView(values = { FieldView.CREATE })
	String fingerprint;
	
	@ObjectField(readOnly = true, type = FieldType.TEXT, searchable = true)
	@IncludeView(values = { FieldView.UPDATE, FieldView.READ })
	Set<String> tags = new HashSet<>();
	
	@ObjectField(readOnly = true, type = FieldType.TEXT)
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
