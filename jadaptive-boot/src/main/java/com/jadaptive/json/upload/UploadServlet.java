package com.jadaptive.json.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.json.ResponseHelper;
import com.jadaptive.passwords.BannedPasswordUploadHandler;
import com.jadaptive.permissions.PermissionService;
import com.jadaptive.session.Session;
import com.jadaptive.session.SessionTimeoutException;
import com.jadaptive.session.SessionUtils;
import com.jadaptive.user.UserService;
import com.jadaptive.utils.FileUtils;

@WebServlet(name="uploadServlet", description="Servlet for handing file uploads", urlPatterns = { "/upload/*" })
public class UploadServlet extends HttpServlet {

	static Logger log = LoggerFactory.getLogger(UploadServlet.class);
	
	@Autowired
	SessionUtils sessionUtils;

	Map<String,UploadHandler> handlers = new HashMap<>();
	
	@Autowired
	BannedPasswordUploadHandler h;
	
	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	UserService userService; 
	
	@PostConstruct
	private void postConstruct() {
		handlers.put("bannedPasswords", h);
	}
	
	public void registerHandler(String name, UploadHandler handler) {
		handlers.put(name, handler);
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
			permissionService.setupUserContext(userService.findUsername(session.getUsername()));
		}
		
		try {
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();

			// Parse the request
			FileItemIterator iter = upload.getItemIterator(req);

			while (iter.hasNext()) {
			    FileItemStream item = iter.next();

			    if(Objects.isNull(handler)) {
			    	log.warn("Missing upload handler for {}", handlerName);
			    	continue;
			    }
			    
			    InputStream stream = new SessionStickyInputStream(
			    		item.openStream(), 
			    		session);
	
			    try {
			    	handler.handleUpload(handlerName, uri, item.getName(), stream);    
			    } finally {
			    	stream.close();
			    }
			    
			}
		
			super.doPost(req, resp);
			
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(Objects.nonNull(session)) {
				permissionService.clearUserContext();
			}
		}
		
		
	}

	private UploadHandler getUploadHandler(String handlerName) {
		return handlers.get(handlerName);
	}

	private static final long serialVersionUID = -6914120139469535232L;

	class SessionStickyInputStream extends InputStream {
		
		InputStream in;
		Session session;
		long lastTouch = System.currentTimeMillis();
		
		SessionStickyInputStream(InputStream in, Session session) {
			this.in = in;
			this.session = session;
		}
		
		@Override
		public int read() throws IOException {
			touchSession();
			return in.read();
		}
		
		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			touchSession();
			return in.read(buf, off, len);
		}
		
		@Override
		public void close() {
			IOUtils.closeQuietly(in);
		}
		
		private void touchSession() {
			if((System.currentTimeMillis() - lastTouch) >  30000) {
				try {
					sessionUtils.touchSession(session);
				} catch (SessionTimeoutException e) {
				}
				lastTouch = System.currentTimeMillis();
			}
		}
		
	}
}
