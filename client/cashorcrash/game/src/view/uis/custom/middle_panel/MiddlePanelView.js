import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BetsListBaseView from '../betslist/BetsListBaseView';
import VerticalSlider from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/slider/VerticalSlider';
import VerticalScrollBar from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollBar';
import MasterBetItem from '../betslist/MasterBetItem';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayInfo from '../../../../model/gameplay/GameplayInfo';
import BattlegroundBetsListScrollableContainer from '../betslist/battleground/BattlegroundBetsListScrollableContainer';
import BattlegroundBetsListItem from '../betslist/battleground/BattlegroundBetsListItem';
import AtlasSprite  from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';
import { BitmapText } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';

class MiddlePanelView extends BetsListBaseView
{
    static get BETS_LIST_SCROLLBAR_WIDTH ()         { return 5; }
    static get BETS_LIST_SCROLLBAR_HEIGHT ()        { return 120; }

    set remainingAstronautsCount(aValue_int)
    {
        if (isNaN(aValue_int) || aValue_int < 0)
        {
            console.log(`Wrong REMAINING astronauts count: ${aValue_int}`);
            return;
        }

        this._fRemainingCaption_ta.text = this._fRemainingCaption_ta.text.replace("/VALUE/", aValue_int);
    }

    lockList()
    {
        let lBetsListScrollBar_vsb = this._fBetsListScrollbar_vsb;

        lBetsListScrollBar_vsb.disableScroll();
        lBetsListScrollBar_vsb.disableDrag();

        this._fBetsListScrollableContainer_bslsc.lockRedraws();
    }

    unlockList()
    {
        let lBetsListScrollBar_vsb = this._fBetsListScrollbar_vsb;

        // to prevent multiple listeners...
        lBetsListScrollBar_vsb.disableScroll();
        lBetsListScrollBar_vsb.disableDrag();
        // ...to prevent multiple listeners

        lBetsListScrollBar_vsb.enableScroll();
        lBetsListScrollBar_vsb.enableDrag();

        this._fBetsListScrollableContainer_bslsc.unlockRedraws();
    }

    validateVisibleArea()
    {
        let lBetInfos_bi_arr = this.uiInfo.allBets;
        let lBetsListScrollbarTopPoint_num = APP.layout.isPortraitOrientation ? - 26 : -34;
        let l_gpi = APP.gameController.gameplayController.info;
        let lRoundInfo_ri = l_gpi.roundInfo;

        if (lRoundInfo_ri.isRoundPlayState || lRoundInfo_ri.isRoundQualifyState)
        {
            if (lBetInfos_bi_arr && lBetInfos_bi_arr[0] && lBetInfos_bi_arr[0].isEjected && this._fBetsListScrollableContainer_bslsc.topItemIndex === 0)
            {
                this._fBetsListScrollbarVisibleArea_rect.height = this._fBetsListScrollableContainer_bslsc.itemHieght * this._fBetsListScrollableContainer_bslsc.scrollingPageSize + 5;
                this._fBetsListScrollableContainer_bslsc.position.y = lBetsListScrollbarTopPoint_num;
                this._fBetsListScrollbar_vsb.position.y = lBetsListScrollbarTopPoint_num - 5;
            }
            else
            {
                this._fBetsListScrollbarVisibleArea_rect.height = this._fBetsListScrollableContainer_bslsc.itemHieght * this._fBetsListScrollableContainer_bslsc.scrollingPageSize;
                this._fBetsListScrollableContainer_bslsc.position.y = lBetsListScrollbarTopPoint_num;
                this._fBetsListScrollbar_vsb.position.y = lBetsListScrollbarTopPoint_num;
            }
        }

        this._fBetsListScrollbar_vsb.visibleArea = this._fBetsListScrollbarVisibleArea_rect;

    }

    updateBets()
    {
        let lBetInfos_bi_arr = this.uiInfo.allBets;
        
        this._fBetsListScrollableContainer_bslsc.updateBets(lBetInfos_bi_arr);
        this._fBetsListSlider_vs.scrollMultiplier = this._calculateScrollMultiplier();

        this._updateIndicators();
    }

