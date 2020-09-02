$(document).ready(function() {
		$.getJSON('/app/api/applicationMenu/list', function(data) {
			if(data.success) {
				var menus = [];
				var top = [];
				$.each(data.resource, function(idx, obj) {
					if(obj.parent != '') {
						if(!menus[obj.parent]) {
							menus[obj.parent] = [];
						}
						menus[obj.parent].push(obj);
					} else {
						if(!menus[obj.uuid]) {
							menus[obj.uuid] = [];
						}
						top.push(obj);
					}
				});
				$('#topMenu').empty();
				
				$.each(top, function(idx, obj) {
				
					if(menus[obj.uuid].length > 0) {
						$('#topMenu').append('<li class="nav-item dropdown mr-3">'
								+ '<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'
								+ obj.title + '</a><div id="' + obj.uuid + '" class="dropdown-menu" aria-labelledby="navbarDropdown"></div></li>');
						
						$.each(menus[obj.uuid], function(idx, child) {
							$('#' + child.parent).append('<a class="dropdown-item mr-3" href="' 
									+ child.path + '"><i class="' + child.icon + ' w-25"></i>&nbsp;' + child.title + '</a>');
					
						});
					}
				});
				
				
			}
		});
	});