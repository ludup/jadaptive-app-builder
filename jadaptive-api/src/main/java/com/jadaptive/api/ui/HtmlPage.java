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
	
	public String getResource() {
		return String.format("%s.html", getClass().getSimpleName());
	}
	
	public final void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {	
		
		Document document = resolveDocument(this);
		generateContent(document);
		processPageExtensions(uri, document);
		documentComplete(document);
		ResponseHelper.sendContent(document.toString(), "text/html; charset=UTF-8;", request, response);
	}

	protected void documentComplete(Document document) { };
	
	private void processPageExtensions(String uri, Document document) throws IOException {
		
		processPageDependencies(document);
		
		processEmbeddedExtensions(document);
		
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
		try {

			Document doc = resolveDocument(this);
			
			if(this instanceof FormProcessor) {
				FormProcessor<?> fp = (FormProcessor<?>) this;
				Method m = ReflectionUtils.getMethod(getClass(), "processForm", Document.class, Object.class);
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

	private void processEmbeddedExtensions(Document parent) throws IOException {
		Elements embeddedElement = parent.getElementsByAttribute("jad:id");
		for(Element embedded : embeddedElement) {
			
			String name = embedded.attr("jad:id");
			PageExtension ext = pageCache.resolveExtension(name);
			Document doc = resolveDocument(ext);
			ext.process(doc, this);
			
			PageDependencies deps = ext.getClass().getAnnotation(PageDependencies.class);
			if(Objects.nonNull(deps) && Objects.nonNull(deps.extensions())) {
				processPageLevelExtensions(parent, deps.extensions());
			}
			
			Elements children = doc.selectFirst("body").children();
			if(Objects.nonNull(children)) {
				for(Element element : children) {
					element.appendTo(embedded);
				}
			}
			
			/**
			 * The child document may have inserted CSS or Scripts into its document. We
			 * have to move them to the parent document.
			 */
			for(Element node : doc.select("script")) {
				PageHelper.appendLast(PageHelper.getOrCreateTag(parent, "head"), "script", node);
			}
			
			for(Element node : doc.select("link")) {
				PageHelper.appendLast(PageHelper.getOrCreateTag(parent, "head"), "link", node);
			}
			
			resolveScript(name, parent, ext.getClass());
			resolveStylesheet(name, parent, ext.getClass());
		}
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

	protected void generateContent(Document document) throws FileNotFoundException { };

	protected Document resolveDocument(Page page) throws IOException {
		return resolveDocument(page.getClass(), page.getResource());
	}
	
	protected Document resolveDocument(PageExtension ext) throws IOException {
		return resolveDocument(ext.getClass(), ext.getResource());
	}
	
	protected Document resolveDocument(Class<?> clz, String resource) throws IOException {
		URL url =clz.getResource(resource);
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
			pageCache.resolveExtension(ext).process(document, this);
		}
	}
}
