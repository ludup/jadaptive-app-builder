package com.jadaptive.app.scheduler;

import javax.lang.model.UnknownEntityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.UriRedirect;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SchedulerController extends AuthenticatedController {
	
	@Autowired
	private SchedulerService schedulerService; 
	
	@RequestMapping(value="/app/api/scheduled-tasks/cancel/{uuid}", method = { RequestMethod.GET })
	@ResponseBody
	public void cancelScheduledTask(HttpServletRequest request, 
			@PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
	
		
		setupUserContext(request);
		
		try {
			schedulerService.cancelTask(uuid, true);
		} catch(Throwable e) {
			Feedback.error(e.getMessage());
		}
		
		throw new UriRedirect("/app/ui/search/schedulerTask");
		 
		
	}
	
	@RequestMapping(value="/app/api/scheduled-tasks/run-now/{uuid}", method = { RequestMethod.GET })
	@ResponseBody
	public void runScheduledTaskNow(HttpServletRequest request, 
			@PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
	
		
		setupUserContext(request);
		
		try {
			schedulerService.runScheduledTaskNow(uuid);
		} catch(Throwable e) {
			Feedback.error(e.getMessage());
		}
		
		throw new UriRedirect("/app/ui/search/schedulerTask");
		 
		
	}
}
