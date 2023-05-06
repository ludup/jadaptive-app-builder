$(function() {
	
	$('#extendObjectDropdown .dropdown-item').click(function(e) {
		var resourceKey = $('input[name="resourceKey"]').val();
		var extension =  $(this).data('resourcekey');

		e.preventDefault();

	    var form = $('#objectForm');
	    var actionUrl = '/app/api/form/extend/add/' + resourceKey + '/' + extension;
	    
	    $.ajax({
	        type: "POST",
	        url: actionUrl,
	        cache: false,
            contentType: false,
    	    processData: false,
            data: new FormData(form[0]),
	        success: function(data)
	        {
				if(data.success) {
	          		window.location = data.message;
	          	} else {
					window.location.reload();
				}
	        }
	    });
	
		
	});
	
	$('.removeExtension').click(function(e) {
		var resourceKey = $('input[name="resourceKey"]').val();
		var extension =  $(this).data('ext');

		e.preventDefault();

	    var form = $('#objectForm');
	    var actionUrl = '/app/api/form/extend/remove/' + resourceKey + '/' + extension;
	    
	    $.ajax({
	        type: "POST",
	        url: actionUrl,
	        cache: false,
            contentType: false,
    	    processData: false,
            data: new FormData(form[0]),
	        success: function(data)
	        {
				if(data.success) {
	          		window.location = data.message;
	          	} else {
					window.location.reload();
				}
	        }
	    });
	
		
	});
				
});