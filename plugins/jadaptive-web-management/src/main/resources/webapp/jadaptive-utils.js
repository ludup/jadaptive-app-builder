var JadaptiveUtils = {

startAwesomeSpin : function(el, icon, spinner) {
		el.removeClass(icon);
		if(spinner) {
			el.addClass(spinner);
		} else {
			el.addClass('fa-spinner');
		}
		el.addClass('fa-spin');
	},
stopAwesomeSpin : function(el, icon, spinner) {
		el.removeClass('fa-spin');
		if(spinner) {
			el.removeClass(spinner);
		} else {
			el.removeClass('fa-spinner');
		}
		el.addClass(icon);
	},
error: function(el, message) {
	el.append('<p class="alert alert-danger"><i class="far fa-exclamation-square"></i> ' + message + '</p>');
},
info: function(el, message) {
	el.append('<p class="alert alert-info"><i class="far fa-info"></i> ' + message + '</p>');
},
success: function(el, message) {
	el.append('<p class="alert alert-success"><i class="far fa-thumbs-up"></i> ' + message + '</p>');
},
warning: function(el, message) {
	el.append('<p class="alert alert-warning"><i class="far fa-warning"></i> ' + message + '</p>');
}
};