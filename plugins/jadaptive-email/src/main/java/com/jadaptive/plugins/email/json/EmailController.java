package com.jadaptive.plugins.email.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.plugins.email.EmailVerificationService;

@Controller
@Extension
public class EmailController extends AuthenticatedController implements PluginController {

	static Logger log = LoggerFactory.getLogger(EmailController.class);
	
	@Autowired
	private EmailVerificationService verificationService; 
	
	@RequestMapping(value = "/app/api/registration/verifyEmail", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> verifyEmail(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam String email) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();
		
		try {
			return new ResourceStatus<Boolean>(true, verificationService.verifyEmail(email), "");
		} catch(Throwable t) {
			log.error("api/registration/verifyEmail", t);
			return new ResourceStatus<Boolean>(false, t.getMessage());
		} finally {
			clearUserContext();
		}
	}
	
	@RequestMapping(value = "/app/api/registration/assertCode", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> verifyCode(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam String email,
			@RequestParam String code) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();
		
		try {
			return new ResourceStatus<Boolean>(new Boolean(verificationService.assertCode(email, code)));
		} catch(Throwable t) {
			log.error("api/registration/assertCode", t);
			return new ResourceStatus<Boolean>(false, t.getMessage());
		} finally {
			clearUserContext();
		}

	}
}
