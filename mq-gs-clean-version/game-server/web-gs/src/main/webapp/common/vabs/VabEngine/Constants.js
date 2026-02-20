var MOUSE_LEFT   = 1;
var MOUSE_MIDDLE = 2;
var MOUSE_RIGHT  = 3;

var KEY_UP    = 38;
var KEY_LEFT  = 37;
var KEY_RIGHT = 39;
var KEY_DOWN  = 40;

var KEY_A = 65;     var KEY_I = 73;     var KEY_Q = 81;     var KEY_Y = 89;
var KEY_B = 66;     var KEY_J = 74;     var KEY_R = 82;     var KEY_Z = 90;
var KEY_C = 67;     var KEY_K = 75;     var KEY_S = 83;
var KEY_D = 68;     var KEY_L = 76;     var KEY_T = 84;
var KEY_E = 69;     var KEY_M = 77;     var KEY_U = 85;
var KEY_F = 70;     var KEY_N = 78;     var KEY_V = 86;
var KEY_G = 71;     var KEY_O = 79;     var KEY_W = 87;
var KEY_H = 72;     var KEY_P = 80;     var KEY_X = 88;

var KEY_ALT      = 18;  var KEY_SHIFT    = 16;  var KEY_CONTROL  = 17;
var KEY_CAPSLOCK = 20;  var KEY_SPACE    = 32;  var KEY_ENTER    = 13;
var KEY_ESCAPE   = 27;

var KEY_0 = 48, KEY_1 = 49, KEY_2 = 50, KEY_3 = 51, KEY_4 = 52;
var KEY_5 = 53, KEY_6 = 54, KEY_7 = 55, KEY_8 = 56, KEY_9 = 57;


var FONT_STYLE_NORMAL = "";
var FONT_STYLE_BOLD = "bold";
var FONT_STYLE_ITALIC = "italic";
var FONT_STYLE_BOLD_ITALIC = "italic bold";

var FONT_NAME_ARIAL           = "Arial";
var FONT_NAME_CALIBRI         = "Calibri";
var FONT_NAME_COURIER         = "Courier";
var FONT_NAME_COURIER_NEW     = "CourierNew";
var FONT_NAME_SANS_SERIF      = "sans-serif";
var FONT_NAME_GEORGIA         = 'Georgia';
var FONT_NAME_TIMES_NEW_ROMAN = 'Times New Roman';


var COLOR_BLACK      = 'rgba(  0,   0,   0,  1.0)';
var COLOR_RED        = 'rgba(255,   0,   0,  1.0)';
var COLOR_MARRON     = 'rgba(  128, 0,   0,  1.0)';
var COLOR_GREEN      = 'rgba(  0, 255,   0,  1.0)';
var COLOR_DARK_GREEN = 'rgba(  0, 128,   0,  1.0)';
var COLOR_CYAN       = 'rgba(  0, 255, 255,  1.0)';
var COLOR_BLUE       = 'rgba(  0,   0, 255,  1.0)';
var COLOR_DARK_BLUE  = 'rgba(  0,   0, 128,  1.0)';
var COLOR_YELLOW     = 'rgba(255, 255,   0,  1.0)';
var COLOR_AZURE      = 'rgba(  0, 255, 255,  1.0)';
var COLOR_PURPLE     = 'rgba(128,   0, 128,  1.0)';
var COLOR_PINK       = 'rgba(255,   0, 255,  1.0)';
var COLOR_ORANGE     = 'rgba(255, 128,  64,  1.0)';
var COLOR_WHITE      = 'rgba(255, 255, 255,  1.0)';
var COLOR_SILVER     = 'rgba(192, 192, 192,  1.0)';
var COLOR_GOLD       = 'rgba(255, 215, 0,  1.0)';
var COLOR_PLATINUM   = 'rgba(229, 228, 226,  1.0)';
var COLOR_NULL       = 'null';
var COLOR_RANDOM     = 'random';

var COLOR_NIGHT     = 'rgba(12, 9, 10,  1.0)';
var COLOR_INDIGO    = 'rgba(75, 0, 130,  1.0)';


