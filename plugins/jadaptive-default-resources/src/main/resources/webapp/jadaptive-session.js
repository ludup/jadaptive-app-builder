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
	$.ajax({
		url: '/app/verify'
	}).done(function() {
		setTimeout(verifySession, 10000);
	}).fail(function(xhr) {
		if(xhr.status == 410 && !window.location.pathname.startsWith('/app/ui/login')) {
			window.location = '/app/ui/login';
		}
		else
			setTimeout(verifySession, 10000);
	});
};

$(function() {
	verifySession();
});
