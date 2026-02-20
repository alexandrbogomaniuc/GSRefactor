import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import BigWinsInfo from '../../../../model/uis/awarding/big_win/BigWinsInfo';
import BigWinsView from '../../../../view/uis/awarding/big_win/BigWinsView';
import BigWinController from './BigWinController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';
import { BIG_WIN_PAYOUT_RATIOS } from '../../../../model/uis/awarding/big_win/BigWinInfo';
import GameField from '../../../../main/GameField';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { ENEMY_TYPES, BATTLEGROUND_STAKE_MULTIPLIER, isPowerUpWeapons } from '../../../../../../shared/src/CommonConstants';

const SKIP_DELAY = 1200;

class BigWinsController extends SimpleUIController
{
	static get EVENT_ON_COIN_LANDED() 				{ return BigWinController.EVENT_ON_COIN_LANDED; }
	static get EVENT_ON_BIG_WIN_AWARD_COUNTED() 	{ return BigWinController.EVENT_ON_BIG_WIN_AWARD_COUNTED; }
	static get EVENT_ON_ANIMATION_INTERRUPTED() 	{ return BigWinController.EVENT_ON_ANIMATION_INTERRUPTED; }
	static get EVENT_ON_ALL_ANIMATIONS_COMPLETED() 	{ return "EVENT_ON_ALL_ANIMATIONS_COMPLETED"; }
	static get EVENT_ON_BIG_WIN_STARTED()			{ return "EVENT_ON_BIG_WIN_STARTED"; }
	static get EVENT_ON_PENDING_BIG_WINS_SKIPPED()	{ return "EVENT_ON_PENDING_BIG_WINS_SKIPPED"; }
	static get EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING() { return BigWinController.EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING;}
	static get EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING() { return BigWinController.EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING;}	

	static get EVENT_BIG_WIN_PRESETNTATION_STARTED() { return BigWinController.EVENT_BIG_WIN_PRESETNTATION_STARTED; }

	i_isBigWin(data)
	{
		return this._checkIfBigWinAnimationNeeded(data) !== false;
	}

	get bigWinsContainerInfo()
	{
		return this._gameScreen.gameField.bigWinsContainerInfo;
	}

	get isAnyBigWinInProgress()
	{
		return this.isAnyBigWinAnimationInProgress 
				|| (this._fPendingAnimBigWinValues_num_arr && this._fPendingAnimBigWinValues_num_arr.length)
	}

	get isAnyBigWinAnimationInProgress()
	{
		return this._fBigWinControllers_bwc_arr && this._fBigWinControllers_bwc_arr.length;
	}

	constructor()
	{
		super(new BigWinsInfo(), new BigWinsView());

		this._gameScreen = null;
		this._gameField = null;

		this._fPlayerInfo_pi = null;

		this._fBigWinControllers_bwc_arr = [];

		this._fSkipTimer_tmr = null;
		this._fIsSkipProhibited_bl = true;
		this._fPendingAnimBigWinValues_num_arr = [];
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._gameScreen = APP.currentWindow;
		this._gameScreen.on(GameScreen.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_SHOT_SHOW_FIRE_START_TIME, this._onShotShowFireStartTime, this);

		this._fPlayerInfo_pi = APP.playerController.info;
	}

