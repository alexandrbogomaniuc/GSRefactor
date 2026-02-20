import VerticalScrollableContainer from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollableContainer';
import ScrollableContainer from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/ScrollableContainer';
import BetsListItem from './BetsListItem';

class BetsListScrollableContainer extends VerticalScrollableContainer
{
    static get NON_VISABLE_AREA_ITEMS_COUNT ()     { return 1; }
    
    //override
    get measuredHeight() //pseudo height
    {
        let lVisibleItemsHeight_num = !isNaN(this.scrollingPageSize) ? this.scrollingPageSize * BetsListItem.ITEM_HEIGHT : 0;
        return Math.max(this._fActiveBets_arr.length*BetsListItem.ITEM_HEIGHT, lVisibleItemsHeight_num); 
    }

    //override
    moveTo(aValue_num) //pseudo scroll
    {
        if (!this._fActiveBets_arr) return;

        let lCurrentStep_num = this._getValidStep(aValue_num, BetsListItem.ITEM_HEIGHT);

        this.y = this.initPosition.y - lCurrentStep_num % BetsListItem.ITEM_HEIGHT;
        let lTopIndex = Math.ceil( lCurrentStep_num / BetsListItem.ITEM_HEIGHT );
        let lBottomLimit = Math.max(this._fActiveBets_arr.length - this._fScrollingPageSize_int, 0);
        if (lBottomLimit !== 0)
        {
            lBottomLimit += 1;
        }

        if (lTopIndex > lBottomLimit)
        {
            lTopIndex = lBottomLimit;
        }
        this._fTopItemIndex_int = lTopIndex;
        let lMinItems = Math.min(this._fActiveBets_arr.length, this._fScrollingPageSize_int + BetsListScrollableContainer.NON_VISABLE_AREA_ITEMS_COUNT);
        for (let i = 0; i < lMinItems; i++)
        {
            let lBetInfo_bi = this._fActiveBets_arr[lTopIndex + i];
            if (lBetInfo_bi)
            {
                let lListItem = this._getBetsListItem(i);
                let lPayout_num = lBetInfo_bi.isBetWinDefined ? lBetInfo_bi.betWin : Math.floor(lBetInfo_bi.betAmount * lBetInfo_bi.multiplier);
                let lMultiplier_num = lBetInfo_bi.isEjected ? lBetInfo_bi.multiplier : undefined;
                lListItem.globalId = lTopIndex + i;
                lListItem.update(lBetInfo_bi.playerName, lBetInfo_bi.betAmount, lBetInfo_bi.isMasterBet, lPayout_num, lMultiplier_num);
                
            }
        }
    }

    updateBets(aAllBetsInfos_bi_arr)
    {
        this._fActiveBets_arr = aAllBetsInfos_bi_arr;
        let lMinItems = Math.min(aAllBetsInfos_bi_arr.length, this._fScrollingPageSize_int + BetsListScrollableContainer.NON_VISABLE_AREA_ITEMS_COUNT);
        let lTopIndex = this._fTopItemIndex_int ? this._fTopItemIndex_int - 1 : this._fTopItemIndex_int;
        if (lTopIndex >= aAllBetsInfos_bi_arr.length - this._fScrollingPageSize_int)
        {
            lTopIndex = Math.max(aAllBetsInfos_bi_arr.length - this._fScrollingPageSize_int - 1, 0);
        } 
        
        for (let i = 0; i < lMinItems; i++)
        {
            let lBetInfo_bi = aAllBetsInfos_bi_arr[i + lTopIndex];
            let lMultiplier_num = lBetInfo_bi.isEjected ? lBetInfo_bi.multiplier : undefined;
            let lPayout_num = lBetInfo_bi.isBetWinDefined ? lBetInfo_bi.betWin : Math.floor(lBetInfo_bi.betAmount * lBetInfo_bi.multiplier);

            let lListItem = this._getBetsListItem(i);

            lListItem.update(lBetInfo_bi.playerName, lBetInfo_bi.betAmount, lBetInfo_bi.isMasterBet, lPayout_num, lMultiplier_num);
            lListItem.show();
            
            if (lBetInfo_bi.isEjected)
            {
                lListItem.showGreenLight(true);
            }
            else
            {
                lListItem.showGreenLight(false);
            }
        }

        let lMaxItems_int = Math.max(this._fScrollingPageSize_int + BetsListScrollableContainer.NON_VISABLE_AREA_ITEMS_COUNT, this._fBetsListItems_bsli_arr.length);
        for (let i = lMinItems; i < lMaxItems_int; i++)
        {
            let lListItem = this._getBetsListItem(i);
            if (lListItem)
            {
                lListItem.clear();
                lListItem.showGreenLight(false);
                if (i >= this._fScrollingPageSize_int)
                {
                    lListItem.hide();
                }
            }
        }

        this.emit(ScrollableContainer.EVENT_ON_CONTENT_UPDATED);
    }

    updateLayout(aBetsListWidth_num, aScrollingPageSize_int)
    {
        this._fScrollingPageSize_int = aScrollingPageSize_int;
        this._fBetsListWidth_num = aBetsListWidth_num;

        

        let lUpdateBetAmount_int = Math.max(this._fBetsListItems_bsli_arr.length, aScrollingPageSize_int);
        
        for (let i = 0; i < lUpdateBetAmount_int; i++)
        {
            let lListItem = this._getBetsListItem(i);
            lListItem.updateItemWidth(aBetsListWidth_num);
            if (i < this._fScrollingPageSize_int)
            {
                lListItem.globalId = i;
                lListItem.show();
               
            }
            else
            {
                lListItem.hide();
            }
        }
        this.emit(ScrollableContainer.EVENT_ON_CONTENT_UPDATED);
    }

    get scrollingPageSize()
    {
        return this._fScrollingPageSize_int;
    }

    constructor()
    {
        super();

        this._fScrollingPageSize_int = undefined;
        this._fBetsListWidth_num = undefined;
        this._fBetsListItems_bsli_arr = [];
        this._fActiveBets_arr = [];
        this._fTopItemIndex_int = 0;
    }
    
    _getBetsListItem(aIndex_int)
    {
        if (!this._fBetsListItems_bsli_arr[aIndex_int])
        {
            let lBetsListItem_bsli = this.addChild(new BetsListItem(aIndex_int));
            if (this._fBetsListWidth_num !== undefined)
            {
                lBetsListItem_bsli.updateItemWidth(this._fBetsListWidth_num);
            }
            lBetsListItem_bsli.position.set(0, aIndex_int * BetsListItem.ITEM_HEIGHT);
            this._fBetsListItems_bsli_arr[aIndex_int] = lBetsListItem_bsli;
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

export default BetsListScrollableContainer