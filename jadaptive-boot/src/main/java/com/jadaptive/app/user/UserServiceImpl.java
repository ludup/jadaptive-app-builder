package com.jadaptive.app.user;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.user.BuiltinUserDatabase;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.CompoundIterable;

@Service
public class UserServiceImpl implements UserService, TenantAware {

	@Autowired
	private BuiltinUserDatabase userRepository; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public User getUserByUUID(String uuid) {
		User user = userRepository.getUserByUUID(uuid);
		
		if(Objects.isNull(user)) {
			throw new EntityNotFoundException(String.format("User with id %s not found", uuid));
		}
		return user;
	}

	@Override
	public boolean verifyPassword(User user, char[] password) {
		try {
			return userRepository.verifyPassword(user, password);
		} catch(EntityNotFoundException e) {
			return false;
		}
	}


	@Override
	public User getUser(String username) {
		User user = userRepository.getUser(username);
		if(Objects.isNull(user)) {
			throw new EntityNotFoundException(String.format("%s not found", username));
		}
		return user;
	}

	@Override
	public void setPassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(SET_PASSWORD_PERMISSION);
		userRepository.setPassword(user, newPassword, passwordChangeRequired);
		
	}
	
	@Override
	public void changePassword(User user, char[] oldPassword, char[] newPassword) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		verifyPassword(user, oldPassword);
		userRepository.setPassword(user, newPassword, false);
		
	}
	
	@Override
	public void changePassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		userRepository.setPassword(user, newPassword, passwordChangeRequired);
		
	}

	@Override
	public void initializeSystem() {

		permissionService.registerStandardPermissions(USER_RESOURCE_KEY);
		permissionService.registerCustomPermission(CHANGE_PASSWORD_PERMISSION);
		permissionService.registerCustomPermission(SET_PASSWORD_PERMISSION);
		
	}
	
	@Override
	public void initializeTenant(Tenant tenant) {

	}

	@Override
	public Iterable<User> iterateUsers() {
		
		CompoundIterable<User> iterator = new CompoundIterable<>();
		iterator.add(userRepository.iterateUsers());
		return iterator;
	}

}
