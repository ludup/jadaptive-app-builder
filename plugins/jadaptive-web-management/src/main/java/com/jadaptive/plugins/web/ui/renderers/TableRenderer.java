package com.jadaptive.plugins.web.ui.renderers;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableAction.Window;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.Html;
import com.jadaptive.utils.Utils;

public class TableRenderer {

	int start;
	int length;
	
	long totalObjects;
	Collection<AbstractObject> objects;
	
	ObjectTemplate template;
	Class<?> templateClazz;
	
	TableView view;
	
	public Elements render() {
		
		Elements tableholder = new Elements();
		Element table;
		tableholder.add(table = Html.table("table").attr("data-toggle", "table"));
		
		Element el;
		table.appendChild(Html.thead().appendChild(el = Html.tr()));
		
		view = templateClazz.getAnnotation(TableView.class);
		int columns = 0;
		for(String column : view.defaultColumns()) {
			el.appendChild(Html.td().appendChild(Html.i18n(template.getBundle(),String.format("%s.name", column))));
			columns++;
		}
		
		if(objects.size() > 0) {
			boolean requiresActions = template.isUpdatable() || template.isDeletable();
			
			for(TableAction action : view.actions()) {
				if(action.target()==Target.ROW) {
					requiresActions |= true;
					break;
				}
			}
			if(requiresActions) {
				el.appendChild(Html.td());
				columns++;
			}
		}
	
		
		table.appendChild(el = Html.tbody());
		
		if(objects.size() > 0) {
			for(AbstractObject obj : objects) {
				Element row = Html.tr();
				
				for(String column : view.defaultColumns()) {
					row.appendChild(Html.td().appendChild(renderElement(obj, template, template.getField(column))));
				}
				
				renderRowActions(row, obj, view, template);
				
				el.appendChild(row);
			}
		} else {
			el.appendChild(Html.tr().appendChild(Html.td().attr("colspan", String.valueOf(columns))
					.addClass("text-center")
					.appendChild(Html.i18n("default", "noResults.text"))));
		}
		
		if(isPaginationRequired()) {
			tableholder.add(renderPagination());
		}

		return tableholder;
		
	}
	
	private boolean isPaginationRequired() {
		return totalObjects > length;
	}

	private Element renderPagination() {
		
		long pages = totalObjects / length;
		if(totalObjects % length > 0) {
			pages++;
		}
		
		int currentPage = 0;
		if(start > 0) {
			currentPage = start / length;
		}
		
		Element pageList;
		Element pagnation = Html.div("col-12").attr("id", "pagination");
		
		
		pagnation.appendChild(Html.nav().appendChild(pageList = Html.ul("pagination")));
		
		if(currentPage > 0) {
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", "0")
							.appendChild(Html.i("far fa-chevron-double-left"))));
			
