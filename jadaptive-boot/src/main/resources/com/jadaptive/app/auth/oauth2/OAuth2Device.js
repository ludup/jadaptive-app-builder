$(function() {
	$('#userCode').on('input', function(event) {
		if($(this).val().length == $(this).attr('maxlength')) {
			this.form.submit();
		}
	});
	$('#reject').off('click').on('click', function(event) {
		$('#approved').val('false');
		$('#deviceForm').submit();
	});
});