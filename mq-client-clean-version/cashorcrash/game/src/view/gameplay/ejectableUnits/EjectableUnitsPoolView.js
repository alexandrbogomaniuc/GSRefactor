import EjectableUnitView from './EjectableUnitView';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayController from '../../../controller/gameplay/GameplayController';

const DEFAULT_MIN_UNITS_DISTANCE = 20;

class EjectableUnitsPoolView extends Sprite {
	static get EVENT_ON_ASTRONAUT_EJECT_STARTED() { return EjectableUnitView.EVENT_ON_EJECT_STARTED; }
	static get EVENT_ON_ASTRONAUT_LANDED() { return EjectableUnitView.EVENT_ON_LANDED; }

	constructor() {
		super();

		let l_gpv = APP.gameController.gameplayController.view;
		this._fGraphView_gv = l_gpv.graphView;

		this._fEjectableUnits_euv_arr = [];

		APP.gameController.gameplayController.on(GameplayController.EVENT_TMB_ROUND_ENDED, this._tmbRoundEnded, this);
		this._isBattlegroundGame = APP.isBattlegroundGame;
		this._gamePlayControllerInfo =  APP.gameController.gameplayController.info;
		this._gamePlayControllerView = APP.gameController.gameplayController.view;
		if(this._isBattlegroundGame)
		{
			for(let i=0; i<100;i++ )
			{
				const poolView = this.getEjectableUnitView(i);
				poolView.initView();
			}
		}
	}

	getStarshipTakeOffDeltaCorrectionDurationInMilliseconds() {
		return 1200;
	}

	getEjectableUnitView(aIndex_int) {
		if (aIndex_int <= this._fEjectableUnits_euv_arr.length - 1) {
			return this._fEjectableUnits_euv_arr[aIndex_int];
		}
		let l_euv = new EjectableUnitView(this._isBattlegroundGame);
		l_euv.on(EjectableUnitView.EVENT_ON_EJECT_STARTED, this.emit, this);
		l_euv.on(EjectableUnitView.EVENT_ON_LANDED, this.emit, this);

		this._fEjectableUnits_euv_arr.push(l_euv);

		return this.addChild(l_euv);
	}


	_tmbRoundEnded(event) {
		this.destroyChildren();
		this._fEjectableUnits_euv_arr = [];
	}

