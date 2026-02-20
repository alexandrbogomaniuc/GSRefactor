import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import BossModeView from '../../../../view/uis/custom/bossmode/BossModeView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';
import { ENEMY_TYPES } from '../../../../../../shared/src/CommonConstants';
import BigWinsController from '../../awarding/big_win/BigWinsController';
import { HIT_RESULT_SINGLE_CASH_ID } from '../../prizes/PrizesController';

class BossModeController extends SimpleUIController
{
	static get EVENT_APPEARING_STARTED() 					{return BossModeView.EVENT_APPEARING_STARTED;}
	static get EVENT_APPEARING_PRESENTATION_STARTED() 		{return BossModeView.EVENT_APPEARING_PRESENTATION_STARTED;}
	static get EVENT_APPEARING_PRESENTATION_CULMINATED() 	{return BossModeView.EVENT_APPEARING_PRESENTATION_CULMINATED;}
	static get EVENT_APPEARING_PRESENTATION_COMPLETION() 	{return BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETION;}
	static get EVENT_APPEARING_PRESENTATION_COMPLETED() 	{return BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETED;}
	static get EVENT_DISAPPEARING_PRESENTATION_STARTED() 	{return BossModeView.EVENT_DISAPPEARING_PRESENTATION_STARTED;}
	static get EVENT_DISAPPEARING_PRESENTATION_COMPLETION() {return BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETION;}
	static get EVENT_DISAPPEARING_PRESENTATION_COMPLETED() 	{return BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETED;}
	static get EVENT_ON_CAPTION_ANIMATION_STARTED()			{return BossModeView.EVENT_ON_CAPTION_ANIMATION_STARTED;}
	static get EVENT_ON_CAPTION_APPEARING_STARTED()			{return BossModeView.EVENT_ON_CAPTION_APPEARING_STARTED;}
	static get EVENT_SHAKE_THE_GROUND_REQUIRED() 			{return BossModeView.EVENT_SHAKE_THE_GROUND_REQUIRED;}
	static get EVENT_ON_BOSS_WIN_MULTIPLIER_LANDED()		{return BossModeView.EVENT_ON_BOSS_WIN_MULTIPLIER_LANDED;}
	
	//INIT...
	__init()
	{
		super.__init();
	}

	//INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		this._gameScreen = APP.gameScreen;
		this._gameScreen.on(GameScreen.EVENT_ON_NEW_BOSS_CREATED, this._onNewBossCreated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_BOSS_DESTROYING, this._onBossDestroying, this);
		this._gameScreen.on(GameScreen.EVENT_ON_BOSS_DESTROYED, this._onBossDestroyed, this);
		this._gameScreen.on(GameScreen.EVENT_ON_TIME_TO_EXPLODE_COINS, this._onTimeToExplodeCoins, this);
		this._gameScreen.on(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
		this._gameScreen.on(GameScreen.EVENT_ON_UPDATE_PLAYER_WIN_CAPTION, this._onUpdatePlayerWinCaption, this);

		let lBigWinsController_bwc = this._gameScreen.bigWinsController;
		lBigWinsController_bwc.on(BigWinsController.EVENT_BIG_WIN_PRESETNTATION_STARTED, this._onSomeBigWinPresentationStarted, this);

		this._gameScreen.on(GameScreen.EVENT_ON_READY, this._onGameReady, this);

		this._fBossKillerSeatId = null;
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_bmv = this.view;

		lView_bmv.on(BossModeView.EVENT_APPEARING_STARTED, this._onAppearingPresentationStarted, this);
		lView_bmv.on(BossModeView.EVENT_APPEARING_PRESENTATION_STARTED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_APPEARING_PRESENTATION_CULMINATED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETION, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_ON_CAPTION_ANIMATION_STARTED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_ON_CAPTION_APPEARING_STARTED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_DISAPPEARING_PRESENTATION_STARTED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETION, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_ON_BOSS_WIN_MULTIPLIER_LANDED, this.emit, this);
		lView_bmv.on(BossModeView.EVENT_SHAKE_THE_GROUND_REQUIRED, this.emit, this);
	}
	//...INIT

	get bossType()
	{
		return this.view.bossType;
	}

	get bossKillerSeatId()
	{
		return this._fBossKillerSeatId;
	}

