package com.jadaptive.plugins.keys;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.upload.UploadHandler;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.publickey.SshPublicKeyFile;
import com.sshtools.common.publickey.SshPublicKeyFileFactory;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.common.util.IOUtils;

@Extension
public class PublicKeyUploadHandler extends AuthenticatedService implements UploadHandler {

	static Logger log = LoggerFactory.getLogger(PublicKeyUploadHandler.class);
	
	@Autowired
	private AuthorizedKeyService keyService;  
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void handleUpload(String handlerName, String uri, Map<String, String> parameters, String filename,
			InputStream in) throws IOException, SessionTimeoutException, UnauthorizedException {
		
		try { 

			String contents = IOUtils.readUTF8StringFromStream(in);
			
			SshPublicKey key;
			String name = parameters.get("name");
			try { 
				SshPublicKeyFile kfile = SshPublicKeyFileFactory.parse(IOUtils.toInputStream(contents, "UTF-8"));
				key = kfile.toPublicKey();
				if(StringUtils.isBlank(name)) {
					name = kfile.getComment();
					if(StringUtils.isBlank(name)) {
						name = "";
					}
				}
			} catch(IOException e) {

				SshPrivateKeyFile kfile = SshPrivateKeyFileFactory.parse(IOUtils.toInputStream(contents, "UTF-8"));
				key = kfile.toKeyPair(parameters.get("passphrase")).getPublicKey();
				if(StringUtils.isBlank(name)) {
					name = kfile.getComment();
					if(StringUtils.isBlank(name)) {
						name = "";
					}
				}
			}
			
			if(StringUtils.isBlank(name)) {
				name = "Uploaded by " + getCurrentUser().getUsername() + " #" + System.currentTimeMillis();
			}
			
			keyService.importPublicKey(name,
					SshKeyUtils.getOpenSSHFormattedKey(key), 
					key.getAlgorithm(), 
					SshKeyUtils.getFingerprint(key), 
					getCurrentUser(),
					false);

		} catch(Throwable e) {
			log.error("Failed to upload public key", e);
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public void sendSuccessfulResponse(HttpServletResponse resp, String handlerName, String uri) throws IOException {
		RequestStatus status = new RequestStatusImpl(true);
		byte[] data = objectMapper.writeValueAsBytes(status);
		resp.setStatus(200);
		resp.getOutputStream().write(data);
		resp.setContentLength(data.length);
		resp.setContentType("application/json");
	}
	
	@Override
	public void sendFailedResponse(HttpServletResponse resp, String handlerName, String uri, Throwable e) throws IOException {
		RequestStatus status = new RequestStatusImpl(false, e.getMessage());
		byte[] data = objectMapper.writeValueAsBytes(status);
		resp.setStatus(200);
		resp.getOutputStream().write(data);
		resp.setContentLength(data.length);
		resp.setContentType("application/json");
	}
	
	@Override
	public boolean isSessionRequired() {
		return false;
	}

	@Override
	public String getURIName() {
		return "public-key";
	}

}
