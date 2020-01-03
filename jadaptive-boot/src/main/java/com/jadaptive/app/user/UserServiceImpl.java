package com.jadaptive.app.user;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.user.DefaultUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserRepository;
import com.jadaptive.api.user.UserService;

@Service
public class UserServiceImpl implements UserService, TenantAware {

	public static final String CHANGE_PASSWORD_PERMISSION = "user.changePassword";
	public static final String SET_PASSWORD_PERMISSION = "user.setPassword";
	public static final String USER_RESOURCE_KEY = "user";
	
	@Autowired
	UserRepository userRepository; 
	
	@Autowired
	PermissionService permissionService; 
	
	@Override
	public User getUser(String uuid) {
		User user = userRepository.get(uuid);
		
		if(Objects.isNull(user)) {
			throw new EntityNotFoundException(String.format("User with id %s not found", uuid));
		}
		return user;
	}
	
	@Override
	public User createUser(String username, char[] password, String name) {
		
		permissionService.assertReadWrite(USER_RESOURCE_KEY);
		
		DefaultUser user = new DefaultUser();
		user.setUsername(username);
		user.setName(name);
		
		userRepository.createUser(user, password);
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
	public User findUsername(String username) {
		User user = userRepository.findUsername(username);
		if(Objects.isNull(user)) {
			throw new EntityNotFoundException(String.format("%s not found", username));
		}
		return user;
	}

	@Override
	public void setPassword(User user, char[] newPassword) {
		
		permissionService.assertPermission(SET_PASSWORD_PERMISSION);
		userRepository.setPassword(user, newPassword);
		
	}
	
	@Override
	public void changePassword(User user, char[] oldPassword, char[] newPassword) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		verifyPassword(user, oldPassword);
		userRepository.setPassword(user, newPassword);
		
	}
	
	@Override
	public void changePassword(User user, char[] newPassword) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		userRepository.setPassword(user, newPassword);
		
	}

	@Override
	public void initializeTenant(Tenant tenant) {
		
		permissionService.registerCustomPermission(CHANGE_PASSWORD_PERMISSION);
		permissionService.registerCustomPermission(SET_PASSWORD_PERMISSION);
	}

	@Override
	public Iterable<? extends User> iterateUsers() {
		return userRepository.list();
	}

}
