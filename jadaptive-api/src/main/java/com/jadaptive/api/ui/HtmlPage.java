package com.jadaptive.api.ui;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
	
	private Collection<HtmlPageExtender> extenders = null;
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
	
		extenders = applicationService.getBean(UserInterfaceService.class).getExtenders(this);
		onCreated();
	}
	
	protected boolean isCacheable() { return false; }
	
	protected int getMaxAge() { return 3600; }
	
	public void onCreated() throws FileNotFoundException { }
	
	public final void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {	
		
		beforeProcess(uri, request, response);
		
		Document document = generateHTMLDocument(uri);
		
		if(!isCacheable()) {
			sessionUtils.setDoNotCache(response);
		} else {
			sessionUtils.setCachable(response, getMaxAge());
		}
		
		afterProcess(uri, request, response);
		ResponseHelper.sendContent(document.toString(), "text/html;charset=UTF-8;", request, response);
	}

	@Override
	public Document generateHTMLDocument(String uri) throws IOException {
		
		Document document = resolveDocument(this);
		currentDocument.set(document);
		
		try {
			processPageDependencies(document);
			
			if(Objects.nonNull(extenders)) {
				for(HtmlPageExtender extender : extenders) {
					extender.processStart(document, uri, this);
				}
			}
			
			generateContent(document);
			
			if(Objects.nonNull(extenders)) {
				for(HtmlPageExtender extender : extenders) {
					extender.generateContent(document, this);
				}
			}
			
			if(Request.isAvailable()) {
				injectFeedback(document, Request.get());
			}
			processPageExtensions(uri, document);
			documentComplete(document);
			
			if(Objects.nonNull(extenders)) {
				for(HtmlPageExtender extender : extenders) {
					extender.processEnd(document, uri, this);
				}
			}
		
		} finally {
			currentDocument.remove();
		}
		return document;
	}

	protected void documentComplete(Document document) throws FileNotFoundException, IOException { };
	
	private void processPageExtensions(String uri, Document document) throws IOException {
		
		processDocumentExtensions(document);
		
		resolveScript(getUri(), document, this);
		resolveStylesheet(getUri(), document, this);

		processPageProcessors(document);
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
		
		beforeProcess(uri, request, response);
		
		try {

			Document doc = resolveDocument(this);
			currentDocument.set(doc);
			
			processPageDependencies(doc);
			
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
				m.invoke(this, doc, formProxy);
				
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

			ResponseHelper.sendContent(doc.toString(), "text/html; charset=UTF-8;", request, response);
			
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
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

	private void injectFeedback(Document doc, HttpServletRequest request) {
		Feedback feedback = (Feedback) request.getSession().getAttribute("feedback");
		if(Objects.nonNull(feedback)) {
			request.getSession().removeAttribute("feedback");
			Element element = doc.selectFirst("#feedback");
			if(Objects.isNull(element)) {
				element = doc.selectFirst("#content");
				if(Objects.nonNull(element)) {
					element.prependChild(Html.div("col-12")
								.appendChild(Html.div("alert", feedback.getAlert())
								.appendChild(Html.i("fa-solid", feedback.getIcon(), "me-2"))
								.appendChild(getTextElement(feedback))));
				} else {
					element = doc.selectFirst("main");
					if(Objects.nonNull(element)) {
						element.appendChild(Html.div("col-12")
								.appendChild(Html.div("alert", feedback.getAlert())
								.appendChild(Html.i("fa-solid", feedback.getIcon(), "me-2"))
								.appendChild(getTextElement(feedback))));
					}
				}
			} else {
				element.appendChild(Html.div("alert", feedback.getAlert())
						.appendChild(Html.i("fa-solid", feedback.getIcon(), "me-2"))
						.appendChild(getTextElement(feedback)));
			}
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
			processEmbeddedExtensions(document, embedded);
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
		URL url = ext.getClass().getResource(ext.getCssResource());
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
		URL url = ext.getClass().getResource(ext.getJsResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendHeadScript(document, "/app/js/" + uri + ".js");
		} else {
			url = classService.getResource(ext.getJsResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendHeadScript(document, "/app/script/" + ext.getJsResource());
			}
		}
	}

	public String getJsResource() {
		return String.format("%s.js", getClass().getSimpleName());
	}

	protected void generateContent(Document document) throws IOException { };

	protected Document resolveDocument(Page page) throws IOException {
		return resolveDocument(page.getClass(), page.getHtmlResource(), false);
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
			try(InputStream in = url.openStream()) {
				return Jsoup.parse(IOUtils.toString(in, "UTF-8"));
			}
		} else {
			if(canFail) {
				throw new IOException("Missing document for " + resource);
			}
			Document doc = new Document(Request.get().getRequestURI());
			doc.appendChild(new Element("body"));
			return doc;
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
	
	private void showFeedback(Document document, String icon, String bundle, String i18n, String... classes) {
		document.selectFirst("#feedback").appendChild(Html.div(classes)
				.appendChild(Html.i("fa-solid", icon))
				.appendChild(Html.i18n(bundle, i18n)));
	}
	
	protected void showError(Document document, String bundle, String i18n) {
		showFeedback(document, "fa-square-exclamation", bundle, i18n, "alert", "alert-danger");
	}
	
	protected void showSuccess(Document document, String bundle, String i18n) {
		showFeedback(document, "fa-thumbs-up", bundle, i18n, "alert", "alert-success");
	}
	
	protected void showInfo(Document document, String bundle, String i18n) {
		showFeedback(document, "fa-square-info", bundle, i18n, "alert", "alert-info");
	}
	
	protected void showWarning(Document document, String bundle, String i18n) {
		showFeedback(document, "fa-triangle-exclamation", bundle, i18n, "alert", "alert-warning");
	}
	
	public void addProcessor(PageExtension ext) {
		
		if(extensions.get()==null) {
			extensions.set(new ArrayList<>());
		}
		extensions.get().add(ext);
	}
}
