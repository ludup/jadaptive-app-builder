package com.jadaptive.api.cluster;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.tenant.TenantService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ClusterController {
	
	public record ClusterInfo(String serverId, List<ClusterNode> nodes) { }
	
	@Autowired
	private ClusterManager clusterManager;
	
	@Autowired
	private TenantService tenantService;

	@RequestMapping(value = "/app/api/cluster-info", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<ClusterInfo> cluster(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return tenantService.asSystem(() -> new ResourceStatus<>(new ClusterInfo(clusterManager.getServerId(), clusterManager.streamAll().toList())));
	}
}
