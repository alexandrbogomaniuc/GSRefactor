import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

class AppearanceView extends Sprite
{
	static get EVENT_APPEARING_STARTED()					{ return "onBossModeAppearingStarted"; }
	static get EVENT_APPEARING_PRESENTATION_STARTED()		{ return "onBossModeAppearingPresentationStarted"; }
	static get EVENT_APPEARING_PRESENTATION_CULMINATED()	{ return "onBossModeAppearingPresentationCulminated"; }
	static get EVENT_APPEARING_PRESENTATION_COMPLETION()	{ return "onBossModeAppearingPresentationCompletion"; }
	static get EVENT_APPEARING_PRESENTATION_COMPLETED()		{ return "onBossModeAppearingPresentationCompleted"; }
	static get EVENT_ON_TIME_TO_START_CAPTION_ANIMATION() 	{ return "onTimeToStartCaptionAnimation"; }
	static get EVENT_SHAKE_THE_GROUND_REQUIRED() 			{ return "onShakeTheGroundRequired"; }

	startAppearing(aZombieView_e)
	{
		this._startAppearing(aZombieView_e);
	}

	interruptAnimation()
	{
		this._destroyAnimation();
	}

	//INIT...
	constructor()
	{
		super();

		this._fBossZombie_e = null;
		this._fYellowView_sprt = null;
		this._fTimer_t = null;
	}
	//...INIT

	//APPEARING PRESENTATION...
	_startAppearing(aZombieView_e)
	{
		this._fBossZombie_e = aZombieView_e;

		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = new Timer(this._onAppearingIntroTime.bind(this), this._appearingIntroDelay);

		this.emit(AppearanceView.EVENT_APPEARING_STARTED);
	}

	_onAppearingIntroTime()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;

		this._playAppearingAnimation();
	}

	_playAppearingAnimation()
	{
		this._onTimeToStartCaptionAnimation();
		
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = new Timer(this._onAppearingCulminated.bind(this), this._appearingCulminationTime);

		this.visible = true;
	}

	_onAppearingCulminated()
	{
		this._fBossZombie_e && this._fBossZombie_e.showBossAppearance(this._bossAppearanceSequences, this._bossAppearanceInit);

		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = new Timer(this._onAppearingCompletionTime.bind(this), this._completionDelay);
		this._startYellowScreenAnimation();
	}

	_onAppearingCompletionTime()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;

		this._fBossZombie_e && this._fBossZombie_e.resetBossAppearance();
	}

	_onAppearingCompleted()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;

		this.visible = false;
		this.emit(AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETED);

		this.interruptAnimation();
	}

	_startYellowScreenAnimation()
	{
		this._fYellowView_sprt = this.addChild(APP.library.getSpriteFromAtlas('boss_mode/yellow_screen'));
		this._fYellowView_sprt.scale.set(3.90625*1.108);
		this._fYellowView_sprt.alpha = 0;
		this._fYellowView_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		let lAlphaSeq = [
			{ tweens: [], 	duration: this.yellowScreenDelay_num * FRAME_RATE},
			{ tweens: [{ prop: "alpha", to: 1 }], 	duration: 7 * FRAME_RATE},
			{ tweens: [], 	duration: 40 * FRAME_RATE},
			{ tweens: [{ prop: "alpha", to: 0 }], 	duration: 24 * FRAME_RATE}
		];
		let lScaleSeq = [
			{ tweens: [], duration: this.yellowScreenDelay_num * FRAME_RATE},
			{ tweens: [{ prop: "scale.x", to: 3.90625*1.13 }, { prop: "scale.y", to: 3.90625*1.13 }], duration: 10 * FRAME_RATE},
			{ tweens: [{ prop: "scale.x", to:  3.90625*1.03 }, { prop: "scale.y", to: 3.90625*1.03 }], 	duration: 30 * FRAME_RATE}
		];

		Sequence.start(this._fYellowView_sprt, lAlphaSeq);
		Sequence.start(this._fYellowView_sprt, lScaleSeq);
	}

	get yellowScreenDelay_num()
	{
		let lDelay_num = 5;
		if (this._fBossZombie_e.name) switch (this._fBossZombie_e.name)
		{
			case 'SpiderBoss':
				lDelay_num = 9;
				break;
			case 'ApeBoss':
				lDelay_num = 20;
				break;
			case 'GolemBoss':
				lDelay_num = 46;
				break;
		}
		return lDelay_num;
	}
	//...APPEARING PRESENTATION

	_destroyAnimation()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;

		this._fBossZombie_e = null;
		Sequence.destroy(Sequence.findByTarget(this._fYellowView_sprt));
		this._fYellowView_sprt = null;
	}

	destroy()
	{
		this._destroyAnimation();
		super.destroy();

		this._fBossZombie_e = null;

		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;
	}
}

export default AppearanceView;