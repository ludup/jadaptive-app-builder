package com.jadaptive.plugins.keys;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Bound;
import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.FileUpload;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bindable.FormBindable.FormField;
import com.codesmith.webbits.bindable.FormBindable.InputRestriction;
import com.codesmith.webbits.widgets.Feedback;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.publickey.SshPublicKeyFile;
import com.sshtools.common.publickey.SshPublicKeyFileFactory;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.common.util.IOUtils;

@Page
@View(contentType = "text/html", paths = { "uploadPublicKey", "uploadPublicKey/{uuid}" })
@ClasspathResource
public class PublicKeyUpload extends AuthenticatedPage {

	static Logger log = LoggerFactory.getLogger(PublicKeyUploadHandler.class);

	public interface UploadForm {
		@InputRestriction(required = true)
		@FormField
		FileUpload getFile();

		@FormField
		@InputRestriction(required = true)
		String getName();

		@FormField
		String getPassphrase();

		Feedback getFeedback();
	}

	String uuid;

	@Autowired
	private PublicKeyUploadHandler handler;

	@Bound
	private UploadForm uploadForm;

	public String getUuid() {
		return uuid;
	}

	@Bound
	public void uploadForm() throws IOException, InvalidPassphraseException, SessionTimeoutException, UnauthorizedException {

		String contents = IOUtils.readUTF8StringFromStream(uploadForm.getFile().in());

		SshPublicKey key;
		String name = uploadForm.getName();
		try {
			SshPublicKeyFile kfile = SshPublicKeyFileFactory.parse(IOUtils.toInputStream(contents, "UTF-8"));
			key = kfile.toPublicKey();
			if (StringUtils.isBlank(name)) {
				name = kfile.getComment();
				if (StringUtils.isBlank(name)) {
					name = "";
				}
			}
		} catch (IOException e) {

			SshPrivateKeyFile kfile = SshPrivateKeyFileFactory.parse(IOUtils.toInputStream(contents, "UTF-8"));
			key = kfile.toKeyPair(uploadForm.getPassphrase()).getPublicKey();
			if (StringUtils.isBlank(name)) {
				name = kfile.getComment();
				if (StringUtils.isBlank(name)) {
					name = "";
				}
			}
		}

		if (StringUtils.isBlank(name)) {
			name = "Uploaded by " + getCurrentUser().getUsername() + " #" + System.currentTimeMillis();
		}

		try {
			handler.handleUpload(getCurrentUser(),  name, key);
		}
		catch(Exception e) {
			log.error("Failed to import key.", e);
			uploadForm.getFeedback().error(e.getMessage());
		}

		throw new Redirect("/table/authorizedKeys");

	}

	@Out(methods = HTTPMethod.GET)
	public Document serviceAnonymous(@In Document contents) throws IOException {

		if (StringUtils.isBlank(uuid)) {
			uuid = getCurrentUser().getUuid();
		}

		contents.selectFirst("form").prepend("<input type=\"hidden\" name=\"uuid\" value=\"" + uuid + "\">");
		return contents;
	}
}
