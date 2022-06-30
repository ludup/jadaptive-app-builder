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
					JadaptiveUtils.error($('#feedback'), data.message);
				}
			}).always(function() {
			    JadaptiveUtils.stopAwesomeSpin($('#finishButton i'));
			});
		}	
    }
})();

$(function() {
	$('#nextButton').click( function() {
		if($('.wizardForm').length > 0) {		
			$.ajax({
	           type: "POST",
	           url: $('form').attr('action'),
	           cache: false,
	           contentType: false,
	    	   processData: false,
	           data: new FormData($("form")[0]),
	           dataType: 'json',
	           success: function(data)
	           {
	           	   	if(data.success) {
						Wizard.next();
	           	   	} else {
	           	    	JadaptiveUtils.error($('#feedback'), data.message);
	           	   	}
	           },
	           complete: function() {
					cancel();
	           }
	      });
		} else {
			Wizard.next();
		}
	});
	$('#backButton').click( function() {
		Wizard.back();
	});
});