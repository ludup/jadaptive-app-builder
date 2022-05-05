package com.jadaptive.plugins.web.ui.renderers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableAction.Window;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.UserInterfaceService;
import com.jadaptive.api.ui.renderers.form.BootstrapBadgeRender;
import com.jadaptive.utils.Utils;

public class TableRenderer {

	int start;
	int length;
	
	long totalObjects;
	Collection<AbstractObject> objects;
	
	ObjectTemplate template;
	Class<?> templateClazz;
	
	TableView view;
	AbstractObject parentObject = null;
	FieldTemplate field;
	private boolean readOnly;
	
	public TableRenderer(boolean readOnly, AbstractObject parentObject, FieldTemplate field) {
		this.parentObject = parentObject;
		this.field = field;
		this.readOnly = readOnly;
	}
	
	public TableRenderer(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Elements render() throws IOException {
		
		try {
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
			
			boolean canUpdate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canUpdate(template);
			if(objects.size() > 0) {
				boolean requiresActions = canUpdate || template.isDeletable();
				
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
			
			ObjectMapper json = new ObjectMapper();
			
			if(objects.size() > 0) {
				for(AbstractObject obj : objects) {
					
					ObjectTemplate rowTemplate = template;
					if(!obj.getResourceKey().equals(template.getResourceKey())) {
						rowTemplate = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(obj.getResourceKey());
					}
					Element row = Html.tr();
					
					if(Objects.nonNull(parentObject)) {
						row.appendChild(new Element("input").attr("type", "hidden")
							.attr("name", this.field.getResourceKey())
							.val(Base64.getUrlEncoder().encodeToString(json.writeValueAsBytes(obj))));
					}
					
					for(String column : view.defaultColumns()) {
						row.appendChild(Html.td().appendChild(renderElement(obj, rowTemplate, template.getField(column))));
					}
					
					renderRowActions(row, obj, view, rowTemplate);
					
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
		
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
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
		
		boolean canUpdate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canUpdate(template);
		boolean canCreate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canCreate(template);
		
		if(canUpdate && !readOnly) {
			if(Objects.isNull(parentObject)) {
				el.appendChild(Html.a(replaceVariables("/app/ui/update/{resourceKey}/{uuid}", obj), "ms-2")
						.appendChild(Html.i("far", "fa-edit","fa-fw")));
			} else {
				el.appendChild(Html.a("#", "ms-2", "stash") 
						.attr("data-action", replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
						.attr("data-url", replaceVariables("/app/ui/object-update/{resourceKey}/{uuid}", parentObject) + "/" + field.getResourceKey() + "/" + obj.getUuid())
						.appendChild(Html.i("far", "fa-edit","fa-fw")));
			}
		}
		
		if(view.requiresView()) {
			if(Objects.isNull(parentObject)) {
				el.appendChild(Html.a(replaceVariables("/app/ui/view/{resourceKey}/{uuid}", obj), "ms-2")
						.appendChild(Html.i("far", "fa-eye","fa-fw")));
			} else {
				el.appendChild(Html.a(replaceVariables("/app/ui/object-view/{resourceKey}/{uuid}", parentObject)
						+ "/" + field.getResourceKey() + "/" + obj.getUuid(), "ms-2")
							.appendChild(Html.i("far", "fa-eye","fa-fw")));
			}
		}
		
		
		if(canCreate && !readOnly) {
			el.appendChild(Html.a(replaceVariables("/app/api/objects/{resourceKey}/copy/{uuid}", obj), "ms-2")
					.appendChild(Html.i("far", "fa-copy","fa-fw")));
		} else {
			el.appendChild(Html.i("far", "fa-fw", "ms-2"));
		}
		
		for(TableAction action : view.actions()) {
			if(action.target()==Target.ROW) {
				if(view.requiresCreate() && !canCreate) {
					el.appendChild(Html.i("far", "fa-fw", "ms-2"));
					continue;
				}
				if(view.requiresUpdate() && !canUpdate) {
					el.appendChild(Html.i("far", "fa-fw", "ms-2"));
					continue;
				}
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
			if(!obj.isSystem() && !readOnly) {
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

	private Node renderElement(AbstractObject obj, ObjectTemplate template, FieldTemplate field) throws UnsupportedEncodingException {
		
		boolean isDefault = StringUtils.defaultString(template.getDefaultColumn()).equals(field.getResourceKey());
		boolean canUpdate = ApplicationServiceImpl.getInstance().getBean(UserInterfaceService.class).canUpdate(template);
		
		if(isDefault) {
			if(canUpdate && !readOnly) {
				if(Objects.isNull(parentObject)) {
					return Html.a(replaceVariables("/app/ui/update/{resourceKey}/{uuid}", obj), "underline").appendChild(renderText(field, obj, template));
				} else {
					return Html.a("#", "underline", "stash")
							.attr("data-action", replaceVariables("/app/api/form/stash/{resourceKey}", parentObject))
							.attr("data-url", replaceVariables("/app/ui/object-update/{resourceKey}/{uuid}", parentObject) + "/" + this.field.getResourceKey() + "/" + obj.getUuid())
							.appendChild(renderText(field, obj, template));
				}
			} else {
				if(Objects.isNull(parentObject)) {
					return Html.a(String.format("/app/ui/view/%s/%s", template.getCollectionKey(), obj.getUuid()) , "underline").appendChild(renderText(field, obj, template));
				} else {
					return Html.a(replaceVariables("/app/ui/object-view/{resourceKey}/{uuid}", parentObject)  + "/" + this.field.getResourceKey() + "/" + obj.getUuid(), "underline").appendChild(renderText(field, obj, template));
				}
				
			}
			
		}
		
		switch(field.getFieldType()) {
		case BOOL:
			return Html.i("far", Boolean.parseBoolean(obj.getValue(field).toString()) ? "text-success fa-check fa-fw" : "text-danger fa-times fa-fw");
		case OBJECT_REFERENCE: 
			String value = (String) obj.getValue(field);
			if(StringUtils.isBlank(value)) {
				return Html.span("-");
			} else {
				ObjectTemplate t = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(field.getValidationValue(ValidationType.RESOURCE_KEY));
				AbstractObject ref = ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(t.getResourceKey(), value.toString());
				return Html.span(StringUtils.defaultIfEmpty((String) ref.getValue(t.getNameField()), "-"));
			}
		case TEXT:
		case ENUM:
		{
			return renderText(field, obj, template);
		}
		default:
			return Html.span(StringUtils.defaultString(obj.getValue(field).toString()), "UTF-8");
		}
		
	}

	private Element renderText(FieldTemplate field, AbstractObject obj, ObjectTemplate template) {
		FieldRenderer renderer = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).getRenderer(field, template);
		switch(renderer) {
		case BOOTSTRAP_BADGE:
		{
			return BootstrapBadgeRender.generateBadge(StringUtils.defaultString(obj.getValue(field).toString()));
		}
		case I18N:
		{
			return Html.i18n(template.getBundle(), StringUtils.defaultString(obj.getValue(field).toString()));
		}
		default:
		{
			return Html.span(StringUtils.defaultString(obj.getValue(field).toString()), "UTF-8");
		}
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
