import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../../config/AtlasConfig';
import CoinParticle from './CoinParticle';
import { FRAME_RATE } from './../../../../../../../shared/src/CommonConstants';
import GameProfilingInfo from './../../../../../model/profiling/GameProfilingInfo';

const EMITTER_SETTINGS = {
	lifetime:		{min: 38*FRAME_RATE, max: 40*FRAME_RATE},
	fadingtime:		{min: 4*FRAME_RATE, max: 5*FRAME_RATE},
	radius:			290, //px
	dispersion:		40, //px
	intense:		{
		[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOW]:		{min: 1*FRAME_RATE, max: 1.2*FRAME_RATE}, //~260
		[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOWER]:	{min: 0.5*FRAME_RATE, max: 1*FRAME_RATE}, //~400
		[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM]:	{min: 0.2*FRAME_RATE, max: 0.5*FRAME_RATE}, //~900
		[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.HIGH]:	{min: 0.18*FRAME_RATE, max: 0.26*FRAME_RATE} //~1400
	},
	emittingtime:	32*FRAME_RATE
};

var COINS_POOL = [];

const POOL_LIMITS = {
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOW]:		260,
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOWER]:	400,
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM]:	900,
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.HIGH]:	1400
};

let coin_textures = null;

function initCoinTextures()
{
	if (!coin_textures)
	{
		let lTextures_arr = AtlasSprite.getFrames(APP.library.getAsset("boss_mode/coins_explode/boss_death_coin"), AtlasConfig.BossDeathCoin, "");
		coin_textures = lTextures_arr;
	}
}

class CoinsEmitter extends Sprite
{
	static get EVENT_ON_EMITTING_COMPLETED()		{return "onEmittingCompleted";}

	static cleanPool()
	{
		for (let lCoin_cp of COINS_POOL)
		{
			lCoin_cp.destroy();
		}

		COINS_POOL = [];
	}

	static get poolLimit()
	{
		let lVFXProfile_str = GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.HIGH;
		if (APP && APP.profilingController && APP.profilingController.info && APP.profilingController.info.vfxProfileValue)
		{
			lVFXProfile_str = APP.profilingController.info.vfxProfileValue;
		}

		return POOL_LIMITS[lVFXProfile_str];
	}

	static fillPool()
	{
		initCoinTextures();

		if (COINS_POOL.length < CoinsEmitter.poolLimit)
		{
			APP.on("tick", CoinsEmitter._preGenerateParticle);
		}
	}

	static _preGenerateParticle()
	{
		if (COINS_POOL.length >= CoinsEmitter.poolLimit)
		{
			APP.off("tick", CoinsEmitter._preGenerateParticle);
			return;
		}

		COINS_POOL.push(CoinsEmitter._generateParticle(true));
	}

	static _generateParticle(aForceCreate_bln = false)
	{
		let lRnd_num = Math.random();
		let lDist_num = EMITTER_SETTINGS.radius + (lRnd_num*EMITTER_SETTINGS.dispersion - EMITTER_SETTINGS.dispersion/2);
		let lLifeTime_num = CoinsEmitter._getMidValue(EMITTER_SETTINGS.lifetime.min, EMITTER_SETTINGS.lifetime.max, lRnd_num);
		let lFadingTime_num = CoinsEmitter._getMidValue(EMITTER_SETTINGS.fadingtime.min, EMITTER_SETTINGS.fadingtime.max, lRnd_num);

		let lCoin_cp;
		if (COINS_POOL.length > 0 && !aForceCreate_bln)
		{
			lCoin_cp = COINS_POOL.pop();
			lCoin_cp.resurrect(lDist_num, lLifeTime_num, lLifeTime_num + lFadingTime_num);
		}
		else
		{
			lCoin_cp = new CoinParticle(coin_textures, lDist_num, lLifeTime_num, lLifeTime_num + lFadingTime_num);
		}

		lCoin_cp.rotation = lRnd_num*12.56637;

		return lCoin_cp;
	}

	static _getMidValue(a, b, seed)
	{
		if (seed === undefined) seed = Math.random();
		return a + seed*(b - a);
	}

	start()
	{
		this._start();
	}

	constructor()
	{
		super();

		initCoinTextures();

		this._fIntenseProfile_obj = EMITTER_SETTINGS.intense[APP.profilingController.info.vfxProfileValue];

		this._fFinishing_bln = false;
		this._fStarted_bln = false;
		this._fParticles_arr = [];

		this._fEmittingTime_num = 0;
		this._fIntenseTimer_num = 0;
	}

	_start()
	{
		if (this._fStarted_bln) return;

		this._fStarted_bln = true;
		APP.on("tick", this._onTick, this);
	}

	_onTick(aEvent_obj)
	{
		if (!this._fParticles_arr)
		{
			APP.off("tick", this._onTick, this);
			return;
		}

		let lDelta_num = aEvent_obj.delta;

		this._fEmittingTime_num += lDelta_num;

		for (let lCoin_cp of this._fParticles_arr)
		{
			lCoin_cp.updateParticle(lDelta_num);
		}

		if (!this._fStarted_bln)
		{
			return;
		}

		if (this._fEmittingTime_num >= EMITTER_SETTINGS.emittingtime)
		{
			this._fFinishing_bln = true;
			this._validateFinish();

			return;
		}

		let lIntense_num = CoinsEmitter._getMidValue(this._fIntenseProfile_obj.min, this._fIntenseProfile_obj.max);
		while (this._fIntenseTimer_num < this._fEmittingTime_num)
		{
			let lParticle_sprt = this.addChild(CoinsEmitter._generateParticle());
			lParticle_sprt.once(CoinParticle.EVENT_ON_DEATH, this._onDeath, this);
			this._fParticles_arr.push(lParticle_sprt);
			lParticle_sprt.play();

			this._fIntenseTimer_num += lIntense_num;
		}
	}

	_onDeath(aEvent_obj)
	{
		let lCoin_cp = aEvent_obj.target;

		let lId_num = this._fParticles_arr.indexOf(lCoin_cp);
		if (~lId_num) this._fParticles_arr.splice(lId_num, 1);

		this.removeChild(lCoin_cp);

		if (COINS_POOL.length < CoinsEmitter.poolLimit)
		{
			lCoin_cp.stop();
			COINS_POOL.push(lCoin_cp);
		}
		else
		{
			lCoin_cp.destroy();
		}

		this._validateFinish();
	}

	_validateFinish()
	{
		if (this._fFinishing_bln && this._fParticles_arr.length == 0)
		{
			this._onFinish();
		}
	}

	_onFinish()
	{
		APP.off("tick", this._onTick, this);
		this._fStarted_bln = false;
		this._fFinishing_bln = false;
		this._fParticles_arr = [];

		this._fEmittingTime_num = 0;
		this._fIntenseTimer_num = 0;

		this.emit(CoinsEmitter.EVENT_ON_EMITTING_COMPLETED, {target: this});
	}

	destroy()
	{
		APP.off("tick", this._onTick, this);

		super.destroy();

		this._fFinishing_bln = undefined;
		this._fStarted_bln = undefined;
		this._fParticles_arr = null;

		this._fEmittingTime_num = null;
		this._fIntenseTimer_num = null;

		this._fIntenseProfile_obj = null;
	}
}

export default CoinsEmitter;