$(function() {
	$('#loginSubmit').click(function(e) {
		if($('#username').val()!=='' && $('#password').val() !=-'') {
			JadaptiveUtils.startAwesomeSpin($('#loginSubmit i'), 'fa-sign-in-alt');
			$('#loginForm').submit();
		}
	});	
});
