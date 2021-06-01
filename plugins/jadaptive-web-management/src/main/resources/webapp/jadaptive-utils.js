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
	}
};