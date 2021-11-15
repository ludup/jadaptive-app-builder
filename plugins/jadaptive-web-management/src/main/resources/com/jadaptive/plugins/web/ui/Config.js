$(document).ready(function() {

    $('#saveButton').click(function(e) {
        e.preventDefault();
    
        $('#feedback').remove();
        
    	var form = $('form');
    	var url = form.attr('action');

        JadaptiveUtils.startAwesomeSpin($('#saveButton i'), 'fa-save');

    	$.ajax({
           type: "POST",
           url: url,
           data: JadaptiveUtils.serializeForm(form),
           success: function(data)
           {
           	   if(data.success) {
           	      $('#content').prepend('<p id="feedback" class="alert alert-success col-12"><i class="far fa-check-square"></i> <span id="feedbackText"></span></p>');
           	      $('#feedbackText').text("Configuration saved.");
           	   } else {
           	      $('#content').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="far fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
           	      $('#feedbackText').text(data.message);
           	   }
               
           },
           complete: function() {
           		JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
           }
         });
    });
});