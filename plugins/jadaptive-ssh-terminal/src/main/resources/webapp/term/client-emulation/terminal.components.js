/* Terminal Components - Pure JavaScript Client Side Emulation version
 * 
 * This is the core of the Terminal Components Client Side Emulation component.
 * It provides nearly all of the same functionality as its 'server side' Java equivalent, 
 * with some worthy advantages (and a couple of disadvantages). Performance is one, with
 * less data being sent, and less load on the service, this method can yield better performance,
 * particularly when used with the new WebSocket based transport (the default).
 * 
 * This may also be used with other server frameworks for other languages such
 * as Apache/PHP. Ideally you would require WebSocket support as this is much easier 
 * to implement on the server.
 * 
 * No particular JavaScript framework is required, although some of the add-ons such as 
 * scrollbars and resizing require jQuery. The components should be easily replaceable
 * with other frameworks.
 * 
 * This history of this code dates back to the Java Telnet Applet to which SSHTools purchased
 * rights to use the use the source. Over the years we have extended and improved on this mature
 * codebase, introducing new renderers, features and possibilities. 
 *  
 * (C)2016 SSHTools - Dual Licensed under a Commercial and GPL license. See LICENSE supplied
 * with this software for more details. 
 */

/*
 * Default keymaps
 */

// TODO move all this into a namespace

var DEFAULT_KEYMAPS = {
		'ansi' : {
			'F1' : '',
			'F2' : '',
			'F3' : '',
			'F4' : '',
			'F5' : '',
			'F6' : '',
			'F7' : '',
			'F8' : '',
			'F9' : '',
			'F10' : '',
			'F11' : '',
			'F12' : '',
			'SF1' : '',
			'SF2' : '',
			'SF3' : '',
			'SF4' : '',
			'SF5' : '',
			'SF6' : '',
			'SF7' : '',
			'SF8' : '',
			'SF9' : '',
			'SF10' : '',
			'SF11' : '',
			'SF12' : '',
			'UP' : '\\e[A',
			'DOWN' : '\\e[B',
			'RIGHT' : '\\e[C',
			'LEFT' : '\\e[D',
			'INSERT' : '\\e[L',
			'REMOVE' : '\\e[3~',
			'HOME' : '\\e[H',
			'PGDOWN' : '',
			'PGUP' : '',
			'END' : '',
			'BACKSPACE' : '\\b',
			'SBACKSPACE' : '\\127',
			'CBACKSPACE' : '\\b',
			'ABACKSPACE' : '\\127',
		},
		'xterm' : {
			'F1' : '\\eOP',
			'F2' : '\\eOQ',
			'F3' : '\\eOR',
			'F4' : '\\eOS',
			'F5' : '\\e[15~',
			'F6' : '\\e[17~',
			'F7' : '\\e[18~',
			'F8' : '\\e[19~',
			'F9' : '\\e[20~',
			'F10' : '\\e[21~',
			'F11' : '\\e[23~',
			'F12' : '\\e[24~',
			'SF1' : '\\eO2P',
			'SF2' : '\\eO2Q',
			'SF3' : '\\eO2R',
			'SF4' : '\\eO2S',
			'SF5' : '\\e[15;2~',
			'SF6' : '\\e[17;2~',
			'SF7' : '\\e[18;2~',
			'SF8' : '\\e[19;2~',
			'SF9' : '\\e[20;2~',
			'SF10' : '\\e[21;2~',
			'SF11' : '\\e[23;2~',
			'SF12' : '\\e[24;2~',
			'UP' : '\\e[A',
			'DOWN' : '\\e[B',
			'RIGHT' : '\\e[C',
			'LEFT' : '\\e[D',
			'INSERT' : '\\e[2~',
			'REMOVE' : '\\e[3~',
			'HOME' : '\\eOH',
			'PGDOWN' : '\\e[6~',
			'PGUP' : '\\e[5~',
			'END' : '\\eOF',
			'SBACKSPACE' : '\\b',
			'BACKSPACE' : '\\127',
			'ABACKSPACE' : '\\b',
			'CBACKSPACE' : '\\127'
		}
}

var MULTI_CLICK_TIME = 500;

/**
 * Autoscroll types
 */
var NO_AUTO_SCROLL = 0;
var FORWARD_AUTO_SCROLL = 1;
var REVERSE_AUTO_SCROLL = 2;

/**
 * Background image types
 */
var NO_BACKGROUND_IMAGE = 0;
var BACKGROUND_IMAGE_CENTERED = 1;
var BACKGROUND_IMAGE_FILL = 2;
var BACKGROUND_IMAGE_SCALED = 3;
var BACKGROUND_IMAGE_TILED = 4;

/**
 * Solid block cursor style
 */
var CURSOR_BLOCK = 0;

/**
 * Underline cursor style
 */
var CURSOR_LINE = 1;

var RESIZE_NONE = 0;
var RESIZE_FONT = 1;
var RESIZE_SCREEN = 2;

//
var SCROLL_UP = false;
var SCROLL_DOWN = true;
var NORMAL = 0x00;
var BOLD = 0x01;
var UNDERLINE = 0x02;
var INVERT = 0x04;
var LOW = 0x08;
var COLOR = 0xff0;
var COLOR_FG = 0xf0;
var COLOR_BG = 0xf00;
var PROTECTED = 0x1000;
var GUARDED = 0x2000;
var DOUBLE_HEIGHT_TOP = 0x01;
var DOUBLE_HEIGHT_BOTTOM = 0x02;
var DOUBLE_WIDTH = 0x04;
var COLOR_BOLD = 8;
var COLOR_INVERT = 9;
var COLOR_FG_STD = 7;
var COLOR_BG_STD = 0;


//
// VDUInput
//
VDUInput = function() {};
VDUInput.KEY_CONTROL = 0x01;
VDUInput.KEY_SHIFT = 0x02;
VDUInput.KEY_ALT = 0x04;
VDUInput.KEY_ACTION = 0x08;
VDUInput.MOUSE_1 = 0x10;
VDUInput.MOUSE_2 = 0x20;
VDUInput.MOUSE_3 = 0x40;

//
// VDUKeyEvent
//
VDUKeyEvent = function() {};
VDUKeyEvent.VK_PAUSE = 0x13;
VDUKeyEvent.VK_ESCAPE = 0x1B;
VDUKeyEvent.VK_F1 = 0x70;
VDUKeyEvent.VK_F2 = 0x71;
VDUKeyEvent.VK_F3 = 0x72;
VDUKeyEvent.VK_F4 = 0x73;
VDUKeyEvent.VK_F5 = 0x74;
VDUKeyEvent.VK_F6 = 0x75;
VDUKeyEvent.VK_F7 = 0x76;
VDUKeyEvent.VK_F8 = 0x77;
VDUKeyEvent.VK_F9 = 0x78;
VDUKeyEvent.VK_F10 = 0x79;
VDUKeyEvent.VK_F11 = 0x7A;
VDUKeyEvent.VK_F12 = 0x7B;
VDUKeyEvent.VK_LEFT = 0x25;
VDUKeyEvent.VK_PAGE_UP = 0x21;
VDUKeyEvent.VK_PAGE_DOWN = 0x22;
VDUKeyEvent.VK_UP = 0x26;
VDUKeyEvent.VK_RIGHT = 0x27;
VDUKeyEvent.VK_DOWN = 0x28;
VDUKeyEvent.VK_INSERT = 0x9B;
VDUKeyEvent.VK_DELETE = 0x7F;
VDUKeyEvent.VK_ENTER = 0x0A;
VDUKeyEvent.VK_BACK_SPACE = 0x08;
VDUKeyEvent.VK_TAB = 0x09;
VDUKeyEvent.VK_END = 0x23;
VDUKeyEvent.VK_HOME = 0x24;
VDUKeyEvent.VK_CAPS_LOCK = 0x14;
VDUKeyEvent.VK_SHIFT = 0x10;
VDUKeyEvent.VK_CONTROL = 0x11;
VDUKeyEvent.VK_ALT = 0x12;
VDUKeyEvent.VK_NUM_LOCK = 0x90;
VDUKeyEvent.VK_NUMPAD0 = 0x60;
VDUKeyEvent.VK_NUMPAD1 = 0x61;
VDUKeyEvent.VK_NUMPAD2 = 0x62;
VDUKeyEvent.VK_NUMPAD3 = 0x63;
VDUKeyEvent.VK_NUMPAD4 = 0x64;
VDUKeyEvent.VK_NUMPAD5 = 0x65;
VDUKeyEvent.VK_NUMPAD6 = 0x66;
VDUKeyEvent.VK_NUMPAD7 = 0x67;
VDUKeyEvent.VK_NUMPAD8 = 0x68;
VDUKeyEvent.VK_NUMPAD9 = 0x69;
VDUKeyEvent.VK_ADD = 0x6B;
VDUKeyEvent.VK_DECIMAL = 0x6E;
VDUKeyEvent.VK_SUBTRACT = 0x6D;
VDUKeyEvent.VK_NUMPAD_ENTER = 0x6C;
VDUKeyEvent.VK_DIVIDE = 0x6F;
VDUKeyEvent.VK_MULTIPLY = 0x6A;


//
// Cell
//

var Cell = function Cell(x, y) {
	this.x = x;
	this.y = y;
};

var DECSPECIAL = [ '\u0040',
// 5f blank
	'\u2666',
	// 60 black diamond
	'\u2592',
	// 61 grey square
	'\u2409',
	// 62 Horizontal tab (ht) pict. for control
	'\u240c',
	// 63 Form Feed (ff) pict. for control
	'\u240d',
	// 64 Carriage Return (cr) pict. for control
	'\u240a',
	// 65 Line Feed (lf) pict. for control
	'\u00ba',
	// 66 Masculine ordinal indicator
	'\u00b1',
	// 67 Plus or minus sign
	'\u2424',
	// 68 New Line (nl) pict. for control
	'\u240b',
	// 69 Vertical Tab (vt) pict. for control
	'\u2518',
	// 6a Forms light up and left
	'\u2510',
	// 6b Forms light down and left
	'\u250c',
	// 6c Forms light down and right
	'\u2514',
	// 6d Forms light up and right
	'\u253c',
	// 6e Forms light vertical and horizontal
	'\u2594',
	// 6f Upper 1/8 block (Scan 1)
	'\u2580',
	// 70 Upper 1/2 block (Scan 3)
	'\u2500',
	// 71 Forms light horizontal or ?em dash? (Scan 5)
	'\u25ac',
	// 72 \u25ac black rect. or \u2582 lower 1/4 (Scan 7)
	'\u005f',
	// 73 \u005f underscore or \u2581 lower 1/8 (Scan 9)
	'\u251c',
	// 74 Forms light vertical and right
	'\u2524',
	// 75 Forms light vertical and left
	'\u2534',
	// 76 Forms light up and horizontal
	'\u252c',
	// 77 Forms light down and horizontal
	'\u2502',
	// 78 vertical bar
	'\u2264',
	// 79 less than or equal
	'\u2265',
	// 7a greater than or equal
	'\u00b6',
	// 7b paragraph
	'\u2260',
	// 7c not equal
	'\u00a3',
	// 7d Pound Sign (british)
	'\u00b7' ];


var UNIMAP = [
		0x0000,
		// #NULL
		0x0001,
		// #START OF HEADING
		0x0002,
		// #START OF TEXT
		0x0003,
		// #END OF TEXT
		0x0004,
		// #END OF TRANSMISSION
		0x0005,
		// #ENQUIRY
		0x0006,
		// #ACKNOWLEDGE
		0x0007,
		// #BELL
		0x0008,
		// #BACKSPACE
		0x0009,
		// #HORIZONTAL TABULATION
		0x000a,
		// #LINE FEED
		0x000b,
		// #VERTICAL TABULATION
		0x000c,
		// #FORM FEED
		0x000d,
		// #CARRIAGE RETURN
		0x000e,
		// #SHIFT OUT
		0x000f,
		// #SHIFT IN
		0x0010,
		// #DATA LINK ESCAPE
		0x0011,
		// #DEVICE CONTROL ONE
		0x0012,
		// #DEVICE CONTROL TWO
		0x0013,
		// #DEVICE CONTROL THREE
		0x0014,
		// #DEVICE CONTROL FOUR
		0x0015,
		// #NEGATIVE ACKNOWLEDGE
		0x0016,
		// #SYNCHRONOUS IDLE
		0x0017,
		// #END OF TRANSMISSION BLOCK
		0x0018,
		// #CANCEL
		0x0019,
		// #END OF MEDIUM
		0x001a,
		// #SUBSTITUTE
		0x001b,
		// #ESCAPE
		0x001c,
		// #FILE SEPARATOR
		0x001d,
		// #GROUP SEPARATOR
		0x001e,
		// #RECORD SEPARATOR
		0x001f,
		// #UNIT SEPARATOR
		0x0020,
		// #SPACE
		0x0021,
		// #EXCLAMATION MARK
		0x0022,
		// #QUOTATION MARK
		0x0023,
		// #NUMBER SIGN
		0x0024,
		// #DOLLAR SIGN
		0x0025,
		// #PERCENT SIGN
		0x0026,
		// #AMPERSAND
		0x0027,
		// #APOSTROPHE
		0x0028,
		// #LEFT PARENTHESIS
		0x0029,
		// #RIGHT PARENTHESIS
		0x002a,
		// #ASTERISK
		0x002b,
		// #PLUS SIGN
		0x002c,
		// #COMMA
		0x002d,
		// #HYPHEN-MINUS
		0x002e,
		// #FULL STOP
		0x002f,
		// #SOLIDUS
		0x0030,
		// #DIGIT ZERO
		0x0031,
		// #DIGIT ONE
		0x0032,
		// #DIGIT TWO
		0x0033,
		// #DIGIT THREE
		0x0034,
		// #DIGIT FOUR
		0x0035,
		// #DIGIT FIVE
		0x0036,
		// #DIGIT SIX
		0x0037,
		// #DIGIT SEVEN
		0x0038,
		// #DIGIT EIGHT
		0x0039,
		// #DIGIT NINE
		0x003a,
		// #COLON
		0x003b,
		// #SEMICOLON
		0x003c,
		// #LESS-THAN SIGN
		0x003d,
		// #EQUALS SIGN
		0x003e,
		// #GREATER-THAN SIGN
		0x003f,
		// #QUESTION MARK
		0x0040,
		// #COMMERCIAL AT
		0x0041,
		// #LATIN CAPITAL LETTER A
		0x0042,
		// #LATIN CAPITAL LETTER B
		0x0043,
		// #LATIN CAPITAL LETTER C
		0x0044,
		// #LATIN CAPITAL LETTER D
		0x0045,
		// #LATIN CAPITAL LETTER E
		0x0046,
		// #LATIN CAPITAL LETTER F
		0x0047,
		// #LATIN CAPITAL LETTER G
		0x0048,
		// #LATIN CAPITAL LETTER H
		0x0049,
		// #LATIN CAPITAL LETTER I
		0x004a,
		// #LATIN CAPITAL LETTER J
		0x004b,
		// #LATIN CAPITAL LETTER K
		0x004c,
		// #LATIN CAPITAL LETTER L
		0x004d,
		// #LATIN CAPITAL LETTER M
		0x004e,
		// #LATIN CAPITAL LETTER N
		0x004f,
		// #LATIN CAPITAL LETTER O
		0x0050,
		// #LATIN CAPITAL LETTER P
		0x0051,
		// #LATIN CAPITAL LETTER Q
		0x0052,
		// #LATIN CAPITAL LETTER R
		0x0053,
		// #LATIN CAPITAL LETTER S
		0x0054,
		// #LATIN CAPITAL LETTER T
		0x0055,
		// #LATIN CAPITAL LETTER U
		0x0056,
		// #LATIN CAPITAL LETTER V
		0x0057,
		// #LATIN CAPITAL LETTER W
		0x0058,
		// #LATIN CAPITAL LETTER X
		0x0059,
		// #LATIN CAPITAL LETTER Y
		0x005a,
		// #LATIN CAPITAL LETTER Z
		0x005b,
		// #LEFT SQUARE BRACKET
		0x005c,
		// #REVERSE SOLIDUS
		0x005d,
		// #RIGHT SQUARE BRACKET
		0x005e,
		// #CIRCUMFLEX ACCENT
		0x005f,
		// #LOW LINE
		0x0060,
		// #GRAVE ACCENT
		0x0061,
		// #LATIN SMALL LETTER A
		0x0062,
		// #LATIN SMALL LETTER B
		0x0063,
		// #LATIN SMALL LETTER C
		0x0064,
		// #LATIN SMALL LETTER D
		0x0065,
		// #LATIN SMALL LETTER E
		0x0066,
		// #LATIN SMALL LETTER F
		0x0067,
		// #LATIN SMALL LETTER G
		0x0068,
		// #LATIN SMALL LETTER H
		0x0069,
		// #LATIN SMALL LETTER I
		0x006a,
		// #LATIN SMALL LETTER J
		0x006b,
		// #LATIN SMALL LETTER K
		0x006c,
		// #LATIN SMALL LETTER L
		0x006d,
		// #LATIN SMALL LETTER M
		0x006e,
		// #LATIN SMALL LETTER N
		0x006f,
		// #LATIN SMALL LETTER O
		0x0070,
		// #LATIN SMALL LETTER P
		0x0071,
		// #LATIN SMALL LETTER Q
		0x0072,
		// #LATIN SMALL LETTER R
		0x0073,
		// #LATIN SMALL LETTER S
		0x0074,
		// #LATIN SMALL LETTER T
		0x0075,
		// #LATIN SMALL LETTER U
		0x0076,
		// #LATIN SMALL LETTER V
		0x0077,
		// #LATIN SMALL LETTER W
		0x0078,
		// #LATIN SMALL LETTER X
		0x0079,
		// #LATIN SMALL LETTER Y
		0x007a,
		// #LATIN SMALL LETTER Z
		0x007b,
		// #LEFT CURLY BRACKET
		0x007c,
		// #VERTICAL LINE
		0x007d,
		// #RIGHT CURLY BRACKET
		0x007e,
		// #TILDE
		0x007f,
		// #DELETE
		0x00c7,
		// #LATIN CAPITAL LETTER C WITH CEDILLA
		0x00fc,
		// #LATIN SMALL LETTER U WITH DIAERESIS
		0x00e9,
		// #LATIN SMALL LETTER E WITH ACUTE
		0x00e2,
		// #LATIN SMALL LETTER A WITH CIRCUMFLEX
		0x00e4,
		// #LATIN SMALL LETTER A WITH DIAERESIS
		0x00e0,
		// #LATIN SMALL LETTER A WITH GRAVE
		0x00e5,
		// #LATIN SMALL LETTER A WITH RING ABOVE
		0x00e7,
		// #LATIN SMALL LETTER C WITH CEDILLA
		0x00ea,
		// #LATIN SMALL LETTER E WITH CIRCUMFLEX
		0x00eb,
		// #LATIN SMALL LETTER E WITH DIAERESIS
		0x00e8,
		// #LATIN SMALL LETTER E WITH GRAVE
		0x00ef,
		// #LATIN SMALL LETTER I WITH DIAERESIS
		0x00ee,
		// #LATIN SMALL LETTER I WITH CIRCUMFLEX
		0x00ec,
		// #LATIN SMALL LETTER I WITH GRAVE
		0x00c4,
		// #LATIN CAPITAL LETTER A WITH DIAERESIS
		0x00c5,
		// #LATIN CAPITAL LETTER A WITH RING ABOVE
		0x00c9,
		// #LATIN CAPITAL LETTER E WITH ACUTE
		0x00e6,
		// #LATIN SMALL LIGATURE AE
		0x00c6,
		// #LATIN CAPITAL LIGATURE AE
		0x00f4,
		// #LATIN SMALL LETTER O WITH CIRCUMFLEX
		0x00f6,
		// #LATIN SMALL LETTER O WITH DIAERESIS
		0x00f2,
		// #LATIN SMALL LETTER O WITH GRAVE
		0x00fb,
		// #LATIN SMALL LETTER U WITH CIRCUMFLEX
		0x00f9,
		// #LATIN SMALL LETTER U WITH GRAVE
		0x00ff,
		// #LATIN SMALL LETTER Y WITH DIAERESIS
		0x00d6,
		// #LATIN CAPITAL LETTER O WITH DIAERESIS
		0x00dc,
		// #LATIN CAPITAL LETTER U WITH DIAERESIS
		0x00a2,
		// #CENT SIGN
		0x00a3,
		// #POUND SIGN
		0x00a5,
		// #YEN SIGN
		0x20a7,
		// #PESETA SIGN
		0x0192,
		// #LATIN SMALL LETTER F WITH HOOK
		0x00e1,
		// #LATIN SMALL LETTER A WITH ACUTE
		0x00ed,
		// #LATIN SMALL LETTER I WITH ACUTE
		0x00f3,
		// #LATIN SMALL LETTER O WITH ACUTE
		0x00fa,
		// #LATIN SMALL LETTER U WITH ACUTE
		0x00f1,
		// #LATIN SMALL LETTER N WITH TILDE
		0x00d1,
		// #LATIN CAPITAL LETTER N WITH TILDE
		0x00aa,
		// #FEMININE ORDINAL INDICATOR
		0x00ba,
		// #MASCULINE ORDINAL INDICATOR
		0x00bf,
		// #INVERTED QUESTION MARK
		0x2310,
		// #REVERSED NOT SIGN
		0x00ac,
		// #NOT SIGN
		0x00bd,
		// #VULGAR FRACTION ONE HALF
		0x00bc,
		// #VULGAR FRACTION ONE QUARTER
		0x00a1,
		// #INVERTED EXCLAMATION MARK
		0x00ab,
		// #LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
		0x00bb,
		// #RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
		0x2591,
		// #LIGHT SHADE
		0x2592,
		// #MEDIUM SHADE
		0x2593,
		// #DARK SHADE
		0x2502,
		// #BOX DRAWINGS LIGHT VERTICAL
		0x2524,
		// #BOX DRAWINGS LIGHT VERTICAL AND LEFT
		0x2561,
		// #BOX DRAWINGS VERTICAL SINGLE AND LEFT DOUBLE
		0x2562,
		// #BOX DRAWINGS VERTICAL DOUBLE AND LEFT SINGLE
		0x2556,
		// #BOX DRAWINGS DOWN DOUBLE AND LEFT SINGLE
		0x2555,
		// #BOX DRAWINGS DOWN SINGLE AND LEFT DOUBLE
		0x2563,
		// #BOX DRAWINGS DOUBLE VERTICAL AND LEFT
		0x2551,
		// #BOX DRAWINGS DOUBLE VERTICAL
		0x2557,
		// #BOX DRAWINGS DOUBLE DOWN AND LEFT
		0x255d,
		// #BOX DRAWINGS DOUBLE UP AND LEFT
		0x255c,
		// #BOX DRAWINGS UP DOUBLE AND LEFT SINGLE
		0x255b,
		// #BOX DRAWINGS UP SINGLE AND LEFT DOUBLE
		0x2510,
		// #BOX DRAWINGS LIGHT DOWN AND LEFT
		0x2514,
		// #BOX DRAWINGS LIGHT UP AND RIGHT
		0x2534,
		// #BOX DRAWINGS LIGHT UP AND HORIZONTAL
		0x252c,
		// #BOX DRAWINGS LIGHT DOWN AND HORIZONTAL
		0x251c,
		// #BOX DRAWINGS LIGHT VERTICAL AND RIGHT
		0x2500,
		// #BOX DRAWINGS LIGHT HORIZONTAL
		0x253c,
		// #BOX DRAWINGS LIGHT VERTICAL AND HORIZONTAL
		0x255e,
		// #BOX DRAWINGS VERTICAL SINGLE AND RIGHT DOUBLE
		0x255f,
		// #BOX DRAWINGS VERTICAL DOUBLE AND RIGHT SINGLE
		0x255a,
		// #BOX DRAWINGS DOUBLE UP AND RIGHT
		0x2554,
		// #BOX DRAWINGS DOUBLE DOWN AND RIGHT
		0x2569,
		// #BOX DRAWINGS DOUBLE UP AND HORIZONTAL
		0x2566,
		// #BOX DRAWINGS DOUBLE DOWN AND HORIZONTAL
		0x2560,
		// #BOX DRAWINGS DOUBLE VERTICAL AND RIGHT
		0x2550,
		// #BOX DRAWINGS DOUBLE HORIZONTAL
		0x256c,
		// #BOX DRAWINGS DOUBLE VERTICAL AND HORIZONTAL
		0x2567,
		// #BOX DRAWINGS UP SINGLE AND HORIZONTAL DOUBLE
		0x2568,
		// #BOX DRAWINGS UP DOUBLE AND HORIZONTAL SINGLE
		0x2564,
		// #BOX DRAWINGS DOWN SINGLE AND HORIZONTAL DOUBLE
		0x2565,
		// #BOX DRAWINGS DOWN DOUBLE AND HORIZONTAL SINGLE
		0x2559,
		// #BOX DRAWINGS UP DOUBLE AND RIGHT SINGLE
		0x2558,
		// #BOX DRAWINGS UP SINGLE AND RIGHT DOUBLE
		0x2552,
		// #BOX DRAWINGS DOWN SINGLE AND RIGHT DOUBLE
		0x2553,
		// #BOX DRAWINGS DOWN DOUBLE AND RIGHT SINGLE
		0x256b,
		// #BOX DRAWINGS VERTICAL DOUBLE AND HORIZONTAL SINGLE
		0x256a,
		// #BOX DRAWINGS VERTICAL SINGLE AND HORIZONTAL DOUBLE
		0x2518,
		// #BOX DRAWINGS LIGHT UP AND LEFT
		0x250c,
		// #BOX DRAWINGS LIGHT DOWN AND RIGHT
		0x2588,
		// #FULL BLOCK
		0x2584,
		// #LOWER HALF BLOCK
		0x258c,
		// #LEFT HALF BLOCK
		0x2590,
		// #RIGHT HALF BLOCK
		0x2580,
		// #UPPER HALF BLOCK
		0x03b1,
		// #GREEK SMALL LETTER ALPHA
		0x00df,
		// #LATIN SMALL LETTER SHARP S
		0x0393,
		// #GREEK CAPITAL LETTER GAMMA
		0x03c0,
		// #GREEK SMALL LETTER PI
		0x03a3,
		// #GREEK CAPITAL LETTER SIGMA
		0x03c3,
		// #GREEK SMALL LETTER SIGMA
		0x00b5,
		// #MICRO SIGN
		0x03c4,
		// #GREEK SMALL LETTER TAU
		0x03a6,
		// #GREEK CAPITAL LETTER PHI
		0x0398,
		// #GREEK CAPITAL LETTER THETA
		0x03a9,
		// #GREEK CAPITAL LETTER OMEGA
		0x03b4,
		// #GREEK SMALL LETTER DELTA
		0x221e,
		// #INFINITY
		0x03c6,
		// #GREEK SMALL LETTER PHI
		0x03b5,
		// #GREEK SMALL LETTER EPSILON
		0x2229,
		// #INTERSECTION
		0x2261,
		// #IDENTICAL TO
		0x00b1,
		// #PLUS-MINUS SIGN
		0x2265,
		// #GREATER-THAN OR EQUAL TO
		0x2264,
		// #LESS-THAN OR EQUAL TO
		0x2320,
		// #TOP HALF INTEGRAL
		0x2321,
		// #BOTTOM HALF INTEGRAL
		0x00f7,
		// #DIVISION SIGN
		0x2248,
		// #ALMOST EQUAL TO
		0x00b0,
		// #DEGREE SIGN
		0x2219,
		// #BULLET OPERATOR
		0x00b7,
		// #MIDDLE DOT
		0x221a,
		// #SQUARE ROOT
		0x207f,
		// #SUPERSCRIPT LATIN SMALL LETTER N
		0x00b2,
		// #SUPERSCRIPT TWO
		0x25a0,
		// #BLACK SQUARE
		0x00a0 ];


/*
 * Color
 *
 * Based loosely on :-
 *
 * A class to parse color values
 *
 * @author Stoyan Stefanov <sstoo@gmail.com>
 * @link   http://www.phpied.com/rgb-color-parser-in-javascript/
 * @license Use it if you like it
*/
var Color = function(r, g, b) {
	if(typeof r  == 'string')
		this.parse(r);
	else {	
		this.r = r;
		this.g = g;
		this.b = b;
	}
};

