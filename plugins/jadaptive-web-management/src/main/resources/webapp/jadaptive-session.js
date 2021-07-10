var verifySession = function() {
	$.getJSON('/app/verify', function(data) {
		if(!data.success) {
			if(!window.location.pathname.startsWith('/app/ui/login')) {
				window.location = '/app/ui/login';
			}
			
		} else {
			setTimeout(verifySession, 30000);
		}
	});
};
verifySession();