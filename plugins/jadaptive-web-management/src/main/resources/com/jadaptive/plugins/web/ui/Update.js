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
           cache: false,
           contentType: false,
    	   processData: false,
           data: new FormData(form[0]),
           success: function(data)
           {
               if(data.redirect) {
                	window.location = data.location;
               } else if(data.success) {
                   window.location = '/app/ui/search/' + form.data('resourcekey');
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
    
    $('.stash').click(function(e) {
		e.preventDefault();
		
		$('#feedback').remove();
		
		var url = $(this).data('action');
		var redirect = $(this).data('url');
		var form = $('form');
		
		$.ajax({
           type: "POST",
           url: url,
           data: JadaptiveUtils.serializeForm(form),
           success: function(data)
           {
                if(data.success) {
                   window.location = redirect;
               } else {
               	   $('#content').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="far fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
               	   $('#feedbackText').text(data.message);
               }
           },
           complete: function() {
           		
           }
         });
		
		
	});
	
    $('.checkExit').click(function(e) {
        e.preventDefault();
        var _self = $(this);
    	bootbox.confirm({
    		message: "Are you sure you want to exit?",
		    buttons: {
		        confirm: {
		            label: 'Yes',
		            className: 'btn-success'
		        },
		        cancel: {
		            label: 'No',
		            className: 'btn-danger'
		        }
		    },
		    callback: function (result) {
		        if(result)
		        {
		        	window.location = _self.attr('href');
		        }
		    }
		});
    });
});