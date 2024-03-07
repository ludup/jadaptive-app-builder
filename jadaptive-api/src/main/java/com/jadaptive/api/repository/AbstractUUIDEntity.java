package com.jadaptive.api.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectField;

@JsonIgnoreProperties(value = { "_clz" })
public abstract class AbstractUUIDEntity extends UUIDEntity {

	private static final long serialVersionUID = -1903178803319512374L;
	
	Boolean system;
	Boolean hidden;
	boolean summarise;
	
	@ObjectField(type = FieldType.TIMESTAMP, hidden = true)
	@ExcludeView(values = FieldView.TABLE)
	Date created;
	
	@ObjectField(type = FieldType.TIMESTAMP, hidden = true)
	@ExcludeView(values = FieldView.TABLE)
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
	
	public Boolean isSystem() {
		return system==null ? Boolean.FALSE : system;
	}

	public void setSystem(Boolean system) {
		this.system = system;
	}

//	public Boolean isHidden() {
//		return hidden==null ? Boolean.FALSE : hidden;
//	}
//
//	public void setHidden(Boolean hidden) {
//		this.hidden = hidden;
//	}

	public boolean isSummarise() {
		return summarise;
	}

	public void setSummarise(boolean summarise) {
		this.summarise = summarise;
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