	_onGameFieldScreenCreated()
	{
		this._gameField = this._gameScreen.gameField;
		this._gameField.on(GameField.EVENT_ON_BULLET_TARGET_TIME, this._onBulletTargetTime, this);
		this._gameField.on(GameField.EVENT_ON_TRY_TO_SKIP_BIG_WIN, this._onTryToSkipBigWin, this);
		this._gameField.on(GameField.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
	}

	_onShotShowFireStartTime(event)
	{
		let lShotResultsData_obj = event.data;
		
		if (lShotResultsData_obj.seatId !== event.masterSeatId) return;

		let result = this._checkIfBigWinAnimationNeeded(lShotResultsData_obj);
		if (result)
		{
			this._fPendingAnimBigWinValues_num_arr.push(result.totalWin);
		}
		else
		{
			if (lShotResultsData_obj.skipAwardedWin)
			{
				return;
			}
		}
	}

	_onBulletTargetTime(aEvent_obj)
	{
		const data = aEvent_obj.data;

		let l_enm = this._gameField.getExistEnemy(data.enemyId);
		let lIsBoss_bl = l_enm && l_enm.isBoss;

		if (data.seatId !== this._fPlayerInfo_pi.seatId) return; // for master player only

		let result = this._checkIfBigWinAnimationNeeded(data);
		
		if (!result) return;

		this._generateBigWinController(result.totalWin, result.shotStake, lIsBoss_bl, result.powerUpMult, result.BossPayout);
	}

	_checkIfBigWinAnimationNeeded(data)
	{
		let lTotalWin_num = 0;

		if (!data || !data.affectedEnemies)
		{
			return false;
		}

		let lBossPayout = 0;

		let lPowerUpMult_int = data.currentPowerUpMultiplier;
		let lIsPowerUpWeaponUsed_bl = isPowerUpWeapons(data.usedSpecialWeapon);

		for (let affectedEnemy of data.affectedEnemies)
		{
			if (affectedEnemy.data.class == "Hit")
			{
				const innerData = affectedEnemy.data;
				if (innerData.enemy.typeId == ENEMY_TYPES.BOSS && innerData.killed)
				{
					return false;
				}
				
				lTotalWin_num += innerData.win + innerData.killBonusPay + innerData.multiplierPay;

				if (!lIsPowerUpWeaponUsed_bl)
				{
					lTotalWin_num /= innerData.currentPowerUpMultiplier;
				}

				if(innerData.enemy.typeId == ENEMY_TYPES.BOSS)
				{
					lBossPayout = innerData.win + innerData.killBonusPay + innerData.multiplierPay;
				}
			}
		}

		if (lTotalWin_num == 0)
		{
			return false;
		}

		if (data.betLevel == null)
		{
			APP.logger.i_pushWarning(`BigWin. No betLevel value in Hit response! ${JSON.stringify(data)}`);
			console.error("No betLevel value in Hit response!", data);
			return false;
		}
		if (data.isPaidSpecialShot == null)
		{
			APP.logger.i_pushWarning(`BigWin. No isPaidSpecialShot in Hit response! ${JSON.stringify(data)}`);
			console.error("No isPaidSpecialShot value in Hit response!");
			return false;
		}

		let lShotStake_num = this._fPlayerInfo_pi.currentStake * data.betLevel;

		if(APP.isBattlegroundGame)
		{
			lShotStake_num = BATTLEGROUND_STAKE_MULTIPLIER * data.betLevel;
		}


		const lRatio_num = lTotalWin_num / lShotStake_num;
		if (lRatio_num >= BIG_WIN_PAYOUT_RATIOS.BIG)
		{
			return {totalWin: lTotalWin_num, shotStake: lShotStake_num, powerUpMult: lPowerUpMult_int, BossPayout: lBossPayout};
		}
		return false;
	}

	_generateBigWinController(aTotalWin_num, aShotStake_num, aOptIsBoss_bl=false, aOptPowerUpMult_int=1, aOptBossPayout = 0)
	{
		this._removePendingBigWinValue(aTotalWin_num);
		this.view.addToContainerIfRequired(this.bigWinsContainerInfo);

		let l_bwc = new BigWinController(aTotalWin_num, aShotStake_num, this.view, aOptPowerUpMult_int);
		l_bwc.i_init();
		l_bwc.on(BigWinController.EVENT_ON_ANIMATION_COMPLETED, this._onBigWinAnimationCompleted, this);
		l_bwc.on(BigWinController.EVENT_ON_COIN_LANDED, this.emit, this);
		l_bwc.on(BigWinController.EVENT_ON_BIG_WIN_AWARD_COUNTED, this.emit, this);
		l_bwc.on(BigWinController.EVENT_ON_ANIMATION_INTERRUPTED, this.emit, this);
		l_bwc.on(BigWinController.EVENT_BIG_WIN_PRESETNTATION_STARTED, (e)=>{
			this.emit(
				BigWinsController.EVENT_BIG_WIN_PRESETNTATION_STARTED,
				Object.assign(e, {
					isBoss: aOptIsBoss_bl,
					value: aTotalWin_num,
					seatId: APP.currentWindow.player.seatId,
					bossPayout: aOptBossPayout
				})
			);

			this.view.updateZIndex();

		}, this);
		l_bwc.on(BigWinController.EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING, this.emit, this);
		l_bwc.on(BigWinController.EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING, this.emit, this);

		this._skipActive(); //skip all previous Big Wins animation

		this._fBigWinControllers_bwc_arr.push(l_bwc);

		l_bwc.i_startAnimation();
		this._startSkipTimer();

		this.emit(BigWinsController.EVENT_ON_BIG_WIN_STARTED);
	}

	_removePendingBigWinValue(aValue_num)
	{
		let lPendingValueIndex_int = this._fPendingAnimBigWinValues_num_arr.indexOf(aValue_num);
		if (lPendingValueIndex_int >= 0)
		{
			this._fPendingAnimBigWinValues_num_arr.splice(lPendingValueIndex_int, 1);
		}
	}

	_startSkipTimer()
	{
		this._resetSkipTimer();
		this._fIsSkipProhibited_bl = true;
		this._fSkipTimer_tmr = new Timer(() => {
			this._fIsSkipProhibited_bl = false
		},SKIP_DELAY);
	}

	_resetSkipTimer()
	{
		this._fSkipTimer_tmr && this._fSkipTimer_tmr.destructor();
		this._fSkipTimer_tmr = null;
	}

	_onBigWinAnimationCompleted(aEvent_obj)
	{
		let lIndex_int = this._fBigWinControllers_bwc_arr.indexOf(aEvent_obj.target);
		if (lIndex_int > -1)
		{
			let l_bwc = this._fBigWinControllers_bwc_arr.splice(lIndex_int, 1)[0];
			this._onAllAnimationsCompleteSuspision();
			l_bwc.destroy();
		}
		else
		{
			throw new Error("Can't find completed BigWinController in array, lIndex_int = " + lIndex_int, aEvent_obj);
		}
	}

	_onAllAnimationsCompleteSuspision()
	{
		if (!this.isAnyBigWinInProgress)
		{
			this.emit(BigWinsController.EVENT_ON_ALL_ANIMATIONS_COMPLETED);
		}
	}

	_onRoomFieldCleared()
	{
		this._skipAll();
	}

	_onTryToSkipBigWin()
	{
		if (!this._fIsSkipProhibited_bl)
		{
			this._skipActive();
		}
	}

	_skipActive()  
	{
		this._resetSkipTimer();

		for (let lBigWinController_bwc of this._fBigWinControllers_bwc_arr)
		{
			lBigWinController_bwc.i_interrupt();
			lBigWinController_bwc.destroy();
		}
		this._fBigWinControllers_bwc_arr = [];

		this._fIsSkipProhibited_bl = true;
	}

	_skipAll()
	{
		this._resetSkipTimer();

		if (this._fPendingAnimBigWinValues_num_arr && this._fPendingAnimBigWinValues_num_arr.length)
		{
			let skippedPendingBigWinsValue = 0;
			while (this._fPendingAnimBigWinValues_num_arr && this._fPendingAnimBigWinValues_num_arr.length)
			{
				skippedPendingBigWinsValue += this._fPendingAnimBigWinValues_num_arr.shift();
			}
			this._fPendingAnimBigWinValues_num_arr = [];

			this.emit(BigWinsController.EVENT_ON_PENDING_BIG_WINS_SKIPPED, {skippedPendingBigWinsValue: skippedPendingBigWinsValue});
		}

		for (let lBigWinController_bwc of this._fBigWinControllers_bwc_arr)
		{
			lBigWinController_bwc.i_interrupt();
			lBigWinController_bwc.destroy();
		}
		this._fBigWinControllers_bwc_arr = [];

		this._fIsSkipProhibited_bl = true;
		
		this._onAllAnimationsCompleteSuspision();
	}
}

export default BigWinsController;