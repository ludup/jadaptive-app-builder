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
           data: JadaptiveUtils.serializeForm(form),
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