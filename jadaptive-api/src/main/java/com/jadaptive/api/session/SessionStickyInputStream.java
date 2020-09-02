package com.jadaptive.api.session;

import java.io.IOException;
import java.io.InputStream;

public class SessionStickyInputStream extends InputStream {
		
		InputStream in;
		Session session;
		protected long lastTouch = System.currentTimeMillis();
		protected SessionUtils sessionUtils;
		
		public SessionStickyInputStream(InputStream in, Session session, SessionUtils sessionUtils) {
			this.sessionUtils = sessionUtils;
			this.in = in;
			this.session = session;
		}
		
		@Override
		public int read() throws IOException {
			touchSession();
			return in.read();
		}
		
		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			touchSession();
			return in.read(buf, off, len);
		}
		
		@Override
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		
		protected void touchSession() {
			if ((System.currentTimeMillis() - lastTouch) > 30000) {
				try {
					sessionUtils.touchSession(session);
				} catch (SessionTimeoutException e) {
				}
				lastTouch = System.currentTimeMillis();
			}
		}
		
	}