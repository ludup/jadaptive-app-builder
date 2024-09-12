package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;
import java.util.Objects;

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

import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.UriRedirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
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

			WizardFlow wizard = wizardService.getWizard(resourceKey);
			if(Objects.isNull(wizard)) {
				throw new FileNotFoundException();
			}
			WizardState state = wizard.getState(request);
			state.start();
			throw new UriRedirect(state.getRedirectURI());
	}
	
	@RequestMapping(value = "/app/api/wizard/next/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> next(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			Wizard.setCurrentState(state);
			try {
				while(!state.isFinished()) {
					WizardSection section = state.moveNext();
					if(!section.isHidden()) {
						state.incrementStep();
						throw new UriRedirect(state.getRedirectURI());
					}
				}
				
				throw new PageRedirect(state.getCompletePage());
			} finally {
				Wizard.clearCurrentState();
			}
			
	}
	
	@RequestMapping(value = "/app/api/wizard/back/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> back(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

			WizardState state = wizardService.getWizard(resourceKey).getState(request);
			Wizard.setCurrentState(state);
			try {
			while(!state.isStartPage()) {
				WizardSection section = state.moveBack();
				if(!section.isHidden()) {
					break;
				}
			}
			
			state.decrementStep();
			throw new UriRedirect(state.getRedirectURI());
			} finally {
				Wizard.clearCurrentState();
			}
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
			
		} catch(Redirect redir) {
			return new RedirectStatus(redir.getUri());
		} catch(Throwable e) {
			log.error("Failed to finish setup", e);
			return new RequestStatusImpl(false, e.getMessage());
		} finally {
			clearUserContext();
		}
	}
	
	@RequestMapping(value = "/app/api/wizard/cancel/{resourceKey}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	public void cancelWizrd(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String resourceKey) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, FileNotFoundException {

		setupSystemContext();
		
		WizardFlow wizard = wizardService.getWizard(resourceKey);
		
		try {
			
			wizardService.clearState(resourceKey, request);
		} catch(Throwable e) {
			log.error("Failed to cancel wizard {}", resourceKey, e);
		} finally {
			clearUserContext();
		}
		
		if(Objects.nonNull(wizard)) {
			throw wizard.getCancelRedirect();
		}
		throw new UriRedirect("/app/ui");
	}
}
