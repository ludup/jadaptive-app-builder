package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import javax.lang.model.UnknownEntityException;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.ErrorPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.MessagePage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageExtension;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.editor.ReplaceTextContentEdit;

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
	private ClassLoaderService classLoader; 
	
	@Autowired
	private I18nService i18n;
	
	@Autowired
	private SystemOnlyObjectDatabase<ReplaceTextContentEdit> replacementDatabase;
	
	@Autowired
	private ServerLifetimeCacheIdentifier cacheId;
	
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
		if(checkReentrance(e))
			return;
		try {
			request.getRequestDispatcher(MessagePage.generatePageNotFoundURI(Request.get().getHeader(HttpHeaders.REFERER))).forward(request, response);
		}
		finally {
			throwableReentranceProtection.remove();
		}
	}
	
	private final static ThreadLocal<Boolean> throwableReentranceProtection = new ThreadLocal<>();
	
	@ExceptionHandler(Throwable.class)
	public void Throwable(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if(checkReentrance(e))
			return;
		try {
			log.error("Captured error", e);
			request.getRequestDispatcher(ErrorPage.generateErrorURI(e, request.getHeader(HttpHeaders.REFERER))).forward(request, response);
		}
		finally {
			throwableReentranceProtection.remove();
		}
	}

	boolean checkReentrance(Throwable e) {
		/* The new startup progress stuff is causing very strange things when there are 
		 * certain startup errors. CPU will go 100% with multiple overflowing stacks. Protecting
		 * against reentrance here seems to do the trick.
		 */
		if(Boolean.TRUE.equals(throwableReentranceProtection.get()))
			return true;
		throwableReentranceProtection.set(true);
		return false;
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
		
		if(page.isBackStop()) {
			log.info("REMOVEME: Setting back stop to {}", request.getRequestURI() + StringUtils.defaultIfBlank(request.getQueryString(), ""));
			request.getSession().setAttribute(Session.BACK_URL, request.getRequestURI() + StringUtils.defaultIfBlank(request.getQueryString(), ""));
		}
	
		page.doGet(resourceUri, request, response);
	}
	
	@RequestMapping(value="/robots.txt", method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public String doRobots(HttpServletRequest request, HttpServletResponse response) {

		StringBuilder b = new StringBuilder();
		b.append("User-agent: *\n");
		b.append("Disallow: /\n");
		return b.toString();
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

		if(checkCache(request)) {
			response.setStatus(HttpStatus.NOT_MODIFIED.value());
			return;
		}
		
		try {
			Page page = pageCache.resolvePage(pageCache.resolvePageClass(name.replace(".css", "")));
			URL url = page.getResourceClass().getResource(page.getCssResource());
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
		
		if(checkCache(request)) {
			response.setStatus(HttpStatus.NOT_MODIFIED.value());
			return;
		}
		
		try {
			try {
				Page page = pageCache.resolvePage(pageCache.resolvePageClass(name.replace(".js", "")));
				URL url = page.getResourceClass().getResource(page.getJsResource());
				response.setContentType("application/javascript");
				response.setStatus(HttpStatus.OK.value());
				
				try(InputStream in = ReaderInputStream.builder().setCharset("UTF-8")
						.setReader(new I18NConvertingReader(new InputStreamReader(url.openStream()), i18n, name)).get()) {
					IOUtils.copy(in, response.getOutputStream());
				}
			} catch(FileNotFoundException e) {
				try {
					PageExtension page = pageCache.resolveExtension(name.replace(".js", ""));
					URL url = page.getResourceClass().getResource(page.getJsResource());
					response.setContentType("application/javascript");
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

		if(checkCache(request)) {
			response.setStatus(HttpStatus.NOT_MODIFIED.value());
			return;
		}
		
		try {
			String name = request.getRequestURI().substring(12);
			URL url = classLoader.getResource(name);
			if(Objects.isNull(url)) {
				log.warn("Not found {}", request.getRequestURI());
				response.setStatus(404);
				return;
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

		if(checkCache(request)) {
			response.setStatus(HttpStatus.NOT_MODIFIED.value());
			return;
		}
		
		String name = request.getRequestURI().substring(11);
		URL url = classLoader.getResource(name);
		if(Objects.isNull(url)) {
			log.warn("Not found {}", request.getRequestURI());
			response.setStatus(404);
			return;
		}
		response.setContentType("text/css;charset=UTF-8");
		response.setStatus(HttpStatus.OK.value());
		try(InputStream in = url.openStream()) {
			IOUtils.copy(in, response.getOutputStream());
		}

	}
	
	@RequestMapping(value="/app/api/i18n/edit", method = RequestMethod.POST, produces = { "application/json"})
	@ResponseBody
	public RequestStatus changeText(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String bundle, @RequestParam String key, @RequestParam String replacementValue) throws RepositoryException, UnknownEntityException, ObjectException, IOException {

		try {
			ReplaceTextContentEdit edit;
			
			try {
				edit = replacementDatabase.get(ReplaceTextContentEdit.class, 
						SearchField.eq("bundle", bundle),
						SearchField.eq("key", key));
			} catch(ObjectNotFoundException e) {
				edit = new ReplaceTextContentEdit();
			}
			
			edit.setBundle(bundle);
			edit.setKey(key);
			edit.setReplacementText(replacementValue);
			
			replacementDatabase.saveOrUpdate(edit);
			
			return new RequestStatusImpl(true);
		} catch(Throwable t) {
			return new RequestStatusImpl(false);
		}
		
	}
	
	private boolean checkCache(HttpServletRequest request) {
		
		if(Boolean.getBoolean("jadaptive.development")) {
			return false;
		}
		// 1. Get the current, server-side ETag.
        String currentEtag = cacheId.getEtag();

        // 2. Manually get the browser's ETag from the request header.
        String ifNoneMatchHeader = request.getHeader("If-None-Match");

        // 3. Compare the ETags.
        if (ifNoneMatchHeader != null && ifNoneMatchHeader.equals(currentEtag)) {
            // The browser's version is up-to-date. Return 304 Not Modified.
            return true;
        }
        
        return false;
	}
}
