import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import GamePlayerInfo from './GamePlayerInfo';
import BetsInfo from '../bets/BetsInfo';

class GamePlayersInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fPlayers_gpi_arr = [];
		this._fMasterSeatId_str = undefined;
		this._fMasterObserverId_str = undefined;
		this._fBetsInfo_bsi = null;
		this._fIsMasterPlayerLeaveRoomTriggered_bl = false;
		this._fRoundInfo_ri = null;
		this._fIsReSitInRequired_bl = false;
		this._isCAFRoomManager = false;
	}

	get masterSeatId()
	{
		return this._fMasterSeatId_str;
	}

	get observerId()
	{
		return this._fMasterObserverId_str;
	}

	set observerId(value)
	{
		this._fMasterObserverId_str = value;
	}

	get isMasterSeatDefined()
	{
		return this._fMasterSeatId_str !== undefined;
	}

	resetMasterSeat()
	{
		this._fMasterSeatId_str = undefined;
	}

	get isMasterPlayerLeaveRoomTriggered()
	{
		return this._fIsMasterPlayerLeaveRoomTriggered_bl;
	}

	set isMasterPlayerLeaveRoomTriggered(value)
	{
		this._fIsMasterPlayerLeaveRoomTriggered_bl = value;
	}

	get betsInfo()
	{
		return this._fBetsInfo_bsi || (this._fBetsInfo_bsi = this._generateBetsInfo());
	}

	get roundInfo()
	{
		return this._fRoundInfo_ri || (this._fRoundInfo_ri = this.gameplayInfo.roundInfo)
	}

	get gameplayInfo()
	{
		return this.i_getParentInfo();
	}
	 get isCAFRoomManager()
	 {
		return this._isCAFRoomManager;
	 }

	setPlayers(aSeatsServerData_obj_arr, isOwner)
	{
		this._isCAFRoomManager = isOwner; 
		let lActualSeats_gpi_arr = [];
		for (let i=0; i<aSeatsServerData_obj_arr.length; i++)
		{
			let lSeatServerData_obj = aSeatsServerData_obj_arr[i];
			let lCurSeatInfo_gpi;
			if (this.getPlayerInfo(lSeatServerData_obj.nickname))
			{
				lCurSeatInfo_gpi = this._updatePlayerInfo(lSeatServerData_obj);
			}
			else
			{
				lCurSeatInfo_gpi = this._addPlayerInfo(lSeatServerData_obj);
			}

			lActualSeats_gpi_arr.push(lCurSeatInfo_gpi);
		}

		for (let i=0; i<this._fPlayers_gpi_arr.length; i++)
		{
			let lCurExistSeatInfo_gpi = this._fPlayers_gpi_arr[i];
			let lIsExistSeatActual_bl = false;
			for (let j=0; j<lActualSeats_gpi_arr.length; j++)
			{
				let lCurActualSeatInfo_gpi = lActualSeats_gpi_arr[j];
				if (lCurActualSeatInfo_gpi.seatId == lCurExistSeatInfo_gpi.seatId)
				{
					lIsExistSeatActual_bl = true;
					break;
				}
			}

			if (!lIsExistSeatActual_bl)
			{
				this._removePlayerInfo(lCurExistSeatInfo_gpi.seatId);
				i--;
			}
		}
	}

	addMasterPlayerInfo(aSeatServerData_obj)
	{
		this._fMasterSeatId_str = aSeatServerData_obj.nickname;

		if (!!this.getPlayerInfo(aSeatServerData_obj.nickname)) // re-sitIn
		{
			return this._updatePlayerInfo(aSeatServerData_obj);
		}

		return this._addPlayerInfo(aSeatServerData_obj);
	}

	addPlayerInfo(aSeatServerData_obj)
	{
		return this._addPlayerInfo(aSeatServerData_obj);
	}

	removePlayerInfo(aSeatId_num)
	{
		if (aSeatId_num === this.masterSeatId)
		{
			this._fMasterSeatId_str = undefined;
		}

		this._removePlayerInfo(aSeatId_num);
	}

	updatePlayerInfo(aSeatServerData_obj)
	{
		return this._updatePlayerInfo(aSeatServerData_obj);
	}

	get masterPlayerInfo()
	{
		if (!this.isMasterSeatDefined)
		{
			return null;
		}
		return this.getPlayerInfo(this.masterSeatId);
	}

	get isReSitInRequired()
	{
		return this._fIsReSitInRequired_bl;
	}

	set isReSitInRequired(value)
	{
		this._fIsReSitInRequired_bl = value;
	}

	getPlayerInfo(aSeatId_num)
	{
		let lPlayers_gpi_arr = this._fPlayers_gpi_arr;

		if (!lPlayers_gpi_arr.length)
		{
			return null;
		}

		for (let i=0; i<lPlayers_gpi_arr.length; i++)
		{
			let lCur_gpi = lPlayers_gpi_arr[i];
			if (lCur_gpi.seatId === aSeatId_num)
			{
				return lCur_gpi;
			}
		}

		return null;
	}

	_addPlayerInfo(aSeatServerData_obj)
	{
		if (!!this.getPlayerInfo(aSeatServerData_obj.nickname))
		{
			throw new Error(`Player already exists: ${aSeatServerData_obj.nickname}.`);
			return null;
		}

		this._generatePlayerInfo(aSeatServerData_obj.nickname);

		return this._updatePlayerInfo(aSeatServerData_obj);
	}

	_removePlayerInfo(aSeatId_num)
	{
		let lPlayers_gpi_arr = this._fPlayers_gpi_arr;
		for (let i=0; i<lPlayers_gpi_arr.length; i++)
		{
			let lCur_gpi = lPlayers_gpi_arr[i];
			if (lCur_gpi.seatId === aSeatId_num)
			{
				lCur_gpi.destroy();
				lPlayers_gpi_arr.splice(i, 1);
				break;
			}
		}
	}

	_updatePlayerInfo(aSeatServerData_obj)
	{
		let l_gpi = this.getPlayerInfo(aSeatServerData_obj.nickname);
		if (!this.getPlayerInfo(aSeatServerData_obj.nickname))
		{
			throw new Error(`Player does not exist: ${aSeatServerData_obj.nickname}.`);
			return null;
		}

		l_gpi.nickName = aSeatServerData_obj.nickname;

		return l_gpi;
	}

	_generatePlayerInfo(aSeatId_num)
	{
		let l_gpi = new GamePlayerInfo(this);
		l_gpi.seatId = aSeatId_num;

		this._fPlayers_gpi_arr.push(l_gpi);

		return l_gpi;
	}

	_generateBetsInfo()
	{
		let lBetsInfo_bsi = new BetsInfo(this);
		return lBetsInfo_bsi;
	}

	destroy()
	{
		while (this._fPlayers_gpi_arr && this._fPlayers_gpi_arr.length)
		{
			this._fPlayers_gpi_arr.pop().destroy();
		}
		this._fPlayers_gpi_arr = null;

		this._fMasterSeatId_str = undefined;
		
		this._fBetsInfo_bsi && this._fBetsInfo_bsi.destroy();
		this._fBetsInfo_bsi = null;

		this._fIsMasterPlayerLeaveRoomTriggered_bl = undefined;

		this._fIsReSitInRequired_bl = undefined;
		this._isCAFRoomManager = undefined;

		super.destroy();
	}
}

export default GamePlayersInfo