package com.jadaptive.api.user;

import java.util.Date;

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
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION, defaultColumn = "username")
@ObjectServiceBean(bean = UserService.class)
@ObjectViews({ @ObjectViewDefinition(bundle = "users", value = "contact") })
@TableView(defaultColumns = { "username", "name", "lastLogin" }, requiresUpdate = true, actions = {
		@TableAction(bundle = "default", icon = "fa-key", resourceKey = "setPassword", target = Target.ROW, url = "/app/ui/set-password/{uuid}") })
public abstract class User extends UUIDEntity implements NamedDocument {

	public static final String RESOURCE_KEY = "users";

	private static final long serialVersionUID = 2210375165051752363L;

	@ObjectField(searchable = true, type = FieldType.TEXT, unique = true)
	@Validator(type = ValidationType.REQUIRED)
	String username;

	@ObjectField(searchable = true, nameField = true, type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String name;

	@ObjectField(searchable = true, nameField = false, type = FieldType.TEXT, automaticEncryption = true)
	@ObjectView("contact")
	String email;

	@ObjectField(searchable = true, nameField = false, type = FieldType.TEXT, automaticEncryption = true)
	@ObjectView("contact")
	String mobilePhone;
	
	@ObjectField(type = FieldType.TIMESTAMP, readOnly = true)
	@ExcludeView(values =  { FieldView.CREATE })
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
