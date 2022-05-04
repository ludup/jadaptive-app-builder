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
		const dropdownToggleEl = el[0];
		const dropdownList = new bootstrap.Dropdown(dropdownToggleEl);
		dropdownList.toggle();
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
	
	function addTag(_this) {
		var source = _this.closest('.multipleTagInput').find('.multipleTagSource');
		var target = _this.closest('.multipleTagInput').find('.multipleTagTarget');
		if(source.val() !== '' && !duplicate(source.val(), target)) {
			target.append('<option class="badge bg-primary me-3" value="' + source.val()
				 + '" class="me-1"><span class="pe-1">' + source.val() + '</span><a href="#" class="jadaptive-tag text-light"><i class="far fa-times"></i></a></option>');
			source.val('');
		}
	}
	
	function addText(_this) {
		var source = _this.closest('.multipleTextInput').find('.multipleTextSource');
		var target = _this.closest('.multipleTextInput').find('.multipleTextTarget');
		if(source.val() !== '' && !duplicate(source.val(), target)) {
			target.append('<option value="' + source.val() + '">' + source.val() + '</option>');
			source.val('');
		}
	}
	
	$(document).on('click', '.multipleTextAdd', function(e) {
		e.stopPropagation();
		addText($(this));
	});
	
	$(document).on('click', '.multipleTagAdd', function(e) {
		e.stopPropagation();
		addTag($(this));
	});
	
	$(document).on('keyup', '.multipleTextSource', function(e) {
		
		if (e.keyCode === 13) {
			e.stopPropagation();
			addText($(this));
		}
	});
	
	$(document).on('keyup', '.multipleTagSource', function(e) {
		
		if (e.keyCode === 13) {
			e.stopPropagation();
			addTag($(this));
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
	
	$(document).on('keyup', '.collectionSearchInputText', function(e) {
		createDropdown($(this).val(), $(this).data('url'), 
							$(this).data('field'),
							$(this).data('id'),
							$(this).closest(".dropdown").find('.dropdown-menu'),
							$(this),
							'collectionSearchInputSelection');
	});
	
	$(document).on('keyup', '.collectionTextInputText', function(e) {
		
		if (e.keyCode === 13) {
			e.stopPropagation();
			
			var select = $(this).closest(".collectionTextInput").find('table');
			var name = $(this).closest('.collectionTextInput').data('resourcekey');
			
			select.append('<tr><input type="hidden" name="' + name + '" value="' + $(this).val() + '"><td>' + $(this).val() + '</td><td>' +
//							'<a href="#" class="collectionSearchUp"><i class="far fa-fw fa-arrow-up me-2"></i></a>'  +
//							'<a href="#" class="collectionSearchDown"><i class="far fa-fw fa-arrow-down me-2"></i></a>' +
							'<a href="#" class="collectionSearchDelete"><i class="far fa-fw fa-trash me-2"></i></a>' + 
						  '</td></tr>');
		    $(this).val('');
		}

	});
	
	$(document).on('click', '.collectionSearchDelete', function(e) {
		e.preventDefault();
		$(this).closest("tr").remove();
	});
	
	$(document).on('click', '.collectionSearchUp', function(e) {
		e.preventDefault();
		var row = $(this).closest("tr");
		var prev = row.prev();
		if(prev.length > 0) {
			prev.before(row);
		}

//		$(this).closest("tbody").find('.fa-arrow-up').show();
//		$(this).closest("tbody").find('tr').find('.fa-arrow-up').first().hide();
//		$(this).closest("tbody").find('.fa-arrow-down').show();
//		$(this).closest("tbody").find('tr').find('.fa-arrow-down').last().hide();
	});
	
	$(document).on('click', '.collectionSearchDown', function(e) {
		e.preventDefault();
		var row = $(this).closest("tr");
		var next = row.next();
		if(next.length > 0) {
			next.after(row);
		}
//		$(this).closest("tbody").find('.fa-arrow-up').show();
//		$(this).closest("tbody").find('tr').find('.fa-arrow-up').first().hide();
//		$(this).closest("tbody").find('.fa-arrow-down').show();
//		$(this).closest("tbody").find('tr').find('.fa-arrow-down').last().hide();
	});
	
//	$('.collectionSearchInput').each(function(idx, obj) {
//		$(obj).find("tbody").find('.fa-arrow-up').show();
//		$(obj).find("tbody").find('tr').find('.fa-arrow-up').first().hide();
//		$(obj).find("tbody").find('.fa-arrow-down').show();
//		$(obj).find("tbody").find('tr').find('.fa-arrow-down').last().hide();	
//	});
	
	$(document).on('click', '.collectionSearchInputSelection', function(e) {
		e.preventDefault();
		var uuid = $(this).data('resourcekey');
		var select = $(this).closest(".collectionSearchInput").find('table');
		var exists = false;
		select.find('tr').each(function(idx, obj) { 
			var thisUUID = $(obj).find('input').first().attr('value');
			if(thisUUID === uuid) {
				exists = true;
				return false;
			}
		});
		
		if(!exists) {
			var name = $(this).closest('.collectionSearchInput').data('resourcekey');
			$(this).closest('.collectionSearchInput').find('.collectionSearchInputText').val('');
			select.append('<tr><input type="hidden" name="' + name + '" value="' + uuid + '"><td>' + $(this).text() + '</td><td>' +
//							'<a href="#" class="collectionSearchUp"><i class="far fa-fw fa-arrow-up me-2"></i></a>'  +
//							'<a href="#" class="collectionSearchDown"><i class="far fa-fw fa-arrow-down me-2"></i></a>' +
							'<a href="#" class="collectionSearchDelete"><i class="far fa-fw fa-trash me-2"></i></a>' + 
						  '</td></tr>');
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
				const dropdownToggleEl = toggle[0];
				const dropdownList = new bootstrap.Dropdown(dropdownToggleEl);
				dropdownList.show();
			}
			
			
		});
	};
	
	$('.jsearchText').on('keyup', function(e) {
		
		createDropdown($(this).val(), 
							$(this).data('url'), 
							$(this).data('field'),
							$(this).data('id'),
							$(this).closest(".dropdown").find('.dropdown-menu'),
							$(this),
							"jdropdown-item");

	});

	$('input[name="theme"').on('change', function(e) {
		document.cookie = "userTheme=" + $(this).val() + '; path=/; expires=Tue, 01 Jan 2038 00:00:00 UTC;';
		window.location.reload();
	});
	
	$('.jadaptive-tag').on('click', function() {
		$(this).parents().find('option').remove();
	});
	
	$('.copyURL').on('click', function(e) {
		e.preventDefault();
		navigator.clipboard.writeText($(this).attr('href'));
		JadaptiveUtils.success($('#feedback'), "The URL has been copied to the clipboard.");
	});
	
	$('.deleteAction').on('click', function(e) {
		e.preventDefault();
		var name = $(this).data('name');
		var url = $(this).data('url');

		bootbox.confirm({
    		message: 'Are you sure you want to delete ' + name + '?',
		    buttons: {
		        confirm: {
		            label: 'Yes',
		            className: 'btn-success'
		        },
		        cancel: {
		            label: 'No',
		            className: 'btn-danger'
		        }
		    },
		    callback: function (result) {
		        if(result)
		        {
		        	$.ajax({
					    url: url,
					    type: 'DELETE',
					    dataType: 'JSON',
					    success: function(result) {
					        window.location.reload();
					    }
					});
		        }
		    }
		});
	});
});