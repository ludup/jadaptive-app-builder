$(function() {
	$('#toggleStack').click(function(e) {
		e.preventDefault();
		if($('#stack').is(':visible')) {
			$('#stack').addClass('d-none');
		} else {
			$('#stack').removeClass('d-none');
		}
	})
});