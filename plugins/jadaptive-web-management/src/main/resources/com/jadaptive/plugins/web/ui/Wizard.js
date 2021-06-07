var Wizard = (function () {

    var resourceKey = null;
	var onNext = null;
	
    return {
		initStep: function(_resourceKey, _onNext) {
			resourceKey = _resourceKey;	
			onNext = _onNext;
		},
		start: function(_resourceKey) {
			window.location = "/app/api/wizard/start/" + _resourceKey;
		},
		next: function() {
			debugger;
			if(onNext) {
				JadaptiveUtils.startAwesomeSpin($('#nextButton i'), "fa-arrow-circle-right");
				onNext(function() {
					window.location = "/app/api/wizard/next/" + resourceKey;
				}, function() {
					JadaptiveUtils.stopAwesomeSpin($('#nextButton i'), "fa-arrow-circle-right");
				});
				return;
			}
			window.location = "/app/api/wizard/next/" + resourceKey;
		},
		back: function() {
			window.location = "/app/api/wizard/back/" + resourceKey;
		},
		finish: function() {
			window.location = "/app/api/wizard/finish/" + resourceKey;
		}	
    }
})();

$(function() {
	$('#nextButton').click( function() {
		Wizard.next();
	});
	$('#backButton').click( function() {
		Wizard.back();
	});
});