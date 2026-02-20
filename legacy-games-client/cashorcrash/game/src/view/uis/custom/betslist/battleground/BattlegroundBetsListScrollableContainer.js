import VerticalScrollableContainer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollableContainer';
import ScrollableContainer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/ScrollableContainer';
import BattlegroundBetsListItem from "./BattlegroundBetsListItem";
import { APP } from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import GameplayInfo from "../../../../../model/gameplay/GameplayInfo";

class BattlegroundBetsListScrollableContainer extends VerticalScrollableContainer
{
    static get NON_VISABLE_AREA_ITEMS_COUNT ()              { return 1; }
    static get VERTICAL_INDENT_FOR_WINNER_ITEM ()           { return 5; }
    
    //override
    get measuredHeight() //pseudo height
    {
        let lVisibleItemsHeight_num = !isNaN(this.scrollingPageSize) ? this.scrollingPageSize * this._fBetItemHeight_num : 0;
        return Math.max(this._fActiveBetInfos_arr.length*this._fBetItemHeight_num, lVisibleItemsHeight_num); 
    }

    //override
    moveTo(aValue_num) //pseudo scroll
    {
        if (!this._fActiveBetInfos_arr) return;

        let lCurrentStep_num = this._getValidStep(aValue_num, this._fBetItemHeight_num);

        this.y = this.initPosition.y - lCurrentStep_num % this._fBetItemHeight_num;
        let lTopIndex = Math.ceil( lCurrentStep_num / this._fBetItemHeight_num );
        let lBottomLimit = Math.max(this._fActiveBetInfos_arr.length - this._fScrollingPageSize_int, 0);
        if (lBottomLimit !== 0)
        {
            lBottomLimit += 1;
        }

        if (lTopIndex > lBottomLimit)
        {
            lTopIndex = lBottomLimit;
        }
        this._fTopItemIndex_int = lTopIndex;
        let lMinItems = Math.min(this._fActiveBetInfos_arr.length, this._fScrollingPageSize_int + BattlegroundBetsListScrollableContainer.NON_VISABLE_AREA_ITEMS_COUNT);

        let lWinnerMultiplier_num = this._winnerMultiplier;
        for (let i = 0; i < lMinItems; i++)
        {
            let lBetInfo_bi = this._fActiveBetInfos_arr[lTopIndex + i];
            if (lBetInfo_bi)
            {
                let lListItem = this._getBetsListItem(i);
                
                lListItem.globalId = lTopIndex + i;
                lListItem.update(lBetInfo_bi, lBetInfo_bi.multiplier === lWinnerMultiplier_num);
            }
        }
    }
    
    updateBets(aAllBetsInfos_bi_arr)
    {
        this._fActiveBetInfos_arr = aAllBetsInfos_bi_arr;

        this._redrawBets();
    }

    lockRedraws()
    {
        this._fIsLocked_bl = true;
    }

    unlockRedraws()
    {
        this._fIsLocked_bl = false;
    }

    _redrawBets(aUpdateLayout_bl=false)
    {
        let lAllBetsInfos_bi_arr = this._fActiveBetInfos_arr;

        let lMinItems = this._fIsLocked_bl ? 0 : Math.min(lAllBetsInfos_bi_arr.length, this._fScrollingPageSize_int);

        let lTopIndex = this._fTopItemIndex_int;
        if (lTopIndex >= lAllBetsInfos_bi_arr.length - this._fScrollingPageSize_int) lTopIndex = Math.max(lAllBetsInfos_bi_arr.length - this._fScrollingPageSize_int - 1, 0);

        let lWinnerMultiplier_num = this._winnerMultiplier;
        for (let i = 0; i < lMinItems; i++)
        {
            let lBetInfo_bi = lAllBetsInfos_bi_arr[i + lTopIndex];

            let lListItem = this._getBetsListItem(i);
            if (aUpdateLayout_bl)
            {
                lListItem.updateLayout(this._fBetsListWidth_num, this._fBetItemHeight_num);
            }
            lListItem.update(lBetInfo_bi, lBetInfo_bi.multiplier === lWinnerMultiplier_num);
            lListItem.show();
        }

        let lMaxItems_int = Math.max(this._fScrollingPageSize_int + BattlegroundBetsListScrollableContainer.NON_VISABLE_AREA_ITEMS_COUNT, this._fBetsListItems_bsli_arr.length);
        for (let i = lMinItems; i < lMaxItems_int; i++)
        {
            let lListItem = this._getBetsListItem(i);

            if (aUpdateLayout_bl)
            {
                lListItem.updateLayout(this._fBetsListWidth_num, this._fBetItemHeight_num);
            }
            lListItem.clear();
            if (i >= this._fScrollingPageSize_int)
            {
                lListItem.hide();
            }
        }

        this.emit(ScrollableContainer.EVENT_ON_CONTENT_UPDATED);
    }

    get _winnerMultiplier()
    {
        let lWinnerMultiplier_num = !!this._fActiveBetInfos_arr.length && this._fActiveBetInfos_arr[0].isEjected ? this._fActiveBetInfos_arr[0].multiplier : undefined;

        return lWinnerMultiplier_num;
    }

    updateLayout(aBetsListWidth_num, aScrollingPageSize_int, aOptBetsListHeight_num)
    {
        if (
                aBetsListWidth_num === this._fScrollingPageSize_int
                && aScrollingPageSize_int === this._fScrollingPageSize_int
                && aOptBetsListHeight_num === this._fBetItemHeight_num
            )
        {
            return;
        }

        this._fScrollingPageSize_int = aScrollingPageSize_int - 1;
        this._fBetsListWidth_num = aBetsListWidth_num;
        this._fBetItemHeight_num = aOptBetsListHeight_num ? aOptBetsListHeight_num : BattlegroundBetsListItem.ITEM_HEIGHT;

        this._redrawBets(true);
    }

    get scrollingPageSize()
    {
        return this._fScrollingPageSize_int;
    }

    get topItemIndex()
    {
        return this._fTopItemIndex_int;
    }

    get itemHieght()
    {
        return this._fBetItemHeight_num;
    }

    constructor()
    {
        super();
        
        this._fScrollingPageSize_int = undefined;
        this._fBetsListWidth_num = undefined;
        this._fBetsListItems_bsli_arr = [];
        this._fActiveBetInfos_arr = [];
        this._fTopItemIndex_int = 0;
        this._fBetItemHeight_num = 0;
        this._fIsLocked_bl = false;
    }
    
    _getBetsListItem(aIndex_int)
    {
		if (!this._fBetsListItems_bsli_arr[aIndex_int])
        {
            let lBetsListItem_bsli = this.addChild(new BattlegroundBetsListItem(aIndex_int));
            
            lBetsListItem_bsli.position.set(0, aIndex_int * this._fBetItemHeight_num);
            this._fBetsListItems_bsli_arr[aIndex_int] = lBetsListItem_bsli;
        }
        else
        {
            this._fBetsListItems_bsli_arr[aIndex_int].position.y = aIndex_int * this._fBetItemHeight_num;
        }
        return this._fBetsListItems_bsli_arr[aIndex_int];
    }

    destroy()
    {
        if (this._fBetsListItems_bsli_arr)
        {
            while (this._fBetsListItems_bsli_arr.length)
            {
                this._fBetsListItems_bsli_arr.pop().destroy();
            }
        }
        this._fBetsListItems_bsli_arr = null;
        this._fBetsListWidth_num = null;

        super.destroy();
    }
}
export default BattlegroundBetsListScrollableContainer;