    _updateIndicators()
    {
        let lBetInfos_bi_arr = this.uiInfo.allBets;

        let lRemainingValue_num = 0;
        let lIsMasterBetRowRequired_bl = false;
        for (let i = 0; i < lBetInfos_bi_arr.length; i++)
        {   
            let lCur_bi = lBetInfos_bi_arr[i];
            if (!lCur_bi.isEjected)
            {
                lRemainingValue_num++;
            }

            if (lCur_bi.isMasterBet)
            {
                let lMasterDistance_num = lCur_bi.isEjected ? lCur_bi.multiplier : undefined;
                let l_gpi = APP.gameController.gameplayController.info;
                let lEjectedBetDuration_num = lCur_bi.ejectTime - l_gpi.multiplierChangeFlightStartTime;
                let lMasterDuration_num = lCur_bi.isEjected ? GameplayInfo.formatTime(lEjectedBetDuration_num) : undefined;
                
                this._fMasterBetItem_mbi.update(lMasterDistance_num, lMasterDuration_num);
                
                lIsMasterBetRowRequired_bl = true
            }
        }

        this._fMasterBetItem_mbi.visible = lIsMasterBetRowRequired_bl;

        this._fRemaining_tf.write(lRemainingValue_num + "");
    }

    _updateViewPosition(aIsPortraitMode_bl)
    {
        let lX_num = this._fContentX_num || 0;
        let lY_num = this._fContentY_num || 0;

        let l_gpi = APP.gameController.gameplayController.info;
        let lRoundInfo_ri = l_gpi.roundInfo;

        if (!!aIsPortraitMode_bl)
        {
            lX_num += (lRoundInfo_ri.isRoundWaitState || !lRoundInfo_ri.isRoundStateDefined) ? -261 : 0;
            lY_num += (lRoundInfo_ri.isRoundWaitState || !lRoundInfo_ri.isRoundStateDefined) ? 212 : 0;
        }

        this.position.set(lX_num, lY_num);
    }

    adjustLayoutSettings()
    {
        this._updateViewPosition(this._fIsPortraitMode_bl);

        this._updateLayoutSettings();
    }

    constructor()
    {
        super();

        this._fContentWidth_num = undefined;
        this._fContentHeight_num = undefined;
        this._fIsPortraitMode_bl = false;

        this._fRemainingCaption_ta = null;
        
        this._fBetsListScrollbar_vsb = null
        this._fBetsListScrollableContainer_bslsc = null;
        this._fBetsListSlider_vs = null;

        this._fMasterBetItem_mbi = null;

        this._fRemaining_tf = null;

        this._fLongBase_gr = null;
        this._fShortBase_gr = null;

        this._fScoreCard_ta = null;

        this._fContentContainer_sprt = null;

        this._fScrollBack_grphc = null;
        this._fScrollThumb_grphc = null;

        this._fBetsListScrollbarVisibleArea_rect = null;
        this._fBetsListScrollbarHitArea_rect = null;

        this._fCurrentItemHeight_num = 0;
    }

    __init()
    {
        super.__init();

        this._fLongBase_gr = this.addChild(new PIXI.Graphics);
        this._fShortBase_gr = this.addChild(new PIXI.Graphics);

        let lContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);
        lContainer_sprt.position.set(56, 50);

        this._fScoreCard_ta = lContainer_sprt.addChild(APP.library.getSprite("labels/scorecard"));

        this._fRemainingCaption_ta = this.addChild(APP.library.getSprite("labels/remaining"));

