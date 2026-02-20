import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameplayBackgroundView from './background/GameplayBackgroundView';
import GraphView from './graph/GraphView';
import GameplayInfo from '../../model/gameplay/GameplayInfo';
import StarshipsPoolView from './starships/StarshipsPoolView';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import StarshipTrackView from './starships/StarshipTrackView';
import MTimeLine from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import InteractiveMultiplierIndicatorView from './interactiveMultiplierIndicator/InteractiveMultiplierIndicatorView';
import GameplayStateScreenView from './screens/GameplayStateScreenView';
import EjectableUnitsPoolView from './ejectableUnits/EjectableUnitsPoolView';
import MAnimation from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';
import { GAME_VIEW_SETTINGS } from '../main/GameBaseView';
import BattlegroundAustronautSitInOutView from '../uis/custom/battleground/BattlegroundAustronautSitInOutView';
import BattlegroundYouWonView from '../uis/custom/battleground/BattlegroundYouWonView';
import PreFlightCountingAnimation from './pre_flight/PreFlightCountingAnimation';
import BattlegroundAstronautLandingRocketExplosion from '../uis/custom/battleground/BattlegroundAstronautLandingRocketExplosion';
import BattlegroundRoundResultInformationScreenView from './battleground/BattlegroundRoundResultInformationScreenView';

class GameplayView extends SimpleUIView {
	static get EVENT_ON_EJECT_INITIATED() { return GameplayStateScreenView.EVENT_ON_EJECT_INITIATED };
	static get EVENT_ON_EJECT_ALL_INITIATED() { return GameplayStateScreenView.EVENT_ON_EJECT_ALL_INITIATED };
	static get EVENT_ON_TIMER_VALUE_UPDATED() { return GameplayStateScreenView.EVENT_ON_TIMER_VALUE_UPDATED };
	static get EVENT_ON_ASTRONAUT_ENTERS_ROCKET() { return "EVENT_ON_ASTRONAUT_ENTERS_ROCKET" };
	static get EVENT_ON_ASTRONAUT_EXIT_ROCKET() { return "EVENT_ON_ASTRONAUT_EXIT_ROCKET" };
	static get EVENT_ON_ASTRONAUT_TRAMPOLINE() { return BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUT_TRAMPOLINE };
	static get EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED() { return StarshipsPoolView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED; }
	static get EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED() { return StarshipsPoolView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED; }
	static get EVENT_ON_ASTRONAUT_EJECT_STARTED() { return EjectableUnitsPoolView.EVENT_ON_ASTRONAUT_EJECT_STARTED; }
	static get EVENT_ON_ASTRONAUT_LANDED() { return EjectableUnitsPoolView.EVENT_ON_ASTRONAUT_LANDED; }
	static get EVENT_ON_YOU_WON_ANIMATION_STARTED() { return BattlegroundYouWonView.EVENT_ON_YOU_WON_ANIMATION_STARTED; }
	static get EVENT_ON_WIN_CROWN_ANIMATION_STARTED() { return BattlegroundYouWonView.EVENT_ON_WIN_CROWN_ANIMATION_STARTED; }

	get backgroundView() {
		return this._fGameplayBackgroundView_gbv;
	}

	get foregroundContainer() {
		return this._fGameplayForegroundContainer_sprt;
	}

	get graphView() {
		return this._fGraphView_gv;
	}

	get trackView() {
		return this._fTrackView_rcstbcv;
	}

	get starshipsPoolView() {
		return this._fStarshipsPoolView_rspv;
	}

	get blackScreenContainer() {
		return this._fBlackScreenContainer;
	}

	get battlegroundYouWonView() {
		return this._fBattlegroundWonView_ywv;
	}

	get starshipForegroundEffectsContainer() {
		return this._fStarshipForegroundEffectsContainer_sprt;
	}

	get battlegroundRoundResultInformationScreenView() {
		return this._fBattlegroundRoundResultInformationScreenView_brrisv;
	}

	updateLatency(latency_float) {
		this.lDebugLatencyIndicatorValueLabel_ta.text = latency_float + " ms";
	}

	lastBalanceUpdate(date_int) {
		this._lastBalanceUpdate = date_int;
	}

	onRoomPaused() {
		this.interruptAstronautAnimations();

		if (APP.isBattlegroundGame) {
			this.battlegroundYouWonView.drop();
		}
	}

	interruptAstronautAnimations() {
		this._interruptAstronautsSitinAnimation();
	}

