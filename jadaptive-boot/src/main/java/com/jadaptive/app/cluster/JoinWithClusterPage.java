package com.jadaptive.app.cluster;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.cluster.ClusterNode;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;

@Extension
@ModalPage
@RequestPage(path = JoinWithClusterPage.URI)
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils" })
@PageProcessors(extensions = { "i18n" })
public class JoinWithClusterPage extends AuthenticatedPage {
	final static String URI = "join-with-cluster";
	final static Logger LOG = LoggerFactory.getLogger(JoinWithClusterPage.class);

	@Autowired
	private PageCache pageCache;

	@Override
	public String getUri() {
		return URI;
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		var req = Request.get();
		var error = req.getParameter("error");
		var errorDescription = req.getParameter("error_description");
		if(error != null) {
			if(errorDescription == null) {
				Feedback.error(ClusterNode.RESOURCE_KEY, "error.failedToJoinWithCluster", error);
			}
			else {
				Feedback.error(ClusterNode.RESOURCE_KEY, "error.failedToJoinWithCluster", error + ". " + errorDescription);
			}
		}
		else {
			Feedback.info(ClusterNode.RESOURCE_KEY, "info.joinedWithCluster");
			
			new Thread(() -> {
				LOG.info("Pending shutdown in 10 seconds as joined cluster.");
				try {
					Thread.sleep(Duration.ofSeconds(10));
				}
				catch(Exception e) {}
				LOG.info("Shutting down as joined cluster.");
				System.exit(0);
			}).start();
		}
		throw new PageRedirect(pageCache.getHomePage());
	}

}
