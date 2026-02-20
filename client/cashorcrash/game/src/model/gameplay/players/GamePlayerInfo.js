import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class GamePlayerInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fSeatId_num = undefined;
		this._fNickName_str = undefined;
	}

	set seatId(value)
	{
		this._fSeatId_num = value;
	}

	get seatId()
	{
		return this._fSeatId_num;
	}

	set nickName(value)
	{
		this._fNickName_str = value;
	}

	get nickName()
	{
		return this._fNickName_str;
	}

	get isMaster()
	{
		return this.seatId === this.i_getParentInfo().masterSeatId;
	}

	get bets()
	{
		return this.i_getParentInfo().betsInfo.getPlayerBets(this.seatId);
	}

	get activeBets()
	{
		return this.i_getParentInfo().betsInfo.getPlayerActiveBets(this.seatId);
	}

	destroy()
	{
		this._fSeatId_num = undefined;
		this._fNickName_str = undefined;

		super.destroy();
	}
}

export default GamePlayerInfo