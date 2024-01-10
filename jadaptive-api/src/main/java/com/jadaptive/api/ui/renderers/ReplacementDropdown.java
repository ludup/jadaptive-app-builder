package com.jadaptive.api.ui.renderers;

import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageHelper;

public class ReplacementDropdown extends InputRender {
	
	Element dropdownMenu;
	Element dropdownInput;
	Element valueElement;
	boolean up;
	boolean dark;
	String bundle;
	String icon = "fa-code";
	String iconGroup = "fa-solid";

	public ReplacementDropdown(String resourceKey, String bundle) {
		super(resourceKey);
		this.bundle = bundle;
	}

	public ReplacementDropdown icon(String icon) {
		this.icon = icon;
		return this;
	}
	
	public ReplacementDropdown group(String icon) {
		this.icon = icon;
		return this;
	}
	
	public ReplacementDropdown up() {
		this.up = true;
		return this;
	}
	public ReplacementDropdown down() {
		this.up = false;
		return this;
	}
	
	public ReplacementDropdown dark() {
		this.dark = true;
		return this;
	}
	
	@Override
	public Element renderInput() {

		return new Element("span")
				.addClass("input-group-text")
				.appendChild(dropdownInput = new Element("div")
						.attr("id", String.format("%sDropdown", resourceKey))
						.addClass("position-relative dropdown" + (up ? " dropup" : ""))
					.appendChild(valueElement = new Element("input")
							.attr("name", resourceKey)
							.attr("type", "hidden"))
					.appendChild(new Element("a")
							.attr("id", String.format("%sText", resourceKey))
							.attr("href", "#")
							.attr("role", "button")
							.addClass("h-100 text-decoration-none" + (dark ? " text-light" : ""))
							.attr("data-bs-toggle", "dropdown")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false")
					.appendChild(new Element("i")
							.addClass(String.format("%s %s me-1 text-decoration-none", iconGroup, icon)))
					.appendChild(dropdownMenu = new Element("div")
							.addClass("dropdown-menu dropdown-size" + (dark ? " dropdown-menu-dark" : ""))
							.attr("aria-labelledby", String.format("%sDropdown", resourceKey)))));
	}

	public void addInputValue(String value, String bundle, String name) {
		
		if(Objects.isNull(dropdownMenu)) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", resourceKey)));
		}
		dropdownMenu.appendChild(new Element("a").attr("href", "#")
				.appendChild(Html.i18n(bundle, name + ".name"))
				.attr("data-resourcekey", value)
				.addClass("replacement-item dropdown-item"));
	}
		
	public void addInputValue(String value, String name) {
		
		if(Objects.isNull(dropdownMenu)) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", resourceKey)));
		}
		dropdownMenu.appendChild(new Element("a").attr("href", "#")
				.text(name)
				.attr("data-resourcekey", value)
				.addClass("replacement-item dropdown-item"));
	}
	
	public void addSection(String name) {
		
		if(Objects.isNull(dropdownMenu)) {
			dropdownInput.appendChild(dropdownMenu = new Element("div")
					.addClass("dropdown-menu dropdown-size")
					.attr("aria-labelledby", String.format("%sDropdown", resourceKey)));
		}
		dropdownMenu.appendChild(PageHelper.createAnchor("#", name)
				.addClass("dropdown-item small text-muted")
				.attr("disabled", "disabled"));
	}

	public void renderTemplateReplacements(List<String> replacementVars) {
		
		for(String variable : replacementVars) {
			addInputValue(variable, variable);			
		}
		
	}
	
}
