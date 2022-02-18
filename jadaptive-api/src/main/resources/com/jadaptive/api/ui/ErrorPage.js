$(function() {
	$('#toggleStack').click(function(e) {
		e.preventDefault();
		if($('#stack').is(':visible')) {
			$('#stack').hide();
		} else {
			$('#stack').show();
		}
	})
});