	_onNewBossCreated(event)
	{
		let lView_bmv = this.view;
		let zombie = this._gameScreen.gameField.getExistEnemy(event.enemyId);

		if (!zombie)
		{
			throw new Error(`No Boss enemy with id = ${event.enemyId} found on the screen`);
		}

		let isLasthandBossView = event.isLasthandBossView;
		//DEBUG...
		//isLasthandBossView = false;
		//...DEBUG
		if (isLasthandBossView)
		{
			lView_bmv.updateBossType(zombie.name);
			return;
		}

		lView_bmv.addToContainerIfRequired(this._gameScreen.gameField.bossModeAppearingContainerInfo);
		lView_bmv.startAppearing(zombie);
	}

	_onSomeBigWinPresentationStarted()
	{
		if (this.view && this.view.isKeepPlayingCaptionInProgress)
		{
			this.view.forceCaptionDisappearing();
		}
	}
	
	_onAppearingPresentationStarted()
	{
		this.emit(BossModeController.EVENT_APPEARING_STARTED);
	}

	_onBossDestroying(event)
	{
		let lView_bmv = this.view;
		lView_bmv.addToContainerIfRequired(this._gameScreen.gameField.bossModeDisappearingContainerInfo);
		lView_bmv.startDisappearing(event.enemyGlobalPos);
	}

	_onBossDestroyed(event)
	{
		this.view.completeDisappearing(event.enemyGlobalPoint);
	}

	_onTimeToExplodeCoins(event)
	{
		this.view.onTimeToExplodeCoins(event.enemyGlobalPoint, event.isCoPlayerWin);
	}

	_onGameFieldCleared()
	{
		this.view && this.view.interruptAnimation();
	}

	i_isKilledBossWin(data)
	{
		return this._checkIfKilledBossWinMessage(data) !== false;
	}

	_checkIfKilledBossWinMessage(data)
	{
		let lTotalWin_num = 0;
		let lIsKilledBoss_bl = false;

		if (!data || !data.affectedEnemies)
		{
			return false;
		}

		for (let affectedEnemy of data.affectedEnemies)
		{
			if (affectedEnemy.data.class == "Hit")
			{
				const innerData = affectedEnemy.data;
				if (innerData.enemy.typeId == ENEMY_TYPES.BOSS)
				{
					let lAddWin_num = innerData.killBonusPay;
					if (lAddWin_num == 0 && innerData.hitResultBySeats && innerData.hitResultBySeats[innerData.seatId])
					{
						let lSeatWins = innerData.hitResultBySeats[innerData.seatId];
						for (let i=0; i<lSeatWins.length; i++)
						{
							if (lSeatWins[i].id == HIT_RESULT_SINGLE_CASH_ID)
							{
								lAddWin_num = +lSeatWins[i].value;
							}
						}
					}
					lTotalWin_num += lAddWin_num;
				}

				if (innerData.killed)
				{
					lIsKilledBoss_bl = true;
				}
			}
		}

		if (lTotalWin_num > 0 && lIsKilledBoss_bl)
		{
			return lTotalWin_num;
		}

		return false;
	}

	_onUpdatePlayerWinCaption(event)
	{
		this._fBossKillerSeatId = event.seatId;
		this.view.updatePlayerWinCaption(event.playerName, event.seatId);
	}

	_onGameReady()
	{
		// CoinsEmitter.fillPool();
		// CoinsEmitterSilver.fillPool();
	}

	destroy()
	{
		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_NEW_BOSS_CREATED, this._onNewBossCreated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_BOSS_DESTROYING, this._onBossDestroying, this);
			this._gameScreen.off(GameScreen.EVENT_ON_BOSS_DESTROYED, this._onBossDestroyed, this);
			this._gameScreen.off(GameScreen.EVENT_ON_TIME_TO_EXPLODE_COINS, this._onTimeToExplodeCoins, this);
			this._gameScreen.off(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
			this._gameScreen.off(GameScreen.EVENT_ON_READY, this._onGameReady, this);
			this._gameScreen = null;

			let lView_bmv = this.view;

			lView_bmv.off(BossModeView.EVENT_APPEARING_STARTED, this._onAppearingPresentationStarted, this);
			lView_bmv.off(BossModeView.EVENT_APPEARING_PRESENTATION_STARTED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_APPEARING_PRESENTATION_CULMINATED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETION, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_APPEARING_PRESENTATION_COMPLETED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_ON_CAPTION_ANIMATION_STARTED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_ON_CAPTION_APPEARING_STARTED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_DISAPPEARING_PRESENTATION_STARTED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETION, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_DISAPPEARING_PRESENTATION_COMPLETED, this.emit, this);
			lView_bmv.off(BossModeView.EVENT_SHAKE_THE_GROUND_REQUIRED, this.emit, this);
		}

		super.destroy();
	}
}

export default BossModeController