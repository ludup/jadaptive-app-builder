package com.jadaptive.plugins.keys;

import java.util.UUID;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.DisableStandardActions;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.UniqueIndex;


@ObjectDefinition(
	 resourceKey = AuthorizedKey.RESOURCE_KEY, 
	 aliases = { "userPrivateKeys" },
	 creatable = false,
     type = ObjectType.COLLECTION, 
     scope = ObjectScope.PERSONAL)
@UniqueIndex(columns = { "ownerUUID", "name" })
@ObjectServiceBean(bean = AuthorizedKeyService.class)
@DisableStandardActions
@TableView(defaultColumns = { "name", "fingerprint", "type" }, optionalColumns = { "id" },
			actions = {
				@TableAction(resourceKey = "generateKey", bundle = AuthorizedKey.RESOURCE_KEY, url = "generate-key"),
				@TableAction(resourceKey = "uploadPublicKey",  bundle = AuthorizedKey.RESOURCE_KEY, url = "upload-key")})
public class AuthorizedKey extends PersonalUUIDEntity {

	private static final long serialVersionUID = 9215617764035887442L;

	public static final String RESOURCE_KEY = "authorizedKeys";
	
	@ObjectField(searchable = true, alternativeId = true,
			hidden = true, type = FieldType.LONG)
	@ExcludeView(values = { FieldView.CREATE, FieldView.TABLE, FieldView.IMPORT })
	Long id;
	
	@ObjectField(type = FieldType.TEXT)
	String name;
	
	@ObjectField(readOnly = true, type = FieldType.TEXT_AREA, unique = true)
	@ExcludeView(values = { FieldView.TABLE })
	String publicKey;
	
	@ObjectField(readOnly = true,  type = FieldType.TEXT, required = false, unique = true)
	@ExcludeView(values = { FieldView.CREATE })
	String fingerprint;
	
	@ObjectField(readOnly = true, type = FieldType.TEXT)
	@ExcludeView(values = { FieldView.CREATE })
	String type;
	
	@ObjectField(readOnly = true, type = FieldType.BOOL)
	@ExcludeView(values = { FieldView.CREATE })
	Boolean deviceKey;
	
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

	public void setDeviceKey(boolean deviceKey) {
		this.deviceKey = deviceKey;
	}

	public boolean getDeviceKey() {
		return deviceKey;
	}
	
}
