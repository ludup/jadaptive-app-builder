package com.jadaptive.api.ui.pages;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Component
@RequestPage(path = "processing/{uuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class ProcessingPage extends HtmlPage {

	
	private String uuid;
	
	@Override
	public String getUri() {
		return "processing";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		
		ProcessingJob job = (ProcessingJob) Request.get().getSession().getAttribute(uuid);
		if(Objects.isNull(job)) {
			throw new IllegalStateException("The job request has timed out");
		}
		
		document.selectFirst("#processingTitle")
			.appendChild(Html.i18n(job.getBundle(), job.getTitle()));
		document.selectFirst("#processingMessage")
			.appendChild(Html.i18n(job.getBundle(), job.getTitle()));
		
		document.selectFirst("#uuid").val(uuid);
		
		super.generateContent(document);
	}
	
	

}
