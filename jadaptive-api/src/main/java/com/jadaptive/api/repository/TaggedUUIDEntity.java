package com.jadaptive.api.repository;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectField;

@JsonIgnoreProperties(value = { "_clz" })
public abstract class TaggedUUIDEntity extends UUIDEntity {

	private static final long serialVersionUID = -1903178803319512374L;
	
	private Boolean system;
	private Boolean hidden;
	private boolean summarise;
	
	@ObjectField(type = FieldType.TIMESTAMP, hidden = true, searchable = true)
	@ExcludeView(values = FieldView.TABLE)
	private Date created;
	
	@ObjectField(type = FieldType.TIMESTAMP, hidden = true, searchable = true)
	@ExcludeView(values = FieldView.TABLE)
	private Date lastModified;


	@ObjectField(type = FieldType.TEXT, searchable = true, renderer = FieldRenderer.TAGS, weight = 9999)
	private Collection<String> tags = new HashSet<>();
	
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
	
	public Collection<String> getTags() {
		return tags;
	}

	public void setTags(Collection<String> tags) {
		this.tags = new HashSet<>(tags);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
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
		TaggedUUIDEntity other = (TaggedUUIDEntity) obj;
		if (getUuid() == null) {
			if (other.getUuid() != null)
				return false;
		} else if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}
	
	
}
