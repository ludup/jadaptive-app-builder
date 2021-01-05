function renderActions(val, obj, idx, total) {

	var ret = '<a href="/app/ui/update/' + obj.resourceKey + '/' + obj.uuid + '" data-uuid="' + obj.uuid + '"><i class="far fa-edit"></i></a>&nbsp;';
	ret += '<a href="/app/ui/view/' + obj.resourceKey + '/' + obj.uuid + '" data-uuid="' + obj.uuid + '"><i class="far fa-eye"></i></a>&nbsp;';
	if(!obj.system) {
		ret += '<a class="clickDelete" href="#" data-uuid="' + obj.uuid + '"><i class="far fa-trash-alt"></i></a>';
	}
	return ret;
}

function renderBool(val) {
	return val ? '<i class="far fa-check"></i>' : '';
}

$(document).ready(function() {

	$.getJSON('/app/api/template/' + $('#table').data('resourcekey'), function(data) {
		if(data.success) {
			var t = data.resource;

			$('#table').bootstrapTable({
				sidePagination: 'server',
				totalField: 'total',
				dataField: 'rows',
				url: '/app/api/' + t.resourceKey + '/table',
				pagination: true,
				search: true,
				showRefresh: true,
				mobileResponsive: true,
				queryParams: function(params) {
					params['searchField'] = $('#searchValue').val();
					return params;
				}
			});
			
			
			
			$('#table').off('post-header.bs.table');
			$('#table').on('post-header.bs.table', function(e) {
				
				if($('#searchField').length == 0) {
					$('.search').parent().append('<div id="searchDropdownHolder" class="columns columns-right mr-1 float-right"></div>');
					
					$('#searchDropdownMenu').empty();

					$.each(t.fields, function(idx, obj) {
						if(!obj.hidden && obj.searchable) {
							$('#searchDropdownMenu').append('<a data-resourcekey="' + obj.resourceKey +'" + class="clickSearch dropdown-item" href="#">' + obj.name + '</a>');
						}
					});
						
						
					
					$("#searchDropdown").appendTo("#searchDropdownHolder");
					$('#searchDropdown').show();
					
					$('.clickSearch').click(function(e) {
						e.preventDefault();
						var value = $(this).data('resourcekey');
						var text = $(this).text();
						$('#searchValue').val(value);
						$('#searchText').val(text);
					});
				}

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
					        	var url = '/app/api/' + t.resourceKey + '/' + uuid;
					    		
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