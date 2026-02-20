import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import NonWobblingTextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import AlignDescriptor from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import { FRAME_RATE } from '../../../../config/Constants';
import { GAME_VIEW_SETTINGS } from '../../../main/GameBaseView';
import { ROUND_STATES } from '../../../../model/gameplay/RoundInfo';
import BattlegroundTrampolineView from '../../battleground/BattlegroundTrampolineView';
import GameplayStateScreenBaseStartView from '../GameplayStateScreenBaseStartView';
import { BTG_PLAY_STATE_MULTIPLIER_CHANGE_DELAY } from '../../../../model/gameplay/GameplayInfo';

const TIMER_BASE_SCALE = 0.375;

class BattlegroundGameplayStateScreenStartView extends GameplayStateScreenBaseStartView {
	static get EVENT_ON_TIMER_VALUE_UPDATED() { return GameplayStateScreenBaseStartView.EVENT_ON_TIMER_VALUE_UPDATED; }

	trampolineJump() {
		this._fTrampoline_sprt && this._fTrampoline_sprt.trampolineJump();
	}

	constructor() {
		super();

		this._fBlackoutContainer_sprt = null;
		this._fBlackoutRRContainer_sprt = null;
		this._fTimeToStart_tf = null;
		this._fBlackout_gr = null;
		this._fBlackoutRR_gr = null;
		this._fTimerContainer_sprt = null;
		this._fTrampoline_sprt = null;

		let l_gpi = APP.gameController.gameplayController.info;

		//CONTENT...
		this._addContent();
		this._updateBlackoutView();
		this._updateBlackoutRRView();
		//...CONTENT

		this._fBlackoutOutroAnimation_mtl = null;
		this._fOutroAnimation_mtl = null;

		this._tryToAddOutroAnimation();
	}

	_tryToAddOutroAnimation() {
		if (this._fOutroAnimation_mtl) {
			return;
		}

		this._addOutroAnimation();
	}