	_adjustAstronautInOutAnimations() {
		if (this.battlegroundYouWonView.isAnimationInProgress || !APP.tickerAllowed) {
			this.interruptAstronautAnimations();
			return;
		}

		let lActualAnimation_basv_arr = [];

		let lActiveBets_bi_arr = this.uiInfo.gamePlayersInfo.betsInfo.allActiveBets;
		let lSitInBets_bi_arr = [];
		let lSitInUnitsCount_int = 0;

		if (!!lActiveBets_bi_arr && !!lActiveBets_bi_arr.length) {
			let lSitInAnimationDuration_num = !!this._fBattlegroundSitinAstronauts_arr.length ? this._fBattlegroundSitinAstronauts_arr[0].sitInAnimationDuration : undefined;
			for (let i = 0; i < lActiveBets_bi_arr.length; i++) {
				let lActiveBet_bi = lActiveBets_bi_arr[i];
				if (lActiveBet_bi.placeBetTime > 0) {
					let lCurBetLifeDurationTime_num = APP.appClientServerTime - lActiveBet_bi.placeBetTime;
					if (lSitInAnimationDuration_num === undefined || lCurBetLifeDurationTime_num < lSitInAnimationDuration_num) {
						lSitInBets_bi_arr.push(lActiveBet_bi);
					}
				}
			}
			lSitInUnitsCount_int = lSitInBets_bi_arr.length;
		}

		for (let i = 0; i < lSitInUnitsCount_int; i++) {
			let lPlacedBetInfo_bi = lSitInBets_bi_arr[i];
			let lSeatId_str = lPlacedBetInfo_bi.seatId;
			let l_basv = this._getBattlegroundAustronautSitInViewInstance(lSeatId_str);
			lActualAnimation_basv_arr.push(l_basv);

			let lPlacedBetLifeDurationTime_num = APP.appClientServerTime - lPlacedBetInfo_bi.placeBetTime;
			let lIsMasterBet_bl = this.uiInfo.gamePlayersInfo.masterSeatId == lSeatId_str;
			l_basv.adjustSitInView(lSeatId_str, lIsMasterBet_bl, lPlacedBetLifeDurationTime_num);
		}


		let lDeactiveBets_bi_arr = this.uiInfo.gamePlayersInfo.betsInfo.allDeactiveBets;
		let lSitOutBets_bi_arr = [];
		let lSitOutUnitsCount_int = 0;

		if (!!lDeactiveBets_bi_arr && !!lDeactiveBets_bi_arr.length) {

			let lSitOutAnimationDuration_num = !!this._fBattlegroundSitinAstronauts_arr.length ? this._fBattlegroundSitinAstronauts_arr[0].sitOutAnimationDuration : undefined;
			for (let i = 0; i < lDeactiveBets_bi_arr.length; i++) {
				let lDectiveBet_bi = lDeactiveBets_bi_arr[i];
				if (lDectiveBet_bi.deactivateBetTime > 0) {
					let lCurBetDeactivatedLifeDurationTime_num = APP.appClientServerTime - lDectiveBet_bi.deactivateBetTime;
					if (lSitOutAnimationDuration_num === undefined || lCurBetDeactivatedLifeDurationTime_num < lSitOutAnimationDuration_num) {
						lSitOutBets_bi_arr.push(lDectiveBet_bi);
					}
				}
			}
			lSitOutUnitsCount_int = lSitOutBets_bi_arr.length;
		}

		for (let i = 0; i < lSitOutUnitsCount_int; i++) {
			let lDeactivatedBetInfo_bi = lSitOutBets_bi_arr[i];
			let lSeatId_str = lDeactivatedBetInfo_bi.seatId;
			let l_basv = this._getBattlegroundAustronautSitOutViewInstance(lSeatId_str);
			lActualAnimation_basv_arr.push(l_basv);

			let lDeactivatedBetLifeDurationTime_num = APP.appClientServerTime - lDeactivatedBetInfo_bi.deactivateBetTime;
			let lIsMasterBet_bl = this.uiInfo.gamePlayersInfo.masterSeatId == lSeatId_str;
			l_basv.adjustSitOutView(lSeatId_str, lIsMasterBet_bl, lDeactivatedBetLifeDurationTime_num);
			if (APP.isCAFMode) {
				if (this.persistentSitoutObject) {
					this.persistentSitoutObject.l_basv.isCafForced = false;
					this.persistentSitoutObject.l_basv.drop();
				}
				this.persistentSitoutObject = {
					l_basv: l_basv,
					lSeatId_str: lSeatId_str,
					lIsMasterBet_bl: lIsMasterBet_bl,
					lDeactivatedBetLifeDurationTime_num: lDeactivatedBetLifeDurationTime_num
				}
			}

		}

		if (APP.isCAFMode && this.persistentSitoutObject) {
			this.persistentSitoutObject.lDeactivatedBetLifeDurationTime_num += 30;
			this.persistentSitoutObject.l_basv.isCafForced = true;
			this.persistentSitoutObject.l_basv.adjustSitOutView(this.persistentSitoutObject.lSeatId_str, this.persistentSitoutObject.lIsMasterBet_bl, this.persistentSitoutObject.lDeactivatedBetLifeDurationTime_num);
			if (this.persistentSitoutObject.lDeactivatedBetLifeDurationTime_num > 1700) {
				this.persistentSitoutObject.l_basv.isCafForced = false;
				this.persistentSitoutObject = null;
			}
		}

		for (let i = 0; i < this._fBattlegroundSitinAstronauts_arr.length; i++) {
			let l_basv = this._fBattlegroundSitinAstronauts_arr[i];
			if (lActualAnimation_basv_arr.indexOf(l_basv) < 0) {
				this._dropAstronautsSitinAnimation(l_basv);
			}
		}
		lActualAnimation_basv_arr = null;
	}

	_getBattlegroundAustronautSitInViewInstance(aSeatId_str) {

		let l_basv = null;
		let lFirstDropped_basv = null;
		for (let i = 0; i < this._fBattlegroundSitinAstronauts_arr.length; i++) {
			let lCur_basv = this._fBattlegroundSitinAstronauts_arr[i];
			if (lCur_basv.isSitInAnimationInProgress && lCur_basv.seatId === aSeatId_str) {

				l_basv = lCur_basv;
				break;
			}

			if (!lFirstDropped_basv && lCur_basv.isDropped) {
				lFirstDropped_basv = lCur_basv;
			}
		}

		if (!l_basv) {

			l_basv = lFirstDropped_basv || this._generateNewAustronautSitInOutViewInstance();
		}

		return l_basv;
	}

