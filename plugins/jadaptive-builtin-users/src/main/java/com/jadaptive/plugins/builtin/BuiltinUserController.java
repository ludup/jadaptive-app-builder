package com.jadaptive.plugins.builtin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.pf4j.Extension;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.user.User;

@Controller
@Extension
public class BuiltinUserController extends AuthenticatedController implements PluginController {

	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private PluginManager pluginManager;
	
	@RequestMapping(value = "/app/api/registration/user", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<User> registerUser(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required=false) String username,
			@RequestParam(required=false) String password,
			@RequestParam(required=false) String name,
			@RequestParam String email,
			@RequestParam(required=false) boolean sendNotifications,
			@RequestParam(required=false) boolean forceChange) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();
		
		try {
			return new ResourceStatus<User>(applicationService.getBean(BuiltinUserDatabase.class).createUser(
					StringUtils.defaultString(username, email), 
					StringUtils.defaultString(name),
					email, 
					password.toCharArray(),
					true));
		} catch (Throwable e) {
			return new ResourceStatus<User>(false, e.getMessage());
		} finally {
			clearUserContext();
		}
	}
}
