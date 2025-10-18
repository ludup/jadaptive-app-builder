package com.jadaptive.api.ui.pages.ext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.jadaptive.api.product.ProductService.ImageURIFormat;

/**
 * Utilities for dealing with Base64 encoded image URIs (parsing and converting
 * from class loader resources).
 */
@Component
public class Base64Images {

	public record Base64ImageResource(String contentType, long size, String extension, byte[] data) {
	}

	private final Map<String, String> b64ImageCache = new HashMap<>();

	/**
	 * Convert a resource path into a Base64 encoded image, then decode that for
	 * transmission.
	 * 
	 * @param clazz class (and so class loader) to derive resource from if it needs
	 *              to be converted
	 * @param value image path
	 * @return image resource data
	 */
	public Base64ImageResource decode(Class<?> clazz, String value) {
		return decodeBase64URI(encodeToString(ImageURIFormat.BASE64_ENCODED, clazz, value)).get();
	}

	/**
	 * Decode a Base64 image URI and extract its data and content type.
	 * 
	 * @param base64Uri
	 * @return parsed image data or empty if URI cannot be parsed.
	 */
	public Optional<Base64ImageResource> decodeBase64URI(String base64Uri) {
		if (base64Uri.startsWith("data:")) {
			var idx = base64Uri.indexOf(',');
			var sl = base64Uri.substring(5, idx);
			var pl = base64Uri.substring(idx + 1);
			var args = sl.split(";");
			if (args.length > 1) {
				if (args[args.length - 1].equals("base64")) {
					var data = pl.substring(1);
					var type = args[0];
					var ext = type.split("/")[1];
					byte[] decoded = Base64.getDecoder().decode(data);
					return Optional.of(new Base64ImageResource(args[0], decoded.length, ext, decoded));
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Return an image URI in the preferred format, converting if required. If the
	 * existing URI is in base64 encoded format, it will only be returned if that is
	 * the request format. If the existing URI is a public image page (i.e. served
	 * by the web server) and the requested format is Base64, then convert it (and
	 * cache it for future use).
	 * 
	 * @param uriFormat preferred URI format
	 * @param clazz     class (and so class loader) to derive resource from if it
	 *                  needs to be converted
	 * @param value     image URI
	 * @return converted image URI
	 */
	public String encodeToString(ImageURIFormat uriFormat, Class<?> clazz, String value) {

		var b64img = decodeBase64URI(value);
		var isB64 = b64img.isPresent();
		var wantB64 = uriFormat == ImageURIFormat.BASE64_ENCODED;
		if (isB64 != wantB64) {
			if (wantB64) {
				var cacheKey = value + "-" + clazz.getName();
				var cachedB64 = b64ImageCache.get(cacheKey);
				if (cachedB64 == null) {
					if (value.startsWith("/app/content/")) {
						var res = clazz.getClassLoader().getResource("webapp/" + value.substring(13));
						if (res == null) {
							throw new IllegalArgumentException("Image resource does not exist. " + value);
						} else {
							try {
								var urlconx = res.openConnection();
								var ctype = urlconx.getContentType();
								if (ctype.equalsIgnoreCase("image/png")) {
									try (var bout = new ByteArrayOutputStream()) {
										try (var bin = urlconx.getInputStream()) {
											bin.transferTo(bout);
										}
										var b64 = "data:" + ctype + ";base64, "
												+ Base64.getEncoder().encodeToString(bout.toByteArray());

										b64ImageCache.put(cacheKey, b64);

										return b64;
									}
								} else {
									throw new IOException("Only PNG images are supported.");
								}
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						}
					} else {
						throw new IllegalArgumentException(
								"Image resource URI path must be /app/content/.... and actually exist in [resources]/webapp/....");
					}
				} else {
					return cachedB64;
				}
			} else {
				throw new IllegalArgumentException("Cannot yet convert Base64 encoded image to a public image URI.");
			}
		} else {
			return value;
		}
	}
}
