$(document).ready(function() {

	$('form').submit(function(e) {
		e.preventDefault();
	});
	
	$('#saveButton').on('click', function(e) {
		e.preventDefault();
		
        $('#feedback').remove();
        
    	var form = $('form');
    	var url = form.attr('action');
		var button = $(this).find('i');
        JadaptiveUtils.startAwesomeSpin(button);
    	$.ajax({
           type: "POST",
           url: url,
           cache: false,
           contentType: false,
    	   processData: false,
           data: new FormData(form[0]),
           success: function(data)
           {
           	   //window.location.reload();
           },
           complete: function() {
           		JadaptiveUtils.stopAwesomeSpin(button);
           }
         });
    });
});