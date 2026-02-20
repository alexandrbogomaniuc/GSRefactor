import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import BetEdit from './BetEdit';
import MultiplyerEditView from './MultiplyerEditView';
import GameButton from '../../../../ui/GameButton';
import Button from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import { DecimalPartValidator } from '../../../../ui/BetInputField';

const AUTO_EJECT_CANCEL_TYPES = 
{
	OFF: "OFF",
	RESET: "RESET"
}

const RECT_RADIUS = 5;

class AstronautPanelView extends Sprite
{
	static get EVENT_ON_PLACE_BET_BUTTON_CLICKED ()				{ return "EVENT_ON_PLACE_BET_BUTTON_CLICKED"; }
	static get EVENT_ON_CANCEL_BET_BUTTON_CLICKED ()			{ return "EVENT_ON_CANCEL_BET_BUTTON_CLICKED"; }
	static get EVENT_ON_EJECT_INITIATED()						{ return 'EVENT_ON_EJECT_INITIATED'; }
	static get EVENT_ON_CANCEL_AUTO_EJECT_INITIATED()			{ return 'EVENT_ON_CANCEL_AUTO_EJECT_INITIATED'; }
	static get EVENT_ON_EDIT_AUTO_EJECT_INITIATED()				{ return 'EVENT_ON_EDIT_AUTO_EJECT_INITIATED'; }

	static get ROUND_RECT_RADIUS ()
	{
		return RECT_RADIUS;
	}

	constructor(aBetDescriptorIndex_int, aLayoutWidth_num)
	{
		super();

		this._fBetDescriptorIndex_int = aBetDescriptorIndex_int;
		this._fLayoutWidth_num = aLayoutWidth_num;

		this._fEditBetButton_gb = null;
		this._fPlaceBetButton_gb = null;
		this._fHalfBetButton_gb = null;
		this._fDoubleBetButton_gb = null;
		this._fMaxBetButton_gb = null;
		this._fDisabledBase_gr_arr = null;
		this._fBase_gr_arr = null;
		this._fPanelBase_gr = null;

		this._fAutoEjectCaption_ta = null;
		this._fAutoEjectResetButton_gb = null;
		this._fAutoEjectEnabled_bl = true;
		
		this._fBetEditView_bev = null;
		this._fContentContainer_sprt = null;

		this._fGameplayInfo_gpi = null;
		this._fBetsInfo_bsi = null;
		this._fBetsController_bsc = null;

		this.__init();
	}

	get betIndex()
	{
		return this._fBetDescriptorIndex_int;
	}

	get betValue()
	{
		return this._fBetEditView_bev.currentAppliedBetValue;
	}

	get autoEjectMultiplier()
	{
		return this._fAutoEjectEnabled_bl ? this._fAutoEjectMultiplierEdit_mev.multiplierValue : undefined;
	}

	updateBetLimits()
	{
		this._fBetEditView_bev.updateBetLimits();
	}

	updateLayout(aLayoutWidth_num)
	{
		this._fLayoutWidth_num = aLayoutWidth_num;

		this._updateLayoutBounds();

		this._updateLayoutSettings();
	}

	get rowWidth()
	{
		return this._rowWidth;
	}

	updateMasterBet()
	{
		let lBetInfo_bi = this._currentBetInfo;
		if (!lBetInfo_bi)
		{
			return;
		}

		this._fAutoEjectMultiplierEdit_mev.multiplierValue = lBetInfo_bi.isAutoEject ? lBetInfo_bi.autoEjectMultiplier : 0;
		this._fAutoEjectMultiplierEdit_mev.unfocus();

		this._fBetEditView_bev.setBetValue(lBetInfo_bi.betAmount);
		this._fBetEditView_bev.unfocus();
	}

	setValues(aMultiplierValue_num, aBetAmount_num)
	{
		this._fAutoEjectMultiplierEdit_mev.unfocus();
		this._fAutoEjectMultiplierEdit_mev.multiplierValue = aMultiplierValue_num || 0;

		this._fBetEditView_bev.unfocus();
		this._fBetEditView_bev.setBetValue(aBetAmount_num);
	}

	confirmCancelAutoEject(aCancelType_str)
	{
	}

	confirmEditAutoEject()
	{
		let lBetInfo_bi = this._currentBetInfo;
		if (!!lBetInfo_bi && lBetInfo_bi.autoEjectMultiplier !== this.autoEjectMultiplier)
		{
			if (this.autoEjectMultiplier === undefined)
			{
				this._startCancelAutoEject(AUTO_EJECT_CANCEL_TYPES.RESET);
			}
			else if (lBetInfo_bi.isAutoEject)
			{
				this._startEditAutoEject();
			}
		}
	}

	handleDeniedAutoEject(aAutoEjectMultValue_num)
	{
		this._fAutoEjectMultiplierEdit_mev.handleDeniedAutoEject(aAutoEjectMultValue_num);
	}

