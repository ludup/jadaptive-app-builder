$(function() {
	
	$('#addColumn').off('click');
	$('#addColumn').on('click', function() {
		debugger;
		$('#csvColumns').append($('.csvColumn').last().clone());
	});
	
	$(document).on('click', '.deleteColumn', function() {
		if($('.csvColumn').length > 1) {
			$(this).parents(".row").first().remove();
		}
	});
	
	$('#importButton').off('click');
	$('#importButton').on('click', function(e) {
		e.preventDefault();
		debugger;
		var orderedFields = '';
		$('.orderedFields').each(function(idx) {
			if(idx > 0) {
				orderedFields += ',';
			}
			orderedFields += $(this).val();
		});
		debugger;
		$('#orderedFields').val(orderedFields);
		$('#uploadForm').attr('action', "/upload/entity/" + $('body').data('resourcekey'));
		$('#uploadForm').submit();
	});
});