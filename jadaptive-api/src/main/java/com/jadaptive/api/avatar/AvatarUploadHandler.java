package com.jadaptive.api.avatar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.upload.UploadHandler;
import com.jadaptive.api.upload.UploadIterator;
import com.jadaptive.api.user.UserService;

@Component
public class AvatarUploadHandler extends AuthenticatedService implements UploadHandler {

	static Logger log = LoggerFactory.getLogger(AvatarUploadHandler.class);
	
	@Autowired
	private UserService userService;  
	
	@Autowired
	private PermissionService permissionService;

	@Override
	public boolean isSessionRequired() {
		return true;
	}

	@Override
	public String getURIName() {
		return "avatar";
	}

	@Override
	public void handleUpload(String handlerName, String uri, Map<String, String[]> parameters, UploadIterator uploads)
			throws IOException, SessionTimeoutException, UnauthorizedException {
		
		try { 
			uploads.forEachRemaining((u)->{
				String enc;
				try(InputStream in = u.openStream()) {
					enc = Base64.getEncoder().encodeToString(in.readAllBytes());
				} catch (IOException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
				
				try(var ctx = permissionService.userContext()) {
					var user = permissionService.getCurrentUser();
					user.setAvatar("data:" + u.getContentType() + ";base64," + enc);
					userService.saveOrUpdate(user);
				}
			});

			Feedback.success("userInterface", "info.avatarUploaded");
		} catch(RuntimeException re) {
			throw re;
		} catch(Throwable e) {
			log.error("Failed to upload avatar", e);
			throw new IOException(e.getMessage(), e);
		} 
	}
}
