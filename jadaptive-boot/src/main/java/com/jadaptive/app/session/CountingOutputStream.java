package com.jadaptive.app.session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class CountingOutputStream extends OutputStream {

	OutputStream out;
	long count = 0;
	
	public CountingOutputStream(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkOpen();
		count+=len;
		out.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		checkOpen();
		write(new byte[] { (byte) b }, 0 , 1);
	}

	@Override
	public void flush() throws IOException {
		checkOpen();
		out.flush();
	}
	
	public void close() throws IOException {
		if(Objects.nonNull(out)) {
			out.close();
			out = null;
		}
	}
	private void checkOpen() throws IOException {
		if(Objects.isNull(out)) {
			throw new IOException("The outputstream is closed!");
		}
	}

	public long getCount() {
		return count;
	}
}
