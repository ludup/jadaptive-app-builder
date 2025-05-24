package com.jadaptive.api.user;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.jadaptive.api.db.Transactional;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.permissions.FeatureGroup;
import com.jadaptive.api.permissions.LicensedFeature;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.DynamicColumn;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldOptions;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.Validators;
import com.jadaptive.utils.ITokenResolver;
import com.jadaptive.utils.StaticResolver;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION)
@ObjectServiceBean(bean = UserService.class)
@ObjectViewDefinition(bundle = "users", value = User.DETAILS_VIEW, weight=0)
@ObjectViewDefinition(bundle = "users", value = User.EMAIL_VIEW, weight=100)
@ObjectViewDefinition(bundle = "users", value = User.PHONE_VIEW, weight=200)
@TableView(defaultColumns = { "avatar", "username", "enabled", "name", "email", "lastLogin" }, otherColumns = {
		@DynamicColumn(resourceKey = "avatar", service = UserService.class) },
requiresUpdate = true, sortField = "username")
@TableAction(resourceKey = "enableUser", bundle = User.RESOURCE_KEY,  icon = "fa-user-unlock", target = Target.ROW, url = "/app/api/accounts/enable/{uuid}", filter = DisabledAccountAction.class, permissions = "users.write")
@TableAction(resourceKey = "disableUser", bundle = User.RESOURCE_KEY,  icon = "fa-user-lock", target = Target.ROW, url = "/app/api/accounts/disable/{uuid}", filter = EnabledAccountAction.class, permissions = "users.write")
@TableAction(resourceKey = "impersonateUser", bundle = User.RESOURCE_KEY,  icon = "fa-mask", target = Target.ROW, url = "/app/ui/impersonateUser/{uuid}", permissions = "tenant.read")
@Transactional
@GenerateEventTemplates(User.RESOURCE_KEY)
@LicensedFeature(group = FeatureGroup.FOUNDATION, includedWithPAYG = true, value = User.RESOURCE_KEY)
public abstract class User extends AbstractUUIDEntity implements NamedDocument {

	public static final String RESOURCE_KEY = "users";

	public static final String DETAILS_VIEW = "details";
	public static final String EMAIL_VIEW = "email";
	public static final String PHONE_VIEW = "telephone";
	public static final String AVATAR_VIEW = "avatar";
	
	private static final long serialVersionUID = 2210375165051752363L;

	@ObjectField(searchable = true, type = FieldType.TEXT, unique = true, view = DETAILS_VIEW)
	@Validator(type = ValidationType.REQUIRED)
	String username;

	@ObjectField(searchable = true, type = FieldType.TEXT, nameField = true, view = DETAILS_VIEW)
	@Validator(type = ValidationType.REQUIRED)
	String name;

	@ObjectField(type = FieldType.BOOL, hidden = true, defaultValue = "true", view = DETAILS_VIEW)
	Boolean enabled = Boolean.TRUE;
	
	@ObjectField(nameField = false, type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = EMAIL_VIEW)
	@Validator(type = ValidationType.EMAIL)
	@Validator(type = ValidationType.EMAIL)
	String email;

	@ObjectField(nameField = false, type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = PHONE_VIEW)
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN, bundle=User.RESOURCE_KEY)
	String mobilePhone;
	
	@ObjectField(type = FieldType.IMAGE, view = AVATAR_VIEW, weight = 9999)
	@Validators({
		@Validator(type = ValidationType.CLASSES, value = "p-3 bg-light"),
		@Validator(type = ValidationType.IMAGE_HEIGHT, value = "512"),
		@Validator(type = ValidationType.IMAGE_WIDTH, value = "512"),
			
	})
	String avatar;
	
	@ObjectField(type = FieldType.TIMESTAMP, readOnly = true, view = DETAILS_VIEW)
	@ExcludeView(values =  { FieldView.CREATE })
	Date lastLogin;
	
	@ObjectField(type = FieldType.TEXT, readOnly = true, view = DETAILS_VIEW, renderer = FieldRenderer.OPTIONAL)
	Collection<String> aliases;

	@ObjectField(type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = EMAIL_VIEW)
	@Validator(type = ValidationType.EMAIL)
	Collection<String> otherEmail = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = PHONE_VIEW)
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN, bundle=User.RESOURCE_KEY)
	Collection<String> otherTelephone = new ArrayList<>();
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public Collection<String> getAliases() {
		return aliases;
	}

	public void setAliases(Collection<String> aliases) {
		this.aliases = aliases;
	}

	public Collection<String> getOtherEmail() {
		return otherEmail;
	}

	public void setOtherEmail(Collection<String> otherEmail) {
		this.otherEmail = otherEmail;
	}

	public Collection<String> getOtherTelephone() {
		return otherTelephone;
	}

	public void setOtherTelephone(Collection<String> otherTelephone) {
		this.otherTelephone = otherTelephone;
	}

	public String getDisplayName() {
		return isBlank(name) ? getUsername() : name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public boolean isEnabled() {
		return enabled==null || enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getPrincipalDescription() {
		return getDisplayName();
	}
	
	public ITokenResolver getAdditionalData() {
		return new StaticResolver();
	}
	
}
