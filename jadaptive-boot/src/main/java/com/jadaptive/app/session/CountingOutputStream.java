package com.jadaptive.app.session;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class CountingOutputStream extends ServletOutputStream {

	CountingOutputStreamListener listener;
	ServletOutputStream out;
	long count = 0;
	
	public CountingOutputStream(ServletOutputStream out, CountingOutputStreamListener listener) {
		this.out = out;
		this.listener = listener;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkOpen();
		count+=len;
		out.write(b, off, len);
		
		System.out.println("That's " + len + " more bytes");
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
	
	private void checkOpen() throws IOException {
		if(Objects.isNull(out)) {
			throw new IOException("The outputstream is closed!");
		}
	}

	@Override
	public void close() throws IOException {
		if(Objects.nonNull(out)) {
			listener.closed(count);
			out.close();
			out = null;
		}
	}

	@Override
	public boolean isReady() {
		return out.isReady();
	}

	@Override
	public void setWriteListener(WriteListener listener) {
		out.setWriteListener(listener);
	}
	
	
}
