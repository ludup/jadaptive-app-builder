package com.jadaptive.api.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;

@JsonIgnoreProperties(value = { "_clz" })
public abstract class AbstractUUIDEntity extends UUIDEntity {

	private static final long serialVersionUID = -1903178803319512374L;
	
	@ObjectField(type = FieldType.TIMESTAMP, hidden = true)
	Date created;
	
	@ObjectField(type = FieldType.TIMESTAMP, hidden = true)
	Date lastModified;

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractUUIDEntity other = (AbstractUUIDEntity) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
	
}