Color.prototype.parse = function(color_string) {

    // strip any leading #
    if (color_string.charAt(0) == '#') { // remove # if any
        color_string = color_string.substr(1,6);
    }

    color_string = color_string.replace(/ /g,'');
    color_string = color_string.toLowerCase();

    // before getting into regexps, try simple matches
    // and overwrite the input
    var simple_colors = {
        aliceblue: 'f0f8ff',
        antiquewhite: 'faebd7',
        aqua: '00ffff',
        aquamarine: '7fffd4',
        azure: 'f0ffff',
        beige: 'f5f5dc',
        bisque: 'ffe4c4',
        black: '000000',
        blanchedalmond: 'ffebcd',
        blue: '0000ff',
        blueviolet: '8a2be2',
        brown: 'a52a2a',
        burlywood: 'deb887',
        cadetblue: '5f9ea0',
        chartreuse: '7fff00',
        chocolate: 'd2691e',
        coral: 'ff7f50',
        cornflowerblue: '6495ed',
        cornsilk: 'fff8dc',
        crimson: 'dc143c',
        cyan: '00ffff',
        darkblue: '00008b',
        darkcyan: '008b8b',
        darkgoldenrod: 'b8860b',
        darkgray: 'a9a9a9',
        darkgreen: '006400',
        darkkhaki: 'bdb76b',
        darkmagenta: '8b008b',
        darkolivegreen: '556b2f',
        darkorange: 'ff8c00',
        darkorchid: '9932cc',
        darkred: '8b0000',
        darksalmon: 'e9967a',
        darkseagreen: '8fbc8f',
        darkslateblue: '483d8b',
        darkslategray: '2f4f4f',
        darkturquoise: '00ced1',
        darkviolet: '9400d3',
        deeppink: 'ff1493',
        deepskyblue: '00bfff',
        dimgray: '696969',
        dodgerblue: '1e90ff',
        feldspar: 'd19275',
        firebrick: 'b22222',
        floralwhite: 'fffaf0',
        forestgreen: '228b22',
        fuchsia: 'ff00ff',
        gainsboro: 'dcdcdc',
        ghostwhite: 'f8f8ff',
        gold: 'ffd700',
        goldenrod: 'daa520',
        gray: '808080',
        green: '008000',
        greenyellow: 'adff2f',
        honeydew: 'f0fff0',
        hotpink: 'ff69b4',
        indianred : 'cd5c5c',
        indigo : '4b0082',
        ivory: 'fffff0',
        khaki: 'f0e68c',
        lavender: 'e6e6fa',
        lavenderblush: 'fff0f5',
        lawngreen: '7cfc00',
        lemonchiffon: 'fffacd',
        lightblue: 'add8e6',
        lightcoral: 'f08080',
        lightcyan: 'e0ffff',
        lightgoldenrodyellow: 'fafad2',
        lightgrey: 'd3d3d3',
        lightgreen: '90ee90',
        lightpink: 'ffb6c1',
        lightsalmon: 'ffa07a',
        lightseagreen: '20b2aa',
        lightskyblue: '87cefa',
        lightslateblue: '8470ff',
        lightslategray: '778899',
        lightsteelblue: 'b0c4de',
        lightyellow: 'ffffe0',
        lime: '00ff00',
        limegreen: '32cd32',
        linen: 'faf0e6',
        magenta: 'ff00ff',
        maroon: '800000',
        mediumaquamarine: '66cdaa',
        mediumblue: '0000cd',
        mediumorchid: 'ba55d3',
        mediumpurple: '9370d8',
        mediumseagreen: '3cb371',
        mediumslateblue: '7b68ee',
        mediumspringgreen: '00fa9a',
        mediumturquoise: '48d1cc',
        mediumvioletred: 'c71585',
        midnightblue: '191970',
        mintcream: 'f5fffa',
        mistyrose: 'ffe4e1',
        moccasin: 'ffe4b5',
        navajowhite: 'ffdead',
        navy: '000080',
        oldlace: 'fdf5e6',
        olive: '808000',
        olivedrab: '6b8e23',
        orange: 'ffa500',
        orangered: 'ff4500',
        orchid: 'da70d6',
        palegoldenrod: 'eee8aa',
        palegreen: '98fb98',
        paleturquoise: 'afeeee',
        palevioletred: 'd87093',
        papayawhip: 'ffefd5',
        peachpuff: 'ffdab9',
        peru: 'cd853f',
        pink: 'ffc0cb',
        plum: 'dda0dd',
        powderblue: 'b0e0e6',
        purple: '800080',
        red: 'ff0000',
        rosybrown: 'bc8f8f',
        royalblue: '4169e1',
        saddlebrown: '8b4513',
        salmon: 'fa8072',
        sandybrown: 'f4a460',
        seagreen: '2e8b57',
        seashell: 'fff5ee',
        sienna: 'a0522d',
        silver: 'c0c0c0',
        skyblue: '87ceeb',
        slateblue: '6a5acd',
        slategray: '708090',
        snow: 'fffafa',
        springgreen: '00ff7f',
        steelblue: '4682b4',
        tan: 'd2b48c',
        teal: '008080',
        thistle: 'd8bfd8',
        tomato: 'ff6347',
        turquoise: '40e0d0',
        violet: 'ee82ee',
        violetred: 'd02090',
        wheat: 'f5deb3',
        white: 'ffffff',
        whitesmoke: 'f5f5f5',
        yellow: 'ffff00',
        yellowgreen: '9acd32'
    };
    for (var key in simple_colors) {
        if (color_string == key) {
            color_string = simple_colors[key];
        }
    }
    // emd of simple type-in colors

    // array of color definition objects
    var color_defs = [
        {
            re: /^rgb\((\d{1,3}),\s*(\d{1,3}),\s*(\d{1,3})\)$/,
            example: ['rgb(123, 234, 45)', 'rgb(255,234,245)'],
            process: function (bits){
                return [
                    parseInt(bits[1]),
                    parseInt(bits[2]),
                    parseInt(bits[3])
                ];
            }
        },
        {
            re: /^(\w{2})(\w{2})(\w{2})$/,
            example: ['#00ff00', '336699'],
            process: function (bits){
                return [
                    parseInt(bits[1], 16),
                    parseInt(bits[2], 16),
                    parseInt(bits[3], 16)
                ];
            }
        },
        {
            re: /^(\w{1})(\w{1})(\w{1})$/,
            example: ['#fb0', 'f0f'],
            process: function (bits){
                return [
                    parseInt(bits[1] + bits[1], 16),
                    parseInt(bits[2] + bits[2], 16),
                    parseInt(bits[3] + bits[3], 16)
                ];
            }
        }
    ];

    // search through the definitions to find a match
    for (var i = 0; i < color_defs.length; i++) {
        var re = color_defs[i].re;
        var processor = color_defs[i].process;
        var bits = re.exec(color_string);
        if (bits) {
            channels = processor(bits);
            this.r = channels[0];
            this.g = channels[1];
            this.b = channels[2];
            this.ok = true;
        }

    }

    // validate/cleanup values
    this.r = (this.r < 0 || isNaN(this.r)) ? 0 : ((this.r > 255) ? 255 : this.r);
    this.g = (this.g < 0 || isNaN(this.g)) ? 0 : ((this.g > 255) ? 255 : this.g);
    this.b = (this.b < 0 || isNaN(this.b)) ? 0 : ((this.b > 255) ? 255 : this.b);
};

Color.prototype.darken = function() {
	var r = parseInt(Math.max(this.r * 0.8, 0.0), 10);
	var g = parseInt(Math.max(this.g * 0.8, 0.0), 10);
	var b = parseInt(Math.max(this.b * 0.8, 0.0), 10);
	return new Color(r, g, b);	
};

Color.prototype.toHTMLColor = function() {
	return '#' + this._pad(this.r.toString(16)) + this._pad(this.g.toString(16)) + this._pad(this.b.toString(16));
};

Color.prototype._pad = function(i) {
	if(('' + i).length < 2)
		return '0' + i;
	return i;
};

Color.prototype.equals = function(other) {
	return this.r == other.r && this.g == other.g && this.b == other.b; 
};

Color.LIGHT_GRAY = new Color(192, 192, 192);
Color.WHITE = new Color(255, 255, 255);
Color.BLACK = new Color(0, 0, 0);
Color.RED = new Color(255, 0, 0);
Color.GREEN = new Color(0, 255, 0);
Color.BLUE = new Color(0, 0, 255);
Color.YELLOW = new Color(255, 255, 0);
Color.MAGENTA = new Color(255, 0, 255);
Color.CYAN = new Color(0, 255, 255);


//
// Font
//

var Font = function(family, style, size) {
	this.family = family;
	this.style = style;
	this.size = size;
}
Font.PLAIN = 0;
Font.BOLD = 1;
Font.ITALIC = 2;

//
// ClientWebDisplay
//

var ClientWebDisplay = function(element, options) {
	
	// Local
	var defaultFocused = true;

	// Public
	this.resizeStrategy = RESIZE_SCREEN;
	this.zoom = 1;
	this.stickyModifiers = [ VDUInput.KEY_CONTROL ];
	this.stickyDelay = 2000;
	this.beepAudioResource = 'resources/beep.wav';;
	this.colors = new Array(Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE,
			Color.MAGENTA, Color.CYAN, Color.LIGHT_GRAY, null, null);
	this.contextMenu = false;
	this.cursorStyle = CURSOR_BLOCK;
	this.mouseWheelIncrement = 3;
	
	// Private
	this._tabIndex = 0;
	this._charWidth = 10;
	this._charHeight = 10;
	this._stickModifiers = false;
	this._stickyTimer = null;
	this._buffer = null;
	this._scrollBar = null;
	this._scrollTimer = null;
	this._center = true;
	this._reverseVideo = false;
	this._cursorBackground = Color.BLACK;
	this._cursorForeground = Color.WHITE;
	this._colorPrinting = true;
	this._htmlBuffer = '';
	this._html = '';
	this._selectVertical = false;
	this._cursorOn = true;
	this._cursorTimer = null;
	this._clickTimer = null;
	this._clickCount = 0;
	this._updateSinceLastRender = true;
	this._width = 0;
	this._height = 0;
	this._isCtrlPressed = this._isAltPressed = this._isAltGrPressed = false;
	this._buttonMask = 0; 
	
	if(typeof element == 'string')
		this._element = document.getElementById(element);
	else
		this._element = element;
	
	/* We need a line height to be able to correctly calculate character sizes */
	var css = window.getComputedStyle(this._element);
	var lh = css.getPropertyValue('line-height');
	if('normal' != lh) {
		var scopeVal = parseInt(lh, 10);
		if(isNaN(scopeVal)) {
			this._element.style.lineHeight = '1.2em';
		}
	}

	this._inner = document.createElement('div');
	this._inner.className = 'termInner';
	this._inner.style.fontSize = '1em';
	this._element.appendChild(this._inner);
	
	if(options) {
		if('resizeStrategy' in options)
			this.resizeStrategy = options['resizeStrategy'];
		if('zoom' in options)
			this.resizeStrategy = options['zoom'];
		if('stickyModifiers' in options)
			this.stickyModifiers = options['stickyModifiers'];
		if('stickyDelay' in options)
			this.stickyDelay = options['stickyDelay'];
		if('colors' in options) {
			this.colors = options['colors'];
			if(this.colors.length != 10)
				throw 'colors option must contain exactly 3 Color() objects';
		}
		if('cursorStyle' in options) {
			this.cursorStyle = options['cursorStyle'];	
		}
		
	}
	
	this._element.addEventListener('focus', this._focused, false);
	this._element.addEventListener('blur', this._blurred, false);
	
	/* Make the element focusable */
	if(this._element.tabIndex == -1) {
		TermUtil.log(LVL_INFO, "Making element focusable to get keyboard events");
		this._element.tabIndex = 0;
		this._element.focus();
	}
	
	var self = this;
	document.addEventListener('keyup', function(e) {
		self._t_keyup(e);
	}, true);
	document.addEventListener('keydown', function(e) {
		self._t_keydown(e);
	}, true);
	document.addEventListener('keypress', function(e) {
		self._t_keypress(e);
	}, true);
	
	// Mouse events

	this._inner.addEventListener("wheel", e => {
	    const delta = Math.sign(e.deltaY);
		TermUtil.log(LVL_DEBUG,'X: '  + e.offsetX + ' Y: ' + e.offsetY + ' ' + delta);
		
		if (self.getModifiers() & VDUInput.KEY_CONTROL)
			self.setZoom(self.zoom + ( delta / 25 ));
		else {
			if(self._buffer._alternateBuffer && self._buffer.alternateScrollMode) {
				if(delta > 0)
					for(i = 0 ; i < self.mouseWheelIncrement ; i++)
						self._buffer.writeSpecial(self._buffer.KeyUp[0]);
				else
					for(var i = 0 ; i < self.mouseWheelIncrement ; i++) 
						self._buffer.writeSpecial(self._buffer.KeyDown[0]);
				return true;				
			}
		}
		//else
		//	scrollbar.scroller("scroll", delta * -1);
		return false;
	});
	this._inner.addEventListener('mousemove', function(e) {
		self._fixWhich(e);
		
		TermUtil.log(LVL_DEBUG,'X: '  + e.offsetX + ' Y: ' + e.offsetY);

		if(self._buttonMask != 0) {
			
			var amt = self._charHeight / 2;
			
			// Dragging
			if (e.offsetY < amt) {
				self._setAutoScroll(REVERSE_AUTO_SCROLL, 10, e.offsetX, e.offsetY);
			} else if (e.offsetY > self._element.offsetHeight - amt ) {
				self._setAutoScroll(FORWARD_AUTO_SCROLL, 10, e.offsetX, e.offsetY);
			} else {
				self._setAutoScroll(NO_AUTO_SCROLL, 0, e.offsetX, e.offsetY);
			}
	
			if (e.which == 1 || self.getModifiers() == 0) {
				self._selectVertical = self.getModifiers() & VDUInput.KEY_CONTROL;
				self._recalcSelection(e.offsetX, e.offsetY, 0.33);
			}
		}
		
		
	});
	
	this._inner.addEventListener('mousedown', function(e) {
		self._fixWhich(e);
		
		var cc = self._calcScreenForMouse(e.offsetX, e.offsetY, 0.33);

        switch(e.which) {
        case 1:
        	self._buttonMask |= VDUInput.MOUSE_1;
        	
        	if(!self._isFocused()) {
        		self._element.focus();
        	}
        	
        	break;
        case 2:
        	self._buttonMask |= VDUInput.MOUSE_2;
        	break;
        case 3:
        	self._buttonMask |= VDUInput.MOUSE_3;
        	break;
        }
        
        self._buffer.mouseUpdate(cc.x, cc.y, self._buttonMask | self.getModifiers());

        if(e.which == 1) {
        	self._buffer.selectBegin.x = cc.x;
        	self._buffer.selectBegin.y = cc.y + self._buffer.windowBase;
        	self._buffer.selectEnd.x = self._buffer.selectBegin.x;
        	self._buffer.selectEnd.y = self._buffer.selectBegin.y;
        	self._buffer.selectPoint.x = self._buffer.selectBegin.x;
        	self._buffer.selectPoint.y = self._buffer.selectBegin.y;
        	
        }	
        
        return self._noBubble(e);
		
	});
	
	document.oncontextmenu = document.body.oncontextmenu = function() {

		
		/* TODO maybe allow the context menu IF the mouse is over some selected text..
		 * This will effectively give us 'Copy' which would be the main reason
		 * Maybe also allow it over a link. */
		
		if(!self.contextMenu)
			return false;
		
		self._buttonMask = 0;
		return true;
	}


	this._inner.addEventListener("touchstart", terminalTouchHandler, true);
	this._inner.addEventListener("touchmove", terminalTouchHandler, true);
	this._inner.addEventListener("touchend", terminalTouchHandler, true);
	this._inner.addEventListener("touchcancel", terminalTouchHandler, true);    
	this._inner.addEventListener('mouseup', function(e) {
		
		self._fixWhich(e);
		self._recalcSelection(e.offsetX, e.offsetY, 0.33);
		
		var cc = self._calcScreenForMouse(e.offsetX, e.offsetY, 0);
    	
		switch(e.which) {
        case 1:
        	self._buttonMask &= ~VDUInput.MOUSE_1;        	
        	break;
        case 2:
        	self._buttonMask &= ~VDUInput.MOUSE_2;
        	break;
        case 3:
        	self._buttonMask &= ~VDUInput.MOUSE_3;
        	break;
        }

        self._buffer.mouseUpdate(cc.x, cc.y, self._buttonMask | self.getModifiers());
		self._setAutoScroll(NO_AUTO_SCROLL, 0, 0, 0);
		
        if(e.which == 1) {
    		self._element.focus();

        	if(self._clickTimer != null) {
        		window.clearInterval(self._clickTimer);
        	}
        	self._clickCount++;
        	self._clickTimer = window.setTimeout(function() {
        		TermUtil.log(LVL_INFO, 'End of click timeout. Count ' + self._clickCount + ', sel = ' + self._buffer.selection);
        		if(self._clickCount == 1 && ( !self._buffer.selection || self._buffer.selection == '') )
		        	self._buffer.dispatchEvent('action');
        		self._clickCount = 0;
        	}, MULTI_CLICK_TIME);
        	
        	if(self._clickCount == 2) {
        		self._selectWord(e.offsetX, e.offsetY);
        	}
        	else if(self._clickCount == 3) {
        		self._selectLine(e.offsetX, e.offsetY);
        	}
        	else if(self._clickCount > 3) {
        		//
        	}
        	else {	        	
        	
				if ((self._buffer.selectBegin.x == self._buffer.selectEnd.x) && (self._buffer.selectBegin.y == self._buffer.selectEnd.y)) {
					
					/* TODO - this is what prevents the menu disappearing on the first click
					 *  in HS. Very strange. It can only be because the HTML gets replaced
					 *  at this point. Investigate why. In the meantime, deferring the update
					 *  slightly works around the problem
					 */ 
					window.setTimeout(function() {
						self._buffer.update[0] = true;
						self.redraw();
					}, 1);
					
					return self._noBubble(e);
				}
				
				// fix end.x and end.y, they can get over the border
				if (self._buffer.selectEnd.x < 0) {
					self._buffer.selectEnd.x = 0;
				}
				if (self._buffer.selectEnd.y < 0) {
					self._buffer.selectEnd.y = 0;
				}
				if (self._buffer.selectEnd.y >= self._buffer.charArray.length) {
					self._buffer.selectEnd.y = self._buffer.charArray.length - 1;
				}
				if (self._buffer.selectEnd.x > self._buffer.charArray[0].length) {
					self._buffer.selectEnd.x = self._buffer.charArray[0].length;
				}
				
				self._buildSelectionText();
        	}
		}

        return self._noBubble(e);
	});
	
	// TODO yes yes .. i know.. should be done properly, but how?
	this._ie = false;
	this._firefox = false;
	this._opera = false;
	this._safari = false;
	this._chrome = false;
	this._ns = false;
	this._ns6 = false;
	this._ieVersion = this._getInternetExplorerVersion();
	if (this._ieVersion >= 1) {
		this._ie = true;
	} else if ((i = navigator.userAgent.indexOf('Firefox')) >= 0) {
		TermUtil.log(LVL_INFO, "Detected as Firefox");
		this._firefox = true;
	} else if ((i = navigator.userAgent.indexOf('Opera')) >= 0) {
		TermUtil.log(LVL_INFO, "Detected as Opera");
		this._opera = true;
	} else if ((i = navigator.userAgent.indexOf('Chrome')) >= 0) {
		TermUtil.log(LVL_INFO, "Detected as Chrome");
		this._chrome = true;
	} else if ((i = navigator.userAgent.indexOf('Safari')) >= 0) {
		TermUtil.log(LVL_INFO, "Detected as Safari");
		this._safari = true;
	} else if(navigator.appName.indexOf("Netscape") >= 0 && _info != null && ((_info.indexOf("Win") > 0 && _info.indexOf("Win16") < 0 && java.lang.System.getProperty("os.version").indexOf("3.5") < 0) || (_info.indexOf("Sun") > 0) || (_info.indexOf("Linux") > 0) || (_info.indexOf("AIX") > 0) || (_info.indexOf("OS/2") > 0) || (_info.indexOf("IRIX") > 0))) {
		TermUtil.log(LVL_INFO, "Detected as Netscape");
		this._ns = true;
		this._ns6 = ((this._ns == true) && (_info.indexOf("Mozilla/5") >= 0));
	}

	if(options && 'cursorBlink' in options)
		this.setCursorBlink(options['cursorBlink']);
};

ClientWebDisplay.prototype.init = function(buffer) {
	this._buffer = buffer;
	var self = this;
	this._buffer.addEventListener('resize', function() {
		self.resized();
	});
	this._buffer.addEventListener('bufferChange', function() {
		self.redraw();
	});
	this._buffer.addEventListener('windowBaseChanged', function(windowBase, currentVDURows) {
		self.redraw();
	});
	this._buffer.addEventListener('titleChange', function() {
		self._setWindowTitle();
	});
	this._setWindowTitle();
	if(this.resizeStrategy == RESIZE_SCREEN)
		return this.calcTerminalSize();
	else
		return null;
};

ClientWebDisplay.prototype.writeText = function(s, attributes) {
	if(attributes instanceof Color) {
		var i = this.colors.indexOf(attributes);
		attributes = NORMAL;
		if(i != -1) {
			attributes &= ~COLOR_FG;
			attributes |= (i + 1) << 4;
		}
	} 
	var len = s.length;
	if (len > 0) {
		for (var i = 0; i < len; i++) {
			var ch = s.charAt(i);
			var cx = this._buffer.cursorX;
			var cy = this._buffer.cursorY;
			if(cx + 1 >= this._buffer.width) {
				cx = 0;
				cy++;
			}
			if(ch == '\r')
				cx = 0;
			else if(ch == '\n')
				cy++;
			else {
				this._buffer._writeChar(cx, cy, ch, attributes);
				cx++;
			}
			this._buffer.setCursorPosition(cx, cy);
		}
		this._buffer.dispatchEvent('bufferChange');
	}
}

ClientWebDisplay.prototype.beep = function() {
	try {
		var audio = new Audio(this.beepAudioResource);
		audio.play();
	}
	catch(e) {}
}

ClientWebDisplay.prototype.getCharacterColumns = function(ch) {
	// TODO
	return 1;
};

ClientWebDisplay.prototype.setZoom = function(zoom) {
	this.zoom = zoom;
	this._inner.style.fontSize = this.zoom + 'em';
	TermUtil.log(LVL_INFO,'Zoom now ' + zoom);
	this._clearStickyTimer();
	this._buffer.dispatchEvent('resize');
}

ClientWebDisplay.prototype.redraw = function() {
	if (this._buffer == null) {
		return;
	}
	var bufferHeight;
	var bufferWindowBase;
	var bufferUpdate;
	var bufferWidth;
	var xoffset = 0;
	var yoffset = 0;
	var selectStartLine;
	var selectEndLine;
	var charAttributes;
	var charArray;
	var lineAttributes;

	var currBackground = null;
	var currForeground = null;
	var currBold = false;
	var currUnderline = false;
	var currSelection = false;

	var cursorY = this._buffer.cursorY;
	var cursorX = this._buffer.cursorX;
	
	var defaultBG = this._getBackgroundColor();
	var defaultFG = this._getColor();

	bufferHeight = this._buffer.height;
	bufferWindowBase = this._buffer.windowBase;
	bufferWidth = this._buffer.width;
	bufferUpdate = this._buffer.update;

	var selectBeginX = this._buffer.selectBegin == null ? 0 : this._buffer.selectBegin.x;
	var selectBeginY = this._buffer.selectBegin == null ? 0 : this._buffer.selectBegin.y;
	var selectEndX = this._buffer.selectEnd == null ? 0 : this._buffer.selectEnd.x;
	var selectEndY = this._buffer.selectEnd == null ? 0 : this._buffer.selectEnd.y;
	if(selectEndY < selectBeginY) {
		var s = selectBeginY;
		selectBeginY = selectEndY;
		selectEndY = s;
		s = selectBeginX;
		selectBeginX = selectEndX;
		selectEndX = s;
	}
	
	selectStartLine = selectBeginY - bufferWindowBase;
	selectEndLine = selectEndY - bufferWindowBase;

	// Copy only the portion of the buffer we are going to paint
	charAttributes = TermUtil.mk2darray(bufferHeight, bufferWidth, 0);
	TermUtil.arraycopy(this._buffer.charAttributes, bufferWindowBase,
			charAttributes, 0, charAttributes.length);
	charArray = TermUtil.mk2darray(bufferHeight, bufferWidth, " ");
	TermUtil.arraycopy(this._buffer.charArray, bufferWindowBase, charArray, 0, charArray.length);
	lineAttributes = TermUtil.mkarray(bufferHeight, 0);
	TermUtil.arraycopy(this._buffer.lineAttributes, bufferWindowBase,
			lineAttributes, 0, lineAttributes.length);

	if (this._html == null || this._html.length != bufferHeight) {
		this._html = TermUtil.mkarray(bufferHeight, "");
	}
		
	for (var bufferLine = 0; bufferLine < bufferHeight; bufferLine++) {

		//
		var htmlLine = '';

		// If not full update or update for this row then skip
		if (!bufferUpdate[0] && !bufferUpdate[bufferLine + 1]) {
			continue;
		}
		this._updateSinceLastRender = true;
		bufferUpdate[bufferLine + 1] = false;

		for (var c = 0; c < bufferWidth; c++) {
			var fg;
			var bg;
			var cursorChar = false;
			var charactersToPrint = 0;
			var bufferColumn = c;
			var currAttr = charAttributes[bufferLine][bufferColumn];
			var currChar = charArray[bufferLine][bufferColumn];
			var currLineAttribute = lineAttributes[bufferLine];
			
			// Work out if any part of the line is selected
			var selectStartColumn = 9999;
			var selectEndColumn = -1;
			var inSelection = false;
			var inSelectionLine = false;
			if (this._buffer.isSelected() && (bufferLine >= selectStartLine) && (bufferLine <= selectEndLine)) {
				selectStartColumn = this._selectVertical ? selectBeginX
						: (((bufferLine == selectStartLine) ? selectBeginX : 0));
				selectEndColumn = this._selectVertical ? selectEndX
						: (((bufferLine == selectEndLine)
								? ((bufferLine == selectStartLine) ? selectEndX : selectEndX)
								: bufferWidth));

				inSelectionLine = true;
				inSelection = c >= selectStartColumn && c < selectEndColumn;
				TermUtil.log(LVL_INFO, 'Sel: ' + selectStartColumn + ' End: ' + selectEndColumn + ' inSelection:' + inSelection + ' ' + c);
			}
			
			// final boolean inSoftFont = sf.inSoftFont(currChar);

			// Underline?
			var underline = (currAttr & UNDERLINE) != 0;

			// Get the cells foreground and background colors.
			if ((currAttr & COLOR_FG) != 0) {
				fg = this.colors[((currAttr & COLOR_FG) >> 4) - 1].darken();
			} else {
				fg = defaultFG;
			}
			if ((currAttr & COLOR_BG) != 0) {
				bg = this.colors[((currAttr & COLOR_BG) >> 8) - 1].darken();
			} else {
				bg = defaultBG;
			}

			// Determine the font
			if ((currAttr & BOLD) != 0) {
				if (null != this.colors[COLOR_BOLD]) {
					fg = defaultFG;
				}
				// else if (inSoftFont) {
				// fg = brighten(foreground);
				// }
			}
			
			// Low intensity
			if ((currAttr & LOW) != 0) {
				fg = fg.darken();
			}

			// Invert
			if ((currAttr & INVERT) != 0) {
				if (null == this.colors[COLOR_INVERT]) {
					var swapc = bg;
					bg = fg;
					fg = swapc;
				} else {
					if (null == this.colors[COLOR_BOLD]) {
						fg = bg;
					} else {
						fg = this.colors[COLOR_BOLD];
					}
					bg = this.colors[COLOR_INVERT];
				}
			}
			// Reverse video
			if (this._cursorOn && cursorY == bufferLine && c == cursorX
					&& this._buffer.showcursor) {
				cursorChar = true;
			} else {
				cursorChar = false;
				if (this._reverseVideo ^ this._buffer.lightBackground) {
					var swapc = fg;
					fg = bg;
					bg = swapc;
				}
			}

			charactersToPrint = this._getRunWidth(null /* sf */,
					this, bufferWidth, charAttributes, charArray,
					bufferLine, bufferColumn, currAttr, selectStartColumn, selectEndColumn, inSelectionLine,
					inSelection, cursorX, cursorY, ' ');

			var isBold = (currAttr & BOLD) != 0;
			//
			
			if (currSelection != inSelection) {
				htmlLine += currSelection ? "</span>" : '<span style="background-color:' + defaultFG.toHTMLColor() + '; color: ' + defaultBG.toHTMLColor() + ';" class="selection">';
				currSelection = inSelection;
			}
			if (currBold != isBold) {
				htmlLine += currBold ? "</b>" : "<b>";
				currBold = isBold;
			}
			if (currUnderline != underline) {
				htmlLine += currUnderline ? "</u>" : "<u>";
				currUnderline = underline;
			}

			if (currBackground == null || !currBackground.equals(bg)
					|| currForeground == null
					|| !currForeground.equals(fg)) {
				if (currBackground != null) {
					htmlLine += "</span>";
					currBackground = null;
				}
				if (currForeground != null) {
					htmlLine += "</span>";
					currForeground = null;
				}
				if (!bg.equals(defaultBG)) {
					htmlLine += "<span style=\"background-color:";
					currBackground = bg;
					htmlLine += currBackground.toHTMLColor();
					htmlLine += "\">";
				}
				if (!fg.equals(defaultFG)) {
					htmlLine += "<span style=\"color:";
					currForeground = fg;
					htmlLine += currForeground.toHTMLColor();
					htmlLine += "\">";
				}
			}
			
			if (cursorChar) {
				if(this.cursorStyle == CURSOR_BLOCK)
					htmlLine += '<span style="background-color:' + defaultFG.toHTMLColor() + '; color: ' + defaultBG.toHTMLColor() + ';" class="cursor">';
				else
					htmlLine += '<span style="text-decoration: underline;" class="cursor">';
			}

			var ca = charArray[bufferLine];
			try {
			var string = ca.join("").substring(bufferColumn, bufferColumn + charactersToPrint);
			if (string.charAt(0) < ' ') {
				var ns = '';
				for (var i = string.length - 1; i >= 0; i--) {
					ns += ' ';;
				}
				string = ns;
			}

			// if (inSoftFont) {
			// // TODO this could be more efficient by generating the
			// whole string as one image
			// for(int i = 0 ; i < string.length(); i++) {
			// char ch = string.charAt(i);
			// switch(ch) {
			// case 'q':
			// ch = '\u2501';;
			// break;
			// }
			// htmlLine.append(ch);
			// // htmlLine.append("<img src=\"t?g&s=");
			// // htmlLine.append(font.getSize() / 2);
			// // htmlLine.append("&f=");
			// // htmlLine.append(URLEncoder.encode(fg.toHTMLColor()));
			// // htmlLine.append("&b=");
			// // htmlLine.append(URLEncoder.encode(bg.toHTMLColor()));
			// // htmlLine.append("&c=");
			// // htmlLine.append((int)string.charAt(i));
			// // htmlLine.append("\" height=\"");
			// // htmlLine.append(font.getSize() / 2);
			// // htmlLine.append("\" width=\"");
			// // htmlLine.append(font.getSize() / 2);
			// // htmlLine.append("\" border=\"0\"/>");
			// }
			// } else {
			string = TermUtil.htmlEscape(string);
			htmlLine += string;
			// }
			
			
			if (cursorChar) {
				htmlLine += '</span>';
			}
			
			
			}
			catch(e) {
				TermUtil.log(LVL_ERROR, 'Something corrupted buffer: ' + e);
			}

			c += (charactersToPrint - 1);
		}
		if (currForeground != null) {
			htmlLine += "</span>";
			currForeground = null;
		}
		if (currBackground != null) {
			htmlLine += "</span>";
			currBackground = null;
		}
		if (currSelection) {
			htmlLine += "</span>";
			currSelection = false;
		}
		if (currUnderline) {
			htmlLine += "</u>";
			currUnderline = false;
		}
		if (currBold) {
			htmlLine += "</b>";
			currBold = false;
		}
		this._html[bufferLine] = htmlLine;
	}

	this._render();
	bufferUpdate[0] = false;
};

