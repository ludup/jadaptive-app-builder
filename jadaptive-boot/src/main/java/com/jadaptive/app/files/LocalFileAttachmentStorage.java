package com.jadaptive.app.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentStorage;
import com.jadaptive.api.files.FileStorageProvider;

@Extension
public class LocalFileAttachmentStorage implements FileAttachmentStorage {

	public static final String UUID = "b908313d-be99-446c-966e-89107b3901ca";
	
	public static final File LOCATION = new File(ApplicationProperties.getConfdFolder(), "attachments");
	
	@Autowired
	private SystemOnlyObjectDatabase<FileStorageProvider> providerDatabase;
	
	@Autowired
	private TenantAwareObjectDatabase<FileAttachment> attachmentDatabase;
	static {
		LOCATION.mkdirs();
	}
	
	@Override
	public String getUuid() {
		return UUID;
	}

	@Override
	public InputStream getAttachmentContent(String attachmentUUID) throws FileNotFoundException {
		return new FileInputStream(new File(LOCATION, attachmentUUID));
	}

	@Override
	public long getMaximumSize() {
		return 1024000;
	}

	@Override
	public FileAttachment createAttachment(InputStream in, String filename, String contentType) throws IOException {
		
		String uuid = java.util.UUID.randomUUID().toString();
		FileAttachment attachment = new FileAttachment();
		attachment.setUuid(uuid);
		attachment.setFilename(filename);
		attachment.setProvider(providerDatabase.get(UUID, FileStorageProvider.class));
		attachment.setContentType(contentType);
		
		File file = new File(LOCATION, uuid);
		try(FileOutputStream fout = new FileOutputStream(file)) {
			try(DigestOutputStream out = new DigestOutputStream(fout, MessageDigest.getInstance("MD5"))) {
				attachment.setSize(IOUtils.copy(in, out, 65535));
				attachment.setHash(HexUtils.toHexString(out.getMessageDigest().digest()));
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		attachmentDatabase.saveOrUpdate(attachment);
		IOUtils.closeQuietly(in);
		
		return attachment;
	}

}
