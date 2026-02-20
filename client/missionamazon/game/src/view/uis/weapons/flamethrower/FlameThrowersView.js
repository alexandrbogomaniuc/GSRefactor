import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import FlameThrowerBeam from '../../../../main/animation/flamethrower/FlameThrowerBeam';
import FlameThrowerHitEffect from '../../../../main/animation/flamethrower/hit_effect/FlameThrowerHitEffect';
import FlameThrowerGunFireEffect from '../../../../main/animation/flamethrower/FlameThrowerGunFireEffect';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { COOP_FIRE_FX_ALPHA } from '../../../../../../shared/src/CommonConstants';

class FlameThrowersView extends SimpleUIView {

	static get EVENT_ON_BEAM_ANIMATION_COMPLETED() 		 	{ return FlameThrowerBeam.EVENT_ON_ANIMATION_COMPLETED; }
	static get EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED() 	{ return FlameThrowerBeam.EVENT_ON_BASIC_ANIMATION_COMPLETED; }
	static get EVENT_ON_BEAM_ROTATION_UPDATED() 			{ return FlameThrowerBeam.EVENT_ON_ROTATION_UPDATED; }

	static get EVENT_ON_HIT_ANIMATION_STARTED() 			{ return 'EVENT_ON_HIT_ANIMATION_STARTED'; }
	static get EVENT_ON_HIT_ANIMATION_COMPLETED() 			{ return 'EVENT_ON_HIT_ANIMATION_COMPLETED'; }

	constructor()
	{
		super();

		this._fBeams_ftb_arr = [];
		this._fHitEffects_fthe_arr = [];
		this._fGunFireEffect_ftgfe_arr = [];
	}

	get _mainContainer ()
	{
		return APP.currentWindow.gameField.flameThrowerEffectContainer;
	}

	//PUBLIC...
	i_init()
	{
		this._init();
	}

	i_showFire(data, aStartPos_pt, aEndPos_pt, callback, weaponScale)
	{
		this._showFire(data, aStartPos_pt, aEndPos_pt, callback, weaponScale);
	}

	i_clearAll()
	{
		this._clearAll();
	}
	//...PUBLIC

	//PRIVATE...
	_init()
	{
		this._mainContainer.container.addChild(this);
		this.zIndex = this._mainContainer.zIndex;
	}


	
	_showFire(data, aStartPos_pt, aEndPos_pt, callback, weaponScale)
	{
		//create beam
		this._createBeam(data, aStartPos_pt, aEndPos_pt, callback, weaponScale);

		//create gun fire effect
		if (APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			this._showGunFireEffect(data, aStartPos_pt, aEndPos_pt, weaponScale);
		}
	}

	//BEAM...
	_createBeam(data, aStartPos_pt, aEndPos_pt, callback, weaponScale)
	{
		//create beam
		let lBeam_ftb = new FlameThrowerBeam(data, callback, weaponScale);

		lBeam_ftb.on(FlameThrowerBeam.EVENT_ON_ANIMATION_COMPLETED, this._onBeamAnimationCompleted, this);
		lBeam_ftb.on(FlameThrowerBeam.EVENT_ON_BASIC_ANIMATION_COMPLETED, this._onBeamBasicAnimationCompleted, this);
		lBeam_ftb.on(FlameThrowerBeam.EVENT_ON_TARGET_ACHIEVED, this._onBeamTargetAchieved, this);
		lBeam_ftb.on(FlameThrowerBeam.EVENT_ON_ROTATION_UPDATED, this.emit, this);
		lBeam_ftb.i_shoot(aStartPos_pt, aEndPos_pt);
		this.addChild(lBeam_ftb);

		if (data.rid == -1)
		{
			lBeam_ftb.alpha = COOP_FIRE_FX_ALPHA;
		}

		this._fBeams_ftb_arr.push(lBeam_ftb);
	}

	_onBeamTargetAchieved(aEvent_obj)
	{
		let lBeam_ftb = aEvent_obj.target;

		this._showHitEffect(lBeam_ftb);
	}

