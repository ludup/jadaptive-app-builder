package com.jadaptive.api.ui.pages.ext;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.HtmlPageExtender;
import com.jadaptive.api.ui.Page;

@Component
public class BootstrapToastPageExtender implements HtmlPageExtender {

	@Override
	public boolean isExtending(Page page, String uri) {
		return true;
	}

	@Override
	public void processEnd(Document doc, String uri, Page page) {
		
		/**
		 * Turn feedback into toaster popups
		 */
		var feedback = doc.getElementById("feedback");
		if(feedback != null) {
			var alert = feedback.selectFirst(".alert");
			if(alert != null) {
				var bg = alert.classNames().stream().filter(c -> c.startsWith("alert-")).map(c -> c.substring(6)).findFirst().orElse("primary");
				var html = alert.selectFirst("span").html();
				var icn = alert.selectFirst("i");
				
				var bdy = Html.div("toast-body");
				var bdytxt = new Element("span");
				bdytxt.html(html);
				if(icn != null) {
					bdy.appendChild(icn);
				}
				bdy.appendChild(bdytxt);
				
				var btn = Html.button("btn-close", "btn-close-white", "me-2", "m-auto");
				btn.dataset().put("bs-dismiss", "toast");
				btn.attr("aria-label", "Close");
				
				var fbox = Html.div("d-flex");
				fbox.appendChild(bdy);
				fbox.appendChild(btn);
				var toast = Html.div("toast", "align-items-center", "text-bg-" + bg, "border-0", "show");
				toast.attr("role", "alert");
				toast.attr("aria-live", "assertive");
				toast.attr("aria-atomic", "true");
				toast.appendChild(fbox);
				
				var cnt = Html.div("toast-container", "p-3", "top-0", "start-50", "translate-middle-x");
				cnt.appendChild(toast);
				
				var out = Html.div("position-relative");
				out.appendChild(cnt);
				
				//doc.body().appendChild(cnt);
				//feedback.parent().appendChild(cnt);
				var hdr = doc.getElementsByTag("header").first();
				if(hdr == null)
					feedback.parent().insertChildren(0, out);
				else
					hdr.after(out);
				
				feedback.remove();
			}
		}
	}
	
}
