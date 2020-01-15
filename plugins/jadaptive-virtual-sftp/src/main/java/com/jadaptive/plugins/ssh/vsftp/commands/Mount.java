package com.jadaptive.plugins.ssh.vsftp.commands;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.apache.commons.vfs2.VFS;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.plugins.ssh.vsftp.VirtualFileService;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Mount extends AbstractVFSCommand {

	@Autowired
	private VirtualFileService fileService; 
	
	public Mount() {
		super("mount", "VFS",  UsageHelper.build("mount [options] path destination",
				"-p, --permanent						Store this mount for future use",
				"-t, --type <name>						The type of file system to mount"), 
				"Mount virtual folders");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		boolean permanent = CliHelper.hasLongOption(args, "permanent")
							|| CliHelper.hasShortOption(args, 'p');
		
		String type = CliHelper.getLongValue(args, "type");
		if(Objects.isNull(type)) {
			type = CliHelper.getShortValue(args, 't');
		}
		
		if(Objects.isNull(type)) {
			type = "file";
		}
		
		String mount = args[args.length - 2];
		String path = args[args.length - 1];
		
		boolean supported = fileService.checkSupportedMountType(type);
		boolean exists = fileService.checkMountExists(mount, user);
		
		String credentials = promptForCredentials(type);
		
		String uri = type + "://" + credentials + path;
		
		console.println("Verifying destination folder");
		VFS.getManager().resolveFile(URI.create(uri));
		VirtualMountTemplate template = new VirtualMountTemplate(mount, path, new VFSFileFactory());
		
		
		getFileFactory().addMountTemplate(template);
		if(permanent) {
			saveMount();
		}
	}

	private String promptForCredentials(String type) {
		
		switch(type) {
		case "smb":
		
			break;
		}
		return "";
	}

	private void saveMount() {
		
		
	}
	
	

}