	applyActualAutoEjectMultiplier()
	{
		this._fAutoEjectMultiplierEdit_mev.unfocus();

		let lBetInfo_bi = this._currentBetInfo;
		if (lBetInfo_bi)
		{
			this._fAutoEjectMultiplierEdit_mev.multiplierValue = lBetInfo_bi.isAutoEject ? lBetInfo_bi.autoEjectMultiplier : 0;
		}
	}

	__init()
	{
		let lIsBlastMode_bl = APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode;

		this._updateLayoutBounds();

		let l_gpc = APP.gameController.gameplayController;
		this._fGameplayInfo_gpi = l_gpc.info;
		this._fGamePlayersController_gpsc = l_gpc.gamePlayersController;
		this._fBetsController_bsc = this._fGamePlayersController_gpsc.betsController;
		this._fBetsInfo_bsi = this._fBetsController_bsc.info;

		this._fDisabledBase_gr_arr = [];
		this._fBase_gr_arr = [];

		this._fBaseContainer_sprt = this.addChild(new Sprite);

		let lContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);
		lContainer_sprt.position.set(AstronautPanelView.PADDING_LEFT, 0);
		
		let lEditBetButtonBase_gr = new PIXI.Graphics();
		let lEditBetAssetName_str = lIsBlastMode_bl ? "TAEditBid" : "TAEditBet";
		let lEditBetButton_gb = this._fEditBetButton_gb = lContainer_sprt.addChild(new GameButton(lEditBetButtonBase_gr, lEditBetAssetName_str, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lEditBetButton_gb.specifiedSoundName = "gui_button_cancel";
		lEditBetButton_gb.on("pointerclick", this._onButtonCancelBetClick, this);

		let lPlaceBetButtonBase_gr = new PIXI.Graphics();
		let lPlaceBetCaptionName_str = lIsBlastMode_bl ? "TAPlaceBid" : "TAPlaceBet";
		let lPlaceBetButton_gb = this._fPlaceBetButton_gb = lContainer_sprt.addChild(new GameButton(lPlaceBetButtonBase_gr, lPlaceBetCaptionName_str, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lPlaceBetButton_gb.on("pointerclick", this._onButtonPlaceBetClick, this);

		let lHalfBetBase_gr = new PIXI.Graphics();
		let lHalfBet_gb = this._fHalfBetButton_gb = lContainer_sprt.addChild(new GameButton(lHalfBetBase_gr, "TAHalfBet", true));
		lHalfBet_gb.grayFilter.greyscale(0.4, false);
		lHalfBet_gb.on("pointerclick", this._onButtonBetHalfClick, this);

		let lDoubleBetBase_gr = new PIXI.Graphics();
		let lDoubleBet_gb = this._fDoubleBetButton_gb = lContainer_sprt.addChild(new GameButton(lDoubleBetBase_gr, "TADoubleBet", true));
		lDoubleBet_gb.grayFilter.greyscale(0.4, false);
		lDoubleBet_gb.on("pointerclick", this._onButtonBetDoubleClick, this);

		let lMaxBetBase_gr = new PIXI.Graphics();
		let lMaxBet_gb = this._fMaxBetButton_gb = lContainer_sprt.addChild(new GameButton(lMaxBetBase_gr, "TAMaxBet", true));
		lMaxBet_gb.grayFilter.greyscale(0.4, false);
		lMaxBet_gb.on("pointerclick", this._onButtonBetMaxClick, this);
	
		let lBetAmountCaptionName_str = lIsBlastMode_bl ? "TABidAmount" : "TABetAmount";
        this._fBetAmountCaption_ta = lContainer_sprt.addChild(I18.generateNewCTranslatableAsset(lBetAmountCaptionName_str));
		this._fBetEditView_bev = lContainer_sprt.addChild(new BetEdit());

		this._fAutoEjectCaption_ta = lContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAAutoCashout"));
		this._fAutoEjectMultiplierEdit_mev = lContainer_sprt.addChild(new MultiplyerEditView(this._rowWidth));
		this._fAutoEjectMultiplierEdit_mev.on(MultiplyerEditView.EVENT_ON_MULTIPLIER_VALUE_RESET, this._onAutoEjectMultiplierValueReset, this);
		this._fAutoEjectMultiplierEdit_mev.on(MultiplyerEditView.EVENT_ON_MULTIPLIER_VALUE_UPDATED, this._onAutoEjectMultiplierValueUpdated, this);
		
		this._fInRocketPanelContainer_irpc = lContainer_sprt.addChild(new Sprite);
		this._fInRocketPanelBase_gr = this._fInRocketPanelContainer_irpc.addChild( new PIXI.Graphics() );
		
		let lInRocket_ta = this._fInRocket_ta = this._fInRocketPanelContainer_irpc.addChild(I18.generateNewCTranslatableAsset("TAInRocket"));

		let lCancelAutoEjectButtonBase_gr = new PIXI.Graphics();
		let lCancelAutoEjectButton_gb = this._fCancelAutoEjectButton_gb = this._fInRocketPanelContainer_irpc.addChild(new GameButton(lCancelAutoEjectButtonBase_gr, "TACancelAutoEject", true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lCancelAutoEjectButton_gb.grayFilter.greyscale(0.35, false);
		lCancelAutoEjectButton_gb.on("pointerclick", this._onCancelAutoEjectPressed, this);
		
		let lEjectButtonBase_gr = new PIXI.Graphics();
		let lEjectButton_gb = this._fEjectButton_gb = this._fInRocketPanelContainer_irpc.addChild(new GameButton(lEjectButtonBase_gr, "TAEjectButton", true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lEjectButton_gb.on("pointerclick", this._onEjectButtonPressed, this);

		this._updateLayoutSettings();
	}

	_updateLayoutBounds()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		AstronautPanelView.ROWS_AMOUNT = lIsPortraitMode_bl ? 2 : 3;
		AstronautPanelView.PADDING_LEFT = 4;
		AstronautPanelView.PADDING_RIGHT = 4;
		AstronautPanelView.PANEL_HEIGHT = lIsPortraitMode_bl ? (APP.isMobile ? 102 : 106) : 105;
		AstronautPanelView.ROWS_OFFSET = lIsPortraitMode_bl ? (APP.isMobile ? 2 : 2) : 2;
		AstronautPanelView.ROW_HEIGHT = lIsPortraitMode_bl ? (APP.isMobile ? 48 : 50) : 33;
	}

	get _rowWidth()
	{
		return this._fLayoutWidth_num-AstronautPanelView.PADDING_LEFT-AstronautPanelView.PADDING_RIGHT;
	}

	_updateLayoutSettings()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;
		let lRowContentOffsetX_num = lIsPortraitMode_bl ? 5 : 3;
		let lIsBlastMode_bl = APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode;

		//DEBUG...
		//...DEBUG

		let lRowWidth_num = this._rowWidth;
		let lRowHeight_num = AstronautPanelView.ROW_HEIGHT;

		this._fPanelBase_gr = this._fPanelBase_gr || this._fBaseContainer_sprt.addChild(new PIXI.Graphics);
		this._fPanelBase_gr.clear();
		if (lIsPortraitMode_bl)
		{
			this._fPanelBase_gr.beginFill(0x4f627f, 1).drawRoundedRect(AstronautPanelView.PADDING_LEFT, AstronautPanelView.PANEL_HEIGHT-6, lRowWidth_num, 3, RECT_RADIUS).endFill();
			this._fPanelBase_gr.beginFill(0x212741, 0.5).drawRoundedRect(AstronautPanelView.PADDING_LEFT, 0, lRowWidth_num, AstronautPanelView.PANEL_HEIGHT-5, RECT_RADIUS).endFill();

			// this._fPanelBase_gr.beginFill(0xffff00, 1).drawRoundedRect(AstronautPanelView.PADDING_LEFT, 0, lRowWidth_num, lRowHeight_num, RECT_RADIUS).endFill();
			// this._fPanelBase_gr.beginFill(0xffff00, 1).drawRoundedRect(AstronautPanelView.PADDING_LEFT, lRowHeight_num+AstronautPanelView.ROWS_OFFSET, lRowWidth_num, lRowHeight_num, RECT_RADIUS).endFill();
		}

		for (let i=0; i<AstronautPanelView.ROWS_AMOUNT-1; i++)
		{
			if (!this._fBase_gr_arr[i])
			{
				this._fDisabledBase_gr_arr[i] = this._fBaseContainer_sprt.addChild(new PIXI.Graphics);
				this._fBase_gr_arr[i] = this._fBaseContainer_sprt.addChild(new PIXI.Graphics);
			}

			this._fDisabledBase_gr_arr[i].clear();
			this._fBase_gr_arr[i].clear();

			let lRowX_num = AstronautPanelView.PADDING_LEFT;
			let lRowY_num = i*(AstronautPanelView.ROW_HEIGHT+AstronautPanelView.ROWS_OFFSET);
			let lRowBaseColor_int = lIsPortraitMode_bl ? 0x212741 : 0x111421;
			let lRowBaseAlpha_num = lIsPortraitMode_bl ? 0.5 : 1;
			let lRowBaseWidth_num = lIsPortraitMode_bl ? lRowWidth_num/2 : lRowWidth_num;

			if (!lIsPortraitMode_bl)
			{
				this._fDisabledBase_gr_arr[i].beginFill(0x212741, 0.5).drawRoundedRect(lRowX_num, lRowY_num, lRowBaseWidth_num, lRowHeight_num, RECT_RADIUS).endFill();
			}
			this._fBase_gr_arr[i].beginFill(lRowBaseColor_int, lRowBaseAlpha_num).drawRoundedRect(lRowX_num, lRowY_num, lRowBaseWidth_num, lRowHeight_num, RECT_RADIUS).endFill();
			
			this._fDisabledBase_gr_arr[i].visible = false;
		}

		for (let i=AstronautPanelView.ROWS_AMOUNT-1; i<this._fBase_gr_arr.length; i++)
		{
			this._fDisabledBase_gr_arr[i].visible = false;
			this._fBase_gr_arr[i].visible = false;
		}

		let lMaxButtonBaseWidth_num = lIsPortraitMode_bl ? 54 : 36;
		let lMaxButtonBaseHeight_num = lIsPortraitMode_bl ? 40 : 26;
		let lMaxButtonX_num = lRowWidth_num - lRowContentOffsetX_num - lMaxButtonBaseWidth_num/2;
		let lMaxButtonY_num = AstronautPanelView.ROW_HEIGHT/2;
		let lMaxButtonBase_gr = new PIXI.Graphics().beginFill(0x242b44, 1).drawRoundedRect(-lMaxButtonBaseWidth_num/2, -lMaxButtonBaseHeight_num/2, lMaxButtonBaseWidth_num, lMaxButtonBaseHeight_num, RECT_RADIUS).endFill();
		let lMaxButton_gb = this._fMaxBetButton_gb;
		lMaxButton_gb.updateBase(lMaxButtonBase_gr);
		let lMaxButtonCaption_ta = I18.generateNewCTranslatableAsset(lIsPortraitMode_bl ? "TAMaxBetPortrait" : "TAMaxBet");
		lMaxButtonCaption_ta.scale.y = lIsPortraitMode_bl ? 1.2 : 1;
		lMaxButton_gb.updateCaptionView(lMaxButtonCaption_ta);
		lMaxButton_gb.position.set(lMaxButtonX_num, lMaxButtonY_num);

		let lDoubleButtonBaseWidth_num = lIsPortraitMode_bl ? 45 : 26;
		let lDoubleButtonBaseHeight_num = lMaxButtonBaseHeight_num;
		let lDoubleButtonX_num = lMaxButtonX_num - lMaxButtonBaseWidth_num/2 - lRowContentOffsetX_num - lDoubleButtonBaseWidth_num/2;
		let lDoubleButtonY_num = lMaxButtonY_num;
		let lDoubleButtonBase_gr = new PIXI.Graphics().beginFill(0x242b44, 1).drawRoundedRect(-lDoubleButtonBaseWidth_num/2, -lDoubleButtonBaseHeight_num/2, lDoubleButtonBaseWidth_num, lDoubleButtonBaseHeight_num, RECT_RADIUS).endFill();
		let lDoubleButton_gb = this._fDoubleBetButton_gb;
		lDoubleButton_gb.updateBase(lDoubleButtonBase_gr);
		lDoubleButton_gb.captionId = lIsPortraitMode_bl ? "TADoubleBetPortrait" : "TADoubleBet";
		lDoubleButton_gb.position.set(lDoubleButtonX_num, lDoubleButtonY_num);

		let lHalfButtonBaseWidth_num = lIsPortraitMode_bl ? 45 : 26;
		let lHalfButtonBaseHeight_num = lMaxButtonBaseHeight_num;
		let lHalfButtonX_num = lDoubleButtonX_num - lDoubleButtonBaseWidth_num/2 - lRowContentOffsetX_num - lHalfButtonBaseWidth_num/2;
		let lHalfButtonY_num = lMaxButtonY_num;
		let lHalfButtonBase_gr = new PIXI.Graphics().beginFill(0x242b44, 1).drawRoundedRect(-lHalfButtonBaseWidth_num/2, -lHalfButtonBaseHeight_num/2, lHalfButtonBaseWidth_num, lHalfButtonBaseHeight_num, RECT_RADIUS).endFill();
		let lHalfButton_gb = this._fHalfBetButton_gb;
		lHalfButton_gb.updateBase(lHalfButtonBase_gr);
		lHalfButton_gb.captionId = lIsPortraitMode_bl ? "TAHalfBetPortrait" : "TAHalfBet";
		lHalfButton_gb.position.set(lHalfButtonX_num, lHalfButtonY_num);
		
		let lPlaceBetButtonX_num = lIsPortraitMode_bl ? lRowWidth_num*3/4 : lRowWidth_num/2;
		let lPlaceBetButtonRowY_num = (AstronautPanelView.ROW_HEIGHT+AstronautPanelView.ROWS_OFFSET);
		if (!lIsPortraitMode_bl)
		{
			lPlaceBetButtonRowY_num *= 2;
		}
		let lPlaceBetButtonY_num = lPlaceBetButtonRowY_num + AstronautPanelView.ROW_HEIGHT/2;
		let lPlaceBetButtonBaseWidth_num = lIsPortraitMode_bl ? lRowWidth_num/2-lRowContentOffsetX_num*2 : lRowWidth_num;
		let lPlaceBetButtonBaseHeight_num = lIsPortraitMode_bl ? 40 : lRowHeight_num;
		let lPlaceBetCaptionName_str = lIsBlastMode_bl ? "TAPlaceBid" : "TAPlaceBet";
		if (lIsPortraitMode_bl)
		{
			lPlaceBetCaptionName_str += "Portrait";
		}
		let lPlaceBetButtonBase_gr = new PIXI.Graphics().beginFill(0xefc033).drawRoundedRect(-lPlaceBetButtonBaseWidth_num/2, -lPlaceBetButtonBaseHeight_num/2, lPlaceBetButtonBaseWidth_num, lPlaceBetButtonBaseHeight_num, RECT_RADIUS).endFill();
		let lPlaceBetButton_gb = this._fPlaceBetButton_gb;
		lPlaceBetButton_gb.updateBase(lPlaceBetButtonBase_gr);
		lPlaceBetButton_gb.captionId = lPlaceBetCaptionName_str;
		lPlaceBetButton_gb.position.set(lPlaceBetButtonX_num, lPlaceBetButtonY_num);

		let lEditBetButtonX_num = lPlaceBetButtonX_num;
		let lEditBetButtonY_num = lPlaceBetButtonY_num;
		let lEditBetButtonBaseWidth_num = lPlaceBetButtonBaseWidth_num;
		let lEditBetButtonBaseHeight_num = lPlaceBetButtonBaseHeight_num;
		let lEditBetAssetName_str = lIsBlastMode_bl ? "TAEditBid" : "TAEditBet";
		if (lIsPortraitMode_bl)
		{
			lEditBetAssetName_str += "Portrait";
		}
		let lEditBetButtonBase_gr = new PIXI.Graphics().beginFill(0xc12d26).drawRoundedRect(-lEditBetButtonBaseWidth_num/2, -lEditBetButtonBaseHeight_num/2, lEditBetButtonBaseWidth_num, lEditBetButtonBaseHeight_num, RECT_RADIUS).endFill();
		let lEditBetButton_gb = this._fEditBetButton_gb;
		lEditBetButton_gb.updateBase(lEditBetButtonBase_gr);
		lEditBetButton_gb.captionId = lEditBetAssetName_str;
		lEditBetButton_gb.position.set(lEditBetButtonX_num, lEditBetButtonY_num);

		let lBetAmountCaptionId_str = lIsBlastMode_bl ? "TABidAmount" : "TABetAmount";
		if (lIsPortraitMode_bl)
		{
			lBetAmountCaptionId_str += "Portrait";
		}
		let lBetAmountCaptionY_num = lIsPortraitMode_bl ? lRowHeight_num/2 : 12;
		this._fBetAmountCaption_ta.position.set(lRowContentOffsetX_num, lBetAmountCaptionY_num);
		this._fBetAmountCaption_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lBetAmountCaptionId_str));

		let lDifferenceBetweenCaptionWidth;
		if (this._fBetAmountCaption_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.width > 60 && lIsPortraitMode_bl)
		{
			lDifferenceBetweenCaptionWidth = this._fBetAmountCaption_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.width - 60;
		}
		else if (this._fBetAmountCaption_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.width > 86 && !lIsPortraitMode_bl )
		{
			lDifferenceBetweenCaptionWidth = this._fBetAmountCaption_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.width - 60;
		}
		else
		{
			lDifferenceBetweenCaptionWidth = 0;
		}

		let lBetEditX_num = lIsPortraitMode_bl ? lRowContentOffsetX_num + this._fBetAmountCaption_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.width + 2 : lRowContentOffsetX_num;

		let lBetEditY_num = lIsPortraitMode_bl ? 16 : 13;
		this._fBetEditView_bev.position.set(lBetEditX_num, lBetEditY_num);

		if (lDifferenceBetweenCaptionWidth !== undefined)
		{
			this._fBetEditView_bev.arrowsXOffset = lDifferenceBetweenCaptionWidth;
		}
		this._fBetEditView_bev.updateLayout();
		

		let lAutoEjectCaptionAssetId_str = lIsPortraitMode_bl ? "TAAutoCashoutPortrait" : "TAAutoCashout";
		let lAutoEjectCaptionY_num = AstronautPanelView.ROW_HEIGHT+AstronautPanelView.ROWS_OFFSET;
		lAutoEjectCaptionY_num += lIsPortraitMode_bl ? 22 : 12;
		this._fAutoEjectCaption_ta.position.set(lRowContentOffsetX_num, lAutoEjectCaptionY_num);
		this._fAutoEjectCaption_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lAutoEjectCaptionAssetId_str));

		let lAutoEjectMultEditY_num = (AstronautPanelView.ROW_HEIGHT+AstronautPanelView.ROWS_OFFSET) + 13;
		if (lIsPortraitMode_bl)
		{
			lAutoEjectMultEditY_num += 12;
		}
  		this._fAutoEjectMultiplierEdit_mev.position.set(lRowContentOffsetX_num, lAutoEjectMultEditY_num);
  		this._fAutoEjectMultiplierEdit_mev.updateLayout(this._rowWidth-lRowContentOffsetX_num);
  
  		// IN ROCKET...
  		let lInRocketPanelBaseWidth_num = this._fLayoutWidth_num - AstronautPanelView.PADDING_LEFT - AstronautPanelView.PADDING_RIGHT-1;
		let lInRocketPanelBaseHeight_num = lIsPortraitMode_bl ? AstronautPanelView.PANEL_HEIGHT-2-3 : AstronautPanelView.PANEL_HEIGHT-2;
  		let lInRocketPanelX_num = lInRocketPanelBaseWidth_num/2;
		let lInRocketPanelY_num = lInRocketPanelBaseHeight_num/2+1;
		let lInRocketPanel_irp = this._fInRocketPanelContainer_irpc;
		lInRocketPanel_irp.position.set(lInRocketPanelX_num, lInRocketPanelY_num);
		this._fInRocketPanelBase_gr.clear().lineStyle(2, 0x4f627f, 1).beginFill(0x111421, 1).drawRoundedRect(-lInRocketPanelBaseWidth_num/2, -lInRocketPanelBaseHeight_num/2, lInRocketPanelBaseWidth_num, lInRocketPanelBaseHeight_num, RECT_RADIUS).endFill();

		let lInRocket_ta = this._fInRocket_ta;
		let lInRocketAssetId_str = lIsPortraitMode_bl ? "TAInRocketPortrait" : "TAInRocket";
		lInRocket_ta.position.set(0, -lInRocketPanelBaseHeight_num/2+lInRocket_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.height/2 + 8);
		lInRocket_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lInRocketAssetId_str));
		
