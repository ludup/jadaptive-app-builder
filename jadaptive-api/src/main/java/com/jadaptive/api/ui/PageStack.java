package com.jadaptive.api.ui;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.servlet.Request;

/**
 * Maintains a stack of pages that the user has visited. This is used to
 * determine the previous page when the user clicks the "Cancel" button, or when
 * we want to return to the previous page after a form submission.
 * <p>
 * Note, only "/app/ui/*" pages should be pushed onto the stack, and the URL
 * should be normalized to remove any query parameters and trailing slashes.
 * This is because we want to treat "/app/ui/page" and
 * "/app/ui/page?param=value" as the same page, and we want to treat
 * "/app/ui/page/" and "/app/ui/page" as the same page.
 */
public class PageStack {
	
	private static final Logger LOG = LoggerFactory.getLogger(PageStack.class);
	private Stack<String> stack = new Stack<>();

	public static PageStack get() {
		var session = Request.get().getSession();
		var stack = (PageStack)session.getAttribute(PageStack.class.getName());
		if(stack == null) {
			stack = new PageStack();
			session.setAttribute(PageStack.class.getName(), stack);
		}
		return stack;
		
	}

	private PageStack() { }
	
	/**
	 * Clear the stack. Generally used when the user logs out, or when we want to
	 * start a new session with a clean stack.
	 */
	public void clear() {
		stack.clear();
	}
	
	/**
	 * Push a page onto the stack. If the page is already in the stack, remove all
	 * pages above it before pushing it onto the stack. This is because we are
	 * returning to a previous page, not navigating to a new page.
	 * 
	 * @param url the URL of the page to push onto the stack
	 */
	public void push(String url) {
		var urlWithoutQuery = normalizeURL(stripQuery(url));
		
		/*
		 * When we return to a page, we want to remove all pages above it in the stack.
		 * This is because we are returning to a previous page, not navigating to a new
		 * page.
		 */
		var it = stack.iterator();
		var popped = 0;
		while(it.hasNext()) {
			var item = it.next();
			var itemWithoutQuery = normalizeURL(stripQuery(item));
			if(itemWithoutQuery.equals(urlWithoutQuery)) {
				it.remove();
				popped++;
				while(it.hasNext()) {
					it.next();
					it.remove();
					popped++;
				}
			}
		}
		
		stack.push(url);

		if(Boolean.getBoolean("jadaptive.development") || Boolean.getBoolean("jadaptive.showPageStack")) {
			LOG.info("REMOVEME: Pushed page onto stack: {}, Stack size: {}, Popped: {}", url, stack.size(), popped);
		}
	}

	/**
	 * Get the previous page in the stack. If there is only one page in the stack, return "/".
	 * Generally used for "Cancel" buttons to return to the previous page, or the root page if there is no previous page.
	 * 
	 * @return the previous page in the stack, or "/" if there is only one page in the stack
	 */
	public String previous() {
		return stack.size() > 1 ? stack.get(stack.size() - 2) : "/";
	}

	
	protected static String normalizeURL(String url) {
		while(url.endsWith("/") && !url.equals("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	protected static String stripQuery(String url) {
		return url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
	}
	
}
