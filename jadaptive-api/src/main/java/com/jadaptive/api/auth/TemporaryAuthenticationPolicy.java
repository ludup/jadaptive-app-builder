package com.jadaptive.api.auth;

public class TemporaryAuthenticationPolicy extends AuthenticationPolicy {
		
		private static final long serialVersionUID = -1704557093329583064L;

		@Override
		public boolean isSessionRequired() {
			return false;
		}
		
		@Override
		public Boolean getPasswordOnFirstPage() {
			return false;
		}
		
		public boolean isTemporary() {
			return true;
		}
}