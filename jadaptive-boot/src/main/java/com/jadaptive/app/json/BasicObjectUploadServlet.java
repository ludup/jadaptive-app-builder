package com.jadaptive.app.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageStack;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.pages.ext.ValidationHelper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Implementation of {@link AbstractObjectUploadServlet} that handles basic form
 * posts. Validation is performed and any responses stuffed into the flash
 * message helper {@link Feedback}. The page will then be redirected either back
 * to the resource create or update page if validation fails, or back to the
 * resources list page if successful.
 * <p>
 * In short, <strong>the response must always be a redirect</strong> to ensure
 * the flash messages are correctly displayed and the user is returned to
 * correct flow. And, <strong>the client should avoid Javascript handling of the
 * submission</strong> for simplicity.
 */
@WebServlet(name="basicObjectServlet", description="Servlet for handing objects via basic form posts", 
urlPatterns = { "/app/api/form/multipart/*", 
		"/app/api/form/validate/*",
		"/app/api/form/stash/*",
		"/app/api/form/stash-child/*"})
public class BasicObjectUploadServlet extends AbstractObjectUploadServlet {

	protected BasicObjectUploadServlet() {
		super("");
	}

	private static final long serialVersionUID = -9186281630952818057L;

	@Override
	protected void doHandlePost(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String handler, String resourceKey, Map<String, String[]> parameters) throws IOException {
		try {
			String uuid = "";
			
			uuid = processHandler(request, resp, paths, handler, resourceKey, parameters, uuid);
			if(ValidationHelper.hasErrors()) {
				throw new ValidationException(I18N.getResource("userInterface", "multipleErrors.text", ValidationHelper.getErrors().size()));
			}

			ObjectTemplate template = templateService.get(resourceKey);
			if(template.hasParent()) {
				resp.sendRedirect(String.format("/app/ui/search/%s", template.getParentTemplate()));
				 //
			} else {
				if(template.getCollectionKey().equals(template.getResourceKey())) {
					resp.sendRedirect(PageStack.get().previous());
				}
				else {
					resp.sendRedirect(String.format("/app/ui/search/%s", template.getCollectionKey()));
				}
			}		

			return;
			
		} catch(ValidationException ex) { 
			Feedback.errorWithValidationErrors(ex.getMessage(), ValidationHelper.getErrors());
		} catch (Redirect e) {
			resp.sendRedirect(e.getUri());
			return;
		} catch (ObjectException e) {
			Feedback.error(e.getMessage());
		} catch (Throwable e) {
			Feedback.error(Objects.toString(e.getMessage(), "Unknown error!"));
		}
		
		/* For all cases other than completion we stay on the same page and display the error messages or success messages. */

		var uuid = parameters.get("uuid");
		if(uuid == null) {
			resp.sendRedirect(String.format("/app/ui/create/%s", resourceKey));
		}
		else {
			resp.sendRedirect(String.format("/app/ui/update/%s/%s", resourceKey, uuid[0]));
		}
	}

	@Override
	protected String processHandler(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String handler, String resourceKey, Map<String, String[]> parameters, String uuid) throws IOException {
		/* We always want to collect feedback messages for validation with basic post, as there is 
		 * only a single POST, not a separate validation step.
		 */
		ValidationHelper.enableMultipleValidation();
		try {
			return super.processHandler(request, resp, paths, handler, resourceKey, parameters, uuid);
		}
		finally {
			ValidationHelper.disableMultipleValidation();
		}
	}

	@Override
	protected String processStashedChildObject(HttpServletRequest request, String resourceKey, List<String> paths,
			Map<String, String[]> parameters) throws ValidationException, IOException {
		String res = super.processStashedChildObject(request, resourceKey, paths, parameters);

		String childResource = paths.get(5);
		String fieldName = paths.get(6);
		ObjectTemplate childTemplate = templateService.get(childResource);
		Feedback.info(childTemplate.getBundle(), fieldName + ".stashed");
		return res;
	}

}
