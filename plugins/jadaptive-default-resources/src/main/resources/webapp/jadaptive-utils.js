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
},

setCookie: function(name,value,days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
},
getCookie: function(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
},
hasCookie: function(name) {
	return this.getCookie(name)!= null;
},
eraseCookie: function(name) {   
    document.cookie = name +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 UTC;';
},
processedFormData: function(form, removeFiles) {
	var fdata = new FormData(form[0]);
	
	if(removeFiles) {
		form.find('input[type="file"]').each(function() {
			fdata.delete($(this).attr('name'));
		});
	}
	
	form.find('.processDepends').each(function() {
		var dependsOn = $(this).data('depends-on');
		var dependsValue = $(this).attr('data-depends-value');

		var allInput = $('#' + dependsOn);
		if (!allInput.length) {
			allInput = $('input[name=' + dependsOn + ']');
		}
		var matchValues = dependsValue.split(',');
		var matches = false;
		allInput.each(function(i, input) {
			input = $(input);
			$.each(matchValues, function(i, obj) {

				var expectedResult = !obj.startsWith("!");
				if (!expectedResult) {
					obj = obj.substring(1);
				}
				var value;
				if (input.attr('type') === 'radio') {
					value = input.is(':checked') ? input.val() : '';
				}
				else if (input.attr('type') === 'checkbox') {
					value = input.is(':checked').toString();
				} else {
					value = input.val();
				}
				if (obj == value) {
					matches = expectedResult;
					return true;
				}
				return false;
			});
			if (matches) {
				return true;
			}
		});
		if (!matches) {
			fdata.delete($(this).find('input').attr('name'));
		}
	});
	
	return fdata;
}
};