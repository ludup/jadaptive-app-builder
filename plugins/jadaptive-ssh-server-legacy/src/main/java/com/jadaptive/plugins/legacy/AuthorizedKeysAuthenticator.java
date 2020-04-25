package com.jadaptive.plugins.legacy;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKey;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshKeyUtils;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.sshd.Connection;
import com.maverick.sshd.auth.DefaultPublicKeyAuthenticationProvider;
import com.maverick.sshd.platform.PermissionDeniedException;
import com.sshtools.publickey.SshPublicKeyFile;

@Component
public class AuthorizedKeysAuthenticator extends DefaultPublicKeyAuthenticationProvider {

	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Override
	public boolean isAuthorizedKey(SshPublicKey key, Connection con) throws IOException {
		
		tenantService.setCurrentTenant(tenantService.resolveTenantName(con.getUsername()));
		
		try {
			User user = userService.getUser(con.getUsername());
			
			for(AuthorizedKey authorizedKey : authorizedKeyService.getAuthorizedKeys(user)) {
				SshPublicKey publicKey = SshKeyUtils.getPublicKey(authorizedKey.getPublicKey());
				if(publicKey.equals(key)) {
					con.setProperty(LegacySSHServer.TENANT, tenantService.getCurrentTenant());
					con.setProperty(LegacySSHServer.USER, user);
					return true;
				}
			}
			return false;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

	@Override
	public void add(SshPublicKey arg0, String arg1, Connection arg2)
			throws IOException, PermissionDeniedException, SshException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<SshPublicKeyFile> getKeys(Connection arg0) throws PermissionDeniedException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(SshPublicKey arg0, Connection arg1) throws IOException, PermissionDeniedException, SshException {
		// TODO Auto-generated method stub
		
	}

}
