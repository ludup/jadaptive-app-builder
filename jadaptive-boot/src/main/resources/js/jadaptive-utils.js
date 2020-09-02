var JadaptiveUtils = {

startAwesomeSpin : function(el, icon, spinner) {
		el.removeClass(icon);
		if(spinner) {
			el.addClass(spinner);
		} else {
			el.addClass('fa-spinner');
		}
		el.addClass('fa-spin');
	},
stopAwesomeSpin : function(el, icon, spinner) {
		el.removeClass('fa-spin');
		if(spinner) {
			el.removeClass(spinner);
		} else {
			el.removeClass('fa-spinner');
		}
		el.addClass(icon);
	}
};

setupDndFileUploads : function(nameSelector) {

	webbits.on('dragEnter', function(evt, input) {
		$('#cloud').addClass('text-success');
	});
	webbits.on('dragLeave', function(evt, input) {
		$('#cloud').removeClass('text-success');
	});
	
	webbits.on('filesChanged', function(el, files, input) {
		$(el).empty();
		jQuery.each(files, function() {
			var li = $('<li class="row">');
			li.html('<div class="col-md-1"><i class="fas fa-trash"></i></div><div class="col-md-6">' + this.name.encodeHTML() + '</div><div class="col-md-2">' + this.type + '</div></div><div class="col-md-2">' + this.size.formatBytes() + '</div>');
			$(el).append(li);

			if(nameSelector) {
				if($(nameSelector).val() === '') {
					var idx = this.name.indexOf('/');
					if(idx === -1)
						idx = this.name.indexOf('\\');
					var n = this.name;
					if(idx !== -1) 
						n = n.substring(idx + 1);
					$('#name').val(n);
				}
			}
			
			var file = this;
			li.on('click', function() {
				webbits.removeFile(file, input);
			});
		});
	});
}