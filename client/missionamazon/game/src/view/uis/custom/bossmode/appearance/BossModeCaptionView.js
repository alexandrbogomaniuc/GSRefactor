import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

class BossModeCaptionView extends Sprite
{
	static get EVENT_ON_ANIMATION_STARTED()				{return "onCaptionAnimationStarted";}
	static get EVENT_ON_CAPTION_APPEARING_STARTED()		{return "EVENT_ON_CAPTION_APPEARING_STARTED";}

	playAnimation(bossId, startDelay)
	{
		this._playAnimation(bossId, startDelay);
	}

	forceDisappear()
	{
		this._forceDisappear();
	}

	//INIT...
	constructor()
	{
		super();

		this._fCaptionContainer_sprt = null;
		this._fCaption_sprt = null;
		this._fCaptionSmoke_sprt = null;
		this._fIsForceDisappearRequired_bl = false;

		this._fStartTimer_t = null;
	}

	//...INIT

	_playAnimation(bossId, startDelay)
	{
		if (startDelay == undefined)
		{
			startDelay = this._startDelay;
		}

		this._fCaptionContainer_sprt = this.addChild(new Sprite());
		this._fCaptionContainer_sprt.alpha = 0;

		this._fCaptionSmoke_sprt = this._fCaptionContainer_sprt.addChild(I18.generateNewCTranslatableAsset(`TABossMode${bossId}SmokeLabel`));
		this._fCaptionSmoke_sprt.assetContent.blendMode = PIXI.BLEND_MODES.ADD;

		this._fCaption_sprt = this._fCaptionContainer_sprt.addChild(new Sprite());
		let captionGlow = this._fCaption_sprt.addChild(I18.generateNewCTranslatableAsset(`TABossMode${bossId}GlowLabel`));
		captionGlow.assetContent.blendMode = PIXI.BLEND_MODES.ADD;
		this._fCaption_sprt.addChild(I18.generateNewCTranslatableAsset(`TABossMode${bossId}Label`));
		this._fCaption_sprt.alpha = 0;

		let lAlphaSeq_arr = [
			{ tweens:[{prop:"alpha", to:1}], duration:8*FRAME_RATE },
			{ tweens:[], duration:58*FRAME_RATE },
			{ tweens:[{prop:"alpha", to:0}], duration:29*FRAME_RATE, onfinish: this._onCaptionAnimationCompleted.bind(this) }
		];
		Sequence.start(this._fCaptionContainer_sprt, lAlphaSeq_arr, startDelay);

		this._fStartTimer_t && this._fStartTimer_t.destructor();
		this._fStartTimer_t = new Timer(this._onAnimationStarted.bind(this), startDelay);
	}

	_onAnimationStarted()
	{
		this.emit(BossModeCaptionView.EVENT_ON_ANIMATION_STARTED);

		let lMoveSeq_arr = [
			{ tweens:[{prop:"y", to:-50}], duration:95*FRAME_RATE }
		];
		Sequence.start(this._fCaptionContainer_sprt, lMoveSeq_arr);

		this._startCaptionTextSequence();
	}

	get _startDelay()
	{
		return 128*FRAME_RATE;
	}

	_startCaptionTextSequence()
	{
		let lAlphaSeq_arr = [
			{ tweens:[], duration:4*FRAME_RATE, onfinish: () => { this.emit(BossModeCaptionView.EVENT_ON_CAPTION_APPEARING_STARTED); } },
			{ tweens:[{prop:"alpha", to:1}], duration:19*FRAME_RATE },
			{ tweens:[], duration:32*FRAME_RATE },
			{ tweens:[{prop:"alpha", to:0}], duration:11*FRAME_RATE }
		];

		Sequence.start(this._fCaption_sprt, lAlphaSeq_arr);
	}

	_onCaptionAnimationCompleted()
	{
		this.destroy();
	}

	_forceDisappear()
	{
		if (this._fIsDisappearStarted_bl)
		{
			return;
		}

		if (this._sweep)
		{
			this._startDisappear();
			return;
		}

		this._fIsForceDisappearRequired_bl = true;
	}

	destroy()
	{
		this._fStartTimer_t && this._fStartTimer_t.destructor();
		this._fStartTimer_t = null;

		this._fCaptionSmoke_sprt && this._fCaptionSmoke_sprt.destroy();
		this._fCaptionSmoke_sprt = null;
		this._fIsForceDisappearRequired_bl = undefined;

		if (this._fCaption_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fCaption_sprt));
			this._fCaption_sprt.destroy();
			this._fCaption_sprt = null;
		}

		if (this._fCaptionContainer_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fCaptionContainer_sprt));
			this._fCaptionContainer_sprt.destroy();
			this._fCaptionContainer_sprt = null;
		}

		super.destroy();
	}
}

export default BossModeCaptionView;