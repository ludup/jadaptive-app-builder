$(document).ready(function() {

	var form = $('form');
	var action = form.attr('action');
	if(action && action.indexOf("/json-") != -1) {
	    form.submit(function(e) {
	        e.preventDefault();
	    
	        $('#feedback').remove();
	        
	    	var form = $('form');
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
	                     if(data.success) {
	                       window.location = '/app/ui/options';
	                     } else {
	                         $('#content').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="' + $('body').data('iconset') + ' fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
	                         $('#feedbackText').text(data.message);
	                     }
	                 },
	                 complete: function() {
	                      JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
	                 }
	               });
	           }, function(data) {
	              JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
	              $('#content').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="' + $('body').data('iconset') + ' fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
	              $('#feedbackText').text(data.message);
	          });    	
	    });
	}
	
	/*( TODO check this .. is it needed, its in jadaptive-forms.js as well, but maybe it should be here instead? */)
    
    var stashFunc = function(e) {
		e.preventDefault();
		
		$('#feedback').remove();
		
		var url = $(this).data('action');
		var redirect = $(this).data('url');
		if(!redirect) {
			redirect = window.location;
		}
		var form = $('form');
		
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
});