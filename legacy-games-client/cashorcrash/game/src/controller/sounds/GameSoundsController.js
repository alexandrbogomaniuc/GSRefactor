import SoundsController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SoundsController';
import GameSoundsInfo from '../../model/sounds/GameSoundsInfo';
import ASSETS from '../../config/assets.json';
import PRELOADER_ASSETS from '../../config/preloader_assets.json';
import SoundSettingsController from './SoundSettingsController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SoundMetrics from '../../../../../common/PIXI/src/dgphoenix/unified/model/sounds/SoundMetrics';
import GameplayController from '../gameplay/GameplayController';
import BetsController from '../gameplay/bets/BetsController';
import RoundController from '../gameplay/RoundController';
import CrashAPP from '../../CrashAPP';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import SimpleSoundChannelController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SimpleSoundChannelController';
import SimpleSoundController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SimpleSoundController';
import BattlegroundGameController from '../main/BattlegroundGameController';

const CROSS_FADE_DURATION = 0;
const BG_SOUND_NAMES = ["bg_main_1", "bg_main_2", "bg_main_3"];
const BEEP_TIME_VALUES = [3000, 2000, 1000];
const BEEP_TIME_INTERVAL = 50;
const ASTEROID_FLYBY_INTERVAL = 8000;
const ASTEROID_FLYBY_SOUND_NAMES = ["meteor_pass_by1", "meteor_pass_by2", "meteor_pass_by3"];

class GameSoundsController extends SoundsController
{
	//IL CONSTRUCTION...
	constructor()
	{
		super(new GameSoundsInfo());

		this._initGSoundsController();
	}
	//...IL CONSTRUCTION

	//IL INTERFACE...
	init(aIsSoundsStereoMode_bl = true, aIsSoundsLoadingAvailable_bl = true, muted = true, fxVolume=0, musicVolume=0)
	{
		super.init(aIsSoundsStereoMode_bl, aIsSoundsLoadingAvailable_bl);

		let info = this.__getInfo();
	
		let lSoundSettings_obj = {
									"fxVolume": 	fxVolume,
									"musicVolume": 	musicVolume,
									"muted":		muted
								};
		this.__updateSoundSettings(lSoundSettings_obj);
	}
	
	//...IL INTERFACE

