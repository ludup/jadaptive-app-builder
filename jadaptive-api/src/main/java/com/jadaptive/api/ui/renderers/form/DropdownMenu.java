package com.jadaptive.api.ui.renderers.form;

import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageHelper;
public class DropdownMenu extends FieldInputRender {
	
	private Element dropdownMenu;
	private Element valueElement;
	private Element emptyLi;
	
	public DropdownMenu(TemplateViewField field) {
		super(field);
	}
	
	public DropdownMenu(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	@Override
	public void renderInput(Element rootElement, String defaultValue, boolean readOnly, String... classes) {

		
		Element e;
		rootElement.appendChild( e =new Element("div"));
		e.addClass("dropdownInput");
		
		if(decorate) {
				
				e.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		}

		e.appendChild(valueElement = new Element("input")
				.addClass(resourceKey)
				.attr("name", getFormVariableWithParents())
				.attr("type", "hidden"));
		
		e.appendChild(dropdownMenu = new Element("ul")
				.addClass(String.format("%sDropdown", getResourceKey()) + " dropdown-menu  dropdown-size d-block position-static"));
		
		if(decorate) {
			e.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}
		
		if(readOnly) {
			dropdownMenu.attr("data-read-only", true);
		}


		emptyLi = Html.li("px-2", "text-muted");
		emptyLi.addClass(String.format("%sDropdownEmpty", getResourceKey()));
		dropdownMenu.appendChild(emptyLi);
		var emptyText = Html.i18nWithFallback("objectFields", "dropdownMenu.empty", getBundle(), String.format("%s.empty", getResourceKey()));
		emptyLi.appendChild(emptyText);
	}
	
	public void addInputValue(String value, String name) {
		
		var li = Html.li("");
		dropdownMenu.appendChild(li);
		li.addClass(String.format("%sListItem", getResourceKey()));
		
		var anchor = PageHelper.createAnchor("#")
				.attr("data-resourcekey", value)
				.attr("role", "dropdown-menu-item-select")
				.attr("data-dropdown-menu-item-target", getResourceKey())
				.addClass("dropdown-item");
		
		if("true".equals(dropdownMenu.attr("data-read-only"))) {
			anchor.attr("disabled", "");
		}
		
		if(Objects.equals(value, this.valueElement.val())) {
			anchor.addClass("active");
		}
		
		var liDiv = Html.div("d-flex");
		anchor.appendChild(liDiv);

		var flexInner = Html.div("flex-grow-1");
		flexInner.text(name);
		liDiv.appendChild(flexInner);
		
		li.appendChild(anchor);
		dropdownMenu.appendChild(li);
		emptyLi.addClass("d-none");
	}
//	
	public void setSelectedValue(String value, String name) {
		valueElement.val(value);
		dropdownMenu.select("[data-dropdown-menu-item-target=\"" + getResourceKey() + "\"]").forEach(el -> el.removeClass("active"));
		var sel = dropdownMenu.select("[data-resourcekey=\"" + value + "\"]").first();
		if(sel != null)
			sel.addClass("active");
	}
//
	public void addI18nValue(String value, String i18n) {
		
		var li = Html.li("");
		dropdownMenu.appendChild(li);
		li.addClass(String.format("%sListItem", getResourceKey()));
		
		var anchor = PageHelper.createAnchor("#")
				.attr("data-resourcekey", value)
				.attr("role", "dropdown-menu-item-select")
				.attr("data-dropdown-menu-item-target", getResourceKey())
				.addClass("dropdown-item");
		
		if("true".equals(dropdownMenu.attr("data-read-only"))) {
			anchor.attr("disabled", "");
		}
		
		if(Objects.equals(value, this.valueElement.val())) {
			anchor.addClass("active");
		}
		
		var liDiv = Html.div("d-flex");
		anchor.appendChild(liDiv);

		var flexInner = Html.div("flex-grow-1");
		flexInner.attr("jad:bundle", bundle)
			.attr("jad:i18n", i18n);
		liDiv.appendChild(flexInner);
		
		li.appendChild(anchor);
		dropdownMenu.appendChild(li);
		emptyLi.addClass("d-none");
	}

}
