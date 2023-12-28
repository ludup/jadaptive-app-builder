window.onload = function() {
	$('.searchTable').click(function(e) {
		e.preventDefault();
		$('#start').val($(this).data('start'));
		$('#form').submit();
	});
	
	$('.searchColumnDropdown .jdropdown-item').click(function(e) {
		e.preventDefault();
		var r = $(this).closest('.searchRow');
		r = r.find('input[name="searchColumn"]').first();
		var f = $(this).data('formvar');
		r.data('formvar', f);
	});
	
	$('.searchColumn').change(function() {
		var currentField = $(this).closest('.searchRow').find('input[name="searchValue"]');
		currentField.val('');
		currentField.attr("name", "unused");
		var currentModifier = $(this).closest('.searchRow').find('input[name="searchModifier"]');
		currentModifier.val('');
		currentModifier.attr("name", "unusedModifier");
		
		var currentParent = currentField.closest('.searchValueField');
		currentParent.addClass("d-none");

		var targetColumn = '.' + $(this).val();
		var v = $(this).data('formvar');
		$(this).val(v);
		var targetField = $(this).closest('.searchRow').find(targetColumn);
		var targetParent = targetField.closest('.searchValueField');
        var targetModifier = targetParent.find(".unusedModifier");
		
		targetField.attr("name", "searchValue");
		targetParent.removeClass("d-none");
		targetModifier.attr("name", "searchModifier");
		
	});
	
	$('form').submit(function() {	
		$('.unused').remove();
		$('.unusedModifier').remove();
	});
};