var COLOR_GRAY_10   = 'rgba(229, 229, 229,  1.0)';
var COLOR_GRAY_20   = 'rgba(204, 204, 204,  1.0)';
var COLOR_GRAY_30   = 'rgba(178, 178, 178,  1.0)';
var COLOR_GRAY_40   = 'rgba(153, 153, 153,  1.0)';
var COLOR_GRAY_50   = 'rgba(127, 127, 127,  1.0)';
var COLOR_GRAY_60   = 'rgba(102, 102, 102,  1.0)';
var COLOR_GRAY_70   = 'rgba( 76,  76,  76,  1.0)';
var COLOR_GRAY_80   = 'rgba( 51,  51,  51,  1.0)';
var COLOR_GRAY_90   = 'rgba( 25,  25,  25,  1.0)';


// States
var STATE_ROUND_COMPLETE_ID    = 1;
var STATE_GAME_START_ID        = 2;
var STATE_GAME_END_ID          = 3;
var STATE_GAME_NOT_FINISHED_ID = 4;
var STATE_GAME_CONTINUATION_ID = 5;
var STATE_HOLD_CARDS_ID        = 12;
var STATE_PLACE_BET_ID         = 20;
var STATE_HIT_ID               = 21;
var STATE_STAND_ID             = 22;
var STATE_DOUBLE_ID            = 23;
var STATE_SPLIT_ID             = 24;
var STATE_INSURANCE_ID         = 25;
var STATE_SURRENDER_ID         = 39;
var STATE_FIRST_STEP_ID        = 40;
var STATE_SECOND_STEP_ID       = 41;
var STATE_DOUBLE_UP_ID         = 43;
var STATE_BONUS_ROUND_ID       = 45;
var STATE_BURN_ID              = 46;
var STATE_SWAP_ROUND_ID        = 47;
var STATE_DATA_INCORRECT_ID    = -99;

var COLOR_ARRAY_5 =
[
    setColor( 255,     0,    0, 1),
    setColor(   0,   255,    0, 1),
    setColor(   0,    0,   255, 1),
    setColor( 255,     0,  255, 1),
    setColor(   0,   255,  255, 1)
];

var COLOR_ARRAY_10 =
[
    setColor( 255,     0,    0, 1),
    setColor(   0,   255,    0, 1),
    setColor(   0,    0,   255, 1),
    setColor( 255,     0,  255, 1),
    setColor(   0,   255,  255, 1),

    setColor( 128,    0,     0, 1),
    setColor(   0,   128,    0, 1),
    setColor(   0,    0,   128, 1),
    setColor( 128,   128,    0, 1),
    setColor( 128,    0,   128, 1),
    setColor(   0,   128,  128, 1)
];


var COLOR_ARRAY_20 =
[
    setColor( 255,     0,    0, 1),
    setColor(   0,   255,    0, 1),
    setColor(   0,    0,   255, 1),
    setColor( 255,   255,    0, 1),
    setColor( 255,     0,  255, 1),
    setColor(   0,   255,  255, 1),

    setColor( 128,    0,     0, 1),
    setColor(   0,   128,    0, 1),
    setColor(   0,    0,   128, 1),
    setColor( 128,   128,    0, 1),
    setColor( 128,    0,   128, 1),
    setColor(   0,   128,  128, 1),

    setColor(   0,   128,  255, 1),
    setColor(   0,   255,  128, 1),
    setColor( 128,    0,   255, 1),
    setColor( 128,   128,  255, 1),
    setColor( 128,   255,    0, 1),

    setColor(   0,    0,     0, 1),
    setColor( 128,   128,  128, 1),
    setColor( 255,   255,  255, 1)
];

var COLOR_ARRAY_27 =
[
    setColor( 255,     0,    0, 1),
    setColor(   0,   255,    0, 1),
    setColor(   0,    0,   255, 1),
    setColor( 255,   255,    0, 1),
    setColor( 255,     0,  255, 1),
    setColor(   0,   255,  255, 1),

    setColor(   0,    0,     0, 1),
    setColor( 128,   128,  128, 1),
    setColor( 255,   255,  255, 1),

    setColor( 128,    0,     0, 1),
    setColor(   0,   128,    0, 1),
    setColor(   0,    0,   128, 1),
    setColor( 128,   128,    0, 1),
    setColor( 128,    0,   128, 1),
    setColor(   0,   128,  128, 1),

    setColor(   0,   128,  255, 1),
    setColor(   0,   255,  128, 1),
    setColor( 128,    0,   255, 1),
    setColor( 128,   128,  255, 1),
    setColor( 128,   255,    0, 1),
    setColor( 128,   255,  128, 1),
    setColor( 128,   255,  255, 1),
    setColor( 255,     0,  128, 1),
    setColor( 255,   128,    0, 1),
    setColor( 255,   128,  128, 1),
    setColor( 255,   128,  255, 1),
    setColor( 255,   255,  128, 1)
];

