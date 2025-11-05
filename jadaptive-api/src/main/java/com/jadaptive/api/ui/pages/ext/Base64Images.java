package com.jadaptive.api.ui.pages.ext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.product.ProductService.ImageURIFormat;

/**
 * Utilities for dealing with Base64 encoded image URIs (parsing and converting
 * from class loader resources).
 */
@Component
public class Base64Images {
	
	@Autowired
	private ClassLoaderService classLoaderService;

	public record Base64ImageResource(String contentType, long size, String extension, byte[] data) {
	}

	private final Map<String, String> b64ImageCache = new HashMap<>();
	
	public  static void main(String[] args) {
		var bims = new Base64Images();
		var res = bims.decode("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAALgAAAAoCAYAAABNVTCEAAAAzXpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjabVHbEQIhDPxPFZYAWR6hHM47Z+zA8g0kp6IuQ17LLCHQ8bjf6DLAEEq5SmmlBEVqqXHXQIKhTxtDmnZiO7m41olPgrUE9bBUip8/6/ElYK5rlD+E5OrEthItub58CbE5jI5GvLtQcyGwEdEFuj0rlCZ1edoRVohtGibJ2vZPXnV6e9Z7wHwgIqgFijWAsTOhawC1jKYHx+qa11nJPjMdyL85hfEz3i0tP4E34fNZQE9ismMZusCGKQAAAYRpQ0NQSUNDIHByb2ZpbGUAAHicfZE9SMNAHMVfW6UiFQeLiHTIUJ3s4AfFsVahCBVCrdCqg8mlX9CkIUlxcRRcCw5+LFYdXJx1dXAVBMEPEHfBSdFFSvxfUmgR48FxP97de9y9A/zNKlPNngSgapaRSSWFXH5VCL4igAiGMYm4xEx9ThTT8Bxf9/Dx9S7Gs7zP/TkGlILJAJ9AnGC6YRFvEMc3LZ3zPnGYlSWF+Jx4wqALEj9yXXb5jXPJYT/PDBvZzDxxmFgodbHcxaxsqMQzxFFF1Sjfn3NZ4bzFWa3WWfue/IWhgrayzHWaEaSwiCWIECCjjgqqsBCjVSPFRIb2kx7+UccvkksmVwWMHAuoQYXk+MH/4He3ZnF6yk0KJYHeF9v+GAOCu0CrYdvfx7bdOgECz8CV1vHXmsDsJ+mNjhY9Aga3gYvrjibvAZc7wMiTLhmSIwVo+otF4P2MvikPDN0C/Wtub+19nD4AWeoqfQMcHALjJcpe93h3X3dv/55p9/cDJedy7hC2gjEAAA14aVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIKICAgIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiCiAgICB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1lbnRzLzEuMS8iCiAgICB4bWxuczpHSU1QPSJodHRwOi8vd3d3LmdpbXAub3JnL3htcC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgeG1wTU06RG9jdW1lbnRJRD0iZ2ltcDpkb2NpZDpnaW1wOjJlMGI1NDIwLWY0NjUtNDNlMy04MTI1LTJhY2RmNjk0ZTRkYyIKICAgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDpmY2Q2ZTg2MC1mNzcyLTQ0YzQtODY3OC04MDQ4ZTdlY2M1NjciCiAgIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDplNzVjY2Y5Ny0yYzEwLTRiODktODI3MC04M2MyM2M1ZmFhN2IiCiAgIGRjOkZvcm1hdD0iaW1hZ2UvcG5nIgogICBHSU1QOkFQST0iMi4wIgogICBHSU1QOlBsYXRmb3JtPSJMaW51eCIKICAgR0lNUDpUaW1lU3RhbXA9IjE3NDMyMDg2NDU5OTY4OTYiCiAgIEdJTVA6VmVyc2lvbj0iMi4xMC4zNiIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIKICAgeG1wOkNyZWF0b3JUb29sPSJHSU1QIDIuMTAiCiAgIHhtcDpNZXRhZGF0YURhdGU9IjIwMjU6MDM6MjlUMDA6Mzc6MjUrMDA6MDAiCiAgIHhtcDpNb2RpZnlEYXRlPSIyMDI1OjAzOjI5VDAwOjM3OjI1KzAwOjAwIj4KICAgPHhtcE1NOkhpc3Rvcnk+CiAgICA8cmRmOlNlcT4KICAgICA8cmRmOmxpCiAgICAgIHN0RXZ0OmFjdGlvbj0ic2F2ZWQiCiAgICAgIHN0RXZ0OmNoYW5nZWQ9Ii8iCiAgICAgIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6NGY3MWMyNzktODljNi00Yjg5LTkzZjktYzEzNzRhNDcyN2I1IgogICAgICBzdEV2dDpzb2Z0d2FyZUFnZW50PSJHaW1wIDIuMTAgKExpbnV4KSIKICAgICAgc3RFdnQ6d2hlbj0iMjAyNS0wMy0yOVQwMDozNzoyNSswMDowMCIvPgogICAgPC9yZGY6U2VxPgogICA8L3htcE1NOkhpc3Rvcnk+CiAgPC9yZGY6RGVzY3JpcHRpb24+CiA8L3JkZjpSREY+CjwveDp4bXBtZXRhPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgCjw/eHBhY2tldCBlbmQ9InciPz6bg9RyAAAABmJLR0QA/wD/AACNv0geAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6QMdACUZlNRtXwAAEVlJREFUeNrtXGt0k1W6ft6dFGibliK0RWiTlnJTuciRUcBeUgEv4BnU47iWiEc9I6i9gKAeZRQRXTMqiAM0LTA46oCjDmcdkYtapG2SFm/DxSMitVybUMA2xRaapKXJt9/zg6b9eqN3tEyetfbqSrLv+/n2ft53v18BP/zwww8//PDDDz/88MMPP/zoJtCVMIhMi+F5Jn4GQKGU9Mj8ZNuPnanHZI6OJyEKfJ8leG56kv2tS5VZaxk6QiHtfxD4NmYMAxBBRP2a5mPmGgBlANmY+HMSvCUt4eQPl6p7tXlolFZoT3ZoEAwJYgckfgJRgSTakZZY/DkRuCtznGHVP0rAG5BUAkkz06YW2zpTT9MxMeOTNKPtzp7ihujt5M76YlgEgJcJ1J9AkwShIDM/dlxPt7vaPDTKZDFsktD+SMCrABmJSN8SuQGA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");
		
		
	}

	/**
	 * Convert a resource path into a Base64 encoded image, then decode that for
	 * transmission.
	 * 
	 * @param value image path
	 * @return image resource data
	 */
	public Base64ImageResource decode(String value) {
		return decodeBase64URI(encodeToString(ImageURIFormat.BASE64_ENCODED, value)).get();
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
					var data = pl.trim();
					var type = args[0].trim();
					var ext = (type.split("/")[1]).trim();
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
	 * @param value     image URI
	 * @return converted image URI
	 */
	public String encodeToString(ImageURIFormat uriFormat, String value) {

		var b64img = decodeBase64URI(value);
		var isB64 = b64img.isPresent();
		var wantB64 = uriFormat == ImageURIFormat.BASE64_ENCODED;
		if (isB64 != wantB64) {
			if (wantB64) {
				var cacheKey = value + "-" + uriFormat.name();
				var cachedB64 = b64ImageCache.get(cacheKey);
				if (cachedB64 == null) {
					if (value.startsWith("/app/content/")) {
						var res = classLoaderService.getResource("webapp/" + value.substring(13));
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
