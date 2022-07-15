package com.jadaptive.api.session;

import java.io.IOException;
import java.io.InputStream;

public abstract class SessionStickyInputStream extends InputStream {
		
		InputStream in;
		Session session;
		protected long lastTouch = 0;
		
		public SessionStickyInputStream(InputStream in, Session session) throws IOException {
			this.in = in;
			this.session = session;
			checkSession();
		}
		
		@Override
		public int read() throws IOException {
			checkSession();
			return in.read();
		}
		
		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			checkSession();
			return in.read(buf, off, len);
		}
		
		protected void checkSession() throws IOException {
			if(lastTouch == 0 || System.currentTimeMillis() - lastTouch > 30000L) {
				lastTouch = System.currentTimeMillis();
				touchSession(session);
			}
		}

		protected abstract void touchSession(Session session) throws IOException;

		@Override
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		
		
		
	}