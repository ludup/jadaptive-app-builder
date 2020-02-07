package com.jadaptive.plugins.sshd;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.auth.AbstractPublicKeyAuthenticationProvider;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.components.SshPublicKey;

@Component
public class AuthorizedKeyProvider extends AbstractPublicKeyAuthenticationProvider {

	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	@Autowired
	private UserService userService; 
	
	@Override
	public boolean isAuthorizedKey(SshPublicKey key, SshConnection con) throws IOException {
		User user = userService.findUsername(con.getUsername());
		for(AuthorizedKey authorizedKey : authorizedKeyService.getAuthorizedKeys(user)) {
			SshPublicKey publicKey = SshKeyUtils.getPublicKey(authorizedKey.getPublicKey());
			if(publicKey.equals(key)) {
				return true;
			}
		}
		return false;
	}

}