var COLOR_ARRAY_30 =
[
    setColor( 255,     0,    0, 1),
    setColor(   0,   255,    0, 1),
    setColor(   0,    0,   255, 1),
    setColor( 255,   255,    0, 1),
    setColor( 255,     0,  255, 1),
    setColor(   0,   255,  255, 1),

    setColor(   0,    0,     0, 1),
    setColor( 128,   128,  128, 1),
    setColor( 255,   255,  255, 1),

    setColor( 128,    0,     0, 1),
    setColor(   0,   128,    0, 1),
    setColor(   0,    0,   128, 1),
    setColor( 128,   128,    0, 1),
    setColor( 128,    0,   128, 1),
    setColor(   0,   128,  128, 1),

    setColor(   0,   128,  255, 1),
    setColor(   0,   255,  128, 1),
    setColor( 128,    0,   255, 1),
    setColor( 128,   128,  255, 1),
    setColor( 128,   255,    0, 1),
    setColor( 128,   255,  128, 1),
    setColor( 128,   255,  255, 1),
    setColor( 255,     0,  128, 1),
    setColor( 255,   128,    0, 1),
    setColor( 255,   128,  128, 1),
    setColor( 255,   128,  255, 1),
    setColor( 255,   255,  128, 1),

    setColor(  64,     0,    0, 1),
    setColor(   0,    64,    0, 1),
    setColor(   0,     0,   64, 1)
];


var COLOR_ARRAY_ALL =
[
    setColor(   0,    0,     0, 1),
    setColor(   0,    0,    64, 1),
    setColor(   0,    0,   128, 1),
    setColor(   0,    0,   255, 1),
    setColor(   0,    64,    0, 1),
    setColor(   0,    64,   64, 1),
    setColor(   0,    64,  128, 1),
    setColor(   0,    64,  255, 1),
    setColor(   0,   128,    0, 1),
    setColor(   0,   128,   64, 1),
    setColor(   0,   128,  128, 1),
    setColor(   0,   128,  255, 1),
    setColor(   0,   255,    0, 1),
    setColor(   0,   255,   64, 1),
    setColor(   0,   255,  128, 1),
    setColor(   0,   255,  255, 1),

    setColor(  64,    0,    0, 1),
    setColor(  64,    0,   64, 1),
    setColor(  64,    0,  128, 1),
    setColor(  64,    0,  255, 1),
    setColor(  64,    64,    0, 1),
    setColor(  64,    64,   64, 1),
    setColor(  64,    64,  128, 1),
    setColor(  64,    64,  255, 1),
    setColor(  64,   128,    0, 1),
    setColor(  64,   128,   64, 1),
    setColor(  64,   128,  128, 1),
    setColor(  64,   128,  255, 1),
    setColor(  64,   255,    0, 1),
    setColor(  64,   255,   64, 1),
    setColor(  64,   255,  128, 1),
    setColor(  64,   255,  255, 1),

    setColor( 128,    0,    0, 1),
    setColor( 128,    0,   64, 1),
    setColor( 128,    0,  128, 1),
    setColor( 128,    0,  255, 1),
    setColor( 128,    64,    0, 1),
    setColor( 128,    64,   64, 1),
    setColor( 128,    64,  128, 1),
    setColor( 128,    64,  255, 1),
    setColor( 128,   128,    0, 1),
    setColor( 128,   128,   64, 1),
    setColor( 128,   128,  128, 1),
    setColor( 128,   128,  255, 1),
    setColor( 128,   255,    0, 1),
    setColor( 128,   255,   64, 1),
    setColor( 128,   255,  128, 1),
    setColor( 128,   255,  255, 1),

    setColor( 255,     0,    0, 1),
    setColor( 255,     0,   64, 1),
    setColor( 255,     0,  128, 1),
    setColor( 255,     0,  255, 1),
    setColor( 255,    64,    0, 1),
    setColor( 255,    64,   64, 1),
    setColor( 255,    64,  128, 1),
    setColor( 255,    64,  255, 1),
    setColor( 255,   128,    0, 1),
    setColor( 255,   128,   64, 1),
    setColor( 255,   128,  128, 1),
    setColor( 255,   128,  255, 1),
    setColor( 255,   255,    0, 1),
    setColor( 255,   255,   64, 1),
    setColor( 255,   255,  128, 1),
    setColor( 255,   255,  255, 1)
];