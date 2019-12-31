package com.jadaptive.json.upload;

import java.io.IOException;
import java.io.InputStream;

public interface UploadHandler {

	void handleUpload(String handlerName, String uri, String filename, InputStream in) throws IOException;

	boolean isSessionRequired();
}