	_addOutroAnimation() {
		let l_gpi = APP.gameController.gameplayController.info;

		//BLACKOUT OUTRO ANIMATION...
		let l_mt = new MTimeLine();
		//BLACKOUT...
		l_mt.addAnimation(
			this._fBlackoutContainer_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 50]
			]);
		//...BLACKOUT
		this._fBlackoutOutroAnimation_mtl = l_mt;
		//...BLACKOUT OUTRO ANIMATION

		//OUTRO ANIMATION...
		l_mt = new MTimeLine();
		//CONTENT...
		l_mt.addAnimation(
			this._fTimerContainer_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				5,
				[0, 40],
				5
			]);

		l_mt.addAnimation(
			this._fPlayersNeededContainer_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				5,
				[0, 40],
				5
			]);
		//...CONTENT

		//TRAMPOLINE...
		l_mt.addAnimation(
			this._fTrampoline_sprt,
			MTimeLine.SET_SCALE,
			1,
			[
				[1.2, 10],
				[0, 7],
				10
			]);
		//...TRAMPOLINE

		let lBaseScale_num = TIMER_BASE_SCALE;

		l_mt.addAnimation(
			this._fTimerContainer_sprt,
			MTimeLine.SET_SCALE,
			lBaseScale_num,
			[
				5,
				[lBaseScale_num * 1.25, 40],
				5
			]);

		l_mt.addAnimation(
			this._fPlayersNeededContainer_sprt,
			MTimeLine.SET_SCALE,
			lBaseScale_num,
			[
				5,
				[lBaseScale_num * 1.25, 40],
				5
			]);

		this._fOutroAnimation_mtl = l_mt;
		//...OUTRO ANIMATION
	}

	updateArea() {
		this._updateBlackoutView();
		this._updateBlackoutRRView();

		this.shipContainer.position.set(-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2, -GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2);

		this._fTrampoline_sprt && (this._fTrampoline_sprt.position.x = APP.layout.isPortraitOrientation ? 80 : 0);
	}

	get startScreenViewEndTime() {
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lRoundStartTime_num = lRoundInfo_ri.roundStartTime;

		return lRoundInfo_ri.isRoundPlayState ? lRoundStartTime_num - BTG_PLAY_STATE_MULTIPLIER_CHANGE_DELAY + this._screenVisibilityExtraTime : l_gpi.gameplayTime + 1100 /* to guarantee start screen in WAIT state*/;
	}

	get sitInOutUnitsContainer() {
		return this._fSitInOutUnitsContainer_sprt;
	}

	get _screenVisibilityExtraTime() {
		return 1500;
	}

	adjust() {
		this._tryToAddOutroAnimation();

		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lCurGameplayTime_num = l_gpi.gameplayTime;
		let lRoundStartTime_num = lRoundInfo_ri.roundStartTime;

		let lViewDisappearingCurDuration_num = 0;
		if (lRoundInfo_ri.isRoundPlayState) {
			lViewDisappearingCurDuration_num = lCurGameplayTime_num - (lRoundStartTime_num - BTG_PLAY_STATE_MULTIPLIER_CHANGE_DELAY);
		}

		this._fBlackoutRR_gr.alpha = 0;
		if (l_gpv.battlegroundRoundResultInformationScreenView.visible) {
			this._fBlackoutRR_gr.alpha = 0.4;
		}

		let lBlackoutOutro_mtl = this._fBlackoutOutroAnimation_mtl;
		lBlackoutOutro_mtl.windToMillisecond(lViewDisappearingCurDuration_num);

		let lOutro_mtl = this._fOutroAnimation_mtl;
		lOutro_mtl.windToMillisecond(lViewDisappearingCurDuration_num);

		let l_tf = this._fTimeToStart_tf;

		let lPrevTimerValue_str = l_tf.text;
		let lRestTimeToRound_num = lRoundInfo_ri.isRoundPlayState ? 0 : l_gpi.roundStartRestTime;
		l_tf.text = this._formatTimeValue(lRestTimeToRound_num);


		if(APP.isCAFMode)
		{
			if(APP.isCAFRoomManager){
				let dialogController = APP.dialogsController._cafRoomManagerController;
				this._fPlayersNeededContainer_sprt.visible = true;
				const info = dialogController.info;
				const readyPlayers = info.readyPlayersAmount;
				const minimalPlayers = APP.minimalCafPlayersReady;
				this._fplayersNeededToStart_tf.text = readyPlayers +  "/"  + minimalPlayers;
			}else{
				this._fPlayersNeededContainer_sprt.visible = false;
			}	
		}

		if (l_tf.text !== lPrevTimerValue_str) {
			this.emit(BattlegroundGameplayStateScreenStartView.EVENT_ON_TIMER_VALUE_UPDATED, { restTime: lRestTimeToRound_num });
		}
	}

	_addBlackoutView() {
		let l_sprt = new Sprite();
		let l_gr = this._fBlackout_gr = l_sprt.addChild(new PIXI.Graphics());

		l_sprt.alpha = 1;
		this.addChild(l_sprt);

		this._fBlackoutContainer_sprt = l_sprt;
	}

	_addBlackoutRRView() {
		let l_sprt = new Sprite();
		let l_gr = this._fBlackoutRR_gr = l_sprt.addChild(new PIXI.Graphics());

		this.addChild(l_sprt);

		this._fBlackoutRRContainer_sprt = l_sprt;
	}

	_updateBlackoutRRView() {
		let l_gr = this._fBlackoutRR_gr;

		l_gr.cacheAsBitmap = false;
		l_gr.clear();

		l_gr.beginFill(0x000000).drawRect(
			-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2 - 1,
			-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2 - 1,
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width + 2,
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + 2)
			.endFill();
	}

	_updateBlackoutView() {
		let l_gr = this._fBlackout_gr;

		l_gr.cacheAsBitmap = false;
		l_gr.clear();

		l_gr.beginFill(0x000000, 0.72).drawRect(
			-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width / 2 - 1,
			-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height / 2 - 1,
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width + 2,
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + 2)
			.endFill();
	}

	_addContent() {
		let l_gpi = APP.gameController.gameplayController.info;

		//BLACKOUT...
		this._addBlackoutView();
		//...BLACKOUT

		this._fShipContainer_sprt = this.addChild(new Sprite);

		this._fTrampoline_sprt = this.addChild(new BattlegroundTrampolineView);
		this._fTrampoline_sprt.startIdleAnimation();

		this._fSitInOutUnitsContainer_sprt = this.addChild(new Sprite);

		this._addBlackoutRRView();

		let lTimerContainer_sprt = this._fTimerContainer_sprt = this.addChild(new Sprite);

		let lPlayersNeededContainer_sprt = this._fPlayersNeededContainer_sprt = this.addChild(new Sprite);

		if (APP.isCAFMode) {
			this._fTimerContainer_sprt.visible = false;
		} else {
			this._fPlayersNeededContainer_sprt.visible = false;
		}

		let lLabelAsset_ta = I18.generateNewCTranslatableAsset("TABattlegroundNextFlightInLabel");
		lLabelAsset_ta.position.set(0, -40);
		this._fNextLaunchInLabel_ta = lLabelAsset_ta;
		lTimerContainer_sprt.addChild(lLabelAsset_ta);

		let lLabelPlayersNeededAsset_ta = I18.generateNewCTranslatableAsset("TABCAFattlegroundNPlayersNeededLabel");
		lLabelPlayersNeededAsset_ta.position.set(0, -40);
		lPlayersNeededContainer_sprt.addChild(lLabelPlayersNeededAsset_ta);


		let lBaseScale_num = TIMER_BASE_SCALE;
		lTimerContainer_sprt.scale.set(1.25 * lBaseScale_num);

		let l_tf = this._fTimeToStart_tf = new NonWobblingTextField();
		lTimerContainer_sprt.addChild(l_tf);
		l_tf.fontName = "fnt_nm_barlow_bold";
		l_tf.fontSize = 100;
		l_tf.fontColor = 0xffffff;
		l_tf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		l_tf.letterSpace = -5;
		l_tf.maxWidth = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;

		
		let l_playersNeeded = this._fplayersNeededToStart_tf = new NonWobblingTextField();
		this._fPlayersNeededContainer_sprt.addChild(l_playersNeeded);
		l_playersNeeded.fontName = "fnt_nm_barlow_bold";
		l_playersNeeded.fontSize = 100;
		l_playersNeeded.fontColor = 0xffffff;
		l_playersNeeded.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		l_playersNeeded.letterSpace = -5;
		l_playersNeeded.maxWidth = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;
		l_playersNeeded.text = "0/4";


		lTimerContainer_sprt.position.x = 58;
		lTimerContainer_sprt.position.y = 183;

		lPlayersNeededContainer_sprt.position.x = 58;
		lPlayersNeededContainer_sprt.position.y = 165;
	}

	_formatTimeValue(aTime_num) {
		aTime_num = +aTime_num;
		if (isNaN(aTime_num) || aTime_num < 0) {
			aTime_num = 0;
		}

		let lMillisecondsCount_int = Math.floor((aTime_num % 1000) / 100);
		let lSecondsCount_int = Math.floor((aTime_num / 1000) % 60);
		let lMinutesCount_int = Math.floor((aTime_num / (1000 * 60)) % 60);

		lMinutesCount_int = (lMinutesCount_int < 10) ? "0" + lMinutesCount_int : lMinutesCount_int;
		lSecondsCount_int = (lSecondsCount_int < 10) ? "0" + lSecondsCount_int : lSecondsCount_int;
		lMillisecondsCount_int = (lMillisecondsCount_int < 10) ? lMillisecondsCount_int + "0" : lMillisecondsCount_int;

		let lFormattedTime_str = `${lMinutesCount_int}:${lSecondsCount_int}:${lMillisecondsCount_int}`;
		return lFormattedTime_str;
	}
}

export default BattlegroundGameplayStateScreenStartView;