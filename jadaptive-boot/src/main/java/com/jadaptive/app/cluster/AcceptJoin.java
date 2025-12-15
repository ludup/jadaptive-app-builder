package com.jadaptive.app.cluster;

import java.util.Base64;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.OAuth2ScopeResponse;
import com.jadaptive.api.auth.oauth2.OAuth2TokenService;
import com.jadaptive.api.cluster.ClusterController.ClusterInfo;
import com.jadaptive.api.cluster.ClusterManager;
import com.jadaptive.api.cluster.ClusterNode;
import com.jadaptive.api.cluster.ClusterNode.ClusterNodeStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.app.encrypt.RsaEncryptionProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Extension
@Controller
public class AcceptJoin implements OAuth2Scope, PluginController {
	
	@Autowired
	private OAuth2TokenService tokenService;
	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private TenantService tenantService;
	@Autowired
	private RsaEncryptionProvider encryptionProvider;

	public AcceptJoin() {
	}

	@Override
	public String getBundle() {
		return ClusterNode.RESOURCE_KEY;
	}

	@Override
	public String getId() {
		return "joinCluster";
	}

	@RequestMapping(value = "oauth2/join-cluster", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public OAuth2ScopeResponse<ClusterJoinInfo> getUserDetails(HttpServletRequest request, HttpServletResponse response,
			@RequestHeader("Authentication") String authentication) throws Exception {
		tokenService.authenticate(authentication, this);

		return tenantService.asSystem(() -> {
			
			if(!clusterManager.isJoined()) {
				return new OAuth2ScopeResponse<>("invalid_state", new IllegalStateException("Cannot join a node that is not joined to a cluster."));
			}
			
			var mongoDbUrl = ApplicationProperties.getValue("mongodb.connection", "mongodb://localhost:27017");
			if(mongoDbUrl.startsWith("mongodb://localhost")) {
				return new OAuth2ScopeResponse<>("invalid_state", new IllegalStateException("Cannot join a node that is using a localhost mongodb URL."));
			}
			
			String ksHost = ApplicationProperties.getValue("keyserver.host", "");
			String privKey = null;
			String pubKey = null;
			if(ksHost.equals("") && encryptionProvider.isEnabled()) {
				/* Not using key server, send local key pair */
				privKey = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(encryptionProvider.getPrvFile().toFile()));
				pubKey = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(encryptionProvider.getPubFile().toFile()));
			}
			
			return new OAuth2ScopeResponse<>(new  ClusterJoinInfo(
				new ClusterInfo(
					clusterManager.getServerId(), 
					ClusterNodeStatus.ONLINE,
					clusterManager.getServices(),
					clusterManager.streamAll().toList()),
				mongoDbUrl,
				ksHost,
				ApplicationProperties.getValue("keyserver.port", 443),
				ApplicationProperties.getValue("keyserver.path", "/ks/api/secrets"),
				ApplicationProperties.getValue("keyserver.secret", ""),
				ApplicationProperties.getValue("keyserver.reference", ""),
				ApplicationProperties.getValue("keyserver.insecureSsl", false),
				privKey,
				pubKey,
				ApplicationProperties.getValue("ha.clusterName", SchedulerService.GENERIC_JAD_CLUSTER),
				ApplicationProperties.getValue("ha.props", SchedulerService.JAD_JGROUPS)
			));
		});
		
		
	}

	@Override
	public void verifyPermissions(Optional<OAuth2Request> oauthRequest, User principal) throws AccessDeniedException {
	}
}
