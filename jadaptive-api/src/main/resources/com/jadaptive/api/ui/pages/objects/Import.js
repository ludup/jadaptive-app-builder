$(function() {

//	$.getJSON('/app/api/template/' + $('body').data('resourcekey'), function(data) {
//		if(data.success) {
//			var t = data.resource;
//			
//			$('.columnNumber').each(function(idx) {
//				$(this).text(idx+1);
//			});
//
//			
//			
//		}
//	});
	
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
		$('#uploadForm').attr('action', "/upload/entity/" + t.resourceKey);
		$('#uploadForm').submit();
	});
});