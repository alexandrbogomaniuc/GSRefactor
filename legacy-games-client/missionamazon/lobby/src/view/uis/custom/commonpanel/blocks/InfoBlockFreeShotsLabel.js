import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import * as Easing from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

class InfoBlockFreeShotsLabel extends Sprite {

	//override
	show()
	{
		if (this.visible && !this._fIsDisappearing_bl) return;
		super.show();
		this._startFlareAnimationTimer();
		this._appear();
	}

	//override
	hide()
	{
		if (!this.visible) return;
		this._stopFlareAnimationTimer();
		this._disappear();
	}

	i_updateFreeShots(aFreeShots_int)
	{
		this._updateFreeShots(aFreeShots_int);
	}

	get translatableAssetName()
	{
		return APP.isMobile ? "TACommonPanelFreeShotsLabelMobile" : "TACommonPanelFreeShotsLabel";
	}

	constructor()
	{
		super();

		this._fTextField_tf = null;
		this._fBaseText_tf = null;
		this._fCurrentFreeShots_int = undefined;

		this._fFlare_sprt = null;
		this._fFlareAnimationTimer_tmr = null;

		this.visible = false;

		this._fIsDisappearing_bl = false;

		this._init();
	}

	_init()
	{
		let lAssetDescriptor_tad = I18.getTranslatableAssetDescriptor(this.translatableAssetName);
		this._fTextField_tf = this.addChild(I18.generateNewCTranslatableAsset(this.translatableAssetName));

		this._fBaseText_str = lAssetDescriptor_tad.textDescriptor.content.text;
	}

	_updateFreeShots(aFreeShots_int)
	{
		this._fTextField_tf.text = this._fBaseText_str.replace("/VALUE/", aFreeShots_int);
		if (this._fCurrentFreeShots_int != aFreeShots_int && this.visible)
		{
			this._fCurrentFreeShots_int = aFreeShots_int;
			this._animate();
		}
	}

	get _flare()
	{
		return this._fFlare_sprt || (this._fFlare_sprt = this._initFlare());
	}

	_initFlare()
	{
		let l_sprt = this.addChild(APP.library.getSprite("common_panel/flare"));
		l_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		l_sprt.scale.set(0);
		return l_sprt;
	}

	_startFlareAnimationTimer()
	{
		this._fFlareAnimationTimer_tmr = new Timer(() => {this._showFlare()}, (90 + Math.random() * 60) * FRAME_RATE);
	}

	_stopFlareAnimationTimer()
	{
		this._resetFlareSequence();
		this._fFlareAnimationTimer_tmr && this._fFlareAnimationTimer_tmr.destructor();
		this._fFlareAnimationTimer_tmr = null;
	}

	_showFlare()
	{
		this._resetFlareSequence();

		let lTextBounds_obj = this._fTextField_tf.getLocalBounds();
		this._flare.position.set(lTextBounds_obj.x + lTextBounds_obj.width -4, lTextBounds_obj.y + lTextBounds_obj.height/2 -4);

		this._flare.scale.set(0);

		let scaleSeq = [
			{
				tweens: [
					{prop: "scale.x", to: 0.155 * 2},
					{prop: "scale.y", to: 0.155 * 2}
				],
				duration: 8 * FRAME_RATE
			},
			{
				tweens: [],
				duration: 13 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 0},
					{prop: "scale.y", to: 0}
				],
				duration: 8 * FRAME_RATE,
				onfinish: () => {
					this._startFlareAnimationTimer();
				}
			}
		];
		Sequence.start(this._flare, scaleSeq);

		let rotationSeq = [
			{
				tweens: [
					{prop: "rotation", to: this._flare.rotation + Utils.gradToRad(102)}
				],
				duration:  34 * FRAME_RATE
			}
		];
		Sequence.start(this._flare, rotationSeq);
	}

	_resetFlareSequence()
	{
		Sequence.destroy(Sequence.findByTarget(this._flare));
	}

	_resetScaleSequences()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		if (this._fIsDisappearing_bl)
		{
			this.visible = false;
			this._clear();
		}
	}

	_animate()
	{
		this._resetScaleSequences();
		let scaleSeq = [
			{
				tweens: [
					{prop: "scale.x", to: 1.15},
					{prop: "scale.y", to: 1.15}
				],
				duration: 4 * FRAME_RATE,
				ease: Easing.quadratic.easeIn
			},
			{
				tweens: [
					{prop: "scale.x", to: 0.97},
					{prop: "scale.y", to: 0.97}
				],
				duration: 4 * FRAME_RATE,
				ease: Easing.quadratic.easeInOut
			},
			{
				tweens: [
					{prop: "scale.x", to: 1},
					{prop: "scale.y", to: 1}
				],
				duration: 4 * FRAME_RATE,
				ease: Easing.quadratic.easeOut
			}
		];
		Sequence.start(this, scaleSeq);
	}

	_appear()
	{
		this._fIsDisappearing_bl = false;
		this._resetScaleSequences();
		this.scale.set(0);
		this._animate();
	}

	_disappear()
	{
		if (this._fIsDisappearing_bl) return;

		this._resetScaleSequences();
		this._fIsDisappearing_bl = true;

		let scaleSeq = [
			{
				tweens: [
					{prop: "scale.x", to: 1.1},
					{prop: "scale.y", to: 1.1}
				],
				duration: 4 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 0},
					{prop: "scale.y", to: 0}
				],
				duration: 5 * FRAME_RATE,
				ease: Easing.quadratic.easeIn,
				onfinish: () => {
					this.visible = false;
					this._clear();
				}
			}
		];
		Sequence.start(this, scaleSeq);
	}

	_clear()
	{
		this._fCurrentFreeShots_int = 0;
		this._fIsDisappearing_bl = false;
	}
}

export default InfoBlockFreeShotsLabel;
