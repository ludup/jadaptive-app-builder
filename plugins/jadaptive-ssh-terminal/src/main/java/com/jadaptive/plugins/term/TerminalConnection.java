package com.jadaptive.plugins.term;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.app.WebSocketClient;
import com.jadaptive.api.app.WebSocketOutput;
import com.sshtools.terminal.emulation.Terminal;
import com.sshtools.terminal.websocket.ClientSideWebSocketTerminal;
import com.sshtools.terminal.websocket.TerminalWebSocketChannel;
import com.sshtools.terminal.websocket.WebSocketTerminal;

public abstract class TerminalConnection implements TerminalWebSocketChannel, WebSocketOutput {

	static Logger log = LoggerFactory.getLogger(TerminalConnection.class);
	
	protected ByteBuffer buffer;
	protected Object sendLock = new Object();
	protected Terminal terminal;

	private WebSocketClient websocket;
	private ScheduledExecutorService dispatcher;
	private ScheduledFuture<?> lastTask;
	private Object wait = new Object();
	private int bufferSize = 4096;
	private int sendDelay = 5;
	private int sendWait = 5;
	private OutputStream out;
	private Exception error;
	private PipedOutputStream pipeOut;
	private boolean inited;
	
	public TerminalConnection(WebSocketClient websocket) {
		this.dispatcher = Executors.newScheduledThreadPool(1);
		this.websocket = websocket;
		buffer = ByteBuffer.allocate(bufferSize);
		try {
			terminal = new ClientSideWebSocketTerminal(this);
			onOpen();

		} catch (Exception e) {
			log.error("Failed to connect to server.", e);
			error = e;
		}
	}
	
	public final void close() {
		onClose();
	}
	
	protected abstract void onClose();
	
	public Terminal getTerminal() {
		return terminal;
	}
	
	protected final void dataIn(byte[] array, int off, int len) throws IOException {
		PipedOutputStream po = pipeOut;
		if (po != null) {
			po.write(array, off, len);
			po.flush();
		} else {
			onDataIn(array, off, len);
		}
	};
	
	protected abstract void onDataIn(byte[] array, int off, int len) throws IOException;
	
	@Override
	public InputStream acquire() throws IOException {
		if (pipeOut != null) {
			throw new IllegalStateException("Already acquired.");
		}
		pipeOut = new PipedOutputStream();
		return new PipedInputStream(pipeOut) {
			@Override
			public void close() throws IOException {
				if (pipeOut == null) {
					throw new IOException("Already released.");
				}
				try {
					super.close();
				} finally {
					pipeOut = null;
				}
			}
		};
	}

	public OutputStream getOutputStream() {
		if (this.out == null) {
			this.out = new ByteArrayOutputStream() {
				@Override
				public synchronized void flush() throws IOException {
					send(ClientSideWebSocketTerminal.DATA, toByteArray());
					reset();
				}
			};
		}
		return out;
	}

	public int getSendDelay() {
		return sendDelay;
	}

	public void setSendDelay(int sendDelay) {
		this.sendDelay = sendDelay;
	}

	public int getSendWait() {
		return sendWait;
	}

	public void setSendWait(int sendWait) {
		this.sendWait = sendWait;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		if (buffer != null) {
			synchronized (buffer) {
				ByteBuffer b = ByteBuffer.allocate(bufferSize);
				b.put(buffer);
				buffer = b;
			}
		}
	}
	
	@Override
	public void receive(byte[] array) {
		try {
			if (array[0] == ClientSideWebSocketTerminal.DATA) {
				dataIn(array, 1, array.length - 1);
			}
			else if (array[0] == ClientSideWebSocketTerminal.INIT) {
				String spec = new String(array, 1, array.length - 1);
				String[] arr = spec.split(",");
				String term = arr[0];
				int width = Integer.parseInt(arr[1]);
				int height = Integer.parseInt(arr[2]);
				inited = true;
				if (error != null) {
					error.printStackTrace(new PrintStream(new CRLFOutputStream(getOutputStream()), true));
				} else {
					send(WebSocketTerminal.INIT, new byte[0]);
					init(term, width, height);
				}
			} else if (array[0] == ClientSideWebSocketTerminal.SCREEN_SIZE) {
				String spec = new String(array, 1, array.length - 1);
				String[] arr = spec.split(",");
				int width = Integer.parseInt(arr[0]);
				int height = Integer.parseInt(arr[1]);
				terminal.setScreenSize(width, height, false);
				changeTerminalDimensions(width, height);
			}
		} catch (Exception e) {
			log.error("Failed to process incoming frame.", e);
		}

	}
	
