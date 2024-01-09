$(function() {
	$('#username').keypress(function(e) {
		if (e.keyCode === 13) {
			e.stopPropagation();
			if($('#username').val().trim()!=='' && $('#password').length == 0) {
				JadaptiveUtils.startAwesomeSpin($('#loginSubmit i'), 'fa-sign-in-alt');
				$('#loginForm').submit();
			}
		}
	});	
});