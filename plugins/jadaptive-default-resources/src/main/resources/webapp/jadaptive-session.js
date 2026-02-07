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

var serverIsDown = false;
var verifySession = function() {
  fetch('/app/verify', {
    credentials: 'include' 
  }).then(function(response) {
    if (response.status === 410 && !window.location.pathname.startsWith('/app/ui/login')) {
      window.location = '/app/ui/login';
      return;
    }

    if (response.ok) {
      if (serverIsDown) {
        console.log("Server is back up, redirecting to login ....");
        window.location = '/app/ui/login';
      } else {
        setTimeout(verifySession, 10000);
      }
    } else {
      setTimeout(verifySession, 10000);
    }
  }).catch(function() {
    if (!serverIsDown) {
      serverIsDown = true;
      console.log("Server is down, waiting ....");
    }
    setTimeout(verifySession, 10000);
  });
};

document.addEventListener('DOMContentLoaded', function() {
  verifySession();
});
