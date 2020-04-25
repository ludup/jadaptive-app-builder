package com.jadaptive.plugins.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.StartupAware;
import com.maverick.nio.Daemon;
import com.maverick.nio.DaemonContext;
import com.maverick.nio.LicenseManager;
import com.maverick.ssh.SshException;
import com.maverick.sshd.SshContext;
import com.maverick.sshd.auth.DefaultAuthenticationMechanismFactory;
import com.maverick.util.IOUtil;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshKeyPairGenerator;

@Service
public class LegacySSHServerImpl extends Daemon implements LegacySSHServer, StartupAware {

	static Logger log = LoggerFactory.getLogger(LegacySSHServerImpl.class);
	
	@Autowired
	private PasswordAuthenticator passwordAuthenticator;
	
	@Autowired
	private AuthorizedKeysAuthenticator authorizedKeysAuthenticator;
	
	@Override
	protected void configure(DaemonContext context) throws IOException, SshException {
		
		SshContext sshContext = new SshContext(this);
		
		int port = ApplicationProperties.getValue("legacy.port", 4444);
		boolean extenalAccess = ApplicationProperties.getValue("legacy.externalAccess", true);

		sshContext.setAuthenicationMechanismFactory(new DefaultAuthenticationMechanismFactory());
		
		if(ApplicationProperties.getValue("legacy.permitPassword", true)) {
			sshContext.getAuthenticationMechanismFactory().addProvider(passwordAuthenticator);
		}
		
		sshContext.getAuthenticationMechanismFactory().addProvider(authorizedKeysAuthenticator);
		
		context.addListeningInterface(extenalAccess ? "::" : "::1", port, sshContext);

		
		try {
//			sshContext.loadOrGenerateHostKey(
//					new File(ApplicationProperties.getConfFolder(), "legacy_host_key_ed25519"), 
//					SshKeyPairGenerator.ED25519, 0);
			sshContext.loadOrGenerateHostKey(
					new File(ApplicationProperties.getConfFolder(), "legacy_host_key_ecdsa_256"), 
					SshKeyPairGenerator.ECDSA, 256);
			sshContext.loadOrGenerateHostKey(
					new File(ApplicationProperties.getConfFolder(), "legacy_host_key_ecdsa_384"), 
					SshKeyPairGenerator.ECDSA, 384);
			sshContext.loadOrGenerateHostKey(
					new File(ApplicationProperties.getConfFolder(), "legacy_host_key_ecdsa_521"), 
					SshKeyPairGenerator.ECDSA, 521);
			sshContext.loadOrGenerateHostKey(
					new File(ApplicationProperties.getConfFolder(), "legacy_host_key_rsa_2048"), 
					SshKeyPairGenerator.SSH2_RSA, 2048);
		} catch (InvalidPassphraseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
	}
	
	@Override
	public void onApplicationStartup() {
		
		File licenseFile = new File(ApplicationProperties.getConfFolder(), "sshd-license.txt");
		if(!licenseFile.exists()) {
			log.info("Missing sshd-license.txt in conf folder");
			return;
		}
		
		try {
			LicenseManager.addLicense(IOUtil.toString(new FileInputStream(licenseFile), "UTF-8"));
			
			startup();
		} catch (IOException e) {
			log.error("SSHD service failed to start", e);
		}
	}

	@Override
	public Integer getStartupPosition() {
		return 0;
	}

}
