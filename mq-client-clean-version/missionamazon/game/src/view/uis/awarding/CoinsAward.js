import BaseAward from './BaseAward';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import WinPayout from '../../../ui/WinPayout';
import SilverWinPayout from '../../../ui/SilverWinPayout';
import WhiteWinPayout from '../../../ui/WhiteWinPayout';
import WinTierUtil from '../../../main/WinTierUtil';
import AtlasConfig from '../../../config/AtlasConfig';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import ProfilingInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';
import CompletedQuestBackEffects from './CompletedQuestBackEffects';
import GameField from './../../../main/GameField';
import { GlowFilter } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';

const COINS_FLY_OUT_INTERVAL	= 1*FRAME_RATE;
const COIN_FLY_DURATION			= 9*FRAME_RATE;
const PAYOUT_APPEAR_TIME		= 15*FRAME_RATE;
const COINS_FLY_OUT_DELAY		= 1*FRAME_RATE;
const FIXED_BATTLEGOUND_STAKE	= 100;

const COINS_HEIGHT_DISTANCE		= 28;
const COINS_WIDTH_DISTANCE		= 22;

const COINS_ANIMATION_DELAY = 70*FRAME_RATE;

export const COINS_ANIMATIONS_PARAMS = [
	{id: 2, 	startFrame: 1, 	x: 19, 	y: -308},
	{id: 3, 	startFrame: 2, 	x: -33, y: -285},
	{id: 1, 	startFrame: 0, 	x: 54, 	y: -207},
	{id: 4, 	startFrame: 3, 	x: -56, y: -240},
	{id: 5, 	startFrame: 4, 	x: -84, y: -265},
	{id: 6, 	startFrame: 2, 	x: 4, 	y: -232},
	{id: 7, 	startFrame: 0, 	x: -8, 	y: -215},
	{id: 8, 	startFrame: 2, 	x: -63, y: -205},
	{id: 9, 	startFrame: 1, 	x: -95, y: -152},
	{id: 10, 	startFrame: 4, 	x: 88, 	y: -260},
	{id: 11, 	startFrame: 2, 	x: 92, 	y: -193},
	{id: 12, 	startFrame: 0, 	x: 89, 	y: -178},
	{id: 13, 	startFrame: 2, 	x: 23, 	y: -170},
	{id: 14, 	startFrame: 1, 	x: -6,  y: -144},
	{id: 15, 	startFrame: 4, 	x: 171, y: -245},
	{id: 16, 	startFrame: 1, 	x: 19+90, 	y: -308},
	{id: 17, 	startFrame: 2, 	x: -33-50, y: -285},
	{id: 18, 	startFrame: 0, 	x: 54+90, 	y: -207},
	{id: 19, 	startFrame: 3, 	x: -56-50, y: -240},
	{id: 20, 	startFrame: 4, 	x: -84+50, y: -265},
	{id: 21, 	startFrame: 2, 	x: 4-90, 	y: -232},
	{id: 22, 	startFrame: 0, 	x: -8+50, 	y: -215},
	{id: 23, 	startFrame: 2, 	x: -63-70, y: -205},
	{id: 24, 	startFrame: 1, 	x: -95+80, y: -152},
	{id: 25, 	startFrame: 4, 	x: 88-60, 	y: -260},
	{id: 26, 	startFrame: 2, 	x: 92+90, 	y: -193},
	{id: 27, 	startFrame: 0, 	x: 89-40, 	y: -178},
	{id: 28, 	startFrame: 2, 	x: 23+50, 	y: -170},
	{id: 29, 	startFrame: 1, 	x: -6-90,  y: -144},
	{id: 30, 	startFrame: 4, 	x: 171+50, y: -245},
	{id: 31, 	startFrame: 4, 	x: -84-60, y: -265+60},
	{id: 32, 	startFrame: 2, 	x: 4+60, 	y: -232+50},
	{id: 33, 	startFrame: 0, 	x: -8-90, 	y: -2155+60},
	{id: 34, 	startFrame: 2, 	x: -63+70, y: -205+50},
	{id: 35, 	startFrame: 1, 	x: -95-50, y: -152+40},
	{id: 36, 	startFrame: 4, 	x: 88+90, 	y: -260-50},
	{id: 37, 	startFrame: 2, 	x: 92-40, 	y: -193-30},
	{id: 38, 	startFrame: 0, 	x: 89+50, 	y: -178-50},
	{id: 39, 	startFrame: 2, 	x: 23-70, 	y: -170-60},
	{id: 40, 	startFrame: 1, 	x: -6+90,  y: -144-50},
];

const PAYOUTS_SETTINGS = [
	{
		timeout: 0*FRAME_RATE,
		percent: 0.13
	},
	{
		timeout: 15*FRAME_RATE,
		percent: 0.2
	},
	{
		timeout: 29*FRAME_RATE,
		percent: 0.27
	},
	{
		timeout: 40*FRAME_RATE,
		percent: 0.33
	},
	{
		timeout: 51*FRAME_RATE,
		percent: 0.4
	},
	{
		timeout: 62*FRAME_RATE,
		percent: 0.53
	},
	{
		timeout: 69*FRAME_RATE,
		percent: 1
	}
];

const PARTICLLES_ANIMATIONS_PARAMS = [
	{
		x: -9.5,
		y: 0
	},
	{
		x: 9.5,
		y: 0
	}
];

const Z_INDEXES = {
	BACK_EFFECTS: 0,
	PAYOUT: 1,
	COINS: 2,
	TOP_EFFECTS: 3
}

let largeWinNumber = 0, mediumWinNumber = 0;
function getLargeWinNumber()	{ return APP.isMobile || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM) ? 2 : (++largeWinNumber > 3 ? (largeWinNumber = 1) : largeWinNumber); }
function getMediumWinNumber()	{ return APP.isMobile || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM) ? 3 : (++mediumWinNumber > 3 ? (mediumWinNumber = 1) : mediumWinNumber); }
function getSmallWinNumber()	{ return APP.isMobile || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM) ? 1 : Utils.random(1, 3); }

function getLargeWinNumber()	{ return APP.isMobile ? 2 : (++largeWinNumber > 3 ? (largeWinNumber = 1) : largeWinNumber); }
function getMediumWinNumber()	{ return APP.isMobile ? 3 : (++mediumWinNumber > 3 ? (mediumWinNumber = 1) : mediumWinNumber); }

let _criticalParticlesTextures = null;

function _initParticlesTextures()
{
	if (_criticalParticlesTextures) return;

	_criticalParticlesTextures = AtlasSprite.getFrames(APP.library.getAsset("critical_hit/critical_particles"), AtlasConfig.CriticalParticles, "");
}

class CoinsAward extends BaseAward
{
	static get EVENT_ON_COIN_LANDED()			{ return "onCoinLanded"; }
	static get EVENT_ON_PAYOUT_APPEARED()		{ return "onPayoutAppeared"; }
	static get EVENT_ON_PAYOUT_IS_VISIBLE()		{ return "onPayoutIsVisible"; }
	static get EVENT_ON_COINS_JOIN_REQUIRED()	{ return "onCoinsJoinRequired"; }
	static get EVENT_ON_JOIN_COMPLETED()		{ return "onJoinCompleted"; }

	static OFFSET_COUNTER = 0;
	static OFFSET_STEP = 7;
	static MAX_OFFSET_COUNTER_VALUE = 7;

	constructor(aGameField_sprt, aAwardType_int, aOptParams_obj)
	{
		super(aGameField_sprt, aAwardType_int);

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			_initParticlesTextures();
		}

		this._fParams_obj = aOptParams_obj;
		this._fRid_num = aOptParams_obj.rid;
		this._fSeatId_int = +aOptParams_obj.seatId;
		this._fBombEnemyId_int = aOptParams_obj.bombEnemyId;
		this._fCoins_sprt_arr = null;
		this._fEnemyTypeId_int = null;
		this._fPayout_wp = null;
		this._fDropPaths_arr_arr = null;
		this._fCoinsTimer_t = null;
		this._fEndPosition_pt = null;
		this._fCoinsCounter_int = undefined;
		this._fPayoutTimer_t = null;
		this._fPayValue_num = null;
		this._fPayoutValue_num = null;
		this._fCoinsMoneyStep_num = null;
		this._fCoinsMoneyCounter_num = null;
		this._fSpecifiedWinSoundTier = aOptParams_obj.specifiedWinSoundTier;
		this._winDevalued_bl = false;
		this._fGenerateId_num = null;
		this._fOffscreenOffsetX_num = null;
		this._fOffscreenOffsetY_num = null;
		this._fIsBoss_bl = null;
		this._fSpeed_num = 1;
		this._fIsMassExplosiveWin_bln = false;
		this._fJoiningTriggered_bln = false;
		this._fOnCoinsLanded_bln = false;
		this._fKilledByBomb_bl = false;
		this._startPathPoint = undefined;
		this._startOffset = undefined;
		this._speed = undefined;
		this._fParticlesContainer_sprt = null;
		this._fMultFlare_sprt = null;
		this._fCircleBlast_sprt = null;
		this._fCircleBlastSecond_sprt = null;
		this._fChMult_num = null;
		this._fPowerUpMultiplier_num = null;
		this._fCoinAnimationTimer_t = null;
		this._fStartAddAnimationTimer_t = null;
		this._fPayoutContainer_sprt = null;
		this._fCoinsScale_num = null;

		this._fStartTimer_tmr = null;

		this._fCoinExplosion_bln = aOptParams_obj.coinExplosion;
		this._fTimers_arr = [];

		this._completedQuestPayoutEffects_arr = null;

		this._fBottomFXsContainer_sprt = null;
		this._fTopFXsContainer_sprt = null;

		this._fKillBonusPay = aOptParams_obj.isBoss ? aOptParams_obj.killBonusPay : 0;
		this._fBundleAward_bln = aOptParams_obj.followEnemy;
		this._fFollowEnemy_bln = aOptParams_obj.followEnemy;
		this._fEnemyId_num = aOptParams_obj.enemyId;
		this._fIsEnemyKilled_bl = aOptParams_obj.killed || false;
		this._fAwardingShowStarted_bln = false;
		this._fBundleCoinsTimer_t = null;
		this._fGlowSeq_s = null;

		this._fDelta_num = 0;

