import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import EmblemRim from './EmblemRim';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

class WeaponSidebarIconEmblemDefault extends Sprite {
	
	static get EVENT_ON_DISAPPEARED () { return "EVENT_ON_DISAPPEARED"; }

	constructor()
	{
		super();
 
		this._fBg_sprt = null;
		this._fBody_sprt = null;
		this._fRim_er = null;
		this._fDisappearSeq = null;
		this._fIsAppearAnimationInProgress_bl = false;

		this._initView();
	}

	show()
	{
		if (this.visible) return;
		super.show();
		this._invalidateBoby();

		this._startAppearAnimation();
	}

	hide()
	{
		if (!this.visible) return;

		let lIsDisappearInProgress = this.isDisappearInProgress;

		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
		this._fDisappearSeq = null;
		Sequence.destroy(Sequence.findByTarget(this._fRim_er));

		super.hide();

		if (lIsDisappearInProgress)
		{
			this._onDisappearAnimationCompleted();
		}
	}

	get isDisappearInProgress()
	{
		return !!this._fDisappearSeq;
	}

	startDisappearAnimation()
	{
		this._startDisappearAnimation();
	}

	updateView()
	{
		this._invalidateBoby();
	}

	interruptTransitions()
	{
		this._interruptTransitions();
	}

	_initView()
	{
		this._fBg_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/back_to_default_emblem_bg"));
		this._fBg_sprt.anchor.set(55.5/128, 57.5/128);

		let lRimGlow = this._fRim_er = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/rim_glow"));
		lRimGlow.scale.set(2);
		lRimGlow.anchor.set(0.48, 0.5);
		lRimGlow.blendMode = PIXI.BLEND_MODES.ADD;
		lRimGlow.alpha = 0;

		this._fBody_sprt = this.addChild(new Sprite);
		this._fBody_sprt.rotation = Utils.gradToRad(20);
		
		this._fArrow_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/back_arrow"));
		this._fArrow_sprt.position.y = -2;
	}

	_invalidateBoby()
	{
		this._fBody_sprt.destroyChildren();

		let lBetLevel_num = APP.playerController.info.betLevel;
		let lPlayerInfo_pi = APP.playerController.info;

		let lViewType_int = lPlayerInfo_pi.getTurretSkinId(lPlayerInfo_pi.betLevel);
		let lAssetName_str = "weapons/DefaultGun/turret_"+lViewType_int+"/turret";
		
		let lView = this._fBody_sprt.addChild(APP.library.getSpriteFromAtlas(lAssetName_str));
		lView.scale.set(0.5)

		if (lViewType_int == 1)
		{
			lView.anchor.set(0.5, 0.585);
		}
	}

	_startAppearAnimation()
	{
		this._fIsAppearAnimationInProgress_bl = true;

		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
		Sequence.destroy(Sequence.findByTarget(this._fRim_er));

		this._fArrow_sprt.scale.set(0);

		let lSeq = [
			{ tweens: [ { prop: "scale.x", to: 1.1 }, { prop: "scale.y", to: 1.1 }], duration: 6*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 1.2 }, { prop: "scale.y", to: 1.2 }], duration: 5*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 1.0 }, { prop: "scale.y", to: 1.0 }], duration: 4*FRAME_RATE }
		];

		Sequence.start(this._fArrow_sprt, lSeq);

		let lRimGlow = this._fRim_er;
		lRimGlow.alpha = 1;

		let lRimSeq = [
			{ tweens: [ { prop: "alpha", to: 0 } ], duration: 16*FRAME_RATE, onfinish: () => { this._onAppearAnimationCompleted(); } }
		];

		Sequence.start(lRimGlow, lRimSeq);
	}

	_onAppearAnimationCompleted()
	{
		this._fIsAppearAnimationInProgress_bl = false;

		this._startRimGlowIdleCycle();
	}

	_startRimGlowIdleCycle()
	{
		let lRimGlow = this._fRim_er;

		let lRimSeq = [
			{ tweens: [ { prop: "alpha", to: 0.4 } ], duration: 33*FRAME_RATE },
			{ tweens: [ { prop: "alpha", to: 0.0 } ], duration: 50*FRAME_RATE, onfinish: () => { this._startRimGlowIdleCycle(); } }
		];

		Sequence.start(lRimGlow, lRimSeq);	
	}

	_startDisappearAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
		this._fIsAppearAnimationInProgress_bl = false;

		let lSeq = [
			{ tweens: [ { prop: "scale.x", to: 1.2 }, { prop: "scale.y", to: 1.2 }], duration: 4*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 1.1 }, { prop: "scale.y", to: 1.1 }], duration: 5*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 0.2 }, { prop: "scale.y", to: 0.2 }], duration: 4*FRAME_RATE, onfinish: () => { this._onDisappearAnimationCompleted(); } }
		];

		this._fDisappearSeq = Sequence.start(this._fArrow_sprt, lSeq);		
	}

	_onDisappearAnimationCompleted()
	{
		this.emit(WeaponSidebarIconEmblemDefault.EVENT_ON_DISAPPEARED);
	}

	_interruptTransitions()
	{
		if (this._fIsAppearAnimationInProgress_bl)
		{
			this._fIsAppearAnimationInProgress_bl = false;
			Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
			Sequence.destroy(Sequence.findByTarget(this._fRim_er));

			this._fArrow_sprt.scale.set(1);
			this._fRim_er.alpha = 0;

			this._startRimGlowIdleCycle();
		}
		
		if (this._fDisappearSeq)
		{
			Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
			this._fDisappearSeq = null;
		}
	}

	destroy()
	{
		this._fArrow_sprt && Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
		this._fRim_er && Sequence.destroy(Sequence.findByTarget(this._fRim_er));

		this._fBg_sprt = null;
		this._fBody_sprt = null;
		this._fArrow_sprt = null;
		this._fRim_er = null;
		this._fDisappearSeq = null;
		this._fIsAppearAnimationInProgress_bl = undefined;

		super.destroy();
	}
}

export default WeaponSidebarIconEmblemDefault;