	public void sendData(byte[] data, int off, int len) throws IOException {
		
		synchronized (sendLock) {
			byte[] array = new byte[len + 1];
			array[0] =  ClientSideWebSocketTerminal.DATA;
			System.arraycopy(data, 0, array, 1,len);
			websocket.sendBinaryMessage(ByteBuffer.wrap(array));
		}
	}
	
	@Override
	public void send(byte code, byte[] data) throws IOException {
		if(!inited) {
			throw new IllegalStateException("Not yet inited.");
		}
//		if (code == ClientSideWebSocketTerminal.DATA) {
//			dataBlockOut(data);
//		}
//		else {
			byte[] msg = new byte[data.length + 1];
			msg[0] = code;
			System.arraycopy(data, 0, msg, 1, data.length);
			synchronized (sendLock) {
				websocket.sendBinaryMessage(msg);
			}
//		}
	}

	protected abstract void init(String term, int width, int height) throws IOException;

//	protected void dataBlockOut(byte[] buf) {
//		dataBlockOut(buf, 0, buf.length);
//	}
//
//	protected void dataBlockOut(byte[] buf, int off, int len) {
//		try {
//			int rem = len;
//			byte[] b;
//			/*
//			 * Split up the data into smaller chunks that will fit inside the
//			 * buffer If the buffer is full, the thread will wait until it has
//			 * been sent back over the websocket and the buffer emptied.
//			 */
//			while (rem > 0) {
//				int sz = Math.min(buffer.limit(), rem);
//				b = new byte[sz];
//				System.arraycopy(buf, off + (len - rem), b, 0, sz);
//				dataFrameOut(b, 0, sz);
//				rem -= sz;
//			}
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//	}

//	protected void dataFrameOut(byte[] buf, int off, int len) {
//		
//		try {
//			synchronized (wait) {
//				if (len > buffer.remaining()) {
//					wait.wait();
//				}
//			}
//		} catch (InterruptedException ie) {
//			throw new RuntimeException(ie);
//		}
//		
//		if (lastTask != null && !lastTask.isDone()) {
//			/*
//			 * Cancel the queued task, it hasn't run yet and we will be queueing
//			 * another
//			 */
//			lastTask.cancel(false);
//		}
//		buffer.put(buf, off, len);
//		lastTask = dispatcher.schedule(new Runnable() {
//			private long lastSend;
//
//			@Override
//			public void run() {
//				long now = System.currentTimeMillis();
//				if (now < lastSend + sendWait) {
//					try {
//						Thread.sleep(lastSend + sendWait - now);
//					} catch (InterruptedException e) {
//					}
//				}
//				ByteBuffer wrappedBuffer;
//				synchronized (wait) {
//					int remain = buffer.position();
//					byte[] b2 = new byte[remain + 1];
//					b2[0] = 'D';
//					System.arraycopy(buffer.array(), 0, b2, 1, remain);
//					buffer.clear();
//					wrappedBuffer = ByteBuffer.wrap(b2, 0, b2.length);
//					// Wake up and any threads waiting for the buffer to empty
//					wait.notifyAll();
//				}
//
//				/* Actually send the packet */
//				try {
//					websocket.sendBinaryMessage(wrappedBuffer);
//					lastSend = System.currentTimeMillis();
//				} catch (IOException e) {
//					log.error("Failed to send websocket binary packet", e);
//					close();
//				}
//				
//
//			}
//		}, sendDelay, TimeUnit.MILLISECONDS);
//	}
	
	protected abstract void onOpen() throws Exception;

	protected abstract void changeTerminalDimensions(int width, int height) throws IOException;

	static class CRLFOutputStream extends FilterOutputStream {
		protected int lastb = -1;
		protected boolean atBOL = true; // at beginning of line?
		private static final byte[] newline = { (byte) '\r', (byte) '\n' };

		public CRLFOutputStream(OutputStream os) {
			super(os);
		}

		public void write(int b) throws IOException {
			if (b == '\r') {
				writeln();
			} else if (b == '\n') {
				if (lastb != '\r')
					writeln();
			} else {
				out.write(b);
				atBOL = false;
			}
			lastb = b;
		}

		public void write(byte b[]) throws IOException {
			write(b, 0, b.length);
		}

		public void write(byte b[], int off, int len) throws IOException {
			int start = off;

			len += off;
			for (int i = start; i < len; i++) {
				if (b[i] == '\r') {
					out.write(b, start, i - start);
					writeln();
					start = i + 1;
				} else if (b[i] == '\n') {
					if (lastb != '\r') {
						out.write(b, start, i - start);
						writeln();
					}
					start = i + 1;
				}
				lastb = b[i];
			}
			if ((len - start) > 0) {
				out.write(b, start, len - start);
				atBOL = false;
			}
		}

		/*
		 * Just write out a new line, something similar to out.println()
		 */
		public void writeln() throws IOException {
			out.write(newline);
			atBOL = true;
		}
	}
}