		let lCancelAutoEjectButtonWidth_num = lIsPortraitMode_bl ? 240 : 110;
		let lCancelAutoEjectButtonHeight_num = lIsPortraitMode_bl ? 50 : 56;
		let lCancelAutoEjectButtonBase_gr = new PIXI.Graphics().beginFill(0xc12d26).drawRoundedRect(-lCancelAutoEjectButtonWidth_num/2, -lCancelAutoEjectButtonHeight_num/2, lCancelAutoEjectButtonWidth_num, lCancelAutoEjectButtonHeight_num, RECT_RADIUS).endFill();
		let lCancelAutoEjectButton_gb = this._fCancelAutoEjectButton_gb;
		lCancelAutoEjectButton_gb.updateBase(lCancelAutoEjectButtonBase_gr);

		let lEjectButtonBase_gr = new PIXI.Graphics().beginFill(0xefc033).drawRoundedRect(-lCancelAutoEjectButtonWidth_num/2, -lCancelAutoEjectButtonHeight_num/2, lCancelAutoEjectButtonWidth_num, lCancelAutoEjectButtonHeight_num, RECT_RADIUS).endFill();
		let lEjectButton_gb = this._fEjectButton_gb;
		lEjectButton_gb.updateBase(lEjectButtonBase_gr);
		lEjectButton_gb.captionId = lIsPortraitMode_bl ? "TAEjectButtonPortrait" : "TAEjectButton";
		
