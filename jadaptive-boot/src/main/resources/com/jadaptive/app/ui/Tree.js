	function renderPerms(val, obj) {
		var perms = obj.readable ? 'r' : '-';
		perms += obj.writable ? 'w': '-';
		return perms;
	}
	
	function renderName(val, obj) {
		var icon = (obj.directory ? '<i class="far fa-folder"></i>' : '<i class="far fa-file"></i>');
		if(obj.directory) {
			return '<a class="clickPath" href="' + obj.path + '">' +  icon + ' ' + obj.name + '</a><br><small>' + obj.path + '</small>';
		} else {
			return icon + ' ' + obj.name + '<br><small>' + obj.path + '</small>';
		}
	}
	
	function renderLength(val) {
		if(val > 0) {
			var i = Math.floor( Math.log(val) / Math.log(1024) );
		    return ( val / Math.pow(1024, i) ).toFixed(2) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
		}
		
		return '';
	}
	
	function renderActions(val, obj) {
		if(!obj.directory) {
			return '<a class="downloadFile" target="_blank" href="/app/vfs/downloadFile' + obj.path + '"><i class="far fa-download"></i></a>';
		}
		return '';
	}
	
	function getMaximumFiles() {
		var results = $('#maximumFiles').val();
		if(results.trim()==='') {
			return 1000;
		}
		return results;
	}
	
	function getBoolean(val) {
		if(val === 'true') {
			return true;
		}
		return false;
	}
	
	function getPath() {
		var path = $(document).data('path');
		if(!path) {
			return '/';
		}
		return path;
	}
	
	function ajaxRequest(params) {
		
		$('#feedback').empty();
		
	    var url = '/app/vfs/listDirectory' + getPath();
	    params.data.filter = $('#filter').val();
	    params.data.files = $('#files').is(":checked");
	    params.data.folders = $('#folders').is(":checked");
	    params.data.hidden =$('#hidden').is(":checked");
	    params.data.maximumResults = getMaximumFiles();
	    params.data.searchDepth = $('#searchDepth').val();
	    
	    $.get(url + '?' + $.param(params.data)).then(function (res) {
	    	  
	    	$('table').show();
		      
	    	  if(res.success) {
		    	  params.success(res);
		      } else {
		    	  $('#feedback').append('<p class="alert alert-danger"><i class="far fa-exclamation-square"></i> ' + res.error + '</p>');
		    	  params.success({
		    		 rows: [],
		    		 total: 0
		    	  });
		      }
	    });
	  }
	
	function changePath(path) {
		debugger;
		$(document).data('path', path);
		$('table').bootstrapTable('refresh');
		
		$('#breadcrumb').empty();
		$('#breadcrumb').append('<li class="breadcrumb-item"><a class="clickPath" href="/"><i class="far fa-hdd"></i></a></li>');
		var lastIdx = path.indexOf('/');
		
		while(lastIdx < path.length-1) {
		
			var idx = path.indexOf('/', lastIdx+1);
			if(idx==-1) {
				idx = path.length;
			}
			$('#breadcrumb').append('<li class="breadcrumb-item"><a class="clickPath" href="' 
					+ path.substring(0,idx) + '">' + path.substring(lastIdx+1, idx) + '</a></li>');
			lastIdx = idx;
		}
		$('.breadcrumb-item').last().addClass('active');
		
	}
	
	function refresh() {
		var path = $(document).data('path');
		if(!path) {
			path = '/';
		}
		changePath(path);
	}

$(document).ready(function() {
	$(document).on('click', '.clickPath', function(e) {
		e.preventDefault();
		changePath($(this).attr('href'));
	});
	
	$('#table').bootstrapTable({
		sidePagination: 'server',
		pagination: true,
		pageList: "[10, 25, 50, 100, 200, All]",
		pageSize: 10,
		pageNumber: 1,
		mobileResponsive: true,
		ajax: 'ajaxRequest',
		loadingTemplate: '<i class="fa fa-spinner fa-spin fa-fw fa-2x"></i>'
	});
	
	$('#spinner').hide();
	$('table').show();
	
	$.getJSON('/app/vfs/mounts', function(data) {
		if(data.success) {
			$.each(data.resources, function(idx, mount) {
				$('#mounts').append('<div class="col-md-3 mb-1"><a href="' + mount.path + '" class="clickPath btn btn-block btn-outline-dark"><i class="' + mount.icon + '"></i> ' + mount.text + '</a></div>');
			});
		}
	});
	
	$('.filter').change(function(e) {
		refresh();
	});
	
	$('#refresh').click(function(e){ 
		refresh();
	});
});
