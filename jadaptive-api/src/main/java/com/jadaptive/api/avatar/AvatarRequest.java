package com.jadaptive.api.avatar;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.jadaptive.api.user.User;

public final class AvatarRequest {

	public final static class Builder {
		private Optional<User> user = Optional.empty();
		private Optional<String> username = Optional.empty();
		private Optional<String> name = Optional.empty();
		private Optional<String> mobilePhone = Optional.empty();
		private Optional<String> email = Optional.empty();
		private Optional<String> id = Optional.empty();

		public Builder forUser(User user) {
			
			this.user = Optional.of(user);
			
			if (StringUtils.isNotBlank(user.getEmail()))
				withEmail(user.getEmail());
			if (StringUtils.isNotBlank(user.getName()))
				withName(user.getName());
			if (StringUtils.isNotBlank(user.getMobilePhone()))
				withMobilePhone(user.getMobilePhone());
			return withUsername(user.getUsername()).withId(user.getUuid());
		}

		public Builder withUsername(String username) {
			this.username = Optional.of(username);
			return this;
		}

		public Builder withName(String name) {
			this.name = Optional.of(name);
			return this;
		}

		public Builder withEmail(String email) {
			this.email = Optional.of(email);
			return this;
		}

		public Builder withMobilePhone(String mobilePhone) {
			this.mobilePhone = Optional.of(mobilePhone);
			return this;
		}

		public Builder withId(String id) {
			this.id = Optional.of(id);
			return this;
		}

		public AvatarRequest build() {
			return new AvatarRequest(this);
		}
	}

	private final Optional<User> user;
	private final Optional<String> username;
	private final Optional<String> email;
	private final Optional<String> mobilePhone;
	private final Optional<String> name;
	private final Optional<String> id;

	private AvatarRequest(Builder builder) {
		this.user = builder.user;
		this.username = builder.username;
		this.email = builder.email;
		this.mobilePhone = builder.mobilePhone;
		this.name = builder.name;
		this.id = builder.id;
	}

	public Optional<User> user() {
		return user;
	}

	public Optional<String> username() {
		return username;
	}

	public Optional<String> email() {
		return email;
	}

	public Optional<String> mobilePhone() {
		return mobilePhone;
	}

	public Optional<String> name() {
		return name;
	}

	public Optional<String> id() {
		return id;
	}
}
