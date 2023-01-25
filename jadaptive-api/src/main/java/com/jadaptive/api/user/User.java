package com.jadaptive.api.user;

import java.util.Date;

import com.jadaptive.api.db.Transactional;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.utils.Utils;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION, defaultColumn = "username")
@ObjectServiceBean(bean = UserService.class)
@ObjectViews({ @ObjectViewDefinition(bundle = "users", value = "contact", weight=100),
	@ObjectViewDefinition(bundle = "users", value = User.DETAILS_VIEW, weight=0)})
@TableView(defaultColumns = { "username", "name", "lastLogin" },
	requiresUpdate = true, sortField = "username", actions = {
		@TableAction(bundle = "default", icon = "fa-key", resourceKey = "setPassword", target = Target.ROW, url = "/app/ui/set-password/{uuid}", writeAction = true) })
@Transactional
public abstract class User extends UUIDEntity implements NamedDocument {

	public static final String RESOURCE_KEY = "users";

	public static final String DETAILS_VIEW = "details";
	
	private static final long serialVersionUID = 2210375165051752363L;

	@ObjectField(searchable = true, type = FieldType.TEXT, unique = true, nameField = true)
	@ObjectView(DETAILS_VIEW)
	@Validator(type = ValidationType.REQUIRED)
	String username;

	@ObjectField(searchable = true, type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	@ObjectView(DETAILS_VIEW)
	String name;

	@ObjectField(nameField = false, type = FieldType.TEXT, automaticEncryption = true)
	@Validator(type = ValidationType.REGEX, value = Utils.EMAIL_PATTERN)
	@ObjectView("contact")
	String email;

	@ObjectField(nameField = false, type = FieldType.TEXT, automaticEncryption = true)
	@ObjectView("contact")
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN)
	String mobilePhone;
	
	@ObjectField(type = FieldType.TIMESTAMP, readOnly = true)
	@ExcludeView(values =  { FieldView.CREATE })
	@ObjectView(DETAILS_VIEW)
	Date lastLogin;

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
	
}
