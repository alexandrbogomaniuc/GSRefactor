import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BetInfo extends SimpleInfo
{
	static generateBetId(aBetIndex_num, aSeatId_num)
	{
		let lTime_num = APP.appClientServerTime;

		return `${lTime_num}_${aBetIndex_num}_${aSeatId_num}`;
	}

	static extractBetIndex(aBetId_str)
	{
		let lParts_str_arr = aBetId_str.split("_");

		return +(lParts_str_arr[1]);
	}

	static extractSeatId(aBetId_str)
	{
		let lParts_str_arr = aBetId_str.split("_");

		let lSeatId_str = lParts_str_arr[2];
		if (lParts_str_arr.length > 3)
		{
			for (let i=3; i<lParts_str_arr.length; i++)
			{
				lSeatId_str += "_" + lParts_str_arr[i];
			}
		}

		return lSeatId_str;
	}

	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		// place bet request: 
		// {"class":"CrashCancelBet","crashBetId":"1644481639662_0","rid":362,"date":1644481656248}
		
		// place bet confirmed: 
		// {"currentMult":1.28,"seatId":0,"seatWin":1,"crashBetId":"1644481639662_0","name":"User1415787","date":1644481656269,"rid":-1,"class":"CrashCancelBetResponse"}

		// place bet request (auto on):
		// {"class":"CrashBet","crashBetAmount":1,"autoPlay":true,"multiplier":5,"rid":144,"date":1644485505147}

		// place bet confirmed (auto on):
		// {"crashBetAmount":1,"balance":9999977,"crashBetKey":"1644485505206_0","date":1644485505207,"rid":144,"class":"CrashBetResponse"}

		// manual eject:
		// {"class":"CrashCancelBet","crashBetId":"1644481639662_0","rid":362,"date":1644481656248}
		// {"currentMult":1.28,"seatId":0,"seatWin":1,"crashBetId":"1644481639662_0","name":"User1415787","date":1644481656269,"rid":-1,"class":"CrashCancelBetResponse"}

		// in bets: 
		// {amount: 1, auto: false, betId: "1644481638962_0", ejectTime: 0, mult: 0, name: "User1415787"},

		this._fBetId_str = undefined;
		this._fAmount_num = undefined;
		this._fIsAutoEject_bl = false;
		this._fPlaceBetTime_num = 0;
		this._fDeactivateBetTime_num = 0;
		this._fEjectTime_num = 0;
		this._fMultiplier_num = 0;
		this._fSeatId_str = undefined;
		this._fBetIndex_num = undefined;
		this._fAutoEjectMultiplier_num = undefined;
		this._fIsConfirmedMasterBet_bl = false;
		this._fIsPossiblyDeactivatedByServerMasterBet_bl = false;
		this._fIsExternallyConfirmedMasterBet_bl = false;
		this._fBetWin_num = 0;
		this._fIsAutoEjectCancelledInRound_bl = false;
	}

	set betId(value)
	{
		this._fBetId_str = value;

		let lParts_str_arr = value.split("_");

		this._fBetIndex_num = BetInfo.extractBetIndex(value);
		this._fSeatId_str = BetInfo.extractSeatId(value);
	}

	get betId()
	{
		return this._fBetId_str;
	}

	get betIndex()
	{
		return this._fBetIndex_num;
	}

	get seatId()
	{
		return this._fSeatId_str;
	}

	get playerName()
	{
		return this.seatId;
	}

	get isMasterBet()
	{
		let lPlayersInfo_gpsi = this.i_getParentInfo().gamePlayersInfo;
		let lMasterPlayerInfo_gpi = lPlayersInfo_gpsi.masterPlayerInfo;
		let lObserverId_str = lPlayersInfo_gpsi.observerId;
		let lMasterBets_bi_arr = !!lMasterPlayerInfo_gpi ? lMasterPlayerInfo_gpi.bets
														: !!lObserverId_str ? APP.gameController.gameplayController.gamePlayersController.betsController.info.getPlayerBets(lObserverId_str) : null;
		if (lMasterBets_bi_arr)
		{
			if (lMasterBets_bi_arr.includes(this))
			{
				return true;
			}
		}
		return lPlayersInfo_gpsi.isMasterSeatDefined && lPlayersInfo_gpsi.masterSeatId == this.seatId;
	}

	set isConfirmedMasterBet(value)
	{
		this._fIsConfirmedMasterBet_bl = value;
	}

	get isConfirmedMasterBet()
	{
		return this._fIsConfirmedMasterBet_bl;
	}

	set isExternallyConfirmedMasterBet(value)
	{
		this._fIsExternallyConfirmedMasterBet_bl = value;
	}

	get isExternallyConfirmedMasterBet()
	{
		return this._fIsExternallyConfirmedMasterBet_bl;
	}

	set isPossiblyDeactivatedByServerMasterBet(value)
	{
		this._fIsPossiblyDeactivatedByServerMasterBet_bl = value;
	}

	get isPossiblyDeactivatedByServerMasterBet()
	{
		return this._fIsPossiblyDeactivatedByServerMasterBet_bl;
	}

	set betAmount(value)
	{
		this._fAmount_num = value;
	}

	get betAmount()
	{
		return this._fAmount_num;
	}

	set isAutoEject(value)
	{
		this._fIsAutoEject_bl = value;
	}

	get isAutoEject()
	{
		return this._fIsAutoEject_bl;
	}

	set isAutoEjectCancelledInRound(value)
	{
		this._fIsAutoEjectCancelledInRound_bl = value;
	}

	get isAutoEjectCancelledInRound()
	{
		return this._fIsAutoEjectCancelledInRound_bl;
	}

	set ejectTime(value)
	{
		this._fEjectTime_num = value;
	}

	get ejectTime()
	{
		return this._fEjectTime_num;
	}

	set placeBetTime(value)
	{
		this._fPlaceBetTime_num = value;
	}

	get placeBetTime()
	{
		return this._fPlaceBetTime_num;
	}

	set deactivateBetTime(value)
	{
		this._fDeactivateBetTime_num = value;
	}

	get deactivateBetTime()
	{
		return this._fDeactivateBetTime_num;
	}

	get isEjected()
	{
		return this.ejectTime > 0;
	}

	set multiplier(value)
	{
		this._fMultiplier_num = value;
	}

	get multiplier()
	{
		return this._fMultiplier_num;
	}

	set autoEjectMultiplier(value)
	{
		this._fAutoEjectMultiplier_num = value;
	}

	get autoEjectMultiplier()
	{
		return this._fAutoEjectMultiplier_num;
	}
	
	set betWin(value)
	{
		this._fBetWin_num = value;
	}

	get betWin()
	{
		return this._fBetWin_num;
	}

	get isBetWinDefined()
	{
		return this._fBetWin_num > 0;
	}

	get isDeactivatedBet()
	{
		return this.betWin > 0 && !this.ejectTime;
	}

	clone()
	{
		let lBetInfo_bi = new BetInfo(this.i_getParentInfo());

		lBetInfo_bi.betId = BetInfo.generateBetId(this.betIndex, this.seatId)
		lBetInfo_bi.betAmount = this.betAmount;
		lBetInfo_bi.isAutoEject = this.isAutoEject;
		lBetInfo_bi.ejectTime = this.ejectTime;
		lBetInfo_bi.multiplier = this.multiplier;
		lBetInfo_bi.autoEjectMultiplier = this.autoEjectMultiplier;
		lBetInfo_bi.isConfirmedMasterBet = this.isConfirmedMasterBet;
		lBetInfo_bi.isExternallyConfirmedMasterBet = this.isExternallyConfirmedMasterBet;
		lBetInfo_bi.betWin = this.betWin;
		
		return lBetInfo_bi;
	}

	destroy()
	{
		this._fBetId_str = undefined;
		this._fAmount_num = undefined;
		this._fIsAutoEject_bl = undefined;
		this._fEjectTime_num = undefined;
		this._fPlaceBetTime_num = undefined;
		this._fDeactivateBetTime_num = undefined;
		this._fMultiplier_num = undefined;
		this._fSeatId_str = undefined;
		this._fBetIndex_num = undefined;
		this._fAutoEjectMultiplier_num = undefined;
		this._fIsConfirmedMasterBet_bl = undefined;
		this._fIsExternallyConfirmedMasterBet_bl = undefined;
		this._fIsPossiblyDeactivatedByServerMasterBet_bl = undefined;
		this._fBetWin_num = undefined;
		this._fIsAutoEjectCancelledInRound_bl = false;
		
		super.destroy();
	}
}

export default BetInfo