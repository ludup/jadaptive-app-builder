package com.jadaptive.app.json.upload;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
		resp.sendError(HttpStatus.METHOD_NOT_ALLOWED.value());
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
		
		if(!JakartaServletFileUpload.isMultipartContent(req)) {
			throw new IOException("Upload must be a multipart request!");
		}

		Map<String,String[]> parameters = new HashMap<>();
		
		try {
			// Create a new file upload handler
			JakartaServletFileUpload<?,?> upload = new JakartaServletFileUpload<>();

			// Parse the request
			FileItemInputIterator iter = upload.getItemIterator(req);

			while(iter.hasNext()) {
			    FileItemInput item = iter.next();

			    if (item.isFormField()) {
			    	String name = item.getFieldName();
			        String value = IOUtils.toString(item.getInputStream(), "UTF-8");
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
