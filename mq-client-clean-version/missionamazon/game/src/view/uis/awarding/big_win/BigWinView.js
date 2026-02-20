import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

import BigWinCoinsAward from './BigWinCoinsAward';

import MegaWinAnimation from './MegaWinAnimation';
import HugeWinAnimation from './HugeWinAnimation';
import BigWinAnimation from './BigWinAnimation';

class BigWinView extends SimpleUIView {

	static get EVENT_ON_ANIMATION_COMPLETED() 	{ return "EVENT_ON_ANIMATION_COMPLETED"; }
	static get EVENT_ON_ANIMATION_INTERRUPTED() { return "EVENT_ON_ANIMATION_INTERRUPTED"; }
	static get EVENT_ON_COIN_LANDED() 			{ return BigWinCoinsAward.EVENT_ON_COIN_LANDED; }
	static get EVENT_ON_BIG_WIN_AWARD_COUNTED() { return "EVENT_ON_BIG_WIN_AWARD_COUNTED"; }
	static get EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING() { return "EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING";}
	static get EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING() { return "EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING";}
	
	static get EVENT_BIG_WIN_PRESETNTATION_STARTED() { return "EVENT_BIG_WIN_PRESETNTATION_STARTED"; }

	i_startAnimation()
	{
		this._startAnimation();
	}

	i_interrupt()
	{
		this._interrupt();
	}

	constructor(aParentContainer_sprt)
	{
		super();
		this._fParentContainer_sprt = aParentContainer_sprt;
		this._fBigWinAnimation_bwa = null;
		this._fBigWinCoinsAward_bwca = null;

		this._fIsBigWinCoinsAwardCompleted_bl = false;
		this._fIsBigWinAnimationCompleted_bl = false;
		this._fIsFadeBackBGSoundEmitted_bl = false;
		this._fIsMuteBgSoundEmitted_bl = false;
	}

	__init()
	{
		super.__init();
	}

	get _screenCenterPoint()
	{
		return {x: APP.config.size.width/4, y: APP.config.size.height/4}
	}

	get _masterCoinsLandingPoint()
	{
		return APP.currentWindow.gameField.getMasterCoinsLandingPosition();
	}

	_startAnimation()
	{
		this._fParentContainer_sprt.addChild(this);

		const lTotalWin_num = this.uiInfo.totalWin;

		if (this.uiInfo.isMegaWin)
		{
			this._fBigWinAnimation_bwa = this.addChild(new MegaWinAnimation(lTotalWin_num));
			this.emit(BigWinView.EVENT_BIG_WIN_PRESETNTATION_STARTED, {bigWinTypeId: "MEGA"});
		}
		else if (this.uiInfo.isHugeWin)
		{
			this._fBigWinAnimation_bwa = this.addChild(new HugeWinAnimation(lTotalWin_num));
			this.emit(BigWinView.EVENT_BIG_WIN_PRESETNTATION_STARTED, {bigWinTypeId: "HUGE"});
		}
		else if(this.uiInfo.isBigWin)
		{
			this._fBigWinAnimation_bwa = this.addChild(new BigWinAnimation(lTotalWin_num));
			this.emit(BigWinView.EVENT_BIG_WIN_PRESETNTATION_STARTED, {bigWinTypeId: "BIG"});
		}
		
		this._fIsMuteBgSoundEmitted_bl = true;
		this.emit(BigWinView.EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING);
		
		this._fBigWinAnimation_bwa.position.set(APP.config.size.width/2, APP.config.size.height/2);
		this._fBigWinAnimation_bwa.once(BigWinAnimation.EVENT_ON_BIG_WIN_ANIMATION_COMPLETED, this._onBigWinAnimationCompleted, this);
		this._fBigWinAnimation_bwa.once(BigWinAnimation.EVENT_ON_BIG_WIN_COINS_REQUIRED, this._onBigWinCoinsRequired, this);
		this._fBigWinAnimation_bwa.startAnimation();
	}

	_onBigWinAnimationCompleted()
	{
		this._fIsBigWinAnimationCompleted_bl = true;
		this._validateCompletion();
	}

	_onBigWinCoinsRequired()
	{
		this._startFinalCoinsAnimation();
	}

	_startFinalCoinsAnimation()
	{
		this._fadeBackBGSoundSuspicision();

		let lFinishPos_obj = this._masterCoinsLandingPoint;
		let lOrigPos_obj = null;
		let lStartPos_obj = this._screenCenterPoint;

		this._fBigWinCoinsAward_bwca = this.addChild(new BigWinCoinsAward(this.uiInfo.totalWin, lStartPos_obj, lFinishPos_obj, lOrigPos_obj));
		this._fBigWinCoinsAward_bwca.on(BigWinCoinsAward.EVENT_ON_COIN_LANDED, this.emit, this);
		this._fBigWinCoinsAward_bwca.on(BigWinCoinsAward.EVENT_AWARDING_COMPLETED, this._onBigWinCoinsAwardCompleted, this);
		this._fBigWinCoinsAward_bwca.i_startAnimation(this._coinsNumber);
	}

	_onBigWinCoinsAwardCompleted()
	{
		this._fIsBigWinCoinsAwardCompleted_bl = true;
		this.emit(BigWinView.EVENT_ON_BIG_WIN_AWARD_COUNTED, {money: this.uiInfo.totalWin});
		this._validateCompletion();
	}

	_validateCompletion()
	{
		if (this._fIsBigWinAnimationCompleted_bl && this._fIsBigWinCoinsAwardCompleted_bl)
		{
			this.emit(BigWinView.EVENT_ON_ANIMATION_COMPLETED);
		}
	}

	_interrupt()
	{
		this._fadeBackBGSoundSuspicision();

		if (this._fBigWinCoinsAward_bwca)
		{
			this._fBigWinCoinsAward_bwca.destroy();
			this._fBigWinCoinsAward_bwca = null;
		}

		if (this._fBigWinAnimation_bwa)
		{
			this._fBigWinAnimation_bwa.destroy();
			this._fBigWinAnimation_bwa = null;
		}
	}

	_fadeBackBGSoundSuspicision()
	{
		if (this._fIsMuteBgSoundEmitted_bl && !this._fIsFadeBackBGSoundEmitted_bl)
		{
			this._fIsFadeBackBGSoundEmitted_bl = true;
			this.emit(BigWinView.EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING);
		}
	}

	get _coinsNumber()
	{
		if (this.uiInfo.isMegaWin) return 40;
		return 20;
	}

	destroy()
	{
		super.destroy();
	}
}

export default BigWinView;