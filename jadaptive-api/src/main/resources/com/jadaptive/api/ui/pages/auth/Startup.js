$(function() {
	
	var checkReady = function() {
		$.getJSON("/app/api/ready", function(data) {
			if(data.success) {
				window.location = "/app/ui/login";
			}
			setTimeout(checkReady, 1000);
		});	
	};
	
	setTimeout(checkReady, 1000);
	
	var toggle = true;
	var reverse = function() {
		if(toggle) {
			$('#spinner').removeClass("fa-spin-reverse");
		} else {
			$('#spinner').addClass("fa-spin-reverse");
		}
		toggle = !toggle;
		setTimeout(reverse, Math.floor(Math.random() * (1000 - 5000 + 1)) + 5000);
	}
	setTimeout(reverse,  Math.floor(Math.random() * (1000 - 5000 + 1)) + 5000);
});