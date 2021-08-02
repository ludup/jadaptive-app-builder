package com.jadaptive.api.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.template.ObjectTemplate;

public class BootstrapTableController<T> extends AuthenticatedController {

	Logger log = LoggerFactory.getLogger(BootstrapTableController.class);

	@SuppressWarnings("unchecked")
	protected BootstrapTableResult<T> processDataTablesRequest(
			HttpServletRequest request, 
			ObjectTemplate template,
			BootstrapTablePageProcessor processor)
			throws UnauthorizedException,
			AccessDeniedException {

		Integer start = 0;
		if(request.getParameter("offset") != null) {
			start = Integer.parseInt(request.getParameter("offset"));
		}
		
		Integer length = 0;
		if(request.getParameter("limit") != null) {
			length = Integer.parseInt(request.getParameter("limit"));
		}

		/**
		 * TODO sorting and direction
		 */

		String searchPattern = "";
		String searchColumn = template.getNameField();
		
		if(request.getParameter("search") != null) {
			searchPattern = request.getParameter("search");
		} 
		
		if(request.getParameter("searchField") != null) {
			searchColumn = request.getParameter("searchField");
		}

		BootstrapTableResult<T> result = new BootstrapTableResult<T>(processor.getPage(
				searchColumn,
				searchPattern, 
				start,
				length, 
				""),
				processor.getTotalCount(searchColumn, searchPattern)/*, template*/);

		if(processor instanceof BootstrapTableResourceProcessor) {
			try {
				result.setResource(((BootstrapTableResourceProcessor<T>)processor).getResource());
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected BootstrapTableResult<T> processDataTablesRequest(
			HttpServletRequest request, 
			BootstrapTablePageProcessor processor)
			throws UnauthorizedException,
			AccessDeniedException {

		Integer start = 0;
		if(request.getParameter("offset") != null) {
			start = Integer.parseInt(request.getParameter("offset"));
		}
		
		Integer length = 0;
		if(request.getParameter("limit") != null) {
			length = Integer.parseInt(request.getParameter("limit"));
		}

		/**
		 * TODO sorting and direction
		 */

		String searchPattern = "";
		String searchColumn = "name";
		
		if(request.getParameter("search") != null) {
			searchPattern = request.getParameter("search");
		} 
		
		if(request.getParameter("searchField") != null) {
			searchColumn = request.getParameter("searchField");
		}

		BootstrapTableResult<T> result = new BootstrapTableResult<T>(processor.getPage(
				searchColumn,
				searchPattern, 
				start,
				length, 
				""),
				processor.getTotalCount(searchColumn, searchPattern));

		if(processor instanceof BootstrapTableResourceProcessor) {
			try {
				result.setResource(((BootstrapTableResourceProcessor<T>)processor).getResource());
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return result;
	}
}
