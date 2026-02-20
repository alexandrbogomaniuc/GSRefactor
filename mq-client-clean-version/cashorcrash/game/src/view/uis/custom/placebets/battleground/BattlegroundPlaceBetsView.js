import PlaceBetsBaseView from './../PlaceBetsBaseView';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import GameButton from '../../../../../ui/GameButton';
import Button from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameplayInfo from '../../../../../model/gameplay/GameplayInfo';
import BattlegroundGameModel from '../../../../../model/main/BattlegroundGameModel';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';
import AtlasConfig from '../../../../../config/AtlasConfig';
import AtlasSprite  from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import { BitmapText } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';


const CANCEL_BET_DELAY_IN_MS = 2500;

class BattlegroundPlaceBetsView extends PlaceBetsBaseView
{
	static get EVENT_ON_PLACE_BETS ()					{ return PlaceBetsBaseView.EVENT_ON_PLACE_BETS; }
	static get EVENT_ON_CANCEL_BET ()					{ return PlaceBetsBaseView.EVENT_ON_CANCEL_BET; }
	static get EVENT_ON_CHANGE_BET_BUTTON_CLICKED ()	{ return "EVENT_ON_CHANGE_BET_BUTTON_CLICKED" }

	needPlaceBet()
	{
		if (this._fPlaceBetButton_gb.enabled)
		{
			this._onPlaceBetClicked();
		}
	}

	skipBetCancelDelay()
	{
		this._destroyCancelBetDelayTimer();
	}

	//INIT...
	constructor()
	{
		super();

		this._fLongBase_gr = null;
		this._fShortBase_gr = null;
		this._fContentContainer_sprt = null;

		this._fDistanceValue_tf = null;
		this._fTimeValue_tf = null;
		this._fPlaceBetButton_gb = null;
		this._fCancelBetButton_gb = null;
		this._fChangeBetButton_gb = null;
		this._fCancelBetDelayTimer_t = null;
		this._fGamePlayersController_gpsc = null;
	}

	_updateViewPosition(aIsPortraitMode_bl)
	{
		let lX_num = this._fContentX_num || 0;
		let lY_num = this._fContentY_num || 0;

		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;

		if (aIsPortraitMode_bl)
		{
			if (lRoundInfo_ri.isRoundWaitState)
			{
				lY_num -= 225;
			}
			else
			{
				lY_num -= 62;
			}

			lX_num = lRoundInfo_ri.isRoundWaitState ? APP.screenWidth/2 : 9;
		}

		this.position.set(lX_num, lY_num);
	}
	
	validate()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;
		let l_gci = APP.gameController.info;
		let lGamePlayersController_gpsc = this._fGamePlayersController_gpsc || (this._fGamePlayersController_gpsc = APP.gameController.gameplayController.gamePlayersController);
		let lBetsInfo_bsi = this._betsController.info;
		let lBetInfo_bi = this._masterBetInfo;
		
		let lIsBetCancelInProgress_bl = !!lBetInfo_bi && this._betsController.isBetCancelInProgress(lBetInfo_bi.betId);

		this._fPlaceBetButton_gb.visible = l_ri.isRoundWaitState && (!lBetInfo_bi || !lBetInfo_bi.isConfirmedMasterBet);
		this._fPlaceBetButton_gb.enabled = this._fPlaceBetButton_gb.visible && l_ri.isRoundWaitState
											&& lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined && !lGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered
											&& !lBetInfo_bi
											&& !lIsBetCancelInProgress_bl;


		if(!lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined && !this._rejoinCalled && l_ri.isRoundWaitState)
		{
			setTimeout(()=>{
				if(!lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined &&  l_ri.isRoundWaitState)
				{
					APP.gameController.gameplayController.gamePlayersController.reJoinGame();	
				}
			},1000);
			this._rejoinCalled = true;
		} 

		if(lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined && l_ri.isRoundWaitState)
		{
			this._rejoinCalled = false;
		}

