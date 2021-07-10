package com.jadaptive.api.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.UriRedirect;

@Controller
@Extension
public class WizardController extends AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(WizardController.class);
	
	@Autowired
	private WizardService wizardService; 
	
	@RequestMapping(value = "/app/api/wizard/start/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> startWizard(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			state.start();
			throw new UriRedirect(String.format("/app/ui/wizards/%s", state.getResourceKey()));
	}
	
	@RequestMapping(value = "/app/api/wizard/next/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> next(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			if(state.isFinished()) {
				throw new PageRedirect(state.getCompletePage());
			}
			state.moveNext();
			throw new UriRedirect(String.format("/app/ui/wizards/%s", state.getResourceKey()));
	}
	
	@RequestMapping(value = "/app/api/wizard/back/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> back(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			state.moveBack();
			throw new UriRedirect(String.format("/app/ui/wizards/%s", state.getResourceKey()));
	}
	
	@RequestMapping(value = "/app/api/wizard/finish/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatusImpl finishSetup(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

		setupSystemContext();
		
		try {
			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			
			state.finish();
			
			return new RequestStatusImpl(true);
			
		} catch(Throwable e) {
			log.error("Failed to finish setup", e);
			return new RequestStatusImpl(false, e.getMessage());
		} finally {
			clearUserContext();
		}
	}
}
