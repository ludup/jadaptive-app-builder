package com.jadaptive.api.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.UriRedirect;

@Controller
@Extension
public class WizardController implements PluginController {

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
	public ResourceStatus<Boolean> finishSetup(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			
			state.finish();
			throw new UriRedirect("");
	}
}
