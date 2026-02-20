import BossDeathFxAnimation from './BossDeathFxAnimation';
import TorchFxAnimation from '../../../../../main/animation/TorchFxAnimation';

const BOSS_CRACKS_PARAMS = [
	{ //crack #1
		offset:	{x:35, y:0},
		times:	[4, 4, 7, 12],
		parts:	[
					[{x:-23, y: 41}, {x:-27, y: 28}, {x:-16, y: 16}, {x:-13, y: 16}, {x:-23, y: 29}, {x:-23, y: 41}],
					[{x:- 4, y: 37}, {x:- 9, y: 20}, {x:- 8, y: 16}, {x:- 6, y: 16}, {x:- 7, y: 20}, {x:- 4, y: 37}],
					[{x:-16, y: 16}, {x:-14, y: 14}, {x:-15, y:  4}, {x:- 4, y:- 8}, {x:- 2, y:- 8}, {x:- 1, y:- 3}, {x:- 6, y: 16}, {x:- 8, y: 16}, {x:- 4, y:- 3}, {x:-12, y:  6}, {x:-13, y: 16}],
					[{x:- 4, y:- 8}, {x:- 9, y:-26}, {x:-16, y:-35}, {x:- 9, y:-29}, {x:  0, y:-47}, {x:- 6, y:-26}, {x:- 2,y:- 8}]
				],
		limits:	[{x:-27, y:28}, {x:0, y:-47}, {x:-23, y:41}],
		weaks:	[{x:-4, y:-16}, {x:1, y:7}, {x:-10, y:17}, {x:-4, y:31}, {x:-21, y:40}],
		color: 0xEB9487,
		blendMode: PIXI.BLEND_MODES.ADD
	},
	{ //crack #2
		offset:	{x:16, y:0},
		times:	[10, 16, 17, 21, 21],
		parts:	[
					[{x: 52, y:-18}, {x: 26, y:- 3}, {x: 18, y:- 3}, {x: 18, y:  0}, {x: 27, y:  0}, {x: 40, y:  6}, {x: 32, y:- 1}, {x: 52, y:-18}],
					[{x: 18, y:- 3}, {x:- 2, y:- 3}, {x:  0, y:  5}, {x:  2, y:  0}, {x: 18, y:  0}],
					[{x:- 2, y:- 3}, {x:-28, y: 13}, {x:-28, y: 16}, {x:- 3, y:  2}, {x:-14, y: 17}, {x:-24, y: 19}, {x:-28, y: 25}, {x:-28, y: 30}, {x:-22, y: 21}, {x:- 9, y: 19}, {x:  0, y:  5}],
					[{x:-28, y: 13}, {x:-31, y: 15}, {x:-58, y: 16}, {x:-31, y: 18}, {x:-28, y: 16}],
					[{x:-28, y: 25}, {x:-39, y: 40}, {x:-53, y: 41}, {x:-36, y: 43}, {x:-28, y: 30}]
				],
		limits:	[{x:-58, y:16}, {x:52, y:-18}, {x:-36, y:43}],
		weaks:	[{x:34, y:-5}, {x:29, y:0}, {x:0, y:0}, {x:-10, y:-17}, {x:-31, y:16}, {x:-35, y:39}],
		color: 0xEB9487,
		blendMode: PIXI.BLEND_MODES.ADD
	},
	{ //crack #3
		offset:	{x:0, y:0},
		times:	[18, 21],
		parts:	[
					[{x:-22, y:-47}, {x:-25, y:-34}, {x:-12, y:-20}, {x:-13, y:-10}, {x:- 3, y:  0}, {x:  1, y:- 2}, {x:- 5, y:-25}, {x:- 3, y:-43}, {x:- 8, y:-25}, {x:- 3, y:- 3}, {x:-11, y:-13}, {x:-11, y:-22}, {x:-22, y:-36}, {x:-22, y:-47}],
					[{x:- 3, y:  0}, {x:- 7, y: 20}, {x:-14, y: 27}, {x:- 8, y: 24}, {x:  0, y: 41}, {x:- 4, y: 20}, {x:  1, y:- 2}]
				],
		limits:	[{x:-25, y:-34}, {x:-22, y:-47}, {x:1, y:-2}, {x:0, y:41}],
		weaks:	[{x:-2, y:-2}, {x:-12, y:-11}, {x:-6, y:22}, {x:-23, y:-35}],
		color: 0xffffff,
		blendMode: PIXI.BLEND_MODES.NORMAL
	},
	{ //crack #4
		offset:	{x:60, y:55},
		times:	[19, 25],
		parts:	[
					[{x:-22, y:-47}, {x:-25, y:-34}, {x:-12, y:-20}, {x:-13, y:-10}, {x:- 3, y:  0}, {x:  1, y:- 2}, {x:- 5, y:-25}, {x:- 3, y:-43}, {x:- 8, y:-25}, {x:- 3, y:- 3}, {x:-11, y:-13}, {x:-11, y:-22}, {x:-22, y:-36}, {x:-22, y:-47}],
					[{x:- 3, y:  0}, {x:- 7, y: 20}, {x:-14, y: 27}, {x:- 8, y: 24}, {x:  0, y: 41}, {x:- 4, y: 20}, {x:  1, y:- 2}]
				],
		limits:	[{x:-25, y:-34}, {x:-22, y:-47}, {x:1, y:-2}, {x:0, y:41}],
		weaks:	[{x:-2, y:-2}, {x:-12, y:-11}, {x:-6, y:22}, {x:-23, y:-35}],
		color: 0xEB9487,
		blendMode: PIXI.BLEND_MODES.ADD
	},
	{ //crack #5
		offset:	{x:97, y:65},
		times:	[26, 26, 32],
		parts:	[
					[{x:-20, y:-46}, {x:-24, y:-33}, {x:-11, y:-19}, {x:-13, y:-9 }, {x:-10, y:-12}, {x:-10, y:-20}, {x:-21, y:-34}, {x:-20, y:-46}],
					[{x:- 2, y:-42}, {x:- 7, y:-24}, {x:- 4, y:-12}, {x:- 1, y:-12}, {x:- 5, y:-25}, {x:- 2, y:-42}],
					[{x:-13, y:- 9}, {x:- 2, y:  2}, {x:- 7, y: 20}, {x:-14, y: 29}, {x:- 7, y: 24}, {x:  1, y: 42}, {x:- 4, y: 21}, {x:  2, y:  0}, {x:- 1, y:-12}, {x:- 4, y:-12}, {x:- 2, y:- 2}, {x:-10, y:-12}]
				],
		limits:	[{x:-24, y:-33}, {x:-20, y:-46}, {x:2, y:0}, {x:1, y:42}],
		weaks:	[{x:0, y:0}, {x:-11, y:-10}, {x:-5, y:23}, {x:-23, y:-33}],
		color: 0xEB9487,
		blendMode: PIXI.BLEND_MODES.ADD
	},
	{ //crack #6
		offset:	{x:-70, y:0},
		times:	[33, 36],
		parts:	[
					[{x: 37, y:-46}, {x: 16, y:-39}, {x: 14, y:-36}, {x: 14, y:-16}, {x: 28, y:- 8}, {x: 25, y: 15}, {x: 12, y: 27}, {x: 26, y: 18}, {x: 27, y: 34}, {x: 30, y:-12}, {x: 15, y:-18}, {x: 17, y:-37}, {x: 37, y:-46}],
					[{x: 16, y:-39}, {x:  4, y:-39}, {x:- 8, y:-26}, {x:- 5, y:  1}, {x:-18, y: 10}, {x:-21, y: 21}, {x:-16, y: 12}, {x:- 4, y:  4}, {x:- 3, y: 21}, {x:- 1, y:  1}, {x:- 7, y:-25}, {x:  3, y:-37}, {x: 14, y:-36}]
				],
		limits:	[{x:-21, y:21}, {x:37, y:-46}, {x:27, y:34}],
		weaks:	[{x:-4, y:1}, {x:27, y:-10}, {x:-7, y:-23}, {x:26, y:17}, {x:15, y:-38}],
		color: 0xEB9487,
		blendMode: PIXI.BLEND_MODES.ADD
	},
	{ //crack #7
		offset:	{x:70, y:-5},
		times:	[37, 41, 45],
		parts:	[
					[{x: 40, y: 48}, {x: 22, y: 42}, {x:  7, y: 41}, {x:- 5, y: 28}, {x:- 4, y: 10}, {x:  0, y: 10}, {x:- 3, y: 27}, {x:  7, y: 39}, {x: 18, y: 38}, {x: 17, y: 18}, {x: 31, y: 10}, {x: 34, y: 14}, {x: 19, y: 20}, {x: 21, y: 38}, {x: 40, y: 48}],
					[{x:- 4, y: 10}, {x:- 2, y:  0}, {x:-14, y:- 9}, {x:-17, y:-19}, {x:-12, y:-10}, {x:- 1, y:- 2}, {x:  1, y:-19}, {x:  3, y:  1}, {x:  0, y: 10}],
					[{x: 31, y: 10}, {x: 28, y:-14}, {x: 15, y:-26}, {x: 29, y:-17}, {x: 31, y:-33}, {x: 34, y: 14}]
				],
		limits:	[{x:-17, y:-19}, {x:31, y:-33}, {x:40, y:48}],
		weaks:	[{x:1, y:0}, {x:31, y:-14}, {x:28, y:14}, {x:15, y:40}],
		color: 0xEB9487,
		blendMode: PIXI.BLEND_MODES.ADD
	}
];

const TORCH_FXS_PARAMS = [
	{
		appearingTime: 34,
		positionDelta: {x: 0, y: 0}
	},
	{
		appearingTime: 37,
		positionDelta: {x: -50, y: 25}
	},
	{
		appearingTime: 40,
		positionDelta: {x: -65, y: -8}
	},
	{
		appearingTime: 40,
		positionDelta: {x: 45, y: -6}
	},
	{
		appearingTime: 42,
		positionDelta: {x: 85, y: -35}
	}
]

const CRACKS_ANIMATION_TICKS_AMOUNT = 45;

class SpiderBossDeathFxAnimation extends BossDeathFxAnimation
{
	//override
	get cracksParams()
	{
		return BOSS_CRACKS_PARAMS;
	}

	//override
	get _torchFXsParams()
	{
		return TORCH_FXS_PARAMS;
	}

	//override
	get _cracksAnimationTicksAmount()
	{
		return CRACKS_ANIMATION_TICKS_AMOUNT;
	}
}

export default SpiderBossDeathFxAnimation;