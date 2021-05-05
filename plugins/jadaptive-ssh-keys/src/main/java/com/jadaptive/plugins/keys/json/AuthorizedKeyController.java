package com.jadaptive.plugins.keys.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.jadaptive.plugins.keys.PublicKeyType;

@Controller
@Extension
public class AuthorizedKeyController extends AuthenticatedController{
	
	public static final String KEY_DOWNLOAD = "key-download";
	
	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	@Autowired
	private UserService userService; 
	
	@RequestMapping(value = { "/generate/personal/key" }, produces = { "application/json" })
	@ResponseBody
	@PostMapping
	public RequestStatus generatePersonalKey(HttpServletRequest request, HttpServletResponse response,
			@RequestParam int type,
			@RequestParam String passphrase,
			@RequestParam String name) throws IOException, AccessDeniedException, UnauthorizedException {
		
		try {
			request.getSession().setAttribute(KEY_DOWNLOAD, 
					authorizedKeyService.createKeyFile(name, authorizedKeyService.createAuthorizedKey(
							PublicKeyType.values()[type], name, getCurrentUser()), passphrase));
			return new RequestStatusImpl(true);
		} catch (Throwable e) {
			return new RequestStatusImpl(false, e.getMessage());
		}
		
	}
	
	@RequestMapping(value = { "/generate/user/key" }, produces = { "application/json" })
	@ResponseBody
	@PostMapping
	public RequestStatus generateUserKey(HttpServletRequest request, HttpServletResponse response,
			@RequestParam int type,
			@RequestParam String name,
			@RequestParam String passphrase,
			@RequestParam String uuid) throws IOException, AccessDeniedException, UnauthorizedException {
		
		try {
			request.getSession().setAttribute(KEY_DOWNLOAD, 
					authorizedKeyService.createKeyFile(name, authorizedKeyService.createAuthorizedKey(
							PublicKeyType.values()[type], name, userService.getUserByUUID(uuid)), passphrase));
			return new RequestStatusImpl(true);
		} catch (Throwable e) {
			return new RequestStatusImpl(false, e.getMessage());
		}
		
	}
	
	@RequestMapping(value = { "/generate/download" }, produces = { "application/octet-stream" })
	@GetMapping
	public void downloadKey(HttpServletRequest request, HttpServletResponse response) throws IOException, AccessDeniedException, UnauthorizedException {
		
		File file = (File) request.getSession().getAttribute(KEY_DOWNLOAD);
		response.setContentLength((int)file.length());
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		try(FileInputStream in = new FileInputStream(file)) {
			IOUtils.copy(in, response.getOutputStream());
		}
		
		file.deleteOnExit();
		
	}
}