ClientWebDisplay.prototype.setCursorBlink = function(blink) {
	if(blink) {
		if(this._cursorTimer == null) {
			this._cursorOn = true;
			this.redraw();
			this._cursorTimer = window.setInterval(this._doBlink, 750, this);
		}
	}
	else {
		if(this._cursorTimer != null)
			window.clearInterval(this._cursorTimer);
		this._cursorTimer == null;
		if(!this._cursorOn) {
			this._cursorOn;
			this.redraw();
		}
			
	}
};

ClientWebDisplay.prototype.getModifiers = function() {
	var m = 0;
	if(this._isCtrlPressed)
		m = m | VDUInput.KEY_CONTROL;
	if(this._isAltPressed)
		m = m | VDUInput.KEY_ALT;
	if(this._isAltGrPressed)
		m = m | VDUInput.KEY_ACTION;
	return m;	 
};

//
// Private
//
ClientWebDisplay.prototype._calcScreenForMouse = function(x, y, xoff) {
	var xoffset =  -( ( xoff ? xoff : 0 ) * this._charWidth );

    var cx = parseInt((x - xoffset) / ( this._charWidth ), 10);
    var cy = parseInt(y / ( this._charHeight ), 10);
    
    TermUtil.log(LVL_INFO, 'Mouse x ' + x + ' = ' + cx + ', y ' + y + ' = ' + cy + ' cw: ' + this._charWidth + 
    		'ch: ' + this._charHeight + ' zoom: ' + this.zoom + ' xoff: ' + xoffset);
    
    return {
    	x: cx,
    	y: cy
    };
};

ClientWebDisplay.prototype._noBubble = function(ev) {
	// TODO Breaks when in iframe, preventing element get keyboard focus
	if(ev.which == 1)
		return true;
	
	ev.cancelBubble=true;
	if (ev.stopPropagation) {
		ev.stopPropagation();
	}
	if (ev.preventDefault) {
		ev.preventDefault();
	}
	return false; 
}

ClientWebDisplay.prototype._setWindowTitle = function() {
	if(this._buffer.windowTitle != null) {
		document.title = this._buffer.windowTitle; 
	}
	else
		document.title = 'Terminal';
};

ClientWebDisplay.prototype._recalcSelection = function(x,y,off) {

	var cc = this._calcScreenForMouse(x, y, off ? off : 0);
	
	var oldsx = this._buffer.selectBegin.x;
	var oldsy = this._buffer.selectBegin.y;
	var oldx = this._buffer.selectEnd.x;
	var oldy = this._buffer.selectEnd.y;
	if ((cc.x <= this._buffer.selectPoint.x) && (cc.y <= this._buffer.selectPoint.y)) {
		oldx = this._buffer.selectBegin.x;
		oldy = this._buffer.selectBegin.y;
		this._buffer.selectBegin.x = cc.x;
		this._buffer.selectBegin.y = cc.y + this._buffer.windowBase;
		this._buffer.selectEnd.x = this._buffer.selectPoint.x;
		this._buffer.selectEnd.y = this._buffer.selectPoint.y;
	} else {
		oldx = this._buffer.selectEnd.x;
		oldy = this._buffer.selectEnd.y;
		this._buffer.selectEnd.x = cc.x;
		this._buffer.selectEnd.y = cc.y + this._buffer.windowBase;
		this._buffer.selectBegin.x = this._buffer.selectPoint.x;
		this._buffer.selectBegin.y = this._buffer.selectPoint.y;
	}
	
	// Sanitise the values
	if (this._buffer.selectBegin.y >= this._buffer.currentVDURows) {
		this._buffer.selectBegin.y = this._buffer.currentVDURows - 1;
	} else if (this._buffer.selectBegin.y < 0) {
		this._buffer.selectBegin.y = 0;
	}
	if (this._buffer.selectEnd.y >= this._buffer.currentVDURows) {
		this._buffer.selectEnd.y = this._buffer.currentVDURows - 1;
	} else if (this._buffer.selectEnd.y < 0) {
		this._buffer.selectEnd.y = 0;
	}
	if (this._buffer.selectBegin.x > this._buffer.width) {
		this._buffer.selectBegin.x = this._bufferwidth - 1;
	} else if (this._buffer.selectBegin.x < 0) {
		this._buffer.selectBegin.x = 0;
	}
	if (this._buffer.selectEnd.x > this._buffer.width) {
		this._buffer.selectEnd.x = this._buffer.width - 1;
	} else if (this._buffer.selectEnd.x < 0) {
		this._buffer.selectEnd.x = 0;
	}

	// Adjust if selection is over double width charactes
	this._checkSelectedPoint(this._buffer.selectBegin);
	this._checkSelectedPoint(this._buffer.selectEnd);

	if (this._buffer.selectBegin.x != oldsx || this._buffer.selectBegin.y != oldsy || this._buffer.selectEnd.x != oldx
			|| this._buffer.selectEnd.y != oldy) {
		this._buffer.update[0] = true;
		TermUtil.log(LVL_DEBUG, 'select([' + this._buffer.selectBegin.x + ',' + this._buffer.selectBegin.y + '],' + '['
					+ this._buffer.selectEnd.x + ',' + this._buffer.selectEnd.y + '])');
		this.redraw();
	}
};

ClientWebDisplay.prototype._checkSelectedPoint = function(point) {
	if (point.y < this._buffer.charArray.length && point.x < this._buffer.charArray[point.y].length) {
		var ch = this._buffer.charArray[point.y][point.x];
		if (ch == ' ' && point.x > 0) {
			ch = this._buffer.charArray[point.y][point.x - 1];
			var cw = this.getCharacterColumns(ch);
			if (cw > 1) {
				point.x = point.x + 1;
			}
		}
	}
}

ClientWebDisplay.prototype._selectWord = function(x, y) {
	this._selectVertical = false;
	var cc = this._calcScreenForMouse(x, y, 0);
	x = cc.x;
	y = cc.y + this._buffer.windowBase;
	
	var charArray = this._buffer.charArray;
	if (y < this._buffer.currentVDURows) {
		var l = charArray[y];
		var i = x;
		for (; (i > -1) && l[i].charCodeAt(0) > 32; i = i - this.getCharacterColumns(charArray[y][i])) {
			;
		}
		if (i != x) {
			this._buffer.selectBegin.y = y;
			this._buffer.selectBegin.x = i + 1;
			this._buffer.selectEnd.y = y;
			var j = i + 1;
			for (; (j < l.length) && l[j].charCodeAt(0) > 32; j = j + this.getCharacterColumns(charArray[y][j])) {
				;
			}
			this._buffer.selectEnd.x = j;
			this._buildSelectionText();
			this._buffer.update[0] = true;
			this.redraw();
		}
	}
}

ClientWebDisplay.prototype._selectLine = function(x, y) {
	this._selectVertical = false;

	var cc = this._calcScreenForMouse(x, y, 0);
	x = cc.x;
	y = cc.y + this._buffer.windowBase;
	
	var charArray = this._buffer.charArray;
	if (y < charArray.length) {
		var l = charArray[y];
		var i = 0;
		for (; (i < l.length) && (l[i].charCodeAt(0) < 33); i++) {
			;
		}
		if (i < l.length) {
			var j = l.length - 1;
			this._buffer.selectBegin.y = y;
			this._buffer.selectEnd.y = y;
			this._buffer.selectBegin.x = i;
			for (; (j >= 0) && (l[j].charCodeAt(0) < 33); j--) {
				;
			}
			this._buffer.selectEnd.x = j + 1;
			this._buildSelectionText();
			this._buffer.update[0] = true;
			this.redraw();
		}
	}
}

ClientWebDisplay.prototype._setAutoScroll = function(direction, delay, x, y) {
	TermUtil.log(LVL_INFO, 'Set auto scroll ' + direction + ', delay: ' + delay + 
			', x: ' + x + ', y: ' + y);

	if (this._scrollTimer != null) {
		window.clearInterval(this._scrollTimer);
		this._scrollTimer = null;
	}
	if (direction != NO_AUTO_SCROLL) {
		var self = this;
		this._scrollTimer = window.setInterval(function() {
			var was = self._buffer.windowBase;
			if (direction == FORWARD_AUTO_SCROLL) {
				self._buffer.setWindowBase(was + 1);

			} else {
				self._buffer.setWindowBase(was - 1);
			}
			
			if(was == self._buffer.windowBase)
				self._setAutoScroll(NO_AUTO_SCROLL, 0, 0, 0);
			
			if (self._buffer.selectBegin.x != 0) {
				self._recalcSelection(x, y);
			}

			self.redraw();
			
		}, delay);
	} 
};

ClientWebDisplay.prototype._buildSelectionText = function() {

	// Swap start and end if end is before start
	var s;
	if (this._buffer.selectBegin.y > this._buffer.selectEnd.y) {
		s = this._buffer.selectBegin.y;
		this._buffer.selectBegin.y = this._buffer.selectEnd.y;
		this._buffer.selectEnd.y = s;
	} else if (this._buffer.selectBegin.y == this._buffer.selectEnd.y && this._buffer.selectBegin.x > this._buffer.selectEnd.x) {
		s = this._buffer.selectBegin.x;
		this._buffer.selectBegin.x = this._buffer.selectEnd.x;
		this._buffer.selectEnd.x = s;
	}

	var b = '';
	var i;
	var start;
	var end;
	var ch;
	for (var l = this._buffer.selectBegin.y; l <= this._buffer.selectEnd.y && l < this._buffer.charArray.length; l++) {
		start = this._selectVertical ? this._buffer.selectBegin.x
				: (((l == this._buffer.selectBegin.y) ? (start = this._buffer.selectBegin.x) : 0));
		end = this._selectVertical ? this._buffer.selectEnd.x
				: (((l == this._buffer.selectEnd.y) ? (end = this._buffer.selectEnd.x) : this._buffer.charArray[l].length));
		
		// Trim all spaces from end of line, like xterm does.
		var sel = this._buffer.charArray[l].join('').substring(start, end).rtrim();
		for (var i = 0; i < sel.length;) {
			ch = sel.charAt(i);
			if (ch.charCodeAt(0) == 0)
				ch = ' ';
			b += ch;
			i += this.getCharacterColumns(ch);
		}
		if (this._selectVertical || (end == this._buffer.charArray[l].length && this._buffer.lineMarker[l])) {
			b += '\n';
		}
	}
	if(b != this._buffer.selection) {
		TermUtil.log(LVL_INFO, 'Selection is now "' + b + '"');
		this._buffer.setSelection(b);
	}
};

ClientWebDisplay.prototype.resized = function() {
	TermUtil.log(LVL_INFO, 'Resized ' + this.resizeStrategy);
	if(RESIZE_FONT == this.resizeStrategy) {
		this._recalculateFontSize();
	}
	else if(RESIZE_SCREEN == this.resizeStrategy) {
		var newSize = this.calcTerminalSize();
		this._buffer.setScreenSize(newSize.width, newSize.height);
	}	    	
};

/**
 * Calculate the font size that will fit given the current space at the current 
 * terminal width and height in characters.
 */
ClientWebDisplay.prototype._recalculateFontSize = function() {
	var elWidth = this._inner.offsetWidth;
	var elHeight = this._inner.offsetHeight;
	this._charWidth = elWidth / this._buffer.width;
	this._charHeight = elHeight / this._buffer.height;
	var sz = Math.min(ch, cw);
	TermUtil.log(LVL_INFO, 'New font size ' + cw + 'x' + ch + ' = ' + cw + ' (em) for char size: ' + charWidth + 'x' + charHeight + ' in ' + elWidth + 'x' + elHeight);
	this._inner.style.fontSize = sz + 'em';
};

/**
 *  Calculate the width of a single character at the current font size. NOTE, this
 *  is a floating point number, as characters won't necessarily be an integer pixel width.
 *  
 *  @return cell width
 */
ClientWebDisplay.prototype._calcCellWidth = function(scope) {
	var scopeTest = document.createElement('div');
	scopeTest.innerHTML = '<pre style="font-family: inherit; display: inline-block; margin: 0 !important; padding: 0 !important;" width="2">WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW</pre>';
	scope.appendChild(scopeTest);
	var scopeVal = parseFloat(scopeTest.firstChild.offsetWidth, 10) / 80.0;
	scope.removeChild(scopeTest);
	return scopeVal;
};

/**
 *  Calculate the height of a single character at the current font size. 
 *  
 *  @return cell width
 */
ClientWebDisplay.prototype._calcCellHeight = function(scope) {
	var scopeTest = document.createElement('div');
	scopeTest.innerHTML = '<pre style="font-family: inherit; margin: 0 !important; padding: 0 !important;" width="1">W</pre>';
	scope.appendChild(scopeTest);
	var scopeVal = parseFloat(scopeTest.firstChild.offsetHeight, 10);
	scope.removeChild(scopeTest);
	return scopeVal;
};

/**
 * Calculate the terminal width and height in characters given the width, height
 * and font size of the container
 * 
 * @return { x, y }
 */
ClientWebDisplay.prototype.calcTerminalSize = function() {
	this._charWidth = this._calcCellWidth(this._inner);
	this._charHeight = this._calcCellHeight(this._inner);
	TermUtil.log(LVL_INFO, 'Calculated char size is ' + this._charWidth + 'x' + this._charHeight + ' for an offset of ' + this._element.offsetWidth + 'x' + this._element.offsetHeight);
	return this._clampTerminalSize({
		width: parseInt(parseInt(this._element.offsetWidth,10) / this._charWidth, 10),
		height: parseInt(parseInt(this._element.offsetHeight,10) / this._charHeight, 10)
	});
};

/**
 * Make sure the terminal size is within sensible value
 */
ClientWebDisplay.prototype._fixWhich = function(e) {
	
	var target  = e.target || e.srcElement,
    rect    = target.getBoundingClientRect(),
    offsetX = e.clientX - rect.left,
    offsetY  = e.clientY - rect.top;

	e.offsetX   = offsetX;
	e.offsetY   = offsetY;
	
	if (!e.which && e.button) {
		if (e.button & 1) e.which = 1      // Left
	    else if (e.button & 4) e.which = 2 // Middle
	    else if (e.button & 2) e.which = 3 // Right
	}
};

ClientWebDisplay.prototype._clampTerminalSize = function(ts) {
    if(isNaN(ts.height) || ts.height < 1 || ts.height > 999) {
    	ts.height = 24;
    }
    if(isNaN(ts.width) || ts.width < 1 || ts.width > 999) {
    	ts.width = 80;
    }
    return ts;
};

ClientWebDisplay.prototype.interruptBlink = function() {
	if (this._cursorTimer != null) {
		TermUtil.log(LVL_INFO, 'Interupt blink');
		window.clearInterval(this._cursorTimer);
		this._cursorOn = true;
		this._buffer.markLine(this._buffer.cursorY, 1);
		this.redraw();
		this._cursorTimer = window.setInterval(this._doBlink, 750, this);
	}
};


ClientWebDisplay.prototype._isFocused = function() {
	return document.activeElement == this._element;
};

ClientWebDisplay.prototype._doBlink = function(self) {
	self._cursorOn = !self._cursorOn;
	TermUtil.log(LVL_DEBUG, 'cursor on: ' + self._cursorOn);
	self._buffer.markLine(self._buffer.cursorY, 1);
	self.redraw();
};

ClientWebDisplay.prototype._getBackgroundColor = function() {
	var css = window.getComputedStyle(this._element);
	var color = css.getPropertyValue('background-color');
	return new Color(color);
}
ClientWebDisplay.prototype._getColor = function() {
	var css = window.getComputedStyle(this._element);
	var color = css.getPropertyValue('color');
	return new Color(color);
}

ClientWebDisplay.prototype._render = function(blink) {
	var defaultBG = this._getBackgroundColor();
	var defaultFG = this._getColor();
	var h = '<pre class="terminalRow" width="';
	h += '' + this._buffer.width;
	h += '" style="font-family: inherit; background-color:';
	h += this._getBackgroundColor().toHTMLColor();
	h += ';color:';
	h += this._getColor().toHTMLColor();
	h += ';margin: 0 !important;padding: 0 !important; overflow: hidden;';
	h += '">';    	
	for(l in this._html)
		h += this._html[l] + '\n';
	h += '</pre>'
	this._inner.innerHTML = h;
};

ClientWebDisplay.prototype._focused = function(evt) {
	TermUtil.log(LVL_INFO, 'Focused');
};

ClientWebDisplay.prototype._blurred = function(evt) {
	TermUtil.log(LVL_INFO, 'Blurred');
};

ClientWebDisplay.prototype._getRunWidth = function(sf, display, bufferWidth, charAttributes,
        charArray, bufferLine, bufferColumn, currAttr,
        selectStartColumn, selectEndColumn, inSelectionLine, inSelectionRange,
        cursorX, cursorY, minCharValue) {
	var previousCharWidth = 1;
	var charactersToPrint = 0;
	while (true) {
		var aheadBufferColumn = bufferColumn + charactersToPrint;
		// If off edge of screen break
		if (aheadBufferColumn >= bufferWidth) {
			break;
		}
	
		// If attributes change then break here
		if (charAttributes[bufferLine][aheadBufferColumn] != currAttr) {
			break;
		}
	
		// If it is a soft font character then break now
		if (sf != null && sf.inSoftFont(charArray[bufferLine][aheadBufferColumn])) {
			break;
		}
	
		// If the next column has the cursor on it then break now
		if (cursorY != -1 && cursorX != -1 && bufferLine == cursorY && aheadBufferColumn == cursorX) {
			break;
		}
		else {	
			// If this is the cursor column, and the next isn't, then break now
			if (cursorY != -1 && cursorX != -1 && bufferLine == cursorY && bufferColumn == cursorX && aheadBufferColumn != cursorX) {
				break;
			}
		}
	
		/*
		 * If we are printing nulls, then everything must be nulls. If we
		 * are printing non nulls, then everything must be non nulls. If
		 * not break
		 */
		if ((charArray[bufferLine][aheadBufferColumn] < minCharValue && charArray[bufferLine][bufferColumn] >= minCharValue)
	      || (charArray[bufferLine][aheadBufferColumn] >= minCharValue && charArray[bufferLine][bufferColumn] < minCharValue)) {
			break;
		}
	
		// If this text either becomes selected or
		// deselected then break here
		if (inSelectionLine) {
			if (inSelectionRange && aheadBufferColumn >= selectEndColumn) {
				break;
			}
			if (!inSelectionRange && aheadBufferColumn >= selectStartColumn && aheadBufferColumn <= selectEndColumn) {
				break;
			}
		}

		// Take character width into account and break if it changes
		var thisCharWidth = display.getCharacterColumns(charArray[bufferLine][aheadBufferColumn]);
		if (previousCharWidth != thisCharWidth) {
			charactersToPrint += thisCharWidth;
			break;
		}
		previousCharWidth = thisCharWidth;
		if (charArray[bufferLine][bufferColumn + charactersToPrint] < ' ') {
			thisCharWidth = display.getCharacterColumns(' ');
		}
		charactersToPrint += thisCharWidth;
	}

	// Must print at least one character
	charactersToPrint = Math.max(1, charactersToPrint);

	return charactersToPrint;
};

ClientWebDisplay.prototype._t_keyup = function(ev) {

	TermUtil.log(LVL_INFO, 'enter keyup: ctrl: ' + this._isCtrlPressed + ' alt: ' + this._isAltPressed + ' altGr: ' + this._isAltGrPressed + ' stick:' + this._stickModifiers);
	
	if(!this._isFocused()) {
		TermUtil.log(LVL_INFO, "Ignoring event, not focused");
		return false;
	}
	
	// Keep track of the current modifier keys. We have to simulate AltGr with Alt+Ctrl. Most 
	// browsers seem to do this.
	if (!ev) 
		var ev=window.event;
	var k = ev.keyCode || ev.which;
	var wasAltGr = this._isAltGrPressed;
	
	if( ( this._isCtrlPressed && !this._isShiftPressed && !this._isAltPressed && !this._isAltGrPressed && k == 67 && (this._buffer.copyMode & ClientTerminalEmulation.COPY_ON_CTRL_C) != 0 ) ||
	    ( this._isCtrlPressed && this._isShiftPressed && !this._isAltPressed && !this._isAltGrPressed && k == 67 && (this._buffer.copyMode & ClientTerminalEmulation.COPY_ON_CTRL_SHIFT_C) != 0 ) ) {
		return this._noBubble(ev);
	}
	
	if(!this._stickModifiers) {
		if(k == 225) {
			this._isAltGrPressed = this._isAltPressed = this._isCtrlPressed = false;
		}
		else if(k == 17) {
			if(this._isAltGrPressed) {
				this._isAltPressed=true;
			}
			this._isCtrlPressed=this._isAltGrPressed=false;
		}
		if(k == 18) {
			if(this._isAltGrPressed) {
				this._isCtrlPressed=true;
			}
			this._isAltPressed=this._isAltGrPressed=false;
		}
	}
	

	if(this._stickyTimer != null && !this._isCtrlPressed && !this._isAltPressed && !this._isAltGrPressed) {
		this._clearStickyTimer();
	}
	
	
	TermUtil.log(LVL_INFO, 'leave keyup: ctrl: ' + this._isCtrlPressed + ' alt: ' + this._isAltPressed + ' altGr: ' + this._isAltGrPressed + ' stick:' + this._stickModifiers);
	return this._isAltGrPressed;
}

ClientWebDisplay.prototype._clearStickyTimer = function() {
	if(this._stickyTimer != null) {
		window.clearTimeout(this._stickyTimer);
		this._stickyTimer = null;
	}
}

ClientWebDisplay.prototype._t_keypress = function(ev) {
	if(!this._isFocused()) {
		TermUtil.log(LVL_DEBUG, "Ignoring press event, not focused");
		return false;
	}
	
	if (!ev) 
		var ev=window.event;
		
	// Key Code - Should be -1 for ordinary ASCII keys
	var kc = 0;
	
	// Key Character - Any ordinary ASCII keys. Empty for NON ascii keys
	var k="";
	
	var alt = this._isAltPressed;
	var altGr = this._isAltGrPressed;
	var ctrl = this._isCtrlPressed;
	var shift = ev.shiftKey;
	
	TermUtil.log(LVL_INFO, 'keypress: ctrl: ' + ctrl + ' alt: ' + alt + ' altGr: ' + altGr + ' shift: ' + shift + ' stick:' + this._stickModifiers);
	
	if (ev.keyCode)
		kc=ev.keyCode;
	if (ev.which && (!ev.type == "keydown" || ev.type == "keypress" ) )
		kc=ev.which;
	
	// If this is just modifiers, skip
	if(kc == 17 || kc == 18 || kc == 225) {
		return true;
	}
	
	if(this._stickModifiers) {
		TermUtil.log(LVL_INFO, 'Clearing stickyness of modifiers');
		this._isCtrlPressed = false;
		this._isAltPressed = false;
		this._isAltGrPressed = false;
		this._stickModifiers = false;
	}
		
	if(this._interpretKey(kc, k, ctrl, shift, alt, altGr, ev.which==0 || ( ev.type == "keydown" ) ))
		return;
	
	// Stop the event propogating
	ev.cancelBubble=true;
	if (ev.stopPropagation) {
		ev.stopPropagation();
	}
	if (ev.preventDefault) {
		ev.preventDefault();
	}
	return false; 
};

