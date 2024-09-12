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
	
	var e = $('header');
	if(e.length == 0) {
		e = $('body');
	}
	var msg = '<div class="position-relative"> \
				    <div class="toast-container p-3 top-0 start-50 translate-middle-x"> \
				     <div class="toast align-items-center text-bg-' + type + ' border-0 show" role="alert" aria-live="assertive" aria-atomic="true"> \
				      <div class="d-flex"> \
				       <div class="toast-body"> \
				        <i class="' + icon + ' me-2 ' + $('body').data('iconset') + '"></i> \
				        <span>' + message + '</span> \
				       </div> \
				       <button class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button> \
				      </div> \
				     </div> \
				    </div> \
				   </div>';
	
	if(e.length > 0) {
		e.after(msg);
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
processedFormData: function(form) {
	var fdata = new FormData(form[0]);
	
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