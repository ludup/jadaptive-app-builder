package com.jadaptive.plugins.ssh.vsftp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.AssignableObjectDatabase;

@Service
public class VirtualFileServiceImpl implements VirtualFileService {

	@Autowired
	private AssignableObjectDatabase<VirtualFolder> repository;
	
	
}
