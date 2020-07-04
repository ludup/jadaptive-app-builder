$(document).ready(function(e) {
	
	$(document).on('click', '.dropdown-item', function(e) {
		debugger;
		$(this).closest(".dropdown").find('input[type="hidden"]').val($(this).data('resourcekey')).change();
		$(this).closest(".dropdown").find('input[type="text"]').val($(this).text());
		$(this).closest(".dropdown").find('.dropdown-menu').removeClass('show');
	});
	
	$(document).on('click', '.jdropdown', function(e) {
		debugger;
		e.stopPropagation();
		var el = $(this).closest(".dropdown");
		el = el.find('.dropdown-toggle');
		el.dropdown('toggle');
	});
	
	var createItem = function(menu, key, value) {
		menu.append("<a data-resourcekey=\"" + key + "\" class=\"" 
			+ "dropdown-item\" href=\"#\">" + value + "</a>");
	}
	
	var createDropdown = function(text, url, menu, toggle) {
		
		$.getJSON(url + '?searchField=name&search=' + text, function(data) {
			
			menu.empty();
			menu.removeClass('show');
			if(data.total > 0) {
				$.each(data.rows, function(idx, obj) {
					createItem(menu, obj.uuid, obj.name);
				});
				toggle.dropdown('toggle');
			}
			
			
		});
	};
	
	$('.jsearchText').on('keyup', function(e) {
		
		createDropdown($(this).val(), 
				$(this).data('searchurl'),
				$(this).closest(".dropdown").find('.dropdown-menu'),
				$(this).closest(".dropdown").find('.jsearchText'));
    

	});
});