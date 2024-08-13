Object.defineProperty(Number.prototype,'fileSize',{value:function(a,b,c,d){
		 return (a=a?[1e3,'k','B']:[1024,'K','iB'],b=Math,c=b.log,
		 d=c(this)/c(a[0])|0,this/b.pow(a[0],d)).toFixed(2)
		 +' '+(d?(a[1]+'MGTPEZY')[--d]+a[2]:'Bytes');
		},writable:false,enumerable:false});
	
function appendFile(fileInput) {
	
	if($('.uploading').length == 0) {
		for(var i=0;i<fileInput[0].files.length;i++) {
			var row = fileInput.parents('.jfiles').find('tr').last();
			row.parent().append(row.clone());
			row.attr('data-filename', fileInput[0].files[i].name);
			row.addClass('file-index');
			row.find(".filename").text(fileInput[0].files[i].name);
			row.find(".size").text((fileInput[0].files[i].size).fileSize(1));
			row.show();
		}
	
		fileInput.parent().append('<input class="file-input file-index d-none" type="file" name="' +
			fileInput.attr('name') + '" multiple/>');
		
		$('.jfiles').removeClass('d-none');
	}
}
$(function() {
	
	$('.uploadProgress').addClass('d-none');
	
	$(document).on('change', '.file-input', function(e) {
		var fileInput = $('.file-input').last();
		appendFile(fileInput);
		return fileInput;
	});
	
	$(document).on('click', '.deleteFile', function(e) {
		e.preventDefault();
		var row = $(this).parents('tr').first();
		if(row.data('uuid')) {
			$(this).removeClass('deleteFile');
			$(this).addClass('undeleteFile');
			row.find('input').addClass('deletedFile').attr('disabled', 'disabled');
			row.addClass('text-decoration-line-through');
			row.find("i").removeClass('fa-trash').addClass('fa-trash-undo text-danger');
		} else {
			row.remove();
		}
		
	});
	
	$(document).on('click', '.undeleteFile', function(e) {
		var row = $(this).parents('tr').first();
		$(this).removeClass('undeleteFile');
		$(this).addClass('deleteFile');
		row.find('input').removeClass('deletedFile').removeAttr('disabled', 'disabled');
		row.removeClass('text-decoration-line-through');
		row.find("i").addClass('fa-trash').removeClass('fa-trash-undo text-danger');
	});

	$('.uploadForm').on('drag dragstart dragend dragover dragenter dragleave drop', function(e) {
    	e.preventDefault();
    	e.stopPropagation();
	}).on('dragover dragenter', function() {
	    $('.dropzone').addClass('is-dragover');
	}).on('dragleave dragend drop', function() {
	    $('.dropzone').removeClass('is-dragover');
	}).on('drop', function(e) {
		
		var droppedFiles = e.originalEvent.dataTransfer.files;
		var fileInput = $('.file-input').last();
		const dT = new DataTransfer();
		$.each( droppedFiles, function(i, file) {
			dT.items.add(file);
        });
        fileInput[0].files = dT.files;
        appendFile(fileInput);
     });
});

