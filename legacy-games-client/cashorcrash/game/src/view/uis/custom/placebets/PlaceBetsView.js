import PlaceBetsBaseView from './PlaceBetsBaseView';
import AstronautPanelView from './AstronautPanelView';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { MAX_MASTER_BETS_AMOUNT } from '../../../../model/gameplay/bets/BetsInfo';
import GameButton from '../../../../ui/GameButton';
import Button from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';

class PlaceBetsView extends PlaceBetsBaseView
{	
	static get AUSTRONAUT_PANELS_COUNT ()          		{ return MAX_MASTER_BETS_AMOUNT; }

	static get EVENT_ON_PLACE_BETS ()					{ return PlaceBetsBaseView.EVENT_ON_PLACE_BETS; }
	static get EVENT_ON_CANCEL_BET ()					{ return PlaceBetsBaseView.EVENT_ON_CANCEL_BET; }
	static get EVENT_ON_EJECT_INITIATED()				{ return AstronautPanelView.EVENT_ON_EJECT_INITIATED; };
	static get EVENT_ON_CANCEL_AUTO_EJECT_INITIATED()	{ return AstronautPanelView.EVENT_ON_CANCEL_AUTO_EJECT_INITIATED; };
	static get EVENT_ON_EDIT_AUTO_EJECT_INITIATED()		{ return AstronautPanelView.EVENT_ON_EDIT_AUTO_EJECT_INITIATED; };

	
	updateBetLimits()
	{
		for (let i = 0; i < PlaceBetsView.AUSTRONAUT_PANELS_COUNT; i++)
		{
			this._getAstronautPanelView(i).updateBetLimits();
		}
	}

	updateMasterBets()
	{
		for (let i = 0; i < PlaceBetsView.AUSTRONAUT_PANELS_COUNT; i++)
		{
			this._getAstronautPanelView(i).updateMasterBet();
		}
	}

	updateMasterBet(aBetIndex_int)
	{
		this._getAstronautPanelView(aBetIndex_int).updateMasterBet();
	}

	//INIT...
	constructor()
	{
		super();

		this._fAstronautPanelsContainer_sprt = null;
		this._fAstronautPanel_apv_arr = null;
		
		this._FRepeatBetsButtonBase_gr = null;
	}
	
