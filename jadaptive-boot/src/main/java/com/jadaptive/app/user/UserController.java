package com.jadaptive.app.user;

import javax.lang.model.UnknownEntityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController extends AuthenticatedController implements PluginController {

	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/app/api/accounts/enable/{uuid}", method = { RequestMethod.GET })
	public void enableUser(HttpServletRequest request, 
			@PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
	
		setupUserContext(request);
		
		try {
			User user = userService.getObjectByUUID(uuid);
			userService.enableUser(user);
			Feedback.success(User.RESOURCE_KEY, "enableUser.success", user.getUsername());
		} catch(Throwable e) {
			Feedback.error(e.getMessage());
		}
		
		throw new UriRedirect("/app/ui/search/" + User.RESOURCE_KEY);
		 
		
	}
	
	@RequestMapping(value="/app/api/accounts/disable/{uuid}", method = { RequestMethod.GET })
	public void disableUser(HttpServletRequest request, 
			@PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
	
		setupUserContext(request);
		
		try {
			User user = userService.getObjectByUUID(uuid);
			userService.disableUser(user);
			Feedback.success(User.RESOURCE_KEY, "disableUser.success", user.getUsername());
		} catch(Throwable e) {
			Feedback.error(e.getMessage());
		}
		
		throw new UriRedirect("/app/ui/search/" + User.RESOURCE_KEY);
		 
		
	}

}
