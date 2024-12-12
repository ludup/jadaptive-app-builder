var JadaptiveForms = {

submit : function(form, callback, invalid) {
	
	var url = form.attr('action');
    
        JadaptiveUtils.startAwesomeSpin($('#saveButton i'), 'fa-save');
		$('.validation').removeClass('validation border border-5 border-danger');
		
        $('.mceEditor').each(function() {
			var editor = tinymce.get($(this).attr('id'));
			editor.save();
		});
		
    	$.ajax({
           type: "POST",
           url: '/app/api/form/validate/' + form.data('resourcekey'),
           cache: false,
           contentType: false,
    	   processData: false,
           data: JadaptiveUtils.processedFormData(form, true),
           success: function(data)
           {
	           if(data.success) {
				   
				   if($('.deletedFile').length > 0) {
					
				   }
				   
	               $.ajax({
			           type: "POST",
			           url: url,
			           cache: false,
			           contentType: false,
			    	   processData: false,
			           data: JadaptiveUtils.processedFormData(form, false),
			           success: function(data)
			           {
							if(callback) {
								callback(data);
							} else {
				               if(data.redirect) {
				                	window.location = data.location;
				               } else if(data.success) {
				                   window.location = '/app/ui/search/' + form.data('resourcekey');
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
			     } else {
					$('#progressModal').modal('hide');
				     //JadaptiveUtils.error($("#feedback"), data.message);
				     
				     $.each(data.errors, function(idx, obj) {
						var field = $('input[name="' + obj.formVariable + '"]').parents(".field").first().find(".form-control");
						if(field.length > 0) {
					     	field.addClass("validation border border-5 border-danger");
					    } else {
							field = $('textarea[name="' + obj.formVariable + '"]');
							if(field.length > 0) {
								field.siblings('.tox-tinymce').addClass("validation border border-5 border-danger");
							}
						}
					  });
				     
				     if(invalid) {
				     	invalid();
				     }
				 }
           },
           complete: function() {
           		JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
           }
         });
}

};

$(document).ready(function() {

	$('form').submit(function(e) {
		e.preventDefault();
	});
	
    $('#saveButton').click(function(e) {
        e.preventDefault();
    
        $('#feedback').remove();
        
    	JadaptiveForms.submit($('form'))
    });
    
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
});