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
			if(onNext) {
				JadaptiveUtils.startAwesomeSpin($('.nextButton i'));
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
	
	$('#nextButton').click( function() {
		Wizard.next();
	});
	
	$('#backButton').click( function() {
		Wizard.back();
	});
});