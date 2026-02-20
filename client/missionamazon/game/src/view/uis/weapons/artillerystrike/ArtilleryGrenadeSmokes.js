import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CommonEffectsManager from '../../../../main/CommonEffectsManager';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import ArtilleryCanisterSmoke from './ArtilleryCanisterSmoke';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const CANISTER_SMOKES_PARAMS = [
	{ typeId: ArtilleryCanisterSmoke.TYPE_WHITE, delay: 0*2*16.7 },
	{ typeId: ArtilleryCanisterSmoke.TYPE_WHITE, delay: 1*2*16.7 },
	{ typeId: ArtilleryCanisterSmoke.TYPE_GREEN, delay: 3*2*16.7 },
	{ typeId: ArtilleryCanisterSmoke.TYPE_GREEN, delay: 9*2*16.7 },
	{ typeId: ArtilleryCanisterSmoke.TYPE_WHITE, delay: 23*2*16.7 }
];

class ArtilleryGrenadeSmokes extends Sprite
{
	static get EVENT_ON_ANIMATION_END() 		{ return 'EVENT_ON_ANIMATION_END'; }

	constructor()
	{
		super();

		CommonEffectsManager.getGroundSmokeTextures();

		this._fBarrelSmoke_sprt = null;
		this._fGroundSmoke_sprt = null;
		this._fCanisterSmokes_sprt = null;

		this._createView();
	}

	_createView()
	{
		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._createBarrelSmoke();
			this._createGroundSmoke();
		}
		this._createCanisterSmokes();
	}

	_createBarrelSmoke()
	{
		let lBarrelSmoke_sprt = this.addChild(new Sprite);

		//pixi-heaven can't be used in IE and Edge
		if (navigator.userAgent.indexOf('Edge') < 0 && navigator.userAgent.indexOf('MSIE') < 0 && navigator.userAgent.indexOf('Trident') < 0)
		{
			let streak = lBarrelSmoke_sprt.addChild(new Sprite);
			streak.textures = CommonEffectsManager.getStreakTextures();
			streak.once('animationend', (e) => {
				streak.stop();
				streak.destroy();
			})
			streak.blendMode = PIXI.BLEND_MODES.ADD;
			streak.scale.set(0.07*4 * 0.312*4);
			streak.scaleXTo(0.15*4, 30*2*16.7);
			streak.rotation = Utils.gradToRad(-104);
			streak.anchor.set(0.16, 0.49);
			//streak.gotoAndPlay(23);
			streak.convertToHeaven();
			streak.color.setLight(0.2, 0.2, 0.2);
			streak.color.invalidate();
		}

		let smoke = lBarrelSmoke_sprt.addChild(new Sprite);		
		smoke.textures = CommonEffectsManager.getDieSmokeUnmultTextures();
		smoke.anchor.set(0.57, 0.81);
		smoke.once('animationend', (e) => {
			smoke.destroy();
		})
		smoke.scale.set(2*0.4, 2*0.6);
		smoke.rotation = Utils.gradToRad(-7);
		smoke.play();

		lBarrelSmoke_sprt.scale.set(0.87, 0.7);
		lBarrelSmoke_sprt.rotation = Utils.gradToRad(35);

		this._fBarrelSmoke_sprt = lBarrelSmoke_sprt;
	}

	_createGroundSmoke()
	{
		let lGroundSmokes_sprt = this.addChild(new Sprite);

		let textures = CommonEffectsManager.getGroundSmokeTextures();
		
		let groundSmokeScreen = lGroundSmokes_sprt.addChild(new Sprite);
		groundSmokeScreen.blendMode = PIXI.BLEND_MODES.SCREEN;
		groundSmokeScreen.textures = textures;
		groundSmokeScreen.scale.set(4*0.27);
		groundSmokeScreen.gotoAndPlay(13);
		groundSmokeScreen.once('animationend', _=> {
			groundSmokeScreen.destroy();
		})

		let groundSmokeUnmult = lGroundSmokes_sprt.addChild(new Sprite);
		groundSmokeUnmult.tint = 0x626262;
		groundSmokeUnmult.scale.set(4*0.37);
		groundSmokeUnmult.gotoAndPlay(13);
		groundSmokeUnmult.once('animationend', _=> {
			groundSmokeUnmult.destroy();
			//this._onAnimationEnd();
		})

		this._fGroundSmoke_sprt = lGroundSmokes_sprt;

	}

	_createCanisterSmokes()
	{
		this._fCanisterSmokes_sprt = this.addChild(new Sprite);

		for (let i=0; i<CANISTER_SMOKES_PARAMS.length; i++)
		{
			let lParams_obj = CANISTER_SMOKES_PARAMS[i];
			let lIsLast_bl = i === CANISTER_SMOKES_PARAMS.length - 1;
			this._createCanisterSmoke(lParams_obj, lIsLast_bl);
		}

		//add mask
		let lMask_sprt = this.addChild(APP.library.getSpriteFromAtlas('weapons/ArtilleryStrike/SmokeGrenadeTrailMask'));
		lMask_sprt.anchor.y = 1;
		lMask_sprt.scale.set(2);
		lMask_sprt.position.y = -10;
		this.addChild(lMask_sprt);

		this._fCanisterSmokes_sprt.mask = lMask_sprt;
	}

	_createCanisterSmoke(aParams_obj, aIsLast_bl = false)
	{
		let lCanisterSmoke_acs = new ArtilleryCanisterSmoke(aParams_obj.typeId, aParams_obj.delay);
		lCanisterSmoke_acs.once(ArtilleryCanisterSmoke.EVENT_ON_ANIMATION_END, _=> {
			lCanisterSmoke_acs.destroy();
			if (aIsLast_bl)
			{
				this._onAnimationEnd();
			}
		})
		this._fCanisterSmokes_sprt.addChild(lCanisterSmoke_acs);
	}

	_onAnimationEnd()
	{
		this.emit(ArtilleryGrenadeSmokes.EVENT_ON_ANIMATION_END);
	}

	destroy()
	{
		this.removeAllListeners();

		super.destroy();
	}
}

export default ArtilleryGrenadeSmokes;