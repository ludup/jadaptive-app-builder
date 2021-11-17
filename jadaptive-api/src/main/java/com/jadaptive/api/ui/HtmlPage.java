package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.servlet.Request;

public abstract class HtmlPage implements Page {
	
	@Autowired
	private PageCache pageCache; 
	
	protected String resourcePath;
	
	public String getResourcePath() {
		return resourcePath;
	}
		
	public String getResource() {
		return String.format("%s.html", getClass().getSimpleName());
	}
	
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		
	}
	
	public final void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {	
		
		beforeProcess(uri, request, response);
		
		Document document = resolveDocument(this);
		generateContent(document);
		processPageExtensions(uri, document);
		documentComplete(document);
		ResponseHelper.sendContent(document.toString(), "text/html; charset=UTF-8;", request, response);
	}

	protected void documentComplete(Document document) { };
	
	private void processPageExtensions(String uri, Document document) throws IOException {
		
		processPageDependencies(document);
		
		processDocumentExtensions(document);
		
		resolveScript(getUri(), document, getClass());
		resolveStylesheet(getUri(), document, getClass());

		processPageProcessors(document);
	}

	private void processPageDependencies(Document document) throws IOException {
		
		PageDependencies deps = getClass().getAnnotation(PageDependencies.class);
		if(Objects.nonNull(deps) && Objects.nonNull(deps.extensions())) {
			processPageLevelExtensions(document, deps.extensions());
		}
		
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
						return Request.get().getParameter(name);
					} else {
						throw new UnsupportedOperationException();
					}
				});
				
				m.invoke(this, doc, formProxy);
				
			} else {
				processPost(uri, request, response);
			}
			
			processPageExtensions(uri, doc);
			ResponseHelper.sendContent(doc.toString(), "text/html; charset=UTF-8;", request, response);
			
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
			if(e.getCause() instanceof Redirect) {
				throw (Redirect) e.getCause();
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	protected void processPost(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		throw new FileNotFoundException();
	}

	private void processDocumentExtensions(Document document) throws IOException {
		Elements embeddedElement = document.getElementsByAttribute("jad:id");
		for(Element embedded : embeddedElement) {
			processEmbeddedExtensions(document, embedded);
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
	
	protected void doProcessEmbeddedExtensions(Document document, Element element, PageExtension ext) throws IOException {
			
			// Stop this being processed again
			element.removeAttr("jad:id"); 
			
			Document doc = resolveDocument(ext);
			
			ext.process(doc, element, this);
			
			PageDependencies deps = ext.getClass().getAnnotation(PageDependencies.class);
			if(Objects.nonNull(deps) && Objects.nonNull(deps.extensions())) {
				processPageLevelExtensions(document, deps.extensions());
			}
			
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
			
			resolveScript(ext.getName(), document, ext.getClass());
			resolveStylesheet(ext.getName(), document, ext.getClass());
			
			processChildExtensions(document, element);
//		}
	}

	private void resolveStylesheet(String uri, Document document, Class<?> clz) {
		URL url = clz.getResource(String.format("%s.css", clz.getSimpleName()));
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(document, "/app/css/" + uri + ".css");
		} 
	}
	
	private void resolveScript(String uri, Document document, Class<?> clz) {
		URL url = clz.getResource(String.format("%s.js", clz.getSimpleName()));
		if(Objects.nonNull(url)) {
			PageHelper.appendScript(document, "/app/js/" + uri + ".js");
		} 
	}

	protected void generateContent(Document document) throws IOException { };

	protected Document resolveDocument(Page page) throws IOException {
		return resolveDocument(page.getClass(), page.getResource());
	}
	
	protected Document resolveDocument(PageExtension ext) throws IOException {
		return resolveDocument(ext.getClass(), ext.getResource());
	}
	
	protected Class<?> getResourceClass() {
		return getClass();
	}
	
	protected Document resolveDocument(Class<?> clz, String resource) throws IOException {
		URL url = clz.getResource(resource);
		if(Objects.isNull(url)) {
			url = getResourceClass().getResource(resource);
		}
		if(Objects.nonNull(url)) {
			try(InputStream in = url.openStream()) {
				return Jsoup.parse(IOUtils.toString(in, "UTF-8"));
			}
		} else {
			Document doc = new Document(Request.get().getRequestURI());
			doc.appendChild(new Element("body"));
			return doc;
		}
	}

	private void processPageLevelExtensions(Document document, String[] extensions) throws IOException {
		
		for(String ext : extensions) {
			pageCache.resolveExtension(ext).process(document, null, this);
		}
	}
}