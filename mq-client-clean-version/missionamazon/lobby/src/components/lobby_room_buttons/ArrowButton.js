import CommonButton from '../../ui/CommonButton';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class ArrowButton extends CommonButton {

	constructor()
	{
		super("lobby/rooms_arrow", null, true, undefined, true);

		this._fIsGlowable_bl = undefined;
		this._fGlow_sprt = null;

		this._init();
	}

	_init()
	{
		this._fGlow_sprt = this.addChild(APP.library.getSprite("lobby/rooms_arrow_glow"));
		this._fGlow_sprt.position.set(15,-1);
		this._fGlow_sprt.alpha = 0;
		this._fGlow_sprt.scale.x = this._baseView.scale.x;
		this._fGlow_sprt.scale.y = this._baseView.scale.y;
		this._fGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		this._validateGlow();
	}

	_startGlow()
	{
		this._stopGlow();
		this._animate();
	}

	_stopGlow()
	{
		this._destroySequences();
		this._fGlow_sprt && (this._fGlow_sprt.alpha = 0);
	}

	_animate()
	{
		this._destroySequences();

		let lDelay_num = 0;
		let lGlowSeq_arr = [
			{tweens: [],						duration: lDelay_num*FRAME_RATE},
			{tweens: [{prop: 'alpha', to: 1}],	duration: 20*FRAME_RATE, ease: Easing.sine.easeIn},
			{tweens: [],						duration: 10*FRAME_RATE},
			{tweens: [{prop: 'alpha', to: 0}],	duration: 20*FRAME_RATE, ease: Easing.sine.easeOut},
			{tweens: [], duration: 30*FRAME_RATE, onfinish:  () => this._animate()}
		];

		Sequence.start(this._fGlow_sprt, lGlowSeq_arr);

	}

	_destroySequences()
	{
		if (this._fGlow_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fGlow_sprt));
		}
	}

	setGlowable(aValue_bl)
	{
		if (this._fIsGlowable_bl !== aValue_bl)
		{
			this._fIsGlowable_bl = aValue_bl;
			this._validateGlow();
		}
	}

	_validateGlow()
	{
		if (this._enabled && this._fIsGlowable_bl)
		{
			this._startGlow();
		}
		else
		{
			this._stopGlow();
		}
	}

	setEnabled()
	{
		super.setEnabled();
		this._validateGlow();
	}

	setDisabled()
	{
		super.setDisabled();
		this._validateGlow();
	}

	destroy()
	{
		this._stopGlow();
		this._destroySequences();

		this._fGlow_sprt = null;

		super.destroy();
	}
}

export default ArrowButton;