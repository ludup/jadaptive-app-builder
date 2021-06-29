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
			if(onNext) {
				JadaptiveUtils.startAwesomeSpin($('.nextButton i'));
				onNext(function() {
					window.location = "/app/api/wizard/next/" + resourceKey;
				}, function() {
					JadaptiveUtils.stopAwesomeSpin($('.nextButton i'));
				});
				return;
			}
			window.location = "/app/api/wizard/next/" + resourceKey;
		},
		back: function() {
			window.location = "/app/api/wizard/back/" + resourceKey;
		},
		finish: function() {
			JadaptiveUtils.startAwesomeSpin($('#finishButton i'));
			$.getJSON("/app/api/wizard/finish/" + resourceKey, function(data) {
				if(data.success) {
					window.location = "/app/api/wizard/next/" + resourceKey;
				} else {
					JadaptiveUtils.error(data.message);
				}
			}).always(function() {
			    JadaptiveUtils.stopAwesomeSpin($('#finishButton i'));
			});
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