import DialogInfo from '../DialogInfo';
import FRBInfo from '../../../../custom/frb/FRBInfo';


class FRBDialogInfo extends DialogInfo {

	static get MESSAGE_FRB_EXPIRED() 		{ return FRBInfo.MESSAGE_FRB_EXPIRED; }
	static get MESSAGE_FRB_CANCELLED()		{ return FRBInfo.MESSAGE_FRB_CANCELLED; }
	static get MESSAGE_FRB_FINISHED()		{ return FRBInfo.MESSAGE_FRB_FINISHED; }
	static get MESSAGE_FRB_LOBBY_INTRO()	{ return FRBInfo.MESSAGE_FRB_LOBBY_INTRO; }
	static get MESSAGE_FRB_ROOM_INTRO()		{ return FRBInfo.MESSAGE_FRB_ROOM_INTRO; }

	constructor(dialogId, priority) {
		super(dialogId, priority);

		this._fMessageType_str = undefined;
		this._fNextModeFRB_bl = false; // hasNextFrb
		//[Y]TODO add free shots count, win sum, etc...
	}

	get isLobbyIntroType()
	{
		return this.messageType === FRBDialogInfo.MESSAGE_FRB_LOBBY_INTRO;
	}

	set messageType(aValue_str)
	{
		this._fMessageType_str = aValue_str;
	}

	get messageType()
	{
		return this._fMessageType_str;
	}

	set nextModeFRB(aValue_bl)
	{
		this._fNextModeFRB_bl = aValue_bl;
	}

	get nextModeFRB()
	{
		return this._fNextModeFRB_bl;
	}
}

export default FRBDialogInfo