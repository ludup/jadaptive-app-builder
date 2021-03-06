package com.jadaptive.plugins.sshd;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKey;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.sshtools.common.auth.AbstractPublicKeyAuthenticationProvider;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.components.SshPublicKey;

@Component
@Qualifier(value = "defaultPkAuthentication")
public class AuthorizedKeyProvider extends AbstractPublicKeyAuthenticationProvider {

	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Override
	public boolean isAuthorizedKey(SshPublicKey key, SshConnection con) throws IOException {
		
		tenantService.setCurrentTenant(tenantService.resolveTenantName(con.getUsername()));
		
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
		} catch(ObjectNotFoundException e) { 
			return false;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

}
