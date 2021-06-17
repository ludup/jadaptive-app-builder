package com.jadaptive.plugins.web.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageDependencies;

@Extension
@PageDependencies(extensions = { "bootstrapTable"})
public class TableWidget extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		if(!(page instanceof TemplatePage)) {
			throw new IllegalStateException();
		}
		
		TemplatePage templatePage = (TemplatePage) page;
		Element table = document.select("#table").first();
		table.attr("data-resourcekey", templatePage.getTemplate().getResourceKey());
		
		Element tr;
		table.appendChild(new Element("thead").appendChild(tr = new Element("tr")));
//		tr.appendChild(new Element("th").attr("data-field", "state").attr("data-checkbox","true"));
		
		for(FieldTemplate field : templatePage.getTemplate().getFields()) {
			if(field.getViews().contains(FieldView.TABLE)) {
				Element el;
				tr.appendChild(el = new Element("th")
						.attr("jad:bundle", templatePage.getTemplate().getBundle())
						.attr("jad:i18n", String.format("%s.name", 
								field.getResourceKey()))
						.attr("data-field", field.getResourceKey()));
				
				switch(field.getFieldType()) {
				case BOOL:
					el.attr("data-formatter", "renderBool");
				default:
				}
			}
		}
		
		tr.appendChild(new Element("th")
				.attr("jad:bundle", "default")
				.attr("jad:i18n", "actions.name")
				.attr("data-formatter", "renderActions"));

	}

	@Override
	public String getName() {
		return "tableWidget";
	}

}
