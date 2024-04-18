package com.jadaptive.api.ui.pages;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.Feedback;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ProcessingController {

	static Logger log = LoggerFactory.getLogger(ProcessingController.class);
	
	@RequestMapping(value = { "/app/api/process/{uuid}" }, produces = { "application/json" }, method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public RequestStatus process(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String uuid) throws IOException, AccessDeniedException, UnauthorizedException {
		
		ProcessingJob job = (ProcessingJob) Request.get().getSession().getAttribute(uuid);
		
		try {
			job.getTask().run();
			Feedback.success(job.getBundle(), job.getFeedbackSuccess());
			return new RequestStatusImpl(true, job.getReturnURL());
		} catch (Throwable e) {
			log.error("Processing jop failed", e);
			Feedback.error(job.getBundle(), job.getFeedbackError(), e.getMessage());
			return new RequestStatusImpl(true, job.getReturnURL());
		}
	}

}
