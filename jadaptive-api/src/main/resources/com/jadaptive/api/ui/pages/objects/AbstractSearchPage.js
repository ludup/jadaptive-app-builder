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
	
	/* if the search field is the default, then set the value of 
       simpleSearchValue to be the same as searchValue. If it is
	   not the default, show the advanced search immediately, and leave
	   the simple search field blank.
		
	   This means we can have simple search without changing the
	   server logic and don't need to store whether search is simple
	   mode or advanced mode
	*/
	var srchCol = $('input[name=searchColumn]');
	var currentField = srchCol.val();
	var defaultCol = srchCol.data('default-search-column');
	var simpleSrchVal = $('[name=simpleSearchValue]');
	var srchVal = $('[name=searchValue]');
	if(currentField == defaultCol) {
		simpleSrchVal.val(srchVal.val());
	}
	else {
		$('#searchFormContainer').addClass('show');
		$('#simpleSearch').removeClass('show');
	}
	var onSrchUpd = function() { srchVal.val(simpleSrchVal.val()); };
	simpleSrchVal.on('focusout', onSrchUpd);
	simpleSrchVal.on('change', onSrchUpd);
	
	$('#searchFormContainer').on('show.bs.collapse', function() {
		new bootstrap.Collapse($('#simpleSearch')[0]).hide();
	});
	$('#searchFormContainer').on('hide.bs.collapse', function() {
		new bootstrap.Collapse($('#simpleSearch')[0]).show();
		if(srchCol.val() == defaultCol) {
			simpleSrchVal.val(srchVal.val());
		}
		else {
			$('[data-resourcekey=' + defaultCol + ']').click();
			simpleSrchVal.val('');
		}
	});
};