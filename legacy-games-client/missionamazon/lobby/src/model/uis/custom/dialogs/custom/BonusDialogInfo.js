import DialogInfo from '../DialogInfo';
import BonusInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';


class BonusDialogInfo extends DialogInfo {

	static get MESSAGE_BONUS_EXPIRED() 			{ return BonusInfo.MESSAGE_BONUS_EXPIRED; }
	static get MESSAGE_BONUS_CANCELLED()		{ return BonusInfo.MESSAGE_BONUS_CANCELLED; }
	static get MESSAGE_BONUS_RELEASED()			{ return BonusInfo.MESSAGE_BONUS_RELEASED; }
	static get MESSAGE_BONUS_LOST()				{ return BonusInfo.MESSAGE_BONUS_LOST; }
	static get MESSAGE_BONUS_LOBBY_INTRO()		{ return BonusInfo.MESSAGE_BONUS_LOBBY_INTRO; }
	static get MESSAGE_BONUS_ROOM_INTRO()		{ return BonusInfo.MESSAGE_BONUS_ROOM_INTRO; }

	constructor(dialogId, priority) {
		super(dialogId, priority);

		this._fMessageType_str = undefined;
		this._fNextModeFRB_bl = false; // hasNextFrb
		//[Y]TODO add free shots count, win sum, etc...
	}

	get isLobbyIntroType()
	{
		return this.messageType === BonusDialogInfo.MESSAGE_BONUS_LOBBY_INTRO;
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

export default BonusDialogInfo