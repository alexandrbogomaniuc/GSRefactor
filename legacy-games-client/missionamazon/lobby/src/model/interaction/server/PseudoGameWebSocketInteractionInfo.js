import {WebSocketInteractionInfo, SERVER_MESSAGES, GAME_CLIENT_MESSAGES} from '../../../../../../common/PIXI/src/dgphoenix/unified/model/interaction/server/WebSocketInteractionInfo';

SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE = 'GetRoomInfoResponse';
// SERVER_MESSAGES.FULL_GAME_INFO = 'FullGameInfo';
// SERVER_MESSAGES.SIT_IN_RESPONSE = 'SitInResponse';
SERVER_MESSAGES.SIT_OUT_RESPONSE = 'SitOutResponse';
// SERVER_MESSAGES.NEW_ENEMY = 'NewEnemy';
// SERVER_MESSAGES.NEW_ENEMIES = 'NewEnemies';
// SERVER_MESSAGES.MISS = 'Miss';
// SERVER_MESSAGES.HIT = 'Hit';
// SERVER_MESSAGES.CLIENTS_INFO = 'ClientsInfo';
// SERVER_MESSAGES.ROUND_RESULT = 'RoundResult';
// SERVER_MESSAGES.GAME_STATE_CHANGED = 'GameStateChanged';
// SERVER_MESSAGES.BUY_IN_RESPONSE = 'BuyInResponse';
// SERVER_MESSAGES.ENEMY_DESTROYED = 'EnemyDestroyed';
// SERVER_MESSAGES.CHANGE_MAP = 'ChangeMap';
// SERVER_MESSAGES.WEAPONS = 'Weapons';
// SERVER_MESSAGES.BALANCE_UPDATED = "BalanceUpdated";
// SERVER_MESSAGES.WEAPON_SWITCHED = "WeaponSwitched";
// SERVER_MESSAGES.UPDATE_TRAJECTORIES = "UpdateTrajectories";
// SERVER_MESSAGES.ROUND_FINISH_SOON = "RoundFinishSoon";


const CLIENT_MESSAGES = GAME_CLIENT_MESSAGES;

class PseudoGameWebSocketInteractionInfo extends WebSocketInteractionInfo
{
	constructor()
	{
		super();
	}
}

export { PseudoGameWebSocketInteractionInfo, SERVER_MESSAGES, CLIENT_MESSAGES };