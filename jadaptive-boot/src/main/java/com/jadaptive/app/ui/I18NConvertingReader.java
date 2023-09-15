package com.jadaptive.app.ui;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.jadaptive.api.i18n.I18nService;

public class I18NConvertingReader extends Reader {

	protected PushbackReader pushbackReader = null;
	protected StringBuilder bundleNameBuffer = new StringBuilder();
	protected StringBuilder keyNameBuffer = new StringBuilder();
	protected StringBuilder argsValueBuffer = new StringBuilder();
	protected String tokenValue = null;
	protected int tokenValueIndex = 0;

	private I18nService i18n;
	private String filename;
	private List<String> args = new ArrayList<>();
	
	public I18NConvertingReader(Reader source, I18nService i18n, String filename) {
		this.pushbackReader = new PushbackReader(source, 2);
		this.i18n = i18n;
		this.filename = filename;
	}
	
	public int read(CharBuffer target) throws IOException {
		throw new RuntimeException("Operation Not Supported");
	}

	public int read() throws IOException {
		if (this.tokenValue != null) {
			if (this.tokenValueIndex < this.tokenValue.length()) {
				return this.tokenValue.charAt(this.tokenValueIndex++);
			}
			if (this.tokenValueIndex == this.tokenValue.length()) {
				this.tokenValue = null;
				this.tokenValueIndex = 0;
				this.args.clear();
				this.bundleNameBuffer.setLength(0);
				this.keyNameBuffer.setLength(0);
				this.argsValueBuffer.setLength(0);
			}
		}

		int data = this.pushbackReader.read();
		if (data != '$')
			return data;

		data = this.pushbackReader.read();
		if (data != '{') {
			this.pushbackReader.unread(data);
			return '$';
		}
		
		/**
		 * Now expecting i18n replacement value with <bundle>:<key>:<arg0>:<arg1> etc,
		 */
		
		data = this.pushbackReader.read();
		while (data != ':') {
			this.bundleNameBuffer.append((char) data);
			if(bundleNameBuffer.length() > 99) {
				 throw new IllegalStateException("Potential bad i18n format found in file " + filename);
			 }
			data = this.pushbackReader.read();
		}
		
		data = this.pushbackReader.read();
		while (data != ':' && data != '}') {
			this.keyNameBuffer.append((char) data);
			if(keyNameBuffer.length() > 99) {
				 throw new IllegalStateException("Potential bad i18n format found in file " + filename);
			 }
			data = this.pushbackReader.read();
		}
		
		while(data!='}') {
			data = this.pushbackReader.read();
			 while (data != ':' && data != '}'); {
				this.argsValueBuffer.append((char) data);
				data = this.pushbackReader.read();
				 if(argsValueBuffer.length() > 99) {
					 throw new IllegalStateException("Potential bad i18n format found in file " + filename);
				 }
			}
			 if(args.size() > 99) {
				 throw new IllegalStateException("Potential bad i18n format found in file " + filename);
			 }
			args.add(argsValueBuffer.toString());
			argsValueBuffer.setLength(0);
		}

		this.tokenValue = i18n.format(bundleNameBuffer.toString(), Locale.getDefault(), keyNameBuffer.toString(), args.toArray(new Object[0]));

		if (this.tokenValue.length() == 0) {
			return read();
		} else {
			return this.tokenValue.charAt(this.tokenValueIndex++);
		}

	}

	public int read(char cbuf[]) throws IOException {
		return read(cbuf, 0, cbuf.length);
	}

	public int read(char cbuf[], int off, int len) throws IOException {
		int charsRead = 0;
		for (int i = 0; i < len; i++) {
			int nextChar = read();
			if (nextChar == -1) {
				if (charsRead == 0) {
					charsRead = -1;
				}
				break;
			}
			charsRead = i + 1;
			cbuf[off + i] = (char) nextChar;
		}
		return charsRead;
	}

	public void close() throws IOException {
		this.pushbackReader.close();
	}

	public long skip(long n) throws IOException {
		throw new RuntimeException("Operation Not Supported");
	}

	public boolean ready() throws IOException {
		return this.pushbackReader.ready();
	}

	public boolean markSupported() {
		return false;
	}

	public void mark(int readAheadLimit) throws IOException {
		throw new RuntimeException("Operation Not Supported");
	}

	public void reset() throws IOException {
		throw new RuntimeException("Operation Not Supported");
	}
}
