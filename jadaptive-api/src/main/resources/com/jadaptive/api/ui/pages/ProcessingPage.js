$(function() {
					
	$.get('/app/api/process/' + $('#uuid').val()).then(function(res) {
		
		setTimeout(function() {
			window.location = res.message;
		}, 2000);
			
	});
		
});