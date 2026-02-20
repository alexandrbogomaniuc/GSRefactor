import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import FlameDebris from '../FlameDebris';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import FlameThrowerHitFlame from './FlameThrowerHitFlame';
import FlameThrowerHitFXs from './FlameThrowerHitFXs';
import FlameThrowerBeam from '../FlameThrowerBeam';

class FlameThrowerHitEffect extends Sprite {

	static get EVENT_ON_ANIMATION_END()		{ return 'EVENT_ON_ANIMATION_END'; }

	constructor(aScreenContainer_sprt, aBeam_fthb)
	{
		super();

		this._beam_fthb = aBeam_fthb;
		this._beam_fthb.on(FlameThrowerBeam.EVENT_ON_ANIMATION_COMPLETED, this._onBeamAnimationCompleted, this);
		this._invalidatePosition();
		this._startFollowingBeam();

		this._fScreenContainer_sprt = aScreenContainer_sprt; //for flame debris - the same container as for enemies
		this._fHitFlame_sprt = null;

		this._fFlameDebris_fd_arr = [];

		this._createView();
	}

	_createView()
	{
		let lIsVfxMediumOrGreater_bln = APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater;

		if (lIsVfxMediumOrGreater_bln)
		{
			this._createFXs();
		}

		this._createHitFlame();

		if (lIsVfxMediumOrGreater_bln)
		{
			this._createFlameDebris();
		}
	}

	_startFollowingBeam()
	{
		APP.on('tick', this._onTick, this);
	}

	_stopFollowingBeam()
	{
		APP.off('tick', this._onTick, this);
	}

	_invalidatePosition()
	{
		if (!this._beam_fthb)
		{
			return;
		}

		let targetPos = this._beam_fthb.endPoint;
		this.position.set(targetPos.x, targetPos.y);
	}

	_onBeamAnimationCompleted(event)
	{
		this._beam_fthb.off(FlameThrowerBeam.EVENT_ON_ANIMATION_COMPLETED, this._onBeamAnimationCompleted, this);
		this._beam_fthb = null;
		this._stopFollowingBeam();
	}

	_onTick(e)
	{
		this._invalidatePosition();
	}

	_createFXs()
	{
		let lHitFXs_sprt = this._fHitFXs_sprt = this.addChild(new FlameThrowerHitFXs());
		lHitFXs_sprt.on(FlameThrowerHitFXs.EVENT_ON_ANIMATION_COMPLETED, this._onHitFXsAnimationCompleted, this);
		lHitFXs_sprt.startAnimation();
	}

	_onHitFXsAnimationCompleted()
	{
		this._fHitFXs_sprt.destroy();
		this._fHitFXs_sprt = null;
		this._onAnimationCompleteSuspicion();
	}

	_createHitFlame()
	{
		let lHitFlame_sprt = this._fHitFlame_sprt = this.addChild(new FlameThrowerHitFlame());
		lHitFlame_sprt.on(FlameThrowerHitFlame.EVENT_ON_ANIMATION_COMPLETED, this._onHitFlameAnimationCompleted, this);
		lHitFlame_sprt.startAnimation();
	}

	_onHitFlameAnimationCompleted()
	{
		this._fHitFlame_sprt.destroy();
		this._fHitFlame_sprt = null;
		this._onAnimationCompleteSuspicion();
	}

	_createFlameDebris()
	{
		let lCount_int = Utils.random(6, 10);
		for (let i=0; i<lCount_int; i++)
		{
			let lFlameDebris_fd = new FlameDebris();
			lFlameDebris_fd.position.set(this.position.x, this.position.y);
			this._fScreenContainer_sprt.addChild(lFlameDebris_fd);
			lFlameDebris_fd.on(FlameDebris.EVENT_ON_ANIMATION_END, this._onFlameDebrisAnimationEnd, this);
			this._fFlameDebris_fd_arr.push(lFlameDebris_fd);
		}
	}

	_onFlameDebrisAnimationEnd(aEvent_obj)
	{
		let lFlameDebris_fd = aEvent_obj.target;
		let lIndex_int = this._fFlameDebris_fd_arr.indexOf(lFlameDebris_fd);
		if (~lIndex_int)
		{
			this._fFlameDebris_fd_arr.splice(lIndex_int, 1);
			lFlameDebris_fd.destroy();
		}
		if (this._fFlameDebris_fd_arr.length === 0)
		{
			this._onAnimationCompleteSuspicion();
		}
	}

	_onAnimationCompleteSuspicion()
	{
		if (!this._fHitFlame_sprt && !this._fHitFXs_sprt && this._fFlameDebris_fd_arr.length === 0)
		{
			this._onAnimationCompleted();
		}
	}

	_onAnimationCompleted()
	{
		this.emit(FlameThrowerHitEffect.EVENT_ON_ANIMATION_END);
		this.destroy();
	}

	destroy()
	{
		this._stopFollowingBeam();

		Sequence.destroy(Sequence.findByTarget(this._fHitFlame_sprt));
		this._fHitFlame_sprt && this._fHitFlame_sprt.destroy();
		this._fHitFlame_sprt = null;

		//remove flame debris
		for (let lFlameDebris_fd of this._fFlameDebris_fd_arr)
		{
			lFlameDebris_fd.destroy();			
		}
		this._fFlameDebris_fd_arr = [];

		this._fScreenContainer_sprt = null;

		this.removeAllListeners();

		super.destroy();
	}

}

export default FlameThrowerHitEffect;