		if (this._fFollowEnemy_bln)
		{
			APP.on('tick', this._onTick, this);
		}
	}

	get countedCoinsAmount()
	{
		return this._fCoinsMoneyCounter_num || this._fBundleCoinsMoneyCounter_num || 0;
	}

	get isMassExplosive()
	{
		return this._fIsMassExplosiveWin_bln;
	}

	get awardingShowStarted()
	{
		return this._fAwardingShowStarted_bln;
	}

	get followEnemy()
	{
		return this._fFollowEnemy_bln;
	}

	get rid()
	{
		return this._fRid_num;
	}

	get seatId()
	{
		return this._fSeatId_int;
	}

	get isWinDevalued()
	{
		return this._winDevalued_bl;
	}

	get isMasterSeat()
	{
		return (this._fSeatId_int === APP.playerController.info.seatId);
	}

	get winValue()
	{
		return this._fPayoutValue_num;
	}

	startJoinAnimation(aPos_obj)
	{
		this._startJoinAnimation(aPos_obj);
	}

	onNextJoinCompleted(aValue_num)
	{
		this._onNextJoinCompleted(aValue_num);
	}

	get payout()
	{
		return this._fPayoutValue_num;
	}

	get enemyId()
	{
		return this._fEnemyId_num;
	}

	hidePayout()
	{
		this._hidePayout();
	}

	get offscreenOffset()
	{
		if (this._fOffscreenOffsetX_num !== null && this._fOffscreenOffsetY_num !== null)
		{
			return {x: this._fOffscreenOffsetX_num, y: this._fOffscreenOffsetY_num};
		}

		return {x: 0, y: 0};
	}

	updateBundleValue(aVal_num, aNoGlow_bln = false)
	{
		this._updateBundleValue(aVal_num, aNoGlow_bln);
	}

	showFinalValue(aVal_num)
	{
		this._showFinalValue(aVal_num);
	}

	_calcAnimationSpeed(aWinTier_int)
	{
		let lIsWinTier_int = aWinTier_int;
		let lIsWinTierSmall_bl = lIsWinTier_int !== WinTierUtil.WIN_TIERS.TIER_BIG;
		let lIsSmallBossWin_bl = this._fIsBoss_bl && lIsWinTierSmall_bl && !this._fCoinExplosion_bln;
		return lIsSmallBossWin_bl ? 2 : 1;
	}

	_showAwarding(aValue_num, aParams_obj)
	{
		this._fIsMassExplosiveWin_bln = aParams_obj.massExplosiveWin;
		this._fRid_num = aParams_obj.rid;
		this._fEnemyTypeId_int = aParams_obj.typeId;

		if (aParams_obj.startDelay > 0)
		{
			this._fStartTimer_tmr = new Timer(this._showAwardingNow.bind(this, aValue_num, aParams_obj), aParams_obj.startDelay);
		}
		else
		{
			this._showAwardingNow(aValue_num, aParams_obj);
		}
	}

	_showAwardingNow(aValue_num, aParams_obj)
	{
		let lValue_num = aValue_num;
		this._fParticlesContainer_sprt = this.addChild(new Sprite());
		this._fPayoutContainer_sprt = this.addChild(new Sprite());

		this._fAwardingShowStarted_bln = true;
		this._fIsBoss_bl = aParams_obj.isBoss;
		this._fIsBombEnemy_bl = aParams_obj.isBombEnemy;
		this._fKilledByBomb_bl = aParams_obj.killedByBomb || this._fIsBombEnemy_bl;

		if (this.isMasterSeat)
		{
			this._fPowerUpMultiplier_num = aParams_obj.currentPowerUpMultiplier;
		}
		else
		{
			lValue_num = this._fPowerUpMultiplier_num > 1 ?  lValue_num * this._fPowerUpMultiplier_num : lValue_num;
			this._fPowerUpMultiplier_num = 1;
		}

		this._fChMult_num = this._fPowerUpMultiplier_num > 1 ? 0 : aParams_obj.chMult;

		if (this._fChMult_num > 1)
		{
			APP.gameScreen.gameField.on(GameField.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED, this._onCriticalHitAnimationEnded, this);
		}

		if (this._fPowerUpMultiplier_num > 1)
		{
			APP.gameScreen.gameField.on(GameField.EVENT_ON_PUWER_UP_ANIMATION_ENDED, this._onPowerUpMultiplierAnimationEnded, this);
		}

		const lWeaponsInfo_wsi = APP.currentWindow.weaponsController.i_getInfo();
		let lCurrentShotPrice_num = lWeaponsInfo_wsi.i_getWeaponShotPrice(lWeaponsInfo_wsi.currentWeaponId);

		let lWinCoins_int = Math.floor(Number(lValue_num));
		if (lWinCoins_int > 15) lWinCoins_int = 15;
		let lWinTier_int = this._calcWinTier(Number(lValue_num));

		this._fEndPosition_pt = aParams_obj.winPoint || this._defaultPoint;
		this._winDevalued_bl = aParams_obj.isQualifyWinDevalued;
		this._fIsQuestCompleateAward_b = aParams_obj.isQuestCompleateAward ? aParams_obj.isQuestCompleateAward : false;

		this._fCoinsScale_num = this._fIsBoss_bl ? 0.6 : APP.isBattlegroundGame ? (this._fEnemyTypeId_int === 5 ||
																				   this._fEnemyTypeId_int === 6	||
																				   this._fEnemyTypeId_int === 7	||
																				   this._fEnemyTypeId_int === 11  ||
																				   this._fEnemyTypeId_int === 12  ||
																				   this._fEnemyTypeId_int === 13 ? 0.25 : 0.5) :
																				  (lValue_num < lCurrentShotPrice_num ? 0.25 : 0.5)

		this._fCoins_sprt_arr = [];
		this._fCoinsCounter_int = lWinCoins_int;

		for (let i=0; i<this._fCoinsCounter_int; i++)
		{
			let coin = this._generateCoin();
			if (this._fFollowEnemy_bln)
			{
				coin.alpha = 0;
			}

			this._fCoins_sprt_arr.push(this.addChild(coin));
		}

		this._fPayValue_num = Number(lValue_num);

		this._fCoinsMoneyStep_num = Math.ceil((Number(lValue_num) + this._fKillBonusPay) / this._fCoinsCounter_int);
		this._fCoinsMoneyCounter_num = 0;

		let lIsNormalAward_bln = !(this._fChMult_num > 1 || this._fPowerUpMultiplier_num > 1);

		let startOffset = aParams_obj.startOffset || {x: 0, y: 0};
		let lPayoutPos_obj = {x:aParams_obj.start.x+startOffset.x, y:aParams_obj.start.y-(lIsNormalAward_bln ? 60 : 90)+startOffset.y};

		let lPayoutFontScale_num = (lWinTier_int == WinTierUtil.WIN_TIERS.TIER_SMALL ? 1.2 : (lWinTier_int == WinTierUtil.WIN_TIERS.TIER_BIG ? 1.56 : 1.4)) * 0.7;
		let lWinPayout_wp = this._createWinPayout(lPayoutFontScale_num);
		lWinPayout_wp.zIndex = Z_INDEXES.PAYOUT;
		this._fPayout_wp = this._fPayoutContainer_sprt.addChild(lWinPayout_wp);
		this._fPayout_wp.visible = false;

		this._fPayoutValue_num = Number(lValue_num);

		if (this._fChMult_num > 1)
		{
			let lPayVal_num = (lValue_num / this._fChMult_num).toFixed(2);
			this._fPayout_wp.value = +lPayVal_num;
		}
		else if (this._fPowerUpMultiplier_num > 1)
		{
			let lPayVal_num = (lValue_num / this._fPowerUpMultiplier_num).toFixed(2);
			this._fPayout_wp.value = +lPayVal_num;
		}
		else
		{
			this._fPayout_wp.value = lValue_num;
		}

		this._fDelta_num = (CoinsAward.OFFSET_COUNTER * CoinsAward.OFFSET_STEP);
		CoinsAward.OFFSET_COUNTER = (CoinsAward.OFFSET_COUNTER + 1 ) % CoinsAward.MAX_OFFSET_COUNTER_VALUE;

		this._fOffscreenOffsetY_num = this._getOffscreenOffsetY(lPayoutPos_obj.y - 20);
		this._fOffscreenOffsetX_num = this._getOffscreenOffsetX(lPayoutPos_obj.x);

		this._fPayout_wp.position.set(lPayoutPos_obj.x + this._fOffscreenOffsetX_num, lPayoutPos_obj.y + this._fOffscreenOffsetY_num);

		let lSpeed_num = this._fSpeed_num = this._calcAnimationSpeed(lWinTier_int);
		if (!this.isMasterSeat)
		{
			lSpeed_num *= 1.5;
		}
		if (!this._fKilledByBomb_bl)
		{
			this._fillDropPath(aParams_obj.start, startOffset, lSpeed_num);
		}
		else
		{
			this._startPathPoint = aParams_obj.start;
			this._startOffset = startOffset;
			this._speed = lSpeed_num;
		}

		if (this._fIsQuestCompleateAward_b)
		{
			this._onQuestCompleatePayoutAppearTime();
		}
		else
		{
			let lPayoutAppearTime_num = (this._fChMult_num > 1 || this._fPowerUpMultiplier_num > 1) ? 0 : PAYOUT_APPEAR_TIME;

			this._fPayoutTimer_t = new Timer(()=>this._onPayoutTimerCounted(), lPayoutAppearTime_num);

			if (!this._fKilledByBomb_bl)
			{
				this._fCoinAnimationTimer_t = new Timer(()=>this._onCoinFlyInTime(), 21*FRAME_RATE); // 21 = 20-2+3 - delays before death pile show

				if (!this._fBundleAward_bln)
				{
					this._playWinTierSoundSuspicion(lWinTier_int);
				}
			}
		}
	}

	//QUEST COMPLETE PAYOUT...
	_onQuestCompleatePayoutAppearTime()
	{
		this._fPayout_wp.alpha = 1;
		this._fPayout_wp.position.y += 90;
		this._fPayout_wp.scale.set(1.3);

		let lBottomFXsContainer_sprt = this._fBottomFXsContainer_sprt = this.addChild(new Sprite);
		lBottomFXsContainer_sprt.zIndex = Z_INDEXES.BACK_EFFECTS;
		lBottomFXsContainer_sprt.position.set(this._fPayout_wp.x, this._fPayout_wp.y);

		let lTopFXsContainer_sprt = this._fTopFXsContainer_sprt = this.addChild(new Sprite);
		lTopFXsContainer_sprt.zIndex = Z_INDEXES.TOP_EFFECTS;
		lTopFXsContainer_sprt.position.set(this._fPayout_wp.x, this._fPayout_wp.y);

		this._completedQuestPayoutEffects_arr = [];

		let lScaleSeq_arr = [
			{tweens: [],	duration: 1*FRAME_RATE, onfinish: () => {this._onPayoutTimerCounted()} },
			{tweens: [{prop: 'scale.x', to: 1},		{prop: 'scale.y', to: 1}],	duration: 7*FRAME_RATE},
			{tweens: [],	duration: 14*FRAME_RATE },
			{tweens: [],	duration: 5*FRAME_RATE, onfinish: () => { this._onCoinFlyInTime(); }},
			{tweens: [],	duration: 2*FRAME_RATE, onfinish: ()=>{ this._completeAwarding(); } },
		];

		Sequence.start(this._fPayout_wp, lScaleSeq_arr);

		this._showCompletedQuestPayoutFXs();

		APP.soundsController.play("quests_win_figure", false);
	}

	_showCompletedQuestPayoutFXs()
	{
		if (!APP.profilingController.info.isVfxProfileValueLowerOrGreater)
		{
			return;
		}

		this._addCompletedQuestPayoutFlares();
	}

	_addCompletedQuestPayoutGlow()
	{
		if (!APP.profilingController.info.isVfxProfileValueLowerOrGreater)
		{
			return;
		}

		let lWhiteQuestCompleatePayout_wp = this._createWinPayoutGlow(this._fPayout_wp.fontScale);
		this._fPayout_wp.addChild(lWhiteQuestCompleatePayout_wp);
		lWhiteQuestCompleatePayout_wp.value = this._fPayout_wp.value;

		lWhiteQuestCompleatePayout_wp.fadeTo(0, 4*FRAME_RATE);
	}

	_addCompletedQuestPayoutFlares()
	{
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			let backEffects = this._fBottomFXsContainer_sprt.addChild(new CompletedQuestBackEffects());
			this._completedQuestPayoutEffects_arr.push(backEffects);
		}

		// top orange flare...
		let lTopOrangeFlare_sprt = this._fTopFXsContainer_sprt.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		lTopOrangeFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lTopOrangeFlare_sprt.visible = false;
		lTopOrangeFlare_sprt.scale.set(0, 0);

		let lTopFlareSeq_arr = [
			{tweens: [], duration: 1*FRAME_RATE, onfinish: () => {lTopOrangeFlare_sprt.visible = true;} },
			{tweens: [], duration: 25*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2*1.1},	{prop: 'scale.y', to: 2*0.4}],	duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2*0.7},	{prop: 'scale.y', to: 2*0.5}],	duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},		{prop: 'scale.y', to: 0}],		duration: 4*FRAME_RATE}
		];

		this._completedQuestPayoutEffects_arr.push(lTopOrangeFlare_sprt);

		Sequence.start(lTopOrangeFlare_sprt, lTopFlareSeq_arr);
		// ...top orange flare
	}
	//...QUEST COMPLETE PAYOUT

	_startJoinAnimation(aPos_obj)
	{
		this._fJoiningTriggered_bln = true;

		Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));

		let lDuration_num = Math.floor(Utils.random(4, 7))*FRAME_RATE/this._fSpeed_num;
		let lSeq_arr = [{
			tweens: [{prop:"position.x", to: aPos_obj.x}, {prop:"position.y", to: aPos_obj.y}], duration: lDuration_num, ease: Easing.sine.easeIn,
			onfinish: ()=>{
				this.emit(CoinsAward.EVENT_ON_JOIN_COMPLETED);

				this._completeAwarding(false);
				this._fUncountedValue_num = null;
			}
		}];

		Sequence.start(this._fPayout_wp, lSeq_arr);
	}

	_onNextJoinCompleted(aValue_num)
	{
		this._fJoiningValuesCount_num--;

		this._fPayoutValue_num += Number(aValue_num);
		this._fUncountedValue_num = this._fPayoutValue_num;
		this._setValue(this._fPayoutValue_num);

		if (this._fJoiningValuesCount_num == 0)
		{
			this._startJoinTargetAnimation();
		}
	}

	_startJoinTargetAnimation()
	{
		this._fKilledByBomb_bl && this._startJoinFlareAnimation();

		let lSeq_arr = [
			{tweens: [{ prop: 'scale.x', to: 1.4}, { prop: 'scale.y', to: 1.4}], duration: 2 * FRAME_RATE / this._fSpeed_num, ease: Easing.sine.easeIn},
			{tweens: [{ prop: 'scale.x', to:   1}, { prop: 'scale.y', to:   1}], duration: 6 * FRAME_RATE / this._fSpeed_num, ease: Easing.sine.easeOut},
			{tweens: [], duration: 10 * FRAME_RATE / this._fSpeed_num, ease: Easing.sine.easeOut,
				onfinish: () =>
				{
					if (this._fKilledByBomb_bl)
					{
						this._fCoins_sprt_arr = [];

						let lWinCoins_int = Math.floor(this._fPayoutValue_num / this.currentStake) || 1;
						if (lWinCoins_int > 15) lWinCoins_int = 15;
						this._fCoinsCounter_int = lWinCoins_int;

						for (let i = 0; i < this._fCoinsCounter_int; i++)
						{
							this._fCoins_sprt_arr.push(this.addChild(this._generateCoin()));
						}

						this._fillDropPath(this._startPathPoint, this._startOffset, this._speed);

						this._startFinalFlareAnimation();
						this._playWinTierSoundSuspicion(this._calcWinTier(this._fPayoutValue_num));
						this._fCoinsMoneyStep_num = Math.ceil(((this._fPayoutValue_num + this._fKillBonusPay) / this._fCoinsCounter_int));
						this._fPayValue_num = this._fPayoutValue_num;
						this._onCoinFlyInTime();
					}

					Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));
					this._fPayout_wp.scale.set(1);
					this._startPayoutFlyOut();
				}
			}
		];

		Sequence.start(this._fPayout_wp, lSeq_arr);
	}

	_startJoinFlareAnimation()
	{
		if (!this._fPayout_wp) return;

		this._destroyFlareIfRequired();
		let lFlare_sprt = this._fFlare_sprt = this.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		lFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFlare_sprt.position.set(this._fPayout_wp.position.x, this._fPayout_wp.position.y);

		lFlare_sprt.scale.set(0);

		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 4}, {prop: 'scale.y', to: 2}], duration: 2 * FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2}, {prop: 'scale.y', to: 2}], duration: 2 * FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0}, {prop: 'scale.y', to: 0}], duration: 2 * FRAME_RATE, onfinish: () => {
				lFlare_sprt && lFlare_sprt.destroy();
				this._fFlare_sprt = null;
			}}
		];

		Sequence.start(lFlare_sprt, lFlareSeq_arr);
	}

	_startFinalFlareAnimation()
	{
		if (!this._fPayout_wp) return;

		Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));

		this._destroyFlareIfRequired();
		let lFlare_sprt = this._fFlare_sprt = this.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		lFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFlare_sprt.position.set(this._fPayout_wp.position.x, this._fPayout_wp.position.y);

		lFlare_sprt.scale.set(0);

		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 4.5},		{prop: 'scale.y', to: 2.2}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2.2},		{prop: 'scale.y', to: 2.2}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},			{prop: 'scale.y', to: 0}],			duration: 4*FRAME_RATE, onfinish: () => {
				lFlare_sprt && lFlare_sprt.destroy();
				this._fFlare_sprt = null;
			}}
		];

		Sequence.start(lFlare_sprt, lFlareSeq_arr);
	}

	_triggerJoining()
	{
		this._fJoiningTriggered_bln = true;
		this._fJoiningValuesCount_num = APP.currentWindow.awardingController.view.getSameAwardCount(this._fRid_num, this._fSeatId_int);

		if (this._fJoiningValuesCount_num > 0)
		{
			this.emit(CoinsAward.EVENT_ON_COINS_JOIN_REQUIRED, {rid: this._fRid_num, joinPosition: this._fPayout_wp.position});
		}
		else
		{
			if (this._fOnCoinsLanded_bln)
			{
				this._startPayoutFlyOut();
			}
			else
			{
				this._fIsMassExplosiveWin_bln = false;
			}
		}
	}

	_createWinPayout(aOptFontScale_num)
	{
		return (this.isMasterSeat ?	new WinPayout(aOptFontScale_num)
								  : new SilverWinPayout(aOptFontScale_num));
	}

	_createWinPayoutGlow(aOptFontScale_num)
	{
		return new WhiteWinPayout(aOptFontScale_num);
	}

	_getOffscreenOffsetY(aPayoutPosY_num)
	{
		let lDelta_num = this._fDelta_num || 0;

		let lOffscreenOffsetY_num = 0;
		let lPayoutHeigth_num = this._fPayout_wp.getBounds().height
		if(aPayoutPosY_num - lPayoutHeigth_num/2 < 16) /* 16 - not to be covered by top black bar (infobar)*/
		{
			lOffscreenOffsetY_num = 16 - (aPayoutPosY_num - lPayoutHeigth_num / 2) + lDelta_num;
		}
		else if(aPayoutPosY_num + lPayoutHeigth_num/2 > 540)
		{
			lOffscreenOffsetY_num = 540 -(aPayoutPosY_num+lPayoutHeigth_num/2) - lDelta_num;
		}

		return lOffscreenOffsetY_num;
	}

	_getOffscreenOffsetX(aPayoutPosX_num)
	{
		let lOffscreenOffsetX_num = 0;

		let lMaxPayoutScale_num = this._fChMult_num > 1 ? 2 : 1.4; /* 2 - more then max scale mult payout presentation(1.8) + additional offset*/
		let lPayoutWidth_num = this._fPayout_wp.getBounds().width * lMaxPayoutScale_num / this._fPayout_wp.scale.x;

		if(aPayoutPosX_num - lPayoutWidth_num/2 < 0)
		{
			lOffscreenOffsetX_num = 0-(aPayoutPosX_num - lPayoutWidth_num/2);
		}
		else if(aPayoutPosX_num + lPayoutWidth_num/2 > 960)
		{
			lOffscreenOffsetX_num = 960-(aPayoutPosX_num+lPayoutWidth_num/2);
		}

		return lOffscreenOffsetX_num;
	}

	_fillDropPath(aStart_obj, startOffset, aSpeed_num)
	{
		let lOrigStartX_num = aStart_obj ? aStart_obj.x : 0;
		lOrigStartX_num += startOffset.x;
		let lOrigStartY_num = (aStart_obj ? aStart_obj.y : 0);
		lOrigStartY_num += startOffset.y;

		this._fDropPaths_arr_arr = [];

		for (let i = 0; i < this._fCoinsCounter_int; ++i)
		{
			let lCoinParams_obj = COINS_ANIMATIONS_PARAMS[i];
			let lFinishX_num = lCoinParams_obj.x/2 * this._fCoinsScale_num;
			let lFinishY_num = lCoinParams_obj.y/2 * this._fCoinsScale_num + 30;
			let lFinalScale_num = this._fIsQuestCompleateAward_b ? 1.5 : 1;
			let lStartX_num = lOrigStartX_num;
			let lStartY_num = lOrigStartY_num + 20;
			let lWiggleYOffset_num = 25;

			if (!this.isMasterSeat)
			{
				lFinishX_num *= 0.53;
				lFinishY_num *= 0.53;
				lStartY_num -= 50;
				lWiggleYOffset_num *= 0.53;
			}

			lFinishX_num += lStartX_num + this._fOffscreenOffsetX_num;
			lFinishY_num += lStartY_num + this._fOffscreenOffsetY_num;

			let speedMultiplier = (this._fIsQuestCompleateAward_b ? 0.5 : 1) * 0.7;

			let seq;
			if (!this._fIsQuestCompleateAward_b)
			{
				seq = [
					{
						tweens: [],
						duration: lCoinParams_obj.startFrame + FRAME_RATE
					},
					{
						tweens: [	{prop:"position.x",	to: lFinishX_num},
									{prop:"position.y",	to: lFinishY_num},
									{prop: 'scale.x', to: -1 * lFinalScale_num * this._fCoinsScale_num},
									{prop: 'scale.y', to: 1 * lFinalScale_num * this._fCoinsScale_num}
								],
						duration: (10 * FRAME_RATE / aSpeed_num) * speedMultiplier,
						ease: Easing.sine.easeOut
					},
					{
						tweens: [
							{ prop: 'position.y', to: lFinishY_num + lWiggleYOffset_num}
						],
						duration: (15 * FRAME_RATE / aSpeed_num) * speedMultiplier,
						ease: Easing.sine.easeOut
					},
					{
						tweens: [
							{ prop: 'position.y', to: lFinishY_num}
						],
						duration: (15 * FRAME_RATE / aSpeed_num) * speedMultiplier,
						ease: Easing.sine.easeOut
					}
				];
			}
			else
			{
				seq = [
					{
						tweens: [],
						duration: lCoinParams_obj.startFrame + FRAME_RATE
					},
					{
						tweens: [	{prop:"position.x",	to: lFinishX_num},
									{prop:"position.y",	to: lFinishY_num},
									{prop: 'scale.x', to: -1 * lFinalScale_num * this._fCoinsScale_num},
									{prop: 'scale.y', to: 1 * lFinalScale_num * this._fCoinsScale_num}
								],
						duration: (5 * FRAME_RATE / aSpeed_num) * speedMultiplier,
						ease: Easing.sine.easeOut
					},
					{
						tweens: [],
						duration: 10 * FRAME_RATE
					},
				];
			}

			if (this._fCoinExplosion_bln)
			{
				for (let j = 0; j < 4; j++)
				{
					seq.push({
								tweens: [
									{ prop: 'position.y', to: lFinishY_num+(j%2?lWiggleYOffset_num:-lWiggleYOffset_num)}
								],
								duration: 10 * FRAME_RATE / aSpeed_num,
								ease: Easing.sine.easeOut
							})
				}
			}

			let startPoint = this._fKilledByBomb_bl ? new PIXI.Point(lFinishX_num - Math.floor(2+Math.random()*3), lFinishY_num - Math.floor(2+Math.random()*3)) : new PIXI.Point(lStartX_num, lStartY_num);

			this._fDropPaths_arr_arr.push([startPoint, seq]);
		}
	}

	_playWinTierSoundSuspicion(aWinTier_int)
	{
		if (this.isMasterSeat)
		{
			this._playWinTierSound(aWinTier_int);
		}
	}

	_playWinTierSound(aWinTier_int)
	{
		if (!this.isMasterSeat)
		{
			return;
		}

		let lSoundId_str = "wins_";
		let lSoundTier_num = this._fSpecifiedWinSoundTier || aWinTier_int;
		if (lSoundTier_num == WinTierUtil.WIN_TIERS.TIER_SMALL) lSoundId_str += "small";
		else if (lSoundTier_num == WinTierUtil.WIN_TIERS.TIER_MEDIUM) lSoundId_str += "medium_";
		else if (lSoundTier_num == WinTierUtil.WIN_TIERS.TIER_BIG) lSoundId_str += "large_";
		else lSoundId_str = null;

		this._interrupPreviousWinSound(lSoundId_str);

		lSoundId_str = this._prepareWinSoundId(lSoundId_str);

		APP.soundsController.play(lSoundId_str);
	}

	_prepareWinSoundId(aPrefix_str)
	{
		let lResult_str = aPrefix_str;

		if (aPrefix_str.indexOf("medium_") != -1)
		{
			lResult_str = aPrefix_str + getMediumWinNumber();
		}
		else if (aPrefix_str.indexOf("large_") != -1)
		{
			lResult_str = aPrefix_str + getLargeWinNumber();
		}

		return lResult_str;
	}

	_playCoinDropSoundSuspicion()
	{
		if(this.isMasterSeat)
		{
			APP.soundsController.play('wins_coincollect');
		}
	}

	//_playCoin

	_startContinuousDelay()
	{
		this._continueAwarding();
	}

	_interrupPreviousWinSound(aSoundId_str)
	{
		if (APP.soundsController.isSoundPlaying(aSoundId_str))
		{
			APP.soundsController.stop(aSoundId_str);
		}
	}

	_calcWinTier(aMoneyValue_num)
	{
		let lCurrentStake_num = (APP.isBattlegroundGame ? FIXED_BATTLEGOUND_STAKE : this.currentStake)*APP.playerController.info.betLevel;
		let lWinTier_int = WinTierUtil.calcWinTier(aMoneyValue_num, lCurrentStake_num);

		if (lWinTier_int === 0)
		{
			throw new Error('CoinsAward can\'t be created for zero wins!');
		}
		return lWinTier_int;
	}

	_onCoinFlyInTime(aCoinIndex_int = 0)
	{
		this._fCoinAnimationTimer_t && this._fCoinAnimationTimer_t.destructor();
		this._fCoinAnimationTimer_t = null;
		this._fCoinsTimer_t && this._fCoinsTimer_t.destructor();

		let lCoin_sprt = this._fCoins_sprt_arr[aCoinIndex_int];
		if (lCoin_sprt)
		{
			let lStartPosition_pt = this._fDropPaths_arr_arr[aCoinIndex_int][0];

			let lStartScale_num = 0.71 * this._fCoinsScale_num;
			if (!this.isMasterSeat)
			{
				lStartScale_num *= 0.53 * this._fCoinsScale_num;
			}
			lCoin_sprt.scale.set(lStartScale_num);
			lCoin_sprt.scale.x *= -1;
			lCoin_sprt.position.set(lStartPosition_pt.x, lStartPosition_pt.y);
			lCoin_sprt.visible = true;
			lCoin_sprt.play();

			let lSequence_s = this._fDropPaths_arr_arr[aCoinIndex_int][1];
			Sequence.start(lCoin_sprt, lSequence_s).once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, () => {
				//if (aCoinIndex_int == this._fCoins_sprt_arr.length - 1)
				{
					this._startContinuousDelay();
				}
			});
		}

		if (aCoinIndex_int < this._fCoins_sprt_arr.length - 1)
		{
			let waitTime = this._fKilledByBomb_bl ? (0.1 * FRAME_RATE) : (1 * FRAME_RATE);
			if (!this.isMasterSeat)
			{
				waitTime = 0.1 * FRAME_RATE;
			}
			this._fCoinsTimer_t = new Timer(this._onCoinFlyInTime.bind(this, aCoinIndex_int + 1), waitTime);
		}
	}

	_continueAwarding()
	{
		this._startCoinFlyOut();
	}

	get _payoutScaleMultiplier()
	{
		return this.isMasterSeat ? 1 : 0.6;
	}

	_onPayoutTimerCounted()
	{
		if (this._fChMult_num > 1)
		{
			this._onMultiplierPayoutAppearTime();
		}
		if (this._fPowerUpMultiplier_num > 1)
		{
			this._onPowerUpMultiplierPayoutAppearTime();
		}
		else
		{
			this._onPayoutAppearTime();
		}

		this.emit(CoinsAward.EVENT_ON_PAYOUT_IS_VISIBLE, {value: this._fPayoutValue_num, seatId: this._fSeatId_int, isBoss: this._fIsBoss_bl});
	}

	_onPayoutAppearTime()
	{
		this._fPayout_wp.scale.set(0);
		this._fPayout_wp.visible = true;

		if (this._fCoinExplosion_bln)
		{
			this._startPayoutCounting();
		}

		let lSequence_s = this._getPayoutSequence();
		if (this._fIsMassExplosiveWin_bln)
		{
			lSequence_s = this._getMassExplodePayoutSequence();
		}

		if (this._fKilledByBomb_bl)
		{
			lSequence_s = this._getBombEnemyMassExplodePayoutSequence();
		}

		if (this._fCoinExplosion_bln)
		{
			let lFinishY_num = this._fPayout_wp.position.y;
			let lFinishX_num = this._fPayout_wp.position.x;
			for (let j = 0; j < 4; j++)
			{
				let lWiggleYOffset_num = Math.floor(2+Math.random()*3);
				let lWiggleXOffset_num = Math.floor(2+Math.random()*2);
				lSequence_s.push({
							tweens: [
								{ prop: 'position.y', to: lFinishY_num+(j%2?lWiggleYOffset_num:-lWiggleYOffset_num)},
								{ prop: 'position.x', to: lFinishX_num+(j%2?-lWiggleXOffset_num:lWiggleXOffset_num)}
							],
							duration: 10 * FRAME_RATE,
							ease: Easing.sine.easeOut
						})
			}
		}

		Sequence.start(this._fPayout_wp, lSequence_s);
	}

	_onMultiplierPayoutAppearTime()
	{
		let lScaleMultiplier_num = this._payoutScaleMultiplier;
		let lSpeed_num = this._fSpeed_num;

		this._fPayout_wp.scale.set(0);
		this._fPayout_wp.visible = true;

		let lSequence_s = [
			{ 	tweens:[	{prop:"scale.x", to: 1.37 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1.37 * lScaleMultiplier_num}		],
				duration:6*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeIn
			},
			{ 	tweens:[	{prop:"scale.x", to: 1 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1 * lScaleMultiplier_num}		],
				duration:8*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeOut
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y + 2},
						{prop:"position.x", to:this._fPayout_wp.position.x - 1} ],
				duration:7*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x}
																				],
				duration:10*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 1.5},
						{prop:"position.x", to:this._fPayout_wp.position.x + 2.5}
																				],
				duration:3*FRAME_RATE, onfinish: () => {
					let lCrit_anim = APP.gameScreen.gameField.getCriticalAnimationById(this._fRid_num, this._fEnemyId_num);
					if (lCrit_anim)
					{
						// Wait for animation end
					}
					else
					{
						// Animation already ended
						this._onMultiplierPayoutUpdateTime();
					}
			}}
		];

		Sequence.start(this._fPayout_wp, lSequence_s);
	}

	_onPowerUpMultiplierPayoutAppearTime()
	{
		let lScaleMultiplier_num = this._payoutScaleMultiplier;
		let lSpeed_num = this._fSpeed_num;

		this._fPayout_wp.scale.set(0);
		this._fPayout_wp.visible = true;

		let lSequence_s = [
			{ 	tweens:[	{prop:"scale.x", to: 1.37 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1.37 * lScaleMultiplier_num}		],
				duration:6*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeIn
			},
			{ 	tweens:[	{prop:"scale.x", to: 1 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1 * lScaleMultiplier_num}		],
				duration:8*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeOut
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y + 2},
						{prop:"position.x", to:this._fPayout_wp.position.x - 1} ],
				duration:7*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x}
																				],
				duration:10*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 1.5},
						{prop:"position.x", to:this._fPayout_wp.position.x + 2.5}
																				],
				duration:3*FRAME_RATE}
		];

		Sequence.start(this._fPayout_wp, lSequence_s);
	}

	_onPowerUpMultiplierAnimationEnded(aEvent_obj)
	{
		if (
			aEvent_obj.rid === this._fRid_num
			&& aEvent_obj.enemyId === this._fEnemyId_num
			&& this._fPayout_wp
		 )
		{
			APP.gameScreen.gameField.off(GameField.EVENT_ON_PUWER_UP_ANIMATION_ENDED, this._onPowerUpMultiplierAnimationEnded, this);
			this._onPowerUpMultiplierPayoutUpdateTime();
		}
	}

	_onPowerUpMultiplierPayoutUpdateTime()
	{
		Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));

		this._fPayout_wp.value = this._fPayoutValue_num;
		this._fPayout_wp.scale.set(1.6);
		this._fPayout_wp.scaleTo(1, 8*FRAME_RATE);

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startMultiplyAnimation();
		}

		let lSequence_s = [
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x + 3}
																				],
				duration:5*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 1},
						{prop:"position.x", to:this._fPayout_wp.position.x + 1}
																				],
				duration:6*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y + 2},
						{prop:"position.x", to:this._fPayout_wp.position.x - 1} ],
				duration:7*FRAME_RATE
			},
			{
			 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x}
																				],
				duration:10*FRAME_RATE, onfinish: () => {
					this._startPayoutFlyOut();
					this._fPowerUpMultiplier_num = 0;
			}},
		];

		Sequence.start(this._fPayout_wp, lSequence_s);
	}

	_onCriticalHitAnimationEnded(aEvent_obj)
	{
		if (
				aEvent_obj.rid === this._fRid_num
				&& aEvent_obj.enemyId === this._fEnemyId_num
		 		&& this._fPayout_wp
		 	)
		{
			APP.gameScreen.gameField.off(GameField.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED, this._onCriticalHitAnimationEnded, this);
			this._onMultiplierPayoutUpdateTime();
		}
	}

	_onMultiplierPayoutUpdateTime()
	{
		Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));

		this._fPayout_wp.value = this._fPayoutValue_num;
		this._fPayout_wp.scale.set(1.6);
		this._fPayout_wp.scaleTo(1, 8*FRAME_RATE);

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startMultiplyAnimation();
		}

		let lSequence_s = [
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x + 3}
																				],
				duration:5*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 1},
						{prop:"position.x", to:this._fPayout_wp.position.x + 1}
																				],
				duration:6*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y + 2},
						{prop:"position.x", to:this._fPayout_wp.position.x - 1} ],
				duration:7*FRAME_RATE
			},
			{
			 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x}
																				],
				duration:10*FRAME_RATE, onfinish: () => {
					this._startPayoutFlyOut();
					this._fChMult_num = 0;
			}},
		];

		Sequence.start(this._fPayout_wp, lSequence_s);
	}

	_startMultiplyAnimation(aPosOpt_obj, aParticlaScaleOpt_num = 2)
	{
		if (this._fMultFlare_sprt)
		{
			//already exist
			return; //TODO probaby to find out what exactly are the cases when double _startMultiplyAnimation is called and prevent it
		}

		let lPos_obj = aPosOpt_obj || {x: this._fPayout_wp.position.x, y: this._fPayout_wp.position.y};
		this._fParticlesContainer_sprt.position.set(lPos_obj.x, lPos_obj.y);

		this._startParticle({x: PARTICLLES_ANIMATIONS_PARAMS[0].x, y: PARTICLLES_ANIMATIONS_PARAMS[0].y}, Utils.gradToRad(30), aParticlaScaleOpt_num, this._fParticlesContainer_sprt);
		this._startParticle({x: PARTICLLES_ANIMATIONS_PARAMS[1].x, y: PARTICLLES_ANIMATIONS_PARAMS[1].y}, Utils.gradToRad(200), aParticlaScaleOpt_num, this._fParticlesContainer_sprt);
		this._startCircleBlast(lPos_obj.x, lPos_obj.y);

		this._fMultFlare_sprt = this.addChild(APP.library.getSprite("critical_hit/flare"));
		this._fMultFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fMultFlare_sprt.position.set(lPos_obj.x, lPos_obj.y);

		this._fMultFlare_sprt.scale.set(0);
		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 0.471},	{prop: 'scale.y', to: 0.471}, {prop: 'rotation', to: Utils.gradToRad(10.8)}],	duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},	{prop: 'scale.y', to: 0}, {prop: 'rotation', to: Utils.gradToRad(32.6)}],	duration: 15*FRAME_RATE, onfinish: () => {
				this._fMultFlare_sprt && this._fMultFlare_sprt.destroy();
				this._fMultFlare_sprt = null;
			}}
		];

		Sequence.start(this._fMultFlare_sprt, lFlareSeq_arr);
	}

	_startCircleBlast(x, y, aContainer)
	{
		if (this._fCircleBlast_sprt)
		{
			//already exists
			return;
		}

		if (aContainer)
		{
			this._fCircleBlast_sprt = aContainer.addChild(APP.library.getSprite("awards/circle_blast"));
			this._fCircleBlastSecond_sprt = aContainer.addChild(APP.library.getSprite("awards/circle_blast"));
		}
		else
		{
			this._fCircleBlast_sprt = this.addChild(APP.library.getSprite("awards/circle_blast"));
			this._fCircleBlastSecond_sprt = this.addChild(APP.library.getSprite("awards/circle_blast"));
		}
		this._fCircleBlast_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fCircleBlast_sprt.position.set(x, y);
		this._fCircleBlast_sprt.alpha = 0.8;
		this._fCircleBlast_sprt.scale.set(0);

		let lBlastSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 0.765},	{prop: 'scale.y', to: 0.765}, {prop: 'alpha', to: 0}],	duration: 9*FRAME_RATE,
			onfinish: () => {
				this._fCircleBlast_sprt && this._fCircleBlast_sprt.destroy();
				this._fCircleBlast_sprt = null;
			}}
		];

		Sequence.start(this._fCircleBlast_sprt, lBlastSeq_arr);

		this._fCircleBlastSecond_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fCircleBlastSecond_sprt.position.set(x, y);
		this._fCircleBlastSecond_sprt.alpha = 0.8;
		this._fCircleBlastSecond_sprt.scale.set(0);

		lBlastSeq_arr = [
			{tweens:[], duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0.765},	{prop: 'scale.y', to: 0.765}, {prop: 'alpha', to: 0}],	duration: 9*FRAME_RATE,
			onfinish: () => {
				this._fCircleBlastSecond_sprt && this._fCircleBlastSecond_sprt.destroy();
				this._fCircleBlastSecond_sprt = null;
			}}
		];

		Sequence.start(this._fCircleBlastSecond_sprt, lBlastSeq_arr);
	}

	_startParticle(aPos_obj, aRot_num, aParticlaScaleOpt_num, aCountainer_spr, callback = null)
	{
		let lParticle_sprt = aCountainer_spr.addChild(new Sprite());
		lParticle_sprt.textures = _criticalParticlesTextures;
		lParticle_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lParticle_sprt.scale.set(aParticlaScaleOpt_num);
		lParticle_sprt.rotation = aRot_num;
		lParticle_sprt.position.set(aPos_obj.x, aPos_obj.y);
		lParticle_sprt.on('animationend', () => {
			callback && callback();
			lParticle_sprt && lParticle_sprt.destroy();
			lParticle_sprt = null;
		});
		lParticle_sprt.play();
		lParticle_sprt.fadeTo(1, 10*FRAME_RATE, null, () => {
			lParticle_sprt.fadeTo(0, 10*FRAME_RATE);
		});
	}

	_getPayoutSequence()
	{
		let lScaleMultiplier_num = this._payoutScaleMultiplier;
		let lSpeed_num = this._fSpeed_num;

		this._fPayout_wp.visible = true;

		let lYOffset_num = 50;
		if (!this.isMasterSeat)
		{
			lYOffset_num *= 0.53;
		}

		let lTargetScale_num = 0.8 * lScaleMultiplier_num;
		let lEndY_num = this._fPayout_wp.position.y - lYOffset_num;

		this._fPayout_wp.scale.set(lTargetScale_num);
		lEndY_num += this._getOffscreenOffsetY(lEndY_num);

		this._fPayout_wp.scale.set(0);

		return [
			{ 	tweens:[{prop:"scale.x", to: 0.97 * lScaleMultiplier_num},
						{prop:"scale.y", to: 0.97 * lScaleMultiplier_num}],
				duration: 3*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeIn
			},
			{ 	tweens:[{prop:"scale.x", to: 0.6 * lScaleMultiplier_num},
						{prop:"scale.y", to: 0.6 * lScaleMultiplier_num}],
				duration: 3*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeOut
			},
			{ 	tweens:[{prop:"scale.x", to: lTargetScale_num},
						{prop:"scale.y", to: lTargetScale_num},
						{prop:"position.y", to: lEndY_num}],
				duration: 5*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeOut,
				onfinish: ()=>{
					this.emit(CoinsAward.EVENT_ON_PAYOUT_APPEARED);
				}
			}
		];
	}

	_getMassExplodePayoutSequence()
	{
		let lScaleMultiplier_num = this._payoutScaleMultiplier;
		let lSpeed_num = this._fSpeed_num;

		return [
			{ 	tweens:[	{prop:"scale.x", to: 1.37 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1.37 * lScaleMultiplier_num}		],
				duration:6*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeIn
			},
			{ 	tweens:[	{prop:"scale.x", to: 1 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1 * lScaleMultiplier_num}		],
				duration:8*FRAME_RATE / lSpeed_num , ease:Easing.sine.easeOut
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y + 2},
						{prop:"position.x", to:this._fPayout_wp.position.x - 1} ],
				duration:7*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x}
																				],
				duration:10*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 2},
						{prop:"position.x", to:this._fPayout_wp.position.x + 3}
																				],
				duration:8*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 1},
						{prop:"position.x", to:this._fPayout_wp.position.x + 1}
																				],
				duration:6*FRAME_RATE, onfinish: ()=>{
					if (!this._fJoiningTriggered_bln && this._fIsMassExplosiveWin_bln)
					{
						this._triggerJoining();
					}
					this.emit(CoinsAward.EVENT_ON_PAYOUT_APPEARED);
				}
			}
		];
	}

	_getBombEnemyMassExplodePayoutSequence()
	{
		let lScaleMultiplier_num = this._payoutScaleMultiplier;
		let lSpeed_num = this._fSpeed_num;

		return [
			{ 	tweens:[	{prop:"scale.x", to: 1.37 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1.37 * lScaleMultiplier_num}		],
				duration:6*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeIn
			},
			{ 	tweens:[	{prop:"scale.x", to: 1 * lScaleMultiplier_num},
						{prop:"scale.y", to: 1 * lScaleMultiplier_num}		],
				duration:8*FRAME_RATE / lSpeed_num, ease:Easing.sine.easeOut
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y + 2},
						{prop:"position.x", to:this._fPayout_wp.position.x - 1} ],
				duration:7*FRAME_RATE
			},
			{ 	tweens:[	{prop:"position.y", to:this._fPayout_wp.position.y - 1},
						{prop:"position.x", to:this._fPayout_wp.position.x + 1}
																				],
				duration:6*FRAME_RATE, onfinish: ()=>{
					if (!this._fJoiningTriggered_bln && this._fIsMassExplosiveWin_bln)
					{
						this._triggerJoining();
					}
					this.emit(CoinsAward.EVENT_ON_PAYOUT_APPEARED);
				}
			}
		];
	}

	_startPayoutCounting()
	{
		let lValue_num = PAYOUTS_SETTINGS[0].percent*this._fPayoutValue_num;
		this._updatePayoutValue(lValue_num, 0);

		for (let i = 1; i < PAYOUTS_SETTINGS.length; ++i)
		{
			this._generateNextPayoutTimer(PAYOUTS_SETTINGS[i].timeout, PAYOUTS_SETTINGS[i].percent*this._fPayoutValue_num, i);
		}
	}

	_generateNextPayoutTimer(aTime_num, aValue_num, aId_num)
	{
		let lTimer_t = new Timer(()=>{
			this._updatePayoutValue(aValue_num, aId_num);
		}, aTime_num);
		this._fTimers_arr.push(lTimer_t);
	}

	_updatePayoutValue(aVal_num, aId_num)
	{
		if (!this._fPayout_wp) return;

		this._setValue(aVal_num);

		let lBaseScale_num = 1*this._payoutScaleMultiplier*0.726;
		let lScalePrev_num = lBaseScale_num + ((aId_num > 0) ? 0.3*((aId_num-1)/(PAYOUTS_SETTINGS.length-1)) : 0);
		let lScale_num = lBaseScale_num + 0.3*(aId_num/(PAYOUTS_SETTINGS.length-1));
		let lSeq_arr = [
			{ tweens:[	{prop:"scale.x", to:lScalePrev_num-0.05},	{prop:"scale.y", to:lScalePrev_num-0.05}],	duration:2*FRAME_RATE, ease:Easing.sine.easeOut},
			{ tweens:[	{prop:"scale.x", to:lScale_num},	{prop:"scale.y", to:lScale_num}],	duration:2*FRAME_RATE, ease:Easing.sine.easeIn}
		];

		Sequence.start(this._fPayout_wp, lSeq_arr);
	}

	//Bundle award...
	_onTick()
	{
		if (this._fFollowEnemy_bln)
		{
			this._updateSelfPosition();
		}
	}

	_updateSelfPosition()
	{
		let lEnemyPos_obj = APP.gameScreen.gameField.getEnemyPosition(this._fEnemyId_num, true);
		if (lEnemyPos_obj && this._fPayout_wp)
		{
			this._fPayout_wp.position.x -= this._fOffscreenOffsetX_num;
			this._fPayout_wp.position.y -= this._fOffscreenOffsetY_num;


			//PREVENTING OUT OF SCREEN...
			let lLeftMostBorderX_num = 50;
			let lRightMostBorderX_num = 910;
			let lTopMostBorderY_num = 50;
			let lBottomMostBorderY_num = 520;

			if(lEnemyPos_obj.x < lLeftMostBorderX_num)
			{
				lEnemyPos_obj.x = lLeftMostBorderX_num;
			}
			else if(lEnemyPos_obj.x > lRightMostBorderX_num)
			{
				lEnemyPos_obj.x = lRightMostBorderX_num;
			}

			if(lEnemyPos_obj.y < lTopMostBorderY_num)
			{
				lEnemyPos_obj.y = lTopMostBorderY_num;
			}
			else if(lEnemyPos_obj.y > lBottomMostBorderY_num)
			{
				lEnemyPos_obj.y = lBottomMostBorderY_num;
			}
			//...PREVENTING OUT OF SCREEN

			let startOffset = this._fParams_obj.startOffset || {x: 0, y: 0}
			let lPayoutPos_obj = {x: lEnemyPos_obj.x + startOffset.x, y: lEnemyPos_obj.y - 60 + startOffset.y};

			this._fOffscreenOffsetX_num = this._getOffscreenOffsetX(lPayoutPos_obj.x);
			this._fOffscreenOffsetY_num = this._getOffscreenOffsetY(lPayoutPos_obj.y - 80);

			this._fPayout_wp.position.x += this._fOffscreenOffsetX_num;
			this._fPayout_wp.position.y += this._fOffscreenOffsetY_num;

			let lX_num = lEnemyPos_obj.x - this._fParams_obj.start.x;
			let lY_num = lEnemyPos_obj.y - this._fParams_obj.start.y;

			this._fPayoutContainer_sprt.position.set(lX_num, lY_num);
		}
	}

	_hidePayout()
	{
		this._fPayout_wp.visible = false;
		this._fNoPayout_bln = true;
		this._fFollowEnemy_bln = false;
		this._fUncountedValue_num = 0;

		if (this._fOnCoinsLanded_bln && this._fBundleAward_bln)
		{
			this._completeAwarding(true, false);
		}
	}

	_updateBundleValue(aVal_num, aNoGlow_bln = false)
	{
		let lPrevPayoutValue_num = this._fPayoutValue_num;
		this._fPayoutValue_num = aVal_num;
		this._fUncountedValue_num += (this._fPayoutValue_num-lPrevPayoutValue_num);

		this._updateBundlePayoutValue(aVal_num, 0.3);
		if (!aNoGlow_bln)
		{
			this._startGlow(0xffdb4d, 5*FRAME_RATE);
		}
	}

	_showFinalValue(aVal_num)
	{
		let lPrevPayoutValue_num = this._fPayoutValue_num;
		this._fPayoutValue_num = aVal_num;
		this._fUncountedValue_num += (this._fPayoutValue_num-lPrevPayoutValue_num);
		let lPos_obj = {x: this._fPayout_wp.position.x + this._fPayoutContainer_sprt.position.x, y: this._fPayout_wp.position.y + this._fPayoutContainer_sprt.position.y};

		this._fFollowEnemy_bln = false;

		this._updateFinalBundlePayoutValue(aVal_num, 0.6);
		this._startGlow(0xffffff, 6*FRAME_RATE, 1.03);

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startMultiplyAnimation(lPos_obj, 1.5);
		}
	}

	_updateBundlePayoutValue(aVal_num, aScaleAdd_num)
	{
		if (!this._fPayout_wp) return;

		this._setValue(aVal_num);

		let lBaseScale_num = this._fPayout_wp.scale.x;
		let lScale_num = lBaseScale_num + aScaleAdd_num;
		let lSeq_arr = [
			{ tweens:[	{prop:"scale.x", to:lScale_num},		{prop:"scale.y", to:lScale_num}],		duration:2*FRAME_RATE, ease:Easing.sine.easeOut},
			{ tweens:[	{prop:"scale.x", to:lBaseScale_num},	{prop:"scale.y", to:lBaseScale_num}],	duration:6*FRAME_RATE, ease:Easing.sine.easeIn}
		];

		Sequence.start(this._fPayout_wp, lSeq_arr);
	}

	_updateFinalBundlePayoutValue(aVal_num, aScaleAdd_num)
	{
		if (!this._fPayout_wp) return;

		this._setValue(aVal_num);

		let lBaseScale_num = this._fPayout_wp.scale.x;
		let lScale_num = lBaseScale_num + aScaleAdd_num;
		let lSeq_arr = [
			{ tweens:[	{prop:"scale.x", to:lScale_num},		{prop:"scale.y", to:lScale_num}],		duration:2*FRAME_RATE, ease:Easing.sine.easeOut},
			{ tweens:[	{prop:"scale.x", to:lScale_num-0.1},	{prop:"scale.y", to:lScale_num-0.1}],	duration:6*FRAME_RATE, ease:Easing.sine.easeIn},
			{ tweens:[],																				duration:6*FRAME_RATE, ease:Easing.sine.easeIn,
				onfinish: ()=>{
				this._startFinalCoinsAnimation(null, aVal_num);
				this._startBundleDisappearAnimation();
			}}
		];

		Sequence.start(this._fPayout_wp, lSeq_arr);
	}

	_startBundleDisappearAnimation()
	{
		if (!this._fPayoutContainer_sprt || !this._fPayout_wp) return;

		let lFlare_sprt = this._fPayoutContainer_sprt.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		lFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFlare_sprt.position.set(this._fPayout_wp.position.x, this._fPayout_wp.position.y);

		lFlare_sprt.scale.set(0);
		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 3.6},		{prop: 'scale.y', to: 1.8}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2},			{prop: 'scale.y', to: 1.8}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},			{prop: 'scale.y', to: 0}],			duration: 4*FRAME_RATE, onfinish: () => {
				lFlare_sprt && lFlare_sprt.destroy();
				lFlare_sprt = null;
			}}
		];

		this._fBundleFlareSeq_s = Sequence.start(lFlare_sprt, lFlareSeq_arr);

		this._fPayout_wp.fadeTo(0, 6*FRAME_RATE, Easing.quadratic.easeInOut);
	}

	_startFinalCoinsAnimation(aPos_obj = null, aVal_num, aCoinsCount_num = null)
	{
		this._playWinTierSoundSuspicion(this._calcWinTier(this._fPayoutValue_num));

		this._fBundleCoinsCounter_int = Math.floor(aVal_num / this.currentStake) || 1;
		if (this._fBundleCoinsCounter_int > 15) this._fBundleCoinsCounter_int = 15;

		if (aCoinsCount_num)
		{
			this._fBundleCoinsCounter_int = aCoinsCount_num;
		}

		this._fBundleCoins_sprt_arr = [];
		for (let i = 0; i < this._fBundleCoinsCounter_int; i++)
		{
			this._fBundleCoins_sprt_arr.push(this.addChild(this._generateCoin()));
		}

		this._fBundlePayValue_num = this._fPayoutValue_num;
		this._fBundleCoinsMoneyStep_num = Math.ceil(this._fBundlePayValue_num/this._fBundleCoinsCounter_int);
		this._fBundleCoinsMoneyCounter_num = 0;

		if (!aPos_obj)
		{
			let lEnemyPos_obj = APP.gameScreen.gameField.getEnemyPosition(this._fEnemyId_num, true);
			if (lEnemyPos_obj)
			{
				let lX_num = lEnemyPos_obj.x - this._fParams_obj.start.x;
				let lY_num = lEnemyPos_obj.y - this._fParams_obj.start.y;
				this._fillBundleDropPath(this._fParams_obj.start, {x: lX_num, y: lY_num}, this._fSpeed_num);

				this._onBundleCoinFlyInTime();
			}
			else
			{
				this._fOnCoinsLanded_bln = true;
				this._completeAwarding();
			}
		}
		else
		{
			let lX_num = aPos_obj.x - this._fParams_obj.start.x;
			let lY_num = aPos_obj.y - this._fParams_obj.start.y;
			this._fillBundleDropPath(this._fParams_obj.start, {x: lX_num, y: lY_num + 100}, this._fSpeed_num);

			this._onBundleCoinFlyInTime();
		}
	}

	_fillBundleDropPath(aStart_obj={x: 0, y: 0}, startOffset, aSpeed_num)
	{
		let lOrigStartX_num = aStart_obj.x + startOffset.x;
		let lOrigStartY_num = aStart_obj.y + startOffset.y;

		this._fBundleDropPaths_arr_arr = [];

		for (let i = 0; i < this._fBundleCoinsCounter_int; ++i)
		{
			let lCoinParams_obj = COINS_ANIMATIONS_PARAMS[i];
			let lFinishX_num = lCoinParams_obj.x/2;
			let lFinishY_num = lCoinParams_obj.y/2;
			let lStartX_num = lOrigStartX_num;
			let lStartY_num = lOrigStartY_num + 20;
			let lWiggleYOffset_num = 25;

			if (!this.isMasterSeat)
			{
				lFinishX_num *= 0.53;
				lFinishY_num *= 0.53;
				lStartY_num -= 50;
				lWiggleYOffset_num *= 0.53;
			}

			lFinishX_num += lStartX_num + this._fOffscreenOffsetX_num;
			lFinishY_num += lStartY_num + this._fOffscreenOffsetY_num;

			let seq = [
				{tweens: [], duration: lCoinParams_obj.startFrame + FRAME_RATE},
				{tweens: [	{prop:"position.x",	to: lFinishX_num}, {prop:"position.y", to: lFinishY_num},{prop: 'scale.x', to: -1}, {prop: 'scale.y', to: 1}],
				duration: 6 * FRAME_RATE / aSpeed_num,ease: Easing.sine.easeOut},
				{tweens: [{ prop: 'position.y', to: lFinishY_num + lWiggleYOffset_num}],duration: 6 * FRAME_RATE / aSpeed_num, ease: Easing.sine.easeOut},
				{tweens: [{ prop: 'position.y', to: lFinishY_num}],duration: 6 * FRAME_RATE / aSpeed_num,ease: Easing.sine.easeOut}
			];

			this._fBundleDropPaths_arr_arr.push([new PIXI.Point(lFinishX_num - Math.floor(2+Math.random()*3), lFinishY_num - Math.floor(2+Math.random()*3)), seq]);
		}
	}

	_onBundleCoinFlyInTime(aCoinIndex_int = 0)
	{
		this._fBundleCoinsTimer_t && this._fBundleCoinsTimer_t.destructor();

		let lCoin_sprt = this._fBundleCoins_sprt_arr[aCoinIndex_int];
		if (lCoin_sprt)
		{
			let lStartPosition_pt = this._fBundleDropPaths_arr_arr[aCoinIndex_int][0];

			let lStartScale_num = 0.71;
			if (!this.isMasterSeat)
			{
				lStartScale_num *= 0.53;
			}
			lCoin_sprt.scale.set(lStartScale_num);
			lCoin_sprt.scale.x *= -1;
			lCoin_sprt.position.set(lStartPosition_pt.x, lStartPosition_pt.y);
			lCoin_sprt.visible = true;
			lCoin_sprt.play();

			let lSequence_s = this._fBundleDropPaths_arr_arr[aCoinIndex_int][1];
			Sequence.start(lCoin_sprt, lSequence_s).once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, () => {
				this._startBundleCoinFlyOut();
			});
		}

		if (aCoinIndex_int < this._fBundleCoins_sprt_arr.length - 1)
		{
			this._fBundleCoinsTimer_t = new Timer(this._onBundleCoinFlyInTime.bind(this, aCoinIndex_int + 1), 1*FRAME_RATE);
		}
	}

	_startBundleCoinFlyOut()
	{
		if (this._fBundleCoinsCounter_int > 0 && this._fBundleCoins_sprt_arr[this._fBundleCoinsCounter_int-1])
		{
			let lIndex_int = this._fBundleCoins_sprt_arr.length - this._fBundleCoinsCounter_int--;
			let lOutCoin_sprt = this._fBundleCoins_sprt_arr[lIndex_int];
			lOutCoin_sprt.position.x = this._fBundleCoins_sprt_arr[lIndex_int].position.x;
			lOutCoin_sprt.position.y = this._fBundleCoins_sprt_arr[lIndex_int].position.y;

			this._onBundleCoinFlyOutTime(lOutCoin_sprt);
		}
		else
		{
			this._completeAwarding();
		}
	}

	_onBundleCoinFlyOutTime(aCoin_sprt)
	{
		let lRestCoinsAmount_int = this._fBundleCoinsCounter_int;
		let lCoinFlyDuration_num = COIN_FLY_DURATION / this._fSpeed_num;

		if (aCoin_sprt)
		{
			aCoin_sprt.visible = true;
			aCoin_sprt.play();
			aCoin_sprt.moveTo(this._fEndPosition_pt.x, this._fEndPosition_pt.y, lCoinFlyDuration_num, null, () => {
				aCoin_sprt.destroy();
				if (this._fBundleAward_bln)
				{
					this._playCoinDropSoundSuspicion();

					if (!this._fBundlePayValue_num)
					{
						this._fBundleCoinsMoneyStep_num = 0;
					}
					else if ((this._fBundleCoinsMoneyCounter_num + this._fBundleCoinsMoneyStep_num) <= this._fBundlePayValue_num)
					{
						this._fBundleCoinsMoneyCounter_num += Number(this._fBundleCoinsMoneyStep_num);
					}
					else
					{
						this._fBundleCoinsMoneyStep_num = Number(this._fBundlePayValue_num) - this._fBundleCoinsMoneyCounter_num;
						this._fBundlePayValue_num = 0;
					}
					this._fCoinsMoneyCounter_num += this._fBundleCoinsMoneyStep_num;
					this.emit(CoinsAward.EVENT_ON_COIN_LANDED, {money: this._fBundleCoinsMoneyStep_num, seatId: this._fSeatId_int});
				}

				if (lRestCoinsAmount_int === 0)
				{
					if (this._fCoinsMoneyCounter_num <  this._fBundleCoinsMoneyCounter_num)
					{
						this._fCoinsMoneyCounter_num = this._fBundleCoinsMoneyCounter_num;
					}

					if (this._fIsQuestCompleateAward_b)
					{
						this.emit(CoinsAward.EVENT_ON_COIN_LANDED, {money: this._fPayValue_num, seatId: this._fSeatId_int});
					}

					this._completeAwarding();
				}
			});
		}
	}

	_startGlow(aColor_hex, aDuration_num, aScale_num = 1)
	{
		if (!this.isMasterSeat) return;

		let lGlow_sprt = this._createGlowCopy(this._fPayout_wp, 4, aColor_hex, aScale_num);
		this._fPayout_wp.addChild(lGlow_sprt);
		lGlow_sprt.position.set(-1, 2);
		let lSeq_arr = [
			{tweens: [{prop: 'alpha', to: 0}],	duration: aDuration_num, ease: Easing.sine.easeIn, onfinish: ()=>{lGlow_sprt && lGlow_sprt.destroy()}}
		];
		this._fGlowSeq_s = Sequence.start(lGlow_sprt, lSeq_arr);
	}

	_createGlowCopy(aSprite_sprt, aExtraSize_num, aColor_hex, aScale_num = 1)
	{
		if (!APP.profilingController.info.isVfxProfileValueMediumOrGreater || APP.isDeprecetedInternetExplorer)
		{
			return new Sprite();
		}

		let lGlowFilter_cmf = new GlowFilter({distance:8, outerStrength:0, innerStrength:2, color:aColor_hex, quality:1});

		let lOldPos_obj = {x: aSprite_sprt.position.x, y: aSprite_sprt.position.y};
		let lOldScale_num = aSprite_sprt.scale.x;
		aSprite_sprt.position.set(0, 0);
		aSprite_sprt.scale.set(1);

		aSprite_sprt.filters = [lGlowFilter_cmf];
		let lGlowBounds_obj = aSprite_sprt.getBounds();
		lGlowBounds_obj.height += aExtraSize_num;
		lGlowBounds_obj.y -= aExtraSize_num/2;
		lGlowBounds_obj.width += aExtraSize_num;
		lGlowBounds_obj.x -= aExtraSize_num/2;

		aSprite_sprt.position.set(lGlowBounds_obj.width/2, lGlowBounds_obj.height/2);
		let lGlowTexture_txt = PIXI.RenderTexture.create({ width: lGlowBounds_obj.width, height: lGlowBounds_obj.height, scaleMode: PIXI.SCALE_MODES.LINEAR, resolution: 2 });
		APP.stage.renderer.render(aSprite_sprt, { renderTexture: lGlowTexture_txt });
		aSprite_sprt.filters = [];

		let lGlowSprite_sprt = new Sprite();
		lGlowSprite_sprt.texture = lGlowTexture_txt;
		lGlowSprite_sprt.tint = aColor_hex;
		lGlowSprite_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lGlowSprite_sprt.scale.set(aScale_num);

		aSprite_sprt.position.set(lOldPos_obj.x, lOldPos_obj.y);
		aSprite_sprt.scale.set(lOldScale_num);

		return lGlowSprite_sprt;
	}
	//...Bundle award

	_setValue(aVal_num)
	{
		let lOldPos_obj = {x: this._fPayout_wp.position.x, y: this._fPayout_wp.position.y};
		let lOldScale_obj = {x: this._fPayout_wp.scale.x, y: this._fPayout_wp.scale.y};

		this._fPayout_wp.position.set(0, lOldPos_obj.y);
		this._fPayout_wp.scale.set(1, 1);

		this._fPayout_wp.value = aVal_num;

		this._fPayout_wp.position.set(lOldPos_obj.x, lOldPos_obj.y);
		this._fPayout_wp.scale.set(lOldScale_obj.x, lOldScale_obj.y);
	}

	_finishPayoutCounting()
	{
		for (let lTimer_t of this._fTimers_arr)
		{
			lTimer_t && lTimer_t.destructor();
			lTimer_t = null;
		}

		this._fTimers_arr = [];

		if (this._fPayout_wp)
		{
			this._setValue(this._fPayoutValue_num);
		}
	}

	_startPayoutFlyOut()
	{
		this._fPayout_wp && Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));
		this._finishPayoutCounting();

		if (this._fPayout_wp)
		{
			let duration = 6*FRAME_RATE;
			if (!this.isMasterSeat)
			{
				duration /= 2;
			}
			this._fPayout_wp.fadeTo(0, duration, Easing.quadratic.easeInOut, () => { this._completeAwarding() });
		}
		else
		{
			this._completeAwarding();
		}
	}

	_startCoinFlyOut()
	{
		if (this._fCoinsCounter_int > 0 && this._fCoins_sprt_arr[this._fCoinsCounter_int-1])
		{
			let lIndex_int = this._fCoins_sprt_arr.length - this._fCoinsCounter_int--;
			let lOutCoin_sprt = this._fCoins_sprt_arr[lIndex_int];
			lOutCoin_sprt.position.x = this._fCoins_sprt_arr[lIndex_int].position.x;
			lOutCoin_sprt.position.y = this._fCoins_sprt_arr[lIndex_int].position.y;

			this._onCoinFlyOutTime(lOutCoin_sprt);
		}
		else
		{
			this._onCoinsLanded();
		}
	}

	_onCoinFlyOutTime(aCoin_sprt)
	{
		let lRestCoinsAmount_int = this._fCoinsCounter_int;
		let lSpeed_num = this._fSpeed_num;
		let lCoinFlyDuration_num = COIN_FLY_DURATION / lSpeed_num;

		if (!this.isMasterSeat)
		{
			lCoinFlyDuration_num /= 2;
		}

		if (aCoin_sprt)
		{
			aCoin_sprt.visible = true;
			aCoin_sprt.play();
			aCoin_sprt.moveTo(this._fEndPosition_pt.x, this._fEndPosition_pt.y, lCoinFlyDuration_num, null, () => {
				if (!this._fBundleAward_bln)
				{
					if(aCoin_sprt === this._fCoins_sprt_arr[0])
					{
						this._playCoinDropSoundSuspicion();
					}
				}

				aCoin_sprt.destroy();

				if (!this._fPayValue_num || this._fBundleAward_bln)
				{
					this._fCoinsMoneyStep_num = 0;
				}
				else if ((this._fCoinsMoneyCounter_num + this._fCoinsMoneyStep_num) <= this._fPayValue_num + this._fKillBonusPay)
				{
					this._fCoinsMoneyCounter_num += Number(this._fCoinsMoneyStep_num);
				}
				else
				{
					this._fCoinsMoneyStep_num = Number(this._fPayValue_num) + this._fKillBonusPay - this._fCoinsMoneyCounter_num;
					this._fPayValue_num = 0;
				}

				this.emit(CoinsAward.EVENT_ON_COIN_LANDED, {money: this._fCoinsMoneyStep_num, seatId: this._fSeatId_int});

				if (lRestCoinsAmount_int === 0)
				{
					this._onCoinsLanded();
				}
			});
		}
	}

	_onCoinsLanded()
	{
		if (this._fNoPayout_bln)
		{
			if (this._fPayout_wp)
			{
				this._onAwardCounted(this._fPayoutValue_num);
			}
			super._completeAwarding();
			return;
		}

		this._fOnCoinsLanded_bln = true;
		if (this._fBundleAward_bln) return;

		if (
				this._fIsMassExplosiveWin_bln
				|| this._fIsQuestCompleateAward_b
			)
		{
			this._validateCompleteAwarding();
		}
		else
		{
			this._startPayoutFlyOut();
		}
	}

	_completeAwarding(aIsFinalAwardingSum = true, aIsEventAwardCountedNeeded_bln = true)
	{
		if (!this._fBundleAward_bln)
		{
			if (this._fPayout_wp)
			{
				this._onAwardCounted(this._fPayoutValue_num, aIsFinalAwardingSum);
			}

			this._validateCompleteAwarding();
		}
		else
		{
			if (aIsEventAwardCountedNeeded_bln)
			{
				super._onAwardCounted(this._fPayoutValue_num);
			}
			else
			{
				this._onAwardCountedWithoutEvent(this._fPayoutValue_num);
			}

			if (this._fPayout_wp)
			{
				Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));
				this._fPayout_wp.destroy();
				this._fPayout_wp = null;
			}

			if (this._fOnCoinsLanded_bln)
			{
				if (!APP.currentWindow.gameFrbController.info.frbMode)
				{
					this._checkAllCoinsDeliveredMoneyToWin();
				}
			}
			super._completeAwarding();
		}
	}


	_onAwardCountedWithoutEvent(aValue_num)
	{
		if (this._fUncountedValue_num !== undefined && aValue_num > 0)
		{
			this._fUncountedValue_num = Math.max(this._fUncountedValue_num - aValue_num, 0);
		}
	}

	_validateCompleteAwarding()
	{
		if (!this._fPayout_wp
			&& (
					this._fOnCoinsLanded_bln
					|| (
							!this._fIsBombEnemy_bl
							&& this._fKilledByBomb_bl
						)
				)
			)
		{
			super._completeAwarding();
		}
	}

	_checkAllCoinsDeliveredMoneyToWin()
	{
		if(this._fPayValue_num != 0 && this._fCoinsMoneyCounter_num < this._fPayValue_num)
		{
			const lUnlearnedWin_num = this._fPayValue_num - this._fCoinsMoneyCounter_num;
			this.emit(CoinsAward.EVENT_ON_COIN_LANDED, {money: lUnlearnedWin_num, seatId: this._fSeatId_int});
		}
	}

	_onAwardCounted(lPayoutValue_num, aIsFinalAwardingSum = true)
	{
		if (aIsFinalAwardingSum)
		{
			super._onAwardCounted(lPayoutValue_num);
		}

		if (this._fPayout_wp)
		{
			this._fPayoutTimer_t && this._fPayoutTimer_t.destructor();
			Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));
			this._fPayout_wp.destroy();
			this._fPayout_wp = null;
		}
	}

	_generateAwardCountedEventData(aValue_num)
	{
		let awardCountedEventData = super._generateAwardCountedEventData(aValue_num);
		awardCountedEventData.isQualifyWinDevalued = this.isWinDevalued;
		return awardCountedEventData;
	}

	_generateCoin()
	{
		let lCoin_sprt = new Sprite();
		if (this.isMasterSeat)
		{
			lCoin_sprt.textures = AtlasSprite.getFrames([APP.library.getAsset("common/coin_spin")], AtlasConfig.WinCoin, "");
		}
		else
		{
			lCoin_sprt.textures = AtlasSprite.getFrames([APP.library.getAsset("common/silver_coin_spin")], AtlasConfig.SilverWinCoin, "");
		}

		++this._fGenerateId_num;
		if (this._fGenerateId_num >= lCoin_sprt.textures.length) this._fGenerateId_num = 0;
		let lStartFrameIndex_num = ++this._fGenerateId_num;

		lCoin_sprt.anchor.set(0.5);
		lCoin_sprt.gotoAndStop(lStartFrameIndex_num);
		lCoin_sprt.visible = false;
		lCoin_sprt.zIndex = Z_INDEXES.COINS;

		return lCoin_sprt;
	}

	_destroyFlareIfRequired()
	{
		if (this._fFlare_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
			this._fFlare_sprt.destroy();
			this._fFlare_sprt = null;
		}
	}

	_interruptAnimationsAndTimers()
	{
		APP.off('tick', this._onTick, this);

		this._fPayoutTimer_t && this._fPayoutTimer_t.destructor();
		this._fPayoutTimer_t = null;

		this._fCoinsTimer_t && this._fCoinsTimer_t.destructor();
		this._fCoinsTimer_t = null;

		this._fStartTimer_tmr && this._fStartTimer_tmr.destructor();
		this._fStartTimer_tmr = null;

		this._fCoinAnimationTimer_t && this._fCoinAnimationTimer_t.destructor();
		this._fCoinAnimationTimer_t = null;

		this._fBundleCoinsTimer_t && this._fBundleCoinsTimer_t.destructor();
		this._fBundleCoinsTimer_t = null;

		this._fStartAddAnimationTimer_t && this._fStartAddAnimationTimer_t.destructor();
		this._fStartAddAnimationTimer_t = null;

		APP.gameScreen.gameField.off(GameField.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED, this._onCriticalHitAnimationEnded, this);
		APP.gameScreen.gameField.off(GameField.EVENT_ON_PUWER_UP_ANIMATION_ENDED, this._onPowerUpMultiplierAnimationEnded, this);

		while (this._fCoins_sprt_arr && this._fCoins_sprt_arr.length)
		{
			let lCoin_sprt = this._fCoins_sprt_arr.pop();
			Sequence.destroy(Sequence.findByTarget(lCoin_sprt));
			lCoin_sprt.destroy();
		}

		while (this._completedQuestPayoutEffects_arr && this._completedQuestPayoutEffects_arr.length)
		{
			let effect = this._completedQuestPayoutEffects_arr.pop();
			effect && Sequence.destroy(Sequence.findByTarget(effect));
		}
		this._completedQuestPayoutEffects_arr = null;

		this._fBottomFXsContainer_sprt = null;
		this._fTopFXsContainer_sprt = null;

		if (this._fFlare_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
			this._fFlare_sprt.destroy();
			this._fFlare_sprt = null;
		}

		if (this._fMultFlare_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fMultFlare_sprt));
			this._fMultFlare_sprt.destroy();
			this._fMultFlare_sprt = null;
		}

		if (this._fCircleBlast_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fCircleBlast_sprt));
			this._fCircleBlast_sprt.destroy();
			this._fCircleBlast_sprt = null;
		}

		if (this._fCircleBlastSecond_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fCircleBlastSecond_sprt));
			this._fCircleBlastSecond_sprt.destroy();
			this._fCircleBlastSecond_sprt = null;
		}

		while (this._fBundleCoins_sprt_arr && this._fBundleCoins_sprt_arr.length)
		{
			let lCoin_sprt = this._fBundleCoins_sprt_arr.pop();
			Sequence.destroy(Sequence.findByTarget(lCoin_sprt));
			lCoin_sprt.destroy();
		}

		if (this._fGlowSeq_s)
		{
			this._fGlowSeq_s.destructor();
			this._fGlowSeq_s = null;
		}

		if (this._fBundleFlareSeq_s)
		{
			this._fBundleFlareSeq_s.destructor();
			this._fBundleFlareSeq_s = null;
		}

		Sequence.destroy(Sequence.findByTarget(this._fPayout_wp));
	}

	destroy()
	{
		this._finishPayoutCounting();

		this._interruptAnimationsAndTimers();

		this._winDevalued_bl = false;

		super.destroy();

		this._fOnCoinsLanded_bln = undefined;
		this._fRid_num = undefined;
		this._fIsMassExplosiveWin_bln = undefined;
		this._fJoiningTriggered_bln = undefined;
		this._fEndPosition_pt = undefined;
		this._fDropPaths_arr_arr = undefined;
		this._fCoinsCounter_int = undefined;
		this._fPayValue_num = undefined;
		this._fCoinsMoneyStep_num = undefined;
		this._fCoinsMoneyCounter_num = undefined;
		this._fSpecifiedWinSoundTier = undefined;
		this._fGenerateId_num = undefined;
		this._fPayout_wp = undefined;
		this._fCoins_sprt_arr = undefined;
		this._fCoinsTimer_t = undefined;
		this._fPayoutTimer_t = undefined;
		this._fOffscreenOffsetX_num = undefined;
		this._fOffscreenOffsetY_num = undefined;
		this._fStartTimer_tmr = undefined;
		this._fSpeed_num = undefined;
		this._fPayoutValue_num = undefined;
		this._fTimers_arr = undefined;
		this._fParticlesContainer_sprt = undefined;
		this._fCircleBlast_sprt = undefined;
		this._fCircleBlastSecond_sprt = undefined;
		this._fMultFlare_sprt = undefined;
		this._fChMult_num = undefined;
		this._fPowerUpMultiplier_num = undefined;
		this._fCoinAnimationTimer_t = undefined;
		this._fStartAddAnimationTimer_t = undefined;
		this._fBundleAward_bln = undefined;
		this._fFollowEnemy_bln = undefined;
		this._fEnemyId_num = undefined;
		this._fEnemyTypeId_int = undefined;
		this._fIsEnemyKilled_bl = undefined;
		this._fAwardingShowStarted_bln = undefined;
		this._fBundleCoinsTimer_t = undefined;
		this._fGlowSeq_s = undefined;
		this._fPayoutContainer_sprt = undefined;
		this._fCoinsScale_num = undefined;
		this._fDelta_num = undefined;
	}
}

export default CoinsAward;