import BattlegroundWinCoinBaseAnimation from './BattlegroundWinCoinBaseAnimation';

const COINS_CONFIG = [
	{
		posStart: {x: 73 , y: -265.5 }, posEnd: {x: 73, y: 265.5},
		scaleStart: 0.4, scaleEnd: 0.4,
		rotationStart: 59, rotationEnd: 181,
		duration: 30, delay: 0
	},
	{
		posStart: {x: -10 , y: -265.5 }, posEnd: {x: -10, y: 265.5},
		scaleStart: 0.4, scaleEnd: 0.4,
		rotationStart: 33, rotationEnd: 194,
		duration: 30, delay: 6
	},
	{
		posStart: {x: 73 , y: -265.5 }, posEnd: {x: 73, y: 265.5},
		scaleStart: 0.2, scaleEnd: 0.2,
		rotationStart: 33, rotationEnd: -102,
		duration: 30, delay: 11
	},
	{
		posStart: {x: 16 , y: -265.5 }, posEnd: {x: 16, y: 265.5},
		scaleStart: 0.3, scaleEnd: 0.3,
		rotationStart: 65, rotationEnd: 208,
		duration: 30, delay: 13
	},
	{
		posStart: {x: -280 , y: -265.5 }, posEnd: {x: -280, y: 265.5},
		scaleStart: 0.35, scaleEnd: 0.35,
		rotationStart: 13, rotationEnd: 120,
		duration: 30, delay: 14
	},
	{
		posStart: {x: 223 , y: -265.5 }, posEnd: {x: 223, y: 265.5},
		scaleStart: 0.3, scaleEnd: 0.3,
		rotationStart: -32, rotationEnd: -104,
		duration: 30, delay: 16
	},
	{
		posStart: {x: 283 , y: -265.5 }, posEnd: {x: 283, y: 265.5},
		scaleStart: 0.35, scaleEnd: 0.35,
		rotationStart: 68, rotationEnd: 148,
		duration: 30, delay: 20
	},
	{
		posStart: {x: 197 , y: -265.5 }, posEnd: {x: 197, y: 265.5},
		scaleStart: 0.4, scaleEnd: 0.4,
		rotationStart: 6, rotationEnd: -105,
		duration: 30, delay: 27
	},	
	{
		posStart: {x: -283 , y: -265.5 }, posEnd: {x: -283, y: 265.5},
		scaleStart: 0.2, scaleEnd: 0.2,
		rotationStart: 64, rotationEnd: 119,
		duration: 30, delay: 33
	},	
	{
		posStart: {x: -26 , y: -265.5 }, posEnd: {x: -26, y: 265.5},
		scaleStart: 0.4, scaleEnd: 0.4,
		rotationStart: 84, rotationEnd: -66,
		duration: 30, delay: 44
	},	
	{
		posStart: {x: -300 , y: -265.5 }, posEnd: {x: -300, y: 265.5},
		scaleStart: 0.4, scaleEnd: 0.4,
		rotationStart: -29, rotationEnd: 111,
		duration: 30, delay: 50
	},	
	{
		posStart: {x: 89 , y: -265.5 }, posEnd: {x: 89, y: 265.5},
		scaleStart: 0.4, scaleEnd: 0.4,
		rotationStart: 12, rotationEnd: 146,
		duration: 30, delay: 56
	},
	{
		posStart: {x: 288 , y: -265.5 }, posEnd: {x: 288, y: 265.5},
		scaleStart: 0.2, scaleEnd: 0.2,
		rotationStart: 47, rotationEnd: 249,
		duration: 30, delay: 66
	}
];

class BattlegroundCoinRainAnimation extends BattlegroundWinCoinBaseAnimation
{
	constructor()
	{
		super(COINS_CONFIG);
	}
}

export default BattlegroundCoinRainAnimation;