$(function() {
	$('#loginSubmit').click(function(e) {
		e.preventDefault();
		$('#feedback').empty();
		if($('#confirmPassword').val() !== $('#password').val()) {
			
       	   $('#feedback').prepend('<p id="feedback" class="alert alert-danger col-12"><i class="fa-solid fa-exclamation-square"></i> <span id="feedbackText"></span></p>');
           $('#feedbackText').text("Passwords do not match!");
           
		} else {
		
			JadaptiveUtils.startAwesomeSpin($('#loginSubmit i'), 'fa-key');
			$('#loginForm').submit();
		}
	});
});