package com.jadaptive.api.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.jadaptive.api.db.Transactional;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION, defaultColumn = "username")
@ObjectServiceBean(bean = UserService.class)
@ObjectViewDefinition(bundle = "users", value = User.DETAILS_VIEW, weight=0)
@ObjectViewDefinition(bundle = "users", value = "email", weight=100)
@ObjectViewDefinition(bundle = "users", value = "telephone", weight=200)
@TableView(defaultColumns = { "username", "name", "lastLogin" }, requiresUpdate = true, sortField = "username")
@Transactional
@GenerateEventTemplates(User.RESOURCE_KEY)
public abstract class User extends AbstractUUIDEntity implements NamedDocument {

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
	@Validator(type = ValidationType.EMAIL)
	@ObjectView("emails")
	@Validator(type = ValidationType.EMAIL)
	String email;

	@ObjectField(nameField = false, type = FieldType.TEXT, automaticEncryption = true)
	@ObjectView("telephone")
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN, bundle=User.RESOURCE_KEY)
	String mobilePhone;
	
	@ObjectField(type = FieldType.TIMESTAMP, readOnly = true)
	@ExcludeView(values =  { FieldView.CREATE })
	@ObjectView(DETAILS_VIEW)
	Date lastLogin;
	
	@ObjectField(type = FieldType.TEXT, readOnly = true)
	@ObjectView(value = DETAILS_VIEW, renderer = FieldRenderer.OPTIONAL)
	Collection<String> aliases;

	@ObjectField(type = FieldType.TEXT, automaticEncryption = true)
	@Validator(type = ValidationType.EMAIL)
	@ObjectView("emails")
	Collection<String> otherEmail = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT, automaticEncryption = true)
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN, bundle=User.RESOURCE_KEY)
	@ObjectView("telephone")
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
	
}
