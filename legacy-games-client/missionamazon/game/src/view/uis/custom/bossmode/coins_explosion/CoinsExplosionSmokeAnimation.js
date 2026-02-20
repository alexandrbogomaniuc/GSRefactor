import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import CommonEffectsManager from './../../../../../main/CommonEffectsManager';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const SMOKES_SETTINGS = [
	{
		position: {x: 18, y: 31},
		angle: 7,
		scale: {x: 1.59, y: 1.226}
	},
	{
		position: {x: 22, y: 2},
		angle: 109,
		scale: {x: 1.59, y: 1.226}
	},
	{
		position: {x: 86, y: -102},
		angle: 224,
		scale: {x: 1.59, y: 1.226}
	},
	{
		position: {x: 15, y: -24},
		angle: 248,
		scale: {x: 1.59, y: 1.226}
	},
	{
		position: {x: 4, y: -44},
		angle: 280,
		scale: {x: 1.59, y: 1.226}
	},
	{
		position: {x: -10, y: -136},
		angle: 162,
		scale: {x: 1.79, y: 1.226}
	}
];

class CoinsExplosionSmokeAnimation extends Sprite
{
	static get EVENT_ON_COINS_EXPLOSION_SMOKE_ANIMATION_COMPLETED()		{return "onCoinsExplosionSmokeAnimationCompleted";}

	startAnimation(aDelay_num)
	{
		if (aDelay_num > 0)
		{
			this._fTimer_t = new Timer(()=>this._startAnimationImmediately(), aDelay_num);
		}
		else
		{
			this._startAnimationImmediately();
		}
	}

	constructor()
	{
		super();

		this._fSmokes_arr = [];
		this._fTimer_t = null;
		this._fDeathGroundSmokes_arr_sprt = [];
	}

	_startAnimationImmediately()
	{
		for (let lSet_obj of SMOKES_SETTINGS)
		{
			this.addChild(this._getSmoke(lSet_obj.position, lSet_obj.angle, lSet_obj.scale));
		}

		this._startGroundSmokeAnimation();
	}

	_getSmoke(aPos_obj, aAngle_num, aScale_obj)
	{
		let lSmoke_sprt = new Sprite;
		lSmoke_sprt.textures = CommonEffectsManager.getDieSmokeUnmultTextures();
		lSmoke_sprt.scale.x = 2*aScale_obj.x;
		lSmoke_sprt.scale.y = 2*aScale_obj.y;
		lSmoke_sprt.anchor.set(0.57, 0.81);
		lSmoke_sprt.position.set(aPos_obj.x, aPos_obj.y);
		lSmoke_sprt.rotation = Utils.gradToRad(aAngle_num);
		lSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lSmoke_sprt.on('animationend', () => {
			let lId_num = this._fSmokes_arr.indexOf(lSmoke_sprt);
			if (~lId_num) this._fSmokes_arr.splice(lSmoke_sprt);

			lSmoke_sprt.destroy();

			if (!this._fSmokes_arr.length)
			{
				this._validateAnimationCompletion();
			}
		});
		lSmoke_sprt.play();

		this._fSmokes_arr.push(lSmoke_sprt);

		return lSmoke_sprt;
	}

	_validateAnimationCompletion()
	{
		if (!this._fDeathGroundSmokes_arr_sprt.length && !this._fSmokes_arr.length)
		{
			this._onAnimationCompleted();
		}
	}

	_onAnimationCompleted()
	{
		this.emit(CoinsExplosionSmokeAnimation.EVENT_ON_COINS_EXPLOSION_SMOKE_ANIMATION_COMPLETED);
	}

	//GROUND SMOKE...
	_startGroundSmokeAnimation()
	{
		this._createDeathGroundSmoke({x: 0, y: 0}, 45);
		this._createDeathGroundSmoke({x: 0, y: 0}, 100);
		this._createDeathGroundSmoke({x: 0, y: 0}, -45);
	}

	_createDeathGroundSmoke(aPosition, aAngle_num)
	{
		let groundSmoke = this.addChild(new Sprite);
		groundSmoke.textures = [PIXI.Texture.EMPTY, PIXI.Texture.EMPTY, PIXI.Texture.EMPTY, PIXI.Texture.EMPTY].concat(this._deathGroundSmokeTextures);
		groundSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		groundSmoke.position.set(aPosition.x, aPosition.y);
		groundSmoke.scale.set(8);
		groundSmoke.rotation = Utils.gradToRad(aAngle_num);
		groundSmoke.animationSpeed = 30/60;

		groundSmoke.once('animationend', (e) => { this._onGroundSmokeCompleted(e.target) });
		groundSmoke.play();

		this._fDeathGroundSmokes_arr_sprt.push(groundSmoke);
	}

	_onGroundSmokeCompleted(aDeathGroundSmoke)
	{
		let lIndex = this._fDeathGroundSmokes_arr_sprt.indexOf(aDeathGroundSmoke);
		if (lIndex >= 0)
		{
			this._fDeathGroundSmokes_arr_sprt.splice(lIndex, 1);
		}

		aDeathGroundSmoke.destroy();

		if (!this._fDeathGroundSmokes_arr_sprt.length)
		{
			this._validateAnimationCompletion();
		}
	}

	get _deathGroundSmokePosition()
	{
		return new PIXI.Point(0, 0);
	}

	get _deathGroundSmokeTextures()
	{
		CommonEffectsManager.getGroundSmokeTextures();
		return CommonEffectsManager.textures['groundSmoke'];
	}
	//...GROUND SMOKE

	destroy()
	{
		for (let lSmoke_sprt of this._fSmokes_arr)
		{
			lSmoke_sprt && lSmoke_sprt.destroy();
		}

		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;

		while (this._fDeathGroundSmokes_arr_sprt.length)
		{
			this._fDeathGroundSmokes_arr_sprt.pop().destroy();
		}
		this._fDeathGroundSmokes_arr_sprt = null;

		super.destroy();

		this._fSmokes_arr = null;
	}
}

export default CoinsExplosionSmokeAnimation;