		let lEjectButtonY_num = lInRocketPanelBaseHeight_num/2 - lCancelAutoEjectButtonHeight_num/2 - 6;
		let lEjectButtonX_num = lInRocketPanelBaseWidth_num/4;
		lEjectButton_gb.position.set(lEjectButtonX_num, lEjectButtonY_num);
		
		let lCancelAutoEjectButtonX_num = -lInRocketPanelBaseWidth_num/4;
		lCancelAutoEjectButton_gb.position.set(lCancelAutoEjectButtonX_num, lEjectButtonY_num);
		// ...IN ROCKET
	}

	get _currentBetInfo()
	{
		let lBetInfo_bi = this._fBetsInfo_bsi.getMasterBetInfoByIndex(this.betIndex, true) || null;
		
		return lBetInfo_bi;	
	}

	_onAutoEjectMultiplierValueReset(event)
	{
		let lBetInfo_bi = this._currentBetInfo;
		if (!!lBetInfo_bi && lBetInfo_bi.isAutoEject)
		{
			this._startCancelAutoEject(AUTO_EJECT_CANCEL_TYPES.RESET);
			return;
		}
	}

	_onAutoEjectMultiplierValueUpdated(event)
	{
		let lBetInfo_bi = this._currentBetInfo;
		if (!!lBetInfo_bi && lBetInfo_bi.autoEjectMultiplier !== this.autoEjectMultiplier)
		{
			if (this.autoEjectMultiplier === undefined)
			{
				this._startCancelAutoEject(AUTO_EJECT_CANCEL_TYPES.RESET);
			}
			else
			{
				this._startEditAutoEject();
			}
		}
	}

	_onCancelAutoEjectPressed()
	{
		let lBetInfo_bi = this._currentBetInfo;
		if (!lBetInfo_bi)
		{
			throw new Error('Attempt to cancel Auto-Eject for undefined bet');
			return;
		}

		this._startCancelAutoEject(AUTO_EJECT_CANCEL_TYPES.OFF);
	}

	_onEjectButtonPressed()
	{
		let lBetInfo_bi = this._currentBetInfo;
		if (!lBetInfo_bi)
		{
			throw new Error('Attempt to eject for undefined bet');
			return;
		}
		this.emit(AstronautPanelView.EVENT_ON_EJECT_INITIATED, {betInfo: lBetInfo_bi});
	}

	_startCancelAutoEject(aCancelType_str=AUTO_EJECT_CANCEL_TYPES.OFF)
	{
		let lBetInfo_bi = this._currentBetInfo;
		this.emit(AstronautPanelView.EVENT_ON_CANCEL_AUTO_EJECT_INITIATED, {betInfo: lBetInfo_bi, cancelType: aCancelType_str});
	}

	_startEditAutoEject()
	{
		let lBetInfo_bi = this._currentBetInfo;
		this.emit(AstronautPanelView.EVENT_ON_EDIT_AUTO_EJECT_INITIATED, {betInfo: lBetInfo_bi, autoEjectMultiplier: this.autoEjectMultiplier});
	}

	validate()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		let l_gpi = this._fGameplayInfo_gpi;
		let lRoundInfo_fi = l_gpi.roundInfo;
		let lBetsInfo_bsi = this._fBetsInfo_bsi;
		let lBetInfo_bi = this._currentBetInfo;

		let lIsBetCancelInProgress_bl = !!lBetInfo_bi && this._fBetsController_bsc.isBetCancelInProgress(lBetInfo_bi.betId);
		let lIsBetCancelAutoEjectInProgress_bl = !!lBetInfo_bi && this._fBetsController_bsc.isBetCancelAutoEjectInProgress(lBetInfo_bi.betId);

		let lCancelAutoEjectButton_gb = this._fCancelAutoEjectButton_gb;

		let lInRocketPanelContainerActive = this._fInRocketPanelContainer_irpc && lRoundInfo_fi.isRoundPlayActive && !!lBetInfo_bi && lBetInfo_bi.isConfirmedMasterBet && !lBetInfo_bi.isEjected;
		if (this._fInRocketPanelContainer_irpc) 
		{
			if (!this._fInRocketPanelContainer_irpc.visible && lInRocketPanelContainerActive)
			{
				this._fAutoEjectMultiplierEdit_mev.hasFocus && this._fAutoEjectMultiplierEdit_mev.autoBlur();
				this._fBetEditView_bev.unfocus();
			}

			this._fInRocketPanelContainer_irpc.visible = lInRocketPanelContainerActive;

			lCancelAutoEjectButton_gb.enabled = !!lBetInfo_bi && lBetInfo_bi.isAutoEject && !lIsBetCancelAutoEjectInProgress_bl && !this._fGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered;
			let lCurCancelAutoEjectButtonAssetId_str = lCancelAutoEjectButton_gb.captionId;
			let lRequiredCancelAutoEjectButtonAssetId_str = (lBetInfo_bi && lBetInfo_bi.isAutoEjectCancelledInRound) ? "TAAutoEjectCancelled" : "TACancelAutoEject";
			if (lIsPortraitMode_bl)
			{
				lRequiredCancelAutoEjectButtonAssetId_str += "Portrait";
			}
			if (lCurCancelAutoEjectButtonAssetId_str !== lRequiredCancelAutoEjectButtonAssetId_str)
			{
				lCancelAutoEjectButton_gb.captionId = lRequiredCancelAutoEjectButtonAssetId_str;
			}
			
			this._fEjectButton_gb.enabled = !!lBetInfo_bi && !lIsBetCancelInProgress_bl && !this._fGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered;
		}
		
		this._fEditBetButton_gb.visible = lRoundInfo_fi.isRoundWaitState && !!lBetInfo_bi && lBetInfo_bi.isConfirmedMasterBet;
		this._fEditBetButton_gb.enabled = this._fEditBetButton_gb.visible && lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined && !this._fGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered
											&& !lIsBetCancelInProgress_bl && !lIsBetCancelAutoEjectInProgress_bl && !lInRocketPanelContainerActive;

		this._fPlaceBetButton_gb.visible = !this._fEditBetButton_gb.visible;
		this._fPlaceBetButton_gb.enabled = this._fPlaceBetButton_gb.visible && lRoundInfo_fi.isRoundWaitState 
											&& lBetsInfo_bsi.gamePlayersInfo.isMasterSeatDefined && !this._fGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered
											&& lBetsInfo_bsi.isValidBetValue(this._fBetEditView_bev.currentAppliedBetValue)
											&& lBetsInfo_bsi.isValidBetValue(this._fBetEditView_bev.currentEnteredValue)
											&& (!this._fAutoEjectEnabled_bl || lBetsInfo_bsi.isValidAutoEjectMultiplier(this.autoEjectMultiplier))
											&& !lBetInfo_bi
											&& !lIsBetCancelInProgress_bl && !lInRocketPanelContainerActive;

		this._fHalfBetButton_gb.visible = true;
		this._fMaxBetButton_gb.visible = true;
		this._fDoubleBetButton_gb.visible = true;

		if ( (lRoundInfo_fi.isRoundWaitState && !!lBetInfo_bi && !lBetInfo_bi.isEjected) || lInRocketPanelContainerActive)
		{
			this._disableBetEditView();

			if (lBetInfo_bi.isConfirmedMasterBet && !lInRocketPanelContainerActive)
			{
				this._enableMultiplyerEditView();
			}
			else
			{
				this._disableMultiplyerEditView();
			}
		}
		else if (lBetsInfo_bsi.isNoMoreBetsPeriod && !!lBetInfo_bi)
		{
			this._disableBetEditView();
			this._disableMultiplyerEditView(lCancelAutoEjectButton_gb.enabled);
		}
		else
		{
			this._enableBetEditView();
			this._enableMultiplyerEditView();
		}
	}

	_disableBetEditView()
	{
		this._fBase_gr_arr[0].visible = false;
		this._fDisabledBase_gr_arr[0].visible = true;
		
		this._fBetEditView_bev.enabled = false;
		this._fHalfBetButton_gb.enabled = false;
		this._fMaxBetButton_gb.enabled = false;
		this._fDoubleBetButton_gb.enabled = false;

		this._fHalfBetButton_gb.alpha = 0.2;
		this._fMaxBetButton_gb.alpha = 0.2;
		this._fDoubleBetButton_gb.alpha = 0.2;

		this._fBetAmountCaption_ta.alpha = 0.2;
		this._fBetEditView_bev.alpha = 0.2;

		this._fBetEditView_bev.unfocus();
	}

	_enableBetEditView()
	{
		this._fBase_gr_arr[0].visible = true;
		this._fDisabledBase_gr_arr[0].visible = false;

		this._fBetEditView_bev.enabled = true;
		this._fHalfBetButton_gb.enabled = true;
		this._fMaxBetButton_gb.enabled = true;
		this._fDoubleBetButton_gb.enabled = true;

		this._fHalfBetButton_gb.alpha = 1;
		this._fMaxBetButton_gb.alpha = 1;
		this._fDoubleBetButton_gb.alpha = 1;

		this._fBetAmountCaption_ta.alpha = 1;
		this._fBetEditView_bev.alpha = 1;
	}

	_disableMultiplyerEditView(aOptKeepEnabledAutoEjectCancel_bl=undefined)
	{
		aOptKeepEnabledAutoEjectCancel_bl = !!aOptKeepEnabledAutoEjectCancel_bl;

		let lIsFullDisable_bl = !aOptKeepEnabledAutoEjectCancel_bl;

		if (this._fBase_gr_arr[1])
		{
			this._fBase_gr_arr[1].visible = !lIsFullDisable_bl;
			this._fDisabledBase_gr_arr[1].visible = lIsFullDisable_bl && AstronautPanelView.ROWS_AMOUNT > 2;
		}

		this._fAutoEjectCaption_ta.alpha = lIsFullDisable_bl ? 0.2 : 1;
		this._fAutoEjectMultiplierEdit_mev.alpha = lIsFullDisable_bl ? 0.2 : 1;
		this._fAutoEjectMultiplierEdit_mev.enabled = false;

		if (aOptKeepEnabledAutoEjectCancel_bl)
		{
			this._fAutoEjectMultiplierEdit_mev.allowAutoEjectReset();
		}

		this._fAutoEjectMultiplierEdit_mev.unfocus();
	}

	_enableMultiplyerEditView()
	{
		if (this._fBase_gr_arr[1])
		{
			this._fBase_gr_arr[1].visible = AstronautPanelView.ROWS_AMOUNT > 2;
			this._fDisabledBase_gr_arr[1].visible = false;
		}
	
		this._fAutoEjectCaption_ta.alpha = 1;
		this._fAutoEjectMultiplierEdit_mev.alpha = 1;
		this._fAutoEjectMultiplierEdit_mev.enabled = true;
	}

	_onButtonBetHalfClick()
	{
		let lCurBetValue_num = this._fBetEditView_bev.currentAppliedBetValue;
		let lNewValue_num;
		
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
		{
			let lPower_num = DecimalPartValidator.getAmountOfDecimalsToTrunc();
			lNewValue_num = Math.floor(lCurBetValue_num*0.5/Math.pow(10, lPower_num))*Math.pow(10, lPower_num);
			lNewValue_num = Math.max(this._fBetsInfo_bsi.betMinimalCentsCount, lNewValue_num);
		}
		else
		{
			lNewValue_num = Math.max(this._fBetsInfo_bsi.betMinimalCentsCount, ~~(lCurBetValue_num*0.5));
		}
		this._fBetEditView_bev.setBetValue(lNewValue_num);
	}

	_onButtonBetDoubleClick()
	{
		let lCurBetValue_num = this._fBetEditView_bev.currentAppliedBetValue;
		let lNewValue_num = Math.min(this._fBetsInfo_bsi.betMaximalCentsCount, ~~(lCurBetValue_num*2));
		
		this._fBetEditView_bev.setBetValue(lNewValue_num);
	}

	_onButtonBetMaxClick()
	{
		this._fBetEditView_bev.setBetValue(this._fBetsInfo_bsi.betMaximalCentsCount);
	}

	_onButtonPlaceBetClick()
	{
		this.emit(AstronautPanelView.EVENT_ON_PLACE_BET_BUTTON_CLICKED);
		this.validate();
	}

	_onButtonCancelBetClick()
	{
		this.emit(AstronautPanelView.EVENT_ON_CANCEL_BET_BUTTON_CLICKED);
		this.validate();
	}

	_setCurrencySymbol()
	{
		this._fBetEditView_bev._setCurrencySymbol();
	}
}
export default AstronautPanelView;