	//IL INIT...
	_initGSoundsController()
	{
		this._fCurrentBGSoundIndex_int = -1;
		this._fMusicBg_uscc = null;

		this._fFlybyIndex_int = 0;
		this._fAsteroidFlybyTimer_tmr = null;

		this._initSoundsMetrics();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(CrashAPP.EVENT_ON_PRELOADER_SOUNDS_READY, this._onPreloaderSoundsReady, this);
		APP.on(CrashAPP.EVENT_ON_ROOM_PAUSED, this._cancelAsteroidFlybyTimer, this);

		this._fSoundSettingsController_ssc = APP.soundSettingsController;
		this._fSoundSettingsController_ssc.on(SoundSettingsController.EVENT_ON_SOUND_SETTINGS_CHANGED, this.__updateSoundSettings, this);

		let l_gpc = this._fGameplayController_gpc = APP.gameController.gameplayController;
		l_gpc.on(GameplayController.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED, this._onStarshipTakeoffExplosionStarted, this);
		l_gpc.on(GameplayController.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this._onStarshipCrashExplosionStarted, this);
		l_gpc.on(GameplayController.EVENT_ON_ASTRONAUT_EJECT_STARTED, this._onAstronautJump, this);
		l_gpc.on(GameplayController.EVENT_ON_ASTRONAUT_LANDED, this._onAstronautLanded, this);

		if (APP.isBattlegroundGame)
		{
			l_gpc.on(GameplayController.EVENT_ON_GAMEPLAY_TIME_UPDATED, this._onGameplayTimeUpdated, this);
			l_gpc.on(GameplayController.EVENT_ON_ASTRONAUT_ENTERS_ROCKET, this._onAstronautEntersRocket, this);
			l_gpc.on(GameplayController.EVENT_ON_ASTRONAUT_EXIT_ROCKET, this._onAstronautExitRocket, this);
			l_gpc.on(GameplayController.EVENT_ON_ASTRONAUT_TRAMPOLINE, this._onAstronautTrampoline, this);
			l_gpc.on(GameplayController.EVENT_ON_YOU_WON_ANIMATION_STARTED, this._onYouWonAnimationStarted, this);
			l_gpc.on(GameplayController.EVENT_ON_WIN_CROWN_ANIMATION_STARTED, this._onWinCrownAnimationStarted, this);
		}
		else
		{
		l_gpc.on(GameplayController.EVENT_ON_TIMER_VALUE_UPDATED, this._onTimerValueUpdated, this);
		}

		l_gpc.gamePlayersController.betsController.on(BetsController.EVENT_ON_BET_CANCELLED, this._onBetCancelled, this);

		this._fRoundController_rc = l_gpc.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);
	}

	__startFxSounds()
	{
		this._playRocketFlySoundIfRequired();
	}

	_initSoundsMetrics()
	{
		this.__getInfo().i_initSoundsMetrics(PRELOADER_ASSETS.sounds);
		this.__getInfo().i_initSoundsMetrics(ASSETS.sounds);
	}
	//...IL INIT

	//BG SOUNDS...
	_onPreloaderSoundsReady(event)
	{
		this._playNextBGSound();
	}

	_playNextBGSound()
	{
		this._destroyChangeMainBgMusicTimer();

		let lCurBgMusic_uscc = this._fMusicBg_uscc;
		
		lCurBgMusic_uscc && lCurBgMusic_uscc.off(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED, this._onBGSoundPlayingCompleted, this, true);

		let lCurBgMusicInfo_ussi = lCurBgMusic_uscc ? lCurBgMusic_uscc.i_getInfo() : null;

		let lNextBGSoundIndex_int = this._fCurrentBGSoundIndex_int + 1;
		if (!lCurBgMusic_uscc)
		{
			lNextBGSoundIndex_int = this._fCurrentBGSoundIndex_int = 0;
		}

		if (lNextBGSoundIndex_int >= BG_SOUND_NAMES.length)
		{
			lNextBGSoundIndex_int = 0;
		}

		let lSound_ussc;
		let lNextSoundName_str = BG_SOUND_NAMES[lNextBGSoundIndex_int];

		if (!SimpleSoundChannelController.isSoundReady(lNextSoundName_str))
		{
			lNextBGSoundIndex_int = this._fCurrentBGSoundIndex_int;
			lNextSoundName_str = BG_SOUND_NAMES[lNextBGSoundIndex_int];
		}
		
		if (lCurBgMusicInfo_ussi && lCurBgMusicInfo_ussi.i_getSoundName() === lNextSoundName_str)
		{
			lSound_ussc = lCurBgMusic_uscc;
			lSound_ussc.once(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED, this._onBGSoundPlayingCompleted, this);
		}
		else
		{
			lSound_ussc = this.i_getSoundController(lNextSoundName_str, false);

			if (!lSound_ussc)
			{
				APP.logger.i_pushWarning(`GameSoundsController. _playNextBGSound. Failed to get sound controller for sound: ${lNextSoundName_str}`);
			}
			
			if (CROSS_FADE_DURATION > 0)
			{
				lSound_ussc && lSound_ussc.i_resetVolume();
				this.setFadeMultiplier(lNextSoundName_str, 0);
				this.setVolumeSmoothly(lNextSoundName_str, 1, CROSS_FADE_DURATION);

				lCurBgMusic_uscc && this.setVolumeSmoothly(lCurBgMusicInfo_ussi.i_getSoundName(), 0, CROSS_FADE_DURATION);

				if (lSound_ussc)
				{
					let duration = lSound_ussc.i_getInfo().i_getSoundDescriptor().i_getLength() - CROSS_FADE_DURATION;
					this._fChangeMainBgMusicTimer_t = new Timer(this._onChangeMainBgMusicTimerCompleted.bind(this), duration);
				}
			}
			else
			{
				lCurBgMusic_uscc && lCurBgMusic_uscc.i_stopPlaying();

				lSound_ussc && lSound_ussc.once(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED, this._onBGSoundPlayingCompleted, this);
			}
			
			lSound_ussc && lSound_ussc.i_startPlaying();
			
		}

		this._fMusicBg_uscc = lSound_ussc;
		this._fCurrentBGSoundIndex_int = lNextBGSoundIndex_int;

		return lSound_ussc;
	}

	_playRocketFlySoundIfRequired()
	{
		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundPlayActive)
		{
			if (!this.isSoundPlaying("rocket_fly"))
			{
				this.play("rocket_fly", true);
			}
		}
		else
		{
			if (this.isSoundPlaying("rocket_fly"))
			{
				this.stop("rocket_fly");
			}
		}
	}

	_onChangeMainBgMusicTimerCompleted()
	{
		this._destroyChangeMainBgMusicTimer();

		this._playNextBGSound();
	}

	_onBGSoundPlayingCompleted(event)
	{
		this._destroyChangeMainBgMusicTimer();

		this._playNextBGSound();
	}

	_destroyChangeMainBgMusicTimer()
	{
		this._fChangeMainBgMusicTimer_t && this._fChangeMainBgMusicTimer_t.destructor();
		this._fChangeMainBgMusicTimer_t = null;
	}
	//...BG SOUNDS

	_onStarshipTakeoffExplosionStarted(event)
	{
		this.play("rocket_launch");
	}

	_onAstronautEntersRocket(event)
	{
		this.play("enter_rocket");
	}

	_onAstronautExitRocket(event)
	{
		this.play("exit_rocket");
	}

	_onAstronautTrampoline(event)
	{
		const basic_trampouline_sound = "trampoline";
		if(APP.isBattlegroundGame)
		{
			let isMasterBet = APP.gameController._fGameplayController_gpc._fGamePlayersController_rsc.__fInfo_usi._fMasterObserverId_str == event.target._fSeatId_str;
			if(isMasterBet){
					this.play("trampoline_master");
				}else{
					this.play(basic_trampouline_sound);
				}
		}else
		{
			this.play(basic_trampouline_sound);
		}
	}

	_onYouWonAnimationStarted(event)
	{
		this.play("win_screen");
	}

	_onWinCrownAnimationStarted(event)
	{
		this.play("win_crown");
	}

	_onStarshipCrashExplosionStarted(event)
	{
		this.play("rocket_explosion");
	}

	_onAstronautJump(event)
	{
		this.play("astronaut_jump");
	}

	_onAstronautLanded(event)
	{
		this.play("astronaut_land");
	}

	_onGameplayTimeUpdated()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;

		if (APP.isBattlegroundGame && lRoundInfo_ri.isRoundPlayActive)
		{
			let lShipTakeOffStartTime_num = lRoundInfo_ri.roundStartTime - l_gpi.preLaunchFlightDuration;
			let lShipTakeOffRestTime_num = lShipTakeOffStartTime_num - l_gpi.gameplayTime;
			this._playCountDownTimerIfPossible(lShipTakeOffRestTime_num);
		}

		this._updateAsteroidFlyby();
	}

	_updateAsteroidFlyby()
	{
		if (this._fAsteroidFlybyTimer_tmr || !APP.isBattlegroundGame)
		{
			return;
		}

		let lRoundInfo_ri = this._fRoundController_rc.info;
		let lGameplayInfo_gpi = this._fGameplayController_gpc.info;
		let lGameplayTime_num = lGameplayInfo_gpi.gameplayTime;

		if (lRoundInfo_ri.isRoundPlayState)
		{
			let lTimerValue_num = ASTEROID_FLYBY_INTERVAL - (lGameplayTime_num - lRoundInfo_ri.roundStartTime) % ASTEROID_FLYBY_INTERVAL;
			this._fAsteroidFlybyTimer_tmr = new Timer(this._onAsteroidFlyby.bind(this), lTimerValue_num);
		}
	}

	_onAsteroidFlyby()
	{
		let lRoundInfo_ri = this._fRoundController_rc.info;
		if (lRoundInfo_ri.isRoundPlayState)
		{
			this.play(ASTEROID_FLYBY_SOUND_NAMES[this._fFlybyIndex_int]);
			this._fFlybyIndex_int = (this._fFlybyIndex_int < ASTEROID_FLYBY_SOUND_NAMES.length - 1)? this._fFlybyIndex_int + 1 : 0;
			this._cancelAsteroidFlybyTimer();
		}
	}

	_cancelAsteroidFlybyTimer()
	{
		this._fAsteroidFlybyTimer_tmr && this._fAsteroidFlybyTimer_tmr.destructor();
		this._fAsteroidFlybyTimer_tmr = null;
	}

	_onTimerValueUpdated(event)
	{
		this._playCountDownTimerIfPossible(event.restTime);
	}

	_playCountDownTimerIfPossible(aFlightRestTime_num)
	{
		let lRestTime_num = aFlightRestTime_num;

		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lBetsInfo_bsi = l_gpi.gamePlayersInfo.betsInfo;

		let lIsBeepTime_bl = false;
		if (lBetsInfo_bsi.isNoMoreBetsPeriodMode && !(lRoundInfo_ri.isRoundPauseState || lRoundInfo_ri.isRoundPlayState))
		{
			lIsBeepTime_bl = false;
		}
		else if (this.isSoundPlaying("timer_countdown"))
		{
			lIsBeepTime_bl = false;
		}
		else
		{
			for (let i=BEEP_TIME_VALUES.length-1; i>=0; i--)
			{
				let lBaseBeepTime_num = BEEP_TIME_VALUES[i];
				if (lRestTime_num <= (lBaseBeepTime_num+BEEP_TIME_INTERVAL) && lRestTime_num > (lBaseBeepTime_num-BEEP_TIME_INTERVAL))
				{
					lIsBeepTime_bl = true;
				}
			}
		}

		if (lIsBeepTime_bl)
		{
			this.play("timer_countdown");
		}
		else
		{
			let lIsPreRocketStartCountingEndState_bl = lRoundInfo_ri.isRoundWaitState;

			if (lBetsInfo_bsi.isNoMoreBetsPeriodMode)
			{
				lIsPreRocketStartCountingEndState_bl = lRoundInfo_ri.isRoundPauseState || lRoundInfo_ri.isRoundPlayState;
			}
			else if (APP.isBattlegroundGame)
			{
				lIsPreRocketStartCountingEndState_bl = lRoundInfo_ri.isRoundPlayState;
			}

			if (
					lIsPreRocketStartCountingEndState_bl
					&& lRestTime_num >= 0 && lRestTime_num < 100 && !this.isSoundPlaying("timer_countend")
				)
			{
				this.play("timer_countend");
			}
		}
	}

	_onBetCancelled(event)
	{
		let lTargetBetInfo_bi = event.betInfo;
		if (
			lTargetBetInfo_bi.isMasterBet
			&& lTargetBetInfo_bi.isConfirmedMasterBet
			&& lTargetBetInfo_bi.isBetWinDefined
			&& !lTargetBetInfo_bi.isDeactivatedBet)
		{
			this.play("player_win");
		}
	}

	_onRoundStateChanged(event)
	{
		this._playRocketFlySoundIfRequired();
		this._cancelAsteroidFlybyTimer();
	}

	destroy()
	{
		super.destroy();
	}
}

export default GameSoundsController;