	_getBattlegroundAustronautSitOutViewInstance(aSeatId_str) {
		let l_basv = null;
		let lFirstDropped_basv = null;
		for (let i = 0; i < this._fBattlegroundSitinAstronauts_arr.length; i++) {
			let lCur_basv = this._fBattlegroundSitinAstronauts_arr[i];
			if (lCur_basv.isSitOutAnimationInProgress && lCur_basv.seatId === aSeatId_str) {
				l_basv = lCur_basv;
				break;
			}

			if (!lFirstDropped_basv && lCur_basv.isDropped) {
				lFirstDropped_basv = lCur_basv;
			}
		}

		if (!l_basv) {
			l_basv = lFirstDropped_basv || this._generateNewAustronautSitInOutViewInstance();
		}

		return l_basv;
	}

	_generateNewAustronautSitInOutViewInstance() {
		let l_basv = new BattlegroundAustronautSitInOutView();
		l_basv.on(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUTS_ANIMATION_PRE_LANDING, this._onAstronautsAnimationPreLanding, this);
		l_basv.on(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUTS_ANIMATION_SITIN_COMPLETION, this._onAstronautsAnimationSitinCompletion, this);
		l_basv.on(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUT_TRAMPOLINE, this._onTrampolineJump, this);
		l_basv.on(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUTS_ANIMATION_SITOUT_STARTED, this._onAstronautsAnimationSitOutStarted, this);

		this._fBattlegroundSitinAstronauts_arr.push(l_basv);
		this._fAstronautSitinContainer_spr.addChild(l_basv);

		return l_basv;
	}

	_onAstronautsAnimationSitOutStarted(aEvent_e) {
		this.emit(GameplayView.EVENT_ON_ASTRONAUT_EXIT_ROCKET);
	}

	_onTrampolineJump(e) {
		this._fGameplayStateScreenView_gssv.trampolineJump();
		this.emit(e);
	}

	_onAstronautsAnimationPreLanding(aEvent_e) {
		let lExplosionRequired_bl = true;
		for (let i = 0; i < this._fBattlegroundAstronautsLandingExplosions_balre_arr.length; i++) {
			let lCur_balre = this._fBattlegroundAstronautsLandingExplosions_balre_arr[i];
			if (!lCur_balre.isDropped && lCur_balre.getProgress() < 0.35) {
				lExplosionRequired_bl = false;
				break;
			}
		}

		if (lExplosionRequired_bl) {
			this._startAstronautLandingExplosionAnimation();
		}
	}

	_onAstronautsAnimationSitinCompletion() {
		this.emit(GameplayView.EVENT_ON_ASTRONAUT_ENTERS_ROCKET);
		this.startWiggleStarship();
	}

	_startAstronautLandingExplosionAnimation() {
		let l_balre = this._getBattlegroundAstronautLandingRocketExplosionViewInstance();
		l_balre.startExplosionAnimation();
	}

	_getBattlegroundAstronautLandingRocketExplosionViewInstance() {
		let l_balre = null;
		for (let i = 0; i < this._fBattlegroundAstronautsLandingExplosions_balre_arr.length; i++) {
			let lCur_balre = this._fBattlegroundAstronautsLandingExplosions_balre_arr[i];
			if (lCur_balre.isDropped) {
				l_balre = lCur_balre;
				break;
			}
		}

		if (!l_balre) {
			l_balre = new BattlegroundAstronautLandingRocketExplosion();

			this._fBattlegroundAstronautsLandingExplosions_balre_arr.push(l_balre);
			this._fAstronautsLandingExplosionsContainer_spr.addChild(l_balre);
		}

		return l_balre;
	}

	_dropAstronautsSitinAnimation(aTargetAnim_basiv) {
		if (!aTargetAnim_basiv) {
			return;
		}

		aTargetAnim_basiv.drop();
	}

	_interruptAstronautsSitinAnimation() {
		if (this._fBattlegroundSitinAstronauts_arr) {
			for (let i = 0; i < this._fBattlegroundSitinAstronauts_arr.length; i++) {
				this._dropAstronautsSitinAnimation(this._fBattlegroundSitinAstronauts_arr[i]);
			}
		}

		if (this._fBattlegroundAstronautsLandingExplosions_balre_arr) {
			for (let i = 0; i < this._fBattlegroundAstronautsLandingExplosions_balre_arr.length; i++) {
				this._dropAstronautsSitinAnimation(this._fBattlegroundAstronautsLandingExplosions_balre_arr[i]);
			}
		}
	}

	getStarshipLaunchX() {
		return this.uiInfo.isPreLaunchFlightRequired ? 30 : 0;
	}

	getStarshipLaunchY() {
		return this.uiInfo.isPreLaunchFlightRequired ? GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height : GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y;
	}

	getStarshipView() {
		return StarshipsPoolView.STARSHIP;
	}

	get ejectButtonsContainer() {
		return this._fEjectButtonsContainer_sprt;
	}

	get preFlightCountingAnimation() {
		return this._fPreFlightCountingAnimation_pfca;
	}

