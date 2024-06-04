Object.defineProperty(Number.prototype,'fileSize',{value:function(a,b,c,d){
		 return (a=a?[1e3,'k','B']:[1024,'K','iB'],b=Math,c=b.log,
		 d=c(this)/c(a[0])|0,this/b.pow(a[0],d)).toFixed(2)
		 +' '+(d?(a[1]+'MGTPEZY')[--d]+a[2]:'Bytes');
		},writable:false,enumerable:false});
	
function appendFile(fileInput) {
	
	if($('.uploading').length == 0) {
		for(var i=0;i<fileInput[0].files.length;i++) {
			debugger;
			var row = fileInput.parents('.jfiles').find('tr').last();
			row.parent().append(row.clone());
			row.attr('data-filename', fileInput[0].files[i].name);
			row.addClass('file-index');
			row.find(".filename").text(fileInput[0].files[i].name);
			row.find(".size").text((fileInput[0].files[i].size).fileSize(1));
			row.show();
		}
	
		fileInput.parent().append('<input class="file-input file-index d-none" type="file" name="file" multiple/>');
		$('.jfiles').removeClass('d-none');
	}
}
$(function() {
	
	$('#uploadProgress').addClass('d-none');
	
	$(document).on('change', '.file-input', function(e) {
		debugger;
		var fileInput = $('.file-input').last();
		appendFile(fileInput);
		return fileInput;
	});
	
	$(document).on('click', '.deleteFile', function(e) {
		e.preventDefault();
		var index=  $(this).parents('tr').data('index');
		$(this).parents('tr').remove();
	});
	
	$('#uploadButton').click(function(e) {
		e.preventDefault();
		debugger;
		UploadWidget.upload();
	});
	
	$('#uploadForm').submit(function(e) {
		e.preventDefault();
		e.stopPropagation();
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
	disableFeedback: function() {
		_self = this;
		_self._options.disableFeedback = true;	
	},
	clearFiles: function() {
		$('.file-input').remove();
		$('.file-index').remove();
		$('#files').append('<input class="file-input d-none" type="file" name="file" multiple/>');
	},
	count: function() {
		var countFiles = 0;
			$('.file-input').each(function(idx, file) {
				for(i=0;i<this.files.length;i++) {
					if($("[data-filename='" + this.files[i].name + "']").length > 0) {
						countFiles++;
					}
				}
			});
		return countFiles;
	},
	size: function() {
		var size = 0;
			$('.file-input').each(function(idx, file) {
				for(i=0;i<this.files.length;i++) {
					if($("[data-filename='" + this.files[i].name + "']").length > 0) {
						size += this.files[i].size;
					}
				}
			});
		return size;
	},
	reset: function() {
			$('#progressBar').css("width", 0).attr('aria-valuenow', "0");
			$('#helpText').show(); 
			$('.dropzoneClick').on('click', function(e) {
				$('.file-input').last().trigger('click');
			});
			$('#uploadButton').attr('disabled', false);
			$('#uploadProgress').addClass('d-none');
	},
    upload: function(complete) {
	
			_self = this;

			$(_self._options.feedbackDiv + ' .alert').remove();

			var doUpload = function(onComplete, onError) {
				if($('.file-input').length <= 1) {
					$(_self._options.feedbackDiv).prepend('<p class="alert alert-danger"><i class="' + $('body').data('iconset') + ' fa-exclamation-circle"></i> There are no files selected!</p>');
					return;
				}
				$('.file-input').last().remove();
				
				JadaptiveUtils.startAwesomeSpin($('#uploadButton i'), 'fa-upload');
	
				$('#uploadProgress').addClass('uploading');
	
				var fd = new FormData();
				if(_self._options.callback) {
					_self._options.callback(fd);
				}
				
				var countFiles = 0;
				var totalSize = 0;

				$('.file-input').each(function(idx, file) {
					for(i=0;i<this.files.length;i++) {
						if($("[data-filename='" + this.files[i].name + "']").length > 0) {
							totalSize += this.files[i].size;
						}
					}
				});
				
				fd.append('totalSize', totalSize);

				$('.file-input').each(function() {
					for(i=0;i<this.files.length;i++) {
						if($("[data-filename='" + this.files[i].name + "']").length > 0) {
							totalSize += this.files[i].size;
							countFiles++;
						}
					}
				});
				
				if(countFiles==1) {
					fd.append("contentLength", totalSize);
				}
				
				$('.file-input').each(function() {
					for(i=0;i<this.files.length;i++) {
						if($("[data-filename='" + this.files[i].name + "']").length > 0) {
							fd.append('file', this.files[i]);
						}
					}
				});
			    
			    
			    
				$('#progressBar').css("width", 0).attr('aria-valuenow', "0");
				$('#helpText').hide(); 
				$('#uploadButton').attr('disabled', 'disabled');
				$('#uploadProgress').removeClass('d-none');
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
						       
								if(complete) {
									if(complete()) {
										return;
									}
								}
								
								if(onComplete) {
									onComplete();
								}
								
								if(!_self._options.disableFeedback) {
									$(_self._options.feedbackDiv).prepend('<p class="alert alert-success"><i class="' + $('body').data('iconset') + ' fa-check"></i> The upload of ' + countFiles + ' file(s) completed.</p>');		
								}
	//							setTimeout(function() {
									JadaptiveUtils.stopAwesomeSpin($('#uploadButton i'), 'fa-upload');
									if(_self._options.successUrl) {
										window.location = _self._options.successUrl;
									}
	//							}, 2000);
	 
			        	   } else {
			        		   _self.clearFiles();
							    if(onError) {
								   onError(data.message);
								} else {
									$(_self._options.feedbackDiv).prepend('<p class="alert alert-danger"><i class="' + $('body').data('iconset') + ' fa-exclamation-circle"></i> ' + data.message + '</p>');
								}
			        	   }
			           },
			           error: function (xhr, ajaxOptions, thrownError) {
				          if(onError) {
							  onError(thrownError);
						  }
				       },
			           complete: function() {
			        	    JadaptiveUtils.stopAwesomeSpin($('#uploadButton i'), 'fa-upload');
						    $('#uploadButton').attr('disabled', false);
							$('#uploadProgress').addClass('d-none');
							$('#uploadProgress').removeClass('uploading');
							$('.file-index').remove();
							$('#files').append('<input class="file-input d-none" type="file" name="file" multiple/>');
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
			if(_self._options.validate) {
				if(!_self._options.validate(doUpload)) {
					return;
				}
			}	
			
			doUpload();		
			
    }
};


