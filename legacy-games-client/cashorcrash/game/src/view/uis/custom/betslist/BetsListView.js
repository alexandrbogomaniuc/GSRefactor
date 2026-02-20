import BetsListBaseView from './BetsListBaseView';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import BetsListItem from './BetsListItem';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import VerticalSlider from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/slider/VerticalSlider';
import VerticalScrollBar from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollBar';
import BetsListScrollableContainer from './BetsListScrollableContainer';

class BetsListView extends BetsListBaseView
{   
	static get BETS_LIST_SCROLLBAR_WIDTH ()    { return 5; }
	static get BETS_LIST_SCROLLBAR_HEIGHT ()    { return 120; } // default height for initialize

	set astronautsCount(aValue_int)
	{
		if (isNaN(aValue_int) || aValue_int < 0)
		{
			console.log("Wrong astronauts count: " + aValue_int);
			return;
		}
		let lAstronautsTemplateText_str = this._fAstronautsTemplateText_str;

		let lPlayerSubStr_str = "#players:";
		lAstronautsTemplateText_str = I18.prepareNumberPoweredMessage(lAstronautsTemplateText_str, lPlayerSubStr_str, this.uiInfo.activePlayersAmount);

		let lAstronautsSubStr_str = "#astronaut:";
		lAstronautsTemplateText_str = I18.prepareNumberPoweredMessage(lAstronautsTemplateText_str, lAstronautsSubStr_str, aValue_int);

		this._fAstronautsCaption_ta.text = lAstronautsTemplateText_str.replace("/VALUE/", aValue_int).replace("/NUMBER/", this.uiInfo.activePlayersAmount);
	}

