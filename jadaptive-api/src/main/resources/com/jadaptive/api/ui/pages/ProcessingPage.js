$(function() {
					
	$.get('/app/api/process/' + $('#uuid').val()).then(function(res) {
		window.location = res.message;	
	});
		
});