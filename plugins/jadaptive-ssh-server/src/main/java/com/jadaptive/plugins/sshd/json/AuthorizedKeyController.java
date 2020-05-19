package com.jadaptive.plugins.sshd.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKey;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;

@Extension
@Controller
public class AuthorizedKeyController extends AuthenticatedController implements PluginController {

	static Logger log = LoggerFactory.getLogger(AuthorizedKeyController.class);

	@Autowired
	private UserService userService; 
	
	@Autowired
	private AuthorizedKeyService keyService; 
	
	@RequestMapping(value = { "/app/api/agent/authorizedKeys/{username}", "/authorizedKeys/{username}" }, method = { RequestMethod.GET, RequestMethod.POST }, produces = { "text/plain" })
	@ResponseBody
	@GetMapping()
	public String listAuthorizedKeys(HttpServletRequest request, HttpServletResponse response, @PathVariable String username) throws IOException, AccessDeniedException, UnauthorizedException {
		
		setupSystemContext();
		
		StringBuffer authorizedKeys = new StringBuffer();
		
		try {

			User principal = userService.getUser(username);
			
			if(Objects.isNull(principal)) {
				authorizedKeys.append(String.format("# No keys for user %s", username));
			} else {
					
				Collection<AuthorizedKey> keys = keyService.getAuthorizedKeys(principal);
				
				for(AuthorizedKey key : keys) {
					if(key.getTags().contains(AuthorizedKeyService.SSH_TAG)) {
						
						SshPublicKey k = SshKeyUtils.getPublicKey(key.getPublicKey());

						if(authorizedKeys.length() > 0) {
							authorizedKeys.append("\r\n");
						}
						authorizedKeys.append(
							SshKeyUtils.getOpenSSHFormattedKey(k) + " " + key.getName());	

					}

				}
			}
			
		} catch(Throwable t) { 
			log.error("Failed to get keys for {}", username);
			authorizedKeys.append(String.format("# Encountered error attempting to retrieve keys for %s", username));
		} finally {
			clearUserContext();
		}
		
		return authorizedKeys.toString();
	}
}
