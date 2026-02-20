import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

export const ROOM_STATES = 
{
	OPENING: 	"OPENING",
	OPENED: 	"OPENED",
	CLOSING: 	"CLOSING",
	CLOSED: 	"CLOSED"
}

class RoomInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fRoomId_num = undefined;
		this._fRoomState_str = undefined;
		this._fRakePercent_int = 0;
		this._frineds = [];
		this._observers = [];
		this._isKicked = false;
		this._minSeats = 1000;
		this._maxRoomPlayers = 50;
	}

	set maxRoomPlayers(aValue_num)
	{
		this._maxRoomPlayers = aValue_num;
	}

	get maxRoomPlayers()
	{
		return this._maxRoomPlayers;
	}


	set isKickedOutOfTheRoom(value)
	{
		this._isKicked = value;
	}

	get isKickedOutOfTheRoom()
	{
		return this._isKicked;
	}	


	set roomId(value)
	{
		this._fRoomId_num = value;
	}

	set minSeats(aValue_num)
	{
		this._minSeats = aValue_num;
	}

	get roomId()
	{
		return this._fRoomId_num;
	}	

	set rakePercent(aValue_num)
	{
		this._fRakePercent_int = aValue_num;
	}

	get rakePercent()
	{
		return this._fRakePercent_int;
	}

	get isRoomIdDefined()
	{
		return this.roomId !== undefined;
	}

	set roomState(value)
	{
		this._fRoomState_str = value;
	}

	get roomState()
	{
		return this._fRoomState_str;
	}

	get isRoomStateDefined()
	{
		return this.roomState !== undefined;
	}

	resetRoomState()
	{
		this.roomState = undefined;
	}

	get isRoomOpeningState()
	{
		return this.roomState === ROOM_STATES.OPENING;
	}

	get isRoomOpened()
	{
		return this.roomState === ROOM_STATES.OPENED;
	}

	get isRoomClosingState()
	{
		return this.roomState === ROOM_STATES.CLOSING;
	}

	get isRoomClosed()
	{
		return this.roomState === ROOM_STATES.CLOSED;
	}

	set observers(data)
	{
		this._observers = data;
	}

	get pendingInvite()
	{
		return this._pendingInvite;
	}

	set pendingInvite(aValue_bull)
	{
		this._pendingInvite = aValue_bull;
	}

	set friends(data){
		this._frineds = data;
	}

	get friends(){
		return this._frineds;
	}

	get observers(){
		return this._observers;
	}

	get minSeats()
	{
		return this._minSeats;
	}

	destroy()
	{
		this._fRoomId_num = undefined;
		this._fRoomState_str = undefined;
		
		super.destroy();
	}
}
export default RoomInfo;