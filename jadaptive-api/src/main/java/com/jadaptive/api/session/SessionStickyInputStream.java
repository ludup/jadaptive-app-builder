package com.jadaptive.api.session;

import java.io.IOException;
import java.io.InputStream;

public abstract class SessionStickyInputStream extends InputStream {
		
		InputStream in;
		Session session;
		protected long lastTouch = System.currentTimeMillis();
		
		public SessionStickyInputStream(InputStream in, Session session) {
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
		
		protected abstract void touchSession();

		@Override
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		
		
		
	}