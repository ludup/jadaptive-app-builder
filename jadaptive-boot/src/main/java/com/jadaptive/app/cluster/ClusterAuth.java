package com.jadaptive.app.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jgroups.Message;
import org.jgroups.auth.AuthToken;
import org.jgroups.util.Bits;
import org.jgroups.util.Util;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.cluster.ClusterManager;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.entity.ObjectNotFoundException;

public class ClusterAuth extends AuthToken {
	
	private static String localEncryptedToken;
	private static String localNodeId;

	private String encryptedToken = localEncryptedToken;
	private String nodeId = localNodeId;
	
	static void setup(String localEncryptedToken, String localNodeId) {
		ClusterAuth.localEncryptedToken = localEncryptedToken;
		ClusterAuth.localNodeId = localNodeId;
	}

	public ClusterAuth() {
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		Bits.writeString(encryptedToken, out);
		Bits.writeString(nodeId, out);
	}

	@Override
	public void readFrom(DataInput in) throws IOException {
		this.encryptedToken = Bits.readString(in);
		this.nodeId = Bits.readString(in);
	}

	public int size() {
		return Util.size(encryptedToken) + Util.size(nodeId);
	}

	@Override
	public String getName() {
		return ClusterAuth.class.getName();
	}

	@Override
	public boolean authenticate(AuthToken token, Message msg) {

		if (token instanceof ClusterAuth cauth) {
			
			var cmgr = clusterManager();
			var esrv = encryptionService();

			/*
			 * Authentication is basically
			 * "Are the two nodes using the same encryption service and keys?"
			 */

			try {
				var cnode = cmgr.getObjectByUUID(cauth.nodeId);
				var match = esrv.decrypt(cauth.encryptedToken).equals(cnode.getAuthenticationToken());
				if (match) {
					log.info("Node passed authentication");
				} else {
					log.warn(
							"Node failed authentication, encrypted tokens do not match. Check encryption configuration");
				}
				return match;
			} catch (ObjectNotFoundException onfe) {
				log.warn("Unknown node ID in authentication.");
				return false;
			}
		}

		if (log.isWarnEnabled())
			log.warn("Invalid AuthToken instance - wrong type or null");
		return false;
	}

	static ClusterManager clusterManager() {
		return ApplicationServiceImpl.getInstance().getBean(ClusterManager.class);
	}

	static EncryptionService encryptionService() {
		return ApplicationServiceImpl.getInstance().getBean(EncryptionService.class);
	}
}
