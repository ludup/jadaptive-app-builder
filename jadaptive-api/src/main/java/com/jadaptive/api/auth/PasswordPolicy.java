package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.NamedAssignableUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@PageMenu(
    parent = ApplicationMenuService.SECURITY_MENU_UUID,
    weight = 20,
    uuid = "9cfe1b1e-7e48-4c2f-9f35-6b993fa94a3a",
    path = "/app/ui/search/passwordPolicies",
    icon = "fa-key",
    bundle = "userInterface",
    i18n = "passwordPolicies.names"
)
@ObjectDefinition(resourceKey = PasswordPolicy.RESOURCE_KEY, scope = ObjectScope.GLOBAL, defaultColumn = "name")
@TableView(defaultColumns = { "name", "minimumLength", "minimumLowercase", "minimumUppercase", "minimumNumeric", "minimumSpecial" })
@ObjectServiceBean(bean = PasswordPolicyService.class)
@GenerateEventTemplates(PasswordPolicy.RESOURCE_KEY)
public class PasswordPolicy extends NamedAssignableUUIDEntity {

    private static final long serialVersionUID = -8067540012863043319L;

    public static final String RESOURCE_KEY = "passwordPolicies";

    @ObjectField(type = FieldType.INTEGER, defaultValue = "12")
    @Validator(type = ValidationType.RANGE, value = "1-1024")
    @Validator(type = ValidationType.REQUIRED)
    private Integer minimumLength = 12;

    @ObjectField(type = FieldType.INTEGER, defaultValue = "1")
    @Validator(type = ValidationType.RANGE, value = "0-1024")
    private Integer minimumLowercase = 1;

    @ObjectField(type = FieldType.INTEGER, defaultValue = "1")
    @Validator(type = ValidationType.RANGE, value = "0-1024")
    private Integer minimumUppercase = 1;

    @ObjectField(type = FieldType.INTEGER, defaultValue = "1")
    @Validator(type = ValidationType.RANGE, value = "0-1024")
    private Integer minimumNumeric = 1;

    @ObjectField(type = FieldType.INTEGER, defaultValue = "1")
    @Validator(type = ValidationType.RANGE, value = "0-1024")
    private Integer minimumSpecial = 1;

    @ObjectField(type = FieldType.TEXT, defaultValue = "!@#$%^&*()-_=+[]{};:,.<>?/\\\\|")
    private String allowedSpecialCharacters = "!@#$%^&*()-_=+[]{};:,.<>?/\\\\|";

    @Override
    public String getResourceKey() {
        return RESOURCE_KEY;
    }

    public Integer getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(Integer minimumLength) {
        this.minimumLength = minimumLength;
    }

    public Integer getMinimumLowercase() {
        return minimumLowercase;
    }

    public void setMinimumLowercase(Integer minimumLowercase) {
        this.minimumLowercase = minimumLowercase;
    }

    public Integer getMinimumUppercase() {
        return minimumUppercase;
    }

    public void setMinimumUppercase(Integer minimumUppercase) {
        this.minimumUppercase = minimumUppercase;
    }

    public Integer getMinimumNumeric() {
        return minimumNumeric;
    }

    public void setMinimumNumeric(Integer minimumNumeric) {
        this.minimumNumeric = minimumNumeric;
    }

    public Integer getMinimumSpecial() {
        return minimumSpecial;
    }

    public void setMinimumSpecial(Integer minimumSpecial) {
        this.minimumSpecial = minimumSpecial;
    }

    public String getAllowedSpecialCharacters() {
        return allowedSpecialCharacters;
    }

    public void setAllowedSpecialCharacters(String allowedSpecialCharacters) {
        this.allowedSpecialCharacters = allowedSpecialCharacters;
    }
}
