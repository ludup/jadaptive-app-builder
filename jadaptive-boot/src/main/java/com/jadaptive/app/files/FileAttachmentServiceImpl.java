package com.jadaptive.app.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.db.SystemSingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.files.FileAttachmentStorage;
import com.jadaptive.api.files.FileStorageProvider;
import com.jadaptive.api.template.FieldTemplate;

@Service
public class FileAttachmentServiceImpl implements FileAttachmentService, StartupAware {

	@Autowired
	private SystemOnlyObjectDatabase<FileStorageProvider> providerDatabase;
	
	@Autowired
	private SystemSingletonObjectDatabase<FilesConfiguration> fileConfig;
	
	@Autowired
	private ApplicationService appService; 
	
	@Autowired
	private TenantAwareObjectDatabase<FileAttachment> attachmentDatabase;
	
	@Override
	public void registerProvider(String uuid, String name) {
		
		if(providerDatabase.count(FileStorageProvider.class, SearchField.eq("uuid", uuid)) == 0) {
			FileStorageProvider newProvider = new FileStorageProvider();
			newProvider.setUuid(uuid);
			newProvider.setName(name);
			
			providerDatabase.saveOrUpdate(newProvider);
		}
	}

	@Override
	public void onApplicationStartup() {
		registerProvider(LocalFileAttachmentStorage.UUID, "Local Storage");
	}
	
	
	@Override
	public FileAttachment createAttachment(InputStream in, String filename, String contentType, String formVariable) throws IOException {
		
		FileAttachmentStorage provider = getProvider();
		return provider.createAttachment(in, filename, contentType, formVariable);
	}
	
	@Override
	public InputStream getAttachmentContent(String uuid) throws IOException {
		
		FileAttachment attachment = attachmentDatabase.get(uuid, FileAttachment.class);
		return getProvider(attachment.getProvider().getUuid()).getAttachmentContent(uuid);
	}
	
	private FileAttachmentStorage getProvider(String uuid) {
		for(FileAttachmentStorage storage : appService.getBeans(FileAttachmentStorage.class)) {
			if(storage.getUuid().equals(uuid)) {
				return storage;
			}
		}
		throw new IllegalStateException("No storage provider for uuid " + uuid);
	}
	
	private FileAttachmentStorage getProvider() {
		FileStorageProvider provider = fileConfig.getObject(FilesConfiguration.class).getStorageProvider();
		if(Objects.isNull(provider)) {
			return appService.getBean(LocalFileAttachmentStorage.class);
		}
		for(FileAttachmentStorage storage : appService.getBeans(FileAttachmentStorage.class)) {
			if(storage.getUuid().equals(provider.getUuid())) {
				return storage;
			}
		}
		return appService.getBean(LocalFileAttachmentStorage.class);
	}

}
