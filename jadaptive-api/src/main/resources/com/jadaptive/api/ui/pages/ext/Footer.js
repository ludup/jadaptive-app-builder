$(function() {
	
	$(document).on('click', '.jdropdown-item', function(e) {
		e.preventDefault();
		$(this).closest(".dropdown").find('input[type="text"]').val($(this).text());
		$(this).closest(".dropdown").find('.jdropdown-text').html($(this).text());
		$(this).closest(".dropdown").find('.dropdown-menu').removeClass('show');
		$(this).closest(".dropdown").find('input[type="hidden"]').val($(this).data('resourcekey')).change();
	});
	
	$(document).on('click', '.replacement-item', function(e) {
		e.preventDefault();
		$(this).closest(".dropdown").find('input[type="text"]').val($(this).data('resourcekey'));
		$(this).closest(".dropdown").find('.jdropdown-text').html($(this).data('resourcekey'));
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
				 + '" class="me-1"><span class="pe-1">' + source.val() + '</span><a href="#" class="jadaptive-tag text-light"><i class="' + $('body').data('iconset') + ' fa-times"></i></a></option>');
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
	
	$(document).on('keypress', 'input[type="text"]', function(e) {
		
		if (e.keyCode === 13) {
			e.stopPropagation();
			return false;
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
		e.preventDefault();
		var source = $(this).closest('.row').find('.multipleSelectSource');
		var target = $(this).closest('.row').find('.multipleSelectTarget');
		var children = source.children('option:selected');
		var parent = $(this).closest('.row');
		$.each(children, function() {
			parent.append('<input class="d-none" name="' + target.attr('id') + '" value="' + $(this).attr('value') + '">');
		});
		children.detach().appendTo(target);
		
	});
	
	$(document).on('click', '.multipleSelectRemove', function(e) {
		e.preventDefault();
		var source = $(this).closest('.row').find('.multipleSelectTarget');
		var target = $(this).closest('.row').find('.multipleSelectSource');
		var children = source.children('option:selected');
		var parent = $(this).closest('.row');
		$.each(children, function() {
			parent.find('input[value="' + $(this).attr('value') + '"]').remove();
		});
		children.detach().appendTo(target);
	});
	
	$(document).on('keyup', '.collectionSearchInputText', function(e) {
		createDropdown($(this).val(), $(this).data('url'), 
							$(this).data('field'),
							$(this).data('id'),
							$(this).closest(".dropdown").find('.dropdown-menu'),
							$(this),
							'collectionSearchInputSelection');
	});
	
	$(document).on('click', '.collectionTextAdd', function(e) {
		
		e.preventDefault();
		var source = $(this).siblings('.collectionTextInputText');
		
		if(source.val()!=='') {
			var select = source.closest(".collectionTextInput").find('table');
			var name = source.closest('.collectionTextInput').data('resourcekey');
			
			select.append('<tr><input type="hidden" name="' + name + '" value="' + source.val() + '"><td>' + source.val() + '</td><td>' +
							'<a href="#" class="collectionSearchUp"><i class="' + $('body').data('iconset') + ' fa-fw fa-arrow-up me-2"></i></a>'  +
							'<a href="#" class="collectionSearchDown"><i class="' + $('body').data('iconset') + ' fa-fw fa-arrow-down me-2"></i></a>' +
							'<a href="#" class="collectionSearchDelete"><i class="' + $('body').data('iconset') + ' fa-fw fa-trash me-2"></i></a>' + 
						  '</td></tr>');
		    source.val('');
		    doOrderState(select.find('tr'));
	    }
	});
	
	$(document).on('keypress', '.collectionTextInputText', function(e) {
		
		if (e.keyCode === 13) {
			e.preventDefault();
			e.stopPropagation();
			
			if($(this).val()!=='') {
				var select = $(this).closest(".collectionTextInput").find('table');
				var name = $(this).closest('.collectionTextInput').data('resourcekey');
				
				select.append('<tr><input type="hidden" name="' + name + '" value="' + $(this).val() + '"><td>' + $(this).val() + '</td><td>' +
								'<a href="#" class="collectionSearchUp"><i class="' + $('body').data('iconset') + ' fa-fw fa-arrow-up me-2"></i></a>'  +
								'<a href="#" class="collectionSearchDown"><i class="' + $('body').data('iconset') + ' fa-fw fa-arrow-down me-2"></i></a>' +
								'<a href="#" class="collectionSearchDelete"><i class="' + $('body').data('iconset') + ' fa-fw fa-trash me-2"></i></a>' + 
							  '</td></tr>');
			    $(this).val('');
			    doOrderState(select.find('tr'));
		    }
		}

	});

	var doOrderState = function(el) {

		el.closest("tbody").find('.collectionSearchUp').find('i').addClass('fa-arrow-up');
		el.closest("tbody").find('.collectionSearchUp').find('i').first().removeClass('fa-arrow-up');
		el.closest("tbody").find('.collectionSearchDown').find('i').addClass('fa-arrow-down');
		el.closest("tbody").find('.collectionSearchDown').find('i').last().removeClass('fa-arrow-down');

	}
		
	$(document).on('click', '.collectionSearchDelete', function(e) {
		e.preventDefault();
		var el = $(this).closest("tbody");
		$(this).closest("tr").remove();
		doOrderState(el.find('tr'));
	});
	
	$(document).on('click', '.collectionSearchUp', function(e) {
		e.preventDefault();
		var row = $(this).closest("tr");
		var prev = row.prev();
		if(prev.length > 0) {
			prev.before(row);
		}

		doOrderState($(this));
	});
	
	$(document).on('click', '.collectionSearchDown', function(e) {
		e.preventDefault();
		var row = $(this).closest("tr");
		var next = row.next();
		if(next.length > 0) {
			next.after(row);
		}
		doOrderState($(this));
	});
	
	$('.collectionSearchInput').each(function(idx, obj) {
		doOrderState($(obj).find('table').find('.collectionSearchUp').first());
	});
	
	$(document).on('click', '.collectionSearchInputSelection', function(e) {
		e.preventDefault();
		debugger;
		var uuid = $(this).data('resourcekey');
		var name = $(this).text();
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
			var variableName = $(this).closest('.collectionSearchInput').data('resourcekey');
			$(this).closest('.collectionSearchInput').find('.collectionSearchInputText').val('');
			select.append('<tr><input type="hidden" name="' + variableName + '" value="' + uuid + '">'
						+ '<input type="hidden" name="' + variableName + 'Text" value="' + name + '"><td>' + $(this).text() + '</td><td>' +
							'<a href="#" class="collectionSearchUp"><i class="' + $('body').data('iconset') + ' fa-fw fa-arrow-up me-2"></i></a>'  +
							'<a href="#" class="collectionSearchDown"><i class="' + $('body').data('iconset') + ' fa-fw fa-arrow-down me-2"></i></a>' +
							'<a href="#" class="collectionSearchDelete"><i class="' + $('body').data('iconset') + ' fa-fw fa-trash me-2"></i></a>' + 
						  '</td></tr>');
			
			doOrderState(select.find('.collectionSearchUp').first());
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
	
	$('.filter-dropdown').on('keyup', function(e) {
		var text = $(this).val().trim();
		debugger;
		$(this).parent().find('.dropdown-menu a').each(function(idx, obj) {
			if(text === '' || $(this).text().startsWith(text)) {
				$(this).show();
			} else {
				$(this).hide();
			}
		});
	});

	$('input[name="theme"]').on('change', function(e) {
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
	
	$('.spinClick').on('click', function() {
		JadaptiveUtils.startAwesomeSpin($(this).children('i'));
	});
	
	$('form').on('submit', function() {
		JadaptiveUtils.startAwesomeSpin($(this).find('.spinForm').children('i'));
	});
	
	$('input').change(function(e) {
		$(this).addClass('dirty');
		$('.processDepends').each(function() {
			var dependsOn = $(this).data('depends-on');
			var dependsValue = $(this).attr('data-depends-value');
			
			var input = $('#' + dependsOn);
			var matchValues = dependsValue.split(',');
			var matches = false;
			$.each(matchValues, function(i, obj) {
				
				var expectedResult = !obj.startsWith("!");
				if(!expectedResult) {
					obj = obj.substring(1);
				}
				var value;
				if(input.attr('type') === 'checkbox') {
					value = input.is(':checked').toString();
				} else {
					value = input.val();
				}
				if(obj == value) {
					matches = expectedResult;
					return true;
				}
				return false;
			});
			if(matches) {
				$(this).removeClass('d-none');
			} else {
				$(this).addClass('d-none');
			}
		});
	});
	
	$('.checkExit').click(function(e) {
        e.preventDefault();
        var _self = $(this);
        if($('.dirty').length > 0) {
	    	bootbox.confirm({
	    		message: "${userInterface:exit.text}",
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
			        	window.location = _self.attr('href');
			        }
			    }
			});
		} else {
			window.location = _self.attr('href');
		}
    });
    
	$('.deleteAction').on('click', function(e) {
		e.preventDefault();
		var name = $(this).data('name');
		var url = $(this).data('url');

		bootbox.confirm({
    		message: '${userInterface:delete.text} ' + name + '?',
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
						beforeSend: function(request) {
					    	request.setRequestHeader("CsrfToken", $('#csrftoken').val());
					  	},
					    type: 'DELETE',
					    dataType: 'JSON',
					    complete: function(result) {
					        window.location = window.location.href;
					    }
					});
		        }
		    }
		});
	});
	
	$('.removeAction').on('click', function(e) {
		e.preventDefault();
		var name = $(this).data('name');
		var _row = $(this).closest('tr');
		bootbox.confirm({
    		message: '${userInterface:delete.text} ' + name + '?',
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
		        	_row.remove();
		        	JadaptiveUtils.success($('#feedback'), name + " ${userInterface:removed.text}");
		        }
		    }
		});
	});
});