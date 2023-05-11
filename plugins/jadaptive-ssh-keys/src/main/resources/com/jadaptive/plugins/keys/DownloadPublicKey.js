$(function() {
	$('#downloadKey').click(function(e) {
		window.location = "/generate/download";
	});
	
	setTimeout(function() {
		$('#downloadKey').trigger('click');
	}, 2000);
});