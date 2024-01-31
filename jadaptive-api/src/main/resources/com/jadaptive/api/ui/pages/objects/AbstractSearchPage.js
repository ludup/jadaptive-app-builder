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
	
	$('.sortColumn').click(function(e) {
		e.preventDefault();
		$('#sortColumn').val($(this).data('column'));
		if($('#sortOrder').val() === 'ASC') {
			$('#sortOrder').val('DESC');
		} else {
			$('#sortOrder').val('ASC');
		}
		$('#form').submit();
		
	});
	
	$('.searchColumn').change(function() {
		var currentField = $(this).closest('.searchRow').find('input[name="searchValue"]');
		var currentParent = currentField.closest('.searchValueField');
		var currentModifier = currentParent.find('input[name="searchModifier"]');
		var currentText = currentParent.find('input[name="searchValueText"]');
		
		currentField.val('');
		currentField.attr("name", "unused");
		currentModifier.val('');
		currentModifier.attr("name", "unusedModifier");
		currentText.attr("name", "unusedText");
		currentParent.addClass("d-none");

		var targetColumn = '.' + $(this).val();
		var v = $(this).data('formvar');
		$(this).val(v);
		var targetField = $(this).closest('.searchRow').find(targetColumn);
		var targetParent = targetField.closest('.searchValueField');
        var targetModifier = targetParent.find(".unusedModifier");
        var targetText = targetParent.find('input[name="unusedText"]');
		
		
		targetParent.removeClass("d-none");
		targetField.attr("name", "searchValue");
		targetModifier.attr("name", "searchModifier");
		targetText.attr("name", "searchValueText");
		
	});

};