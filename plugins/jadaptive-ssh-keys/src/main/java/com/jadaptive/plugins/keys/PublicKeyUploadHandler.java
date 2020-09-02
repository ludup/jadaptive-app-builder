package com.jadaptive.plugins.keys;

import java.io.IOException;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.user.User;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;

@Extension
public class PublicKeyUploadHandler extends AuthenticatedService {

	static Logger log = LoggerFactory.getLogger(PublicKeyUploadHandler.class);
	
	@Autowired
	private AuthorizedKeyService keyService; 
	
	public void handleUpload(User user, String name, SshPublicKey key) throws IOException, SessionTimeoutException, UnauthorizedException {
		
		setupUserContext(user);
		
		try { 
			keyService.importPublicKey(name,
					SshKeyUtils.getOpenSSHFormattedKey(key), 
					key.getAlgorithm(), 
					SshKeyUtils.getFingerprint(key), 
					getCurrentUser(), 
					AuthorizedKeyService.SSH_TAG);

		} catch(Throwable e) {
			log.error("Failed to upload public key", e);
			throw new IOException(e.getMessage(), e);
		} finally {
			clearUserContext();
		}
	}


}
