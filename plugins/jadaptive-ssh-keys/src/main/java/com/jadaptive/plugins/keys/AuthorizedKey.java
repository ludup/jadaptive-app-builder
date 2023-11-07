package com.jadaptive.plugins.keys;

import java.util.Date;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.DisableStandardActions;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.UniqueIndex;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;


@ObjectDefinition(
	 resourceKey = AuthorizedKey.RESOURCE_KEY, 
	 aliases = { "userPrivateKeys" },
	 creatable = false,
	 updatable = false,
	 defaultColumn = "name",
     type = ObjectType.COLLECTION, 
     scope = ObjectScope.PERSONAL)
@UniqueIndex(columns = { "ownerUUID", "name" })
@ObjectServiceBean(bean = AuthorizedKeyService.class)
@DisableStandardActions
@GenerateEventTemplates(value = AuthorizedKey.RESOURCE_KEY)
@ObjectViews(@ObjectViewDefinition(bundle = AuthorizedKey.RESOURCE_KEY, value = AuthorizedKey.KEY_VIEW))
@TableView(defaultColumns = { "name", "fingerprint", "type", "expires" })
@TableAction(resourceKey = "generateKey", bundle = AuthorizedKey.RESOURCE_KEY, url = "generate-key", icon = "fa-solid fa-wrench", writeAction = true)
@TableAction(resourceKey = "uploadPublicKey",  bundle = AuthorizedKey.RESOURCE_KEY, url = "upload-key", icon = "fa-solid fa-upload", writeAction = true)
public class AuthorizedKey extends PersonalUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 9215617764035887442L;

	public static final String RESOURCE_KEY = "authorizedKeys";
	public static final String KEY_VIEW = "key";
	public static final String PERSONAL_KEYS_PERMISSION = "authorizedKeys.read";
	
	@ObjectField(type = FieldType.TEXT, searchable = true)
	@Validator(type = ValidationType.REQUIRED)
	String name;
	
	@ObjectField(type = FieldType.DATE)
	@ObjectView(value = "", renderer = FieldRenderer.OPTIONAL)
	Date expires;
	
	@ObjectField(readOnly = true, type = FieldType.TEXT_AREA)
	@ExcludeView(values = { FieldView.TABLE })
	@ObjectView(KEY_VIEW)
	String publicKey;
	
	@ObjectField(readOnly = true,  type = FieldType.TEXT, searchable = true, unique = true)
	@ExcludeView(values = { FieldView.CREATE })
	@ObjectView(KEY_VIEW)
	String fingerprint;
	
	@ObjectField(readOnly = true, type = FieldType.TEXT, searchable = true)
	@ExcludeView(values = { FieldView.CREATE })
	@ObjectView(KEY_VIEW)
	String type;
	
	@ObjectField(readOnly = true,  type = FieldType.INTEGER)
	@ObjectView(value = KEY_VIEW, renderer = FieldRenderer.OPTIONAL)
	Integer bits;
	
//	@ObjectField(readOnly = true, type = FieldType.BOOL)
//	@ExcludeView(values = { FieldView.CREATE })
//	@ObjectView(KEY_VIEW)
//	Boolean deviceKey;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
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

//	public void setDeviceKey(boolean deviceKey) {
//		this.deviceKey = deviceKey;
//	}
//
//	public boolean getDeviceKey() {
//		return deviceKey;
//	}

	public Integer getBits() {
		return bits;
	}

	public void setBits(Integer bits) {
		this.bits = bits;
	}
}
