package com.jadaptive.plugins.term;

import java.io.IOException;

import com.jadaptive.api.app.WebSocketClient;
import com.sshtools.client.PseudoTerminalModes;
import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClient;
import com.sshtools.common.ssh.RequestFuture;
import com.sshtools.common.ssh.SshException;
import com.sshtools.terminal.screen.output.ScreenOutput;

public class SshTerminalConnection extends TerminalConnection {

	SshClientConnector connector;	
	String sessionId;
	SessionChannelNG session;
	SshClient ssh;
	
	public SshTerminalConnection(WebSocketClient socket, SshClientConnector connector, String sessionId) {
		super(socket);
		this.connector = connector;
		this.sessionId = sessionId;
	}
	
	@Override
	protected void onDataIn(byte[] array, int off, int len) throws IOException {
		session.getOutputStream().write(array, off, len);
	}

	@Override
	protected void init(String term, int cols, int rows) throws IOException {

		final ScreenOutput output = new ScreenOutput(terminal);
		output.printStringNewline("Connecting...");
		
		try {
			ssh = connector.connect(sessionId);
			session = ssh.openSessionChannel();
			session.getConnection().addTask(new Runnable() {
	
				@Override
				public void run() {
					
					try {
						
						output.printStringNewline("Allocating pty...");
						terminal.setScreenSize(cols, rows, false);
						
						PseudoTerminalModes pty =  new PseudoTerminalModes();
						
						pty.setTerminalMode(PseudoTerminalModes.VERASE, 127);
						pty.setTerminalMode(PseudoTerminalModes.TTY_OP_ISPEED, 14400);
						pty.setTerminalMode(PseudoTerminalModes.TTY_OP_OSPEED, 14400);

						RequestFuture f = session.allocatePseudoTerminal(term, cols, rows, pty);
						f.waitFor(10000);
						if(!f.isSuccess())  {
							throw new IOException("Timeout waiting for pty");
						}
						
						output.printStringNewline("Starting shell...");
						f = session.startShell();
						f.waitFor(10000);
						if(!f.isSuccess())  {
							throw new IOException("Timeout waiting for session open");
						}
						
						byte[] data = new byte[1024];
						int r;
						while((r = session.getInputStream().read(data)) > -1) {
							sendData(data, 0, r);
						}
						
					} catch(IOException | SshException e ) {
						log.error("I/O error received from SSH session", e);
					} finally {
						close();
						try {
							output.printStringNewline("Disconnected");
						} catch (IOException e) {
						}
					}
				}
				
			});
		
		} catch(IOException | SshException e) {
			output.printStringNewline("Connection failed");
			output.printStringNewline(e.getMessage());
		}
	}

	@Override
	protected void onOpen() throws Exception {
		
	}

	@Override
	protected void changeTerminalDimensions(int cols, int rows) throws IOException {
		session.changeTerminalDimensions(cols, rows, 0, 0);
	}

	@Override
	protected void onClose() {
		session.close();
		ssh.disconnect();
	}

}
