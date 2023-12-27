window.onload = function() {
	$('.searchTable').click(function(e) {
		e.preventDefault();
		$('#start').val($(this).data('start'));
		$('#form').submit();
	});
	
	$('#searchColumnDropdown .jdropdown-item').click(function(e) {
		e.preventDefault();
		$('#searchColumn').data('formvar', $(this).data('formvar'));
	});
	
	$('#searchColumn').change(function() {
		var e = $('#searchValueHolder').find('input[name="searchValue"]');
		e.val('');
		var m = $('#searchValueHolder').find('input[name="searchModifier"]');
		m.val('');
		
		var p = e.closest('.searchValueField');
		var x = p.find(".unusedModifier");
		
		var column = '#' + $(this).val();
		$('#searchColumn').val($(this).data('formvar'));
		var n = $(document).find(column);
		var f = n.closest('.searchValueField');

		e.attr("name", "unused");
		if(m){
		   m.attr("name", "unusedModifier");
		}
		if(x) {
			x.attr("name", "searchModifier");
		}
		p.addClass("d-none");
		n.attr("name", "searchValue");
		f.removeClass("d-none");
	});
};