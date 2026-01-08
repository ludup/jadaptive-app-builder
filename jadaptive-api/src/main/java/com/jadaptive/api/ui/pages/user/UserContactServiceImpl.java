package com.jadaptive.api.ui.pages.user;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
public class UserContactServiceImpl extends AuthenticatedService implements UserContactService, AbstractUUIDObjectService<UserContactPreferences> {

	@Autowired
	private UserService userService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public UserContactPreferences getObjectByUUID(String uuid) {
		throw new UnsupportedOperationException();
	}

	public UUIDDocument createNew(ObjectTemplate template) { 
		/**
		 * Get the object as the cached version seems to be missing updates
		 */
		return new UserContactPreferences(userService.getObjectByUUID(getCurrentUser().getUuid()));
	}
	
	@Override
	public String saveOrUpdate(UserContactPreferences prefs) {
		
		User user = getCurrentUser();
		
		user.setEmail(prefs.getEmail());
		user.setOtherEmail(prefs.getOtherEmail());
		user.setMobilePhone(prefs.getMobilePhone());
		user.setOtherTelephone(prefs.getOtherTelephone());
		
		permissionService.asSystem(()->{
			userService.updateUser(user);
			return null;
		});
		
		return user.getUuid();
	}

	@Override
	public void deleteObject(UserContactPreferences object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteObjectByUUID(String uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<UserContactPreferences> allObjects(SearchField... search) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<? extends UUIDDocument> searchTable(int start, int length, SortOrder order, String sortField,
			SearchField... fields) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long countTable(SearchField... fields) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<UserContactPreferences> collection(SearchField... fields) {
		throw new UnsupportedOperationException();
	}

}