	adjust() {


		let l_gpi = this._gamePlayControllerInfo;
		let l_gpv = this._gamePlayControllerView;

		let lEjectedBets_bi_arr = l_gpi.gamePlayersInfo.betsInfo.allEjectedBets;
		let lStarshipView_sv = l_gpv.getStarshipView();
		let lUnitsCount_int = !!lEjectedBets_bi_arr ? lEjectedBets_bi_arr.length : 0;
		let lCurMultiplierRoundDuration_num = l_gpi.multiplierRoundDuration;
		let lPaddingX_num = this._isBattlegroundGame ? 10 : 100;

		lEjectedBets_bi_arr && lEjectedBets_bi_arr.sort(function (a, b) {
			return a.ejectTime - b.ejectTime;
		});

		let lMinUnitsDistance_num = DEFAULT_MIN_UNITS_DISTANCE;
		let lBottomTrajectoryPoint_p = new PIXI.Point(l_gpv.getStarshipLaunchX(), l_gpv.getStarshipLaunchY());
		let lDefaultMaxUnitsDistance_num = l_gpv.trackView.trackLength;
		let lMaxUnitsDistance_num = lDefaultMaxUnitsDistance_num;

		if (this._isBattlegroundGame && l_gpv.battlegroundYouWonView.isAnimationInProgress) {
			for (let i = 0; i < this._fEjectableUnits_euv_arr.length; i++) {
				this._fEjectableUnits_euv_arr[i].drop();
			}
			return;
		}

		if (this._isBattlegroundGame) {

			for (let i = 0; i < lUnitsCount_int; i++) {
				const lBetInfo_bi = lEjectedBets_bi_arr[i];
				const l_euv = this.getEjectableUnitView(i);
				const lEjectedBetDuration_num = lBetInfo_bi.ejectTime - l_gpi.multiplierChangeFlightStartTime;
				let lTrackCurvePoint_p = null;

				if (l_gpi.isPreLaunchFlightRequired) {
					lTrackCurvePoint_p = this._isBattlegroundGame ? l_gpv.trackView.getBTGCurvePoint(lBetInfo_bi.multiplier)
						: l_gpv.trackView.getCurvePoint(lEjectedBetDuration_num + l_gpi.preLaunchFlightDuration);
				}
				else {
					lTrackCurvePoint_p = l_gpv.trackView.getCrashCurvePoint(lBetInfo_bi.multiplier);
				}

				//JUMP OUT X OFFSET...
				const lTargetX_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.x : this._fGraphView_gv.getCorrespondentCoordinateX(lEjectedBetDuration_num);
				let lDeltaX_num = lStarshipView_sv.position.x - lTargetX_num;

				if (lDeltaX_num < 0) {
					lDeltaX_num = 0;
				}

				const lDeltaCorrectionMillisecondsCount_int = this.getStarshipTakeOffDeltaCorrectionDurationInMilliseconds();
				let lEjectableUnitLifeDuration_int = lCurMultiplierRoundDuration_num - lEjectedBetDuration_num;

				if (lEjectableUnitLifeDuration_int < 0) {
					lEjectableUnitLifeDuration_int = 0;
				}

				let lDeltaDownscale_num = 1 - lEjectableUnitLifeDuration_int / lDeltaCorrectionMillisecondsCount_int;

				if (lDeltaDownscale_num < 0) {
					lDeltaDownscale_num = 0;
				}
				//...JUMP OUT X OFFSET

				lDeltaX_num *= lDeltaDownscale_num;

				let lX_num = lTargetX_num;
				if (lDeltaX_num > 0);
				{
					if (l_gpi.isPreLaunchFlightRequired) {
						lTrackCurvePoint_p = l_gpv.trackView.getPointInDistance(lTrackCurvePoint_p, lDeltaX_num);
						lX_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.x : lX_num;
					}
					else {
						lX_num = lTargetX_num + lDeltaX_num;
					}
				}

				let lY_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.y : this._fGraphView_gv.getCorrespondentCoordinateY(lBetInfo_bi.multiplier);

				let lCurPos_p = new PIXI.Point(lX_num, lY_num);

				if (i == 0) {
					let lLeftMostUnitPoint_p = lBottomTrajectoryPoint_p;
					if (lLeftMostUnitPoint_p && lX_num > lLeftMostUnitPoint_p.x) {
						lLeftMostUnitPoint_p = lCurPos_p;
					}

					//LEFT PADDING...
					if (
						!l_gpi.isPreLaunchFlightRequired
						&& (lX_num < lBottomTrajectoryPoint_p.x + lPaddingX_num)
					) {
						let lPositionWithPadding_p = l_gpv.trackView.getPointInDistance(lBottomTrajectoryPoint_p, lPaddingX_num);
						lLeftMostUnitPoint_p = lPositionWithPadding_p;
						lX_num = lPositionWithPadding_p.x;
						lY_num = lPositionWithPadding_p.y;
					}
					//...LEFT PADDING

					lMaxUnitsDistance_num = Math.floor(l_gpv.trackView.getTrackDistance(lLeftMostUnitPoint_p) / (lUnitsCount_int - 1));
					lMinUnitsDistance_num = Math.min(lMinUnitsDistance_num, lMaxUnitsDistance_num);
				}
				else {

					let lPrevUnit_euv = this.getEjectableUnitView(i - 1);
					let lPrevUnitPos_p = new PIXI.Point(lPrevUnit_euv.position.x, lPrevUnit_euv.position.y);
					let lCurUnitsDistance_num = l_gpv.trackView.getTrackDistance(lPrevUnitPos_p, lCurPos_p);
					if (lCurPos_p.x < lPrevUnitPos_p.x || lCurUnitsDistance_num < lMinUnitsDistance_num) {
						let lAdjustedPoint_p = l_gpv.trackView.getPointInDistance(lPrevUnitPos_p, lMinUnitsDistance_num);
						//let lAdjustedPoint_p = { x: lPrevUnitPos_p.x + lMinUnitsDistance_num, y: lPrevUnitPos_p.y + lMinUnitsDistance_num };
						if (!!lAdjustedPoint_p) {
							lX_num = lAdjustedPoint_p.x;
							lY_num = lAdjustedPoint_p.y;
						}
					}
				}

				if(lX_num>5)
				{
					l_euv.adjust(lBetInfo_bi);
					l_euv.position.set(lX_num, lY_num);
					let lKingOfTheHill_bi = false;
					if (i === lUnitsCount_int - 1 || lBetInfo_bi.multiplier === lEjectedBets_bi_arr[lUnitsCount_int - 1].multiplier) {
						lKingOfTheHill_bi = true;
					}
					l_euv.setKingOfTheHill(lKingOfTheHill_bi);
				}else{
					l_euv.drop();
				}
			}
		} else {
			let masterUnits = [];

			for (let d = 0; d < lUnitsCount_int; d++) {
				let lBetInfo_bi = lEjectedBets_bi_arr[d];
				if (lBetInfo_bi.isMasterBet) {
					masterUnits.push({ bet: lBetInfo_bi, unit: this.getEjectableUnitView(d) });
				}
			}

			let masterUnitCount_int = masterUnits.length;

			for (let g = 0; g < masterUnitCount_int; g++) {

				let lBetInfo_bi = masterUnits[g].bet;
				let l_euv = masterUnits[g].unit;
				let lEjectedBetDuration_num = lBetInfo_bi.ejectTime - l_gpi.multiplierChangeFlightStartTime;
				let lTrackCurvePoint_p = null;

				if (l_gpi.isPreLaunchFlightRequired) {
					lTrackCurvePoint_p = this._isBattlegroundGame ? l_gpv.trackView.getBTGCurvePoint(lBetInfo_bi.multiplier)
						: l_gpv.trackView.getCurvePoint(lEjectedBetDuration_num + l_gpi.preLaunchFlightDuration);
				}
				else {
					lTrackCurvePoint_p = l_gpv.trackView.getCrashCurvePoint(lBetInfo_bi.multiplier);
				}

				//JUMP OUT X OFFSET...
				let lTargetX_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.x : this._fGraphView_gv.getCorrespondentCoordinateX(lEjectedBetDuration_num);
				let lDeltaX_num = lStarshipView_sv.position.x - lTargetX_num;

				if (lDeltaX_num < 0) {
					lDeltaX_num = 0;
				}

				let lDeltaCorrectionMillisecondsCount_int = this.getStarshipTakeOffDeltaCorrectionDurationInMilliseconds();
				let lEjectableUnitLifeDuration_int = lCurMultiplierRoundDuration_num - lEjectedBetDuration_num;

				if (lEjectableUnitLifeDuration_int < 0) {
					lEjectableUnitLifeDuration_int = 0;
				}

				let lDeltaDownscale_num = 1 - lEjectableUnitLifeDuration_int / lDeltaCorrectionMillisecondsCount_int;

				if (lDeltaDownscale_num < 0) {
					lDeltaDownscale_num = 0;
				}
				//...JUMP OUT X OFFSET

				lDeltaX_num *= lDeltaDownscale_num;

				let lX_num = lTargetX_num;
				if (lDeltaX_num > 0);
				{
					if (l_gpi.isPreLaunchFlightRequired) {
						lTrackCurvePoint_p = l_gpv.trackView.getPointInDistance(lTrackCurvePoint_p, lDeltaX_num);
						lX_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.x : lX_num;
					}
					else {
						lX_num = lTargetX_num + lDeltaX_num;
					}
				}

				let lY_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.y : this._fGraphView_gv.getCorrespondentCoordinateY(lBetInfo_bi.multiplier);

				let lCurPos_p = new PIXI.Point(lX_num, lY_num);

				if (g == 0) {
					let lLeftMostUnitPoint_p = lBottomTrajectoryPoint_p;
					if (lLeftMostUnitPoint_p && lX_num > lLeftMostUnitPoint_p.x) {
						lLeftMostUnitPoint_p = lCurPos_p;
					}

					//LEFT PADDING...
					if (
						!l_gpi.isPreLaunchFlightRequired
						&& (lX_num < lBottomTrajectoryPoint_p.x + lPaddingX_num)
					) {
						let lPositionWithPadding_p = l_gpv.trackView.getPointInDistance(lBottomTrajectoryPoint_p, lPaddingX_num);
						lLeftMostUnitPoint_p = lPositionWithPadding_p;
						lX_num = lPositionWithPadding_p.x;
						lY_num = lPositionWithPadding_p.y;
					}
					//...LEFT PADDING

					lMaxUnitsDistance_num = Math.floor(l_gpv.trackView.getTrackDistance(lLeftMostUnitPoint_p) / (masterUnitCount_int - 1));
					lMinUnitsDistance_num = Math.min(lMinUnitsDistance_num, lMaxUnitsDistance_num);
				}
				else {

					let lPrevUnit_euv = masterUnits[g - 1].unit;
					let lPrevUnitPos_p = new PIXI.Point(lPrevUnit_euv.position.x, lPrevUnit_euv.position.y);
					let lCurUnitsDistance_num = l_gpv.trackView.getTrackDistance(lPrevUnitPos_p, lCurPos_p);
					if (lCurPos_p.x < lPrevUnitPos_p.x || lCurUnitsDistance_num < lMinUnitsDistance_num) {
						let lAdjustedPoint_p = l_gpv.trackView.getPointInDistance(lPrevUnitPos_p, lMinUnitsDistance_num);
						//let lAdjustedPoint_p = { x: lPrevUnitPos_p.x + lMinUnitsDistance_num, y: lPrevUnitPos_p.y + lMinUnitsDistance_num };
						if (!!lAdjustedPoint_p) {
							lX_num = lAdjustedPoint_p.x;
							lY_num = lAdjustedPoint_p.y;
						}
					}
				}

				l_euv.position.set(lX_num, lY_num);
				l_euv.adjust(lBetInfo_bi);

			}

			let nonMasterUnits = [];

			for (let n = 0; n < lUnitsCount_int; n++) {
				let lBetInfo_bi = lEjectedBets_bi_arr[n];
				if (!lBetInfo_bi.isMasterBet) {
					nonMasterUnits.push({ bet: lBetInfo_bi, unit: this.getEjectableUnitView(n) });
				}
			}

			let nonMasterUnitCount_int = nonMasterUnits.length;

			for (let v = 0; v < nonMasterUnitCount_int; v++) {
				const nonMasterUnit =  nonMasterUnits[v];
				let lBetInfo_bi = nonMasterUnit.bet;
				let l_euv = nonMasterUnit.unit;
				let lEjectedBetDuration_num = lBetInfo_bi.ejectTime - l_gpi.multiplierChangeFlightStartTime;
				let lTrackCurvePoint_p = null;

				if (l_gpi.isPreLaunchFlightRequired) {
					lTrackCurvePoint_p = this._isBattlegroundGame ? l_gpv.trackView.getBTGCurvePoint(lBetInfo_bi.multiplier)
						: l_gpv.trackView.getCurvePoint(lEjectedBetDuration_num + l_gpi.preLaunchFlightDuration);
				}
				else {
					lTrackCurvePoint_p = l_gpv.trackView.getCrashCurvePoint(lBetInfo_bi.multiplier);
				}

				//JUMP OUT X OFFSET...
				let lTargetX_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.x : this._fGraphView_gv.getCorrespondentCoordinateX(lEjectedBetDuration_num);
				let lDeltaX_num = lStarshipView_sv.position.x - lTargetX_num;

				if (lDeltaX_num < 0) {
					lDeltaX_num = 0;
				}
				let lDeltaCorrectionMillisecondsCount_int = this.getStarshipTakeOffDeltaCorrectionDurationInMilliseconds();
				let lEjectableUnitLifeDuration_int = lCurMultiplierRoundDuration_num - lEjectedBetDuration_num;
				if (lEjectableUnitLifeDuration_int < 0) {
					lEjectableUnitLifeDuration_int = 0;
				}
				let lDeltaDownscale_num = 1 - lEjectableUnitLifeDuration_int / lDeltaCorrectionMillisecondsCount_int;
				if (lDeltaDownscale_num < 0) {
					lDeltaDownscale_num = 0;
				}
				//...JUMP OUT X OFFSET
				lDeltaX_num *= lDeltaDownscale_num;
				let lX_num = lTargetX_num;
				if (lDeltaX_num > 0);
				{
					if (l_gpi.isPreLaunchFlightRequired) {
						lTrackCurvePoint_p = l_gpv.trackView.getPointInDistance(lTrackCurvePoint_p, lDeltaX_num);
						lX_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.x : lX_num;
					}
					else {
						lX_num = lTargetX_num + lDeltaX_num;
					}
				}
				let lY_num = lTrackCurvePoint_p ? lTrackCurvePoint_p.y : this._fGraphView_gv.getCorrespondentCoordinateY(lBetInfo_bi.multiplier);
				let lCurPos_p = new PIXI.Point(lX_num, lY_num);
				if (v == 0) {
					let lLeftMostUnitPoint_p = lBottomTrajectoryPoint_p;
					if (lLeftMostUnitPoint_p && lX_num > lLeftMostUnitPoint_p.x) {
						lLeftMostUnitPoint_p = lCurPos_p;
					}
					//LEFT PADDING...
					if (
						!l_gpi.isPreLaunchFlightRequired
						&& (lX_num < lBottomTrajectoryPoint_p.x + lPaddingX_num)
					) {
						let lPositionWithPadding_p = l_gpv.trackView.getPointInDistance(lBottomTrajectoryPoint_p, lPaddingX_num);
						lLeftMostUnitPoint_p = lPositionWithPadding_p;
						lX_num = lPositionWithPadding_p.x;
						lY_num = lPositionWithPadding_p.y;
					}
					//...LEFT PADDING
					lMaxUnitsDistance_num = Math.floor(l_gpv.trackView.getTrackDistance(lLeftMostUnitPoint_p) / (nonMasterUnitCount_int - 1));
					lMinUnitsDistance_num = Math.min(lMinUnitsDistance_num, lMaxUnitsDistance_num);
				}
				else {
					let lPrevUnit_euv = nonMasterUnits[v - 1].unit;
					let lPrevUnitPos_p = new PIXI.Point(lPrevUnit_euv.position.x, lPrevUnit_euv.position.y);
					let lCurUnitsDistance_num = l_gpv.trackView.getTrackDistance(lPrevUnitPos_p, lCurPos_p);
					if (lCurPos_p.x < lPrevUnitPos_p.x || lCurUnitsDistance_num < lMinUnitsDistance_num) {
						let lAdjustedPoint_p = l_gpv.trackView.getPointInDistance(lPrevUnitPos_p, lMinUnitsDistance_num);
						//let lAdjustedPoint_p = { x: lPrevUnitPos_p.x + lMinUnitsDistance_num, y: lPrevUnitPos_p.y + lMinUnitsDistance_num };
						if (!!lAdjustedPoint_p) {
							lX_num = lAdjustedPoint_p.x;
							lY_num = lAdjustedPoint_p.y;
						}
					}
				}
				l_euv.position.set(lX_num, lY_num);
				l_euv.adjust(lBetInfo_bi);
			}
		}


		for (let i = lUnitsCount_int; i < this._fEjectableUnits_euv_arr.length; i++) {
			this._fEjectableUnits_euv_arr[i].drop();
		}



		if (lUnitsCount_int > 0) {
			const pullCounter = [];
			let lCurrWinMult = lEjectedBets_bi_arr[lUnitsCount_int - 1].multiplier
			for (let i = lUnitsCount_int - 1; i >= 0; i--) {
				let l_euv = this.getEjectableUnitView(i);
				let lBetInfo_bi = lEjectedBets_bi_arr[i];
				let isMaster = lBetInfo_bi.isMasterBet;
				let ejectTime = lBetInfo_bi.ejectTime;
				if (isMaster) {
					l_euv.initView();
					lCurrWinMult = lBetInfo_bi.multiplier;
					l_euv.transparent = false;
					l_euv.visible = true;
					l_euv.alpha = 1;
				}
				else if (!l_euv.hideTime) {
				
					if (!this._isBattlegroundGame) 
					{

						let previousMasterBets = [];
						let previousOtherBets = [];
						
						for (let m = 0; m < lEjectedBets_bi_arr.length; m++) {
							let mbetInfo = lEjectedBets_bi_arr[m];
							if(mbetInfo.isMasterBet)
							{
								previousMasterBets.push(mbetInfo);
							}else{
								previousOtherBets.push(mbetInfo);
							}
						}

						let invisible = false;

						for (let t = 0; t < previousMasterBets.length; t++) {
							const mBet = previousMasterBets[t];
							let ejetDif = mBet.ejectTime - lBetInfo_bi.ejectTime; 
							if(ejetDif<0)
							{
								ejetDif = ejetDif  * -1;
							}
							if (ejetDif < 700 ) {
								invisible = true;
							}
						}

						if(!pullCounter[ejectTime])
						{
							pullCounter[ejectTime] = 1;
						}else{
							pullCounter[ejectTime] += 1;
							if(pullCounter[ejectTime]>3)
							{
								invisible = true;
							}
						}
						if (!invisible) {
							l_euv.initView();
							l_euv.transparent = true;
							l_euv.hideTime = ejectTime;
						}else{
							l_euv.hideTime = ejectTime;
						}
					}else{
						l_euv.initView();
						l_euv.transparent = true;
						l_euv.hideTime = lBetInfo_bi.ejectTime;
					}
				}
			}
		}
	}
}
export default EjectableUnitsPoolView;