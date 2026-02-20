import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class RoundDetailsInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._init();
	}

    set roundStartTime(aValue_num)
    {
        this._fRoundStartTime_num = aValue_num;
    }

    get roundStartTime()
    {
        return this._fRoundStartTime_num;
    }

    set roundId(aValue_num)
    {
        this._fRoundId_num = aValue_num;
    }

    get roundId()
    {
        return this._fRoundId_num;
    }

    set betsCount(aValue_int)
    {
        this._fBetsCount_int = aValue_int;
    }

    get betsCount()
    {
        return this._fBetsCount_int;
    }

    set multiplier(aValue_num)
    {
        this._fMultiplier_num = aValue_num;
    }

    get multiplier()
    {
        return this._fMultiplier_num;
    }

    set uniqueToken(aValue_str)
    {
        this._fUniqueToken_str = aValue_str;
    }

    get uniqueToken()
    {
        return this._fUniqueToken_str;
    }

    set currentRoundId(aValue_num)
    {
        this._fCurrentRoundId_num = aValue_num;
    }

    get currentRoundId()
    {
        return this._fCurrentRoundId_num;
    }

	_init ()
	{
		this._fRoundStartTime_num = null;
        this._fRoundId_num = null;
        this._fBetsCount_int = null;
        this._fMultiplier_num = null;
        this._fUniqueToken_str = null;
        this._fCurrentRoundId_num = null;
	}

	destroy()
	{
		super.destroy();
		
		this._fRoundStartTime_num = null;
        this._fRoundId_num = null;
        this._fBetsCount_int = null;
        this._fMultiplier_num = null;
        this._fUniqueToken_str = null;
        this._fCurrentRoundId_num = null;
	}
}

export default RoundDetailsInfo