/*
ClientWebSocketTransport

This transport uses WebSockets and a simple protocol to communicate
with a web server with WebSocket support. The web server would need
to be configured with a Server Endpoint that understands this protocol.

As part of the Terminal Components suite, an implementation called 
'AbstractAnnotatedClientSideTerminalEndpoint' is provided that has support
for this protocol and may be used a template to port to other
HTTP server technologies.

As an alternative, you can provide you own transport implementation
to pass to the emulator that uses any protocol you wish, as long
as it implements the same public functions and properties this 
implementation does (i.e. all members that DONT start with '_').
*/

var ClientWebSocketTransport = function ClientWebSocketTransport(url) {
	AbstractWebSocketTransport.call(this, url);
};

ClientWebSocketTransport.prototype = Object.create(AbstractWebSocketTransport.prototype);
ClientWebSocketTransport.prototype.constructor = ClientWebSocketTransport;

ClientWebSocketTransport.prototype._sendInit = function() {
	this._send(AbstractWebSocketTransport.INIT + this._emulator.termType + ',' + this._emulator.width + ',' + this._emulator.height);
};

ClientWebSocketTransport.prototype._handle = function(code, data) {

	switch(code) {
	case AbstractWebSocketTransport.DATA:
		if(DEBUG_LEVEL >= LVL_DEBUG)
			TermUtil.log(LVL_DEBUG, 'WS DATA IN: ' + data);
	    this._emulator.putString(data);
	    break;
	case ClientWebSocketTransport.CLEAR:
		this._emulator.clearScreen();
	    break;
	case AbstractWebSocketTransport.INIT:
		this._ready = true;
		break;
	case ClientWebSocketTransport.SET_LOCAL_ECHO:
		this._emulator.localecho = data.charCodeAt(0) == 1;
	    TermUtil.log(LVL_INFO, 'Local echo now ' + this._emulator.localecho);
	    break;
	case ClientWebSocketTransport.SET_OUTPUT_EOL:
		this._emulator.outputEOL = data.charCodeAt(0);
	    break;
	case ClientWebSocketTransport.IBM_CHARSET:
		this._emulator.useibmcharset = data.charCodeAt(0) == 1;
	    break;
	case ClientWebSocketTransport.VMS:
		this._emulator.setVMS(data.charCodeAt(0) == 1);
	    break;
	case ClientWebSocketTransport.EOL:
		this._emulator.eol = data.charCodeAt(0);
	    break;
	case ClientWebSocketTransport.TERM:
		this._emulator.termType = data;
	    break;
	case ClientWebSocketTransport.ANSWERBACK:
		this._emulator.setAnswerBack(data);
		break;
	case ClientWebSocketTransport.CHARSET:
		this._emulator.charsetName = data;
		break;
	case AbstractWebSocketTransport.SCREEN_SIZE:
		var a = data.split(',');
		this._emulator.setScreenSize(parseInt(a[0], 10), parseInt(a[1], 10), a[2] == 'true');
		break;
	default:
		TermUtil.log(LVL_WARN, 'Unsupported client side command code. ' + code);
		break;
	}			
};

//
// Private
//

ClientWebSocketTransport.SET_OUTPUT_EOL = 'N';
ClientWebSocketTransport.SET_LOCAL_ECHO = 'E';
ClientWebSocketTransport.EOL = 'L';
ClientWebSocketTransport.VMS = 'T';
ClientWebSocketTransport.ANSWERBACK = 'A';
ClientWebSocketTransport.CHARSET = 'C';
ClientWebSocketTransport.IBM_CHARSET = 'B';
ClientWebSocketTransport.VMS = 'V';
ClientWebSocketTransport.TERM = 'T';
ClientWebSocketTransport.CLEAR = 'R';
