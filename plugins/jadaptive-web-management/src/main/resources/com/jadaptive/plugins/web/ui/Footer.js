$(function() {
	
	$(document).on('click', '.jdropdown-item', function(e) {
		e.preventDefault();
		$(this).closest(".dropdown").find('input[type="hidden"]').val($(this).data('resourcekey')).change();
		$(this).closest(".dropdown").find('input[type="text"]').val($(this).text());
		$(this).closest(".dropdown").find('.jdropdown-text').html($(this).text());
		$(this).closest(".dropdown").find('.dropdown-menu').removeClass('show');
	});
	
	$(document).on('click', '.jdropdown', function(e) {
		e.stopPropagation();
		var el = $(this).closest(".dropdown");
		el = el.find('.dropdown-toggle');
		el.dropdown('toggle');
	});
	
	function duplicate(text, list) {
		var found = false;
		list.find('option').each(function(idx, obj) {
			if($(obj).val() === text) {
				found = true;
				return true;
			}
		});
		return found;
	};
	
	$(document).on('click', '.multipleTextAdd', function(e) {
		e.stopPropagation();
		var source = $(this).closest('.multipleTextInput').find('.multipleTextSource');
		var target = $(this).closest('.multipleTextInput').find('.multipleTextTarget');
		if(source.val() !== '' && !duplicate(source.val(), target)) {
			target.append('<option value="' + source.val() + '">' + source.val() + '</option>');
			source.val('');
		}
	});
	
	$(document).on('keyup', '.multipleTextSource', function(e) {
		e.stopPropagation();
		if (e.keyCode === 13) {
			var source = $(this).closest('.multipleTextInput').find('.multipleTextSource');
			var target = $(this).closest('.multipleTextInput').find('.multipleTextTarget');
			if(source.val() !== '' && !duplicate(source.val(), target)) {
				target.append('<option value="' + source.val() + '">' + source.val() + '</option>');
				source.val('');
			}
		}
	});
	
	$(document).on('click', '.multipleTextRemove', function(e) {
		e.preventDefault();
		var source = $(this).closest('.multipleTextInput').find('.multipleTextTarget');
		source.children('option:selected').detach();
	});
	
	$(document).on('click', '.multipleSelectAdd', function(e) {
		e.stopPropagation();
		var source = $(this).closest('.row').find('.multipleSelectSource');
		var target = $(this).closest('.row').find('.multipleSelectTarget');
		source.children('option:selected').detach().appendTo(target);
	});
	
	$(document).on('click', '.multipleSelectRemove', function(e) {
		e.stopPropagation();
		var source = $(this).closest('.row').find('.multipleSelectTarget');
		var target = $(this).closest('.row').find('.multipleSelectSource');
		source.children('option:selected').detach().appendTo(target);
	});
	
	$(document).on('keyup', '.multipleSearchInputText', function(e) {
		createDropdown($(this).val(), $(this).data('url'), 
							$(this).data('field'),
							$(this).data('id'),
							$(this).closest(".dropdown").find('.dropdown-menu'),
							$(this),
							'multipleSearchInputSelection');
	});
	
	$(document).on('click', '.multipleSearchDelete', function(e) {
		e.preventDefault();
		$(this).closest(".multipleSearchInput").find('.multipleSearchTarget option:selected').remove();
	});
	
	
	$(document).on('click', '.multipleSearchInputSelection', function(e) {
		e.preventDefault();
		var uuid = $(this).data('resourcekey');
		var select = $(this).closest(".multipleSearchInput").find('select');
		var exists = false;
		select.children('option').each(function(idx, obj) { 
			if($(obj).val() === uuid) {
				exists = true;
				return false;
			}
		});
		
		if(!exists) {
			select.append('<option value="' + uuid + '">' + $(this).text() + '</option>');
		}
	}); 
	
	var createItem = function(menu, key, value, selectionClass) {
		menu.append('<a data-resourcekey="' + key + '" class="' + selectionClass + ' dropdown-item" href="#">' + value + '</a>');
	}
	
	var createDropdown = function(text, url, field, id, menu, toggle, selectionClass) {

		$.getJSON(url + '?searchField=' + field + '&searchValue=' + text, function(data) {
			
			menu.empty();
			menu.removeClass('show');
			if(data.total > 0) {
				$.each(data.rows, function(idx, obj) {
					createItem(menu, obj[id], obj[field], selectionClass);
				});
				toggle.dropdown('toggle');
			}
			
			
		});
	};
	
	$('.jsearchText').on('keyup', function(e) {
		
		createDropdown($(this).val(), 
				$(this).data('url'),
				$(this).closest(".dropdown").find('.dropdown-menu'),
				$(this).closest(".dropdown").find('.jsearchText'));
    

	});

	$('input[name="theme"').on('change', function(e) {
		document.cookie = "userTheme=" + $(this).val() + '; path=/; expires=Tue, 01 Jan 2038 00:00:00 UTC;';
		window.location.reload();
	});
});