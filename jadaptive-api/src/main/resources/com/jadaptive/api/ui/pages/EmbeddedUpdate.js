$(document).ready(function() {

    $('#saveButton').click(function(e) {
        e.preventDefault();
    
        $('#feedback').remove();
        
    	var form = $('form');
    	var action = $(this).data('action');
    	var url = $(this).data('url');
    	
        JadaptiveUtils.startAwesomeSpin($('#saveButton i'), 'fa-save');
        
    	$.ajax({
           type: "POST",
           url: action,
           cache: false,
           contentType: false,
    	   processData: false,
           data: new FormData(form[0]),
           success: function(data)
           {
               if(data.success) {
                   window.location = url;
               } else {
               	   $('#content').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="far fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
               	   $('#feedbackText').text(data.message);
               }
           },
           complete: function() {
           		
           }
         });
    });
  
});