	__init()
	{
		super.__init();
		
		let l_gpc = APP.gameController.gameplayController;
		this._fGameplayInfo_gpi = l_gpc.info;
		this._fGamePlayersController_gpsc = l_gpc.gamePlayersController;
		this._fBetsInfo_bsi = this._fGamePlayersController_gpsc.info.betsInfo;
		let lIsBlastMode_bl = APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode;
		
		this._fAstronautPanel_apv_arr = [];
		this._fAstronautPanelsContainer_sprt = this.addChild(new Sprite());
		
		this._fAstronautPanelsContainer_sprt.position.set(0, 0);
		this._fAstronautPanelsContainer_sprt.zIndex = 3;

		for (let i=0; i<PlaceBetsView.AUSTRONAUT_PANELS_COUNT; i++)
		{
			this._getAstronautPanelView(i);
		}

		let lRepeatBetsButtonBase_gr = new PIXI.Graphics();
		let lPlaceBetCaptionName_str = lIsBlastMode_bl ? "TARepeatAllBids" : "TARepeatAllBets";
		let lRepeatBetsButton_gb = this._fRepeatBetsButton_gb = this.addChild(new GameButton(lRepeatBetsButtonBase_gr, lPlaceBetCaptionName_str, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lRepeatBetsButton_gb.on("pointerclick", this._onButtonRepeatAllBetsClick, this);

		this._updateLayoutSettings();
	}
	//...INIT

	_updateLayoutSettings()
	{
		super._updateLayoutSettings();

		if (this._fContentWidth_num === undefined || this._fContentHeight_num === undefined) return;

		let lTopOffset_num = this._fIsPortraitMode_bl ? 0 : 0;
		let lPlaceBetOffsetY_num = this._fIsPortraitMode_bl ? (APP.isMobile ? 1 : 2) : 1;
		let lInternalContentWidth_num = this._fContentWidth_num;
		let lIsBlastMode_bl = APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode;
				
		for (let i = 0; i < PlaceBetsView.AUSTRONAUT_PANELS_COUNT; i++)
		{
			let lAstronautPanelView_bsv = this._getAstronautPanelView(i);
			lAstronautPanelView_bsv.updateLayout(lInternalContentWidth_num);
			lAstronautPanelView_bsv.position.y = i*(AstronautPanelView.PANEL_HEIGHT + lPlaceBetOffsetY_num) + lTopOffset_num;
		}

		let lBetsBottomY_num = PlaceBetsView.AUSTRONAUT_PANELS_COUNT*(AstronautPanelView.PANEL_HEIGHT + lPlaceBetOffsetY_num) + lTopOffset_num;

		let lRepeatBetsButtonBaseWidth_num = this._getAstronautPanelView(0).rowWidth;
		if (this._fIsPortraitMode_bl)
		{
			lRepeatBetsButtonBaseWidth_num *= 0.5;
			lRepeatBetsButtonBaseWidth_num -= 5*2;
		}
		let lRepeatBetsButtonBaseHeight_num = this._fIsPortraitMode_bl ? 40 : AstronautPanelView.ROW_HEIGHT;
		let lRepeatBetsButtonX_num = this._fIsPortraitMode_bl ? lInternalContentWidth_num - AstronautPanelView.PADDING_RIGHT - lRepeatBetsButtonBaseWidth_num/2 - 5 : AstronautPanelView.PADDING_LEFT + lRepeatBetsButtonBaseWidth_num/2;
		let lRepeatBetsButtonY_num = lBetsBottomY_num + lRepeatBetsButtonBaseHeight_num/2;
		let lRepeatBetsCaptionName_str = lIsBlastMode_bl ? "TARepeatAllBids" : "TARepeatAllBets";
		if (this._fIsPortraitMode_bl)
		{
			lRepeatBetsCaptionName_str += "Portrait";
		}
		let lRepeatBetsButtonBase_gr = this._FRepeatBetsButtonBase_gr =  new PIXI.Graphics().beginFill(0x1b941d).drawRoundedRect(-lRepeatBetsButtonBaseWidth_num/2, -lRepeatBetsButtonBaseHeight_num/2, lRepeatBetsButtonBaseWidth_num, lRepeatBetsButtonBaseHeight_num, AstronautPanelView.ROUND_RECT_RADIUS).endFill();
		let lRepeatBetsButton_gb = this._fRepeatBetsButton_gb;
		lRepeatBetsButton_gb.updateBase(lRepeatBetsButtonBase_gr);
		lRepeatBetsButton_gb.captionId = lRepeatBetsCaptionName_str;
		lRepeatBetsButton_gb.position.set(lRepeatBetsButtonX_num, lRepeatBetsButtonY_num);
	}

	_getAstronautPanelView(aIndex_int)
	{
		if (!this._fAstronautPanel_apv_arr[aIndex_int])
		{
			let lAstronautPanelView_bsv = this._fAstronautPanelsContainer_sprt.addChild(new AstronautPanelView(aIndex_int, this._fContentWidth_num));
			lAstronautPanelView_bsv.on(AstronautPanelView.EVENT_ON_PLACE_BET_BUTTON_CLICKED, this._onPlaceBetClicked, this);
			lAstronautPanelView_bsv.on(AstronautPanelView.EVENT_ON_CANCEL_BET_BUTTON_CLICKED, this._onCancelBetClicked, this);
			lAstronautPanelView_bsv.on(AstronautPanelView.EVENT_ON_EJECT_INITIATED, this.emit, this);
			lAstronautPanelView_bsv.on(AstronautPanelView.EVENT_ON_CANCEL_AUTO_EJECT_INITIATED, this.emit, this);
			lAstronautPanelView_bsv.on(AstronautPanelView.EVENT_ON_EDIT_AUTO_EJECT_INITIATED, this.emit, this);
			
			lAstronautPanelView_bsv.zIndex = PlaceBetsView.AUSTRONAUT_PANELS_COUNT-aIndex_int;
			this._fAstronautPanel_apv_arr[aIndex_int] = lAstronautPanelView_bsv;
		}
		return this._fAstronautPanel_apv_arr[aIndex_int];
	}

	_setCurrencySymbols()
	{
		for (let i = 0; i < PlaceBetsView.AUSTRONAUT_PANELS_COUNT; i++)
		{
			this._getAstronautPanelView(i)._setCurrencySymbol();
		}
	}

	validate()
	{
		let l_gpi = this._fGameplayInfo_gpi;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lBetsInfo_bsi = this._fBetsInfo_bsi;
		let lMasterSeatId_num = lBetsInfo_bsi.gamePlayersInfo.masterSeatId;

		let lIsAnyAstronautRepeatBetEnabled_bl = false;
		for (let i = PlaceBetsView.AUSTRONAUT_PANELS_COUNT - 1; i >= 0; i--)
		{
			let lAstronautPanelView_apv = this._getAstronautPanelView(i);
			lAstronautPanelView_apv.validate();

			let lRepeatBetInfo_rbi = lBetsInfo_bsi.getLastConfirmedBet(lAstronautPanelView_apv.betIndex);
			let lActiveBetInfo_bi = lBetsInfo_bsi.getMasterBetInfoByIndex(lAstronautPanelView_apv.betIndex, true);

			if (!!lRepeatBetInfo_rbi && !lActiveBetInfo_bi)
			{
				lIsAnyAstronautRepeatBetEnabled_bl = true;
			}
		}

		this._fRepeatBetsButton_gb.enabled = lRoundInfo_ri.isRoundWaitState && lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined
											&& lIsAnyAstronautRepeatBetEnabled_bl && !lRoundInfo_ri.lastSecondsStarted;

		
		this._FRepeatBetsButtonBase_gr.visible = this._fRepeatBetsButton_gb.enabled;
	}

	_onPlaceBetClicked(event)
	{
		let lTargetAstronautView_apv = event.target;

		let lBetIndex_num = lTargetAstronautView_apv.betIndex;
		let lBetValue_num = lTargetAstronautView_apv.betValue;
		let lBetAutoEjectMult_num = lTargetAstronautView_apv.autoEjectMultiplier;

		this._emitPlaceBets([{betIndex: lBetIndex_num, betValue: lBetValue_num, autoEjectMultipleier: lBetAutoEjectMult_num}]);
	}

	_onCancelBetClicked(event)
	{
		let lTargetAstronautView_apv = event.target;	

		let lBetIndex_num = lTargetAstronautView_apv.betIndex;
		
		this.emit(PlaceBetsView.EVENT_ON_CANCEL_BET, {betIndex: lBetIndex_num});
	}

	_onButtonRepeatAllBetsClick(event)
	{
		let l_gpi = this._fGameplayInfo_gpi;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lBetsInfo_bsi = this._fBetsInfo_bsi;
		let lMasterSeatId_num = lBetsInfo_bsi.gamePlayersInfo.masterSeatId;

		let lIsAnyAstronautRepeatBetEnabled_bl = false;
		let lBets_arr = [];
		for (let i=0; i<PlaceBetsView.AUSTRONAUT_PANELS_COUNT; i++)
		{
			let lAstronautPanelView_apv = this._getAstronautPanelView(i);
			let lBetIndex_num = lAstronautPanelView_apv.betIndex;

			let lRepeatBetInfo_rbi = lBetsInfo_bsi.getLastConfirmedBet(lBetIndex_num);
			let lActiveBetInfo_bi = lBetsInfo_bsi.getMasterBetInfoByIndex(lBetIndex_num, true);

			if (!!lRepeatBetInfo_rbi && !lActiveBetInfo_bi)
			{
				lAstronautPanelView_apv.setValues(lRepeatBetInfo_rbi.autoEjectMultiplier, lRepeatBetInfo_rbi.betAmount);
				lBets_arr.push( {betIndex: lBetIndex_num, betValue: lRepeatBetInfo_rbi.betAmount, autoEjectMultipleier: lRepeatBetInfo_rbi.autoEjectMultiplier} );
			}
		}

		if (!!lBets_arr.length)
		{
			this._emitPlaceBets(lBets_arr);
		}
	}

	_emitPlaceBets(aBets_arr)
	{
		if (!!aBets_arr && !!aBets_arr.length)
		{
			this.emit(PlaceBetsView.EVENT_ON_PLACE_BETS, {bets: aBets_arr});
		}
	}

	confirmCancelAutoEject(aAstronautIndex_int, aCancelType_str)
	{
		this._getAstronautPanelView(aAstronautIndex_int).confirmCancelAutoEject(aCancelType_str);
	}

	confirmEditAutoEject(aAstronautIndex_int)
	{
		this._getAstronautPanelView(aAstronautIndex_int).confirmEditAutoEject();
	}

	handleDeniedAutoEject(aAstronautIndex_int, aAutoEjectMultValue_num)
	{
		this._getAstronautPanelView(aAstronautIndex_int).handleDeniedAutoEject(aAutoEjectMultValue_num);
	}

	applyActualAutoEjectMultiplier(aAstronautIndex_int)
	{
		this._getAstronautPanelView(aAstronautIndex_int).applyActualAutoEjectMultiplier();
	}
}

export default PlaceBetsView;