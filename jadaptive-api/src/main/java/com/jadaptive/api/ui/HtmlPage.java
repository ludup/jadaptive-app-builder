package com.jadaptive.api.ui;

import static com.jadaptive.utils.Instrumentation.timed;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.App;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class HtmlPage implements Page {
	
	static Logger log = LoggerFactory.getLogger(HtmlPage.class);
	
	@Autowired
	private PageCache pageCache; 
	
	@Autowired
	protected SessionUtils sessionUtils;
	
	@Autowired
	private HtmlContentService contentService; 
	
	private ThreadLocal<List<PageExtension>> extensions = new ThreadLocal<>();
	private ThreadLocal<List<HtmlPageExtender>> extenders = new ThreadLocal<>();
	private ThreadLocal<List<PageEnd>> pageEnd = ThreadLocal.withInitial(()->new ArrayList<>());
	
	protected String resourcePath;
	
	static ThreadLocal<Document> currentDocument = new ThreadLocal<>();
	
	public HtmlPage() {
		
	}
	
	public String getResourcePath() {
		return resourcePath;
	}
		
	public String getHtmlResource() {
		return String.format("%s.html", getClass().getSimpleName());
	}
	
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		
	}
	
	protected void afterProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		
	}
	
	public static boolean isProcessingDocument() {
		return currentDocument.get()!=null;
	}
	
	public static Document getCurrentDocument() {
		return currentDocument.get();
	}
	
	public final void created() throws FileNotFoundException {
		onCreated();
	}
	
	protected boolean isCacheable() { return false; }
	
	protected int getMaxAge() { return 3600; }
	
	public void onCreated() throws FileNotFoundException { }
	
	public final void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {	
		
		Document document = generateHTMLDocument(uri);
		
		if(!isCacheable()) {
			sessionUtils.setDoNotCache(response);
		} else {
			sessionUtils.setCachable(response, getMaxAge());
		}
		
		for(PageEnd end : pageEnd.get()) {
			end.finishPage(document, uri, this);
		}
		
		ResponseHelper.sendContent(document.toString(), "text/html;charset=UTF-8;", request, response);
	}

	@SuppressWarnings("unused")
	@Override
	public Document generateHTMLDocument(String uri) throws IOException {
		
		Document document = resolveDocument(this);
		currentDocument.set(document);
		setupExtenders(uri);
		
		try {
			
			try(var timed = timed("HtmlPage.generateHTMLDocument#beforeProcess(" + uri + ")")) {
				beforeProcess(uri, Request.get(), Request.response());
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#processPageDependencies(" + uri + ")")) {
				processPageDependencies(document);
			}

			var exts = extenders.get();
			if(Objects.nonNull(exts)) {
				try(var timed = timed("HtmlPage.generateHTMLDocument#extender.processStart(" + uri + ")")) {
					for(HtmlPageExtender ext : exts) {
						try(var timed2 = timed(ext.getClass().getName())) {
							ext.processStart(document, uri, this);
						}
					}
				}
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#generateContent(" + uri + ")")) {
				generateContent(document);
			}
			
			if(Objects.nonNull(exts)) {
				try(var timed = timed("HtmlPage.generateHTMLDocument#extender.generateContent(" + uri + ")")) {
					for(HtmlPageExtender ext : exts) {
						try(var timed2 = timed(ext.getClass().getName())) {
							ext.generateContent(document, this);
						}
					}
				}
			}
			
			if(Request.isAvailable()) {
				injectFeedback(document, Request.get());
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#processPageExtensions(" + uri + ")")) {
				processPageExtensions(uri, document);
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#documentComplete(" + uri + ")")) {
				documentComplete(document);
			}
			
			if(Objects.nonNull(exts)) {
				try(var timed = timed("HtmlPage.generateHTMLDocument#extender.processEnd(" + uri + ")")) {
					for(HtmlPageExtender ext : exts) {
						try(var timed2 = timed(ext.getClass().getName())) {
							ext.processEnd(document, uri, this);
						}
					}
				}
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#afterProcess(" + uri + ")")) {
				afterProcess(uri, Request.get(), Request.response());
			}
		
		} finally {
			currentDocument.remove();
		}
		return document;
	}

	private void setupExtenders(String uri) {
		
		extenders.set(new ArrayList<>());
		for(HtmlPageExtender ext : App.beans(HtmlPageExtender.class)) {
			if(ext.isExtending(this, uri)) {
				extenders.get().add(ext);
			}
		}
		
	}

	protected void documentComplete(Document document) throws FileNotFoundException, IOException { 
		PageHelper.appendHeadScript(document, "/app/content/jadaptive-session.js");
	};
	
	@SuppressWarnings("unused")
	private void processPageExtensions(String uri, Document document) throws IOException {
		

		try(var timed = timed("HtmlPage.processPageExtensions#processDocumentExtensions(" + uri + ")")) {
			processDocumentExtensions(document);
		}

		try(var timed = timed("HtmlPage.processPageExtensions#resolveScript(" + uri + ")")) {
			contentService.resolveScript(getUri(), document, this);
		}

		try(var timed = timed("HtmlPage.processPageExtensions#resolveStylesheet(" + uri + ")")) {
			contentService.resolveStylesheet(getUri(), document, this);
		}

		try(var timed = timed("HtmlPage.processPageExtensions#processPageProcessors(" + uri + ")")) {
			processPageProcessors(document);
		}
	}

	private void processPageDependencies(Document document) throws IOException {
		
		PageDependencies deps = getClass().getAnnotation(PageDependencies.class);
		if(Objects.nonNull(deps) && Objects.nonNull(deps.extensions())) {
			processPageLevelExtensions(document, deps.extensions());
		}
		
		afterPageDependencies(document);
		
	}
	
	protected void afterPageDependencies(Document document) {
		
	}
	
	private void processPageProcessors(Document document) throws IOException {
		
		PageProcessors deps = getClass().getAnnotation(PageProcessors.class);
		if(Objects.nonNull(deps) && Objects.nonNull(deps.extensions())) {
			processPageLevelExtensions(document, deps.extensions());
		}
		
	}

	public final void doPost(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {

			Document doc = resolveDocument(this);
			currentDocument.set(doc);
			setupExtenders(uri);
			
			beforeProcess(uri, request, response);
			
			processPageDependencies(doc);
			
			var exts = extenders.get();
			if(Objects.nonNull(exts)) {
				for(HtmlPageExtender ext : exts) {
					ext.processStart(doc, uri, this);
				}
			}
			
			if(this instanceof FormProcessor) {
				FormProcessor<?> fp = (FormProcessor<?>) this;
				
				Method m;
				
				try {
					m = ReflectionUtils.getMethod(getClass(), "processForm", Document.class, fp.getFormClass());
				} catch(NoSuchMethodException e) {
					m = ReflectionUtils.getMethod(getClass(), "processForm", Document.class, Object.class);
				}
				@SuppressWarnings("unused")
				Object formProxy = Proxy.newProxyInstance(
						  getClass().getClassLoader(), 
						  new Class<?>[] { fp.getFormClass() }, 
						  (proxy, method, methodArgs) -> {
						  
					if(method.getName().startsWith("get") && method.getName().length() > 3) {
						String name = method.getName().substring(3,4).toLowerCase();
						if(method.getName().length() > 4) {
							name += method.getName().substring(4);
						}
						
						if(method.getReturnType().isArray()) {
							String[] values = Request.get().getParameterValues(name);
							if(method.getReturnType().isAssignableFrom(int[].class)) {
								return values == null || values.length == 0 
										? new int[0] 
										: Arrays.stream(values)
					                       .mapToInt(Integer::parseInt)
					                       .toArray();
							} else if(method.getReturnType().isAssignableFrom(long.class)) {
								return values == null || values.length == 0 
										? new int[0] 
										: Arrays.stream(values)
					                       .mapToLong(Long::parseLong)
					                       .toArray();
							} else if(method.getReturnType().isAssignableFrom(boolean.class)) {
								if(values == null || values.length == 0) {
									return new boolean[0];
								} else {
									boolean[] boolArray = new boolean[values.length];

									for (int i = 0; i < values.length; i++) {
									    boolArray[i] = Boolean.parseBoolean(values[i]);
									}
									return boolArray;
								}
							} else {
								return values;
							}
							
						} else {
							String value = Request.get().getParameter(name);
							if(method.getReturnType().isAssignableFrom(int.class)) {
								return StringUtils.isBlank(value) ? 0 : Integer.parseInt(value);
							} else if(method.getReturnType().isAssignableFrom(long.class)) {
								return StringUtils.isBlank(value) ? 0 : Long.parseLong(value);
							} else if(method.getReturnType().isAssignableFrom(boolean.class)) {
								return StringUtils.isBlank(value) ? false : Boolean.parseBoolean(value);
							} else {
								return value;
							}
						}
					} else if(method.getName().startsWith("is") && method.getName().length() > 2) {
						String name = method.getName().substring(2,3).toLowerCase();
						if(method.getName().length() > 3) {
							name += method.getName().substring(3);
						}
						String value = Request.get().getParameter(name);
						if(Objects.equals("on", value)) {
							value = "true";
						}
						if(method.getReturnType().isAssignableFrom(boolean.class)) {
							return StringUtils.isBlank(value) ? false : Boolean.parseBoolean(value);
						} else {
							return value;
						}
					} else {
						throw new UnsupportedOperationException();
					}
				});
				
				beforeForm(doc, request, response);
				try {
					m.invoke(this, doc, formProxy);
				} catch(InvocationTargetException e) {
					if(e.getTargetException() instanceof Redirect) {
						throw (Redirect) e.getTargetException();
					}
					Feedback.error(e.getTargetException().getMessage());
				} 
				
			} else {
				processPost(doc, uri, request, response);
			}
			
			if(Objects.nonNull(exts)) {
				for(HtmlPageExtender extender : exts) {
					extender.processPost(doc, this);
				}
			}
			injectFeedback(doc, request);
			processPageExtensions(uri, doc);
			
			documentComplete(doc);
			
			if(Objects.nonNull(exts)) {
				for(HtmlPageExtender extender : exts) {
					extender.processEnd(doc, uri, this);
				}
			}

			afterProcess(uri, request, response);
			
			for(PageEnd end : pageEnd.get()) {
				end.finishPage(doc, uri, this);
			}
			
			ResponseHelper.sendContent(doc.toString(), "text/html; charset=UTF-8;", request, response);
			
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException e) {
			clearFeedback();
			if(e.getCause() instanceof Redirect) {
				throw (Redirect) e.getCause();
			}
			log.error("Failed to generate HTML page", e);
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			currentDocument.remove();
		}
	}
	
	protected void clearFeedback() {
		Feedback feedback = (Feedback) Request.get().getSession().getAttribute("feedback");
		if(Objects.nonNull(feedback)) {
			Request.get().getSession().removeAttribute("feedback");
		}
	}

	protected void beforeForm(Document doc, HttpServletRequest request, HttpServletResponse response) {
		
	}

	protected void injectFeedback(Document doc, HttpServletRequest request) {
		Feedback feedback = (Feedback) request.getSession().getAttribute("feedback");
		if(Objects.nonNull(feedback) && Objects.nonNull(feedback.getI18n())) {
			request.getSession().removeAttribute("feedback");
			Element element = doc.selectFirst("header");
			if(Objects.isNull(element)) {
					element = doc.selectFirst("body");
			} 
			
			var bdy = Html.div("toast-body");

			if(feedback.getIcon() != null) {
				bdy.appendChild(Html.i("fa-solid", feedback.getIcon(), "me-2"));
			}
			
			bdy.appendChild(getTextElement(feedback));
			
			var btn = Html.button("btn-close", "btn-close-white", "me-2", "m-auto");
			btn.dataset().put("bs-dismiss", "toast");
			btn.attr("aria-label", "Close");
			
			var fbox = Html.div("d-flex");
			fbox.appendChild(bdy);
			fbox.appendChild(btn);
			var toast = Html.div("toast", "align-items-center", "text-bg-" + feedback.getAlert(), "border-0", "show");
			toast.attr("role", "alert");
			toast.attr("aria-live", "assertive");
			toast.attr("aria-atomic", "true");
			toast.appendChild(fbox);
			
			var cnt = Html.div("toast-container", "p-3", "top-0", "start-50", "translate-middle-x");
			cnt.appendChild(toast);
			
			var out = Html.div("position-relative");
			out.appendChild(cnt);
			
			element.after(out);
			
		}
		
	}

	private Element getTextElement(Feedback feedback) {
		if(feedback.isRawText()) {
			return Html.span(feedback.getI18n());
		} else {
			return Html.i18n(feedback.getBundle(), feedback.getI18n(), feedback.getArgs());
		}
	}
	protected void processPost(Document document, String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		throw new FileNotFoundException();
	}

	private void processDocumentExtensions(Document document) throws IOException {
		Elements embeddedElement = document.getElementsByAttribute("jad:id");
		for(Element embedded : embeddedElement) {
			try(@SuppressWarnings("unused")
			var timed = timed("HtmlPage.processDocumentExtensions#embedded(" + embedded.attr("jad:id") + ")")) {
				processEmbeddedExtensions(document, embedded);
			}
		}
		
		afterDocumentExtensions(document);
	}
	
	protected void afterDocumentExtensions(Document document) throws IOException {	
		List<PageExtension> exts = this.extensions.get();
		
		if(Objects.nonNull(exts)) {
			for(PageExtension ext : exts) {
				ext.process(document, null, this);
			}
			
			this.extensions.get().clear();
		}
	}
	
	private void processChildExtensions(Document document, Element element) throws IOException {
		Elements embeddedElement = element.getElementsByAttribute("jad:id");
		for(Element embedded : embeddedElement) {
			if(element.equals(embedded)) {
				continue;
			}
 			processEmbeddedExtensions(document, embedded);
 			
		}
	}
	
	private void processEmbeddedExtensions(Document document, Element element) throws IOException {
			
			String name = element.attr("jad:id");
			PageExtension ext = pageCache.resolveExtension(name);
			
			doProcessEmbeddedExtensions(document, element, ext);
			
	}
	
	@Override
	public void injectHtmlSection(Document document, Element element, Class<?> clz, String resource, boolean canFail) throws IOException {
		
		Document doc = resolveDocument(clz, resource, canFail);
		
		Elements children = doc.selectFirst("body").children();
		if(Objects.nonNull(children)) {
			for(Element e : children) {
				e.appendTo(element);
			}
		}
		
		/**
		 * The child document may have inserted CSS or Scripts into its document. We
		 * have to move them to the parent document.
		 */
		for(Element node : doc.select("script")) {
			PageHelper.appendLast(PageHelper.getOrCreateTag(document, "head"), "script", node);
		}
		
		for(Element node : doc.select("link")) {
			PageHelper.appendLast(PageHelper.getOrCreateTag(document, "head"), "link", node);
		}
		
		processChildExtensions(document, element);
	}
	
	@Override
	public void injectHtmlSection(Document document, Element element, PageExtension ext) throws IOException {
		
		Document doc = resolveDocument(ext);
		
		PageDependencies deps = ext.getClass().getAnnotation(PageDependencies.class);
		if(Objects.nonNull(deps) && Objects.nonNull(deps.extensions())) {
			processPageLevelExtensions(document, deps.extensions());
		}
		
		ext.process(doc, element, this);
		
		Elements children = doc.selectFirst("body").children();
		if(Objects.nonNull(children)) {
			for(Element e : children) {
				e.appendTo(element);
			}
		}
		
		/**
		 * The child document may have inserted CSS or Scripts into its document. We
		 * have to move them to the parent document.
		 */
		for(Element node : doc.select("script")) {
			PageHelper.appendLast(PageHelper.getOrCreateTag(document, "head"), "script", node);
		}
		
		for(Element node : doc.select("link")) {
			PageHelper.appendLast(PageHelper.getOrCreateTag(document, "head"), "link", node);
		}
		
		processChildExtensions(document, element);
	}
	
	protected void doProcessEmbeddedExtensions(Document document, Element element, PageExtension ext) throws IOException {
			
			// Stop this being processed again
			element.removeAttr("jad:id"); 
			
			injectHtmlSection(document, element, ext);

			contentService.resolveScript(ext.getName(), document, ext);
			contentService.resolveStylesheet(ext.getName(), document, ext);
	}

	
	
	public String getCssResource() {
		return String.format("%s.css", getClass().getSimpleName());
	}

	public String getJsResource() {
		return String.format("%s.js", getClass().getSimpleName());
	}

	protected void generateContent(Document document) throws IOException { };

	protected Document resolveDocument(Page page) throws IOException {
		return resolveDocument(page.getResourceClass(), page.getHtmlResource(), false);
	}
	
	protected Document resolveDocument(PageExtension ext) throws IOException {
		return resolveDocument(ext.getResourceClass(), ext.getHtmlResource(), false);
	}
	
	protected Document resolveDocument(Class<?> clz, String resource, boolean canFail) throws IOException {
		return contentService.resolveDocument(clz, this, resource, canFail);
	}

	private void processPageLevelExtensions(Document document, String[] extensionIds) throws IOException {
		
		for(String ext : extensionIds) {
			pageCache.resolveExtension(ext).process(document, null, this);
		}
	}
	
	private void showFeedback(Document document, String icon, String bundle, String i18n, Set<String> classes, Object... args) {
		var feedback = document.selectFirst("#feedback");
		feedback.appendChild(Html.div(classes.toArray(new String[0]))
				.appendChild(Html.i("fa-solid", icon))
				.appendChild(Html.i18n(bundle, i18n, args)));
	}
	
	protected void showError(Document document, String bundle, String i18n, Object... args) {
		showFeedback(document, "fa-square-exclamation", bundle, i18n, Set.of("alert", "alert-danger"), args);
	}
	
	protected void showSuccess(Document document, String bundle, String i18n, Object... args) {
		showFeedback(document, "fa-thumbs-up", bundle, i18n, Set.of("alert", "alert-success"), args);
	} 
	
	protected void showInfo(Document document, String bundle, String i18n, Object... args) {
		showFeedback(document, "fa-square-info", bundle, i18n, Set.of("alert", "alert-info"), args);
	}
	
	protected void showWarning(Document document, String bundle, String i18n, Object... args) {
		showFeedback(document, "fa-triangle-exclamation", bundle, i18n, Set.of("alert", "alert-warning"), args);
	}
	
	@Override
	public void addProcessor(PageExtension ext) {
		
		if(extensions.get()==null) {
			extensions.set(new ArrayList<>());
		}
		extensions.get().add(ext);
	}
	
	@Override
	public void addPageEnd(PageEnd end) {
		pageEnd.get().add(end);
	}
	
	@FunctionalInterface
	public interface PageEnd {
		
		void finishPage(Document doc, String uri, Page page);
	}
}
