package com.jadaptive.api.ui.pages.user;

import java.util.ArrayList;
import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldOptions;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = UserContactPreferences.RESOURCE_KEY, bundle = User.RESOURCE_KEY, type = ObjectType.OBJECT, requiresPermission = false)
@ObjectServiceBean(bean = UserContactService.class)
@PageMenu(bundle = User.RESOURCE_KEY, i18n = "contactPreferences.name", icon = "fa-user-pen", parent = ApplicationMenuService.USER_MENU, path = "/app/ui/contact-preferences")
public class UserContactPreferences extends AbstractUUIDEntity {

	private static final long serialVersionUID = -3166522056081651733L;

	public static final String RESOURCE_KEY = "userContactPreferences";
	
	public UserContactPreferences() { } 
	
	public UserContactPreferences(User user) {
		this.email = user.getEmail();
		this.otherEmail = new ArrayList<>(user.getOtherEmail());
		this.mobilePhone = user.getMobilePhone();
		this.otherTelephone = new ArrayList<>(user.getOtherTelephone());
	}
	
	@ObjectField(nameField = false, type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = User.EMAIL_VIEW)
	@Validator(type = ValidationType.EMAIL)
	private String email;

	@ObjectField(nameField = false, type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = User.PHONE_VIEW)
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN, bundle=User.RESOURCE_KEY)
	private String mobilePhone;
	
	@ObjectField(type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = User.EMAIL_VIEW)
	@Validator(type = ValidationType.EMAIL)
	private Collection<String> otherEmail = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT, options = FieldOptions.AUTOMATIC_ENCRYPTION, view = User.PHONE_VIEW)
	@Validator(type = ValidationType.REGEX, value = Utils.PHONE_PATTERN, bundle=User.RESOURCE_KEY)
	private Collection<String> otherTelephone = new ArrayList<>();

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
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
