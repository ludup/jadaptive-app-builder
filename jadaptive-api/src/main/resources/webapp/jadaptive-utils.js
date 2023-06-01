var JadaptiveUtils = {

startAwesomeSpin : function(el, icon, spinner) {
	
	
		if(!icon) {
			var classList = el.prop('class');
			if(classList) {
				$.each(classList.split(/\s+/), function(index, item) {
				    if (item.startsWith('fa-') && !item.startsWith("fa-spin") && !item.startsWith("fa-spinner")
				    && !item.startsWith("fa-solid") && !item.startsWith("fa-light")) {
				       icon = item;
					   el.data('faicon', icon);
				    }
				});
			}
		}
		el.removeClass(icon);
		if(spinner) {
			el.addClass(spinner);
		} else {
			el.addClass('fa-spinner');
		}
		el.addClass('fa-spin');
	},
stopAwesomeSpin : function(el, icon, spinner) {
	    if(!icon) {
			icon = el.data('faicon');
		}
		el.removeClass('fa-spin');
		if(spinner) {
			el.removeClass(spinner);
		} else {
			el.removeClass('fa-spinner');
		}
		el.addClass(icon);
	},
error: function(el, message) {
	el.empty();
	el.append('<p class="alert alert-danger"><i class="fa-solid fa-exclamation-square"></i> ' + message + '</p>');
},
info: function(el, message) {
	el.empty();
	el.append('<p class="alert alert-info"><i class="fa-solid fa-info"></i> ' + message + '</p>');
},
success: function(el, message) {
	el.empty();
	el.append('<p class="alert alert-success"><i class="fa-solid fa-thumbs-up"></i> ' + message + '</p>');
},
warning: function(el, message) {
	el.empty();
	el.append('<p class="alert alert-warning"><i class="fa-solid fa-warning"></i> ' + message + '</p>');
},
checkBlank: function(elements) {
	var empty = false;
	$.each(elements, function(idx, el) {
		if(el.val().trim() === '') {
			empty = true;
			return true;
		}
	});
	return !empty;
},
toggleDropdown: function(element) {
	var el = element.closest(".dropdown");
		el = el.find('.dropdown-toggle');
		const dropdownToggleEl = el[0];
		const dropdownList = new bootstrap.Dropdown(dropdownToggleEl);
		dropdownList.toggle();	
},
serializeForm: function(form) {
	form.find('.jadaptive-select option').prop('selected', true);
    var results = form.serialize();
	form.find('.jadaptive-select option').prop('selected', false);
    return results;
} 
};