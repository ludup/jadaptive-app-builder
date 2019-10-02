package com.jadaptive.user;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository; 
	
	@Override
	public User createUser(String username, char[] password, String name) {
		
		/**
		 * TODO enforce permission
		 */
		
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
		
		/**
		 * TODO enforce permission
		 */
		userRepository.setPassword(user, newPassword);
		
	}

}
