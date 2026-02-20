import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayStateScreenStartView from './GameplayStateScreenStartView';
import BattlegroundGameplayStateScreenStartView from './battleground/BattlegroundGameplayStateScreenStartView';
import GameplayStateScreenEndView from './GameplayStateScreenEndView';
import GameplayStateScreenProgressView from './GameplayStateScreenProgressView';
import BattlegroundGameplayStateScreenProgressView from './battleground/BattlegroundGameplayStateScreenProgressView';

class GameplayStateScreenView extends Sprite {
	static get EVENT_ON_ACTIVE_SCREEN_CHANGED() { return "EVENT_ON_ACTIVE_SCREEN_CHANGED"; }
	static get EVENT_ON_EJECT_INITIATED() { return GameplayStateScreenProgressView.EVENT_ON_EJECT_INITIATED };
	static get EVENT_ON_EJECT_ALL_INITIATED() { return GameplayStateScreenProgressView.EVENT_ON_EJECT_ALL_INITIATED };
	static get EVENT_ON_TIMER_VALUE_UPDATED() { return GameplayStateScreenStartView.EVENT_ON_TIMER_VALUE_UPDATED };

	constructor() {
		super();

		this._fStartView_gssscv = null;
		this._fProgressView_gsspcv = null;
		this._fEndView_gssecv = null;

		this._fLastActiveScreen_sprt = null;

		this._addRoundStartView();
		this._addRoundProgressView();
		this._addRoundEndView();
	}

	trampolineJump() {
		this._fStartView_gssscv.trampolineJump();
	}

	get isStartRoundScreenActive() {
		return this._fStartView_gssscv === this._fLastActiveScreen_sprt;
	}

	get isProgressRoundScreenActive() {
		return this._fProgressView_gsspcv === this._fLastActiveScreen_sprt;
	}

	get isEndRoundScreenActive() {
		return this._fEndView_gssecv === this._fLastActiveScreen_sprt;
	}

	get shipContainer() {
		return this._fStartView_gssscv._fShipContainer_sprt;
	}

	get startScreenView() {
		return this._fStartView_gssscv;
	}

	adjust() {
		let l_gpc = APP.gameController.gameplayController;
		let l_gpi = l_gpc.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lBetsInfo_bsi = l_gpi.gamePlayersInfo.betsInfo;

		let lRoundStartTime_num = lRoundInfo_ri.roundStartTime;
		let lRoundEndTime_num = lRoundInfo_ri.roundEndTime;
		let lStartViewEndTime_num = this._fStartView_gssscv.startScreenViewEndTime;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		let lNewActiveScreen_sprt = null;
		let lStartViewRequired_bl = lBetsInfo_bsi.isNoMoreBetsPeriodMode ?
			(lRoundInfo_ri.isRoundWaitState || lBetsInfo_bsi.isNoMoreBetsPeriod || (lRoundInfo_ri.isRoundPlayState && lCurGameplayTime_num < lStartViewEndTime_num))
			: (lCurGameplayTime_num < lStartViewEndTime_num && (lRoundInfo_ri.isRoundWaitState || lRoundInfo_ri.isRoundPlayState));

		if (lStartViewRequired_bl) {
			let lIsStartViewRenderingAllowed_bl = true;
			if (APP.isBattlegroundGame) {
				let lYouWonAnimation_bywav = APP.gameController.gameplayController.view.battlegroundYouWonView;
				if (lYouWonAnimation_bywav.isAnimationInProgress) {
					lIsStartViewRenderingAllowed_bl = false;
				}
			}

			if (lIsStartViewRenderingAllowed_bl) {
				this._fStartView_gssscv.adjust();
				this._fStartView_gssscv.visible = true;
			}
			else {
				this._fStartView_gssscv.visible = false;
			}

			lNewActiveScreen_sprt = this._fStartView_gssscv;

			this._fProgressView_gsspcv.visible = false;

			if (!!this._fEndView_gssecv) {
				this._fEndView_gssecv.visible = false;
			}
		}
		else {
			this._fStartView_gssscv.visible = false;
		}

		let lProgressViewRequired_bl = null;
		let lProgressViewStartTime_num = 0;

		if (APP.isBattlegroundGame) {
			lProgressViewRequired_bl = lRoundInfo_ri.isRoundPlayActive && l_gpi.multiplierChangeFlightRestTime < 0;
		}
		else {
			lProgressViewStartTime_num = lRoundStartTime_num - (l_gpi.isPreLaunchFlightRequired ? 400 : 0);
			lProgressViewRequired_bl = lCurGameplayTime_num >= lProgressViewStartTime_num
				&& (lRoundInfo_ri.isRoundPlayActive || l_gpi.isPreLaunchFlightRequired)
		}

		if (lProgressViewRequired_bl) {
			this._fProgressView_gsspcv.adjust(lProgressViewStartTime_num);
			this._fProgressView_gsspcv.visible = true;
			lNewActiveScreen_sprt = this._fProgressView_gsspcv;

			this._fStartView_gssscv.visible = lCurGameplayTime_num < lStartViewEndTime_num;

			if (!!this._fEndView_gssecv) {
				this._fEndView_gssecv.visible = false;
			}
		}
		else {
			this._fProgressView_gsspcv.visible = false;
		}

		let lEndViewRequired_bl = !!this._fEndView_gssecv && lCurGameplayTime_num >= lRoundEndTime_num && (lRoundInfo_ri.isRoundQualifyState || lRoundInfo_ri.isRoundEndTimeDefined);
		if (lEndViewRequired_bl) {

			this._fEndView_gssecv.adjust();
			this._fEndView_gssecv.visible = true;
			lNewActiveScreen_sprt = this._fEndView_gssecv;

			this._fStartView_gssscv.visible = false;
			this._fProgressView_gsspcv.visible = false;
		}
		else {
			if (!!this._fEndView_gssecv) {
				this._fEndView_gssecv.visible = false;
			}
		}

		if (!this._fLastActiveScreen_sprt || this._fLastActiveScreen_sprt !== lNewActiveScreen_sprt) {
			this._fLastActiveScreen_sprt = lNewActiveScreen_sprt;
			this.emit(GameplayStateScreenView.EVENT_ON_ACTIVE_SCREEN_CHANGED);
		}
	}

	updateArea() {
		this._fStartView_gssscv.updateArea();
		this._fProgressView_gsspcv.updateArea();
	}

	_addRoundStartView() {
		let l_gsssv = APP.isBattlegroundGame ? new BattlegroundGameplayStateScreenStartView : new GameplayStateScreenStartView;
		let l_sprt = this._fStartView_gssscv = this.addChild(l_gsssv);
		l_sprt.on(GameplayStateScreenStartView.EVENT_ON_TIMER_VALUE_UPDATED, this.emit, this);
		l_sprt.visible = false;
	}

	_addRoundProgressView() {
		let l_gsspcv = this._fProgressView_gsspcv = APP.isBattlegroundGame ? new BattlegroundGameplayStateScreenProgressView : new GameplayStateScreenProgressView;
		l_gsspcv.on(GameplayStateScreenProgressView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		l_gsspcv.on(GameplayStateScreenProgressView.EVENT_ON_EJECT_ALL_INITIATED, this.emit, this);
		l_gsspcv.visible = false;

		this.addChild(l_gsspcv);
	}

	_addRoundEndView() {
		if (APP.isBattlegroundGame) {
			// GameplayStateScreenEndView is not used in Battleground version
		}
		else {
			let l_sprt = this._fEndView_gssecv = this.addChild(new GameplayStateScreenEndView);
			l_sprt.visible = false;
		}
	}
}

export default GameplayStateScreenView