	_onBeamAnimationCompleted(aEvent_obj)
	{
		let lBeam_ftb = aEvent_obj.target;
		let lIndex_int = this._fBeams_ftb_arr.indexOf(lBeam_ftb);
		if (~lIndex_int)
		{
			this._fBeams_ftb_arr.splice(lIndex_int, 1);
			lBeam_ftb.destroy();
		}
	}

	_onBeamBasicAnimationCompleted(aEvent_obj)
	{
		let lBeam_ftb = aEvent_obj.target;
		this.emit(FlameThrowersView.EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED, {shotData: lBeam_ftb.shotData});
	}
	//...BEAM

	//HIT EFFECT...
	_showHitEffect(aBeam_ftb)
	{
		let lHitEffect_fthe = new FlameThrowerHitEffect(this._mainContainer.container, aBeam_ftb);
		lHitEffect_fthe.alpha = aBeam_ftb.alpha;
		lHitEffect_fthe.once(FlameThrowerHitEffect.EVENT_ON_ANIMATION_END, this._onHitEffectAnimationEnd, this);
		this.addChild(lHitEffect_fthe);
		this._fHitEffects_fthe_arr.push(lHitEffect_fthe);

		this.emit(FlameThrowersView.EVENT_ON_HIT_ANIMATION_STARTED);
	}

	_onHitEffectAnimationEnd(aEvent_obj)
	{
		let lHitEffect_fthe = aEvent_obj.target;
		let lIndex_int = this._fHitEffects_fthe_arr.indexOf(lHitEffect_fthe);
		if (~lIndex_int)
		{
			this._fHitEffects_fthe_arr.splice(lIndex_int, 1);
			lHitEffect_fthe.destroy();
		}

		this.emit(FlameThrowersView.EVENT_ON_HIT_ANIMATION_COMPLETED);
	}
	//...HIT EFFECT

	//GUN FIRE EFFECT...
	_showGunFireEffect(data, aStartPos_pt, aEndPos_pt, weaponScale)
	{
		let angle = Math.PI / 2 - Utils.getAngle(aStartPos_pt, aEndPos_pt);
		let lDist_num = 84 * weaponScale - 24; // distance from the center of the gun to the muzzle

		let dx = lDist_num * Math.cos(angle);
		let dy = lDist_num * Math.sin(angle);
		let lPos_pt = new PIXI.Point(aStartPos_pt.x + dx, aStartPos_pt.y + dy);

		let lGunFireEffect_ftgfe = new FlameThrowerGunFireEffect(weaponScale);
		lGunFireEffect_ftgfe.position.set(lPos_pt.x, lPos_pt.y);
		lGunFireEffect_ftgfe.rotation = angle + Math.PI/2;
		lGunFireEffect_ftgfe.once(FlameThrowerGunFireEffect.EVENT_ON_ANIMATION_END, this._onGunFireEffectAnimationEnd, this);
		this.addChild(lGunFireEffect_ftgfe);
		this._fGunFireEffect_ftgfe_arr.push(lGunFireEffect_ftgfe);

		if (data.rid == -1)
		{
			lGunFireEffect_ftgfe.alpha = COOP_FIRE_FX_ALPHA;
		}
	}

	_onGunFireEffectAnimationEnd(aEvent_obj)
	{
		let lGunFireEffect_ftgfe = aEvent_obj.target;
		let lIndex_int = this._fGunFireEffect_ftgfe_arr.indexOf(lGunFireEffect_ftgfe);
		if (~lIndex_int)
		{
			this._fGunFireEffect_ftgfe_arr.splice(lIndex_int, 1);
			lGunFireEffect_ftgfe.destroy();
		}
	}
	//...GUN FIRE EFFECT

	_clearAll()
	{
		for (let lBeam_ftb of this._fBeams_ftb_arr)
		{
			lBeam_ftb.destroy();
		}
		this._fBeams_ftb_arr = [];

		for (let lHitEffect_fthe of this._fHitEffects_fthe_arr)
		{
			lHitEffect_fthe.destroy();
		}
		this._fHitEffects_fthe_arr = [];

		for (let lGunFireEffect_ftgfe of this._fGunFireEffect_ftgfe_arr)
		{
			lGunFireEffect_ftgfe.destroy();
		}
		this._fGunFireEffect_ftgfe_arr = [];
	}
	//...PRIVATE
}

export default FlameThrowersView;