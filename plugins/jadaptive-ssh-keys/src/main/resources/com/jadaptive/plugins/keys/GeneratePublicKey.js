$(function() {
	$('#generateForm').submit(function(e) {
		e.preventDefault();
		$('.alert').remove();
		if($('#passphrase').val() !== $('#confirmPassphrase').val()) {
			$('.card-body').prepend('<div class="alert alert-danger"><i class="fa-solid fa-exclamation-square"></i>  Passphrases do not match!</div>');
			return false;
		}
		$.post($(this).attr('action'), $(this).serialize(), function(data) {
		      if(data.success) {
		    	  window.location = '/app/ui/download-key';
		      } else {
		    	  $('.card-body').prepend('<div class="alert alert-danger"><i class="fa-solid fa-exclamation-square"></i> ' + data.message + '</div>');
		      }
		}, 'json');
		return false;
	});
})