import GUSExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from './GUSExternalCommunicator';

/** @ignore */
class GUSGameExternalCommunicator extends GUSExternalCommunicator
{
	static get LOBBY_MESSAGE_RECEIVED()
	{
		return "onLobbyMessageReceived";
	}

	constructor()
	{
		super();
	}

	handleMessage(type, data)
	{
		if (!this.__isMessageTypeSuppotred(type))
		{
			return;
		}

		super.handleMessage(type, data);

		this.emit(GUSGameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, { type: type, data: data });
	}

	__isMessageTypeSuppotred(type)
	{
		return super.__isMessageTypeSuppotred(type) || this.__isCustomLobbyMessageTypeSupported(type);
	}

	__isCustomLobbyMessageTypeSupported(type)
	{
		for (let lType_str in LOBBY_MESSAGES)
		{
			if (LOBBY_MESSAGES[lType_str] === type)
			{
				return true;
			}
		}

		return false;
	}
}

export {GAME_MESSAGES, LOBBY_MESSAGES}
export default GUSGameExternalCommunicator;