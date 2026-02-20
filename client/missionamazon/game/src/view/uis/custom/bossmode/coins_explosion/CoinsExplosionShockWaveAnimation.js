import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sequence } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import { FRAME_RATE } from './../../../../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

const SEQUENCE_SETTINGS_A = [
	{
		position: {x: 65, y: -80},
		start: 0*FRAME_RATE
	},
	{
		position: {x: 0, y: 0},
		start: 0*FRAME_RATE
	},
	{
		position: {x: 18, y: 90},
		start: 0*FRAME_RATE
	}
];

const SEQUENCE_SETTINGS_B = [
	{
		position: {x: 70, y: -90},
		start: 0*FRAME_RATE
	},
	{
		position: {x: 0, y: 0},
		start: 11*FRAME_RATE
	},
	{
		position: {x: 18, y: 90},
		start: 22*FRAME_RATE
	},
	{
		position: {x: 90, y: 0},
		start: 36*FRAME_RATE
	},
	{
		position: {x: -30, y: -80},
		start: 64*FRAME_RATE
	}
];

class CoinsExplosionShockWaveAnimation extends Sprite
{
	startPartA()
	{
		this._startPartA();
	}

	startPartB()
	{
		this._startPartB();
	}

	constructor()
	{
		super();

		this._fWaves_arr = [];
	}

	_startPartA()
	{
		this._startAnimations(SEQUENCE_SETTINGS_A);
	}

	_startPartB()
	{
		this._startAnimations(SEQUENCE_SETTINGS_B);
	}

	_startAnimations(aSettings_arr)
	{
		for (let lSet_obj of aSettings_arr)
		{
			this._startWaveAnimation(lSet_obj.position, lSet_obj.start);
		}
	}

	_startWaveAnimation(aPos_obj, aDelay_num)
	{
		let lWave_sprt = this.addChild(this._getShockWave(aPos_obj));
		this._fWaves_arr.push(lWave_sprt);

		let lSeq_arr = [
			{tweens: [{prop: 'alpha', to: 1}], duration: 0},
			{tweens: [{prop: 'scale.x', to: 0.585}, {prop: 'scale.y', to: 0.585}, {prop: 'rotation', to: Utils.gradToRad(-26.5)}], duration: 5*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.74}, {prop: 'scale.y', to: 1.74}, {prop: 'alpha', to: 0}, {prop: 'rotation', to: Utils.gradToRad(-180.2)}], duration: 29*FRAME_RATE, onfinish: () => {
				let lId_num = this._fWaves_arr.indexOf(lWave_sprt);
				if (~lId_num) this._fWaves_arr.splice(lId_num, 1);

				lWave_sprt.destroy();
			}}
		];

		Sequence.start(lWave_sprt, lSeq_arr, aDelay_num);
	}

	_getShockWave(aPos_obj)
	{
		let lWave_sprt = APP.library.getSprite("boss_mode/coins_explode/shockwave");
		lWave_sprt.scale.set(0.454);
		lWave_sprt.position.set(aPos_obj.x, aPos_obj.y);
		lWave_sprt.alpha = 0;
		lWave_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		return lWave_sprt;
	}

	destroy()
	{
		for (let lWave_sprt of this._fWaves_arr)
		{
			if (lWave_sprt)
			{
				Sequence.destroy(Sequence.findByTarget(lWave_sprt));
				lWave_sprt.destroy();
			}
		}

		super.destroy();

		this._fWaves_arr = null;
	}
}

export default CoinsExplosionShockWaveAnimation;