$(function() {
	$('#reject').off('click').on('click', function(event) {
		$('#approved').val('false');
		$('#approveForm').submit();
	});
});