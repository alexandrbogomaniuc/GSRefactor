import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { GlowFilter, OutlineFilter} from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import SpineEnemy from './SpineEnemy';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';

class ScorpionEnemy extends SpineEnemy
{
	constructor(params)
	{
		super(params);
	}

	_addSpecialEffectsIfRequired()
	{
		this._fInnerRing_sprt = null;
		this._fOuterRing_sprt = null;
		this._fGroundFlare_sprt = null;
		this._fOuterGlow_f = null;
		this._fInnerGlow_f = null;

		this._startGlowRingAnimation();
		this._addGroundFlare();
	}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		if (!this._fIsFrozen_bl)
		{
			this._interruptAnimation();
			this._fInnerRing_sprt.visible = false;
			this._fOuterRing_sprt.visible = false;
			this._fGroundFlare_sprt.visible = false;
		}
		this.spineView.filters = [];
		super._freeze(aIsAnimated_bl);
	}

	//override
	_unfreeze(aIsAnimated_bl = true)
	{
		if (this._fIsFrozen_bl)
		{
			this._startNextCircles();
			this._startNextFlareRotation();
		}
		this._fInnerRing_sprt.visible = true;
		this._fOuterRing_sprt.visible = true;
		this._fGroundFlare_sprt.visible = true;
		this._addShineFilters();
		super._unfreeze(aIsAnimated_bl);
	}

	//override
	_onSpineViewGenerated()
	{
		this._addShineFilters();
	}
	
	//override
	_setDefaultFilters()
	{
		if(this.spineView && !this._fIsFrozen_bl)
		{
			this._addShineFilters();
		}
	}

	_addShineFilters()
	{
		if (APP.isDeprecetedInternetExplorer)
		{
			this._fOuterGlow_f = new OutlineFilter(2, 0xf3c867);
			this._fOuterGlow_f.resolution = 2;
			this.spineView.filters = [this._fOuterGlow_f];
		}
		else
		{
			this._fOuterGlow_f =  new GlowFilter({distance: 4, outerStrength: 2, innerStrength: 0, color: 0xf3c867, quality: 0.5});
			this._fInnerGlow_f = new GlowFilter({distance: 6, outerStrength: 0, innerStrength: 0.8, color: 0xf3b048, quality: 1});
			this._fOuterGlow_f.resolution = 2;
			this._fInnerGlow_f.resolution = 2;
			this.spineView.filters = [this._fOuterGlow_f, this._fInnerGlow_f];
		}
	}

	_startGlowRingAnimation()
	{
		this._fInnerRing_sprt = this.container.addChild(APP.library.getSprite("enemies/scorpion/fx/glow_ring"));
		this._fInnerRing_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		this._fOuterRing_sprt = this.container.addChild(APP.library.getSprite("enemies/scorpion/fx/glow_ring"));
		this._fOuterRing_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		if (APP.isMobile)
		{
			let lCutAmount_num = 4;

			let lInnerBounds_obj = this._fInnerRing_sprt.getBounds();
			lInnerBounds_obj.width -= lCutAmount_num;
			lInnerBounds_obj.height -= lCutAmount_num;
			let lInnerMask_gr = new PIXI.Graphics();
			lInnerMask_gr.beginFill().drawRect(-lInnerBounds_obj.width/2, -lInnerBounds_obj.height/2, lInnerBounds_obj.width, lInnerBounds_obj.height).endFill();
			this._fInnerRing_sprt.addChild(lInnerMask_gr);
			this._fInnerRing_sprt.mask = lInnerMask_gr;

			let lOuterBounds_obj = this._fOuterRing_sprt.getBounds();
			lOuterBounds_obj.width -= lCutAmount_num;
			lOuterBounds_obj.height -= lCutAmount_num;
			let lOuterMask_gr = new PIXI.Graphics();
			lOuterMask_gr.beginFill().drawRect(-lOuterBounds_obj.width/2, -lOuterBounds_obj.height/2, lOuterBounds_obj.width, lOuterBounds_obj.height).endFill();
			this._fOuterRing_sprt.addChild(lOuterMask_gr);
			this._fOuterRing_sprt.mask = lOuterMask_gr;
		}

		this._startNextCircles();
	}

	_startNextCircles()
	{
		this._fInnerRing_sprt.alpha = 0.7;
		this._fInnerRing_sprt.scale.set(0);
		this._fOuterRing_sprt.alpha = 0.7;
		this._fOuterRing_sprt.scale.set(0);

		let lInner_seq = [
			{tweens: [{prop: "alpha", to: 0.5},	{prop: "scale.x", to: 0.488},	{prop: "scale.y", to: 0.488}],	duration: 20*FRAME_RATE},
			{tweens: [{prop: "alpha", to: 0},	{prop: "scale.x", to: 0.927},	{prop: "scale.y", to: 0.927}],	duration: 18*FRAME_RATE},
			{tweens: [],																						duration: 29*FRAME_RATE, onfinish: ()=>{
				this._startNextCircles();
			}}
		];

		let lOuter_seq = [
			{tweens: [{prop: "alpha", to: 0.15},{prop: "scale.x", to: 0.1464},	{prop: "scale.y", to: 0.1464}],	duration: 6*FRAME_RATE, onfinish: ()=>{
				Sequence.start(this._fInnerRing_sprt, lInner_seq);
			}},
			{tweens: [{prop: "alpha", to: 0.5},	{prop: "scale.x", to: 0.488},	{prop: "scale.y", to: 0.488}],	duration: 14*FRAME_RATE},
			{tweens: [{prop: "alpha", to: 0},	{prop: "scale.x", to: 0.927},	{prop: "scale.y", to: 0.927}],	duration: 18*FRAME_RATE}
		];

		Sequence.start(this._fOuterRing_sprt, lOuter_seq);
	}

	_addGroundFlare()
	{
		this._fGroundFlare_sprt = this.container.addChild(APP.library.getSprite("enemies/scorpion/fx/ground_flare"));
		this._fGroundFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		if (APP.isMobile)
		{
			let lCutAmount_num = 4;

			let lGroundBounds_obj = this._fGroundFlare_sprt.getBounds();
			lGroundBounds_obj.width -= lCutAmount_num;
			lGroundBounds_obj.height -= lCutAmount_num;
			let lGroundMask_gr = new PIXI.Graphics();
			lGroundMask_gr.beginFill().drawRect(-lGroundBounds_obj.width/2, -lGroundBounds_obj.height/2, lGroundBounds_obj.width, lGroundBounds_obj.height).endFill();
			this._fGroundFlare_sprt.addChild(lGroundMask_gr);
			this._fGroundFlare_sprt.mask = lGroundMask_gr;
		}

		this._startNextFlareRotation();
	}

	_startNextFlareRotation()
	{
		let lRotation_seq = [
			{tweens: [{prop: "rotation", to: Math.PI*2}], duration: 108*FRAME_RATE, onfinish: ()=>{
				this._fGroundFlare_sprt.rotation = 0;
				this._startNextFlareRotation();
			}}
		];

		Sequence.start(this._fGroundFlare_sprt, lRotation_seq);
	}

	//override
	setDeathFramesAnimation(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		this._fInnerRing_sprt.visible = false;
		this._fOuterRing_sprt.visible = false;
		this._fGroundFlare_sprt.visible = false;
		super.setDeathFramesAnimation(aIsInstantKill_bl, aPlayerWin_obj);
	}

	_interruptAnimation()
	{
		this._fInnerRing_sprt && Sequence.destroy(Sequence.findByTarget(this._fInnerRing_sprt));
		this._fOuterRing_sprt && Sequence.destroy(Sequence.findByTarget(this._fOuterRing_sprt));
		this._fGroundFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fGroundFlare_sprt));
	}

	destroy(purely)
	{
		this._interruptAnimation();
		this._fInnerRing_sprt = null;
		this._fOuterRing_sprt = null;
		this._fGroundFlare_sprt = null;
		this._fOuterGlow_f = null;
		this._fInnerGlow_f = null;
		super.destroy(purely);
	}
}

export default ScorpionEnemy;