		this._fCancelBetButton_gb.visible = l_ri.isRoundWaitState && !!lBetInfo_bi && lBetInfo_bi.isConfirmedMasterBet;
		this._fCancelBetButton_gb.enabled = this._fCancelBetButton_gb.visible && lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined && !lGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered
											&& !lIsBetCancelInProgress_bl && !this._isCancelBetDelayInPeriod;

		this._fChangeBetButton_gb.visible = l_ri.isRoundWaitState;
		this._fChangeBetButton_gb.enabled = this._fChangeBetButton_gb.visible && l_ri.isRoundWaitState && (!lBetInfo_bi || !lBetInfo_bi.isConfirmedMasterBet);

		this._fNextRound_ta.visible = l_ri.isRoundWaitState;
		
		this._fDistanceLabel_ta.visible = l_ri.isRoundPlayState || l_ri.isRoundQualifyState;
		this._fTimeLabel_ta.visible = l_ri.isRoundPlayState || l_ri.isRoundQualifyState;

		this._fDistanceValue_tf.visible = l_ri.isRoundPlayState || l_ri.isRoundQualifyState;
		this._fTimeValue_tf.visible = l_ri.isRoundPlayState || l_ri.isRoundQualifyState;
		
		this._fShortBase_gr.visible = l_ri.isRoundPlayState || l_ri.isRoundQualifyState;
		this._fLongBase_gr.visible = l_ri.isRoundWaitState;
		
		if (this._fDistanceValue_tf.visible || this._fTimeValue_tf.visible)
		{
			this._updateStatusFields();
		}

		if (l_ri.isRoundWaitState)
		{
			this._startCancelBetDelayTimerIfRequired();
		}
		else
		{
			this._destroyCancelBetDelayTimer();
		}

