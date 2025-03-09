package com.jadaptive.plugins.sshd;

import java.util.Objects;
import java.util.Optional;

public final class UserSpec {
	public final static class Builder {

		private String username;
		private Optional<String> tenant = Optional.empty();
		private Optional<String> device = Optional.empty();
		private char deviceSeparator = '@';
		private char tenantSeparator = '#';

		public Builder withDeviceSeparator(char deviceSeparator) {
			this.deviceSeparator = deviceSeparator;
			return this;
		}

		public Builder withTenantSeparator(char tenantSeparator) {
			this.tenantSeparator = tenantSeparator;
			return this;
		}
		
		public Builder withUsername(String username) {
			this.username = username;
			return this;
		}
		
		public Builder withDevice(String device) {
			this.device = Optional.of(device);
			return this;
		}
		
		public Builder withTenant(String tenant) {
			this.tenant = Optional.of(tenant);
			return this;
		}
		
		public Builder withSpec(String spec) {
			
			var idx = spec.indexOf(deviceSeparator);
			if(idx == -1) {
				device = Optional.empty();
			}
			else {
				device = Optional.of(spec.substring(0,  idx));
				spec = spec.substring(idx + 1);						
			}
			
			idx = spec.indexOf(tenantSeparator);
			if(idx == -1) {
				tenant = Optional.empty();
				username = spec;
			}
			else {
				tenant = Optional.of(spec.substring(idx + 1));
				username = spec.substring(0,  idx);
			}
			
			return this;
		}
		
		public UserSpec build() {
			return new UserSpec(this);
		}

	}

	private final String username;
	private final Optional<String> tenant;
	private final Optional<String> device;
	private final char deviceSeparator;
	private final char tenantSeparator;

	private UserSpec(Builder builder) {
		this.username = Objects.requireNonNull(builder.username);
		this.tenant = builder.tenant;
		this.device = builder.device;
		this.deviceSeparator = builder.deviceSeparator;
		this.tenantSeparator = builder.tenantSeparator;
	}

	public String username() {
		return username;
	}

	public String tenant() {
		return tenantOr().orElseThrow(() -> new IllegalStateException("UserSpec does not have a tenant."));
	}

	public Optional<String> tenantOr() {
		return tenant;
	}

	public String device() {
		return deviceOr().orElseThrow(() -> new IllegalStateException("UserSpec does not have a device."));
	}

	public Optional<String> deviceOr() {
		return device;
	}
	
	@Override
	public String toString() {
		var b = new StringBuilder();
		device.ifPresent(d -> {
			b.append(d);
			b.append(deviceSeparator);
		});
		b.append(username);
		tenant.ifPresent(t -> {
			b.append(tenantSeparator);
			b.append(t);
		});
		return b.toString();
	}
}
