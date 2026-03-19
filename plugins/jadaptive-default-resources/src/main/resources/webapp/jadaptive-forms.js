var JadaptiveForms = (function () {

return {

submit : function(form, callback, invalid) {
	form =$(form);
	var url = form.attr('action');
    
	JadaptiveUtils.startAwesomeSpin($('#saveButton i'), 'fa-save');
	
	JadaptiveUtils.validate(form, function() {
	   
       $.ajax({
           type: "POST",
           url: url,
           cache: false,
           contentType: false,
    	   processData: false,
           data: JadaptiveUtils.processedFormData(form, false),
           success: function(data)
           {
				$('.mdeEditor').each(function() {
					$(this).data('mde').clearAutosavedValue();
				});
				
				if(callback) {
					callback(data);
				} else {
	               if(data.redirect) {
	                   window.location = data.location;
	               } else if(data.success) {
					   JadaptiveUtils.success($("#feedback"), data.message);
	               } else {
	               	   JadaptiveUtils.error($("#feedback"), data.message);
	               }
               }
           },
           complete: function() {
				$('#progressModal').modal('hide');
				JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
		   },
           xhr: function() {
		        var xhr = new window.XMLHttpRequest();

				if($('input[type="file"]').length > 1) {
                    
                    $('#progressModal').modal('show');
			        xhr.upload.addEventListener("progress", function(evt){
			            if (evt.lengthComputable) {
			                var percentComplete = Math.round((evt.loaded / evt.total) * 100);
						  	$('#progressBar.auto-progress-bar').width(percentComplete + "%");
			            }
			       }, false);
		       }
		       
		       return xhr;
		   }
         });
	}, function(e) {
		
		if(invalid) {
			invalid(e);		
		} 
		
		JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
	});
	
    }
  }

})();

var form = $('form');
var action = form.attr('action');
if(action && action.indexOf("/json-") != -1) {
	/* This form is NOT a basic HTML post, and so posts using JSON and supports
	   pre-validation and auto-stashing */
	
	var saveButton = $('#saveButton');
	debugger;

	if(saveButton.attr('type') == 'submit') {
		/* The form has been converted to new style submit-button-inside-form */
		form.submit(function(e) {
			e.preventDefault();
			JadaptiveForms.submit(this);
		});		
	}
	else {
		/* The forms submit button is not really a submit button, and it exists outside 
		   of the form, do it the old way */
	    form.submit(function(e) {
		   e.preventDefault();
	    });
	
	    $('#saveButton').click(function(e) {
	        e.preventDefault();
	        $('#feedback').remove();
			JadaptiveForms.submit(form);
	    });
	}
}

/* Stashing currently requires Javascript form submission, as the submit may be initiated
   by things other than the save button, such as auto-saving on input change. */
   
var stashFunc = function(e) {
	e.preventDefault();
	
	$('#feedback').remove();
	
	var url = $(this).data('action');
	var redirect = $(this).data('url');
	if(!redirect) {
		redirect = window.location;
	}
	
	$.ajax({
       type: "POST",
       url: url,
       cache: false,
       contentType: false,
	   processData: false,
       data: new FormData(form[0]),
       success: function(data)
       {
            if(data.success) {
               window.location = redirect;
           } else {
           	   $('#content').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="' + $('body').data('iconset') + ' fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
           	   $('#feedbackText').text(data.message);
           }
       },
       complete: function() {
       		
       }
     });
};

$('.stash').click(stashFunc);         
$('.processAutosave').on('autosave', stashFunc);   

$('input').change(function(e) {
    var auto = $(this).closest('.processAutosave');
	if(auto.length > 0) {
		if(!$(this).hasClass('jsearchText')) {
			$(this).data('action', auto.data('action'));
			$('.processAutosave').first().trigger('autosave');
		}
	}
});