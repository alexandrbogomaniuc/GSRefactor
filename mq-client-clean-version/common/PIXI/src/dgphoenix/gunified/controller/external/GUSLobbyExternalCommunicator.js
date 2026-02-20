import GUSExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from './GUSExternalCommunicator';

/** @ignore */
class GUSLobbyExternalCommunicator extends GUSExternalCommunicator
{
	static get GAME_MESSAGE_RECEIVED()
	{
		return "onGameMessageReceived";
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

		this.emit(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, { type: type, data: data });
	}

	__isMessageTypeSuppotred(type)
	{
		return super.__isMessageTypeSuppotred(type) || this.__isCustomGameMessageTypeSupported(type);
	}

	__isCustomGameMessageTypeSupported(type)
	{
		for (let lType_str in GAME_MESSAGES)
		{
			if (GAME_MESSAGES[lType_str] === type)
			{
				return true;
			}
		}

		return false;
	}
}

export { GAME_MESSAGES, LOBBY_MESSAGES }
export default GUSLobbyExternalCommunicator;