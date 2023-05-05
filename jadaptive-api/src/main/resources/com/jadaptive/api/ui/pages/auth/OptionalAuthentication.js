$(function() {
					
	$('.select').click(function(e) {
		e.preventDefault()
		$('.card-body').removeClass('bg-secondary text-light');
		$('.stretched-link').empty();
		$(this).parents().closest('.card-body').addClass('bg-secondary text-light');
		$('#authenticator').val($(this).data('authenticator'));
	});
	
	$('.select').first().trigger('click');
	
	$('#loginSubmit').click(
		function(e) {
			e.preventDefault();
			if($('#authenticator').val()!=='') {
				JadaptiveUtils.startAwesomeSpin(
						$('#loginSubmit i'),
						'fa-sign-in-alt');
				$('#loginForm').submit();
			} else {
				JadaptiveUtils.error($('#feedback'), 'You must select one of the Authenticators to continue!');
			}
	});
		
});