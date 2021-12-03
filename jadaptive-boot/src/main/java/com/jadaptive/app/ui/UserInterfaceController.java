package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageExtension;
import com.jadaptive.api.ui.Redirect;

@Controller
public class UserInterfaceController extends AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(UserInterfaceController.class);
	
	@Autowired
	private PageCache pageCache; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@RequestMapping(value="/app/verify", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public RequestStatus verifySession(HttpServletRequest request, HttpServletResponse response)  {

		Session session;
		try {
			session = sessionUtils.getSession(request);
			return new RequestStatusImpl(session!=null);
		} catch (UnauthorizedException | SessionTimeoutException e) {
			return new RequestStatusImpl(false);
		}
		
	}
	
	@RequestMapping(value="/app/ui/**", method = RequestMethod.GET)
	public void doPageGet(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		String uri = request.getRequestURI();
		String resourceUri = uri.length() >= 8 ? uri.substring(8) : "";
		
		try {
			Page page = pageCache.resolvePage(resourceUri);
			page.doGet(resourceUri, request, response);
		} catch(Redirect e) {
			log.info("Redirecting to {}", e.getUri());
			response.sendRedirect(e.getUri());
		} catch(FileNotFoundException e) {
			log.info("Cannot find {}", resourceUri);
			response.sendError(HttpStatus.NOT_FOUND.value());
		}
	}
	
	@RequestMapping(value="/app/ui/**", method = RequestMethod.POST)
	public void doPagePost(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		String uri = request.getRequestURI();
		String resourceUri = uri.length() >= 8 ? uri.substring(8) : "";
		
		try {
			Page page = pageCache.resolvePage(resourceUri);
			page.doPost(resourceUri, request, response);
		} catch(Redirect e) {
			response.sendRedirect(e.getUri());
		} catch(FileNotFoundException e) {
			response.sendError(HttpStatus.NOT_FOUND.value());
		}
	}
	
	@RequestMapping(value="/app/css/{name}", method = RequestMethod.GET)
	public void doStylesheet(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		try {
			Page page = pageCache.resolvePage(name.replace(".css", ""));
			URL url = page.getClass().getResource(String.format("%s.css", page.getClass().getSimpleName()));
			response.setContentType("text/css");
			response.setStatus(HttpStatus.OK.value());
			try(InputStream in = url.openStream()) {
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch(FileNotFoundException e) {
			try {
				PageExtension page = pageCache.resolveExtension(name.replace(".css", ""));
				URL url = page.getClass().getResource(String.format("%s.css", page.getClass().getSimpleName()));
				response.setContentType("text/css");
				response.setStatus(HttpStatus.OK.value());
				try(InputStream in = url.openStream()) {
					IOUtils.copy(in, response.getOutputStream());
				}
			} catch(FileNotFoundException e2) {
				response.sendError(HttpStatus.NOT_FOUND.value());
			}
		}
	}
	
	@RequestMapping(value="/app/js/{name}", method = RequestMethod.GET)
	public void doScript(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		try {
			Class<?> page = pageCache.resolvePageClass(name.replace(".js", ""));
			URL url = page.getResource(String.format("%s.js", page.getSimpleName()));
			response.setContentType("application/javascript");
			response.setStatus(HttpStatus.OK.value());
			try(InputStream in = url.openStream()) {
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch(FileNotFoundException e) {
			try {
				PageExtension page = pageCache.resolveExtension(name.replace(".js", ""));
				URL url = page.getClass().getResource(String.format("%s.js", page.getClass().getSimpleName()));
				response.setContentType("text/javascript");
				response.setStatus(HttpStatus.OK.value());
				try(InputStream in = url.openStream()) {
					IOUtils.copy(in, response.getOutputStream());
				}
			} catch(FileNotFoundException e2) {
				response.sendError(HttpStatus.NOT_FOUND.value());
			}
		}
	}
}
