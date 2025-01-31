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
           url: url,
           cache: false,
           contentType: false,
    	   processData: false,
           data: new FormData(form[0]),
           success: function(data)
           {
			if(data.success) {
           	    JadaptiveUtils.success($('#feedback'), '${userInterface:configuration.saved}');
			} else {
				JadaptiveUtils.error(data.message);
			}
           },
           complete: function() {
           		JadaptiveUtils.stopAwesomeSpin($('#saveButton i'), 'fa-save');
           }
         });
    });
});