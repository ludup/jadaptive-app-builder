function getCookie(cname) {
  let name = cname + "=";
  let decodedCookie = decodeURIComponent(document.cookie);
  let ca = decodedCookie.split(';');
  for(let i = 0; i <ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

$.ajaxSetup({
   xhrFields: { withCredentials: true },
   beforeSend: function(request) {
	  request.setRequestHeader("X-Browser-URL", window.location);
   }
});


var verifySession = function() {
	$.getJSON('/app/verify', function(data) {
		if(!data.success) {
			if(!window.location.pathname.startsWith('/app/ui/login')) {
				window.location = '/app/ui/login';
			}
		} else {
			setTimeout(verifySession, 10000);
		}
	});
};

$(function() {
	verifySession();
});