ClientWebDisplay.prototype._interpretKey = function(kc, k, ctrl, shift, alt, altGr, keydown) {
	
	// Alt
	if (alt) {
		if(kc == 9 || (this._firefox && kc == 192)) {
			return true;
		}
		else if (kc>=65 && kc<=90) {
			k = String.fromCharCode( ( kc + 32 ) | 0x80);
			kc = -1;
		}
		else if (kc>=97 && kc<=122) {
			k = String.fromCharCode( ( kc + 27 ) | 0x80);
			kc = -1;
		} else {
			if(kc == 51)
				kc = 35;
			
			TermUtil.log(LVL_WARN, 'converting altgr workaround: k = ' + k + ' kc = ' + kc + ' to ' + String.fromCharCode(kc));
			k=String.fromCharCode(kc);
			altGr = true;
			alt = false;
			kc = -1;
		}
	// Ctrl
	} else if (ctrl) {
		// Don't send Ctrl+Tab - let browser / WM handle it
		if(kc == 9) {
			return true; 
		}
		else if (kc>=65 && kc<=90) {
			k=String.fromCharCode(kc-64); // Ctrl-A..Z
		}
		else if (kc>=97 && kc<=122) k=String.fromCharCode(kc-96); // Ctrl-A..Z
		else if (kc==54)  k=String.fromCharCode(30); // Ctrl-^
		else if (kc==109) k=String.fromCharCode(31); // Ctrl-_
		else if (kc==219) k=String.fromCharCode(27); // Ctrl-[
		else if (kc==220) k=String.fromCharCode(28); // Ctrl-\
		else if (kc==221) k=String.fromCharCode(29); // Ctrl-]
		else if (kc==219) k=String.fromCharCode(29); // Ctrl-]
		else if (kc==219) k=String.fromCharCode(0);  // Ctrl-@
		else if (kc==91) k=String.fromCharCode(27); // Ctrl-[
		else if (kc==93) k=String.fromCharCode(29); // Ctrl-]
		else if (kc==47) k=String.fromCharCode(31);  // Ctrl-`
		else if (kc==96) k=String.fromCharCode(30);  // Ctrl-`
		else if (kc==92) k=String.fromCharCode(28);  // Ctrl-`
		
		if(k.length > 0) {
			kc = -1;
			ctrl = false;
		}
	//
	} else if (keydown) {
		if (kc==9) k=String.fromCharCode(9);  // Tab
		else if (kc==8) k=String.fromCharCode(127);  // Backspace
		else if (kc==27) k=String.fromCharCode(27); // Escape
		else {
			if (kc==45) kc=0x9b;   // Ins
			if (kc==46) kc=0x7f;   // Del
		}
	} else {	
		if(kc == 13) {
			kc = 10;
		}
		else {
			if(kc == 16 || kc == 17 || kc == 18) {
				// Stops opera sending modifiers only keys
				k = '';
				kc = -1;
			}
			else if(kc != 8) {
				if(kc == 43) {
					// I have no idea!
					k = '+';
				}
				else {
					k=String.fromCharCode(kc);
				}
				kc = -1;
			}
		}
	}
	
	if(kc >= 0 || k.length > 0) {
		keyMods = 0;
		if(ctrl)
			keyMods += 1;
		if(shift)
			keyMods += 2; 
		if(alt)
			keyMods += 4;
		if(altGr)
			keyMods += 8;
		

		TermUtil.log(LVL_INFO, 'sending keyMods: ' + keyMods + ' keycode: ' + kc + ' keychar: ' + k + ' encoded key: ' + encodeURIComponent(k));
		this._buffer.keyPressed(kc, k, keyMods);
	}
	
	return false;
};

ClientWebDisplay.prototype._t_keydown = function(ev) {
	
	if (!ev) {
		var ev=window.event;
	}
	
	// Keep track of the current modifier keys. We have to simulate AltGr with Alt+Ctrl. Most 
	// browsers seem to do this.
	var k = ev.keyCode || ev.which;
	TermUtil.log(LVL_INFO, 'enter keydown k = ' + k + ' altKey:' + ev.altKey + ' ctrlKey:' + ev.ctrlKey);

	if(!this._isFocused()) {
		TermUtil.log(LVL_DEBUG, "Ignoring event, not focused");
		return false;
	}

	if(!this._stickModifiers) {
		// If the user alt-tabbed away from the browser, then back again, then the alt key
		// state will be wrong. Try and fix it
		if(this._isAltPressed && !ev.altKey) {
			this._isAltPressed = false;
		}
		
		if(k == 225) {
			this._isAltGrPressed = true;
			this._isAltPressed = this._isCtrlPressed = false;
		}
		else if(k == 17) { 
			if(this._isAltPressed) {
				this._isAltGrPressed = true;
				this._isAltPressed = false;
			}
			else {
				this._isCtrlPressed = true;
			}
		}
		else if(k == 18) {
			if(this._isCtrlPressed) {
				this._isAltGrPressed = true;
				this._isCtrlPressed = false;
			}
			else {
				this._isAltPressed = true;
			}
		}
		
		if( ( this._isCtrlPressed && this.stickyModifiers.indexOf(VDUInput.KEY_CONTROL) != -1 ) ||
			( this._isAltPressed && this.stickyModifiers.indexOf(VDUInput.KEY_ALT) != -1 ) ||
			( this._isAltGrPressed && this.stickyModifiers.indexOf(VDUInput.KEY_ACTION)  != -1 ) ) {
			if(this._stickyTimer != null)
				window.clearTimeout(this._stickyTimer);
			var self = this;
			TermUtil.log(LVL_INFO, 'Sticky modifier timer started');
			self._stickyTimer = window.setTimeout(function() {
				self.beep();
				self._stickModifiers = true;
				TermUtil.log(LVL_INFO, 'Fired sticky modifier timer');
			}, this.stickyDelay);
		}
	}
	
	if( ( this._isCtrlPressed && !this._isShiftPressed && !this._isAltPressed && !this._isAltGrPressed && k == 67 && (this._buffer.copyMode & ClientTerminalEmulation.COPY_ON_CTRL_C) != 0 ) ||
	    ( this._isCtrlPressed && this._isShiftPressed && !this._isAltPressed && !this._isAltGrPressed && k == 67 && (this._buffer.copyMode & ClientTerminalEmulation.COPY_ON_CTRL_SHIFT_C) != 0 ) ) {
		this._buffer.copySelectionToClipboard();
		
		/* Focus might get stolen in event handling */
		this._element.focus();
		
		return this._noBubble(ev);
	}

	TermUtil.log(LVL_INFO, 'leave keydown: ctrl: ' + this._isCtrlPressed + ' alt: ' + this._isAltPressed + ' altGr: ' + this._isAltGrPressed + ' stick:' + this._stickModifiers);
	
//		s="kd keyCode="+ev.keyCode+" which="+ev.which+" shiftKey="+ev.shiftKey+" ctrlKey="+ev.ctrlKey+" altKey="+ev.altKey;
//		debug(s);
	o={9:1,8:1,27:1,33:1,34:1,35:1,36:1,37:1,38:1,39:1,40:1,45:1,46:1,112:1,
		113:1,114:1,115:1,116:1,117:1,118:1,119:1,120:1,121:1,122:1,123:1, 17:1 };
	if ((o[ev.keyCode] || ev.ctrlKey || ev.altKey) && !this._isAltGrPressed) {
		ev.which=0; // This has no effect in Safari/Chrome and possibly later IE version so we use an alternative method in t_keypress (check for event type)
		return this._t_keypress(ev);
	}
	
	if(this._isAltGrPressed) {
		return true;
	}
};

ClientWebDisplay.prototype._getInternetExplorerVersion = function(ev) {
	var rv = -1;
	if (navigator.appName == 'Microsoft Internet Explorer') {
		var ua = navigator.userAgent;
		var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
		if (re.exec(ua) != null)
			rv = parseFloat( RegExp.$1 );
	}
	else if (navigator.appName == 'Netscape') {
		var ua = navigator.userAgent;
		var re  = new RegExp("Trident/.*rv:([0-9]{1,}[\.0-9]{0,})");
		if (re.exec(ua) != null)
			rv = parseFloat( RegExp.$1 );
	}
	return rv;
}
ClientWebDisplay.prototype._send_keystroke = function(ev) {
	
}

/*
 * Base interface for all displays
 */
var VDUBuffer = function(display, width, height) {
	this.update = null;
	this.charArray = null;
	this.charAttributes = null;
	this.lineAttributes = null;
	this.lineMarker = null;
	this.currentVDURows = 0; 
	this.fill = 0;
	this.screenBase = 0;
	this.windowBase = 0;
	this.scrollMarker = 0;
	this.selectBegin = null;
	this.selectEnd = null;
	this.selection = null;
	this.selectPoint = 0;
	this.topMargin = 0;
	this.bottomMargin = 0;
	this.showcursor = true;
	this.cursorX = 0;
	this.cursorY = 0;
	this.windowTitle = null;
	this.prevChar = '';
	this.prevAttr = 0;
	this.height = 0;
	this.width = 0;
	
	this._alternateBuffer = null;
	this._alternateCharArray = null;
	this._alternateCharAttributes = null;
	this._alternateLineAttributes = null;
	this._alternateLineMarker = null;
	this._oldMaxBufSize = 0;
	this._oldBufSize = 0;
	this._oldScreenBase = 0;
	this._oldWindowBase = 0;
	this._oldCursorY = 0;
	this._maximumVDURows = 0;
	this._oldCursorX = 0;
	
	this._events = {};
	
	this.clearSelection();
	
	this.registerEvent('bufferChange');
	this.registerEvent('error');
	this.registerEvent('closed');
	this.registerEvent('resize');
	this.registerEvent('selection');
	this.registerEvent('copy');
	this.registerEvent('action');
	this.registerEvent('titleChange');
	this.registerEvent('windowBaseChanged');
	
	this.display = display;
	var initialSize = this.display.init(this);
	
	if(!initialSize) {
		initialSize = {
				width: 80,
				height: 24
		};
	}
	if(width)
		initialSize.width = width;
	if(height)
		initialSize.height = height;
	
	this.setScreenSize(initialSize.width, initialSize.height);
};

VDUBuffer.prototype.clearSelection = function() {
	this.selectBegin = new Cell(0, 0);
	this.selectEnd = new Cell(0, 0);
	this.selectPoint = new Cell(0, 0);
	this.selection = null;
};

VDUBuffer.prototype.registerEvent = function(eventName) {
	var event = new Event(eventName);
	this._events[eventName] = event;
};

VDUBuffer.prototype.dispatchEvent = function(eventName, eventArgs) {
	this._events[eventName].callbacks.forEach(function(callback) {
		  callback(eventArgs);
	});
};

VDUBuffer.prototype.addEventListener = function(eventName, callback){
  this._events[eventName].registerCallback(callback);
};

