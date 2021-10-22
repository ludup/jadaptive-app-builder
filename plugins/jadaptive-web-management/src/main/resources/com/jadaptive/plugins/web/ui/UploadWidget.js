Object.defineProperty(Number.prototype,'fileSize',{value:function(a,b,c,d){
		 return (a=a?[1e3,'k','B']:[1024,'K','iB'],b=Math,c=b.log,
		 d=c(this)/c(a[0])|0,this/b.pow(a[0],d)).toFixed(2)
		 +' '+(d?(a[1]+'MGTPEZY')[--d]+a[2]:'Bytes');
		},writable:false,enumerable:false});
	
function appendFile(fileInput) {
	var row = fileInput.parents('.jfiles').find('tr').last();
	row.parent().append(row.clone());
	var index = Date.now();
	row.attr('data-index', index);
	row.addClass('file-index');

	fileInput.attr('data-index', index);
	fileInput.parent().append('<input class="file-input file-index" type="file" name="file" style="display: none;" />');
	
	row.find(".filename").text(fileInput[0].files[0].name);
	row.find(".size").text((fileInput[0].files[0].size).fileSize(1));
	row.show();
		
	$('.jfiles').show();
}
$(function() {
	
	$(document).on('change', '.file-input', function(e) {
		debugger;
		var fileInput = $('.file-input').last();
		appendFile(fileInput);
		return fileInput;
	});
	
	$(document).on('click', '.deleteFile', function(e) {
		e.preventDefault();
		var index=  $(this).parents('tr').data('index');
		$('.file-index[data-index="' + index + '"]').remove();
	});
	
	$('#uploadForm').submit(function(e) {
		e.preventDefault();
		UploadWidget.upload();
	});
	
	$('#uploadForm').on('drag dragstart dragend dragover dragenter dragleave drop', function(e) {
    	e.preventDefault();
    	e.stopPropagation();
	})
	.on('dragover dragenter', function() {
	    $('.dropzone').addClass('is-dragover');
	})
	.on('dragleave dragend drop', function() {
	    $('.dropzone').removeClass('is-dragover');
	})
	.on('drop', function(e) {
		var droppedFiles = e.originalEvent.dataTransfer.files;
		$.each( droppedFiles, function(i, file) {
      		var fileInput = $('.file-input').last();
            const dT = new DataTransfer();
            dT.items.add(file);
			fileInput[0].files = dT.files;
			appendFile(fileInput);
        });
     });
});

var UploadWidget = {
	
	_options: {},
	init: function(postUrl, successUrl, feedbackDiv, callback, validate) {
		_self = this;
		_self._options.postUrl = postUrl;
		_self._options.successUrl = successUrl;
		_self._options.feedbackDiv = feedbackDiv ? feedbackDiv : "#uploadForm";
		_self._options.callback = callback;
		_self._options.validate = validate;
		_self.reset();
	},
	clearFiles: function() {
		$('.file-input').remove();
		$('.file-index').remove();
		$('#files').append('<input class="file-input" type="file" name="file" style="display: none;" />');
	},
	reset: function() {
			$('#progressBar').css("width", 0).attr('aria-valuenow', "0");
			$('#helpText').show(); 
			$('.dropzoneClick').on('click', function(e) {
				$('.file-input').last().trigger('click');
			});
			$('#uploadButton').attr('disabled', false);
			$('#uploadProgress').hide();
	},
    upload: function() {
	
			_self = this;

			$(_self._options.feedbackDiv + ' .alert').remove();

			if(_self._options.validate) {
				if(!_self._options.validate()) {
					return;
				}
			}			
			if($('.file-input').length <= 1) {
				$(_self._options.feedbackDiv).prepend('<p class="alert alert-danger">There are no files selected!</p>');
				return;
			}
			$('.file-input').last().remove();
			
			JadaptiveUtils.startAwesomeSpin($('#uploadButton i'), 'fa-upload');

			var fd = new FormData();
			if(_self._options.callback) {
				_self._options.callback(fd);
			}
			
			$('.file-input').each(function(idx, file) {
				fd.append('file', this.files[0]);
			});
		    
			$('#progressBar').css("width", 0).attr('aria-valuenow', "0");
			$('#helpText').hide(); 
			$('#uploadButton').attr('disabled', 'disabled');
			$('#uploadProgress').show();
		    $.ajax({
		           type: "POST",
		           url: _self._options.postUrl,
		           dataType: "json",
		           contentType: false,
		           processData: false,
		           data: fd,
		           success: function(data)
		           {
		        	   if(data.success) {
					       
						   setTimeout(function() {
							
							JadaptiveUtils.stopAwesomeSpin($('#uploadButton i'), 'fa-upload');
							_self.reset();
							
							if(_self._options.successUrl) {
								window.location = _self._options.successUrl;
							}
						   }, 2000);
		        		   
		        	   } else {
		        		   $(_self._options.feedbackDiv).prepend('<p class="alert alert-danger">' + data.message + '</p>');
							_self.clearFiles();
		        	   }
		           },
		           complete: function() {
		        	    JadaptiveUtils.stopAwesomeSpin($('#uploadButton i'), 'fa-upload');
					    $('#uploadButton').attr('disabled', false);
						$('#uploadProgress').hide();
						$('.file-index').remove();
						$('#files').append('<input class="file-input" type="file" name="file" style="display: none;" />');
						$('#helpText').show(); 
		           },
				   xhr: function() {
				        var xhr = new window.XMLHttpRequest();
				
				        // Upload progress
				        xhr.upload.addEventListener("progress", function(evt){
				            if (evt.lengthComputable) {
				                var percentComplete = Math.round((evt.loaded / evt.total) * 100);
							  $('#progressBar').css('width', percentComplete + "%").attr('aria-valuenow', percentComplete);
				            }
				       }, false);
				       
				       return xhr;
				    }
		     });
    }
};