			pageList.appendChild(Html.li("page-item")
						.appendChild(Html.a("#", "page-link searchTable")
								.attr("data-start", String.valueOf((currentPage-1)*length))
								.appendChild(Html.i("far fa-chevron-left"))));
		} else {
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("far fa-chevron-double-left"))));
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("far fa-chevron-left"))));
		}

		for(int i=0;i<pages;i++) {
			pageList.appendChild(Html.li("page-item", currentPage == i ? "active" : "")
						.appendChild(Html.a("#", "page-link searchTable")
								.attr("data-start", String.valueOf(i*length))
								.text(String.valueOf(i+1))));
			if(i== 4)
				break;
		}
		
		if(currentPage < pages - 1) {
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", String.valueOf((currentPage+1)*length))
							.appendChild(Html.i("far fa-chevron-right"))));
			pageList.appendChild(Html.li("page-item")
					.appendChild(Html.a("#", "page-link searchTable")
							.attr("data-start", String.valueOf((pages-1)*length))
							.appendChild(Html.i("far fa-chevron-double-right"))));
		} else {
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("far fa-chevron-right"))));
			pageList.appendChild(Html.li("page-item disabled")
					.appendChild(Html.a("#", "page-link")
							.appendChild(Html.i("far fa-chevron-double-right"))));
		}
		
		return pagnation;
	}
	
	private void renderRowActions(Element row, AbstractObject obj, TableView view, ObjectTemplate template) {
		
		Element el = Html.td("text-end");
		
		if(template.isUpdatable()) {
			el.appendChild(Html.a(replaceVariables("/app/ui/view/{resourceKey}/{uuid}", obj), "ms-2")
					.appendChild(Html.i("far", "fa-eye","fa-fw")));
		}
		
		for(TableAction action : view.actions()) {
			if(action.target()==Target.ROW) {
				Object val = obj.getValue(template.getDefaultColumn());
				if(action.confirmationRequired()) {
					if(StringUtils.isNotBlank(template.getDefaultColumn())) {
						el.appendChild(Html.a("#", "deleteAction", "ms-2")
									.attr("data-name", val == null ? "" : val.toString())
									.attr("data-url", replaceVariables(action.url(), obj))
									.attr("target", action.window() == Window.BLANK ? "_blank" : "_self")
									.appendChild(Html.i("far ", action.icon(), "fa-fw")));
					} else {
						el.appendChild(Html.a("#", "deleteAction", "ms-2")
								.attr("data-name", obj.getUuid())
								.attr("data-url", replaceVariables(action.url(), obj))
								.attr("target", action.window() == Window.BLANK ? "_blank" : "_self")
								.appendChild(Html.i("far ", action.icon(), "fa-fw")));
					}
				} else {
					el.appendChild(Html.a(replaceVariables(action.url(), obj), "ms-2")
							.attr("target", action.window() == Window.BLANK ? "_blank" : "_self")
							.appendChild(Html.i("far ", action.icon(), "fa-fw")));
				}
			} 
		}
		
		if(template.isDeletable()) {
			if(!obj.isSystem()) {
				el.appendChild(Html.a("#", "deleteAction", "ms-2")
						.attr("data-name", obj.getValue(template.getDefaultColumn()).toString())
						.attr("data-url", replaceVariables("/app/api/objects/{resourceKey}/{uuid}", obj))
						.appendChild(Html.i("far", "fa-trash", "fa-fw")));
			} else {
				el.appendChild(Html.i("far", "fa-fw", "ms-2"));
			}
		} 
		
		
		if(el.children().size() > 0) {
			row.appendChild(el);
		}
		
	}

	private String replaceVariables(String url, AbstractObject obj) {
		for(String var : Utils.extractVariables(url)) {
			url = url.replace(String.format("{%s}", var), StringUtils.defaultString((String)obj.getValue(var)));
		}
		return url;
	}

	private Node renderElement(AbstractObject obj, ObjectTemplate template, FieldTemplate field) {
		
		boolean isDefault = StringUtils.defaultString(template.getDefaultColumn()).equals(field.getResourceKey());
		
		if(isDefault) {
			if(template.isUpdatable()) {
				return Html.a(String.format("/app/ui/update/%s/%s", template.getCollectionKey(), obj.getUuid()) , "underline").text(StringUtils.defaultString(obj.getValue(field).toString()));
			} else {
				return Html.a(String.format("/app/ui/view/%s/%s", template.getCollectionKey(), obj.getUuid()) , "underline").text(StringUtils.defaultString(obj.getValue(field).toString()));
			}
			
		}
		
		switch(field.getFieldType()) {
		case BOOL:
			return Html.i("far", Boolean.parseBoolean(obj.getValue(field).toString()) ? "text-success fa-check fa-fw" : "text-danger fa-times fa-fw");
		default:
			return Html.span(StringUtils.defaultString(obj.getValue(field).toString()));
		}
		
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setTotalObjects(long totalObjects) {
		this.totalObjects = totalObjects;
	}

	public void setObjects(Collection<AbstractObject> objects) {
		this.objects = objects;
	}

	public void setTemplate(ObjectTemplate template) {
		this.template = template;
	}

	public void setTemplateClazz(Class<?> templateClazz) {
		this.templateClazz = templateClazz;
	}

	public TableView getView() {
		return view;
	}
}