	adjust() {
		let l_gpi = this.uiInfo;
		let lRoundInfo_ri = l_gpi.roundInfo;

		let lRoundStartTime_num = lRoundInfo_ri.roundStartTime;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		if (!APP.isBattlegroundGame || !this.battlegroundYouWonView.isAnimationInProgress) {
			this.backgroundView && this.backgroundView.adjust();
			this.graphView.visible = true;
			this._fMultiplierIndicator_imiv.visible = false;
		}
		else {
			this._fMultiplierIndicator_imiv.visible = false;
			this.graphView.visible = false;
		}

		this._addPreFlightCountingAnimationInRequired();
		this.graphView && this.graphView.adjust();

		if (APP.isBattlegroundGame) {
			this.battlegroundYouWonView.adjust();
			this.battlegroundRoundResultInformationScreenView.adjust();
		}

		StarshipsPoolView.STARSHIP.adjust();

		this._fTrackView_rcstbcv.adjust();

		this._fMultiplierIndicator_imiv.adjust();

		this._fEjectableUnitsPoolView_eupv.adjust();

		//ROUND END FLASH...
		this._adjustFlash();
		//...ROUND END FLASH

		this._fGameplayStateScreenView_gssv.adjust();

		if (APP.isBattlegroundGame) {
			this._adjustAstronautInOutAnimations();
		}

		if (this.preFlightCountingAnimation) {
			this.preFlightCountingAnimation.adjust();
		}

		let lPrevShipParent_sprt = this._fStarshipsPoolView_rspv.parent;
		let lNewShipParent_sprt = this._fStarshipsContainer_sprt;
		if (this._fGameplayStateScreenView_gssv.isStartRoundScreenActive) {
			lNewShipParent_sprt = this._fGameplayStateScreenView_gssv.shipContainer;
		}

		if (lNewShipParent_sprt !== lPrevShipParent_sprt) {
			let lPrevShipPos_p = this._fStarshipsPoolView_rspv.position;
			let lNewShipPos_p = lPrevShipParent_sprt.localToLocal(lPrevShipPos_p.x, lPrevShipPos_p.y, lNewShipParent_sprt);

			this._fStarshipsPoolView_rspv.position.set(lNewShipPos_p.x, lNewShipPos_p.y);
			lNewShipParent_sprt.addChild(this._fStarshipsPoolView_rspv);
		}

		if (lRoundInfo_ri.isRoundPlayActive) {

			if(APP.isBattlegroundGame){
				if(this.connectionInteruptedState == 2){
					this._fConnectionInterruptedContainer_sprt.visible = false;
					this._fSpinnerAnimation_mtl.stop();
					return;
				}
				if(this.connectionInteruptedState == 1){
					this._fConnectionInterruptedContainer_sprt.visible = false;
					this._fSpinnerAnimation_mtl.stop();
					this.connectionInteruptedState = 2;
					setTimeout(()=>{
						this.connectionInteruptedState = 0;
					},GameplayInfo.MAX_CONNECTION_INTERRUPTION_DELAY*3);
					return;
				}
			}

			
			this._lastBalanceUpdate = Date.now();
			let lConnectionInterruptionDuration = 0;
			if (l_gpi.serverMultiplierTimeDefined) {
				lConnectionInterruptionDuration = APP.appClientServerTime - l_gpi.serverMultiplierRecieveTime - GameplayInfo.MAX_CONNECTION_INTERRUPTION_DELAY;
			}
			else {
				lConnectionInterruptionDuration = APP.appClientServerTime - l_gpi.multiplierChangeFlightStartTime - GameplayInfo.MAX_CONNECTION_INTERRUPTION_DELAY;
			}

			if (lConnectionInterruptionDuration > 0) {
				this._setPharase();
				this._fConnectionInterruptedContainer_sprt.visible = true;
				
				this._fSpinnerAnimation_mtl.playLoop();
			}
			else {
				this._fConnectionInterruptedContainer_sprt.visible = false;
				this._fSpinnerAnimation_mtl.stop();
			}
		} else if (!lRoundInfo_ri.isRoundQualifyState) {
			if(APP.isBattlegroundGame){
				this.connectionInteruptedState = 1;
			}

			let balanceLag = false;

			if (this._lastBalanceUpdate) {
				const balanceDelay = Date.now() - this._lastBalanceUpdate;
				if ((balanceDelay - 8500 - GameplayInfo.MAX_CONNECTION_INTERRUPTION_DELAY) > 0) {
					balanceLag = true;
				}
			}

			if (balanceLag) {
				this._setPharase();
				this._fConnectionInterruptedContainer_sprt.visible = true;
				this._fSpinnerAnimation_mtl.playLoop();
			} else {
				this._fConnectionInterruptedContainer_sprt.visible = false;
				this._fSpinnerAnimation_mtl.stop();
			}
		} else {
			if(APP.isBattlegroundGame){
				this.connectionInteruptedState = 1;
			}
			this._fConnectionInterruptedContainer_sprt.visible = false;
			this._fSpinnerAnimation_mtl.stop();
			this._lastBalanceUpdate = Date.now();
		}

		this._validateBlur();
	}

	_setPharase()
	{
		if(this._fConnectionInterruptedContainer_sprt.visible == true) return;
		const total = this._connectionPhrases_ar.length; 
		const rand = Math.ceil(Math.random() * total);
		for( let i=0; i<total; i++)
		{
			const instance = this._connectionPhrases_ar[i];
			if(i == rand)
			{
				instance.visible = true; 
			}else{
				instance.visible = false;
			}
		}
	}

