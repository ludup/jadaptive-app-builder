package com.jadaptive.plugins.sshd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.tenant.TenantService;
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
	
	@Autowired
	private TenantService tenantService; 
	
	@Override
	public boolean isAuthorizedKey(SshPublicKey key, SshConnection con) throws IOException {
		
		tenantService.setCurrentTenant(StringUtils.substringAfter(con.getUsername(), "@"));
		
		try {
			User user = userService.getUser(con.getUsername());
			
			for(AuthorizedKey authorizedKey : authorizedKeyService.getAuthorizedKeys(user)) {
				SshPublicKey publicKey = SshKeyUtils.getPublicKey(authorizedKey.getPublicKey());
				if(publicKey.equals(key)) {
					con.setProperty(SSHDService.TENANT, tenantService.getCurrentTenant());
					con.setProperty(SSHDService.USER, user);
					return true;
				}
			}
			return false;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

}
