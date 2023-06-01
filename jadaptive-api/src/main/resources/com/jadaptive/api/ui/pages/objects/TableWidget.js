function renderActions(val, obj, idx, total) {

	var ret = '';
	var t = $(document).data('t');
	if(t.updatable) {
	ret += '<a href="/app/ui/update/' + obj.resourceKey + '/' + obj.uuid + '" data-uuid="' + obj.uuid + '"><i class="' + $('body').data('iconset') + ' fa-edit"></i></a>&nbsp;';
	}
	ret += '<a href="/app/ui/view/' + obj.resourceKey + '/' + obj.uuid + '" data-uuid="' + obj.uuid + '"><i class="' + $('body').data('iconset') + ' fa-eye"></i></a>&nbsp;';
	if(!obj.system) {
		ret += '<a class="clickDelete" href="#" data-uuid="' + obj.uuid + '"><i class="' + $('body').data('iconset') + ' fa-trash-alt"></i></a>';
	}
	$('.tableAction').each(function(idx, row) {
	    var url = $(this).data('url');
		ret += '&nbsp;<a href="' + url.replace('{uuid}', obj.uuid) + '" ' + ($(this).data('window') === 'BLANK' ? 'target="_blank"' : '') + '><i class="' + $(this).data('icon-group') + ' ' + $(this).data('icon') + '"></i></a>';
	
	});
	return ret;
}

function renderBool(val) {
	return val ? '<i class="' + $('body').data('iconset') + ' fa-check"></i>' : '';
}

$(document).ready(function() {

	$.getJSON('/app/api/template/' + $('#table').data('resourcekey'), function(data) {
		if(data.success) {
			$(document).data('t', data.resource);
			var t = data.resource;
			$('#table').bootstrapTable({
				sidePagination: 'server',
				totalField: 'total',
				dataField: 'rows',
				url: '/app/api/objects/' + t.resourceKey + '/table',
				pagination: true,
				search: true,
				showRefresh: true,
				minWidth: 767,
				mobileResponsive: true,
				queryParams: function(params) {
					params['searchField'] = $('#searchColumn').val();
					params['searchValue'] = $('#searchValue').val();
					return params;
				}
			});
			
			
			
			$('#table').off('post-header.bs.table');
			$('#table').on('post-header.bs.table', function(e) {
				
				/*if($('#searchDropdownHolder').length == 0) {
					$('.fixed-table-toolbar').append('<div id="searchDropdownHolder" class="float-end"></div>');
					
					$.each(t.fields, function(idx, obj) {
						if(!obj.hidden && obj.searchable) {
							$('#searchColumnItems').append('<a data-resourcekey="' + obj.resourceKey +'" + class="clickSearch jdropdown-item dropdown-item" href="#">' + obj.resourceKey + '</a>');
						}
					});
						
						
					$('#searchDropdown').appendTo($("#searchDropdownHolder"));
					$('#searchDropdown').show();
					
					$('.clickSearch').click(function(e) {
						e.preventDefault();
						var value = $(this).data('resourcekey');
						var text = $(this).text();
						$('#searchValue').val(value);
						$('#searchText').val(text);
					});
				}*/

				$('.clickDelete').off('click');
				
				$('.clickDelete').on('click', function(e) {
					e.preventDefault();
					var uuid = $(this).data('uuid');
					bootbox.confirm({
					    message: "Are you sure you want to delete the entity with uuid " + uuid + "?",
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
					        if(result) {
					        	var url = '/app/api/objects/' + t.resourceKey + '/' + uuid;
					    		
					    		$.ajax({
					                url: url,
					                type: 'delete',
					                dataType: 'json',
					                success: function (data) {
					                	$('#table').bootstrapTable('refresh');
					                }
					            });
					        }
					    }
					});
				});
			});
		} else {
			alert(data.message);
		}
	});
});