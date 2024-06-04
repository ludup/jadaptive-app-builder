package com.jadaptive.app.json.upload;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.ResponseHelper;
import com.jadaptive.api.upload.UploadHandler;
import com.jadaptive.api.upload.UploadIterator;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.FileUtils;
import com.jadaptive.utils.ParameterHelper;

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
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String uri = FileUtils.checkStartsWithNoSlash(req.getPathInfo());
		String handlerName = FileUtils.firstPathElement(uri);
		uri = FileUtils.stripParentPath(handlerName, uri);

		UploadHandler handler = getUploadHandler(handlerName);
		
		req.getRequestDispatcher(handler.getPageURL(uri)).forward(req, resp);
	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String uri = FileUtils.checkStartsWithNoSlash(req.getPathInfo());
		String handlerName = FileUtils.firstPathElement(uri);
		uri = FileUtils.stripParentPath(handlerName, uri);

		UploadHandler handler = getUploadHandler(handlerName);
		
		if(Objects.isNull(handler)) {
			ResponseHelper.send404NotFound(uri, req, resp);
			return;
		}
		Optional<Session> session = Session.getOr(req);
		
		if(handler.isSessionRequired() && Objects.isNull(session)) {
			ResponseHelper.send403Forbidden(req, resp);
			return;
		}
		
		if(session.isPresent()) {
			permissionService.setupUserContext(session.get().getUser());
		} 

		Map<String,String[]> parameters = new HashMap<>();
		
		try {
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();

			// Parse the request
			FileItemIterator iter = upload.getItemIterator(req);
			
			
			while (iter.hasNext()) {
			    FileItemStream item = iter.next();

			    if (item.isFormField()) {
			    	String name = item.getFieldName();
			        String value = IOUtils.toString(item.openStream(), "UTF-8");
			        ParameterHelper.setValue(parameters, name, value);
			    } else {
				    
				    if(StringUtils.isBlank(item.getName())) {
				    	continue;
				    }
				    
			    	try(var scope = SessionUtils.scopedIoWithoutSessionTimeout(req)) {
			    		handler.handleUpload(handlerName, uri, parameters, new UploadIterator(iter, item));  
				    }  
				   break;
			    }
			    
			}
		
			handler.sendSuccessfulResponse(resp, handlerName, uri, parameters);

		} catch (Throwable e) {
			log.error("Upload failure", e);
			Feedback.error(e.getMessage());
			handler.sendFailedResponse(resp, handlerName, uri, parameters, e);
		} finally {
			if(session.isPresent()) {
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
