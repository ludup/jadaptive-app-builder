package com.jadaptive.api.cluster;

import java.util.Objects;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = ClusterService.RESOURCE_KEY, type = ObjectType.OBJECT)
@TableView(defaultColumns = {})
public class ClusterService extends AbstractUUIDEntity {
	public final static String HTTPS_SERVICE = "https";

	private static final long serialVersionUID = -7414091315758232866L;
	
	public static final String RESOURCE_KEY = "clusterService";

	@ObjectField(type = FieldType.TEXT)
	private String service;

	@ObjectField(type = FieldType.INTEGER)
	private int port;

	@ObjectField(type = FieldType.TEXT)
	private String publicKey;
	
	public ClusterService() {
	}

	public ClusterService(String service, int port) {
		this(service, port, null);
	}
	
	public ClusterService(String service, int port, String publicKey) {
		setService(service);
		setPort(port);
		setPublicKey(publicKey);
	}
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(port, service);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusterService other = (ClusterService) obj;
		return port == other.port && Objects.equals(service, other.service);
	}
}