		if(APP.isCAFMode)
		{
			this._fPlaceBetButton_gb.visible = false;
			this._fPlaceBetButton_gb.enabled = false;
			this._fCancelBetButton_gb.visible = false;
			this._fCancelBetButton_gb.enabled = false;
			this._fChangeBetButton_gb.visible = false;
			this._fChangeBetButton_gb.enabled = false;
			this._fNextRound_ta.visible = false;
		}
	}

	get _masterBetInfo()
	{
		let lBetInfo_bi = this._betsController.info.getMasterBetInfoByIndex(this.uiInfo.betIndex, true) || null;
		
		return lBetInfo_bi;	
	}

	get _betsController()
	{
		return this._fBetsController_bsc || (this._fBetsController_bsc = APP.gameController.gameplayController.gamePlayersController.betsController);
	}

	_updateStatusFields()
	{
		let l_gpi = APP.gameController.gameplayController.info;

		let lMultiplier_num = l_gpi.serverMultiplierValue;
		let lDistanceValue_str = GameplayInfo.formatMultiplier(lMultiplier_num);
		if (this._fDistanceValue_tf)
		{
			this._updateDistanceValue(lDistanceValue_str);
		}

		let lTimeValue_num = l_gpi.serverMultiplierRoundDuration;
		this._updateTimeValue(lTimeValue_num);
	}

	__init()
	{
		super.__init();

		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("roboto/bmp_roboto_medium")], [AtlasConfig.BmpRobotoMedium], "");
		this._fTextures_tx_map_full = AtlasSprite.getMapFrames([APP.library.getAsset("scorescoreboard_font/scoreboard_font")], [AtlasConfig.ScoreBoardFont], "");

		this._fLongBase_gr = this.addChild(new PIXI.Graphics);
		this._fShortBase_gr = this.addChild(new PIXI.Graphics)

		let lContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);
		lContainer_sprt.position.set(0, 284);

		let lPlaceBetButtonBase_gr = new PIXI.Graphics();
		let lPlaceBetCaptionName_str = "TABattlegroundBuyIn";
		let lPlaceBetButton_gb = this._fPlaceBetButton_gb = lContainer_sprt.addChild(new GameButton(lPlaceBetButtonBase_gr, lPlaceBetCaptionName_str, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lPlaceBetButton_gb.on("pointerclick", this._onPlaceBetClicked, this);

		let lCancelBetButtonBase_gr = new PIXI.Graphics();
		let lCancelBetCaptionName_str = "TABattlegroundCancelBuyIn";
		let lCancelBetButton_gb = this._fCancelBetButton_gb = lContainer_sprt.addChild(new GameButton(lCancelBetButtonBase_gr, lCancelBetCaptionName_str, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lCancelBetButton_gb.on("pointerclick", this._onCancelBetClicked, this);
		
		let lChangeBetButtonBase_gr = new PIXI.Graphics();
		let lChangeBetCaptionName_str = "TABattlegroundChangeBet";
		let lChangeBetButton_gb = this._fChangeBetButton_gb = lContainer_sprt.addChild(new GameButton(lChangeBetButtonBase_gr, lChangeBetCaptionName_str, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lChangeBetButton_gb.on("pointerclick", this._onChangeBetClicked, this);
		
		this._fNextRound_ta = lContainer_sprt.addChild(new BitmapText(this._fTextures_tx_map_full, "",2));
		this._fNextRound_ta.write("PLAY AGAIN");
	
		this._fTimeLabel_ta = lContainer_sprt.addChild(APP.library.getSprite("labels/time"))
		this._fDistanceLabel_ta = lContainer_sprt.addChild(APP.library.getSprite("labels/distance"));


		
		this._fDistanceValue_tf = lContainer_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", -20));
		this._fTimeValue_tf = lContainer_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", -20));

		this._updateLayoutSettings();
	}
	//...INIT

	_onPlaceBetClicked(event)
	{
		let lBetIndex_num = this.uiInfo.betIndex;
		let lBetValue_num = this.uiInfo.betValue;
		let lBetAutoEjectMult_num = this.uiInfo.betAutoEjectMult;

		this._emitPlaceBets([{betIndex: lBetIndex_num, betValue: lBetValue_num, autoEjectMultipleier: lBetAutoEjectMult_num}]);
	}

	_emitPlaceBets(aBets_arr)
	{
		let l_ri = APP.gameController.gameplayController.info.roundInfo;
		if (!!aBets_arr && !!aBets_arr.length && !this._betsController.isPlaceBetInProgress() && l_ri.isRoundWaitState)
		{
			this.emit(BattlegroundPlaceBetsView.EVENT_ON_PLACE_BETS, {bets: aBets_arr});
		}
		else
		{
			console.log("WARNING: can't confirm BUY IN, try again later.");
		}
	}

	_onCancelBetClicked()
	{
		this.emit(BattlegroundPlaceBetsView.EVENT_ON_CANCEL_BET, {betIndex: BattlegroundGameModel.DEFAULT_BET_INDEX});
	}

	_onChangeBetClicked()
	{
		this.emit(BattlegroundPlaceBetsView.EVENT_ON_CHANGE_BET_BUTTON_CLICKED);
	}

	_updateLayoutSettings()
	{
		super._updateLayoutSettings();

		if (this._fContentWidth_num === undefined || this._fContentHeight_num === undefined) return;

		let lIsPortraitMode_bi = this._fIsPortraitMode_bl;

		let lMobilePostfix_str = APP.isMobile ? 'Mobile' : '';
		let lPortraitPostfix_str = lIsPortraitMode_bi ? 'Portrait' : '';

		// BUY IN BUTTON...
		let lPlaceBet_ta = "TABattlegroundBuyIn" + lPortraitPostfix_str;
		let lPlaceBetButtonBase_gr = new PIXI.Graphics();
		if (lIsPortraitMode_bi)
		{
			lPlaceBetButtonBase_gr.beginFill(0x13ad42).drawRoundedRect(-100, -27, 200, 60, 4).endFill();
		}
		else
		{
			lPlaceBetButtonBase_gr.beginFill(0x13ad42).drawRoundedRect(-100, -20, 200, 40, 4).endFill();
		}

		let lPlaceBetButton_gb = this._fPlaceBetButton_gb;
		lPlaceBetButton_gb.updateBase(lPlaceBetButtonBase_gr);
		lPlaceBetButton_gb.updateCaptionView(I18.generateNewCTranslatableAsset(lPlaceBet_ta));
		lPlaceBetButton_gb.position.x = 126;
		lPlaceBetButton_gb.position.y = lIsPortraitMode_bi ? 148 : 38;
		// ...BUY IN BUTTON

		// CANCEL BET BUTTON...
		let lCancelBet_ta = "TABattlegroundCancelBuyIn" + lPortraitPostfix_str;
		let lCancelBetButtonBase_gr = new PIXI.Graphics();
		if (lIsPortraitMode_bi)
		{
			lCancelBetButtonBase_gr.beginFill(0xff4a4a).drawRoundedRect(-100, -27, 200, 60, 4).endFill();
		}
		else
		{
			lCancelBetButtonBase_gr.beginFill(0xff4a4a).drawRoundedRect(-100, -20, 200, 40, 4).endFill();
		}

		let lCancelBetButton_gb = this._fCancelBetButton_gb;
		lCancelBetButton_gb.updateBase(lCancelBetButtonBase_gr);
		lCancelBetButton_gb.updateCaptionView(I18.generateNewCTranslatableAsset(lCancelBet_ta));
		lCancelBetButton_gb.position.x = 126;
		lCancelBetButton_gb.position.y = lIsPortraitMode_bi ? 148 : 38;
		// ...CANCEL BET BUTTON

		// CHANGE BET BUTTON...
		let lChangeBet_ta = "TABattlegroundChangeBet" + lPortraitPostfix_str;
		let lChangeBetButtonBase_gr = new PIXI.Graphics();
		if (lIsPortraitMode_bi)
		{
			lChangeBetButtonBase_gr.beginFill(0xe6bf4a).drawRoundedRect(-100, -27, 200, 60, 4).endFill();
		}
		else
		{
			lChangeBetButtonBase_gr.beginFill(0xe6bf4a).drawRoundedRect(-100, -20, 200, 40, 4).endFill();
		}
		
		let lChangeBetButton_gb = this._fChangeBetButton_gb;
		lChangeBetButton_gb.updateBase(lChangeBetButtonBase_gr);
		lChangeBetButton_gb.updateCaptionView(I18.generateNewCTranslatableAsset(lChangeBet_ta));
		lChangeBetButton_gb.position.x = 126;
		lChangeBetButton_gb.position.y = lIsPortraitMode_bi ? 238 : 88;
		// CHANGE BET BUTTON...

		let lNextRound_ta = this._fNextRound_ta;
		lNextRound_ta.position.set(APP.isMobile ? 45 : 45, APP.isMobile ? -15 : -15);

		//DISTANCE INDICATOR...
		let lDistanceLabel_ta = this._fDistanceLabel_ta;
		lDistanceLabel_ta.position.x = 70;
		lDistanceLabel_ta.position.y = lIsPortraitMode_bi?100:78;

		let lDistanceValue_tf = this._fDistanceValue_tf;
		lDistanceValue_tf.position.x = lIsPortraitMode_bi ? 120 : 120;
		lDistanceValue_tf.position.y = lIsPortraitMode_bi ? 101 : 79;
		lDistanceValue_tf.scale.set(0.4, 0.4);
		//...DISTANCE INDICATOR

		//TIME INDICATOR...
		this._fTimeLabel_ta.position.x = 50;
		this._fTimeLabel_ta.position.y = lIsPortraitMode_bi ? (APP.isMobile ? 70 : 70) : 54; 
		let lTimeValue_tf = this._fTimeValue_tf;
		lTimeValue_tf.scale.set(0.4, 0.4);
		lTimeValue_tf.position.x = lIsPortraitMode_bi ? (APP.isMobile ? 77 : 77) : 76;
		lTimeValue_tf.position.y = lIsPortraitMode_bi ? 71 : 55;
		//...TIME INDICATOR

		this._updateBaseViews();
	}

	_updateBaseViews()
	{
		let lIsPortraitMode_bi = this._fIsPortraitMode_bl;
		
		let lShort_gr = this._fShortBase_gr;
		let lLong_gr = this._fLongBase_gr;
		
		lShort_gr.cacheAsBitmap = false;
		lLong_gr.cacheAsBitmap = false;

		lShort_gr.clear();
		lLong_gr.clear();

		if (lIsPortraitMode_bi)
		{
			lShort_gr.beginFill(0x111421, 1).drawRect(2, 318, 250, 151).endFill();
			lLong_gr.beginFill(0x111421, 1).drawRect(2, 235, 250, 397).endFill();
		}
		else
		{
			lShort_gr.beginFill(0x111421, 1).drawRect(2, 318, 250, 86).endFill();
			lLong_gr.beginFill(0x111421, 1).drawRect(2, 235, 250, 167).endFill();
		}
	}

	_updateDistanceValue(aDistanceValue_str)
	{
		this._fDistanceValue_tf.write(aDistanceValue_str);
	}

	_updateTimeValue(aTimeValue_num)
	{
		let lTimeValue_str = this._formatTimeValue(aTimeValue_num);
		this._fTimeValue_tf.write(lTimeValue_str + 's');
	}

	_formatTimeValue(aTime_num)
	{
		aTime_num = +aTime_num;
		if (isNaN(aTime_num) || aTime_num < 0)
		{
			aTime_num = 0;
		}

		let lMillisecondsCount_int = Math.floor((aTime_num % 1000) / 100);
		let lSecondsCount_int = Math.floor(aTime_num / 1000);

		lMillisecondsCount_int = (lMillisecondsCount_int < 10) ? lMillisecondsCount_int + "0" : lMillisecondsCount_int;

		let lFormattedTime_str = `${lSecondsCount_int}.${lMillisecondsCount_int}`;
		return lFormattedTime_str;
	}	

	_getMeasurementTextFormat()
    {
        return {
            fontFamily: "fnt_nm_roboto_medium",
            fontSize: 18,
            fill: 0xffffff
        };
    }


    // CANCEL BET DELAY...
    get _isCancelBetDelayInPeriod()
	{
		let lLastBetRequestSendTime = this._lastBetRequestSendTime;

		return lLastBetRequestSendTime !== undefined
				&& ((Date.now() - lLastBetRequestSendTime) < CANCEL_BET_DELAY_IN_MS);
	}

	get _lastBetRequestSendTime()
	{
		return APP.webSocketInteractionController.getLastRequestSendTime(CLIENT_MESSAGES.CRASH_BET);
	}

	_startCancelBetDelayTimerIfRequired()
	{
		let lIsCancelBetDelayInPeriod_bl = this._isCancelBetDelayInPeriod;

		if (lIsCancelBetDelayInPeriod_bl && !this._isCancelBetDelayTimerInProgress)
		{
			let lDelay_int = Date.now() - this._lastBetRequestSendTime;
			if (lDelay_int > 0)
			{
				this._startCancelBetDelayTimer(lDelay_int);
			}
		}
	}

    _startCancelBetDelayTimer(aDuration_int)
    {
    	this._fCancelBetDelayTimer_t = new Timer(() => {
    														this._destroyCancelBetDelayTimer();
														},
													aDuration_int);
    }

    get _isCancelBetDelayTimerInProgress()
    {
    	return this._fCancelBetDelayTimer_t && this._fCancelBetDelayTimer_t.isInProgress;
    }

    _destroyCancelBetDelayTimer()
    {
    	this._fCancelBetDelayTimer_t && this._fCancelBetDelayTimer_t.destructor()
    	this._fCancelBetDelayTimer_t;
    }
    // ...CANCEL BET DELAY
}

export default BattlegroundPlaceBetsView;