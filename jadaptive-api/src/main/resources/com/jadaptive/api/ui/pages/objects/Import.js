$(function() {

	$.getJSON('/app/api/template/' + $('body').data('resourcekey'), function(data) {
		if(data.success) {
			var t = data.resource;
			
			$('.columnNumber').each(function(idx) {
				$(this).text(idx+1);
			});

			$('#addColumn').off('click');
			$('#addColumn').on('click', function(e) {
				var column = $('.csvColumn').length+1;
				$('.csvColumns').append('<div id="c' + column + '" class="row"><div class="csvColumn col-10"><select class="orderedFields form-control" id="templateFields' + column + '"><option value="">&lt;Unused&gt;</option><option value="UUID">UUID</option></select></div><div class="col-2 text-start m-auto"><a href="#" id="d' + column + '"><i class="fa-solid fa-trash-alt"></i></a></div></div></div>');
				debugger;
				$.each(t.fields, function(idx, obj) {
					$('select[id="templateFields' + column  + '"]').append('<option value="' + obj.resourceKey + '">' + obj.name + "</option>");
				});
				$('.columnNumber').each(function(idx) {
					$(this).text(idx+1);
				});
				$('#d' + column).click(function(e) {
					$('#c' + column).remove();
					$('.columnNumber').each(function(idx) {
						$(this).text(idx+1);
					});
				});
			});
		
			$('#addColumn').click();
			
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
			
		}
	});
});