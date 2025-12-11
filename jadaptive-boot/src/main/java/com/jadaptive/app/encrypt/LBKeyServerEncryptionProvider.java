package com.jadaptive.app.encrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.jgroups.util.UUID;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.encrypt.EncryptionProvider;
import com.jadaptive.utils.Instrumentation;

@Component
public class LBKeyServerEncryptionProvider implements EncryptionProvider {
	
	private char[] secret;
	private String uri;
	private String reference;

	@Override
	public int priority() {
		return 0;
	}

	@Override
	public void init() throws Exception {
		var host = ApplicationProperties.getValue("keyserver.host", "");
		if(host.equals("")) {
			throw new IllegalArgumentException("No keyserver hostname configured (keyserver.host).");
		}
		var port = ApplicationProperties.getValue("keyserver.port", 443);
		var path = ApplicationProperties.getValue("keyserver.path", "/ks/api/secrets");
		
		uri = String.format("https://%s:%d%s", host, port, path);

		reference = ApplicationProperties.getValue("keyserver.reference", "");
		if(reference.equals("")) {
			throw new IllegalStateException("A unique UUID must be set in the `keyserver.reference` system property. This UUID must be the same on members of this cluster. I will generate a UUID you can use ....\n\nkeyserver.reference=" + UUID.randomUUID().toString());
		}
		secret = ApplicationProperties.getValue("keyserver.secret", "").toCharArray();
		if(secret.length == 0) {
			throw new IllegalStateException("No keyserver secret configured (keyserver.secret).");
		}
	}

	@Override
	public String encrypt(String toEncrypt) throws Exception {
		return Instrumentation.call("encrypt()", () -> {
			return doHttp(toEncrypt, "encrypt");
		});
	}

	private String doHttp(String data, String op) throws UnsupportedEncodingException, IOException, InterruptedException {
		var form = String.format("secret=%s&ref=%s&data=%s",
				URLEncoder.encode(new String(secret), "UTF-8"),
				URLEncoder.encode(new String(reference), "UTF-8"),
				URLEncoder.encode(data, "UTF-8"));
		
		var uriStr = URI.create(uri + "/" + op);
		
		var request = HttpRequest.newBuilder()
				  .uri(uriStr)
				  .headers("Content-Type", "application/x-www-form-urlencoded")
				  .POST(HttpRequest.BodyPublishers.ofString(form))
				  .build();
		
		try(var httpClient = HttpClient
				  .newBuilder()
				  .build()) {
			
			var response = httpClient.send(request, BodyHandlers.ofString());
			
			if(response.statusCode() != 200) {
				throw new IOException("Unexpected response code " + response.statusCode() + ", cannot encrypt string. Please contact an administrator.");
			}
			return response.body();
		}
	}

	@Override
	public String decrypt(String encrypted) throws Exception {
		return Instrumentation.call("decrypt()", () -> {
			return doHttp(encrypted, "decrypt");
		});
	}

}
