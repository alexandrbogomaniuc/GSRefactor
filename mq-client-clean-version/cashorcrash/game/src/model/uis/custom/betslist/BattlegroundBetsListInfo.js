import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class BattlegroundBetsListInfo extends SimpleUIInfo
{
    get betsCount()
    {
        return this._fBetsCount_int;
    }

    get betsTotalSum()
    {
        return this._fBetsTotalSum_num;
    }

    get allBets()
    {
        return this._fBetInfos_bi_arr;
    }

    clearBetsData()
    {
        this._fBetInfos_bi_arr = [];
        this._fBetsCount_int = 0;
        this._fBetsTotalSum_num = 0;
    }

    setBetsData(aBetInfos_bi_arr)
    {
        this.clearBetsData();
        if (aBetInfos_bi_arr)
        {
            for (let i = 0; i < aBetInfos_bi_arr.length; i++)
            {
                this.addBetData(aBetInfos_bi_arr[i])
            }
        }
    }

    updateBetsData(aBetInfo_bi)
    {
        if (aBetInfo_bi)
        {
            if (aBetInfo_bi.isDeactivatedBet || aBetInfo_bi.isEjected)
            {
                this.removeBetData(aBetInfo_bi)
            }
    
            if (!aBetInfo_bi.isDeactivatedBet)
            {
                this.addBetData(aBetInfo_bi)
            } 
        }
    }

    addBetData(aBetInfo_bi)
    {
        if (this._fBetsCount_int == 0)
        {
            this._fBetInfos_bi_arr.push(aBetInfo_bi);
        }
        else
        {
            this._sortingBetsList(aBetInfo_bi);
        }

        this._fBetsTotalSum_num += aBetInfo_bi.betAmount;
        this._fBetsCount_int++;
    }

    removeBetData(aBetInfo_bi)
    {
        let lIndex_int = 0;
        for (lIndex_int; lIndex_int < this._fBetInfos_bi_arr.length; lIndex_int++)
        {
            let lCur_bi = this._fBetInfos_bi_arr[lIndex_int];
            if (lCur_bi.betId === aBetInfo_bi.betId)
            {
                this._fBetInfos_bi_arr.splice(lIndex_int, 1);
                this._fBetsCount_int--;
                this._fBetsTotalSum_num -= aBetInfo_bi.betAmount;
                break;
            }
        }
    }

    get isBetsListModified()
    {
        return this._fIsBetsListModified_bl;
    }

    set isBetsListModified(value)
    {
        this._fIsBetsListModified_bl = value;
    }

    get isBetsListAdjustLayoutRequired()
    {
        return this._fIsBetsListAdjustLayoutRequired_bl;
    }

    set isBetsListAdjustLayoutRequired(value)
    {
        this._fIsBetsListAdjustLayoutRequired_bl = value;
    }

	constructor()
	{
		super();

		this._init();
	}

	_init()
	{
        this._fBetInfos_bi_arr = [];
        this._fBetsCount_int = 0;
        this._fBetsTotalSum_num = 0;
        this._fMasterTotalWin_num = 0;

        this._fIsBetsListModified_bl = false;
        this._fIsBetsListAdjustLayoutRequired_bl = false;
	}

    _sortingBetsList(aBetInfo_bi)
    {
        let lIndex_int = 0;
        let lPrevBetInfosCount_int = this._fBetInfos_bi_arr.length;

        while (lIndex_int < lPrevBetInfosCount_int && this._compareBetInfos(this._fBetInfos_bi_arr[lIndex_int], aBetInfo_bi)) 
        {
            lIndex_int++;
        }
        for (let j = lPrevBetInfosCount_int; j > lIndex_int; j--)
        {
            this._fBetInfos_bi_arr[j] = this._fBetInfos_bi_arr[j-1];
        }
        this._fBetInfos_bi_arr[lIndex_int] = aBetInfo_bi;   
    }

    _compareBetInfos(aFirstInfo_bi, aSecondInfo_bi)
    {
        let lFirstWeight_num = aFirstInfo_bi.isEjected ? aFirstInfo_bi.multiplier : 1;
        let lSecondWeight_num = aSecondInfo_bi.isEjected ? aSecondInfo_bi.multiplier : 0;
        if (lFirstWeight_num === lSecondWeight_num && !aFirstInfo_bi.isEjected)
        {
            return !aSecondInfo_bi.isEjected;
        }
        return (lFirstWeight_num >= lSecondWeight_num);
    }

	destroy()
	{
		super.destroy();

        this._fBetInfos_bi_arr = null;
        this._fBetsCount_int = null;
        this._fBetsTotalSum_num = null;
        this._fMasterTotalWin_num = null;

        this._fIsBetsListModified_bl = undefined;
        this._fIsBetsListAdjustLayoutRequired_bl = undefined;
	}
}

export default BattlegroundBetsListInfo