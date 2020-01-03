package com.jadaptive.plugins.json.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.pf4j.Extension;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.PluginServlet;

@WebServlet(name="pluginServlet", description="Servlet for handing plugin controller requests", urlPatterns = { "/plugins/*" })
@Extension
public class ExampleServlet extends PluginServlet {

	static Logger log = LoggerFactory.getLogger(ExampleServlet.class);
	
//	@Autowired
//	SessionUtils sessionUtils;
	
//	@Autowired
//	PermissionService permissionService; 
//	
//	@Autowired
//	UserService userService; 
	
	@Autowired
	PluginManager pluginManager; 
	
	@PostConstruct
	private void postConstruct() {
		System.out.println("Done");
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		System.out.println("Done");
//		String uri = FileUtils.checkStartsWithNoSlash(req.getPathInfo());
//		String pluginName = FileUtils.firstPathElement(uri);
//		
//		PluginWrapper wrapper = pluginManager.getExtension(pluginName);
//		PluginServlet servlet;
//		
//		Session session = sessionUtils.getActiveSession(req);
//		
//		if(Objects.nonNull(session)) {
//			permissionService.setupUserContext(userService.findUsername(session.getUsername()));
//			
//			try {
//				doService(req, resp);
//			} finally {
//				permissionService.clearUserContext();
//			}
//		} else {
//			doService(req, resp);
//		}
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
//			if((System.currentTimeMillis() - lastTouch) >  30000) {
//				try {
//					sessionUtils.touchSession(session);
//				} catch (SessionTimeoutException e) {
//				}
//				lastTouch = System.currentTimeMillis();
//			}
		}
		
	}
}
