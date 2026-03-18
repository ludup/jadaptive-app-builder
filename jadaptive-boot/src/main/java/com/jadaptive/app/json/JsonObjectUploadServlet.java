package com.jadaptive.app.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.ValidationRequestImpl;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.pages.ext.ValidationHelper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Implementation of {@link AbstractObjectUploadServlet} that handles form posts
 * via JSON posts. Validation is performed in a separate call and any responses
 * stuffed returns as JSON for the client side script to process and display
 * appropriate error messages. Redirects are also handled by the JSON responses,
 * but redirects only occur when there are no validation (or other errors), and
 * the user is to be redirected back to the parent resource list page. In the
 * case of validation errors, the client side script is solely responsible for
 * display the error messages. It can if it it wishs and has the facility,
 * highlight which fields are in error, but this is not a requirement.
 * <p>
 * <ul>
 * <li>The response must always be JSON</li>
 * <li>The response must always be a 200</li>
 * <li>Server side feedback messages and redirects only on success</li>
 * <li>The server decides on the redirect URL, the client should not attempt to
 * determine this, simply perform the redirect</li>
 * </ul>
 */
@WebServlet(name="objectServlet", description="Servlet for handing objects via JSON posts", 
urlPatterns = { "/app/api/form/json-multipart/*", 
		"/app/api/form/json-validate/*",
		"/app/api/form/json-stash/*",
		"/app/api/form/json-stash-child/*"})
public class JsonObjectUploadServlet extends AbstractObjectUploadServlet {


	private static final long serialVersionUID = -9186281630952818057L;
	
	private  ObjectMapper json = new ObjectMapper();

	protected JsonObjectUploadServlet() {
		super("json-");
	}

	@Override
	protected String processHandler(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String handler, String resourceKey, Map<String, String[]> parameters, String uuid) throws IOException {
		/* We only need to collect feedback messages for validation with JSON, other handlers will just use error
		 * messages.
		 */
		switch(handler) {
		case VALIDATE:
			ValidationHelper.enableMultipleValidation();
			try {
				return super.processHandler(request, resp, paths, handler, resourceKey, parameters, uuid);
			}
			finally {
				ValidationHelper.disableMultipleValidation();
			}
		default:
			return super.processHandler(request, resp, paths, handler, resourceKey, parameters, uuid);
		}
	}
	
	@Override
	protected void doHandlePost(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String handler, String resourceKey, Map<String, String[]> parameters) throws IOException {
		try {

			String uuid = "";
			resp.setStatus(200);
			resp.setContentType("application/json");
			
			uuid = processHandler(request, resp, paths, handler, resourceKey, parameters, uuid);
			
			if(ValidationHelper.hasErrors()) {
				json.writer().writeValue(resp.getOutputStream(), new ValidationRequestImpl(false,
						I18N.getResource("userInterface", "multipleErrors.text", ValidationHelper.getErrors().size()),
						ValidationHelper.getErrors()));
			}
			else {
				/* All is good, instruct client to redirect to the parent resource list page. */
				ObjectTemplate template = templateService.get(resourceKey);
				if(template.hasParent()) {
					json.writer().writeValue(resp.getOutputStream(), new RedirectStatus(String.format("/app/ui/search/%s", template.getParentTemplate())));
				} else {
					json.writer().writeValue(resp.getOutputStream(), new RedirectStatus(String.format("/app/ui/search/%s", template.getCollectionKey())));
				}		
			}
			
			return;
			
		} catch(ValidationException ex) { 
			json.writer().writeValue(resp.getOutputStream(), new RequestStatusImpl(false, ex.getMessage()));
		} catch (Redirect e) {
			json.writer().writeValue(resp.getOutputStream(), new RedirectStatus(e.getUri()));
		} catch (ObjectException e) {
			json.writer().writeValue(resp.getOutputStream(), new RequestStatusImpl(false, e.getMessage()));
		} catch (Throwable e) {
			json.writer().writeValue(resp.getOutputStream(), new RequestStatusImpl(false, Objects.toString(e.getMessage(), "Unknown error!")));
		}
	}

}
