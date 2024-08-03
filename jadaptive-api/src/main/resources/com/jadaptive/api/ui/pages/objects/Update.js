$(document).ready(function() {

	$('form').submit(function(e) {
		e.preventDefault();
	});
	
    $('#saveButton').click(function(e) {
        e.preventDefault();
    
        $('#feedback').remove();
        
    	var form = $('form');
    	var url = form.attr('action');
    
        JadaptiveUtils.startAwesomeSpin($('#saveButton i'), 'fa-save');

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
	               $.ajax({
			           type: "POST",
			           url: url,
			           cache: false,
			           contentType: false,
			    	   processData: false,
			           data: JadaptiveUtils.processedFormData(form, false),
			           success: function(data)
			           {
			               if(data.redirect) {
			                	window.location = data.location;
			               } else if(data.success) {
			                   window.location = '/app/ui/search/' + form.data('resourcekey');
			               } else {
			               	   JadaptiveUtils.error(data.message);
			               }
			           },
			           complete: function() {
			           		JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
			           },
			           xhr: function() {
					        var xhr = new window.XMLHttpRequest();
					
							$('body').append('<!-- Modal --> \
			                 <div class="modal fade" id="progressModal" data-bs-backdrop="static" \
			                               data-bs-keyboard="false" tabindex="-1" \
			                               aria-labelledby="staticBackdropLabel" aria-hidden="true"> \
								  <div class="modal-dialog modal-dialog-centered"> \
								    <div class="modal-content"> \
								      <div class="modal-body"> \
											<div id="uploadProgress"> \
									   			<div class="progress mx-auto my-1 w-100"> \
													<div id="progressBar" class="progress-bar" role="progressbar" \
															 aria-valuenow="50" aria-valuemin="0" aria-valuemax="100"></div> \
												</div> \
										      </div> \
										     <div class="mt-1"> \
										        <span class="form-text text-muted">${userInterface:uploadingFiles.text}</span> \
										     </div> \
								      </div> \
								    </div> \
								  </div> \
								</div>');
			
			                $('#progressBar').css("width", 50).attr('aria-valuenow', "50");
			                $('#progressModal').modal('show');
			                
					        // Upload progress
					        xhr.upload.addEventListener("progress", function(evt){
					            if (evt.lengthComputable) {
					                var percentComplete = Math.round((evt.loaded / evt.total) * 100);
								  $('#progressBar').width(percentComplete + "%")
								  		.attr('aria-valuenow', percentComplete);
					            }
					       }, false);
					       
					       return xhr;
					   }
			         });
			     } else {
				     JadaptiveUtils.error(data.message);
				 }
           },
           complete: function() {
           		JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
           }
         });
    });
    
    $('.stash').click(function(e) {
		e.preventDefault();
		
		$('#feedback').remove();
		
		var url = $(this).data('action');
		var redirect = $(this).data('url');
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
	});

});