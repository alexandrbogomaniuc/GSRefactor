import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CryogunBeam from '../../../../main/animation/cryogun/CryogunBeam';
import CryogunsEffectView from './CryogunsEffectView';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { COOP_FIRE_FX_ALPHA } from '../../../../../../shared/src/CommonConstants';

class CryogunsView extends SimpleUIView {

	static get EVENT_ON_BEAM_TARGET_ACHIEVED() 		{ return CryogunBeam.EVENT_ON_TARGET_ACHIEVED }
	static get EVENT_ON_BEAM_ANIMATION_COMPLETED() 	{ return CryogunBeam.EVENT_ON_ANIMATION_COMPLETED }

	constructor()
	{
		super();

		this._fBeams_cb_arr = [];
		this._fEffectViews_cev_arr = [];
	}

	get _mainContainer ()
	{
		return APP.currentWindow.gameField.cryogunEffectContainer;
	}

	//PUBLIC...
	i_init()
	{
		this._createView();
	}

	i_showFire(aShotData_obj, aStartPos_pt, aEndPos_pt, aWeaponScale, callback)
	{
		this._showFire(aShotData_obj, aStartPos_pt, aEndPos_pt, aWeaponScale, callback)
	}

	i_clearAll()
	{
		for (let lBeam_cb of this._fBeams_cb_arr)
		{
			lBeam_cb.destroy();
		}
		for (let lEffectView_cev of this._fEffectViews_cev_arr)
		{
			lEffectView_cev.destroy();
		}

		this._fBeams_cb_arr = [];
		this._fEffectViews_cev_arr = [];
	}
	//...PUBLIC

	//PRIVATE...
	_createView()
	{
		this._mainContainer.container.addChild(this);
		this.zIndex = this._mainContainer.zIndex;

		//DEBUG...
		// let gr = new PIXI.Graphics();
		// gr.beginFill(0x00ffff);
		// gr.drawCircle(960/2, 540/2, 10);
		// this.addChild(gr);
		//...DEBUG
	}

	_showFire(aShotData_obj, aStartPos_pt, aEndPos_pt, aWeaponScale, callback)
	{
		//DEBUG...
		// let gr = new PIXI.Graphics();
		// gr.beginFill(0xffff00);
		// gr.drawCircle(aStartPos_pt.x, aStartPos_pt.y, 10);
		// this.addChild(gr);
		//...DEBUG

		let lBeam_cb = new CryogunBeam(aShotData_obj, callback);
		lBeam_cb.on(CryogunBeam.EVENT_ON_ANIMATION_COMPLETED, this._onBeamAnimationCompleted, this);
		lBeam_cb.on(CryogunBeam.EVENT_ON_TARGET_ACHIEVED, this._onBeamTargetAchieved, this);
		lBeam_cb.scale.y = aWeaponScale;
		let lStartPos_pt = this.getStartPosBasedOnWeaponScale(aStartPos_pt, aEndPos_pt, aWeaponScale);
		lBeam_cb.i_shoot(lStartPos_pt, aEndPos_pt);
		this.addChild(lBeam_cb);

		if (aShotData_obj.rid == -1)
		{
			lBeam_cb.alpha = COOP_FIRE_FX_ALPHA;
		}

		this._fBeams_cb_arr.push(lBeam_cb);
	}

	getStartPosBasedOnWeaponScale(aStartPos_pt, aEndPos_pt, aWeaponScale)
	{
		let angle = Math.PI / 2 - Utils.getAngle(aStartPos_pt, aEndPos_pt);
		let distance = 60 * (aWeaponScale - 1);

		let lNewStartPos_pt = {
			x: aStartPos_pt.x + Math.cos(angle)*(distance), 
			y: aStartPos_pt.y + Math.sin(angle)*(distance)
		};

		return lNewStartPos_pt;
	}

	_onBeamTargetAchieved(aEvent_obj)
	{
		this._startCentralEffect(aEvent_obj.x, aEvent_obj.y, aEvent_obj.target.alpha);
	}

	_onBeamAnimationCompleted(aEvent_obj)
	{
		let lBeam_cb = aEvent_obj.target;
		this.emit(CryogunsView.EVENT_ON_BEAM_ANIMATION_COMPLETED, {shotData: lBeam_cb.shotData});
		let lIndex_int = this._fBeams_cb_arr.indexOf(lBeam_cb);
		if (~lIndex_int)
		{
			this._fBeams_cb_arr.splice(lIndex_int, 1);
			lBeam_cb.destroy();
		}
	}

	_startCentralEffect(x, y, aAlpha_num)
	{
		let lEffectView_cev = new CryogunsEffectView();
		lEffectView_cev.position.set(x, y);
		lEffectView_cev.alpha = aAlpha_num;
		this.addChild(lEffectView_cev);
		lEffectView_cev.once(CryogunsEffectView.EVENT_ON_ANIMATION_COMPLETED, this._onEffectAnimationCompleted, this);
		this._fEffectViews_cev_arr.push(lEffectView_cev);
	}

	_onEffectAnimationCompleted(aEvent_obj)
	{
		let index = this._fEffectViews_cev_arr.indexOf(aEvent_obj.target);
		if (~index)
		{
			let lEffectView_cev = this._fEffectViews_cev_arr.splice(index, 1)[0];
			lEffectView_cev.destroy();
		}
	}
	//...PRIVATE
}

export default CryogunsView;