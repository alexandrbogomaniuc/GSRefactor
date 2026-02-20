import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../config/AtlasConfig';
import ProfilingInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';

const TYPE_ID_CO_PLAYER = 0;
const TYPE_ID_LEVEL_1 = 1;
const TYPE_ID_LEVEL_2 = 2;
const TYPE_ID_LEVEL_3 = 3;
const TYPE_ID_LEVEL_4 = 4;
const TYPE_ID_LEVEL_5 = 5;

const TYPE_IDS =
	[
		TYPE_ID_CO_PLAYER,
		TYPE_ID_LEVEL_1,
		TYPE_ID_LEVEL_2,
		TYPE_ID_LEVEL_3,
		TYPE_ID_LEVEL_4,
		TYPE_ID_LEVEL_5
	];

class MissEffect extends Sprite
{
	static get IS_MISS_EFFECT_REQUIRED()	{ return true; }
	static get IS_SMOKE_REQUIRED()			{ return !APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.HIGH); }

	static get TYPE_ID_CO_PLAYER()			{ return TYPE_ID_CO_PLAYER; }
	static get TYPE_ID_LEVEL_1()			{ return TYPE_ID_LEVEL_1; }
	static get TYPE_ID_LEVEL_2()			{ return TYPE_ID_LEVEL_2; }
	static get TYPE_ID_LEVEL_3()			{ return TYPE_ID_LEVEL_3; }
	static get TYPE_ID_LEVEL_4()			{ return TYPE_ID_LEVEL_4; }
	static get TYPE_ID_LEVEL_5()			{ return TYPE_ID_LEVEL_5; }

	static get TYPE_IDS()					{ return TYPE_IDS; }
	static get TYPES_COUNT()				{ return TYPE_IDS.length; }

	constructor()
	{
		super();

		this._fCanBeReused_bl = false;
		this.__fTimeLine_mt = null;

		//INIT...
		this.init();

		if (this.__fTimeLine_mt)
		{
			this.__fTimeLine_mt.callFunctionAtFrame(this.show, 1, this);
			this.__fTimeLine_mt.callFunctionOnFinish(this.drop, this);
		}
		//...INIT
	}

	init()
	{
		//must be overrided
	}


	reset(aX_num, aY_num)
	{
		//console.log("BulletProblem:_missEfect");
		this._fCanBeReused_bl = false;
		this.position.set(aX_num, aY_num);
		this.__fTimeLine_mt.play();
	}

	drop()
	{
		this._fCanBeReused_bl = true;
		this.visible = false;

		this.__fTimeLine_mt.stop();
	}

	canBeReused()
	{
		return this._fCanBeReused_bl;
	}

	destroy()
	{
		this._fCanBeReused_bl = undefined;
		this.__fTimeLine_mt.destroy();
		super.destroy();
	}
}

MissEffect.getSmokeTextures = function ()
{
	if (!MissEffect.smokeTextures)
	{
		MissEffect.setSmokeTextures();
	}
	return MissEffect.smokeTextures;
}

MissEffect.setSmokeTextures = function ()
{
	var asset = APP.library.getAsset("enemy_impact/smoke_frames");
	var config = AtlasConfig.SmokeImpactEnemy[0];
	var pathName = "SmokeImpactEnemy";

	MissEffect.smokeTextures = AtlasSprite.getFrames([asset], [config], pathName);
	MissEffect.smokeTextures.sort(function (a, b) { if (a._atlasName > b._atlasName) return 1; else return -1 });
}

export default MissEffect;