package com.jadaptive.app.json.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionStickyInputStream;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.upload.UploadHandler;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.json.ResponseHelper;
import com.jadaptive.utils.FileUtils;

@WebServlet(name="uploadServlet", description="Servlet for handing file uploads", urlPatterns = { "/upload/*" })
public class UploadServlet extends HttpServlet {

	static Logger log = LoggerFactory.getLogger(UploadServlet.class);
	
	@Autowired
	SessionUtils sessionUtils;
	
	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	UserService userService; 
	
	@Autowired
	PluginManager pluginManager;
	
	Map<String,UploadHandler> uploadHandlers = new HashMap<>();
	
	@Override
	protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws ServletException, IOException {
		
		super.service(httpRequest, httpResponse);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String uri = FileUtils.checkStartsWithNoSlash(req.getPathInfo());
		String handlerName = FileUtils.firstPathElement(uri);
		uri = FileUtils.stripParentPath(handlerName, uri);

		UploadHandler handler = getUploadHandler(handlerName);
		
		Session session = sessionUtils.getActiveSession(req);
		
		if(handler.isSessionRequired() && Objects.isNull(session)) {
			ResponseHelper.send403Forbidden(req, resp);
			return;
		}
		
		if(Objects.nonNull(session)) {
			permissionService.setupUserContext(userService.getUser(session.getUsername()));
		}
		
		try {
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();

			// Parse the request
			FileItemIterator iter = upload.getItemIterator(req);
			Map<String,String> parameters = new HashMap<>();
			while (iter.hasNext()) {
			    FileItemStream item = iter.next();

			    if (item.isFormField()) {
			    	String name = item.getFieldName();
			        String value = IOUtils.toString(item.openStream(), "UTF-8");
			        parameters.put(name, value);
			    } else {
			     
				    if(Objects.isNull(handler)) {
				    	log.warn("Missing upload handler for {}", handlerName);
				    	continue;
				    }
				    
				    InputStream stream = new SessionStickyInputStream(
				    		item.openStream(), 
				    		session) {
				    	protected void touchSession() {
							if((System.currentTimeMillis() - lastTouch) >  30000) {
								try {
									sessionUtils.touchSession(session);
								} catch (SessionTimeoutException e) {
								}
								lastTouch = System.currentTimeMillis();
							}
						}
				    };
		
				    try {
				    	handler.handleUpload(handlerName, uri, parameters, item.getName(), stream);    
				    } finally {
				    	stream.close();
				    }
			    }
			    
			}
		
			resp.setStatus(HttpStatus.OK.value());
			
		} catch (FileUploadException | SessionTimeoutException | UnauthorizedException e) {
			log.error("Upload failure", e);
			resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error. Please see application log for more information.");
		} finally {
			if(Objects.nonNull(session)) {
				permissionService.clearUserContext();
			}
		}
		
		
	}

	private UploadHandler getUploadHandler(String handlerName) {
		
		UploadHandler handler = uploadHandlers.get(handlerName);
		
		if(Objects.isNull(handler)) {
			for(UploadHandler h : pluginManager.getExtensions(UploadHandler.class)) {
				if(h.getURIName().equalsIgnoreCase(handlerName)) {
					handler = h;
					uploadHandlers.put(handlerName, h);
					break;
				}
			}
		}
		
		return handler;
	}

	private static final long serialVersionUID = -6914120139469535232L;
}
