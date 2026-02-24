package com.jadaptive.app.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.db.SystemSingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.files.FileAttachmentStorage;
import com.jadaptive.api.files.FileStorageProvider;

@Service
public class FileAttachmentServiceImpl implements FileAttachmentService, StartupAware {

	static Logger log = LoggerFactory.getLogger(FileAttachmentServiceImpl.class);
	
	@Autowired
	private SystemOnlyObjectDatabase<FileStorageProvider> providerDatabase;
	
	@Autowired
	private SystemSingletonObjectDatabase<FilesConfiguration> fileConfig;
	
	@Autowired
	private App appService; 
	
	@Autowired
	private TenantAwareObjectDatabase<FileAttachment> attachmentDatabase;
	
	@Autowired
	private EventService eventService;
	
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
		
		for(FileAttachmentStorage provider : App.beans(FileAttachmentStorage.class)) {
			registerProvider(provider.getUuid(), provider.getName());
		}
		
		eventService.deleting(FileAttachment.class, (e)->{
			FileAttachmentStorage provider = getProvider();
			if(e.getObject().getProvider().getUuid().equals(provider.getUuid())) {
				try {
					provider.deleteAttachment(e.getObject());
				} catch (IOException e1) {
					log.error("Unable to delete attachment content for " + e.getObject().getUuid(), e1);
				}
			}
		});
	}
	
	@Override
	public OutputStream getOutputStream(String path, String contentType) throws IOException {
		FileAttachmentStorage provider = getProvider();
		return provider.getOutputStream(path, contentType);
	}
	
	@Override
	public InputStream getInputStream(String path) throws IOException {
		FileAttachmentStorage provider = getProvider();
		return provider.getInputstream(path);
	}
	
	@Override
	public FileAttachment createAttachment(InputStream in, String filename, String contentType, String formVariable, String template) throws IOException {
		
		FileAttachmentStorage provider = getProvider();
		return provider.createAttachment(in, filename, contentType, formVariable, template);
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

	@Override
	public FileAttachment getAttachment(String uuid) {
		return attachmentDatabase.get(uuid, FileAttachment.class);
	}

	@Override
	public void markForRemoval(Collection<FileAttachment> attachments) {
		
		for(FileAttachment attach : attachments) {
			attachmentDatabase.delete(attach);
		}
		
	}

	@Override
	public void markForRemoval(String uuid) {
		attachmentDatabase.delete(getAttachment(uuid));
	}

	@Override
	public void deleteAttachment(FileAttachment attachment) throws IOException {
		attachmentDatabase.delete(attachment);
	}

}
