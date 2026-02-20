export const SCENE_WIDTH = 960;
export const SCENE_HEIGHT = 540;

export const BOMB_RADIUS = 500;

export const ENEMY_DIRECTION = {
	LEFT_DOWN:	'dir0',
	RIGHT_DOWN:	'dir90',
	RIGHT_UP:	'dir180',
	LEFT_UP:	'dir270',
	DOWN:		'dir45',
	RIGHT:		'dir135',
	UP:			'dir225',
	LEFT:		'dir315'
};

export const WIN_FIELD_DESTINATION_POSITION = new PIXI.Point(300, 540);

let lWhiteFilter_cmf = new PIXI.filters.ColorMatrixFilter();
lWhiteFilter_cmf.matrix = [0,0,0,0,255, 0,0,0,0,255, 0,0,0,0,255, 0,0,0,1,0];

export const WHITE_FILTER = lWhiteFilter_cmf;

let lGreyScaleFilter_cmf = new PIXI.filters.ColorMatrixFilter();
lGreyScaleFilter_cmf.matrix = [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
lGreyScaleFilter_cmf.resolution = 6;
lGreyScaleFilter_cmf.greyscale(0.3, false);

export const GREYSCALE_FILTER = lGreyScaleFilter_cmf;

export const DEBUG_WEB_SOCKET_URL = 'ws://games-mp-gp3.discreetgaming.com/websocket/mpgame';
// export const DEBUG_WEB_SOCKET_URL = 'ws://games-mp-beta.discreetgaming.com/websocket/mpgame';
export const MIN_SHOT_Y = 16;

export const MAX_SHOT_Y = 540;

export const ROUND_FINISH_SOON_DURATION = 10000;