	adjustRandomElements() {
		if (APP.isBattlegroundGame) {
			if (this.battlegroundYouWonView.isAnimationInProgress) {
				this.battlegroundYouWonView.once(BattlegroundYouWonView.EVENT_ON_YOU_WON_ANIMATION_COMPLETED, this.adjustRandomElements, this);
				this.battlegroundYouWonView.once(BattlegroundYouWonView.EVENT_ON_YOU_WIN_INTERRUPT_ANIMATION, this.adjustRandomElements, this);
				return;
			}
			else {
				this.battlegroundYouWonView.off(BattlegroundYouWonView.EVENT_ON_YOU_WON_ANIMATION_COMPLETED, this.adjustRandomElements, this);
				this.battlegroundYouWonView.off(BattlegroundYouWonView.EVENT_ON_YOU_WIN_INTERRUPT_ANIMATION, this.adjustRandomElements, this);
			}
		}

		this.backgroundView && this.backgroundView.adjustRandomElements();
		this.starshipsPoolView && this.starshipsPoolView.setRandomStarship();
	}

	startWiggleStarship() {
		this.starshipsPoolView && this.starshipsPoolView.startWiggleStarship();
	}

	updateDebugIndicators(aForcedNA) {
		this._updateDebugIndicators(aForcedNA);
	}

	updateArea() {
		this._updateArea();
	}

	getCorrespondentZoomOutScale(aMillisecondIndex_int) {
		let lZoomScale_num = 1;
		let l_gpi = this.uiInfo;

		if (l_gpi.isPreLaunchFlightRequired && aMillisecondIndex_int < 0) {
			let lScaleDuration_int = l_gpi.preLaunchZoomOutDuration;
			let lScaleProgress_num = 1;
			let lRoundStartRestTime_int = -aMillisecondIndex_int;

			if (lRoundStartRestTime_int > l_gpi.preLaunchFlightDuration) {
				lScaleProgress_num = 0;
			}
			else if (lRoundStartRestTime_int > (l_gpi.preLaunchFlightDuration - lScaleDuration_int)) {
				lScaleProgress_num = (l_gpi.preLaunchFlightDuration - lRoundStartRestTime_int) / lScaleDuration_int;
			}

			lZoomScale_num = 1 + 1.5 * (1 - MAnimation.getEasingMultiplier(MAnimation.EASE_IN, lScaleProgress_num));
		}

		return lZoomScale_num;
	}

	getCorrespondentZoomInScale(aMillisecondIndex_int) {
		let lZoomScale_num = 1;
		let l_gpi = this.uiInfo;

		if (l_gpi.isPreLaunchFlightRequired && aMillisecondIndex_int < 0) {
			lZoomScale_num = 1;
		}
		else if (aMillisecondIndex_int < l_gpi.maxRoundDuration) {
			lZoomScale_num = 1 + aMillisecondIndex_int / (60 * 1000);
		}
		else {
			lZoomScale_num = 1 + l_gpi.maxRoundDuration / (60 * 1000)
		}

		return lZoomScale_num;
	}

	constructor() {
		super();

		this._fContainer_sprt = null;
		this._fGameplayBackgroundView_gbv = null;
		this._fGraphView_gv = null;
		this._fBattlegroundWonView_ywv = null;
		this._fStarshipsPoolView_rspv = new StarshipsPoolView();
		this._fFlashAnimation_mtl = null;
		this._RRBlackScreenAnimation_mtl = null;
		this._fMultiplierIndicator_imiv = null;
		this._fGameplayStateScreenView_gssv = null;
		this._fProfillingInfp_pi = null;
		this._fEjectableUnitsPoolView_eupv = null;
		this._fEjectButtonsContainer_sprt = null;
		this._fDebugIndicatorLabel_ta = null;
		this._fDebugInfocatorTextTemplate_str = null;
		this._fClipping_gr = null;
		this._fBattlegroundSitinAstronauts_arr = [];
		this._fBattlegroundAstronautsLandingExplosions_balre_arr = [];
		this._fPreFlightCountingAnimation_pfca = null;
		this._fPreFlightCountingAnimationContainer_sprt = null;
		this._fConnectionInterruptedContainer_sprt = null;
		this._fSpinnerAnimation_mtl = null;
		this._fCurFlashAlpha_num = undefined;
		this._lastLatencyUpdate = null;
	}

	__init() {
		super.__init();

		this._fProfillingInfp_pi = APP.profilingController.info;

		let lElementsContainer_sprt = this._fContainer_sprt = this.addChild(new Sprite());

		//BACKGROUND...
		let l_gbv = this._fGameplayBackgroundView_gbv = new GameplayBackgroundView();
		lElementsContainer_sprt.addChild(l_gbv);
		//...BACKGROUND

		// //GRAPH...
		let l_gv = this._fGraphView_gv = new GraphView();
		lElementsContainer_sprt.addChild(l_gv);
		// //...GRAPH

		this._fTrackView_rcstbcv = new StarshipTrackView();

		//RRBLACKSCREEN...
		this._fBlackScreenContainer = this.addChild(new Sprite());
		let lBounds = this.getBounds();
		this._fRRBlackScreen = this._fBlackScreenContainer.addChild(new PIXI.Graphics()).beginFill(0x000000).drawRect(0, 0, lBounds.width, lBounds.height).endFill();
		this._fRRBlackScreen.alpha = 0;
		l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fRRBlackScreen,
			MTimeLine.SET_ALPHA,
			0,
			[
				9,
				[0.4, 9],
			]);

		this._RRBlackScreenAnimation_mtl = l_mtl;
		//...RRBLACKSCREEN

