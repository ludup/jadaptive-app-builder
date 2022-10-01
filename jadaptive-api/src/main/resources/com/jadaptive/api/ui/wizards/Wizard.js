var Wizard = (function () {

   	var onNext = null;
	var _self;
    return _self = {
		initStep: function(_onNext) {
			onNext = _onNext;
		},
		start: function(_resourceKey) {
			window.location = "/app/api/wizard/start/" + $('body').attr('jad:wizard');
		},
		next: function() {
			JadaptiveUtils.startAwesomeSpin($('.nextButton i'));
			if(onNext) {
				onNext(function() {
					window.location = "/app/api/wizard/next/" + $('body').attr('jad:wizard');
				}, function() {
					JadaptiveUtils.stopAwesomeSpin($('.nextButton i'));
				});
				return;
			} else if($("form").length > 0) {
				_self.postForm(onNext);
			} else {
				window.location = "/app/api/wizard/next/" + $('body').attr('jad:wizard');
			}
		},
		back: function() {
			JadaptiveUtils.startAwesomeSpin($('.backButton i'));
			window.location = "/app/api/wizard/back/" + $('body').attr('jad:wizard');
		},
		finish: function() {
			JadaptiveUtils.startAwesomeSpin($('#finishButton i'));
			$.getJSON("/app/api/wizard/finish/" + $('body').attr('jad:wizard'), function(data) {
				if(data.success) {
					window.location = "/app/api/wizard/next/" + $('body').attr('jad:wizard');
				} else {
					JadaptiveUtils.error($('#feedback'), data.message);
				}
			}).always(function() {
			    JadaptiveUtils.stopAwesomeSpin($('#finishButton i'));
			});
		},
		postForm: function(success, cancel) {
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
							if(success)
		           				success();
		           			else
		           				window.location = "/app/api/wizard/next/" + $('body').attr('jad:wizard');
		           	   	} else {
		           	    	JadaptiveUtils.error($('#feedback'), data.message);
							JadaptiveUtils.stopAwesomeSpin($('.nextButton i'));
		           	   	}
		           },
		           complete: function() {
					 if(cancel)
						cancel();
		           }
	      		});
		}
    }
})();

$(function() {
	
	if($('.wizardNext').length > 0) {
		$('.wizardNext').click( function(e) {
			e.preventDefault();
			Wizard.next();
		});
	} else {
		$('#nextButton').click( function(e) {
			e.preventDefault();
			Wizard.next();
		});
	}
	
	if($('.wizardBack').length > 0) {
		$('.wizardBack').click( function(e) {
			e.preventDefault();
			Wizard.back();
		});
	} else {
		$('#backButton').click( function(e) {
			e.preventDefault();
			Wizard.back();
		});
	}
	
	if($('.wizardFinish').length > 0) {
		$('.wizardFinish').click( function(e) {
			e.preventDefault();
			Wizard.finish();
		});
	}
	
	if($('.wizardStart').length > 0) {
		$('.wizardStart').click( function(e) {
			e.preventDefault();
			Wizard.start();
		});
	}
	
});