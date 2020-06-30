package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.utils.FileUtils;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public abstract class AbstractRoleAssignmentCommand<T extends AssignableUUIDEntity> extends AbstractTenantAwareCommand {

	
	@Autowired
	private RoleService roleService;  

	public AbstractRoleAssignmentCommand(String name, String subsystem, String signature, String description) {
		super(name, subsystem, signature, description);
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length < 4) {
			throw new UsageException("Not enough arguments provided");
		}
		
		String name = FileUtils.checkStartsWithSlash(
				FileUtils.checkEndsWithNoSlash(args[1]));
		
		T obj = loadObject(name);
	
		
		Set<Role> roles = new HashSet<>();
		
		for(int i=2;i<args.length;i++) {
			roles.add(roleService.getRoleByName(args[i]));
		}
		
		for(Role role : roles) {
			obj.getRoles().add(role.getUuid());
		}

		try {
			
			saveObject(obj);
			
		} catch (ObjectException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	protected abstract void saveObject(T obj) throws IOException, UsageException;

	protected abstract T loadObject(String name) throws IOException, UsageException;

	protected abstract void loadCandidates(List<Candidate> candidates);
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		if(line.wordIndex() == 1) {
			loadCandidates(candidates);
		} else if(line.wordIndex() > 1) {
			for(Role role : roleService.allRoles()) {
				candidates.add(new Candidate(role.getName()));
			}
		}
	}

	

}
