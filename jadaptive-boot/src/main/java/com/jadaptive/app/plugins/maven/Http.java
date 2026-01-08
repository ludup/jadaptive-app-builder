package com.jadaptive.app.plugins.maven;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Builder;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public class Http {
	
	private final static class LazyHttpClientFactory {
		private final static HttpClientFactory DEFAULT = new HttpClientFactory() {
			
			@Override
			public Builder get() {
				return createDefaultHttpClientBuilder();
			}
		};
	}
	
	public interface HttpClientFactory extends Supplier<HttpClient.Builder> {
		
	}
	
	public static Optional<Long> contentLength(HttpResponse<?> response) {
		var length = response.headers().firstValueAsLong("Content-Length");
		return length.isEmpty() ? Optional.empty() : Optional.of(length.getAsLong());
	}
	
	public static HttpClientFactory defaultClientFactory() {
		return LazyHttpClientFactory.DEFAULT;
	}

	public static HttpClient.Builder createDefaultHttpClientBuilder() {
		return HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10));
	}

	public static String urlEncode(String val) {
		try {
			return URLEncoder.encode(val, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
