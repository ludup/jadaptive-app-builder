/* Terminal Components - Common JavaScript 
 * 
 * This file contains common code shared between the 'server side' and 'client side'
 * versions of the terminal emulator.
 * 
 * You will also need to include at least one of the Emulator implementations (client
 * side or server side) and one or more of the transport (Websocket or XMLHTTPRequest). 
 *  
 * (C)2016 SSHTools - Dual Licensed under a Commercial and GPL license. See LICENSE supplied
 * with this software for more details. 
 */


/* Function to detect mobile browser (http://stackoverflow.com/questions/11381673/detecting-a-mobile-browser#11381730) */
window.mobilecheck = function() {
  var check = false;
  (function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
  return check;
};

/**
 * Inject rtrim if not available.
 */
String.prototype.rtrim = function() {
	return this.replace(/\s+$/,'');
};

/**
 * Inject endsWith if not available.
 */
if (!String.prototype.endsWith) {
  Object.defineProperty(String.prototype, 'endsWith', {
    value: function(searchString, position) {
      var subjectString = this.toString();
      if (position === undefined || position > subjectString.length) {
        position = subjectString.length;
      }
      position -= searchString.length;
      var lastIndex = subjectString.indexOf(searchString, position);
      return lastIndex !== -1 && lastIndex === position;
    }
  });
}

/* Logging constants */
var LVL_OFF = 0;
var LVL_ERROR = 1;
var LVL_WARN = 2;
var LVL_INFO = 3;
var LVL_DEBUG = 4;
var LEVELS = ['OFF','ERROR','WARN','INFO','DEBUG'];

/* Default log level */
var DEBUG_LEVEL = LVL_INFO;

/*
 * Simulate mouse events on touch events
 */
function terminalTouchHandler(event)
{
    var touches = event.changedTouches,
        first = touches[0],
        type = "";
    switch(event.type)
    {
        case "touchstart": type = "mousedown"; break;
        case "touchmove":  type = "mousemove"; break;        
        case "touchend":   type = "mouseup";   break;
        default:           return;
    }

    // initMouseEvent(type, canBubble, cancelable, view, clickCount, 
    //                screenX, screenY, clientX, clientY, ctrlKey, 
    //                altKey, shiftKey, metaKey, button, relatedTarget);

    var simulatedEvent = document.createEvent("MouseEvent");
    simulatedEvent.initMouseEvent(type, true, true, window, 1, 
                                  first.screenX, first.screenY, 
                                  first.clientX, first.clientY, false, 
                                  false, false, false, 0/*left*/, null);

    first.target.dispatchEvent(simulatedEvent);
    event.preventDefault();
}

/*
 * TermUtil
 *
 * Various utilities used in both version of the web terminal
 */ 
var TermUtil = {
		log: function(lvl, str) {
			if(DEBUG_LEVEL >= lvl)
				console.log(LEVELS[lvl] + ':' + str);
		},
		elementWidth: function(el) {
			
		},
		elementHeight: function(el) {
			
		},
		mobileCheck: function() {
		},
		findPos: function (obj, foundScrollLeft, foundScrollTop) {
		    var curleft = 0;
		    var curtop = 0;
		    if(obj.offsetLeft) curleft += parseInt(obj.offsetLeft);
		    if(obj.offsetTop) curtop += parseInt(obj.offsetTop);
		    if(obj.scrollTop && obj.scrollTop > 0) {
		        curtop -= parseInt(obj.scrollTop);
		        foundScrollTop = true;
		    }
		    if(obj.scrollLeft && obj.scrollLeft > 0) {
		        curleft -= parseInt(obj.scrollLeft);
		        foundScrollLeft = true;
		    }
		    if(obj.offsetParent) {
		        var pos = this.findPos(obj.offsetParent, foundScrollLeft, foundScrollTop);
		        curleft += pos[0];
		        curtop += pos[1];
		    } else if(obj.ownerDocument) {
		        var thewindow = obj.ownerDocument.defaultView;
		        if(!thewindow && obj.ownerDocument.parentWindow)
		            thewindow = obj.ownerDocument.parentWindow;
		        if(thewindow) {
		            if (!foundScrollTop && thewindow.scrollY && thewindow.scrollY > 0) curtop -= parseInt(thewindow.scrollY);
		            if (!foundScrollLeft && thewindow.scrollX && thewindow.scrollX > 0) curleft -= parseInt(thewindow.scrollX);
		            if(thewindow.frameElement) {
		                var pos = this.findPos(thewindow.frameElement);
		                curleft += pos[0];
		                curtop += pos[1];
		            }
		        }
		    }
		    return [curleft,curtop];
		},
		htmlEscape: function (str) {
		    return str
		        .replace(/&/g, '&amp;')
		        .replace(/"/g, '&quot;')
		        .replace(/'/g, '&#39;')
		        .replace(/</g, '&lt;')
		        .replace(/>/g, '&gt;');
		},
		mkarray: function(c, ch) {
			return new Array(c).fill(ch, 0, c);
		},
		mk2darray: function(r, c, ch) {
			var a = new Array();
			for(var v = 0 ; v < r ; v++) {
				a.push(TermUtil.mkarray(c, ch));
			}
			return a;
		},
		arraycopy: function(src, src_pos, dest, dest_pos, len) {
			for(var i = 0 ; i < len ; i++) {
				dest[i + dest_pos] = src[i + src_pos]; 
			}
		},
		loadScript : function(scriptSrc, callback) {
			this.log(LVL_INFO, 'Dynamically loading script ' + scriptSrc);
			var head= document.getElementsByTagName('head')[0];
			var script= document.createElement('script');
			script.type= 'text/javascript';
			script.src= scriptSrc;
			if(callback) {
				script.onreadystatechange= function () {
					if (this.readyState == 'complete') 
						callback();
				   };
				script.onload = callback;
			}
			head.appendChild(script);
		},
		toclipboard: function (text) {
			this.log(LVL_INFO, 'Setting "' + text + '" to clipboard');
			var textArea = document.createElement("textarea");
			textArea.style.position = 'fixed';
			textArea.style.top = 0;
			textArea.style.left = 0;
			textArea.style.width = '2em';
			textArea.style.height = '2em';
			textArea.style.padding = 0;
			textArea.style.border = 'none';
			textArea.style.outline = 'none';
			textArea.style.boxShadow = 'none';
			textArea.style.background = 'transparent';
			textArea.value = text;
			document.body.appendChild(textArea);
			textArea.select();
			var ok = true;
			try {
				var successful = document.execCommand('copy');
				var msg = successful ? 'successful' : 'unsuccessful';
				console.log('Copying text command was ' + msg);
			} catch (err) {
				console.log('Oops, unable to copy');
				ok = false;
			}
			document.body.removeChild(textArea);
			return ok;
		}

};
// 
// Event
//
var Event = function Event(name) {
    	this.name = name;
    	this.callbacks = [];
};

Event.prototype.registerCallback = function(callback){
	this.callbacks.push(callback);
};


/*  AbstractWebSocketTransport

Abstract implementation of a terminal components transport that uses 
WebSockets and a simple protocol to communicate with a web server with 
WebSocket support. The web server would need to be configured with a 
Server Endpoint that understands this protocol.

As part of the Terminal Components suite, an implementation called 
'AbstractAnnotatedTerminalEndpoint' is provided that has abstract support
for this protocol and may be used a template to port to other
HTTP server technologies.

You will not directly use this class, instead use either ClientWebSocketTransport
or ServerWebSocketTransport.

As an alternative, you can provide you own transport implementation
to pass to the emulator that uses any protocol you wish, as long
as it implements the same public functions and properties this 
implementation does (i.e. all members that DONT start with '_').

*/

var AbstractWebSocketTransport = function AbstractWebSocketTransport(url) {
	if(!url.startsWith('wss://') && !url.startsWith('ws://')) {
		// TODO relies on DOM being present
		// TODO maybe use URL - https://developer.mozilla.org/en-US/docs/Web/API/URL#Properties
		if(!document)
			throw 'Absolute WebSocket URI required';
		var l = document.createElement('a');
		l.href = document.location;
		if(!url.startsWith('/') && l.pathname)
			if(l.pathname.endsWith('/')) 
				url = l.pathname + url;				
			else
				url = l.pathname.substring(0, l.pathname.lastIndexOf('/') + 1) + url;		
		var proto = l.protocol == 'https' || l.protocol == 'https:' ? 'wss' : 'ws';
		var idx = url.lastIndexOf('#');
		if(idx != -1) {
			url =  url.substring(0, idx);
		}
		if(l.port)
			url = proto + '://' + l.hostname + ':' + l.port + url;
		else
			url = proto + '://' + l.hostname + url;
	}
	this._ready = false;
	this._url = url;
	TermUtil.log(LVL_INFO, 'Transport URL is ' + this._url);
};

AbstractWebSocketTransport.prototype.close = function() {
	if(this._socket)
		this._socket.close();
}

AbstractWebSocketTransport.prototype.isOpen = function() {
	return this._socket && this._socket.readyState == WebSocket.OPEN;
}

AbstractWebSocketTransport.prototype.init = function(emulator) {
	var self = this;
	
	this._emulator = emulator;
	this._emulator.addEventListener('resize', function() {
		self._sendScreenSize();
	});
	
	// Connect!
	this._socket = new WebSocket(this._url);
	this._socket.binaryType = 'arraybuffer';
	this._socket.onopen = function() {
		/* Safari and IE do not have TextEncode and TextDecoder so we need to load
		 * this polyfill
		 * 
		 * https://github.com/inexorabletash/text-encoding
		 */
		if(typeof TextEncoder == 'undefined' || typeof TextDecoder == 'undefined') 
			this._loadPolyfill();
		else 
			self._sendInit();
	};
	this._socket.onclose = function(event) {
		var reason;
        // See http://tools.ietf.org/html/rfc6455#section-7.4.1
        if (event.code == 1000) {
            self._emulator.dispatchEvent('closed');
            return true;
        }
        else if(event.code == 1001)
            reason = "The server is not available. Please try again in a few minutes.";
        else if(event.code == 1002)
            reason = "An endpoint is terminating the connection due to a protocol error";
        else if(event.code == 1003)
            reason = "An endpoint is terminating the connection because it has received a type of data it cannot accept (e.g., an endpoint that understands only text data MAY send this if it receives a binary message).";
        else if(event.code == 1004)
            reason = "Reserved. The specific meaning might be defined in the future.";
        else if(event.code == 1005)
            reason = "No status code was actually present.";
        else if(event.code == 1006)
           reason = "The connection was closed abnormally, e.g., without sending or receiving a Close control frame. Check the developer console for more information.";
        else if(event.code == 1007)
            reason = "An endpoint is terminating the connection because it has received data within a message that was not consistent with the type of the message (e.g., non-UTF-8 [http://tools.ietf.org/html/rfc3629] data within a text message).";
        else if(event.code == 1008)
            reason = "An endpoint is terminating the connection because it has received a message that \"violates its policy\". This reason is given either if there is no other sutible reason, or if there is a need to hide specific details about the policy.";
        else if(event.code == 1009)
           reason = "An endpoint is terminating the connection because it has received a message that is too big for it to process.";
        else if(event.code == 1010) // Note that this status code is not used by the server, because it can fail the WebSocket handshake instead.
            reason = "An endpoint (client) is terminating the connection because it has expected the server to negotiate one or more extension, but the server didn't return them in the response message of the WebSocket handshake. <br /> Specifically, the extensions that are needed are: " + event.reason;
        else if(event.code == 1011)
            reason = "A server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.";
        else if(event.code == 1015)
            reason = "The connection was closed due to a failure to perform a TLS handshake (e.g., the server certificate can't be verified).";
        else
            reason = "Unknown reason";
        
        self._emulator.dispatchEvent('error', reason);
		//self._send(ServerWebSocketTransport.INIT + self._emulator.termType + ',' + self._emulator.width + ',' + self._emulator.height);
	};
	
	this._socket.onmessage = function (event) {
		var str = new TextDecoder(self._emulator.charsetName).decode(new DataView(event.data));
		var code = str.substring(0, 1);
		var data = str.substring(1);
		self._handle(code, data);
	};
};

AbstractWebSocketTransport.prototype._handle = function(code, data) {
	TermUtil.log(LVL_WARN, 'Message handler not implemented!');
};

AbstractWebSocketTransport.prototype._sendInit = function() {
	TermUtil.log(LVL_WARN, 'Send init not implemented!');
};

AbstractWebSocketTransport.prototype._loadPolyfill = function() {
	TermUtil.log(LVL_INFO, 'Need encoding library, deferring init until that is loaded.');
	var self = this;
	var scriptPath = ''; 
	if(document) {
		var scripts = document.getElementsByTagName('script');
		for (var i = scripts.length - 1; i >= 0; --i) {
		    var src = scripts[i].src;
		    var l = src.length;
		    var length = name.length;
		    if (src.endsWith('/terminal.components.js') && !src.endsWith('emulation/terminal.components.js')) {
		        scriptPath = src.substr(0, src.lastIndexOf('/')) + 'encoding.js';
		        break;
		    }
	    }
	}
	if(scriptPath == '') {
		TermUtil.log(LVL_WARN, 'Could not automatically locate encoding.js, is it in the same folder as ' +
				'terminal.components.js');
		scriptPath = 'encoding.js';
	}
	
	TermUtil.loadScript(scriptPath, function() {
		self._sendInit();
	});
};

AbstractWebSocketTransport.prototype.write = function(s) {
	if(this._ready)
		this._send(AbstractWebSocketTransport.DATA + s);
	else
		TermUtil.log(LVL_WARN,'Got request to write before ready.');
};

//
// Private
//

AbstractWebSocketTransport.prototype._send = function(s) {		
    // The TextDecoder interface is documented at http://encoding.spec.whatwg.org/#interface-textdecoder
	this._socket.send(new TextEncoder(this._emulator.charsetName).encode(s));
};

AbstractWebSocketTransport.prototype._sendScreenSize = function() {
	if(this._ready) {
		/* Recalculate rather than use the stored .width and .height properties as this
		 * is handled by an event and you can't be sure which happens first, the local
		 * resize or this
		 */
		var sz = this._emulator.display.calcTerminalSize();
		TermUtil.log(LVL_INFO, 'Sending screen size of ' + sz.width + ',' + sz.height);
		this._send(AbstractWebSocketTransport.SCREEN_SIZE + sz.width + ',' + sz.height);
	}
	else {
		TermUtil.log(LVL_WARN,'Got request to send new screen size before.');		
	}
};

AbstractWebSocketTransport.INIT = 'I';
AbstractWebSocketTransport.DATA = 'D';
AbstractWebSocketTransport.SCREEN_SIZE = 'S';
