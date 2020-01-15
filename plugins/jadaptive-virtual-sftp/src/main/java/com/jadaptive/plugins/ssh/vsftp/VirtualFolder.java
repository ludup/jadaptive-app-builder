package com.jadaptive.plugins.ssh.vsftp;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.template.Entity;
import com.jadaptive.api.template.EntityField;
import com.jadaptive.api.template.FieldType;

@Entity(name="Virtual Folder", alias = "vfolder", type = EntityType.COLLECTION)
public class VirtualFolder extends AssignableUUIDEntity {

	@EntityField(description = "The path on which this folder is mounted within the virtual file system", type = FieldType.TEXT)
	String mountPath;
	
	@EntityField(description = "The destination URI serving as the source for this virutal folder", type = FieldType.TEXT)
	String destinationUri;

	public String getMountPath() {
		return mountPath;
	}

	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}

	public String getDestinationUri() {
		return destinationUri;
	}

	public void setDestinationUri(String destinationUri) {
		this.destinationUri = destinationUri;
	}
}