	set astronautsBetsAmount(aValue_num)
	{
		if (isNaN(aValue_num) || aValue_num < 0)
		{
			console.log("Wrong astronauts bets amount: " + aValue_num);
			return;
		}

		// [OWL] TODO: apply changes for alll systems without any conditions
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
		{
			this._fAstronautsBetsAmount_tf.text = APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
		}
		else
		{
			this._fAstronautsBetsAmount_tf.text = APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2, undefined, false);
		}
	}

	set masterPlayerWin(aValue_num)
	{
		let lFormattedValue_str;
		// [OWL] TODO: apply changes for alll systems without any conditions
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
		{
			lFormattedValue_str = APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
		}
		else
		{
			lFormattedValue_str = APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2, undefined, false);
		}
		this._fTotalRoundWinCaption_ta.text = this._fTotalRoundWinTemplate_str.replace("/VALUE/", lFormattedValue_str);
	}

	updateBets()
	{
		let lBetInfos_bi_arr = this.uiInfo.allBets;
		if (lBetInfos_bi_arr)
		{
			let lItemsCountByScroll_int = lBetInfos_bi_arr && lBetInfos_bi_arr.length ?
				Math.min(this._fBetsListScrollableContainer_bslsc.scrollingPageSize, Math.ceil(lBetInfos_bi_arr.length / 32)) : 1;
			this._fBetsListSlider_vs.scrollMultiplier = BetsListItem.ITEM_HEIGHT * lItemsCountByScroll_int;
			this._fBetsListScrollableContainer_bslsc.updateBets(lBetInfos_bi_arr);
		}
		this.astronautsCount = this.uiInfo.betsCount;
		this.astronautsBetsAmount = this.uiInfo.betsTotalSum;
	}

	updateTotalWinIndicator()
	{
		this.masterPlayerWin = this.uiInfo.masterPlayerTotalWin;
	}

	updateLayout(aLayout_rt, aIsPortraitMode_bl)
	{
		this.position.set(aLayout_rt.x, aLayout_rt.y);

		if (this._fContentWidth_num !== aLayout_rt.width || this._fContentHeight_num !== aLayout_rt.height || this._fIsPortraitMode_bl !== aIsPortraitMode_bl)
		{
			this._fContentWidth_num = aLayout_rt.width;
			this._fContentHeight_num = aLayout_rt.height;
			this._fIsPortraitMode_bl = aIsPortraitMode_bl;

			if (this.uiInfo) // view is already initialized
			{
				this._updateLayoutSettings();
			}
		}
	}

	//INIT...
	constructor()
	{
		super();

		this._fBetsListPositionY_num = 28;

		this._fAstronautsCaption_ta = null;
		this._fAstronautsTemplateText_str = null;
		this._fAstronautsMaxWidth_num = null;

		this._fBetsListScrollBar_vsb = null;
		this._fBetsListScrollableContainer_bslsc = null;
		this._fBetsListSlider_vs = null;
	}
	
	__init()
	{
		super.__init();

		this._fTotalRoundWinCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TATotalRoundWin"));
		this._fTotalRoundWinTemplate_str = this._fTotalRoundWinCaption_ta.text;
		
		this._fAstronautsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAAstronauts"));
		this._fAstronautsTemplateText_str = this._fAstronautsCaption_ta.text;

		this._fAstronautsBetsAmount_tf = this.addChild(new TextField(this._getAstronautsTextFormat()));
		this._fAstronautsBetsAmount_tf.anchor.set(1, 1);
		
		this._fAstronautsMaxWidth_num = this._fAstronautsCaption_ta.descriptor.areaInnerContentDescriptor.areaDescriptor.width;

		this._fBetsListScrollableContainer_bslsc = this.addChild(new BetsListScrollableContainer());
		this._fBetsListScrollableContainer_bslsc.position.set(0, this._fBetsListPositionY_num);
		
		let lScrollbarWidth_num = BetsListView.BETS_LIST_SCROLLBAR_WIDTH;
		let lScrollbarHeight_num = BetsListView.BETS_LIST_SCROLLBAR_HEIGHT;
		let lScrollBack_grphc = new PIXI.Graphics().beginFill(0x262626).drawRoundedRect(-lScrollbarWidth_num / 2, -lScrollbarHeight_num / 2, lScrollbarWidth_num, lScrollbarHeight_num, 2).endFill();
		let lScrollThumb_grphc = new PIXI.Graphics().beginFill(0x5c5c5c).drawRoundedRect(-lScrollbarWidth_num / 2, -lScrollbarHeight_num / 2, lScrollbarWidth_num, lScrollbarHeight_num, 2).endFill();
		let lBetsListSlider_vs = this._fBetsListSlider_vs = this.addChild(new VerticalSlider(lScrollBack_grphc, lScrollThumb_grphc, undefined, undefined, 0, null, false));
		lBetsListSlider_vs.scrollMultiplier = BetsListItem.ITEM_HEIGHT;
		lBetsListSlider_vs.visible = false;

		let lBetsListScrollBar_vsb = this._fBetsListScrollBar_vsb = this.addChild(new VerticalScrollBar());
		lBetsListScrollBar_vsb.visibleArea = new PIXI.Rectangle(0, 0, 100, 100);
		lBetsListScrollBar_vsb.hitArea = new PIXI.Rectangle(0, 0, 100, 100);
		lBetsListScrollBar_vsb.slider = lBetsListSlider_vs;
		lBetsListScrollBar_vsb.scrollableContainer = this._fBetsListScrollableContainer_bslsc;
		lBetsListScrollBar_vsb.enableScroll();
		lBetsListScrollBar_vsb.enableDrag();

		this._updateLayoutSettings();
	}
	//...INIT

	_updateLayoutSettings()
	{
		if (this._fContentWidth_num === undefined || this._fContentHeight_num === undefined) return;

		let lIsPortraitMode_bl = this._fIsPortraitMode_bl;
		let lMargin_num = lIsPortraitMode_bl ? 7 : 2;
		let lAddPortraitMargin_num = lIsPortraitMode_bl ? 2 : 0;

		this._fBetsListPositionY_num = lIsPortraitMode_bl ? 33 : 28;

		let lTotalRoundWinCaptionX_num = lIsPortraitMode_bl ? lMargin_num + this._fContentWidth_num + lAddPortraitMargin_num : lMargin_num;
		let lTotalRoundWinCaptionY_num = lIsPortraitMode_bl ? 87 : 8;
		let lTotalRoundWinCaptionAssetId_str = lIsPortraitMode_bl ? "TATotalRoundWinPortrait" : "TATotalRoundWin";
		this._fTotalRoundWinCaption_ta.position.set(lTotalRoundWinCaptionX_num, lTotalRoundWinCaptionY_num);
		this._fTotalRoundWinCaption_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lTotalRoundWinCaptionAssetId_str));
		this._fTotalRoundWinTemplate_str = this._fTotalRoundWinCaption_ta.text;
		this.updateTotalWinIndicator();

		let lAstronautsCaptionX_num = lIsPortraitMode_bl ? lTotalRoundWinCaptionX_num : lMargin_num;
		let lAstronautsCaptionY_num = lIsPortraitMode_bl ? 116 : 28;
		let lAstronautsCaptionAssetId_str = lIsPortraitMode_bl ? "TAAstronautsPortrait" : "TAAstronauts";
		this._fAstronautsCaption_ta.position.set(lAstronautsCaptionX_num, lAstronautsCaptionY_num);
		this._fAstronautsCaption_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lAstronautsCaptionAssetId_str));
		this._fAstronautsTemplateText_str = this._fAstronautsCaption_ta.text;
		this.astronautsCount = this.uiInfo.betsCount;
		
		let lRightBoundary_num = this._fContentWidth_num - lMargin_num - BetsListItem.ITEM_MARGIN_RIGHT;

		this._fAstronautsBetsAmount_tf.position.x = lIsPortraitMode_bl ? lRightBoundary_num + (lTotalRoundWinCaptionX_num - lMargin_num - lAddPortraitMargin_num): lRightBoundary_num;
		this._fAstronautsBetsAmount_tf.position.y = lIsPortraitMode_bl ? 116 : 28;
		let lAstronautsBetsAmountFontSize_num = lIsPortraitMode_bl ? 14 : this._getAstronautsTextFormat().fontSize;
		this._fAstronautsBetsAmount_tf.updateFontSize(lAstronautsBetsAmountFontSize_num);
		this._fAstronautsBetsAmount_tf.maxWidth = lRightBoundary_num - this._fAstronautsMaxWidth_num - lMargin_num * 3 /*additional indent between fields*/;
		if (lIsPortraitMode_bl)
		{
			this._fAstronautsBetsAmount_tf.maxWidth -= lAddPortraitMargin_num;
		}

		let lBetsListWidth_num = this._fContentWidth_num - lMargin_num * (lIsPortraitMode_bl ? 1 : 2);
		let lBetsListHeight_num = this._fContentHeight_num - this._fBetsListPositionY_num;
		lBetsListHeight_num = Math.floor(lBetsListHeight_num/BetsListItem.ITEM_HEIGHT)*BetsListItem.ITEM_HEIGHT;
		
		this._fBetsListScrollBar_vsb.visibleArea = new PIXI.Rectangle(0, 0, lBetsListWidth_num, lBetsListHeight_num);
		this._fBetsListScrollBar_vsb.hitArea = new PIXI.Rectangle(0, 0, this._fContentWidth_num - lMargin_num, lBetsListHeight_num);
		this._fBetsListScrollBar_vsb.position.set(lMargin_num, this._fBetsListPositionY_num);

		let lScrollbarWidth_num = BetsListView.BETS_LIST_SCROLLBAR_WIDTH;
		let lScrollbarHeight_num = lBetsListHeight_num;
		let lScrollBack_grphc = new PIXI.Graphics().beginFill(0x262626).drawRoundedRect(-lScrollbarWidth_num / 2, -lScrollbarHeight_num / 2, lScrollbarWidth_num, lScrollbarHeight_num, 2).endFill();
		let lScrollThumb_grphc = new PIXI.Graphics().beginFill(0x5c5c5c).drawRoundedRect(-lScrollbarWidth_num / 2, -lScrollbarHeight_num / 2, lScrollbarWidth_num, lScrollbarHeight_num, 2).endFill();
		this._fBetsListSlider_vs.updateView(lScrollBack_grphc, lScrollThumb_grphc);

		this._fBetsListSlider_vs.position.set(
			this._fContentWidth_num - lMargin_num*(lIsPortraitMode_bl ? 0 : 1) - BetsListView.BETS_LIST_SCROLLBAR_WIDTH / 2, 
			this._fBetsListPositionY_num + lBetsListHeight_num / 2
		);

		this._fBetsListScrollableContainer_bslsc.updateLayout(lBetsListWidth_num, Math.floor(lBetsListHeight_num / BetsListItem.ITEM_HEIGHT));
		this._fBetsListScrollableContainer_bslsc.position.set(lMargin_num, this._fBetsListPositionY_num);
		this._fBetsListScrollableContainer_bslsc.resetInitPosition();
	}

	_getAstronautsTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 11,
			align: "right",
			fill: 0x575757
		};
	}
}

export default BetsListView;