package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import javax.lang.model.UnknownEntityException;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.ErrorPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.MessagePage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageExtension;
import com.jadaptive.api.ui.Redirect;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserInterfaceController extends AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(UserInterfaceController.class);

	public static final String BUNDLE = "userInterface";
	
	@Autowired
	private PageCache pageCache; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private ClassLoaderService classLoader; 
	
	@Autowired
	private I18nService i18n;
	
	@ExceptionHandler(UnauthorizedException.class)
	public void handleException(HttpServletRequest request,
			HttpServletResponse response,
			Throwable e) throws IOException {
		
		Feedback.error("userInterface", "unauthorized.text");
		if(request.getRequestURI().startsWith("/app/ui/")) {
			response.sendRedirect("/app/ui/login");
		} else {
			response.sendError(HttpStatus.UNAUTHORIZED.value());
		}
	}

	@ExceptionHandler(AccessDeniedException.class)
	public void handleException(HttpServletRequest request, 
			HttpServletResponse response,
			AccessDeniedException e) throws IOException {
	
		Feedback.error("userInterface", "unauthorized.text");
		if(request.getRequestURI().startsWith("/app/ui/")) {
			response.sendRedirect("/app/ui/login");
		} else {
			response.sendError(HttpStatus.FORBIDDEN.value());
		}
	}
	
	@ExceptionHandler(FileNotFoundException.class)
	public void FileNotFoundException(FileNotFoundException e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		request.getRequestDispatcher(MessagePage.generatePageNotFoundURI(Request.get().getHeader(HttpHeaders.REFERER))).forward(request, response);
	}
	
	@ExceptionHandler(Throwable.class)
	public void Throwable(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		log.error("Captured error", e);
		request.getRequestDispatcher(ErrorPage.generateErrorURI(e, request.getHeader(HttpHeaders.REFERER))).forward(request, response);
	}
	
	@ExceptionHandler(Redirect.class)
	public void Redirect(Redirect e, HttpServletResponse response) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("Redirecting to {}", e.getUri());
		}
		response.sendRedirect(e.getUri());	
	}

	@RequestMapping(value="/app/ui/**", method = RequestMethod.GET)
	public void doPageGet(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		String uri = request.getRequestURI();
		String resourceUri = uri.length() >= 8 ? uri.substring(8) : "";
		
		Page page = pageCache.resolvePage(resourceUri, true);
		page.doGet(resourceUri, request, response);
	}
	
	@RequestMapping(value="/app/ui/**", method = RequestMethod.POST)
	public void doPagePost(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		String uri = request.getRequestURI();
		String resourceUri = uri.length() >= 8 ? uri.substring(8) : "";
		
		Page page = pageCache.resolvePage(resourceUri, true);
		page.doPost(resourceUri, request, response);

	}
	
	@RequestMapping(value="/app/css/{name}", method = RequestMethod.GET, produces = { "text/css" })
	public void doStylesheet(HttpServletRequest request, HttpServletResponse response, @PathVariable("name") String name) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		sessionUtils.setCachable(response, 600);
		
		try {
			Page page = pageCache.resolvePage(pageCache.resolvePageClass(name.replace(".css", "")));
			URL url = page.getClass().getResource(page.getCssResource());
			response.setContentType("text/css");
			response.setStatus(HttpStatus.OK.value());
			try(InputStream in = url.openStream()) {
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch(FileNotFoundException e) {
			try {
				PageExtension page = pageCache.resolveExtension(name.replace(".css", ""));
				URL url = page.getClass().getResource(page.getCssResource());
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
	
	
	
	@RequestMapping(value="/app/js/{name}", method = RequestMethod.GET, produces = { "text/javascript" })
	public void doScript(HttpServletRequest request, HttpServletResponse response, @PathVariable("name") String name) throws RepositoryException, UnknownEntityException, ObjectException, IOException {
		
		sessionUtils.setCachable(response, 600);
		
		try {
			try {
				Page page = pageCache.resolvePage(pageCache.resolvePageClass(name.replace(".js", "")));
				URL url = page.getClass().getResource(page.getJsResource());
				response.setContentType("text/javascript");
				response.setStatus(HttpStatus.OK.value());
				
				try(InputStream in = ReaderInputStream.builder().setCharset("UTF-8")
						.setReader(new I18NConvertingReader(new InputStreamReader(url.openStream()), i18n, name)).get()) {
					IOUtils.copy(in, response.getOutputStream());
				}
			} catch(FileNotFoundException e) {
				try {
					PageExtension page = pageCache.resolveExtension(name.replace(".js", ""));
					URL url = page.getClass().getResource(page.getJsResource());
					response.setContentType("text/javascript");
					response.setStatus(HttpStatus.OK.value());
					try(InputStream in = ReaderInputStream.builder().setCharset("UTF-8")
							.setReader(new I18NConvertingReader(new InputStreamReader(url.openStream()), i18n, name)).get()) {
						IOUtils.copy(in, response.getOutputStream());
					}
				} catch(FileNotFoundException e2) {
					response.sendError(HttpStatus.NOT_FOUND.value());
				}
			}
		} catch(Throwable e) {
			log.error("Script content error", e);
			response.sendError(500);
		}
		
	}
	
	@RequestMapping(value="/app/script/**", method = RequestMethod.GET)
	public void doScript(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		sessionUtils.setCachable(response, 600);
		
		try {
			String name = request.getRequestURI().substring(12);
			URL url = classLoader.getResource(name);
			if(Objects.isNull(url)) {
				throw new FileNotFoundException();
			}
			response.setContentType("application/javascript");
			response.setStatus(HttpStatus.OK.value());
			try(InputStream in = ReaderInputStream.builder().setCharset("UTF-8")
					.setReader(new I18NConvertingReader(new InputStreamReader(url.openStream()), i18n, name)).get()) {
				IOUtils.copy(in, response.getOutputStream());
			}
			
		} catch(Throwable e) {
			log.error("Script content error", e);
			response.sendError(500);
		}

	}
	
	@RequestMapping(value="/app/style/**", method = RequestMethod.GET, produces = { "text/css"})
	public void doStyle(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		sessionUtils.setCachable(response, 600);
		
		String name = request.getRequestURI().substring(11);
		URL url = classLoader.getResource(name);
		if(Objects.isNull(url)) {
			throw new FileNotFoundException();
		}
		response.setContentType("text/css;charset=UTF-8");
		response.setStatus(HttpStatus.OK.value());
		try(InputStream in = url.openStream()) {
			IOUtils.copy(in, response.getOutputStream());
		}

	}
}
