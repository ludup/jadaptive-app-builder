var JadaptiveUtils = {

startAwesomeSpin : function(el, icon, spinner) {
	
	
		if(!icon) {
			var classList = el.prop('class');
			if(classList) {
				$.each(classList.split(/\s+/), function(index, item) {
				    if (item.startsWith('fa-') && !item.startsWith("fa-spin") && !item.startsWith("fa-spinner")
				    && !item.startsWith($('body').data('iconset'))) {
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
	this.feedback(el, message, 'danger', 'fa-exclamation-square');
},
info: function(el, message) {
	this.feedback(el, message, 'info', 'fa-info-square');
},
success: function(el, message) {
	this.feedback(el, message, 'success', 'fa-thumbs-up');
},	
warning: function(el, message) {
	this.feedback(el, message, 'warning', 'fa-warning');
},
feedback: function(el, message, type, icon) {
	$('.feedback').remove();
	if(el.length == 0) {
		var e = $('main');
		var msg = '<p class="feedback alert alert-' + type + '"><i class="' + $('body').data('iconset') + ' ' + icon + '"></i> ' + message + '</p>';
		if(e.length == 0) {
			e = $('#content');
		}
		if(e.length > 0) {
			e.prepend(msg);
		} 
	} else {
		el.append('<p class="feedback alert alert-' + type + '"><i class="' + $('body').data('iconset') + ' ' + icon + '"></i> ' + message + '</p>');
	}
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