VDUBuffer.prototype.clearScreen = function() {
	TermUtil.log(LVL_INFO, 'Clear screen');
	this.charArray = TermUtil.mk2darray(this.currentVDURows, this.width, " ");
	this.charAttributes = TermUtil.mk2darray(this.currentVDURows, this.width, 0);
	this.lineMarker = TermUtil.mkarray(this.currentVDURows, false);
	this.lineAttributes = TermUtil.mkarray(this.currentVDURows,0);
	this.setCursorPosition(0, 0);
	this.redisplay();
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.write = function(c, l, s, attributes) {
	var len = s.length;
	if (len > 0) {
		for (var i = 0; i < len; i++) {
			this._writeChar(s.charAt(i), c, l, attributes);
		}
		this.dispatchEvent('bufferChange');
	}
}

VDUBuffer.prototype.writeChar = function(c, l, ch, attributes) {
	var r = this._writeChar(c, l, ch, attributes);
	this.dispatchEvent('bufferChange');
	return r;
}

VDUBuffer.prototype._writeChar = function(c, l, ch, attributes) {
	if(attributes === undefined)
		attributes = NORMAL;
	
	if(typeof ch != "string")
		ch = String.fromCharCode(ch);
	
	c = this._checkBounds(c, 0, this.width - 1);
	l = this._checkBounds(l, 0, this.height - 1);
	if (this.screenBase + l >= this.charArray.length) {
		TermUtil.log(LVL_WARN, 'CHAR: ' + ch + ' would go out of bounds. L = ' + l + ' C = ' + c + ' SB: ' + this.screenBase + ' CA: ' + this.charArray.length);
	} else {
		this.charArray[this.screenBase + l][c] = ch;
		this.charAttributes[this.screenBase + l][c] = attributes;
		if (this.screenBase + l > this.fill)
			this.fill = this.screenBase + l;
		var w = Math.min(1, this.display == null ? 1 : this.display.getCharacterColumns(ch));
		for (var i = 1; i < w && (c + i) < this.width; i++) {
			this.charArray[this.screenBase + l][c + i] = ' ';
		}
		this.prevChar = ch;
		this.prevAttr = attributes;
		this.markLine(l, 1);
		return w;
	}
	return 0;
};

VDUBuffer.prototype.getChar = function(c, l) {
	c = this._checkBounds(c, 0, this.width - 1);
	l = this._checkBounds(l, 0, this.height - 1);
	return this.charArray[this.screenBase + l][c];
};

VDUBuffer.prototype.getLine = function(l) {
	l = this._checkBounds(l, 0, this.height - 1);
	return this.charArray[this.screenBase + l].join("");
};

VDUBuffer.prototype.getAttributes = function(c,l) {
	c = this._checkBounds(c, 0, this.width - 1);
	l = this._checkBounds(l, 0, this.height - 1);
	return this.charAttributes[this.screenBase + l][c];
};


VDUBuffer.prototype.getLineAttributes = function(l) {
	l = this._checkBounds(l, 0, this.height - 1);
	return this.lineAttributes[this.screenBase + l];
};

VDUBuffer.prototype.insertChar = function(c, l, ch, attributes) {
	this._insertChar(c, l, ch, attributes);
	this.dispatchEvent('bufferChange');
}

VDUBuffer.prototype.deleteChar = function(c, l, attributes) {
	this._deleteChar(c, l, attributes);
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.writeChar = function(c, l, s, attributes) {
	for(var i = 0; (i < s.length) && ((c + i) < this.width); i++) {
		this._writeChar(c + i, l, s.charAt(i), attributes);
	}
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.insertLine = function(l, n, scrollDown) {
	if(arguments.length == 1) {
		n = 1;
		scrollDown = SCROLL_UP;
	}
	else if(typeof n == 'boolean') {
		scrollDown = n;
		n = 1;
	}
	
	TermUtil.log(LVL_INFO, 'Inserting ' + n + ' lines at ' + l + ' scrolling '+ (scrollDown ? 'down' : 'up'));
	
	l = this._checkBounds(l, 0, this.height - 1);
	var cbuf = null;
	var abuf = null;
	var lbuf = null;
	var labuf = null;
	var offset = 0;
	var oldBase = this.screenBase;
	if (l > this.bottomMargin) {
		/*
		 * We do not scroll below bottom margin (below the scrolling
		 * region).
		 */
		return;
	}
	var top = ((l < this.topMargin) ? 0
			: ((l > this.bottomMargin) ? (((this.bottomMargin + 1) < height) ? (this.bottomMargin + 1)
					: (this.height - 1))
					: this.topMargin));
	var bottom = ((l > this.bottomMargin) ? (this.height - 1)
			: ((l < this.topMargin) ? ((this.topMargin > 0) ? (this.topMargin - 1) : 0)
					: this.bottomMargin));
	var scrollFromLine = oldBase + l;
	var currAttr = this.charAttributes[scrollFromLine][0];
	var linesToScroll = n;
	if (scrollDown) {
		if (linesToScroll > (bottom - top)) {
			linesToScroll = (bottom - top);
		}
		var scrollToLine = bottom - l - (linesToScroll - 1);
		
		cbuf = TermUtil.mk2darray(scrollToLine, this.width, " ");
		abuf = TermUtil.mk2darray(scrollToLine, this.width, 0);
		lbuf = TermUtil.mkarray(scrollToLine, false);
		labuf = TermUtil.mkarray(scrollToLine, 0);

		// Copy the lines we will move down into the temporary buffer
		TermUtil.arraycopy(this.charArray, scrollFromLine, cbuf, 0, scrollToLine);
		TermUtil.arraycopy(this.charAttributes, scrollFromLine, abuf, 0, scrollToLine);
		TermUtil.arraycopy(this.lineMarker, scrollFromLine, lbuf, 0, scrollToLine);
		TermUtil.arraycopy(this.lineAttributes, scrollFromLine, labuf, 0, scrollToLine);

		this.scrollSelection(-1);

		// Copy the temporary buffer into its new position
		TermUtil.arraycopy(cbuf, 0, this.charArray, scrollFromLine + linesToScroll, scrollToLine);
		TermUtil.arraycopy(abuf, 0, this.charAttributes, scrollFromLine + linesToScroll, scrollToLine);
		TermUtil.arraycopy(lbuf, 0, this.lineMarker, scrollFromLine+ linesToScroll, scrollToLine);
		TermUtil.arraycopy(labuf, 0, this.lineAttributes, scrollFromLine	+ linesToScroll, scrollToLine);
		
		cbuf = this.charArray;
		abuf = this.charAttributes;
		lbuf = this.lineMarker;
		labuf = this.lineAttributes;
	} else {
		var actualBufferRowCount = this._getActualBufferSize();
		if (linesToScroll > ((bottom - top) + 1)) {
			linesToScroll = (bottom - top) + 1;
		}
		
		this.scrollSelection(1);

		if (this.currentVDURows < actualBufferRowCount) {
			if ((this.currentVDURows + linesToScroll) > actualBufferRowCount) {
				offset = linesToScroll - (actualBufferRowCount - this.currentVDURows);
				this.scrollMarker += offset;
				this.currentVDURows = actualBufferRowCount;
				this.screenBase = actualBufferRowCount - this.height - 1;
				this.windowBase = this.screenBase;
				TermUtil.log(LVL_INFO, 'Scrolled, window base now ' + this.windowBase);
			} else {
				this.scrollMarker += linesToScroll;
				this.screenBase += linesToScroll;
				this.windowBase += linesToScroll;
				this.currentVDURows += linesToScroll;
			}
			
			cbuf = TermUtil.mk2darray(this.currentVDURows, this.width, " ");
			abuf = TermUtil.mk2darray(this.currentVDURows, this.width, 0);
			lbuf = TermUtil.mkarray(this.currentVDURows, false);
			labuf = TermUtil.mkarray(this.currentVDURows, 0);
			
		} else {
			offset = linesToScroll;
			cbuf = this.charArray;
			abuf = this.charAttributes;
			lbuf = this.lineMarker;
			labuf = this.lineAttributes;
		}
		
		/* copy anything from the top of the buffer (+offset) to the
		 * new top up to the screenBase.
		 */
		if (oldBase > 0) {
			TermUtil.arraycopy(this.charArray, offset, cbuf, 0, oldBase - offset);
			TermUtil.arraycopy(this.charAttributes, offset, abuf, 0, oldBase - offset);
			TermUtil.arraycopy(this.lineMarker, offset, lbuf, 0, oldBase	- offset);
			TermUtil.arraycopy(this.lineAttributes, offset, labuf, 0, oldBase - offset);
		}
		/* copy anything from the top of the screen (screenBase) up
		 to the topMargin to the new screen */
		if (this.top > 0) {
			TermUtil.arraycopy(this.charArray, oldBase, cbuf, this.screenBase, top);
			TermUtil.arraycopy(this.charAttributes, oldBase, abuf, this.screenBase, top);
			TermUtil.arraycopy(this.lineMarker, oldBase, lbuf, this.screenBase, top);
			TermUtil.arraycopy(this.lineAttributes, oldBase, labuf, this.screenBase, top);
		}
		
		/* copy anything from the topMargin up to the amount of lines
		 inserted to the gap left over between scrollback buffer and screenBase */
		if (oldBase > 0) {
			TermUtil.arraycopy(this.charArray, oldBase + top, cbuf, oldBase - offset, linesToScroll);
			TermUtil.arraycopy(this.charAttributes, oldBase + top, abuf, oldBase - offset, linesToScroll);
			TermUtil.arraycopy(this.lineMarker, oldBase + top, lbuf, oldBase - offset, linesToScroll);
			TermUtil.arraycopy(this.lineAttributes, oldBase + top, labuf, oldBase - offset, linesToScroll);
		}
		
		/* copy anything from topMargin + n up to the line linserted to the topMargin */
		TermUtil.arraycopy(this.charArray, oldBase + top + linesToScroll, cbuf, this.screenBase + top, l - top - (linesToScroll - 1));
		TermUtil.arraycopy(this.charAttributes, oldBase + top + linesToScroll, abuf, this.screenBase + top, l - top - (linesToScroll - 1));
		TermUtil.arraycopy(this.lineMarker, oldBase + top + linesToScroll, lbuf, this.screenBase + top, l - top - (linesToScroll - 1));
		TermUtil.arraycopy(this.lineAttributes, oldBase + top + linesToScroll, labuf, this.screenBase + top, l - top - (linesToScroll - 1));
		
		/*  copy the all lines next to the inserted to the new buffer*/
		if (l < (this.height - 1)) {
			TermUtil.arraycopy(this.charArray, scrollFromLine + 1, cbuf, this.screenBase + l + 1, (this.height - 1) - l);
			TermUtil.arraycopy(this.charAttributes, scrollFromLine + 1, abuf, this.screenBase + l + 1, (this.height - 1) - l);
			TermUtil.arraycopy(this.lineMarker, scrollFromLine + 1, lbuf, this.screenBase + l + 1, (this.height - 1) - l);
			TermUtil.arraycopy(this.lineAttributes, scrollFromLine + 1, labuf, this.screenBase + l + 1, (this.height - 1) - l);
		}
	}
	// this is a little helper to mark the scrolling
	this.scrollMarker -= linesToScroll;
	for (var i = 0; i < linesToScroll; i++) {
		var row = (this.screenBase + l) + (scrollDown ? i : (-i));
		cbuf[row] = TermUtil.mkarray(this.width, " ");
		abuf[row] = TermUtil.mkarray(this.width, 0);
		if (currAttr != 0) {
			for (var j = 0; j < this.width; j++) {
				abuf[row][j] = this.currAttr;
			}
		}
	}
	TermUtil.log(LVL_INFO, 'After scrolling, a buffer of ' + this.currentVDURows + ' gives an array ' + cbuf.length);
	this.charArray = cbuf;
	this.charAttributes = abuf;
	this.lineMarker = lbuf;
	this.lineAttributes = labuf;
	if (scrollDown) {
		this.markLine(l, bottom - l + 1);
	} else {
		this.markLine(top, l - top + 1);
	}
	this.dispatchEvent('windowBaseChanged',this);
};

VDUBuffer.prototype.isSelected = function() {
	return this.selectBegin != null && ( this.selectBegin.x != this.selectEnd.x || this.selectBegin.y != this.selectEnd.y );
};

VDUBuffer.prototype.scrollSelection = function(amount) {
	TermUtil.log(LVL_INFO, 'Scroll selection by ' + amount);
	if(this.isSelected() && this.currentVDURows == this.maximumVDURows) {
		if(this.selectEnd.y == 0)
			this.clearSelection();
		else {
			this.selectBegin.y = Math.min(this.currentVDURows, Math.max(this.selectBegin.y - amount, 0));				
			this.selectEnd.y = Math.min(this.currentVDURows, Math.max(this.selectEnd.y - amount, 0));
			if(this.selectPoint != null) {
				this.selectPoint.y = Math.min(this.currentVDURows, Math.max(this.selectPoint.y - amount, 0));
			}
		}
	}
};

VDUBuffer.prototype.deleteLine = function(l, attributes) {
	l = this._checkBounds(l, 0, this.height - 1);

	var bottom = ((l > this.bottomMargin) ? (this.height - 1)
			: ((l < this.topMargin) ? this.topMargin : (this.bottomMargin + 1)));
	
	var topLine = this.screenBase + l + 1;
	var bottomLine = (this.screenBase + bottom) - 1;

	TermUtil.arraycopy(this.charArray, topLine, this.charArray, this.screenBase + l, bottom - l - 1);
	TermUtil.arraycopy(this.charAttributes, topLine, this.charAttributes, this.screenBase + l, bottom - l - 1);
	TermUtil.arraycopy(this.lineMarker, topLine, this.lineMarker, this.screenBase + l, bottom - l - 1);
	TermUtil.arraycopy(this.lineAttributes, topLine, this.lineAttributes, this.screenBase + l, bottom - l - 1);
	
	this.charArray[bottomLine] = TermUtil.mkarray(this.width, " ");
	this.charAttributes[bottomLine] = TermUtil.mkarray(this.width, 0);
	
	if (attributes != 0) {
		for (var i = 0; i < this.width; i++) {
			this.charAttributes[bottomLine][i] = attributes;
		}
	}
	this.lineMarker[bottomLine] = false;
	this.lineAttributes[bottomLine] = 0;
	this.markLine(l, bottom - l);
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.deleteArea = function(c, l, w, h, curAttr, affectAttributes, selective, affectLineAttributes) {

	TermUtil.log(LVL_INFO, 'Deleting area starting ' + c + ',' + l + ' for ' + w	+ ' x ' + h + ' attr = ' + curAttr + ' affect attr = '
			+ affectAttributes + ' selective = ' + selective + ' affectLineAttributes = ' + affectLineAttributes);

	c = this._checkBounds(c, 0, this.width - 1);
	l = this._checkBounds(l, 0, this.height - 1);
	
	var cbuf = TermUtil.mkarray(w, ' ');
	var abuf = TermUtil.mkarray(w, curAttr);
	var j;
	for (var i = 0; (i < h) && ((l + i) < this.height); i++) {
		j = this.screenBase + l + i;
		if (selective) {
			for (var k = 0; k < w; k++) {
				if ((this.charAttributes[j][k + c] & PROTECTED) == 0) {
					this.charArray[j][k + c] = 0;
				}
			}
		} else {
			TermUtil.arraycopy(cbuf, 0, this.charArray[j], c, w);
		}

		if (affectAttributes) {
			TermUtil.arraycopy(abuf, 0, this.charAttributes[j], c, w);
		}
		this.lineMarker[j] = false;
		if (affectLineAttributes && w == this.width) {
			this.lineAttributes[j] = 0;
		}
	}
	this.markLine(l, h);
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.setCursorPosition = function(c, l) {
	TermUtil.log(LVL_DEBUG, 'VDU: cursor position [' + c + ',' + l + ']');
	if(c != this.cursorX || l != this.cursorY) {
		var cy = this.cursorY;
		this.cursorX = this._checkBounds(c, 0, this.width - 1);
		this.cursorY = this._checkBounds(l, 0, this.height - 1);
		TermUtil.log(LVL_DEBUG, 'VDU: cursor now  [' + c + ',' + l + ']');
		this.markLine(this.cursorY, 1);
		if(cy != this.cursorY)
			this.markLine(cy, 1);
		this.dispatchEvent('bufferChange');
	}
};

VDUBuffer.prototype.setWindowBase = function(line) {
	if(isNaN(line))
		throw 'Window base must be a number.';
	var updateWindowBase = false;
	TermUtil.log(LVL_INFO, 'VDU: setting window base to ' + line + ' for screenbase of ' + this.screenBase + ' in ' + this.currentVDURows);
	if (line > this.screenBase) {
		line = this.screenBase;
	} else if (line < 0) {
		line = 0;
	}
	if (this.windowBase != line) {
		this.cursorY += this.windowBase - line;
		this.windowBase = line;
		this.updateWindowBase = true;
	}
	if (this.updateWindowBase) {
		this.update[0] = true;
		this.dispatchEvent('windowBaseChanged',this);
	}
	else
		this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.getWindowBase = function() {
	return windowBase;
};

VDUBuffer.prototype.setTopMargin = function(l) {
	if (l > this.bottomMargin) {
		this.topMargin = this.bottomMargin;
		this.bottomMargin = l;
	} else {
		this.topMargin = l;
	}
	if (this.topMargin < 0) {
		this.topMargin = 0;
	}
	if (this.bottomMargin > (this.height - 1)) {
		this.bottomMargin = this.height - 1;
	}
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.setBottomMargin = function(l) {
	if (l < this.topMargin) {
		this.bottomMargin = this.topMargin;
		this.topMargin = l;
	} else {
		this.bottomMargin = l;
	}
	if (this.topMargin < 0) {
		this.topMargin = 0;
	}
	if (this.bottomMargin > (this.height - 1)) {
		this.bottomMargin = this.height - 1;
	}
	this.dispatchEvent('bufferChange');
};

VDUBuffer.prototype.setAlternateBuffer = function(alternateBuffer) {
	if (this.alternateBuffer == alternateBuffer) {
		return;
	}
	this.alternateBuffer = alternateBuffer;

	var tempCharArray = this.charArray;
	var tempCharAttributes = this.charAttributes;
	var tempLineAttributes = this.lineAttributes;
	var tempLineMarker = this.lineMarker;

	if (this._alternateCharArray == null) {
		this._alternateCharArray = TermUtil.mk2darray(this.height, this.width, " ");
		this._alternateCharAttributes = TermUtil.mk2darray(this.height, this.width, 0);
		this._alternateLineAttributes = TermUtil.mkarray(this.height, " ");
		this._alternateLineMarker = TermUtil.mkarray(this.height, false);
	}

	this.charArray = this._alternateCharArray;
	this.charAttributes = this._alternateCharAttributes;
	this.lineAttributes = this._alternateLineAttributes;
	this.lineMarker = this._alternateLineMarker;

	this._alternateCharArray = tempCharArray;
	this._alternateCharAttributes = tempCharAttributes;
	this._alternateLineAttributes = tempLineAttributes;
	this._alternateLineMarker = tempLineMarker;
	
	var actualBufferRowCount = this._getActualBufferSize();

	if (alternateBuffer) {
		TermUtil.log(LVL_INFO, 'Switching to alternate buffer');

		// Save for the restore
		this.oldMaxBufSize = this.maximumVDURows;
		this.oldBufSize = this.currentVDURows;
		this.oldScreenBase = this.screenBase;
		this.oldWindowBase = this.windowBase;
		this.oldCursorY = this.cursorY;
		this.oldCursorX = this.cursorX;

		// New values
		this.maximumVDURows = this.height;
		this.currentVDURows = this.height;
		this.screenBase = 0;
		this.windowBase = 0;
		this.cursorX = 0;
		this.cursorY = 0;
	} else {
		TermUtil.log(LVL_INFO, 'Switching to main buffer (maxVDU: ' + this.oldMaxBufSize + ' sb: ' + this.oldScreenBas + ' wb: ' + this.oldWindowBase + ')');

		this.maximumVDURows = this.oldMaxBufSize;
		this.screenBase = this.oldScreenBase;
		this.windowBase = this.oldWindowBase;
		this.currentVDURows = this.oldBufSize;
		this.cursorY = this.oldCursorY;
		this.cursorX = this.oldCursorX;

		this._alternateCharArray = null;
		this._alternateCharAttributes = null;
		this._alternateLineAttributes = null;
		this._alternateLineMarker = null;
	}
	this.update[0] = true;
	this.dispatchEvent('windowBaseChanged',this);
};

VDUBuffer.prototype.setBufferSize = function(size) {
	var amount = size;
	/*
	 * The alternate buffer is always the same size as the height and
	 * always at least the same size when using main buffer
	 */
	if (this._alternateBuffer || amount < this.height) {
		amount = this.height;
	}

	if (amount < this.maximumVDURows) {
		var cbuf = TermUtil.mk2darray(amount, width, " ");
		var abuf = TermUtil.mk2darray(amount, width, 0);
		var lbuf = TermUtil.mkarray(width, false);
		var labuf = TermUtil.mkarray(width, 0);
		
		var copyStart = ((this.currentVDURows - amount) < 0) ? 0 : (this.currentVDURows - amount);
		var copyCount = ((this.currentVDURows - amount) < 0) ? this.currentVDURows : amount;
		if (this.charArray != null) {
			TermUtil.arraycopy(this.charArray, copyStart, cbuf, 0, copyCount);
		} else {
			var sp = TermUtil.mkarray(width, " ");
			for (var i = 0; i < cbuf.length; i++) {
				cbuf[i] = sp;
			}
		}
		
		if (this.charAttributes != null) {
			TermUtil.arraycopy(this.charAttributes, copyStart, abuf, 0, copyCount);
		}
		if (this.lineMarker != null) {
			TermUtil.arraycopy(this.lineMarker, copyStart, lbuf, 0, copyCount);
		}
		if (this.lineAttributes != null) {
			TermUtil.arraycopy(this.lineAttributes, copyStart, labuf, 0, copyCount);
		}
		this.lineMarker = lbuf;
		this.lineAttributes = labuf;
		this.charArray = cbuf;
		this.charAttributes = abuf;
		this.currentVDURows = copyCount;
		this.screenBase = this.currentVDURows - this.height;
		this.windowBase = this.screenBase;
	}
	this.maximumVDURows = size;
	this.update[0] = true;
	this.dispatchEvent('windowBaseChanged',this);
};

VDUBuffer.prototype.setScreenSize = function(newWidth, newHeight, remote) {
	
	var size = 0;
	var cbuf;
	var abuf;
	var lbuf;
	var labuf;
	var previewsVDURows = this.currentVDURows;
	var oldWindowBase = this.windowBase;
	var shrinking = false;
	
	if ((newWidth < 1) || (newHeight < 1)
			|| (newWidth == this.width && newHeight == this.height)) {
		TermUtil.log(LVL_WARN, 'VDU: no change in screen size [' + newWidth + ',' + newHeight + '], ignoring');
		return;
	}
	TermUtil.log(LVL_INFO, 'VDU: screen size [' + newWidth + ',' + newHeight + ',' + remote + ']');
	
	var actualBufferRows = this._getActualBufferSize();
	
	if (newHeight > actualBufferRows) {
		actualBufferRows = newHeight;
	}
	
	// If the buffer has shrunk, delete from the top
	var offset = 0;
	if(this.currentVDURows > actualBufferRows) {
		offset = this.currentVDURows - actualBufferRows;
		this.currentVDURows = actualBufferRows;
	}
	
	// Determine if the screen is shrinking
	if (newHeight > this.currentVDURows) {
		this.currentVDURows = newHeight;
	} else {
		if(newHeight < this.height) {
			TermUtil.log(LVL_INFO, 'Shrinking from ' + this.currentVDURows + ' to ' + newHeight);
			shrinking = true;
		}
	}
	
	size = this.fill < this.currentVDURows ? this.fill : this.currentVDURows;
	this.windowBase = (size - newHeight > 0 ? size - newHeight + 1 : 0);
	this.screenBase = this.windowBase;
	if (this.cursorY >= this.newHeight) {
		TermUtil.log(LVL_DEBUG, 'Moving cursor back from ' + this.cursorY + ' because it is beyond the height ' + newHeight);
		this.cursorY = newHeight - 1;
	}
	else if (newHeight != this.height && oldWindowBase != this.windowBase) { 
		if (this.windowBase == 0 && size <= newHeight) {
			TermUtil.log(LVL_DEBUG, 'Moving cursor to ' + size);
			this.cursorY = size;
		}
		else {
			TermUtil.log(LVL_DEBUG, 'Moving cursor by ' + (newHeight - this.height));
			this.cursorY = this.cursorY + newHeight - this.height;
		}
	}
	TermUtil.log(LVL_INFO, 'VDU: about to screen size set (cursorY=' + this.cursorY
			+ ' screenBase=' + this.screenBase + ' rows=' + this.currentVDURows
			+ ' height=' + this.height
			+ ' windowBase=' + this.windowBase + ' size=' + size);

	/*
	 * If the screen is shrinking trim any empty lines from the bottom
	 * of the buffer
	 */
	if (shrinking) {
		var spArr = TermUtil.mkarray(this.charArray[0].length, " ");
		for (var i = this.currentVDURows - 1; i >= Math.max(1, newHeight); i--) {
			if (!this.isAllSpaces(this.charArray[i])) {
				break;
			}
			this.currentVDURows--;
		}
	}

	if (this.screenBase > this.currentVDURows - newHeight) {
		this.screenBase = this.currentVDURows - newHeight;
	}

	cbuf = TermUtil.mk2darray(this.currentVDURows, newWidth, " ");
	cbuf[0][0]='X';
	abuf = TermUtil.mk2darray(this.currentVDURows, newWidth, 0);
	lbuf = TermUtil.mkarray(this.currentVDURows, false);
	labuf = TermUtil.mkarray(this.currentVDURows, 0);
	if ((this.charArray != null) && (this.charAttributes != null)) {
		if(offset > 0) {
			this.scrollSelection(offset);
			TermUtil.log(LVL_INFO, 'Copying offset ' + offset + ' buffer p: ' + this.charArray.length);
			TermUtil.arraycopy(this.charArray, offset, this.charArray, 0, this.charArray.length - offset);
			TermUtil.arraycopy(this.charAttributes, offset, this.charAttributes, 0, this.charAttributes.length - offset);
		}

		TermUtil.log(LVL_INFO, 'Copying buffer p: ' + previewsVDURows + ' c: ' + this.currentVDURows);
		for (var i = 0; (i < previewsVDURows) && (i < this.currentVDURows); i++) {
			TermUtil.arraycopy(this.charArray[i], 0, cbuf[i], 0, (newWidth < this.width) ? newWidth : this.width);
			TermUtil.arraycopy(this.charAttributes[i], 0, abuf[i], 0, (newWidth < this.width) ? newWidth : this.width);
			lbuf[i] = this.lineMarker[i];
			labuf[i] = this.lineAttributes[i];
		}
	} else {
		for (var i = 0; i < cbuf.length; i++) {
			cbuf[i] = TermUtil.mkarray(newWidth, " ");
		}
	}

	this.charArray = cbuf;
	this.charAttributes = abuf;
	this.lineMarker = lbuf;
	this.lineAttributes = labuf;
	this.width = newWidth;
	this.height = newHeight;
	this.topMargin = 0;
	this.bottomMargin = newHeight - 1;

	// Create the alternate buffer if it does

	this.update = TermUtil.mkarray(newHeight + 1, false);
	this.update[0] = true;

	if (this.cursorY + this.screenBase >= this.currentVDURows) {
		this.cursorY = this.currentVDURows - this.screenBase - 1;
	}
	if (this.cursorY < 0) {
		this.cursorY = 0;
	}
	
	TermUtil.log(LVL_DEBUG, 'VDU: new screen size is [' + this.width + ',' + this.height + '], remote=' + remote);

	this.dispatchEvent('resize', this.width, this.height, remote);

	TermUtil.log(LVL_INFO, 'VDU: screen size set (cursorY=' + this.cursorY
			+ ' screenBase=' + this.screenBase + ' rows=' + this.currentVDURows
			+ ' la= ' + this.lineAttributes.length + ' height=' + this.height
			+ ' windowBase=' + this.windowBase + ' size=' + size);
	this.dispatchEvent('windowBaseChanged',this);
};

VDUBuffer.prototype.markLine = function(l, n) {
	l = this._checkBounds(l, 0, this.height - 1);
	for (var i = 0; (i < n) && ((l + i) < this.height); i++) {
		this.update[l + i + 1] = true;
	}
};

VDUBuffer.prototype.setWindowTitle = function(windowTitle) {
	this.windowTitle = windowTitle;
	this.dispatchEvent('titleChange', windowTitle);
};

VDUBuffer.prototype.isAllSpaces = function(arr) {
	for (var i = 0; i < arr.length; i++) {
		if (arr[i] != ' ' && arr[i] != '\0') {
			return false;
		}
	}
	return true;
};

//
// Private
//


VDUBuffer.prototype._deleteChar = function(c, l, attributes) {
	c = this._checkBounds(c, 0, this.width - 1);
	l = this._checkBounds(l, 0, this.height - 1);
	if (c < (this.width - 1)) {
		this.charArray[this.screenBase + l].splice(c, 1);
		this.charArray[this.screenBase + l].push(' ');
		this.charAttributes[this.screenBase + l].splice(c, 1);
		this.charAttributes[this.screenBase + l].push(' ');
	}
	return this._writeChar(this.width - 1, l, 0, attributes);
};

VDUBuffer.prototype._insertChar = function(c, l, ch, attributes) {
	c = this._checkBounds(c, 0, this.width - 1);
	l = this._checkBounds(l, 0, this.height - 1);
	this.charArray[this.screenBase + l].splice(c, 0, " ");
	this.charArray[this.screenBase + l].splice(this.width, 1);
	this.charAttributes[this.screenBase + l].splice(c, 0, NORMAL);
	this.charAttributes[this.screenBase + l].splice(this.width, 1);
	return this._writeChar(c, l, ch, attributes);
};

VDUBuffer.prototype._checkBounds = function(value, lower, upper) {
	if (value < lower) {
		return lower;
	}
	if (value > upper) {
		return upper;
	}
	return value;
};

VDUBuffer.prototype._getActualBufferSize = function() {
	return Math.max(1, this.maximumVDURows < this.height ? this.height : this.maximumVDURows);
};

VDUBuffer.prototype.redisplay = function() {
	this.update[0] = true;
	this.dispatchEvent('bufferChange');
};

/*
 * Extends VDUBuffer adding VTxxx emulation features.
 */
var ClientTerminalEmulation = function(display, termType, io, width, height) {
	
	VDUBuffer.call(this, display, width, height);

	this.io = io;
	this.localecho = false;
	this.maskInput = false;
	this.maskCharacter = '*';
	this.inputEOL = ClientTerminalEmulation.EOL_CR;
	this.outputEOL = ClientTerminalEmulation.EOL_DEFAULT;
	this.visibleBell = false;
	this.audibleBell = true;
	this.useibmcharset = false;
	this.keypadmode = false;
	this.charsetName = 'UTF-8';
	this.lightBackground = false;
	this.copyMode = ClientTerminalEmulation.COPY_ON_CTRL_SHIFT_C;
	this.allow80to132 = true;

	this.termType = 'xterm';
	this._answerBack = 'Use ClientTerminalEmulation.setAnswerback() to set ...\n';
	this._attributes = 0;
	this._Sc = 0;
	this._Sr = 0;
	this._Sa = 0;
	this._Sgr = 0;
	this._Sgl = 0;
	this._Sgx = 0;
	this._insertmode = 0;
	this._vt52mode = false;

	this._output8bit = false;
	this._moveoutsidemargins = true;
	this._Smoveoutsidemargins = false;
	this._wraparound = true;
	this._sendcrlf = false;
	this._capslock = false;
	this._numlock = false;
	this._mouserpt = 0;
	this._mousebut = 0;
	this._lastwaslf = 0;
	this._usedcharsets = false;
	this._bracketedPaste = false;
	this._reverseWraparound = true;
	this._alternateScrollMode = true;

	this._gx = [ClientTerminalEmulation.DEFAULT_G0, ClientTerminalEmulation.DEFAULT_G1, ClientTerminalEmulation.DEFAULT_G2, ClientTerminalEmulation.DEFAULT_G3];
	this._gl = 0;
	this._gr = 0;
	this._onegl = -1;

	this._osc;
	this._dcs;
	/*
	 * to memorize OSC & DCS control sequence
	 */
	this._readLineBuffer = '';
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	this._vms = false;
	this._Tabs = [];
	this._DCEvars = TermUtil.mkarray(30, 0);
	this._DCEvar = 0;
	this._oldSc = 0;
	this._oldSr = 0;
	this._applicationCursorKeys = false;
	
	// Keys
	this.Numpad = null;
	this.FunctionKey = null;
	this.FunctionKeyShift = null;
	this.FunctionKeyCtrl = null;
	this.FunctionKeyAlt = null;
	this.TabKey = null;
	this.KeyUp = null;
	this.KeyDown = null;
	this.KeyLeft = null;
	this.KeyRight = null;
	this.NUMLock = null;
	this.NUMDivide = null;
	this.NUMMultiply = null;
	this.NUMMinus = null;
	this.NUMEnter = null;
	this.PF1 = null;
	this.PF2 = null;
	this.PF3 = null;
	this.PF4 = null;
	this.KeyHome = null;
	this.KeyEnd = null;
	this.Insert = null;
	this.Remove = null;
	this.PrevScn = null;
	this.NextScn = null;
	this.Escape = null;
	this.BackSpace = null;
	this.NUMDot = null;
	this.NUMPlus = null;
	
	//this.setBufferSize(100);
	this.setTerminalType(termType);
	this.reset();
	
	this.io.init(this);
	
};

ClientTerminalEmulation.prototype.output = function(s) {
	if(this._bracketedPaste)
		s = String.fromCharCode(27) + '[200~' + s + String.fromCharCode(27) + '[201~';
	this.io.write(s);
}

ClientTerminalEmulation.prototype = Object.create(VDUBuffer.prototype);
ClientTerminalEmulation.prototype.constructor = ClientTerminalEmulation;

ClientTerminalEmulation.prototype.setTerminalType = function(termType) {
	this.termType = termType;
	this._setDefaultKeyCodes();
	if(termType in DEFAULT_KEYMAPS) {
		this.setKeyCodes(DEFAULT_KEYMAPS[termType]);
	}
};

ClientTerminalEmulation.prototype.unescape = function(tmp) {
	var idx = 0;
	var oldidx = 0;
	var cmd = '';
	while (((idx = tmp.indexOf('\\', oldidx)) >= 0)
			&& (++idx <= tmp.length)) {
		cmd += tmp.substring(oldidx, idx - 1);
		if (idx == tmp.length) {
			return cmd;
		}
		switch (tmp.charAt(idx)) {
		case 'b':
			cmd += '\b';
			break;
		case 'e':
			cmd += String.fromCharCode(27);
			break;
		case 'n':
			cmd += '\n';
			break;
		case 'r':
			cmd += '\r';
			break;
		case 't':
			cmd += '\t';
			break;
		case 'v':
			cmd += '\u000b';
			break;
		case 'a':
			cmd += '\u0012';
			break;
		default:
			if ((tmp.charAt(idx) >= '0') && (tmp.charAt(idx) <= '9')) {
				var i;
				for (i = idx; i < tmp.length; i++) {
					if ((tmp.charAt(i) < '0') || (tmp.charAt(i) > '9')) {
						break;
					}
				}
				cmd += String.fromCharCode(parseInt(tmp.substring(idx, i), 10));
				idx = i - 1;
			} else {
				cmd += tmp.substring(idx, ++idx);
			}
			break;
		}
		oldidx = ++idx;
	}
	if (oldidx <= tmp.length) {
		cmd += tmp.substring(oldidx);
	}
	return cmd;
};

ClientTerminalEmulation.prototype.setSelection = function(text) {
	if(this.selection != text) {
		this.selection = text;
		this.dispatchEvent('selection', text);
		if((this.copyMode & ClientTerminalEmulation.COPY_ON_SELECT) != 0) {
			this.copySelectionToClipboard();
		}
	}
};

ClientTerminalEmulation.prototype.copySelectionToClipboard = function() {
	this.dispatchEvent('copy', TermUtil.toclipboard(this.selection));
};

ClientTerminalEmulation.prototype.setKeyCodes = function(codes) {
	var res;
	var prefixes = [ '', 'S', 'C', 'A' ];
	var i;
	for (var j = 0; j < 4; j++) {
		for (i = 0; i < 10; i++) {
			res = codes[prefixes[j] + 'NUMPAD' + i];
			if (res != undefined) {
				this.Numpad[i][j] = this.unescape(res);
			}
		}
	}
	for (i = 1; i < 13; i++) {
		res = codes['F' + i];
		if (res != undefined) {
			this.FunctionKey[i] = this.unescape(res);
		}
		res = codes['SF' + i];
		if (res != undefined) {
			this.FunctionKeyShift[i] = this.unescape(res);
		}
		res = codes['CF' + i];
		if (res != undefined) {
			this.FunctionKeyCtrl[i] = this.unescape(res);
		}
		res = codes['AF' + i];
		if (res != undefined) {
			this.FunctionKeyAlt[i] = this.unescape(res);
		}
	}
	for (i = 0; i < 4; i++) {
		res = codes[prefixes[i] + 'PGUP'];
		if (res != null) {
			this.PrevScn[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'PGDOWN'];
		if (res != null) {
			this.NextScn[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'END'];
		if (res != null) {
			this.KeyEnd[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'HOME'];
		if (res != null) {
			this.KeyHome[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'INSERT'];
		if (res != null) {
			this.Insert[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'REMOVE'];
		if (res != null) {
			this.Remove[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'UP'];
		if (res != null) {
			this.KeyUp[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'DOWN'];
		if (res != null) {
			this.KeyDown[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'LEFT'];
		if (res != null) {
			this.KeyLeft[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'RIGHT'];
		if (res != null) {
			this.KeyRight[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'ESCAPE'];
		if (res != null) {
			this.Escape[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'BACKSPACE'];
		if (res != null) {
			this.BackSpace[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'TAB'];
		if (res != null) {
			this.TabKey[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'NUMPLUS'];
		if (res != null) {
			NUMPlus[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'NUMDECIMAL'];
		if (res != null) {
			this.NUMDot[i] = this.unescape(res);
		}

		res = codes[prefixes[i] + 'NUMMULTIPLY'];
		if (res != null) {
			this.NUMMultiply[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'NUMENTER'];
		if (res != null) {
			this.NUMEnter[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'NUMMINUS'];
		if (res != null) {
			this.NUMMinus[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'NUMLOCK'];
		if (res != null) {
			this.NUMLock[i] = this.unescape(res);
		}
		res = codes[prefixes[i] + 'NUMDIVIDE'];
		if (res != null) {
			this.NUMDivide[i] = this.unescape(res);
		}
	}
};

ClientTerminalEmulation.prototype.reset = function() {
	this.hardReset();
};

ClientTerminalEmulation.prototype.hardReset = function() {
	this.softReset();
	/*
	 * reset character sets
	 */
	this._gx[0] = ClientTerminalEmulation.DEFAULT_G0;
	this._gx[1] = ClientTerminalEmulation.DEFAULT_G1;
	this._gx[2] = ClientTerminalEmulation.DEFAULT_G2;
	this._gx[3] = ClientTerminalEmulation.DEFAULT_G3;

	this._gl = ClientTerminalEmulation.DEFAULT_GL;
	this._gr = ClientTerminalEmulation.DEFAULT_GR;
	/*
	 * reset tabs
	 */
	var nw = this.width;
	if (nw < 132) {
		nw = 132;
	}
	this.Tabs = TermUtil.mkarray(nw, 0);
	for (var i = 0; i < nw; i += 8) {
		this.Tabs[i] = 1;
	}
	this.clearScreen();
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
}

/**
 * Perform a soft reset
 */
ClientTerminalEmulation.prototype.softReset = function() {
	TermUtil.log(LVL_INFO, 'Soft reset');
	
	this.showcursor = true;
	this.screenBase = 0;
	this.keypadmode = false;
	
	this._insertmode = 0;
	this._moveoutsidemargins = true;
	this._wraparound = true;
	this._reverseWraparound = true;
	this._attributes = 0;
	this._output8bit = false;
	this._usedcharsets = false;
	this._bracketedPaste = false;
	
	this.setCursorPosition(0, 0);
	this.setTopMargin(0);
	this.setBottomMargin(9999999);

	// Save cursor state
	this._Smoveoutsidemargins = false;
	this._Sc = 0;
	this._Sr = 0;
	this._Sgl = ClientTerminalEmulation.DEFAULT_GL;
	this._Sgr = ClientTerminalEmulation.DEFAULT_GR;
	this._Sa = this._attributes;
	this._Sgx = new Array(4);
	for (var i = 0; i < 4; i++) {
		this._Sgx[i] = this._gx[i];
	}
	this._Smoveoutsidemargins = this._moveoutsidemargins;

};

ClientTerminalEmulation.prototype.putCharacter= function(c, doshowcursor) {
	this._putCharacter(c, doshowcursor);
	this.dispatchEvent('bufferChange');
};

ClientTerminalEmulation.prototype.putString = function(s) {
	var len = s.length;
	if (len > 0) {
		for (var i = 0; i < len; i++) {
			this._putCharacter(s.charAt(i), false);
		}
		this.dispatchEvent('bufferChange');
	}
};

ClientTerminalEmulation.prototype.keyPressed = function(keyCode, keyChar, modifiers) {
	TermUtil.log(LVL_INFO, 'keyPressed(' + keyCode + ',' + keyChar + ',' + modifiers);
	if (this.display != null) {
		this.display.interruptBlink();
	}

	// Scroll to the end
	this.setWindowBase(this.currentVDURows);

	// Determine which modifiers are in use
	var control = (modifiers & VDUInput.KEY_CONTROL) != 0;
	var shift = (modifiers & VDUInput.KEY_SHIFT) != 0;
	var alt = (modifiers & VDUInput.KEY_ALT) != 0;

	if(keyChar.charCodeAt(0) > 0 && keyChar.charCodeAt(0) <= 31 && keyChar.charCodeAt(0) != 10 && keyChar.charCodeAt(0) != 13 && keyChar.charCodeAt(0) != 8 && keyChar.charCodeAt(0) != 9 && keyChar.charCodeAt(0) != 27) {
		keyCode = -1;
		control = true;
	}

	// Chose the function key map to use
	var fmap;
	var xind = 0;
	var fmap = this.FunctionKey;
	if (shift) {
		fmap = this.FunctionKeyShift;
		xind = 1;
	}
	if (control) {
		fmap = this.FunctionKeyCtrl;
		xind = 2;
	}
	if (alt) {
		fmap = this.FunctionKeyAlt;
		xind = 3;
	}
	switch (keyCode) {
	case VDUKeyEvent.VK_PAUSE:
		if (shift || control) {
			// TODO
			// this.sendTelnetCommand(243);
		}
		return;
	case VDUKeyEvent.VK_ESCAPE:
		this._writeSpecial(this.Escape[xind]);
		return;
	case VDUKeyEvent.VK_F1:
		this._writeSpecial(fmap[1]);
		return;
	case VDUKeyEvent.VK_F2:
		this._writeSpecial(fmap[2]);
		return;
	case VDUKeyEvent.VK_F3:
		this._writeSpecial(fmap[3]);
		return;
	case VDUKeyEvent.VK_F4:
		this._writeSpecial(fmap[4]);
		return;
	case VDUKeyEvent.VK_F5:
		this._writeSpecial(fmap[5]);
		return;
	case VDUKeyEvent.VK_F6:
		this._writeSpecial(fmap[6]);
		return;
	case VDUKeyEvent.VK_F7:
		this._writeSpecial(fmap[7]);
		return;
	case VDUKeyEvent.VK_F8:
		this._writeSpecial(fmap[8]);
		return;
	case VDUKeyEvent.VK_F9:
		this._writeSpecial(fmap[9]);
		return;
	case VDUKeyEvent.VK_F10:
		this._writeSpecial(fmap[10]);
		return;
	case VDUKeyEvent.VK_F11:
		this._writeSpecial(fmap[11]);
		return;
	case VDUKeyEvent.VK_F12:
		this._writeSpecial(fmap[12]);
		return;
	case VDUKeyEvent.VK_UP:
		this._writeSpecial(this.KeyUp[xind]);
		return;
	case VDUKeyEvent.VK_DOWN:
		this._writeSpecial(this.KeyDown[xind]);
		return;
	case VDUKeyEvent.VK_LEFT:
		this._writeSpecial(this.KeyLeft[xind]);
		return;
	case VDUKeyEvent.VK_RIGHT:
		this._writeSpecial(this.KeyRight[xind]);
		return;
	case VDUKeyEvent.VK_PAGE_DOWN:
		this._writeSpecial(this.NextScn[xind]);
		return;
	case VDUKeyEvent.VK_PAGE_UP:
		this._writeSpecial(this.PrevScn[xind]);
		return;
	case VDUKeyEvent.VK_INSERT:
		this._writeSpecial(this.Insert[xind]);
		return;
	case VDUKeyEvent.VK_DELETE:
		this._writeSpecial(this.Remove[xind]);
		return;
	case VDUKeyEvent.VK_BACK_SPACE:
		this._writeSpecial(this.BackSpace[xind]);
		if (this.localecho) {
			if (this.BackSpace[xind] == '\b') {
				this.putString('\b \b');
				// make the last char 'deleted'
			} else {
				if(this._vms) {
					switch(xind) {
					case 0:
					case 3:
						this.putString("\u007f");
						break;
					case 1:
						this.putString("\u000a");
						break;
					case 2:
						this.putString("\u0018");
						break;
					}
				}
				else
					this.putString(this.BackSpace[xind]);
			}
		}
		return;
	case VDUKeyEvent.VK_HOME:
		this._writeSpecial(this.KeyHome[xind]);
		return;
	case VDUKeyEvent.VK_END:
		this._writeSpecial(this.KeyEnd[xind]);
		return;
	case VDUKeyEvent.VK_NUM_LOCK:
		if (this._vms && control) {
			this._writeSpecial(PF1);
		}
		if (!control) {
			this._numlock = !this._numlock;
		}
		return;
	case VDUKeyEvent.VK_CAPS_LOCK:
		this._capslock = !this._capslock;
		return;
	case VDUKeyEvent.VK_SHIFT:
	case VDUKeyEvent.VK_CONTROL:
	case VDUKeyEvent.VK_ALT:
		return;
	}

	if (keyChar == '\t') {
		if (shift) {
			this.put(this.TabKey[1], false);
		} else {
			if (control) {
				this.put(this.TabKey[2], false);
			} else {
				if (alt) {
					this.put(this.TabKey[3], false);
				} else {
					this.put(this.TabKey[0], false);
				}
			}
		}
		return;
	}

	// If actual Enter key pressed
	if (keyCode == VDUKeyEvent.VK_ENTER && !control) {
		if (((this.inputEOL == ClientTerminalEmulation.EOL_DEFAULT) && this._sendcrlf) || (this.inputEOL == ClientTerminalEmulation.EOL_CR_LF)) {
			if (this.localecho) {
				this.putString("\r\n");
			}
			TermUtil.log(LVL_INFO, 'Writing CRLF. EOL = ' + this.inputEOL + ', sendcrlf = ' + this._sendcrlf);
			this.put("\r\n", false);
		} else {
			if (this.localecho) {
				this.putString("\r");
			}
			TermUtil.log(LVL_INFO, 'Writing CRL EOL = ' + this.inputEOL + ', sendcrlf = ' + this._sendcrlf);
			this.put("\r", false);
		}
		return;
	}
	// FIXME: on german PC keyboards you have to use Alt-Ctrl-q to get an
	// @,
	// so we can't just use it here... will probably break some other VMS
	// codes. -Marcus
	// if(((!vms && keyChar == '2') || keyChar == '@' || keyChar == ' ')
	// && control)
	if (((!this._vms && (keyChar == '2')) || (keyChar == ' ')) && control) {
		this.put(String.fromCharCode(0));
	}
	if (this._vms) {
		if ((keyChar == 127) && !control) {
			if (shift) {
				this._writeSpecial(this.Insert[0]);
			}
			// VMS shift delete = insert
			else {
				this._writeSpecial(this.Remove[0]);
			}
			// VMS delete = remove
			return;
		} else if (control) {
			switch (keyChar) {
			case '0':
				this._writeSpecial(this.Numpad[0][xind]);
				return;
			case '1':
				this._writeSpecial(this.Numpad[1][xind]);
				return;
			case '2':
				this._writeSpecial(this.Numpad[2][xind]);
				return;
			case '3':
				this._writeSpecial(this.Numpad[3][xind]);
				return;
			case '4':
				this._writeSpecial(this.Numpad[4][xind]);
				return;
			case '5':
				this._writeSpecial(this.Numpad[5][xind]);
				return;
			case '6':
				this._writeSpecial(this.Numpad[6][xind]);
				return;
			case '7':
				this._writeSpecial(this.Numpad[7][xind]);
				return;
			case '8':
				this._writeSpecial(this.Numpad[8][xind]);
				return;
			case '9':
				this._writeSpecial(this.Numpad[9][xind]);
				return;
			case '.':
				this._writeSpecial(this.NUMDot[xind]);
				return;
			case '-':
			case 31:
				this._writeSpecial(this.NUMMinus[xind]);
				return;
			case '+':
				this._writeSpecial(this.NUMPlus[xind]);
				return;
			case 10:
				this._writeSpecial(this.NUMEnter[xind]);
				return;
			case '/':
				this._writeSpecial(this.NUMDivide[xind]);
				return;
			case '*':
				this._writeSpecial(this.NUMMultiply[xind]);
				return;
				/*
				 * NUMLOCK handled in keyPressed
				 */
			default:
				break;
			}
		}
		/*
		 * Now what does this do and how did it get here. -Marcus if (shift
		 * && keyChar < 32) { write(PF1+(char)(keyChar + 64)); return; }
		 */
	}
	TermUtil.log(LVL_DEBUG, 'vt320: keyPressed ' + keyCode + ' "' + keyChar + '"');

	if (keyCode == VDUKeyEvent.VK_ESCAPE) {
		this._writeSpecial(this.Escape[xind]);
		return;
	}

	if (this.keypadmode) {
		switch (keyCode) {
		case VDUKeyEvent.VK_NUMPAD0:
			this._writeSpecial(this.Numpad[0][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD1:
			this._writeSpecial(this.Numpad[1][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD2:
			this._writeSpecial(this.Numpad[2][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD3:
			this._writeSpecial(this.Numpad[3][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD4:
			this._writeSpecial(this.Numpad[4][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD5:
			this._writeSpecial(this.Numpad[5][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD6:
			this._writeSpecial(this.Numpad[6][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD7:
			this._writeSpecial(this.Numpad[7][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD8:
			this._writeSpecial(this.Numpad[8][xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD9:
			this._writeSpecial(this.Numpad[9][xind]);
			return;
		case VDUKeyEvent.VK_DECIMAL:
			this._writeSpecial(this.NUMDot[xind]);
			return;
		case VDUKeyEvent.VK_ADD:
			this._writeSpecial(this.NUMPlus[xind]);
			return;
		case VDUKeyEvent.VK_NUM_LOCK:
			this._writeSpecial(this.NUMLock[xind]);
			return;
		case VDUKeyEvent.VK_DIVIDE:
			this._writeSpecial(this.NUMDivide[xind]);
			return;
		case VDUKeyEvent.VK_MULTIPLY:
			this._writeSpecial(this.NUMMultiply[xind]);
			return;
		case VDUKeyEvent.VK_SUBTRACT:
			this._writeSpecial(this.NUMMinus[xind]);
			return;
		case VDUKeyEvent.VK_NUMPAD_ENTER:
			this._writeSpecial(this.NUMEnter[xind]);
			return;
		}
	}

	// If Ctrl+J pressed
	if (keyChar == '\n' && !control) {
		if (this.localecho) {
			this.putString("\n");
		}
		this.put("\n", false);
		return;
	}
	// If Ctrl+M pressed
	if (keyChar == '\r' && !control) {
		TermUtil.log(LVL_INFO, 'Sending \\r');
		this.put('\r', false);
		return;
	}

	if (!((keyChar.charCodeAt(0) == 8) || (keyChar.charCodeAt(0) == 127) || (keyChar == '\r') || (keyChar == '\n'))) {
		this.put(keyChar);
		return;
	}
};

ClientTerminalEmulation.prototype.mouseUpdate = function(x, y, modifiers) {
	TermUtil.log(LVL_INFO, 'Mouse update at ' +  x + ',' + y + ' mods:' + modifiers);
	
	if (this._mouserpt == 0) {
		return;
	}
	var mods = modifiers;
	this._mousebut = this._encodeMouseButton(mods);
	var mousecode;
	if (this.mouserpt == 9) {
		/*
		 * X10 Mouse
		 */
		mousecode = 0x20 | this._mousebut;
	} else {
		/*
		 * normal xterm mouse reporting
		 */
		mousecode = this._encodeMouseModifiers(this._mousebut | 0x20, mods);
	}
	this.put(ClientTerminalEmulation.ESC +  '[M' + String.fromCharCode(mousecode) + String.fromCharCode(0x20 + x + 1) + String.fromCharCode(0x20 + y + 1) );
}

ClientTerminalEmulation.prototype.put = function(s, doecho) {
	if(arguments.length < 2)
		doecho = this.localecho;
	
	TermUtil.log(LVL_DEBUG, 'put(|' + s + '|' + doecho + ')');
	if (s == null) {
		// aka the empty string.
		return true;
	}
	this.io.write(s);
	if (doecho) {
		if(this.maskInput) {
			this.putCharacter(this.maskCharacter, true);
		} else {
			this.putString(s);
		}
	}
	return true;
}

ClientTerminalEmulation.prototype.setVMS = function(vms) {
	this._vms = vms;
};

ClientTerminalEmulation.prototype.setAnswerBack = function(ab) {
	this._answerBack = this.unescape(ab);
};

ClientTerminalEmulation.prototype.setAlternateBuffer = function(alternateBuffer) {
	if (this._alternateBuffer == alternateBuffer) {
		return;
	}
	VDUBuffer.prototype.setAlternateBuffer.call(this, alternateBuffer);
	if (this.alternateBuffer) {
		this._oldSc = this._Sc;
		this._oldSr = this._Sr;
		this._Sc = 0;
		this._Sr = 0;
	} else {
		this._Sc = this._oldSc;
		this._Sr = this._oldSr;
	}
}

//
// Constants
//
ClientTerminalEmulation.NO_COPY = 0;
ClientTerminalEmulation.COPY_ON_SELECT = 1;
ClientTerminalEmulation.COPY_ON_CTRL_C = 2;
ClientTerminalEmulation.COPY_ON_CTRL_SHIFT_C = 4;
ClientTerminalEmulation.NO_PASTE = 0;
ClientTerminalEmulation.PASTE_ON_MIDDLE_CLICK = 1;
ClientTerminalEmulation.PASTE_ON_CTRL_V = 2;
ClientTerminalEmulation.PASTE_ON_CTRL_SHIFT_V = 4;

//
// Private
//

ClientTerminalEmulation.prototype._encodeMouseModifiers = function(mousecode, mods) {
	if ((mods & VDUInput.KEY_CONTROL) != 0) {
		mousecode += 16;
	}
	if ((mods & VDUInput.KEY_ALT) != 0) {
		mousecode = 8;
	}
	if ((mods & VDUInput.KEY_SHIFT) != 0) {
		mousecode = 4;
	}
	return mousecode;
}

ClientTerminalEmulation.prototype._encodeMouseButton = function(mods) {
	if ((mods & VDUInput.MOUSE_1) != 0) {
		return 0;
	}
	if ((mods & VDUInput.MOUSE_2) != 0) {
		return 1;
	}
	if ((mods & VDUInput.MOUSE_3) != 0) {
		return 2;
	}
	return 3;
};

ClientTerminalEmulation.prototype._map_cp850_unicode = function(x) {
	if (x >= 0x100) {
		return x;
	}
	return UNIMAP[x];
};

ClientTerminalEmulation.prototype._isInvalidCharacterSet = function(c) {
	if(typeof c != 'string')
		c = String.fromCharCode(c);
	return (c != '0') && (c != 'A') && (c != 'B') && (c != '<')
			&& (c != '2' && c != '1');
};

ClientTerminalEmulation.prototype._putCharacter = function(c, doshowcursor) {
	if(typeof c == 'string')
		c = c.charCodeAt(0);
	
	var rows = this.height;

	var tm = this.topMargin;
	var bm = this.bottomMargin;
	
	var mapped = false;
	if (c > 255) {
		TermUtil.log(LVL_WARN, 'char > 255:' + c + ' [' + String.fromCharCode(c) + ']');
	}
	switch (this._term_state) {
	case ClientTerminalEmulation.TSTATE_DATA:
		TermUtil.log(LVL_DEBUG, 'DATA');
		/*
		 * FIXME: we shouldn't use chars with bit 8 set if ibmcharset.
		 * probably... but some BBS do anyway...
		 */
		if (!this.useibmcharset && this._output8bit) {
			if (this._handle_ctrl(c, rows, tm, bm)) {
				break;
			}
		}
		this._handle_char(c, rows, this.width, tm, bm, mapped);
		break;
	case ClientTerminalEmulation.TSTATE_OSC:
		TermUtil.log(LVL_DEBUG, 'OSC');
		this._handle_osc(c, rows, this.width, tm, bm);
		break;
	case ClientTerminalEmulation.TSTATE_ESCSPACE:
		TermUtil.log(LVL_DEBUG, 'ESCSPACE');
		this._handle_escspace(c);
		break;
	case ClientTerminalEmulation.TSTATE_CSI_SPACE:
		TermUtil.log(LVL_DEBUG, 'CSI_SPACE');
		this._handle_csi_space(c);
		break;
	case ClientTerminalEmulation.TSTATE_ESC:
		TermUtil.log(LVL_DEBUG, 'ESC');
		this._handle_esc(c, tm, bm);
		break;
	case ClientTerminalEmulation.TSTATE_VT52X:
		TermUtil.log(LVL_DEBUG, 'VT52X');
		this.cursorX = c - 37;
		this._term_state = ClientTerminalEmulation.TSTATE_VT52Y;
		break;
	case ClientTerminalEmulation.TSTATE_VT52Y:
		TermUtil.log(LVL_DEBUG, 'VT52Y');
		this.cursorY= c - 37;
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		break;
	case ClientTerminalEmulation.TSTATE_SETG0:
		TermUtil.log(LVL_DEBUG, 'SETG0');
		if (this._isInvalidCharacterSet(c)) {
			TermUtil.log(LVL_INFO, 'ESC ( ' + c + ': G0 char set?  (' + c + ')');
		} else {
			TermUtil.log(LVL_INFO, 'ESC ( : G0 char set  (' + c + ' ' + c + ')');
			this._gx[0] = c;
		}
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		break;
	case ClientTerminalEmulation.TSTATE_SETG1:
		TermUtil.log(LVL_DEBUG, 'SETG1');
		if (this._isInvalidCharacterSet(c)) {
			TermUtil.log(LVL_INFO, 'ESC ) ' + c + ' (' + (c) + ') :G1 char set?');
		} else {
			TermUtil.log(LVL_INFO, 'ESC ) :G1 char set  (' + c + ' ' + (c) + ')');
			this._gx[1] = c;
		}
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		break;
	case ClientTerminalEmulation.TSTATE_SETG2:
		TermUtil.log(LVL_DEBUG, 'SETG2');
		if (this._isInvalidCharacterSet(c)) {
			TermUtil.log(LVL_INFO, 'ESC*:G2 char set?  (' + (c) + ')');
		} else {
			TermUtil.log(LVL_INFO, 'ESC*:G2 char set  (' + c + ' ' + (c) + ')');
			this._gx[2] = c;
		}
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		break;
	case ClientTerminalEmulation.TSTATE_SETG3:
		TermUtil.log(LVL_DEBUG, 'SETG3');
		if (this._isInvalidCharacterSet(c)) {
			TermUtil.log(LVL_INFO, 'ESC+:G3 char set?  (' + (c) + ')');
		} else {
			TermUtil.log(LVL_INFO, 'ESC+:G3 char set  (' + c + ' ' + (c) + ')');
			this._gx[3] = c;
		}
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		break;
	case ClientTerminalEmulation.TSTATE_ESCSQUARE:
		TermUtil.log(LVL_DEBUG, 'ESCSQUARE');
		this._handle_escsquare(c);
		break;
	case ClientTerminalEmulation.TSTATE_DCS:
		TermUtil.log(LVL_DEBUG, 'DCS ' + c);
		if ((c == '\\'.charCodeAt(0)) && (this._dcs.charAt(this._dcs.length - 1).charCodeAt(0) == ClientTerminalEmulation.ESC)) {
			this._handle_dcs(_dcs);
			this._term_state = ClientTerminalEmulation.TSTATE_DATA;
			break;
		}
		this._dcs = this._dcs + c;
		break;
	case ClientTerminalEmulation.TSTATE_DCEQ:
		TermUtil.log(LVL_DEBUG, 'DCEQ');
		this._handle_dceq(c);
		break;
	case ClientTerminalEmulation.TSTATE_DCERQPM:
		TermUtil.log(LVL_DEBUG, 'DCERQPM');
		this._handle_dcerqpm(c);
		break;
	case ClientTerminalEmulation.TSTATE_CSI_EX:
		TermUtil.log(LVL_DEBUG, 'CSI_EX');
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		switch (c) {
		case  ClientTerminalEmulation.ESC:
			this._term_state = ClientTerminalEmulation.TSTATE_ESC;
			break;
		case 'p':
			/*
			 * DECSTR - Softreset
			 */
			this.softReset();
			break;
		default:
			TermUtil.log(LVL_WARN, 'Unknown character ESC[! character is ' + c);
			break;
		}
		break;
	case ClientTerminalEmulation.TSTATE_CSI_TICKS:
		TermUtil.log(LVL_DEBUG, 'CSI_TICKS');
		this._handle_csi_ticks(c);
		break;
	case ClientTerminalEmulation.TSTATE_CSI_DOLLAR:
		TermUtil.log(LVL_DEBUG, 'CSI_DOLLAR');
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		switch (c) {
		case '}':
			TermUtil.log(LVL_INFO, 'Active Status Display now ' + this._DCEvars[0]);
			break;
		case '~':
			TermUtil.log(LVL_INFO, 'Status Line mode now ' + this._DCEvars[0]);
			break;
		default:
			TermUtil.log(LVL_INFO, 'UNKNOWN Status Display code ' + c + ', with Pn=' + this._DCEvars[0]);
			break;
		}
		break;
	case ClientTerminalEmulation.TSTATE_CSI:
		TermUtil.log(LVL_DEBUG, 'CSI');
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		this._handle_csi(c, tm, bm);
		break;
	default:
		TermUtil.log(LVL_DEBUG, 'DEFAULT');
		this._term_state = ClientTerminalEmulation.TSTATE_DATA;
		break;
	}
	if (this.doshowcursor) {
		this.setCursorPosition(this.cursorX, this.cursorY);
	}
	this.markLine(this.cursorY, 1);
};

ClientTerminalEmulation.prototype._handle_escspace = function(c) {
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	switch (String.fromCharCode(c)) {
	case 'F':
		/*
		 * S7C1T, Disable output of 8-bit controls, use 7-bit
		 */
		this._output8bit = false;
		break;
	case 'G':
		/*
		 * S8C1T, Enable output of 8-bit control codes
		 */
		this._output8bit = true;
		break;
	default:
		TermUtil.log(LVL_WARN, 'ESC <space> ' + c + ' unhandled.');
	}
};

ClientTerminalEmulation.prototype._handle_char = function(c, rows, columns, tm, bm, mapped) {

	switch (c) {
	case ClientTerminalEmulation.SS3:
		this._onegl = 3;
		break;
	case ClientTerminalEmulation.SS2:
		this._onegl = 2;
		break;
	case ClientTerminalEmulation.CSI:
		// should be in the 8bit section, but some BBS use this
		this._DCEvar = 0;
		this._DCEvars[0] = 0;
		this._DCEvars[1] = 0;
		this._DCEvars[2] = 0;
		this._DCEvars[3] = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case ClientTerminalEmulation.ESC:
		this._term_state = ClientTerminalEmulation.TSTATE_ESC;
		this._lastwaslf = 0;
		break;
	case 5:
		/*
		 * ENQ
		 */
		this.put(this._answerBack, false);
		break;
	case 12:
		/*
		 * FormFeed, Home for the BBS world
		 */
		this.deleteArea(0, 0, columns, rows, this._attributes, true, false, true);
		this.cursorX = this.cursorY = 0;
		break;
	case '\b'.charCodeAt(0):
		/*
		 * 8
		 */
		if (this.cursorX == this._getColumnsForLine(this._cursorY)) {
			this.cursorX--;
		}
		this.cursorX--;
		if (this.cursorX < 0) {
			this.cursorX = 0;
		}
		this._lastwaslf = 0;
		break;
	case '\t'.charCodeAt(0):
		this._advanceTab();
		this._lastwaslf = 0;
		break;
	case '\r'.charCodeAt(0):
		this.cursorX = 0;
		this.markLine(this.cursorY, 1);
		if (this.outputEOL == ClientTerminalEmulation.EOL_CR_LF) {
			this._handle_newline(bm, c, rows);
		}
		break;
	case '\n'.charCodeAt(0):

		TermUtil.log(LVL_DEBUG, 'R= ' + this.cursorY + ', bm ' + bm + ', tm=' + tm + ', rows=' + rows);
		if(this._handle_newline(bm, c, rows))
			break;
		if (this.outputEOL == ClientTerminalEmulation.EOL_LF_CR) {
			this.cursorX = 0;
		}
		break;
	case 7:
		this.display.beep();
		break;
	case '\016'.charCodeAt(0):
		/*
		 * SMACS , as
		 */
		/*
		 * ^N, Shift out - Put G1 into GL
		 */
		this._gl = 1;
		this._usedcharsets = true;
		break;
	case '\017'.charCodeAt(0):
		/*
		 * RMACS , ae
		 */
		/*
		 * ^O, Shift in - Put G0 into GL
		 */
		this._gl = 0;
		this._usedcharsets = true;
		break;
	default: {
		var thisgl = this._gl;
		if (this._onegl >= 0) {
			thisgl = this._onegl;
			this._onegl = -1;
		}
		this._lastwaslf = 0;
		if (c < 32) {
			if (c != 0) {
				TermUtil.log(LVL_DEBUG, 'TSTATE_DATA char: ' + c);
			}
			/*
			 * break; some BBS really want those characters, like hearst
			 * etc.
			 */
			if (c == 0) {
				/*
				 * print 0 ... you bet
				 */
				break;
			}
		}
		// Mapping if DEC Special is chosen charset
		if (this._usedcharsets) {
			if ((c >= 0x20) && (c <= 0x7f)) {

				// GL

				switch (String.fromCharCode(this._gx[thisgl])) {
				case '0':
				case '2':
					// Remap SCOANSI line drawing to VT100
					// line
					// drwing chars
					// for our SCO using customers.
					if (this.termType == ClientTerminalEmulation.SCOANSI) {
						for (var i = 0; i < this.scoansi_acs.length; i += 2) {
							if (c == this.scoansi_acs.charAt(i)) {
								c = this.scoansi_acs.charAt(i + 1);
								break;
							}
						}
					}
					if ((c >= 0x5f) && (c <= 0x7e)) {
						c = DECSPECIAL[c - 0x5f];
						mapped = true;
					}
					break;
				case '<':
					// 'user preferred' is currently 'ISO
					// Latin-1
					// suppl
					c = ((c & 0x7f) | 0x80);
					mapped = true;
					break;
				case 'A':
					// British
					c = c == '$' ? 0xA3 : c;
					mapped = true;
					break;
				case 'B':
				case '1':
					// Latin-1 , ASCII -> fall through
					mapped = true;
					break;
				default:
					TermUtil.log(LVL_WARN, 'Unsupported GL (' + thisgl + ') mapping: ' + String.fromCharCode(this._gx[thisgl]));
					break;
				}
			}

			// GR
			if (!mapped && ((c >= 0x80) && (c <= 0xff))) {
				switch (String.fromCharCode(this._gx[this._gr])) {
				case '0':
				case '2':
					if (this.charsetName == null) {
						c = this.map_cp850_unicode(c);
						mapped = true;
					} else {
						if ((c >= 0xdf) && (c <= 0xfe)) {
							var nxx = c;
							var nyy = c - 0xdf;
							var oc = c;
							c = DECSPECIAL[c - 0xdf];
							mapped = true;
						}
					}
					break;
				case '<':
				case 'A':
				case 'B':
					mapped = true;
					break;
				default:
					TermUtil.log(LVL_WARN, 'Unsupported GR mapping: ' + this._gx[this._gr]);
					break;
				}
			}
		}
		if (!mapped && this.useibmcharset) {
			c = this.map_cp850_unicode(c);
		}
		/*
		 * if(true || (statusmode == 0)) {
		 */

		this._checkForWrap();
		if (this._insertmode == 1) {
			TermUtil.log(LVL_DEBUG, 'Inserting character "' + c + '" at col ' + this.cursorX + ' line ' + this.cursorY);
			this.cursorX += this._insertChar(this.cursorX, this.cursorY, c, this._attributes);
		} else {
			TermUtil.log(LVL_DEBUG, 'Putting character "' + c + "' at col " + this.cursorX + " line " + this.cursorY);
			this.cursorX += this._writeChar(this.cursorX, this.cursorY, c, this._attributes);
		}
		break;
		}
	}
};

ClientTerminalEmulation.prototype._handle_newline = function(bm, c, rows) {
	if (!this._vms) {
		if ((this._lastwaslf != 0) && (this._lastwaslf != c)) {
			// Ray: I do not understand this logic.
			return true;
		}
		this._lastwaslf = c;
		/*
		 * C = 0;
		 */
	}
	// TODO hmm
	this.markLine(this.cursorY, 1);
	this.lineMarker[this.screenBase + this.cursorY] = true;
	if ((this.cursorY == bm) || (this.cursorY >= (rows - 1))) {
		this.insertLine(this.cursorY, 1, SCROLL_UP);
	} else {
		this.cursorY = this.cursorY + 1;
	}
	return false;
}

ClientTerminalEmulation.prototype._handle_esc = function(c, tm, bm) {
	var linecols = this._getColumnsForLine(this.cursorY);
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	switch (String.fromCharCode(c)) {
	case ' ':
		this._term_state = ClientTerminalEmulation.TSTATE_ESCSPACE;
		break;
	case '#':
		this._term_state = ClientTerminalEmulation.TSTATE_ESCSQUARE;
		break;
	case 'c':
		/*
		 * RIS - Hard terminal reset
		 */
		this.hardReset();
		break;
	case '[':
		this._DCEvar = 0;
		this._DCEvars[0] = 0;
		this._DCEvars[1] = 0;
		this._DCEvars[2] = 0;
		this._DCEvars[3] = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case ']':
		this._osc = '';
		this._term_state = ClientTerminalEmulation.TSTATE_OSC;
		break;
	case 'P':
		this._dcs = '';
		this._term_state = ClientTerminalEmulation.TSTATE_DCS;
		break;
	case 'A':
		/*
		 * CUU
		 */
		TermUtil.log(LVL_DEBUG, 'CUU');
		this.cursorY = this.cursorY - 1;
		if (this.cursorY < 0) {
			this.cursorY = 0;
		}
		break;
	case 'B':
		/*
		 * CUD
		 */
		this.cursorY = this.cursorY + 1;
		TermUtil.log(LVL_DEBUG, 'CUD');
		if (this.cursorY > (this.height - 1)) {
			this.cursorY = this.height - 1;
		}
		break;
	case 'C':
		this.cursorX++;
		if (this.cursorX >= this.linecols) {
			this.cursorX = this.linecols - 1;
		}
		break;
	case 'I':
		// RI
		this.insertLine(this.cursorY, 1, SCROLL_DOWN);
		break;
	case 'E':
		/*
		 * NEL
		 */
		if ((this.cursorY == bm) || (this.cursorY == (this.height - 1))) {
			this.insertLine(this.cursorY, 1, SCROLL_UP);
		} else {
			this.cursorY = this.cursorY + 1;
		}
		this.cursorX = 0;

		TermUtil.log(LVL_INFO, 'ESC E (at ' + this.cursorY + ')');
		break;
	case 'D':
		/*
		 * IND
		 */
		if ((this.cursorY == bm) || (this.cursorY == (this.height - 1))) {
			this.insertLine(this.cursorY, 1, SCROLL_UP);
		} else {
			this.cursorY = this.cursorY + 1;
		}

		TermUtil.log(LVL_DEBUG, 'ESC D (at ' + this.cursorY + ' )');
		break;
	case 'J':
		/*
		 * ED - erase to end of screen
		 */
		if (this.cursorY < (this.height - 1)) {
			this.deleteArea(0, this.cursorY + 1, this.width, this.height - this.cursorY - 1, this._attributes, true, false, true);
		}
		if (this.cursorX < (this.width - 1)) {
			this.deleteArea(this.cursorX, this.cursorY, this.width - this.cursorX, 1, this._attributes, true, false, true);
		}
		break;
	case 'K':
		if (this.cursorX < (this.width - 1)) {
			this.deleteArea(this.cursorX, this.cursorY, this.width - this.cursorX, 1, this._attributes, true, false, false);
		}
		break;
	case 'M':
		// RI
		if (this.cursorY > bm) {
			// outside scrolling region
			break;
		}
		if (this.cursorY > tm) {
			// just go up 1 line.
			this.cursorY = this.cursorY - 1;
		} else {
			// scroll down
			this.insertLine(this.cursorY, 1, SCROLL_DOWN);
		}
		/*
		 * else do nothing ;
		 */

		TermUtil.log(LVL_DEBUG, 'ESC M ');
		break;
	case 'H':

		TermUtil.log(LVL_DEBUG, 'ESC H at ' + this.cursorX);
		/*
		 * right border probably ...
		 */
		if (this.cursorX >= this.linecols) {
			this.cursorX = this.linecols - 1;
		}
		this.Tabs[this.cursorX] = 1;
		break;
	case 'N':
		// SS2
		this._onegl = 2;
		break;
	case 'O':
		// SS3
		this._onegl = 3;
		break;
	case '=':
		/*
		 * application keypad
		 */

		TermUtil.log(LVL_DEBUG, 'ESC =');
		this.keypadmode = true;
		break;
	case '<':
		/*
		 * vt52 mode off
		 */
		this._vt52mode = false;
		break;
	case '>':
		/*
		 * normal keypad
		 */

		TermUtil.log(LVL_DEBUG, 'ESC >');
		this.keypadmode = false;
		break;
	case '7':
		/*
		 * save cursor, attributes, margins
		 */
		this._Sc = this.cursorX;
		this._Sr = this.cursorY;
		this._Sgl = this._gl;
		this._Sgr = this._gr;
		this._Sa = this._attributes;
		this._Sgx = new Array(4);
		for (var i = 0; i < 4; i++) {
			this._Sgx[i] = this._gx[i];
		}
		this._Smoveoutsidemargins = this._moveoutsidemargins;

		TermUtil.log(LVL_DEBUG, 'ESC 7');
		break;
	case '8':
		/*
		 * restore cursor, attributes, margins
		 */
		this.cursorX = this._Sc;
		this.cursorY = this._Sr;
		this._gl = this._Sgl;
		this._gr = this._Sgr;
		for (var i = 0; i < 4; i++) {
			this._gx[i] = this._Sgx[i];
		}
		this._moveoutsidemargins = this._Smoveoutsidemargins;
		this._attributes = this._Sa;
		TermUtil.log(LVL_DEBUG, 'ESC 8');
		break;
	case '(':
		/*
		 * Designate G0 Character set (ISO 2022)
		 */
		this._term_state = ClientTerminalEmulation.TSTATE_SETG0;
		this._usedcharsets = true;
		break;
	case ')':
		/*
		 * Designate G1 character set (ISO 2022)
		 */
		this._term_state = ClientTerminalEmulation.TSTATE_SETG1;
		this._usedcharsets = true;
		break;
	case '*':
		/*
		 * Designate G2 Character set (ISO 2022)
		 */
		this._term_state = ClientTerminalEmulation.TSTATE_SETG2;
		this._usedcharsets = true;
		break;
	case '+':
		/*
		 * Designate G3 Character set (ISO 2022)
		 */
		this._term_state = ClientTerminalEmulation.TSTATE_SETG3;
		this._usedcharsets = true;
		break;
	case '~':
		/*
		 * Locking Shift 1, right
		 */
		this._gr = 1;
		this._usedcharsets = true;
		break;
	case 'n':
		/*
		 * Locking Shift 2
		 */
		this._gl = 2;
		this._usedcharsets = true;
		break;
	case '}':
		/*
		 * Locking Shift 2, right
		 */
		this._gr = 2;
		this._usedcharsets = true;
		break;
	case 'o':
		/*
		 * Locking Shift 3
		 */
		this._gl = 3;
		this._usedcharsets = true;
		break;
	case '|':
		/*
		 * Locking Shift 3, right
		 */
		this._gr = 3;
		this._usedcharsets = true;
		break;
	case 'Y':
		/*
		 * vt52 cursor address mode , next chars are x,y
		 */
		this._term_state = ClientTerminalEmulation.TSTATE_VT52Y;
		break;
	case 'V':
		/*
		 * SPA - Start protected area
		 */
		this._attributes |= GUARDED;
		break;
	case 'W':
		/*
		 * EPA - Start protected area
		 */
		this._attributes &= ~GUARDED;
		break;
	default:
		TermUtil.log(LVL_WARN, 'ESC unknown letter: ' + c + ' (' + c + ')');
		break;
	}
};

ClientTerminalEmulation.prototype._handle_csi_space = function(c) {
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	switch (String.fromCharCode(c)) {
	case '@':
		/*
		 * SL - Scroll whole screen left
		 */
		TermUtil.log(LVL_INFO, 'ESC [ <space> ' + this._DCEvars[0] + ' @');
		for (var j = 0; j <= this.height; j++) {
			for (var i = 0; i < Math.max(this._DCEvars[0], 1); i++) {
				this._deleteChar(0, j, this._attributes);
				this._insertChar(this.cursorX, j, ' ', this._attributes);
				this.markLine(j, 1);
			}
		}
		break;
	case 'A':
		/*
		 * SR - Scroll whole screen right
		 */
		TermUtil.log(LVL_INFO, 'ESC [ <space> ' + this._DCEvars[0] + ' @');
		for (var j = 0; j <= this.height; j++) {
			for (var i = 0; i < Math.max(this._DCEvars[0], 1); i++) {
				this._insertChar(0, j, ' ', this._attributes);
			}
		}
		break;
	case 'q':
		/* Cursor style DECSCUSR vt520 */
		switch(this._DCEvars[0]) {
		case 0:
		case 1:
			this.display.setCursorBlink(true);
			this.display.cursorStyle = CURSOR_BLOCK;
			break;
		case 2:
			this.display.setCursorBlink(false);
			this.display.cursorStyle = CURSOR_BLOCK;
			break;
		case 3:
		case 5:
			this.display.setCursorBlink(true);
			this.display.cursorStyle = CURSOR_LINE;
			break;
		case 4:
		case 6:
			this.display.setCursorBlink(false);
			this.display.cursorStyle = CURSOR_LINE;
			break;
		}
		this.markLine(this.cursorY, 1);
		this.dispatchEvent('bufferChange');
		break;		
	default:
		log(LOG_WARN, 'ESC [ <space> ' + c + ' unhandled.');
	}
}

ClientTerminalEmulation.prototype._handle_ctrl = function(c, rows, tm, bm) {
	var doneflag = true;
	switch (String.fromCharCode(c)) {
	case ClientTerminalEmulation.OSC:
		this._osc = '';
		this._term_state = ClientTerminalEmulation.TSTATE_OSC;
		break;
	case ClientTerminalEmulation.RI:
		if (this.cursorY > tm) {
			this.cursorY = this.cursorY - 1;
		} else {
			this.insertLine(this.cursorY, 1, SCROLL_DOWN);
		}
		TermUtil.log(LVL_DEBUG, 'RI');
		break;
	case ClientTerminalEmulation.IND:
		log.debug('IND at ' + this.cursorY + ', tm is ' + tm + ', bm is ' + bm);
		if ((this.cursorY == bm) || (this.cursorY == (this.rows - 1))) {
			this.insertLine(this.cursorY, 1, SCROLL_UP);
		} else {
			this.cursorY = this.cursorY + 1;
		}

		TermUtil.log(LVL_DEBUG, 'IND (at ' + this.cursorY + ' )');
		break;
	case ClientTerminalEmulation.NEL:
		if ((this.cursorY == bm) || (this.cursorY == (this.rows - 1))) {
			this.insertLine(this.cursorY, 1, SCROLL_UP);
		} else {
			this.cursorY = this.cursorY + 1;
		}
		this.cursorX = 0;
		TermUtil.log(LVL_DEBUG, 'NEL (at ' + this.cursorY + ' )');
		break;
	case ClientTerminalEmulation.HTS:
		this.Tabs[this.cursorX] = 1;
		TermUtil.log(LVL_DEBUG, "HTS");
		break;
	case ClientTerminalEmulation.DCS:
		this._dcs = "";
		this._term_state = ClientTerminalEmulation.TSTATE_DCS;
		break;
	default:
		doneflag = false;
		break;
	}
	return doneflag;	
};

ClientTerminalEmulation.prototype._handle_csi = function(c, tm, bm) {

	var cols = this._getColumnsForLine(this.cursorY);
	switch (String.fromCharCode(c)) {
	case '"':
		this._term_state = ClientTerminalEmulation.TSTATE_CSI_TICKS;
		break;
	case '$':
		this._term_state = ClientTerminalEmulation.TSTATE_CSI_DOLLAR;
		break;
	case '!':
		this._term_state = ClientTerminalEmulation.TSTATE_CSI_EX;
		break;
	case '?':
		this._DCEvar = 0;
		this._DCEvars[0] = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_DCEQ;
		break;
	case 0x0a:
		// This is a work around for case #20503. An application is
		// inserting 0a in a CSI sequence
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case '0':
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9':
		this._DCEvars[this._DCEvar] = ((this._DCEvars[this._DCEvar] * 10) + c) - 48;
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case ';':
		this._DCEvar++;
		this._DCEvars[this._DCEvar] = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case '>':
		this.put(String.fromCharCode(ClientTerminalEmulation.ESC) + '[>0;1;0c', false);
		break;
	case String.fromCharCode(11):
		var ylimit;
		if (this.cursorY < tm) {
			ylimit = tm - 1;
		} else if (this.cursorY <= bm) {
			ylimit = bm;
		} else {
			ylimit = this.height - 1;
		}
		this.cursorY = this.cursorY + 1;
		
		if (this.cursorY > ylimit) {
			TermUtil.log(LVL_DEBUG, 'Limited from ' + this.cursorY + ' to ' + ylimit);
			this.cursorY = ylimit;
		} else {
			TermUtil.log(LVL_DEBUG, 'Not limited.');
		}
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case '\r':
		this.cursorX = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case '\b':
		/*
		 * 8
		 */
		this.cursorX--;
		if (this.cursorX < 0) {
			this.cursorX = 0;
		}
		this._lastwaslf = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_CSI;
		break;
	case 'y':
		/*
		 * DECTST - self test
		 */
		TermUtil.log(LVL_DEBUG, 'ESC [ y (DECTST - not implemented)');
		break;
	case 'c':
		/*
		 * send primary device attributes
		 */
		/*
		 * send (ESC[?61c)
		 */
		var subcode = '';
		if (this.termType == 'vt320') {
			subcode = '63;';
		}
		if (this.termType == 'vt220' || this.termType == 'ansi') {
			subcode = '62;';
		}
		if (this.termType == 'vt100') {
			subcode = '61;';
		}
		this.put(String.fromCharCode(ClientTerminalEmulation.ESC) + '[?' + subcode + '1;2;6c', false);
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' c');
		break;
	case 'g':
		/*
		 * used for tabsets
		 */
		switch (this._DCEvars[0]) {
		case 3:
			/*
			 * clear them
			 */
			this.Tabs = TermUtil.mkarray(this.width, 0);;
			break;
		case 0:
			this.Tabs[this.cursorX] = 0;
			break;
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' g');
		break;
	case 'h':
		switch (this._DCEvars[0]) {
		case 4:
			TermUtil.log(LVL_INFO, 'Now insert mode');
			this._insertmode = 1;
			break;
		case 20:
			this._sendcrlf = true;
			break;
		case 12:
			this.localecho = false;
			break;
		default:
			TermUtil.log(LVL_WARN, 'unsupported: ESC [ ' + this._DCEvars[0] + ' h');
			break;
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' h');
		break;
	case 'i':
		// Printer Controller mode.
		// "Transparent printing sends all output, except the
		// CSI 4 i
		// termination string, to the printer and not the
		// screen,
		// uses an 8-bit channel if no parity so NUL and DEL
		// will be
		// seen by the printer and by the termination recognizer
		// code,
		// and all translation and character set selections are
		// bypassed."
		switch (this._DCEvars[0]) {
		case 0:
			TermUtil.log(LVL_WARN, 'CSI 0 i:  Print Screen, not implemented.');
			break;
		case 4:
			TermUtil.log(LVL_WARN, 'CSI 4 i:  Enable Transparent Printing, not implemented.');
			break;
		case 5:
			TermUtil.log(LVL_WARN, 'CSI 4/5 i:  Disable Transparent Printing, not implemented.');
			break;
		default:
			TermUtil.log(LVL_WARN, 'ESC [ ' + this._DCEvars[0] + ' i, unimplemented!');
			break;
		}
		break;
	case 'l':
		switch (this._DCEvars[0]) {
		case 4:
			TermUtil.log(LVL_INFO, 'Leaving insert mode');
			this._insertmode = 0;
			break;
		case 20:
			this._sendcrlf = false;
			break;
		case 12:
			this.localecho = true;
			break;
		default:
			TermUtil.log(LVL_WARN, 'ESC [ ' + this._DCEvars[0] + ' l, unimplemented!');
			break;
		}
		break;
	case 'A': {
		// CUU
		this._decreaseCursor(tm, bm);
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' A');
		break;
	}
	case 'F': {
		// CPL
		this._decreaseCursor(tm, bm);
		this.cursorX = 0;
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' F');
		break;
	}
	case 'B': {
		// CUD
		/*
		 * cursor down n (1) times
		 */
		this._increaseCursor(tm, bm);
		TermUtil.log(LVL_DEBUG, 'to: ' + this.cursorY);
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' B (at C=' + this.cursorX + ')');
		break;
	}
	case 'E': {
		// CNL
		/*
		 * cursor down n (1) times
		 */
		this._increaseCursor(tm, bm);
		this.cursorX = 0;
		TermUtil.log(LVL_DEBUG, 'to: ' + this.cursorY);
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' E (at C=' + this.cursorX + ')');
		break;
	}
	case 'C':
		if (this._DCEvars[0] == 0) {
			this.cursorX++;
		} else {
			this.cursorX += this._DCEvars[0];
		}
		var linecols = this._getColumnsForLine(this.cursorY);
		if (this.cursorX >= linecols) {
			this.cursorX = linecols - 1;
		}
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' C');
		break;
	case 'd':
		// CVA
		this.cursorY = this._DCEvars[0] - 1;
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' d');
		break;
	case 'D':
		if (this._DCEvars[0] == 0) {
			this.cursorX--;
		} else {
			this.cursorX -= this._DCEvars[0];
		}
		if (this.cursorX < 0) {
			this.cursorX = 0;
		}
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' D');
		break;
	case 'r':
		// DECSTBM
		if (this._DCEvar > 0) {
			// Ray: Any argument is optional
			this.cursorY = this._DCEvars[1] - 1;
			if (this.cursorY < 0) {
				this.cursorY = this.height - 1;
			} else if (this.cursorY >= this.height) {
				this.cursorY = this.height - 1;
			}
		} else {
			this.cursorY = this.height - 1;
		}
		this.setBottomMargin(this.cursorY);
		if (this.cursorY >= this._DCEvars[0]) {
			this.cursorY = this._DCEvars[0] - 1;
			if (this.cursorY < 0) {
				this.cursorY = 0;
			}
		}
		this.setTopMargin(this.cursorY);
		this._setCursor(0, 0);
		TermUtil.log(LVL_DEBUG, 'ESC [' + this._DCEvars[0] + ' ; ' + this._DCEvars[1] + ' r');
		break;
	case 'G': // CHA
	case '`': // HPA
		/*
		 * CUP / cursor absolute column
		 */
		this._checkForWrap();
		this.cursorX = this._DCEvars[0] - 1;
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' ' + c);
		break;
	case 'Z':
		/*
		 * CBT
		 */
		for (var i = 0; this.cursorX > 0 && i < this._DCEvars[0]; i++) {
			if (this.cursorX > 0) {
				this.cursorX--;
			}
			do {
				this.cursorX--;
			} while ((this.cursorX >= 0) && (this.Tabs[this.cursorX] == 0));
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' Z');
		break;
	case 'I':
		/*
		 * CHT
		 */
		this._checkForWrap();
		var times = this._DCEvars[0] == 0 ? 1 : this._DCEvars[0];
		for (var i = 0; this.cursorX < cols && i < times; i++) {
			if (i != 0 || this.cursorX != 0) {
				this._advanceTab();
			}
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' I');
		break;
	case 'H':
		/*
		 * CUP / cursor position
		 */
		/*
		 * gets 2 arguments
		 */
		this._setCursor(this._DCEvars[0] - 1, this._DCEvars[1] - 1);
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ';' + this._DCEvars[1] + ' H, moveoutsidemargins ' + this._moveoutsidemargins);
		TermUtil.log(LVL_DEBUG, '    -> R now ' + this.cursorY + ', C now ' + this.cursorX);
		break;
	case 'f':
		/*
		 * move cursor 2
		 */
		/*
		 * gets 2 arguments
		 */
		this.cursorY = this._DCEvars[0] - 1;
		this.cursorX = this._DCEvars[1] - 1;
		if (this.cursorX < 0) {
			this.cursorX = 0;
		}
		if (this.cursorY < 0) {
			this.cursorY = 0;
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ';' + this._DCEvars[1] + ' f');
		break;
	case 'S':
		/*
		 * ind aka 'scroll forward'
		 */
		if (this._DCEvars[0] == 0) {
			this.insertLine(this.height - 1, SCROLL_UP);
		} else {
			this.insertLine(this.height - 1, this._DCEvars[0], SCROLL_UP);
		}
		break;
	case 'L':
		/*
		 * insert n lines
		 */
		if (this._DCEvars[0] == 0) {
			this.insertLine(this.cursorY, SCROLL_DOWN);
		} else {
			this.insertLine(this.cursorY, this._DCEvars[0], SCROLL_DOWN);
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + '' + c + ' (at R ' + this.cursorY + ')');
		break;
	case 'T':
		/*
		 * 'ri' aka scroll backward
		 */
		if (this._DCEvars[0] == 0) {
			this.insertLine(0, SCROLL_DOWN);
		} else {
			this.insertLine(0, this._DCEvars[0], SCROLL_DOWN);
		}
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + '' + c + ' at 0');
		break;
	case 'M':
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + '' + c + ' at R=' + this.cursorY);
		if (this._DCEvars[0] == 0) {
			this.deleteLine(this.cursorY, this._attributes);
		} else {
			for (var i = 0; i < this._DCEvars[0]; i++) {
				this.deleteLine(this.cursorY, this._attributes);
			}
		}
		break;
	case 'K':

		TermUtil.log(LVL_DEBUG, 'ESC [ '  + this._DCEvars[0] + ' K');
		/*
		 * EL - clear in line
		 */
		switch (this._DCEvars[0]) {
		case 6:
			/*
			 * 97801 uses ESC[6K for delete to end of line
			 */
		case 0:
			/*
			 * clear to right
			 */
			if (this.cursorX < (this.width - 1)) {
				this.deleteArea(this.cursorX, this.cursorY, this.width - this.cursorX, 1,
						this._attributes, true, false, false);
			}
			break;
		case 1:
			/*
			 * clear to the left, including this
			 */
			if (this.cursorX > 0) {
				this.deleteArea(0, this.cursorY, this.cursorX + 1, 1, this._attributes, true, false, false);
			}
			break;
		case 2:
			/*
			 * clear whole line
			 */
			this.deleteArea(0, this.cursorY, this.width, 1, this._attributes, true, false, false);
			break;
		}
		break;
	case 'J':
		/*
		 * ED - clear below current line
		 */
		switch (this._DCEvars[0]) {
		case 0:
			if (this.cursorY < (this.height - 1)) {
				this.deleteArea(0, this.cursorY + 1, this.width, this.height - this.cursorY - 1, this._attributes, true, false, true);
			}
			if (this.cursorX < (this.width - 1)) {
				this.deleteArea(this.cursorX, this.cursorY, this.width - this.cursorX, 1,
						this._attributes, true, false, true);
			}
			break;
		case 1:
			if (this.cursorY > 0) {
				this.deleteArea(0, 0, this.width, this.cursorY, this._attributes, true, false, true);
			}
			if (this.cursorX > 0) {
				this.deleteArea(0, this.cursorY, this.cursorX + 1, 1, this._attributes, true, false, true);
			}
			// include up to and including current
			break;
		case 2:
			this.deleteArea(0, 0, this.width, this.height, this._attributes, true, false, true);
			break;
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' J');
		break;
	case ' ':

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' ' + String.fromCharCode(this._DCEvars[1]) + ' <SPACE>');
		_term_state = ClientTerminalEmulation.TSTATE_CSI_SPACE;
		break;
	case '@':

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' @');
		for (var i = 0; i < Math.max(this._DCEvars[0], 1); i++) {
			this._insertChar(this.cursorX, this.cursorY, ' ', this._attributes);
		}
		break;
	case 'X': {
		var toerase = this._DCEvars[0];
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' X, C=' + this.cursorX + ',R=' + this.cursorY);
		if (toerase == 0) {
			toerase = 1;
		}
		if ((toerase + this.cursorX) > this.width) {
			toerase = this.width - this.cursorX;
		}
		this.deleteArea(this.cursorX, this.cursorY, toerase, 1, this._attributes, true, false, false);
		// does not change cursor position
		break;
	}
	case 'P':
		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' P, C=' + this.cursorX + ',R=' + this.cursorY);
		if (this._DCEvars[0] == 0) {
			this._DCEvars[0] = 1;
		}
		for (var i = 0; i < this._DCEvars[0]; i++) {
			this._deleteChar(this.cursorX, this.cursorY, this._attributes);
		}
		break;
	case 'n':
		switch (this._DCEvars[0]) {
		case 5:
			/*
			 * malfunction? No malfunction.
			 */
			this._writeSpecial(String.fromCharCode(ClientTerminalEmulation.ESC) + '[0n');
			TermUtil.log(LVL_DEBUG, 'ESC[5n');
			break;
		case 6:
			// DO NOT offset R and C by 1! (checked against
			// /usr/X11R6/bin/resize
			// FIXME check again.
			// FIXME: but vttest thinks different???
			// BPS - I'm going with vttest, it makes more sense as
			// all others are offset by 1?

			this._writeSpecial(String.fromCharCode(ClientTerminalEmulation.ESC) + '[' + (this.cursorY + 1) + ';' + (this.cursorX + 1) + 'R');
			TermUtil.log(LVL_DEBUG, 'ESC[6n');
			break;
		default:
			TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' n??');
			break;
		}
		break;
	case 's':
		/*
		 * DECSC - save cursor
		 */
		this._saveCursor();
		TermUtil.log(LVL_DEBUG, 'ESC[s');
		break;
	case 'u':
		/*
		 * DECRC - restore cursor
		 */
		this._restoreCursor();
		TermUtil.log(LVL_DEBUG, 'ESC[u');
		break;
	case 'b':
		/*
		 * REP - Repeat previous character
		 */
		if (this.prevChar != 0) {

			var times = this._DCEvars[0] == 0 ? 1 : this._DCEvars[0];
			for (var i = 0; i < times; i++) {
				this._checkForWrap();
				this.cursorX += this._writeChar(this.cursorX, this.cursorY, this.prevChar, this.prevAttr);
			}
			TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' b');
			this.prevChar = 0;
		}
		break;
	case 'm':
		/*
		 * attributes as color, bold , blink,
		 */
		TermUtil.log(LVL_DEBUG, 'ESC [ ');
		if ((this._DCEvar == 0) && (this._DCEvars[0] == 0)) {
			this._attributes = 0;
		}
		for (var i = 0; i <= this._DCEvar; i++) {
			switch (this._DCEvars[i]) {
			case 0:
				if (this._DCEvar > 0) {
					if (this.termType == ClientTerminalEmulation.SCOANSI) {
						this._attributes &= COLOR;
						/*
						 * Keeps color. Strange but true.
						 */
					} else {
						this._attributes = 0;
					}
				}
				break;
			case 1:
				this._attributes |= BOLD;
				this._attributes &= ~LOW;
				break;
			case 2:
				/*
				 * SCO color hack mode
				 */
				if (this.termType.equals(TerminalEmumlation.SCOANSI) && ((this._DCEvar - i) >= 2)) {
					var ncolor;
					this._attributes &= ~(COLOR | BOLD);
					var ncolor = this._DCEvars[i + 1];
					if ((this.ncolor & 8) == 8) {
						this._attributes |= BOLD;
					}
					ncolor = ((ncolor & 1) << 2) | (ncolor & 2)
							| ((ncolor & 4) >> 2);
					this._attributes |= (((ncolor) + 1) << 4);
					ncolor = this._DCEvars[i + 2];
					ncolor = ((ncolor & 1) << 2) | (ncolor & 2)
							| ((ncolor & 4) >> 2);
					this._attributes |= (((ncolor) + 1) << 8);
					i += 2;
				} else {
					this._attributes |= LOW;
				}
				break;
			case 4:
				this._attributes |= UNDERLINE;
				break;
			case 7:
				this._attributes |= INVERT;
				break;
			case 5:
				/*
				 * blink on
				 */
				break;
			/*
			 * 10 - ANSI X3.64-1979, select primary font, don't display
			 * control chars, don't set bit 8 on output
			 */
			case 10:
				this._gl = 0;
				this._usedcharsets = true;
				break;
			/*
			 * 11 - ANSI X3.64-1979, select second alt. font, display
			 * control chars, set bit 8 on output
			 */
			case 11:
				/*
				 * SMACS , as
				 */
			case 12:
				this._gl = 1;
				this._usedcharsets = true;
				break;
			case 21:
				/*
				 * normal intensity
				 */
				this._attributes &= ~(LOW | BOLD);
				break;
			case 25:
				/*
				 * blinking off
				 */
				break;
			case 27:
				this._attributes &= ~INVERT;
				break;
			case 24:
				this._attributes &= ~UNDERLINE;
				break;
			case 22:
				this._attributes &= ~BOLD;
				break;
			case 30:
			case 31:
			case 32:
			case 33:
			case 34:
			case 35:
			case 36:
			case 37:
				this._attributes &= ~COLOR_FG;
				this._attributes |= (((this._DCEvars[i] - 30) + 1) << 4);
				break;
			case 39:
				this._attributes &= ~COLOR_FG;
				break;
			case 40:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
				this._attributes &= ~COLOR_BG;
				this._attributes |= (((this._DCEvars[i] - 40) + 1) << 8);
				break;
			case 49:
				this._attributes &= ~COLOR_BG;
				break;
			default:
				TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[i] + ' m unknown...');
				break;
			}
			TermUtil.log(LVL_DEBUG, '' + this._DCEvars[i] + ';');
		}

		TermUtil.log(LVL_DEBUG, ' (attributes = ' + this._attributes + ')m \n');
		break;
	// case '\0':
	// term_state = TSTATE_CSI;
	// console.log('ESC [ " + DCEvars[0] + " NUL");
	// break;
	case 'q':
		/*
		 * DECLL - LEDs
		 */
		var keys = TermUtil.mkarray(4, false);
		for (var i = 0; i < this._DCEvars.length; i++) {
			if (this._DCEvars[i] > 0 && this._DCEvars[i] < 5) {
				keys[this._DCEvars[i] - 1] = true;
			}
		}
		this._setLEDs(keys);
		break;
	default:
		TermUtil.log(LVL_WARN, 'ESC [ unknown letter:' + c + ' (' + c + ')');
		break;
	} 
};

ClientTerminalEmulation.prototype._saveCursor = function() {
	this._Sc = this.cursorX;
	this._Sr = this.cursorY;
	this._Sa = this._attributes;
};

ClientTerminalEmulation.prototype._restoreCursor = function() {
	this.cursorX = this._Sc;
	this.cursorY = this._Sr;
	this._attributes = this._Sa;
};

ClientTerminalEmulation.prototype._setLEDs = function(keys) {
};

ClientTerminalEmulation.prototype._handle_csi_ticks = function(c) {
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	switch (String.fromCharCode(c)) {
	case 'p':
		TermUtil.log(LVL_DEBUG, 'Conformance level: ' + this._DCEvars[0] + ' (unsupported),' + this._DCEvars[1]);
		if (this._DCEvars[0] == 61) {
			this._output8bit = false;
			break;
		}
		if (this._DCEvars[1] == 1) {
			this._output8bit = false;
		} else {
			this._output8bit = true;
			/*
			 * 0 or 2
			 */
		}
		break;
	case 'q':
		/*
		 * DECSCA - select character attributes
		 */

		if (this._DCEvars[0] == 1) {
			this._attributes |= ClientTerminalEmulation.PROTECTED;
		} else {
			this._attributes &= ~ClientTerminalEmulation.PROTECTED;
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ' + this._DCEvars[0] + ' " q');
		break;
	default:
		TermUtil.log(LVL_WARN, 'Unknown ESC [...  "' + c);
		break;
	}
}

ClientTerminalEmulation.prototype._handle_osc = function(c, rows, columns, tm, bm) {
	switch (String.fromCharCode(c)) {
	case '0':
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9':
		this._DCEvars[this._DCEvar] = ((this._DCEvars[this._DCEvar] * 10) + c) - 48;
		this._term_state = ClientTerminalEmulation.TSTATE_OSC;
		break;
	case ';':
		this._DCEvar++;
		this._DCEvars[this._DCEvar] = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_OSC;
		break;
	default:
		if ((c < 0x20) && (c != ClientTerminalEmulation.ESC)) {
			// NP - No printing character
			TermUtil.log(LVL_DEBUG, 'OSC: ' + this._DCEvars[0] + ' osc');

			switch (this._DCEvars[0]) {
			case 1:
				/*
				 * Set window title
				 */
				this.setWindowTitle(this._osc);
				break;
			default:
				//
				TermUtil.log(LVL_WARN, 'OSC: ' + this._DCEvars[0] + ' (' + this._osc + ') unsupported');
				break;
			}
			this._DCEvar = 0;
			this._term_state = ClientTerminalEmulation.TSTATE_DATA;
			break;
		}
		// but check for vt102 ESC \
		if ((c == '\\') && (this._osc.charCodeAt(this._osc.length - 1) == ClientTerminalEmulation.ESC)) {
			TermUtil.log(LVL_DEBUG, 'OSC: ' + this._osc);
			this._term_state = ClientTerminalEmulation.TSTATE_DATA;
			this._DCEvar = 0;
			break;
		}
		this._osc = this._osc + String.fromCharCode(c);
	}
	
};

ClientTerminalEmulation.prototype._handle_dcerqpm = function(c) {
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	switch (String.fromCharCode(c)) {
	case 'p':
		var r = '0';
		switch(this._DCEvars[0]) {
		case 1:
			// DECCKM
			r = this._applicationCursorKeys ? '1' : '0';
			break;
		case 2:
			// DECANM
			r = this._vt52mode ? '0' : '1';
			break;
		case 3:
			// DECCOLM
			r = this.width == 132 ? '1' : '0';
			break;
		case 5:
			/*
			 * light background
			 */
			r = this.lightBackground ? '1' : '0';
			break;
		case 6:
			/*
			 * DECOM: move inside margins.
			 */
			r = this._moveoutsidemargins ? '0' : '1';
			break;
		case 7:
			/*
			 * DECAWM: Autowrap Mode
			 */
			r = this._wraparound ? '1' : '0';
			break;
		case 8:
			/*
			 * Auto repeat
			 */
			r = this.display.autoRepeat ? '1' : '0';
			break;
		case 12:
			/* Start Blinking Cursor (att610) */
			r = this.display.isCursorBlink() ? '1' : '0';
			break;
		case 25:
			/*
			 * turn cursor on
			 */
			r = this.showcursor ? '1' : '0';
			break;
		case 40:
			r = this.allow80to132 ? '1' : '0';
			break;
		case 45:
			/*
			 * http://stackoverflow.com/questions/31360385/an-obscure-
			 * one-documented-vt100-soft-wrap-escape-sequence#31360700
			 * XTerm – Frequently Asked Questions (FAQ) =
			 * http://invisible-island.net/xterm/xterm.faq.html#
			 * vt100_wrapping VT100 Termcap Entry (CENG 455) -
			 * http://www.pitt.edu/%7Ejcaretto/text/cleanup/vt100-
			 * termcap.html
			 */
			r = this._reverseWraparound ? '1' : '0';
			break;
		case 47:
			/*
			 * switch to alternate buffer
			 */
			r = this.alternateBuffer ? '1' : '0';
			break;
		case 2004:
			r = this._bracketedPaste ? '1' : '0';
			break;
		case 9:
			/*
			 * X10 mouse
			 */
		case 1000:
			/*
			 * xterm style mouse report on
			 */
		case 1001:
		case 1002:
		case 1003:
			r = '' + this._mouserpt;
			break;	
		case 1007:
			r = this._alternateScrollMode ? '1' : '0';
			break;
		default:
			TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' $ ' + c + ', unsupported.');
			break;
		}
		this.io.write((ESC) + '[?' + this._DCEvars[0] + ';' + r + 'y');
		break;
	default:
		TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' $ ' + c + ', unsupported.');
		break;
	}
};

ClientTerminalEmulation.prototype._handle_dceq = function(c) {
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
	switch (String.fromCharCode(c)) {
	case '0':
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9':
		this._DCEvars[this._DCEvar] = ((this._DCEvars[this._DCEvar] * 10) + (c)) - 48;
		this._term_state = ClientTerminalEmulation.TSTATE_DCEQ;
		break;
	case ';':
		this._DCEvar++;
		this._DCEvars[this._DCEvar] = 0;
		this._term_state = ClientTerminalEmulation.TSTATE_DCEQ;
		break;
	case '$':
		this._term_state = ClientTerminalEmulation.TSTATE_DECRQPM;
		break;
	case 's':
		// XTERM_SAVE missing!
		TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' s unimplemented!');
		break;
	case 'r':
		// XTERM_RESTORE

		TermUtil.log(LVL_DEBUG, 'ESC [ ? ' + this._DCEvars[0] + ' r');
		
		/*
		 * DEC Mode reset
		 */
		for (var i = 0; i <= this._DCEvar; i++) {
			switch (this._DCEvars[i]) {
			case 3:
				/*
				 * 80 columns
				 */
				this.setScreenSize(80, this.height, true);
				this.clearScreen();
				break;
			case 4:
				/*
				 * scrolling mode, smooth
				 */
				break;
			case 5:
				/*
				 * light background
				 */
				this.lightBackground = true;
				TermUtil.log(LVL_INFO, 'Now using light background');
				this.update[0] = true;
				break;
			case 6:
				/*
				 * DECOM (Origin Mode) move inside margins.
				 */
				this._moveoutsidemargins = true;
				break;
			case 7:
				/*
				 * DECAWM: Autowrap Mode
				 */
				this._wraparound = false;
				break;
			case 12:
				/*
				 * local echo off
				 */
				break;
			case 9:
				/*
				 * X10 mouse
				 */
			case 1000:
				/*
				 * xterm style mouse report on
				 */
			case 1001:
			case 1002:
			case 1003:
				this._mouserpt = this._DCEvars[i];
				TermUtil.log(LVL_INFO, 'ESC [ ? ' + this._DCEvars[0] + ' (Xterm Mouse report off )');
				break;
			case 1007:
				this._alternateScrollMode = true;
				break;
			default:
				TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' r, unimplemented!');
			}
		}
		break;
	case 'h':
		// DECSET
		TermUtil.log(LVL_DEBUG, 'ESC [ ? ' + this._DCEvars[0] + ' h');
		/*
		 * DEC Mode set
		 */
		for (var i = 0; i <= this._DCEvar; i++) {
			switch (this._DCEvars[i]) {
			case 1:
				/*
				 * Application cursor keys
				 */
				this._setApplicationCursorKeys(true);
				break;
			case 2:
				/*
				 * DECANM
				 */
				this._vt52mode = false;
				break;
			case 3:
				/*
				 * 132 columns
				 */
				if (this.allow80to132) {
    				this.setScreenSize(132, this.height, true);
    				this.clearScreen();
				}
				break;
			case 6:
				/*
				 * DECOM: move inside margins.
				 */
				this._moveoutsidemargins = false;
				break;
			case 7:
				/*
				 * DECAWM: Autowrap Mode
				 */
				this._wraparound = true;
				break;
			case 8:
				/*
				 * Auto repeat
				 */
				this.display.autoRepeat = true;
				break;
			case 25:
				/*
				 * turn cursor on
				 */
				this.showcursor = true;
				this.update[0] = true;
				break;
			case 9:
				/*
				 * X10 mouse
				 */
			case 1000:
				/*
				 * xterm style mouse report on
				 */
			case 1001:
			case 1002:
			case 1003:
				this._mouserpt = this._DCEvars[i];
				break;
			case 1007:
				this._alternateScrollMode = true;
				break;
			case 5:
				/*
				 * light background
				 */
				this.lightBackground = true;
				TermUtil.log(LVL_INFO, 'Now using light background');
				this.update[0] = true;
				break;
			case 12:
				this.display.setCursorBlink(true);
				break;
			case 47:
			case 1047:
				/*
				 * switch to alternate buffer
				 */
				this.setAlternateBuffer(true);
				break;
			case 1048:
				this._saveCursor();
				break;
			case 1049:
				this._saveCursor();
				this.setAlternateBuffer(true);
				break;
			case 2004:
				this._bracketedPaste = true;
				break;
			/*
			 * unimplemented stuff, fall through
			 */
			/*
			 * 4 - scrolling mode, smooth
			 */
			/*
			 * 18 - DECPFF - Printer Form Feed Mode -> On
			 */
			/*
			 * 19 - DECPEX - Printer Extent Mode -> Screen
			 */
			default:
				TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' h, unsupported.');
				break;
			}
		}
		break;
	case 'i':
		// DEC Printer Control, autoprint, echo screenchars to
		// printer
		// This is different to CSI i!
		// Also: 'Autoprint prints a final display line only
		// when the
		// cursor is moved off the line by an autowrap or LF,
		// FF, or
		// VT (otherwise do not print the line).'
		switch (this._DCEvars[0]) {
		case 1:
			TermUtil.log(LVL_INFO, 'CSI ? 1 i : Print line containing cursor');
			break;
		case 4:
			TermUtil.log(LVL_INFO, 'CSI ? 4 i : Start passthrough printing');
			break;
		case 5:
			TermUtil.log(LVL_INFO, 'CSI ? 4 i : Stop passthrough printing');
			break;
		}
		break;
	case 'l':
		// DECRST
		/*
		 * DEC Mode reset
		 */

		TermUtil.log(LVL_DEBUG, 'ESC [ ? ' + this._DCEvars[0] + ' l');
		for (var i = 0; i <= this._DCEvar; i++) {
			switch (this._DCEvars[i]) {
			case 1:
				/*
				 * Application cursor keys
				 */
				this._setApplicationCursorKeys(false);
				break;
			case 2:
				/*
				 * DECANM
				 */
				this._vt52mode = true;
				break;
			case 3:
				/*
				 * 80 columns
				 */
				this.setScreenSize(80, this.height, true);
				this.clearScreen();
				break;
			case 6:
				/*
				 * DECOM: move outside margins.
				 */
				this._moveoutsidemargins = true;
				break;
			case 7:
				/*
				 * DECAWM: Autowrap Mode OFF
				 */
				this._wraparound = false;
				break;
			case 25:
				/*
				 * turn cursor off
				 */
				this.showcursor = false;
				this.update[0] = true;
				break;
			case 5:
				/*
				 * dark background
				 */
				this.lightBackground = false;
				TermUtil.log(LVL_INFO, 'Now using dark background');
				this.update[0] = true;
				break;
			case 8:
				/*
				 * Auto repeat
				 */
				this.display.autoRepeat = false;
				break;
			case 40:
				this.allow80to132 = false;
				break;
			case 45:
				/*
				 * http://stackoverflow.com/questions/31360385/an-obscure-
				 * one-documented-vt100-soft-wrap-escape-sequence#31360700
				 * XTerm – Frequently Asked Questions (FAQ) =
				 * http://invisible-island.net/xterm/xterm.faq.html#
				 * vt100_wrapping VT100 Termcap Entry (CENG 455) -
				 * http://www.pitt.edu/%7Ejcaretto/text/cleanup/vt100-
				 * termcap.html
				 */
				this._reverseWraparound = true;
				break;
			case 47:
			case 1047:
				/*
				 * switch to alternate buffer
				 */
				this.setAlternateBuffer(false);
				break;
			case 1048:
				this._restoreCursor();
				break;
			case 1049:
				this._restoreCursor();
				this.setAlternateBuffer(false);
				break;
			case 12:
				this.display.setCursorBlink(false);
				break;
			case 2004:
				this._bracketedPaste = false;
				break;
			/*
			 * Unimplemented stuff:
			 */
			/*
			 * 4 - scrolling mode, jump
			 */
			/*
			 * 12 - local echo on
			 */
			/*
			 * 18 - DECPFF - Printer Form Feed Mode -> Off
			 */
			/*
			 * 19 - DECPEX - Printer Extent Mode -> Scrolling Region
			 */
			case 9:
				/*
				 * X10 mouse
				 */
			case 1000:
				/*
				 * xterm style mouse report OFF
				 */
			case 1001:
			case 1002:
			case 1003:
				this._mouserpt = 0;
				TermUtil.log(LVL_INFO, 'ESC [ ? ' + this._DCEvars[0] + ' (Xterm Mouse report off )');
				break;
			case 1007:
				this._alternateScrollMode = false;
				break;
			default:
				TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' l, unsupported.');
				break;
			}
		}
		break;
	case 'n':
		console.log('ESC [ ? ' + this._DCEvars[0] + ' n');
		switch (this._DCEvars[0]) {
		case 15:
			/*
			 * printer? no printer.
			 */
			this.io.write(String.fromCharCode(ClientTerminalEmulation.ESC) + '[?13n');
			TermUtil.log(LVL_DEBUG, 'ESC[5n');
			break;
		default:
			TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' n, unsupported.');
			break;
		}
		break;
	case 'K':
		TermUtil.log(LVL_DEBUG, 'ESC [ ? ' + this._DCEvars[0] + ' K');
		
		/*
		 * DECSEL - clear in line
		 */
		switch (this._DCEvars[0]) {
		case 6:
			/*
			 * 97801 uses ESC[6K for delete to end of line
			 */
		case 0:
			/*
			 * clear to right
			 */
			if (this.cursorX < (this.width - 1)) {
				this.deleteArea(this.cursorX, this.cursorY, this.width - this.cursorX, 1,
						this._attributes, true, true, false);
			}
			break;
		case 1:
			/*
			 * clear to the left, including this
			 */
			if (this.cursorX > 0) {
				this.deleteArea(0, this.cursorY, this.cursorX + 1, 1, this._attributes, true,
						true, false);
			}
			break;
		case 2:
			/*
			 * clear whole line
			 */
			this.deleteArea(0, this.cursorY, this.width, 1, this._attributes, true, true, false);
			break;
		}
		break;
	case 'J':
		/*
		 * DECSED - clear below current line
		 */
		switch (this._DCEvars[0]) {
		case 0:
			if (this.cursorY < (this.height - 1)) {
				this.deleteArea(0, this.cursorY + 1, this.width, this.height - this.cursorY - 1,
						this._attributes, true, true, false);
			}
			if (this.cursorX < (this.width - 1)) {
				this.deleteArea(this.cursorX, this.cursorY, this.width - this.cursorX, 1,
						this._attributes, true, true, false);
			}
			break;
		case 1:
			if (this.cursorY > 0) {
				this.deleteArea(0, 0, this.width, this.cursorY, this._attributes, true, true,
						false);
			}
			if (this.cursorX > 0) {
				this.deleteArea(0, this.cursorY, this.cursorX + 1, 1, this._attributes, true,
						true, false);
			}
			// include up to and including current
			break;
		case 2:
			/*
			 * Entire display
			 */
			this.deleteArea(0, 0, this.width, this.height, this._attributes, true, true, false);
			break;
		}

		TermUtil.log(LVL_DEBUG, 'ESC [ ? ' + this._DCEvars[0] + ' J');
		break;
	default:
		TermUtil.log(LVL_WARN, 'ESC [ ? ' + this._DCEvars[0] + ' ' + c + ', unsupported.');
		break;
	}
};

ClientTerminalEmulation.prototype._handle_dcs = function(c) {
	TermUtil.log(LVL_INFO, 'DCS: ' + dcs);
};

ClientTerminalEmulation.prototype._handle_escsquare = function(c) {
	switch (String.fromCharCode(c)) {
	case '3':
		/*
		 * DECDHL - Double height line (top)
		 */
		this.lineAttributes[this.cursorY + this.screenBase] = DOUBLE_HEIGHT_TOP;
		TermUtil.log(LVL_DEBUG, 'ESC # 3  (DECDHL)');
		break;
	case '4':
		/*
		 * DECDHL - Double height line (bottom)
		 */
		this.lineAttributes[this.cursorY + this.screenBase] = DOUBLE_HEIGHT_BOTTOM;
		TermUtil.log(LVL_DEBUG, 'ESC # 4  (DECDHL)');
		break;
	case '6':
		/*
		 * DECDWL - Double width line
		 */
		this.lineAttributes[this.cursorY + this.screenBase] = DOUBLE_WIDTH;
		TermUtil.log(LVL_DEBUG, 'ESC # 6  (DECDWL)');
		break;
	case '5':
		/*
		 * DECSWL - Single width line
		 */
		this.lineAttributes[this.cursorY + this.screenBase] = 0;
		TermUtil.log(LVL_DEBUG, 'ESC # 5  (DECSWL)');
		break;
	case '8':
		for (var i = 0; i < this.width; i++) {
			for (var j = 0; j < this.height; j++) {
				this._writeChar(i, j, 'E', 0);
			}
		}
		break;
	default:
		TermUtil.log(LVL_WARN, 'ESC # ' + c + ' not supported.');
		break;
	}
	this._term_state = ClientTerminalEmulation.TSTATE_DATA;
};

ClientTerminalEmulation.prototype._getColumnsForLine = function(cursorY) {
	var l = this.cursorY + this.screenBase;
	if(l >= this.lineAttributes.length) {
		TermUtil.log(LVL_WARN, 'Outside of attributes array!! cursorY = ' + this.cursorY + ' screenbase = ' +this.screenBase + ' la: ' + this.lineAttributes.length);
		l = this.lineAttributes.length - 1;
	}
	return this.lineAttributes[l] == 0 ? this.width
			: this.width / 2;
}

ClientTerminalEmulation.prototype._decreaseCursor = function(tm, bm) {
	var limit;
	/*
	 * FIXME: xterm only cares about 0 and topmargin
	 */
	if (this.cursorY > bm) {
		limit = bm + 1;
	} else if (this.cursorY >= tm) {
		limit = tm;
	} else {
		limit = 0;
	}
	if (this._DCEvars[0] == 0) {
		this.cursorY = this.cursorY - 1;
	} else {
		this.cursorY = this.cursorY - this._DCEvars[0];
	}
	if (this.cursorY < limit) {
		TermUtil.log(LVL_DEBUG, 'Limited from ' + this.cursorY + ' to ' + limit);
		this.cursorY = limit;
	} else {
		TermUtil.log(LVL_DEBUG, 'Not limited.');
	}
};  

ClientTerminalEmulation.prototype._increaseCursor = function(tm, bm) {
	var limit;
	if (this.cursorY < tm) {
		limit = tm - 1;
	} else if (this.cursorY <= bm) {
		limit = bm;
	} else {
		limit = this.height - 1;
	}
	if (this._DCEvars[0] == 0) {
		this.cursorY = this.cursorY + 1;
	} else {
		this.cursorY = this.cursorY + this._DCEvars[0];
	}
	if (this.cursorY  > limit) {
		TermUtil.log(LVL_DEBUG, 'Limited from ' + this.cursorY + ' to ' + limit);
		this.cursorY = limit;
	} else {
		TermUtil.log(LVL_DEBUG, 'Not limited.');
	}
}

ClientTerminalEmulation.prototype._setCursor = function(row, col) {
	var maxr = this.height;
	var tm = this.topMargin;
	var R = (row < 0) ? 0 : row;
	var C = (col < 0) ? 0 : col;
	if (!this._moveoutsidemargins) {
		R += tm;
		maxr = this.bottomMargin;
	}
	if (R > maxr) {
		R = maxr;
	}
	this.setCursorPosition(C, R);
}

ClientTerminalEmulation.prototype._checkForWrap = function() {
	var cols = this._getColumnsForLine(this.cursorY);
	if (this.cursorX >= cols) {
		// Do not wrap if the line is double height / width
		if (!this._reverseWraparound && ( this._wraparound && cols == this.width)) {
			if (this.cursorY < (this.height - 1)) {
				this.cursorY = this.cursorY + 1;
			} else {
				this.insertLine(this.cursorY, 1, SCROLL_UP);
			}
			this.cursorX = 0;
		} else if (this._reverseWraparound) {
			// cursor stays on last character.
			this.cursorX = cols - 1;
		}
	}
};

ClientTerminalEmulation.prototype._advanceTab = function() {
	var lineCols = this._getColumnsForLine(this.cursorY);
	do {
		// Don't overwrite or insert! TABS are not
		// destructive, but
		// movement!
		this.cursorX++;
	} while ((this.cursorX < lineCols) && (this.Tabs[this.cursorX] == 0));
	if (this.cursorX == lineCols) {
		this.cursorX--;
	}
};

ClientTerminalEmulation.prototype._writeSpecial = function(s) {
	if (s == null || s == '') {
		return true;
	}
	if (((s.length >= 3) && (s.charCodeAt(0) == 27) && (s.charAt(1) == 'O'))) {
		if (this._vt52mode) {
			if ((s.charAt(2) >= 'P') && (s.charAt(2) <= 'S')) {
				s = '\u001b' + s.substring(2);
				/*
				 * ESC x
				 */
			} else {
				s = '\u001b?' + s.substring(2);
				/*
				 * ESC ? x
				 */
			}
		} else {
			if (this._output8bit) {
				s = '\u008f' + s.substring(2);
				/*
				 * SS3 x
				 */
			}
			/*
			 * else keep string as it is
			 */
		}
	}
	if (((s.length >= 3) && (s.charCodeAt(0) == 27) && (s.charAt(1) == '['))) {
		if (this._output8bit) {
			s = '\u009b' + s.substring(2);
			/*
			 * CSI ...
			 */
		}
		/*
		 * else keep
		 */
	}
	return this.put(s, false);
};

ClientTerminalEmulation.prototype._setApplicationCursorKeys = function(applicationCursorKeys) {
	if (this._applicationCursorKeys == applicationCursorKeys) {
		return;
	}
	this._applicationCursorKeys = applicationCursorKeys;
	TermUtil.log(LVL_INFO, 'application cursor keys now: ' + applicationCursorKeys);
	if (applicationCursorKeys) {
		this.KeyUp[0] = '\u001bOA';
		this.KeyDown[0] = '\u001bOB';
		this.KeyRight[0] = '\u001bOC';
		this.KeyLeft[0] = '\u001bOD';
	} else {
		this.KeyUp[0] = '\u001b[A';
		this.KeyDown[0] = '\u001b[B';
		this.KeyRight[0] = '\u001b[C';
		this.KeyLeft[0] = '\u001b[D';
	}
};

ClientTerminalEmulation.prototype._setDefaultKeyCodes = function() {

	this.PF1 = '\u001bOP';
	this.PF2 = '\u001bOQ';
	this.PF3 = '\u001bOR';
	this.PF4 = '\u001bOS';

	this.Insert = TermUtil.mkarray(4, ' ');
	this.Remove = TermUtil.mkarray(4, ' ');
	this.KeyHome = TermUtil.mkarray(4, ' ');
	this.KeyEnd = TermUtil.mkarray(4, ' ');
	this.NextScn = TermUtil.mkarray(4, ' ');
	this.PrevScn = TermUtil.mkarray(4, ' ');
	this.Escape = TermUtil.mkarray(4, ' ');
	this.TabKey = TermUtil.mkarray(4, ' ');
	this.KeyUp = TermUtil.mkarray(4, ' ');
	this.KeyDown = TermUtil.mkarray(4, ' ');
	this.KeyRight = TermUtil.mkarray(4, ' ');
	this.KeyLeft = TermUtil.mkarray(4, ' ');
	this.BackSpace = TermUtil.mkarray(4, ' ');
	this.Numpad = TermUtil.mk2darray(10, 2, ' ');
	this.FunctionKey = TermUtil.mkarray(13, ' ');
	this.FunctionKeyShift = TermUtil.mkarray(13, ' ');
	this.FunctionKeyAlt = TermUtil.mkarray(13, ' ');
	this.FunctionKeyCtrl = TermUtil.mkarray(13, ' ');
	this.NUMDot = TermUtil.mkarray(4, ' ');
	this.NUMEnter = TermUtil.mkarray(4, ' ');
	this.NUMMinus = TermUtil.mkarray(4, ' ');
	this.NUMPlus = TermUtil.mkarray(4, ' ');
	this.NUMDot = TermUtil.mkarray(4, ' ');
	this.NUMLock = TermUtil.mkarray(4, ' ');
	this.NUMDivide = TermUtil.mkarray(4, ' ');
	this.NUMMultiply = TermUtil.mkarray(4, ' ');

	this.Insert[0] = this.Insert[1] = this.Insert[2] = this.Insert[3] = '';
	this.Remove[0] = this.Remove[1] = this.Remove[2] = this.Remove[3] = '';
	this.KeyHome[0] = this.KeyHome[1] = this.KeyHome[2] = this.KeyHome[3] = '';
	this.KeyEnd[0] = this.KeyEnd[1] = this.KeyEnd[2] = this.KeyEnd[3] = '';
	this.NextScn[0] = this.NextScn[1] = this.NextScn[2] = this.NextScn[3] = '';
	this.PrevScn[0] = this.PrevScn[1] = this.PrevScn[2] = this.PrevScn[3] = '';
	this.Escape[0] = this.Escape[1] = this.Escape[2] = this.Escape[3] = '\u001b';

	this.TabKey[0] = '\u0009';
	this.TabKey[1] = '\u001bOP\u0009';
	this.TabKey[2] = this.TabKey[3] = '';

	this.FunctionKey[0] = '';
	this.FunctionKey[1] = this.PF1;
	this.FunctionKey[2] = this.PF2;
	this.FunctionKey[3] = this.PF3;
	this.FunctionKey[4] = this.PF4;
	for (var i = 0; i < 13; i++) {
		this.FunctionKeyShift[i] = '';
		this.FunctionKeyAlt[i] = '';
		this.FunctionKeyCtrl[i] = '';
	}

	this.KeyUp[0] = '\u001b[A';
	this.KeyDown[0] = '\u001b[B';
	this.KeyRight[0] = '\u001b[C';
	this.KeyLeft[0] = '\u001b[D';

	this.Numpad[0][0] = '\u001bOp';
	this.Numpad[1][0] = '\u001bOq';
	this.Numpad[2][0] = '\u001bOr';
	this.Numpad[3][0] = '\u001bOs';
	this.Numpad[4][0] = '\u001bOt';
	this.Numpad[5][0] = '\u001bOu';
	this.Numpad[6][0] = '\u001bOv';
	this.Numpad[7][0] = '\u001bOw';
	this.Numpad[8][0] = '\u001bOx';
	this.Numpad[9][0] = '\u001bOy';

	this.NUMDot[0] = '\u001bOn';
	this.NUMEnter[0] = '\u001bOM';
	this.NUMMinus[0] = '\u001bOm';
	this.NUMPlus[0] = '\u001bOl';
	this.NUMLock[0] = '';
	this.NUMDivide[0] = '/';
	this.NUMMultiply[0] = '*';
	
	this.BackSpace[0] = '\b';
	this.BackSpace[1] = '\u007f';
	this.BackSpace[2] = '\b';
	this.BackSpace[3] = '\u007f';
};

ClientTerminalEmulation.EOL_DEFAULT = 0;
ClientTerminalEmulation.EOL_CR_LF = 1;
ClientTerminalEmulation.EOL_CR = 2;
ClientTerminalEmulation.EOL_LF_CR = 3;
ClientTerminalEmulation.DEFAULT_REMOTE_CHARSET = 'ISO-8859-1';
ClientTerminalEmulation.VT320 = 'vt320';
ClientTerminalEmulation.VT220 = 'vt220';
ClientTerminalEmulation.VT100 = 'vt100';
ClientTerminalEmulation.ANSI = 'ansi';
ClientTerminalEmulation.SCOANSI = 'scoansi';
ClientTerminalEmulation.XTERM = 'xterm';
ClientTerminalEmulation.AT386 = 'at386';
ClientTerminalEmulation.SUPPORTED_EMULATIONS = [
                                          ClientTerminalEmulation.ANSI,
                                          ClientTerminalEmulation.AT386,
                                          ClientTerminalEmulation.VT100,
                                          ClientTerminalEmulation.VT220,
                                          ClientTerminalEmulation.VT320,
                                          ClientTerminalEmulation.SCOANSI,
                                          ClientTerminalEmulation.XTERM
                                          ];
ClientTerminalEmulation.ESC = 27;
ClientTerminalEmulation.IND = 132;
ClientTerminalEmulation.NEL = 133;
ClientTerminalEmulation.RI = 141;
ClientTerminalEmulation.SS2 = 142;
ClientTerminalEmulation.SS3 = 143;
ClientTerminalEmulation.DCS = 144;
ClientTerminalEmulation.HTS = 136;
ClientTerminalEmulation.CSI = 155;
ClientTerminalEmulation.OSC = 157;

ClientTerminalEmulation.scoansi_acs = "Tm7k3x4u?kZl@mYjEnB\u2566DqCtAvM\u2550:\u2551N\u2557I\u2554;\u2557H\u255a0a<\u255d";
ClientTerminalEmulation.delay = 0;

ClientTerminalEmulation.DEFAULT_GL = 0;
ClientTerminalEmulation.DEFAULT_GR = 1;

ClientTerminalEmulation.DEFAULT_G0 = 'B';
ClientTerminalEmulation.DEFAULT_G1 = '0';
ClientTerminalEmulation.DEFAULT_G2 = '<';
ClientTerminalEmulation.DEFAULT_G3 = 'A';

ClientTerminalEmulation.TSTATE_DATA = 0;
ClientTerminalEmulation.TSTATE_ESC = 1;
ClientTerminalEmulation.TSTATE_CSI = 2;
ClientTerminalEmulation.TSTATE_DCS = 3;
ClientTerminalEmulation.TSTATE_DCEQ = 4;
ClientTerminalEmulation.TSTATE_ESCSQUARE = 5;
ClientTerminalEmulation.TSTATE_OSC = 6;
ClientTerminalEmulation.TSTATE_SETG0 = 7;
ClientTerminalEmulation.TSTATE_SETG1 = 8;
ClientTerminalEmulation.TSTATE_SETG2 = 9;
ClientTerminalEmulation.TSTATE_SETG3 = 10;
ClientTerminalEmulation.TSTATE_CSI_DOLLAR = 11;
ClientTerminalEmulation.TSTATE_CSI_EX = 12;
ClientTerminalEmulation.TSTATE_ESCSPACE = 13;
ClientTerminalEmulation.TSTATE_VT52X = 14;
ClientTerminalEmulation.TSTATE_VT52Y = 15;
ClientTerminalEmulation.TSTATE_CSI_TICKS = 16;
ClientTerminalEmulation.TSTATE_CSI_SPACE = 17;
ClientTerminalEmulation.TSTATE_DCERQPM = 18;

// 

