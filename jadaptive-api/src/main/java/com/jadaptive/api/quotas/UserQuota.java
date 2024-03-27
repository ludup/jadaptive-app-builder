package com.jadaptive.api.quotas;

import java.util.Collection;

import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.AssignableDocument;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.user.User;

@ObjectDefinition(resourceKey = UserQuota.RESOURCE_KEY, system = true)
@ObjectViewDefinition(value = UserQuota.USERS_VIEW, weight = 99998)
@ObjectViewDefinition(value = UserQuota.ROLES_VIEW, weight = 99999)
@GenerateEventTemplates
public class UserQuota extends QuotaThreshold implements AssignableDocument {

	private static final long serialVersionUID = 6344677508192690706L;
	public static final String RESOURCE_KEY = "userQuotas";
	public static final String USERS_VIEW = "usersQuotaView";
	public static final String ROLES_VIEW = "rolesQuotaView";
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = User.RESOURCE_KEY)
	@ObjectView(USERS_VIEW)
	Collection<User> users;

	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = Role.RESOURCE_KEY)
	@ObjectView(ROLES_VIEW)
	Collection<Role> roles;

	public Collection<User> getUsers() {
		return users;
	}

	public void setUsers(Collection<User> users) {
		this.users = users;
	}

	public Collection<Role> getRoles() {
		return roles;
	}

	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
	}
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