		//YOU WON ANIMATION...
		if (APP.isBattlegroundGame) {
			this._fBattlegroundWonView_ywv = new BattlegroundYouWonView();
			this._fBattlegroundWonView_ywv.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2);
			this._fBattlegroundWonView_ywv.on(BattlegroundYouWonView.EVENT_ON_YOU_WON_ANIMATION_STARTED, this.emit, this);
			this._fBattlegroundWonView_ywv.on(BattlegroundYouWonView.EVENT_ON_WIN_CROWN_ANIMATION_STARTED, this.emit, this);
			this.addChild(this._fBattlegroundWonView_ywv);
		}
		//...YOU WON ANIMATION

		//STARSHIPS...
		this._fStarshipForegroundEffectsContainer_sprt = new Sprite;

		let l_rspv = this._fStarshipsPoolView_rspv;
		l_rspv.getBackgroundContainer().addChild(this._fTrackView_rcstbcv);
		l_rspv.on(StarshipsPoolView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED, this.emit, this);
		l_rspv.on(StarshipsPoolView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this.emit, this);

		l_rspv.position.set(0, 0);
		l_rspv.init();
		this._fStarshipsContainer_sprt = lElementsContainer_sprt.addChild(new Sprite);
		this._fStarshipsContainer_sprt.addChild(l_rspv);
		//...STARSHIPS

		//EJECTABLE UNITS...
		let l_eupv = new EjectableUnitsPoolView();
		l_eupv.on(EjectableUnitsPoolView.EVENT_ON_ASTRONAUT_EJECT_STARTED, this.emit, this);
		l_eupv.on(EjectableUnitsPoolView.EVENT_ON_ASTRONAUT_LANDED, this.emit, this);
		this._fEjectableUnitsPoolView_eupv = l_eupv;
		lElementsContainer_sprt.addChild(l_eupv);
		//...EJECTABLE UNITS

		//FOREGROUND...
		let l_sprt = this._fGameplayForegroundContainer_sprt = new Sprite();
		lElementsContainer_sprt.addChild(l_sprt);
		//...FOREGROUND

		//FLASH...
		let lFlash_gr = this._flash_gr = lElementsContainer_sprt.addChild(new PIXI.Graphics);
		let l_mtl = new MTimeLine();
		if (APP.isBattlegroundGame) {
			l_mtl.addAnimation(
				this._updateFlashView,
				MTimeLine.EXECUTE_METHOD,
				0.4,
				[
					1,
					[0.4, 1],
					[0, 16]
				],
				this);
		}
		else {
			l_mtl.addAnimation(
				this._updateFlashView,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					1,
					[1, 1],
					[0, 20]
				],
				this);
		}
		this._fFlashAnimation_mtl = l_mtl;
		//...FLASH

		//OVERLAYS...
		this._fEjectButtonsContainer_sprt = new Sprite;

		let l_gssv = this.addChild(new GameplayStateScreenView());
		l_gssv.on(GameplayStateScreenView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		l_gssv.on(GameplayStateScreenView.EVENT_ON_EJECT_ALL_INITIATED, this.emit, this);
		l_gssv.on(GameplayStateScreenView.EVENT_ON_ACTIVE_SCREEN_CHANGED, this._onActiveScreenChanged, this);
		l_gssv.on(GameplayStateScreenView.EVENT_ON_TIMER_VALUE_UPDATED, this.emit, this);

		this._fGameplayStateScreenView_gssv = l_gssv;
		//...OVERLAYS

		this.addChild(this._fStarshipForegroundEffectsContainer_sprt);

		this._fPreFlightCountingAnimationContainer_sprt = this.addChild(new Sprite);
		this._addPreFlightCountingAnimationInRequired();

		//ROUND ID INDICATOR...
		let lDebugIndicatorTranslatableAssetName_str = APP.isBattlegroundGame ? "TADebugIndicatorLabelMaxBlastChampions" : (APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode ? "TADebugIndicatorLabelTripleMaxBlast" : "TADebugIndicatorLabel");
		let lDebugIndicatorLabel_ta = this._fDebugIndicatorLabel_ta = this.addChild(I18.generateNewCTranslatableAsset(lDebugIndicatorTranslatableAssetName_str));
		lDebugIndicatorLabel_ta.alpha = 0.5;
		this._fDebugInfocatorTextTemplate_str = lDebugIndicatorLabel_ta.text;


		//lATENCY INDICATOR...
		let lDebugLatencyIndicatorLabel_ta = this._lDebugLatencyIndicatorLabel_ta = this.addChild(I18.generateNewCTranslatableAsset(lDebugIndicatorTranslatableAssetName_str));
		lDebugLatencyIndicatorLabel_ta.text = "Latency:";
		lDebugLatencyIndicatorLabel_ta.alpha = 0.5;

		//lATENCY INDICATOR...
		let lDebugLatencyIndicatorValueLabel_ta = this.lDebugLatencyIndicatorValueLabel_ta = this.addChild(I18.generateNewCTranslatableAsset(lDebugIndicatorTranslatableAssetName_str));
		lDebugLatencyIndicatorValueLabel_ta.text = "0.5 ms";
		lDebugLatencyIndicatorValueLabel_ta.alpha = 0.5;



		this._updateDebugIndicators();

		//INTERACTIVE MULTIPLIER INDICATOR...
		let l_imiv = this._fMultiplierIndicator_imiv = this.addChild(new InteractiveMultiplierIndicatorView());
		//...INTERACTIVE MULTIPLIER INDICATOR

		//CONNECTION INTERRUPTED...
		this._fConnectionInterruptedContainer_sprt = this.addChild(new Sprite);
		this._fConnectionInterruptedContainer_sprt.visible = false;

		this._connectionPhrases_ar = [];

		let lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel0"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);

		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel1"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel2"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel3"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		
		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel4"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel5"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel6"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		lConnectionInterruptedLabel_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedLabel7"));
		lConnectionInterruptedLabel_ta.position.set(0, 0);
		this._connectionPhrases_ar.push(lConnectionInterruptedLabel_ta);

		this._setPharase();


		let lConnectionInterruptedText_ta = this._fConnectionInterruptedContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAConnectionInterruptedText"));
		lConnectionInterruptedText_ta.position.set(0, 15);

		let lConnectionInterruptedSpinnerContainer_sprt = this._fConnectionInterruptedContainer_sprt.addChild(new Sprite);
		lConnectionInterruptedSpinnerContainer_sprt.position.set(0, 42);

		let lConnectionInterruptedSmallSpinner_sprt = lConnectionInterruptedSpinnerContainer_sprt.addChild(APP.library.getSprite("game/load_spinner_small"));
		lConnectionInterruptedSmallSpinner_sprt.anchor.set(0.33, 0.33);
		lConnectionInterruptedSmallSpinner_sprt.position.set(0, 0);

		let lConnectionInterruptedBigSpinner_sprt = lConnectionInterruptedSpinnerContainer_sprt.addChild(APP.library.getSprite("game/load_spinner_big"));
		lConnectionInterruptedBigSpinner_sprt.position.set(0, 0);

		let lTextShadowParam_obj =
		{
			dropShadow: true,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 3
		}
		lConnectionInterruptedText_ta.assetContent.textFormat = lTextShadowParam_obj;
		lConnectionInterruptedLabel_ta.assetContent.textFormat = lTextShadowParam_obj;

		//SPINNER ANIMATION...
		let lSpinner_mtl = new MTimeLine();
		lSpinner_mtl.addAnimation(
			lConnectionInterruptedBigSpinner_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[360, 150]
			]);

		lSpinner_mtl.addAnimation(
			lConnectionInterruptedSmallSpinner_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[-360, 150]
			]);

		this._fSpinnerAnimation_mtl = lSpinner_mtl;
		//...SPINNER ANIMATION
		//...CONNECTION INTERRUPTED

		this.addChild(this._fEjectButtonsContainer_sprt);

		if (APP.isBattlegroundGame) {
			this._fBattlegroundRoundResultInformationScreenView_brrisv = this.addChild(new BattlegroundRoundResultInformationScreenView);
		}

		let gr = this._fClipping_gr = this.addChild(new PIXI.Graphics);

		this._fAstronautSitinContainer_spr = this._fGameplayStateScreenView_gssv.startScreenView.sitInOutUnitsContainer.addChild(new Sprite());
		this._fAstronautsLandingExplosionsContainer_spr = this._fGameplayStateScreenView_gssv.startScreenView.sitInOutUnitsContainer.addChild(new Sprite());

		this._updateArea();
	}

	_addPreFlightCountingAnimationInRequired() {
		if (!!this._fPreFlightCountingAnimation_pfca) {
			return;
		}

		let lBetsInfo_bsi = this.uiInfo.gamePlayersInfo.betsInfo;

		if (
			(lBetsInfo_bsi.isNoMoreBetsPeriodModeDefined && lBetsInfo_bsi.isNoMoreBetsPeriodMode)
			|| APP.isBattlegroundGame
		) {
			this._fPreFlightCountingAnimation_pfca = this._fPreFlightCountingAnimationContainer_sprt.addChild(new PreFlightCountingAnimation);

			this._updatePreFlightCountingAnimationLocation();
		}
	}

	_updateDebugIndicators(aForcedNA = false) {
		if (this._fDebugIndicatorLabel_ta && this._fDebugInfocatorTextTemplate_str !== undefined) {
			let lRoomId_str = "N/A";
			let lRoundId_str = "N/A";

			if (!aForcedNA) {
				lRoomId_str = this.uiInfo.roomInfo.roomId ? this.uiInfo.roomInfo.roomId : "N/A";
				lRoundId_str = this.uiInfo.roundInfo.roundId ? this.uiInfo.roundInfo.roundId : "N/A";
			}

			this._fDebugIndicatorLabel_ta.text = this._fDebugInfocatorTextTemplate_str.replace("/ROOM_ID/", lRoomId_str).replace("/ROUND_ID/", lRoundId_str);
		}
	}

	_updateArea() {
		let lElementsContainer_sprt = this._fContainer_sprt;
		lElementsContainer_sprt.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y);

		if (APP.isBattlegroundGame) {
			this.battlegroundYouWonView.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2);
			this.battlegroundRoundResultInformationScreenView.updateArea();
		}

		let lConnectionInterruptedPositionY_num;
		if (APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode) {
			lConnectionInterruptedPositionY_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + 65;
		}
		else {
			if (APP.isCAFMode) {
				lConnectionInterruptedPositionY_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height - 60;
			} else {
				lConnectionInterruptedPositionY_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height - 65;
			}
		}
		this._fConnectionInterruptedContainer_sprt.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2, lConnectionInterruptedPositionY_num);
		this._fStarshipForegroundEffectsContainer_sprt.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y);

		let l_gssv = this._fGameplayStateScreenView_gssv;
		let lGameplayStateScreenViewPosition_obj =
		{
			x: GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2,
			y: GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2
		};
		l_gssv.position.set(lGameplayStateScreenViewPosition_obj.x, lGameplayStateScreenViewPosition_obj.y);

		this._fDebugIndicatorLabel_ta.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + 3 /*indent*/, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y);
		this._lDebugLatencyIndicatorLabel_ta.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + this._fDebugIndicatorLabel_ta.text.length * 5.1, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y);

		this.lDebugLatencyIndicatorValueLabel_ta.position.set(this._lDebugLatencyIndicatorLabel_ta.x + 50, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y);

		this._fAstronautSitinContainer_spr.position.set(-lGameplayStateScreenViewPosition_obj.x, -lGameplayStateScreenViewPosition_obj.y);
		this._fAstronautsLandingExplosionsContainer_spr.position.set(-lGameplayStateScreenViewPosition_obj.x, -lGameplayStateScreenViewPosition_obj.y);

		//CLIPPING...
		let gr = this._fClipping_gr;
		gr.cacheAsBitmap = false;
		gr.clear();
		gr.beginFill(0x000000).drawRect(0, 0, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x, APP.screenHeight).endFill();
		gr.beginFill(0x000000).drawRect(0, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height, APP.screenWidth, APP.screenHeight - (GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height)).endFill();
		//...CLIPPING

		this.backgroundView.updateArea();

		this.graphView.updateArea();

		this._fTrackView_rcstbcv.updateArea();

		this._fMultiplierIndicator_imiv.updateArea();

		this._fGameplayStateScreenView_gssv.updateArea();

		this._updatePreFlightCountingAnimationLocation();

		//DEBUG...
		// this._fDebug_gr = this._fDebug_gr || lElementsContainer_sprt.addChild(new PIXI.Graphics).beginFill(0x00ff00).drawCircle(0, 0, 40).endFill();
		// this._fDebug_gr.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height*0.45);
		//...DEBUG

		for (let i = 0; i < this._fBattlegroundSitinAstronauts_arr.length; i++) {
			let lCur_basv = this._fBattlegroundSitinAstronauts_arr[i];
			if (!lCur_basv.isDropped) {
				lCur_basv.updateArea();
			}
		}

		for (let i = 0; i < this._fBattlegroundAstronautsLandingExplosions_balre_arr.length; i++) {
			let lCur_balre = this._fBattlegroundAstronautsLandingExplosions_balre_arr[i];
			if (!lCur_balre.isDropped) {
				lCur_balre.updateArea();
			}
		}
	}

	_updatePreFlightCountingAnimationLocation() {
		let l_pfca = this._fPreFlightCountingAnimation_pfca;
		if (l_pfca) {
			l_pfca.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2);
			l_pfca.updateArea();
		}
	}

	_adjustFlash() {
		let l_gpi = this.uiInfo;
		let l_ri = l_gpi.roundInfo;

		if (l_ri.isRoundPlayActive || !l_gpi.outOfRoundDuration) {
			this._fFlashAnimation_mtl.windToMillisecond(0);
			this._updateFlashView(0);
		}
		else {
			this._fFlashAnimation_mtl.windToMillisecond(l_gpi.outOfRoundDuration);
		}
	}

	_updateFlashView(aAlhpa_num = 0) {
		if (this._fCurFlashAlpha_num === aAlhpa_num) {
			return;
		}

		this._fCurFlashAlpha_num = aAlhpa_num;

		let lFlash_gr = this._flash_gr;
		lFlash_gr.clear();

		if (aAlhpa_num > 0) {
			lFlash_gr.beginFill(0xffffff, aAlhpa_num).drawRect(0, 0, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height).endFill();
		}
	}

	_validateBlur() {
		let l_gpi = this.uiInfo;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let l_gssv = this._fGameplayStateScreenView_gssv;
		let lIsBlurAvailableState_bl = l_gssv.isEndRoundScreenActive;

		if (this.uiInfo.isPreLaunchFlightRequired) {
			lIsBlurAvailableState_bl = lIsBlurAvailableState_bl || l_gssv.isStartRoundScreenActive;

			if (APP.isBattlegroundGame) {
				lIsBlurAvailableState_bl = false;
			}
		}

		if (lIsBlurAvailableState_bl) {
			this.backgroundView.setBlur(10);
			this.foregroundContainer.setBlur(10);
			this.foregroundContainer.zIndex = -1;
		}
		else {
			this.backgroundView.setBlur(0);
			this.foregroundContainer.setBlur(0);
			this.foregroundContainer.zIndex = 1;
		}

		if (APP.isBattlegroundGame) {
			this._adjustRRBlackScreenAnimation();
			if (l_gssv.isEndRoundScreenActive || this.battlegroundYouWonView.isAnimationInProgress) {
				this.foregroundContainer.zIndex = -1;
			}
			else {
				this.foregroundContainer.zIndex = 1;
			}
		}
	}

	_adjustRRBlackScreenAnimation() {
		let l_gpi = this.uiInfo;
		if (this.battlegroundYouWonView.isAnimationInProgress) {
			let lTotalDuration = l_gpi.outOfRoundDuration + (l_gpi.outOfRoundDuration == 0 ? l_gpi.gameplayTime : 0)
			this._RRBlackScreenAnimation_mtl.windToMillisecond(lTotalDuration);
			return;
		}
		this._RRBlackScreenAnimation_mtl.windToMillisecond(l_gpi.outOfRoundDuration)
	}

	_onActiveScreenChanged(event) {
		if (this._fGameplayStateScreenView_gssv.isProgressRoundScreenActive) {
			// no need to update visibility of ejectButtonsContainer here, it is already handled in GameplayStateScreenProgressView::adjust
		}
		else {
			this.ejectButtonsContainer.visible = false;
		}
	}
}

export default GameplayView;