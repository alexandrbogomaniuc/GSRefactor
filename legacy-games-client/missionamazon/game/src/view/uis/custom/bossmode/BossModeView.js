import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { ENEMIES, FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import BossModeDisappearanceFxView from './death/BossModeDisappearanceFxView';
import SpiderAppearanceView from './appearance/SpiderAppearanceView';
import GolemAppearanceView from './appearance/GolemAppearanceView';
import AppearanceView from './appearance/AppearanceView';
import BossModeCaptionView from './appearance/BossModeCaptionView';
import ApeAppearanceView from './appearance/ApeAppearanceView';
import BossModePlayerWinAnimation from './death/BossModePlayerWinAnimation';
import YouWinAnimation from './death/YouWinAnimation';

class BossModeView extends SimpleUIView
{
	static get EVENT_APPEARING_STARTED()					{return AppearanceView.EVENT_APPEARING_STARTED;}
	static get EVENT_APPEARING_PRESENTATION_STARTED()		{return AppearanceView.EVENT_APPEARING_PRESENTATION_STARTED;}
	static get EVENT_APPEARING_PRESENTATION_CULMINATED()	{return AppearanceView.EVENT_APPEARING_PRESENTATION_CULMINATED;}
	static get EVENT_APPEARING_PRESENTATION_COMPLETION()	{return AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETION;}
	static get EVENT_APPEARING_PRESENTATION_COMPLETED()		{return AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETED;}
	static get EVENT_SHAKE_THE_GROUND_REQUIRED() 			{return AppearanceView.EVENT_SHAKE_THE_GROUND_REQUIRED;}

	static get EVENT_DISAPPEARING_PRESENTATION_STARTED()	{return "onDisappearingPresentationStarted";}
	static get EVENT_DISAPPEARING_PRESENTATION_COMPLETION()	{return "onDisappearingPresentationCompleteon";}
	static get EVENT_DISAPPEARING_PRESENTATION_COMPLETED()	{return "onDisappearingPresentationCompleted";}
	static get EVENT_ON_CAPTION_ANIMATION_STARTED()			{return BossModeCaptionView.EVENT_ON_ANIMATION_STARTED;}
	static get EVENT_ON_CAPTION_APPEARING_STARTED()			{return BossModeCaptionView.EVENT_ON_CAPTION_APPEARING_STARTED;}
	static get EVENT_ON_BOSS_WIN_MULTIPLIER_LANDED()		{return YouWinAnimation.EVENT_ON_YOU_WIN_MULTIPLIER_LANDED;}

	addToContainerIfRequired(aViewContainerInfo_obj)
	{
		this._addToContainerIfRequired(aViewContainerInfo_obj);
	}

	startAppearing(aZombieView_e)
	{
		this._startAppearing(aZombieView_e);
	}

	startDisappearing(bossGlobalPos)
	{
		this._startDisappearing(bossGlobalPos);
	}

	completeDisappearing(aZombiePosition_pt)
	{
		this._completeDisappearing(aZombiePosition_pt);
	}

	interruptAnimation()
	{
		this._destroyAnimations();
	}

	updateBossType(aEnemyName_str)
	{
		this._updateBossType(aEnemyName_str);
	}

	onTimeToExplodeCoins(aZombiePosition_pt, aIsCoPlayerWin_bln)
	{
		this._fIsCoPlayerWin_bl = aIsCoPlayerWin_bln;
		this._onTimeToExplodeCoins(aZombiePosition_pt, aIsCoPlayerWin_bln);
	}

	updatePlayerWinCaption(aPlayerName_str, aSeatId_int)
	{
		this._updatePlayerWinCaption(aPlayerName_str, aSeatId_int)
	}

	get bossType()
	{
		return this._fBossType_str;
	}

	forceCaptionDisappearing()
	{
		if (this._fCaptionView_bmcv)
		{
			this._fCaptionView_bmcv.forceDisappear();
		}
	}

	//INIT...
	constructor()
	{
		super();

		this._fViewContainerInfo_obj = null;
		this._fBossType_str = null;
		this._fCaptionView_bmcv = null;
		this._fAppearanceView_av = null;
		this._fDeathDisappearanceFxView_bmdfxv = null;
		this._fBossModePlayerWinAnimation_bmpwa = null;
		this._fIsCoPlayerWin_bl = null;
	}

	_initAppearing()
	{
		this._fAppearanceView_av && this._fAppearanceView_av.destroy();
		this._fAppearanceView_av = this.addChild(this._appearanceView);

		this._fAppearanceView_av.on(AppearanceView.EVENT_APPEARING_STARTED, this.emit, this);
		this._fAppearanceView_av.on(AppearanceView.EVENT_APPEARING_PRESENTATION_STARTED, this.emit, this);
		this._fAppearanceView_av.on(AppearanceView.EVENT_APPEARING_PRESENTATION_CULMINATED, this.emit, this);
		this._fAppearanceView_av.on(AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETION, this.emit, this);
		this._fAppearanceView_av.on(AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETED, this._onAppearingCompleted, this);
		this._fAppearanceView_av.on(AppearanceView.EVENT_SHAKE_THE_GROUND_REQUIRED, this.emit, this);
		this._fAppearanceView_av.once(AppearanceView.EVENT_ON_TIME_TO_START_CAPTION_ANIMATION, this._onTimeToStartCaptionAnimation, this);
	}

	get _appearanceView()
	{
		switch (this._fBossType_str)
		{
			case ENEMIES.SpiderBoss:
				return new SpiderAppearanceView(this._fViewContainerInfo_obj);
			case ENEMIES.GolemBoss:
				return new GolemAppearanceView(this._fViewContainerInfo_obj);
			case ENEMIES.ApeBoss:
				return new ApeAppearanceView(this._fViewContainerInfo_obj);
		}

		return undefined;
	}

	get _bossModePlayerWinAnimation()
	{
		return this._fBossModePlayerWinAnimation_bmpwa || this._initBossModePlayerWinAnimation();
	}

	_initBossModePlayerWinAnimation()
	{
		this._fBossModePlayerWinAnimation_bmpwa = this.addChild(new BossModePlayerWinAnimation());
		this._fBossModePlayerWinAnimation_bmpwa.zIndex = 1000;
		this._fBossModePlayerWinAnimation_bmpwa.position.y = -50;

		if(APP.isBattlegroundGame)
		{
			this._fBossModePlayerWinAnimation_bmpwa.on(BossModePlayerWinAnimation.ON_END_CUPTION_ANIMATION, this._startMultiplierCuptionAnimaton, this)
		}
		
		return this._fBossModePlayerWinAnimation_bmpwa;
	}
	//...INIT

	//APPEARING PRESENTATION...
	_startAppearing(aZombieView_e)
	{
		aZombieView_e && this._updateBossType(aZombieView_e.name);

		this._initAppearing();
		this._fAppearanceView_av.startAppearing(aZombieView_e);

		this.visible = true;
	}

	_onAppearingCompleted()
	{
		this.emit(BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETED);

		this._destroyAppearance();
	}
	//...APPEARING PRESENTATION

	//CAPTION...
	_onTimeToStartCaptionAnimation(aEvent_obj)
	{
		this._fCaptionView_bmcv && this._fCaptionView_bmcv.destroy();

		let lCaptionPosition_obj = aEvent_obj.captionPosition;
		let lStartDelay_num = aEvent_obj.startDelay;

		this._fCaptionView_bmcv = new BossModeCaptionView();

		let lViewContainer_sprt = this._fViewContainerInfo_obj.container;
		lViewContainer_sprt.addChild(this._fCaptionView_bmcv);
		this._fCaptionView_bmcv.zIndex = this._fViewContainerInfo_obj.captionZIndex;
		this._fCaptionView_bmcv.position.set(this.position.x + lCaptionPosition_obj.x, this.position.y + lCaptionPosition_obj.y);
		this._fCaptionView_bmcv.playAnimation(this.bossType, lStartDelay_num);

		this._fCaptionView_bmcv.once(BossModeCaptionView.EVENT_ON_ANIMATION_STARTED, this.emit, this);
		this._fCaptionView_bmcv.once(BossModeCaptionView.EVENT_ON_CAPTION_APPEARING_STARTED, this.emit, this);
	}
	//...CAPTION

	//DISAPPEARING PRESENTATION...
	_onTimeToExplodeCoins(aZombiePosition_pt, aIsCoPlayerWin_bln)
	{
		let lZombiePosition_pt = this.globalToLocal(aZombiePosition_pt.x, aZombiePosition_pt.y);

		if (!this._fDeathDisappearanceFxView_bmdfxv)
		{
			this._fDeathDisappearanceFxView_bmdfxv = this.addChild(this._generateDisappearanceFxViewInstance(aIsCoPlayerWin_bln));
		}

		this._fDeathDisappearanceFxView_bmdfxv.position.set(lZombiePosition_pt.x, lZombiePosition_pt.y);
		this._fDeathDisappearanceFxView_bmdfxv.startCoinsExplodeAnimation();


		this.visible = true;
	}
	_startMultiplierCuptionAnimaton()
	{
		let lYouWinAnim = this.addChild(new YouWinAnimation(this._fPayoutValue_num, !this._fIsCoPlayerWin_bl, this._fCurrentSeatId_int));

		// lYouWinAnim.once(YouWinAnimation.EVENT_ON_BIG_WIN_COINS_REQUIRED, this._onBigWinCoinsRequired, this);
		lYouWinAnim.once(YouWinAnimation.EVENT_ON_BIG_WIN_PAYOUT_APPEARED, this.emit, this);
		lYouWinAnim.once(YouWinAnimation.EVENT_ON_YOU_WIN_MULTIPLIER_LANDED, this.emit, this);

		lYouWinAnim.startAnimation();
	}
	_startDisappearing(bossGlobalPos)
	{
		this.emit(BossModeView.EVENT_DISAPPEARING_PRESENTATION_STARTED, {bossGlobalPos: bossGlobalPos});

		let completionTimer = this.bossType === ENEMIES.SpiderBoss ? 23*FRAME_RATE : 6*FRAME_RATE;
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = new Timer(() => { this.emit(BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETION) }, completionTimer);
	}

	_completeDisappearing(aOptZombiePosition_pt)
	{
		if (!aOptZombiePosition_pt)
		{
			this._onDisappearingCompleted(false);
			return;
		}

		let lZombiePosition_pt = this.globalToLocal(aOptZombiePosition_pt.x, aOptZombiePosition_pt.y);

		if (!this._fDeathDisappearanceFxView_bmdfxv)
		{
			this._fDeathDisappearanceFxView_bmdfxv = this.addChild(this._generateDisappearanceFxViewInstance());
			this._fDeathDisappearanceFxView_bmdfxv.position.set(lZombiePosition_pt.x, lZombiePosition_pt.y);
		}

		this._bossModePlayerWinAnimation.zIndex = 1000;
		this._bossModePlayerWinAnimation.startAnimation();
		
		this._fDeathDisappearanceFxView_bmdfxv.once(BossModeDisappearanceFxView.EVENT_ANIMATION_COMPLETED, this._onDisappearingCompleted, this);
		this._fDeathDisappearanceFxView_bmdfxv.startAnimation();

		this.visible = true;
	}

	_generateDisappearanceFxViewInstance(aIsCoPlayerWin_bln)
	{
		return new BossModeDisappearanceFxView(this._fBossType_str, aIsCoPlayerWin_bln);
	}

	_onDisappearingCompleted(aOptDestroyDisappearance_bl=true)
	{
		this.emit(BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETED);

		if (aOptDestroyDisappearance_bl)
		{
			this._destroyDisappearance();

			if (!this._fAppearanceView_av)
			{
				this._destroyAnimations();
				this.visible = false;
			}
		}
	}
	//...DISAPPEARING PRESENTATION

	_updateBossType(aEnemyName_str)
	{
		switch (aEnemyName_str)
		{
			case ENEMIES.SpiderBoss:
			case ENEMIES.GolemBoss:
			case ENEMIES.ApeBoss:
				this._fBossType_str = aEnemyName_str;
				break;
			default:
				throw new Error('Unexpected boss ' + aEnemyName_str);
		}
	}

	_updatePlayerWinCaption(aPlayerName_str, aSeatId_int)
	{
		this._bossModePlayerWinAnimation.playerName = aPlayerName_str;
		this._fCurrentSeatId_int = aSeatId_int;
	}

	_addToContainerIfRequired(aViewContainerInfo_obj)
	{
		if (this.parent)
		{
			let lViewZIndex_num = aViewContainerInfo_obj.zIndex;
			this.zIndex = lViewZIndex_num;
			this._fViewContainerInfo_obj = aViewContainerInfo_obj;

			return;
		}

		this._fViewContainerInfo_obj = aViewContainerInfo_obj;

		let lViewContainer_sprt = aViewContainerInfo_obj.container;
		let lViewZIndex_num = aViewContainerInfo_obj.zIndex;

		lViewContainer_sprt.addChild(this);

		this.position.set(480, 270);
		this.zIndex = lViewZIndex_num;
	}

	_destroyAppearance()
	{
		if (this._fAppearanceView_av)
		{
			this._fAppearanceView_av.off(AppearanceView.EVENT_APPEARING_STARTED, this.emit, this);
			this._fAppearanceView_av.off(AppearanceView.EVENT_APPEARING_PRESENTATION_STARTED, this.emit, this);
			this._fAppearanceView_av.off(AppearanceView.EVENT_APPEARING_PRESENTATION_CULMINATED, this.emit, this);
			this._fAppearanceView_av.off(AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETION, this.emit, this);
			this._fAppearanceView_av.off(AppearanceView.EVENT_APPEARING_PRESENTATION_COMPLETED, this._onAppearingCompleted, this);
			this._fAppearanceView_av.off(AppearanceView.EVENT_ON_TIME_TO_START_CAPTION_ANIMATION, this._onTimeToStartCaptionAnimation, this);
			this._fAppearanceView_av.off(AppearanceView.EVENT_SHAKE_THE_GROUND_REQUIRED, this.emit, this);

			this._fAppearanceView_av.destroy();
		}

		this._fAppearanceView_av = null;
	}

	_destroyDisappearance()
	{
		if (this._fDeathDisappearanceFxView_bmdfxv)
		{
			this._fDeathDisappearanceFxView_bmdfxv.off(BossModeDisappearanceFxView.EVENT_ANIMATION_COMPLETED, this._onDisappearingCompleted, this);
			this._fDeathDisappearanceFxView_bmdfxv.destroy();
		}

		this._fDeathDisappearanceFxView_bmdfxv = null;
	}

	_destroyCaption()
	{
		if (this._fCaptionView_bmcv)
		{
			this._fCaptionView_bmcv.off(BossModeCaptionView.EVENT_ON_ANIMATION_STARTED, this._onCaptionAnimationStarted, this);
			this._fCaptionView_bmcv.destroy();
		}

		this._fCaptionView_bmcv = null;
	}

	_destroyAnimations()
	{
		this._destroyAppearance();
		this._destroyDisappearance();
		this._destroyCaption();
		this._fBossModePlayerWinAnimation_bmpwa && this._fBossModePlayerWinAnimation_bmpwa.destroy();
		this._fBossModePlayerWinAnimation_bmpwa = null;

		this.parent && this.parent.removeChild(this);

		this._fBossType_str = undefined;
	}

	destroy()
	{
		this._destroyAnimations();

		super.destroy();

		this._fViewContainerInfo_obj = null;
		this._fBossType_str = null;
	}
}

export default BossModeView;