package com.jadaptive.api.ui;

import static com.jadaptive.utils.Instrumentation.timed;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.utils.FileUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class HtmlPage implements Page {
	
	static Logger log = LoggerFactory.getLogger(HtmlPage.class);
	
	@Autowired
	private PageCache pageCache; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private ClassLoaderService classService; 
	
	private ThreadLocal<List<PageExtension>> extensions = new ThreadLocal<>();
	
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
	
	protected Collection<HtmlPageExtender> extenders() {
		return applicationService.getBean(UserInterfaceService.class).getExtenders(this);
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
		
		ResponseHelper.sendContent(document.toString(), "text/html;charset=UTF-8;", request, response);
	}

	@Override
	public Document generateHTMLDocument(String uri) throws IOException {
		
		Document document = resolveDocument(this);
		currentDocument.set(document);
		
		try {
			
			try(var timed = timed("HtmlPage.generateHTMLDocument#beforeProcess(" + uri + ")")) {
				beforeProcess(uri, Request.get(), Request.response());
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#processPageDependencies(" + uri + ")")) {
				processPageDependencies(document);
			}

			var extenders = extenders();
			if(Objects.nonNull(extenders)) {
				try(var timed = timed("HtmlPage.generateHTMLDocument#extender.processStart(" + uri + ")")) {
					for(HtmlPageExtender extender : extenders) {
						try(var timed2 = timed(extender.getClass().getName())) {
							extender.processStart(document, uri, this);
						}
					}
				}
			}

			try(var timed = timed("HtmlPage.generateHTMLDocument#generateContent(" + uri + ")")) {
				generateContent(document);
			}
			
			if(Objects.nonNull(extenders)) {
				try(var timed = timed("HtmlPage.generateHTMLDocument#extender.generateContent(" + uri + ")")) {
					for(HtmlPageExtender extender : extenders) {
						try(var timed2 = timed(extender.getClass().getName())) {
							extender.generateContent(document, this);
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
			
			if(Objects.nonNull(extenders)) {
				try(var timed = timed("HtmlPage.generateHTMLDocument#extender.processEnd(" + uri + ")")) {
					for(HtmlPageExtender extender : extenders) {
						try(var timed2 = timed(extender.getClass().getName())) {
							extender.processEnd(document, uri, this);
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

	protected void documentComplete(Document document) throws FileNotFoundException, IOException { };
	
	private void processPageExtensions(String uri, Document document) throws IOException {
		

		try(var timed = timed("HtmlPage.processPageExtensions#processDocumentExtensions(" + uri + ")")) {
			processDocumentExtensions(document);
		}

		try(var timed = timed("HtmlPage.processPageExtensions#resolveScript(" + uri + ")")) {
			resolveScript(getUri(), document, this);
		}

		try(var timed = timed("HtmlPage.processPageExtensions#resolveStylesheet(" + uri + ")")) {
			resolveStylesheet(getUri(), document, this);
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

			beforeProcess(uri, request, response);
			
			processPageDependencies(doc);
			
			var extenders = extenders();
			if(Objects.nonNull(extenders)) {
				for(HtmlPageExtender extender : extenders) {
					extender.processStart(doc, uri, this);
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
				Object formProxy = Proxy.newProxyInstance(
						  getClass().getClassLoader(), 
						  new Class<?>[] { fp.getFormClass() }, 
						  (proxy, method, methodArgs) -> {
						  
					if(method.getName().startsWith("get") && method.getName().length() > 3) {
						String name = method.getName().substring(3,4).toLowerCase();
						if(method.getName().length() > 4) {
							name += method.getName().substring(4);
						}
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
					} else if(method.getName().startsWith("is") && method.getName().length() > 2) {
						String name = method.getName().substring(2,3).toLowerCase();
						if(method.getName().length() > 3) {
							name += method.getName().substring(3);
						}
						String value = Request.get().getParameter(name);
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
			
			if(Objects.nonNull(extenders)) {
				for(HtmlPageExtender extender : extenders) {
					extender.processPost(doc, this);
				}
			}
			injectFeedback(doc, request);
			processPageExtensions(uri, doc);
			
			documentComplete(doc);
			
			if(Objects.nonNull(extenders)) {
				for(HtmlPageExtender extender : extenders) {
					extender.processEnd(doc, uri, this);
				}
			}

			afterProcess(uri, request, response);
			
			ResponseHelper.sendContent(doc.toString(), "text/html; charset=UTF-8;", request, response);
			
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException e) {
			if(e.getCause() instanceof Redirect) {
				throw (Redirect) e.getCause();
			}
			log.error("Failed to generate HTML page", e);
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			currentDocument.remove();
		}
	}
	
	protected void beforeForm(Document doc, HttpServletRequest request, HttpServletResponse response) {
		
	}

	protected void injectFeedback(Document doc, HttpServletRequest request) {
		Feedback feedback = (Feedback) request.getSession().getAttribute("feedback");
		if(Objects.nonNull(feedback)) {
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
			try(var timed = timed("HtmlPage.processDocumentExtensions#embedded(" + embedded.attr("jad:id") + ")")) {
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

			resolveScript(ext.getName(), document, ext);
			resolveStylesheet(ext.getName(), document, ext);
	}

	protected void resolveStylesheet(String uri, Document document, PageResources ext) {
		URL url = ext.getResourceClass().getResource(ext.getCssResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(document, "/app/css/" + uri + ".css");
		} else {
			url = classService.getResource(ext.getCssResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendStylesheet(document, "/app/style/" + uri + ".css");
			} 
		}
	}
	
	public String getCssResource() {
		return String.format("%s.css", getClass().getSimpleName());
	}

	protected void resolveScript(String uri, Document document, PageResources ext) {
		URL url = ext.getResourceClass().getResource(ext.getJsResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendBodyScript(document, "/app/js/" + uri + ".js");
		} else {
			url = classService.getResource(ext.getJsResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendBodyScript(document, "/app/script/" + ext.getJsResource());
			}
		}
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
		
		if(isOverride(clz)) {
			return getCustomizedContent(clz);
		} else {
			return getPageDocument(clz, resource, canFail);
		}
		
	}
	
	protected Document getPageDocument(Class<?> clz, String resource, boolean canFail) throws IOException {
		
		URL url = clz.getResource(resource);
		if(Objects.isNull(url)) {
			url = getResourceClass().getResource(resource);
		}
		if(Objects.isNull(url)) {
			url = classService.getResource(resource);
		}
		if(resource.startsWith("/")) {
			resource = FileUtils.checkStartsWithNoSlash(resource);
			return resolveDocument(clz, resource, canFail);
		}
		if(Objects.nonNull(url)) {
			return loadDocument(url);
		} else {
			if(canFail) {
				throw new IOException("Missing document for " + resource);
			}
			Document doc = new Document(Request.get().getRequestURI());
			doc.appendChild(new Element("body"));
			return doc;
		}
		
	}

	protected Document loadDocument(URL url) throws IOException {
		try(InputStream in = url.openStream()) {
			return Jsoup.parse(in, "UTF-8", url.toExternalForm());
		}
	}

	private Document getCustomizedContent(Class<?> clz) {
		
//		HtmlContentService contentService = applicationService.getBean(HtmlContentService.class);
		
		return null;
	}

	private boolean isOverride(Class<?> clz) {
		return false; //clz.getAnnotation(CustomizablePage.class) != null;
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
	
	public void addProcessor(PageExtension ext) {
		
		if(extensions.get()==null) {
			extensions.set(new ArrayList<>());
		}
		extensions.get().add(ext);
	}
}
