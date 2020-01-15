package com.jadaptive.plugins.ssh.vsftp;

import java.util.Collection;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.regions.Regions;
import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.sshtools.vfs.s3.provider.s3.S3FileSystemConfigBuilder;

@Service
public class VirtualFileServiceImpl implements VirtualFileService {

	static Logger log = LoggerFactory.getLogger(VirtualFileServiceImpl.class);
	
	@Autowired
	private AssignableObjectDatabase<VirtualFolder> repository;
	
	@Autowired
	private PermissionService permissionService;
	
	public Collection<VirtualFolder> getVirtualFolders() {
		return repository.getAssignedObjects(
				VirtualFolder.class, 
				permissionService.getCurrentUser());
	}

	
	public void mountS3(String name, String region, String accessKeyId, String secretAccessKey) {
		FileSystemOptions opts = new FileSystemOptions();
        try {
        	
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, 
            		new StaticUserAuthenticator(null, accessKeyId, secretAccessKey));
            
            S3FileSystemConfigBuilder.getInstance().setRegion(opts, 
            		Regions.fromName(region));
        } catch (FileSystemException e) {
            log.error(String.format("Failed to set credentials on %s", name));
        }
	}




	@Override
	public boolean checkMountExists(String mount, User user) {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean checkSupportedMountType(String type) {
		// TODO Auto-generated method stub
		return false;
	}
}