        this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("roboto/bmp_roboto_bold")], [AtlasConfig.BmpRobotoBold], "");
		this._fRemaining_tf = this.addChild(new BitmapText(this._fTextures_tx_map, "", 0));
        this._fRemaining_tf.visible = true;
        
        this._fBetsListScrollableContainer_bslsc = lContainer_sprt.addChild(new BattlegroundBetsListScrollableContainer());
        let lMasterPlayerBetItem_mbi = this._fMasterBetItem_mbi = lContainer_sprt.addChild(new MasterBetItem());
        lMasterPlayerBetItem_mbi.visible = false;

        let lScrollBack_grphc = this._fScrollBack_grphc = new PIXI.Graphics().drawRoundedRect(0, 0, 1, 1, 4);
        let lScrollThumb_grphc = this._fScrollThumb_grphc = new PIXI.Graphics().drawRoundedRect(0, 0, 1, 1, 4);
        let lBetsListSlider_vs = this._fBetsListSlider_vs = lContainer_sprt.addChild(new VerticalSlider(lScrollBack_grphc, lScrollThumb_grphc, undefined, undefined, 0, null, false));
        lBetsListSlider_vs.scrollMultiplier = BattlegroundBetsListItem.ITEM_HEIGHT;
        lBetsListSlider_vs.visible = true;

        let lBetsListScrollBar_vsb = this._fBetsListScrollbar_vsb = lContainer_sprt.addChild(new VerticalScrollBar());
        lBetsListScrollBar_vsb.visibleArea = this._fBetsListScrollbarVisibleArea_rect = new PIXI.Rectangle(0, 0, 10, 10);
        lBetsListScrollBar_vsb.hitArea = this._fBetsListScrollbarHitArea_rect = new PIXI.Rectangle(0, 0, 240, 100);
        lBetsListScrollBar_vsb.slider = lBetsListSlider_vs;
        lBetsListScrollBar_vsb.scrollableContainer = this._fBetsListScrollableContainer_bslsc;
        lBetsListScrollBar_vsb.enableScroll();
        lBetsListScrollBar_vsb.enableDrag();

        this.adjustLayoutSettings();
    }

    _updateLayoutSettings()
    {
        if (this._fContentWidth_num === undefined || this._fContentHeight_num === undefined) return;

        let lIsPortraitMode_bl = this._fIsPortraitMode_bl;
        let l_gpi = APP.gameController.gameplayController.info;
        let lRoundInfo_ri = l_gpi.roundInfo;

        let lScoreCard_ta = this._fScoreCard_ta;
        
        if (lRoundInfo_ri.isRoundQualifyState || lRoundInfo_ri.isRoundPlayState)
        {
            let lScorecardPositionLeftPoint_num = APP.isMobile ? (lIsPortraitMode_bl ? 25 : 25) : 25;
            lScoreCard_ta.position.set(lScorecardPositionLeftPoint_num, (APP.isMobile && lIsPortraitMode_bl)? -51 : -56);
        }
        else if (lRoundInfo_ri.isRoundWaitState || !lRoundInfo_ri.isRoundStateDefined)
        {   
            lScoreCard_ta.position.set(APP.isMobile ? 43 : 30, lIsPortraitMode_bl ? -116 : -119);
        }

        let lRemainingAstronauts_ta = this._fRemainingCaption_ta;

        lRemainingAstronauts_ta.position.x = APP.isMobile ? 69 : 69;
        lRemainingAstronauts_ta.position.y = lIsPortraitMode_bl ? (APP.isMobile ?301:308) : 150;

        let lRemaining_tf = this._fRemaining_tf;
        lRemaining_tf.scale.set(lIsPortraitMode_bl ? 0.2 : 0.15, lIsPortraitMode_bl ? 0.2 : 0.15);
        lRemaining_tf.position.x = APP.isMobile ? 120 : (lIsPortraitMode_bl ? 130 : 120);
        lRemaining_tf.position.y = APP.isMobile ? (lIsPortraitMode_bl ? 304 : 153) : (lIsPortraitMode_bl ? 311 : 153);

        this._fRemainingCaption_ta.visible = lRoundInfo_ri.isRoundQualifyState || lRoundInfo_ri.isRoundPlayState;
        this._fRemaining_tf.visible = lRoundInfo_ri.isRoundQualifyState || lRoundInfo_ri.isRoundPlayState;

        this._fBetsListScrollableContainer_bslsc.position.x = -54;
        this._fBetsListScrollableContainer_bslsc.position.y = lIsPortraitMode_bl
                                                                ? ( (lRoundInfo_ri.isRoundWaitState || !lRoundInfo_ri.isRoundStateDefined) ? -93 : - 26)
                                                                : ( (lRoundInfo_ri.isRoundWaitState || !lRoundInfo_ri.isRoundStateDefined) ? -103: -34);

        this._fBetsListScrollableContainer_bslsc.resetInitPosition();

        this._updateBaseViews();
        this._fLongBase_gr.visible = lRoundInfo_ri.isRoundQualifyState || lRoundInfo_ri.isRoundPlayState;
        this._fShortBase_gr.visible = lRoundInfo_ri.isRoundWaitState;

        this._updateBetsListItemsAmount();
    }

    _updateBaseViews()
    {
        let lIsPortraitMode_bl = this._fIsPortraitMode_bl;
        
        let lShort_gr = this._fShortBase_gr;
        let lLong_gr = this._fLongBase_gr;
        
        lShort_gr.cacheAsBitmap = false;
        lLong_gr.cacheAsBitmap = false;

        lShort_gr.clear();
        lLong_gr.clear();

        if (lIsPortraitMode_bl)
        {
            lShort_gr.beginFill(0x111421, 1).drawRect(2, -85, 250, 44).endFill();
            lLong_gr.beginFill(0x111421, 1).drawRect(2, -23, 250, 349).endFill();
        }
        else
        {
            lShort_gr.beginFill(0x111421, 1).drawRect(2, -85, 250, 34).endFill();
            lLong_gr.beginFill(0x111421, 1).drawRect(2, -23, 250, 186).endFill();
        }
    }

    _updateBetsListItemsAmount()
    {
        let l_gpi = APP.gameController.gameplayController.info;
        let lRoundInfo_ri = l_gpi.roundInfo;
        let lIsPortraitMode_bl = this._fIsPortraitMode_bl;
        let lItemWidth_num = 250;
        let lItemHeight_num = 0;
        let lItemsAmount_num = 6;

        if (lIsPortraitMode_bl)
        {
            if (lRoundInfo_ri.isRoundPlayState || lRoundInfo_ri.isRoundQualifyState)
            {
                lItemHeight_num = BattlegroundBetsListItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE;
                lItemsAmount_num = 8;
            }
            else
            {
                lItemHeight_num = BattlegroundBetsListItem.ITEM_HEIGHT_PORTRAIT_WAIT_STATE;
            }
        }
        else
        {
            lItemHeight_num = BattlegroundBetsListItem.ITEM_HEIGHT;
            lItemsAmount_num = 6;
        }

        this._fCurrentItemHeight_num = lItemHeight_num;
        this._fMasterBetItem_mbi.updateSize(lItemWidth_num, lItemHeight_num, lIsPortraitMode_bl);

        let lBetListContainerTopPoint_num = this._fBetsListScrollableContainer_bslsc.position.y;
        let lBetsListContainerLeftPoint_num = this._fBetsListScrollableContainer_bslsc.position.x;
        
        let lScrollbarHeight_num = lItemHeight_num * (lItemsAmount_num - 1);

        this._fBetsListScrollbarVisibleArea_rect.width = lItemWidth_num;
        this._fBetsListScrollbarVisibleArea_rect.height = lScrollbarHeight_num;

        this._fBetsListScrollbarHitArea_rect.width = lItemWidth_num;
        this._fBetsListScrollbarHitArea_rect.height = lScrollbarHeight_num;

        this._fBetsListScrollbar_vsb.visibleArea = this._fBetsListScrollbarVisibleArea_rect;
        this._fBetsListScrollbar_vsb.hitArea = this._fBetsListScrollbarHitArea_rect;

        this._fBetsListScrollbar_vsb.position.set(lBetsListContainerLeftPoint_num, lBetListContainerTopPoint_num);
    
        let lScrollbarWidth_num = MiddlePanelView.BETS_LIST_SCROLLBAR_WIDTH;

        let lScrollBack_grphc = new PIXI.Graphics().beginFill(0x262626).drawRoundedRect(-lScrollbarWidth_num/2, -lScrollbarHeight_num / 2, lScrollbarWidth_num, lScrollbarHeight_num, 2).endFill();
        let lScrollThumb_grphc = new PIXI.Graphics().beginFill(0x5c5c5c).drawRoundedRect(-lScrollbarWidth_num/2, -lScrollbarHeight_num / 2, lScrollbarWidth_num, lScrollbarHeight_num, 2, 2).endFill();
        
        this._fBetsListScrollableContainer_bslsc.updateLayout(lItemWidth_num, lItemsAmount_num, lItemHeight_num);
        
        this._fBetsListSlider_vs.updateView(lScrollBack_grphc, lScrollThumb_grphc);
        this._fBetsListSlider_vs.position.set(lBetsListContainerLeftPoint_num + lItemWidth_num - lScrollbarWidth_num / 2, lBetListContainerTopPoint_num + lScrollbarHeight_num / 2);
        this._fBetsListSlider_vs.scrollMultiplier = this._calculateScrollMultiplier();

        this._fMasterBetItem_mbi.position.set(-54, lBetListContainerTopPoint_num + lScrollbarHeight_num);
    }

    _calculateScrollMultiplier()
    {
        let lBetInfos_bi_arr = this.uiInfo.allBets;
        let lItemsCountByScroll_int = lBetInfos_bi_arr && lBetInfos_bi_arr.length
                                            ? Math.min(this._fBetsListScrollableContainer_bslsc.scrollingPageSize, Math.ceil(lBetInfos_bi_arr.length /32))
                                            : 1;

        return this._fCurrentItemHeight_num * lItemsCountByScroll_int;
    }
}

export default MiddlePanelView;