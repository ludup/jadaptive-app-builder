package com.jadaptive.app.cluster;

import com.jadaptive.api.cluster.ClusterController.ClusterInfo;

public record ClusterJoinInfo(
		ClusterInfo info, 
		String mongoDbUrl, 
		String keyserverHost,
		int  keyserverPort,
		String keyserverPath,
		String keyserverSecret,
		String keyserverReference,
		String privateKey,
		String publicKey,
		String clusterName,
		String props) { }