import { WebSocketInteractionInfo, SERVER_MESSAGES, GAME_CLIENT_MESSAGES } from '../../../../unified/model/interaction/server/WebSocketInteractionInfo';

SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE = 'GetRoomInfoResponse';
SERVER_MESSAGES.SIT_OUT_RESPONSE = 'SitOutResponse';

const CLIENT_MESSAGES = GAME_CLIENT_MESSAGES;

class GUSPseudoGameWebSocketInteractionInfo extends WebSocketInteractionInfo
{
	constructor()
	{
		super();
	}
}

export { SERVER_MESSAGES, CLIENT_MESSAGES };
export default GUSPseudoGameWebSocketInteractionInfo;