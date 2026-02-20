import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import DialogInfo from './DialogInfo';
import NetworkErrorDialogInfo from './custom/NetworkErrorDialogInfo';
import GameNetworkErrorDialogInfo from './custom/game/GameNetworkErrorDialogInfo';
import CriticalErrorDialogInfo from './custom/CriticalErrorDialogInfo';
import GameCriticalErrorDialogInfo from './custom/game/GameCriticalErrorDialogInfo';
import ReconnectDialogInfo from './custom/ReconnectDialogInfo';
import GameReconnectDialogInfo from './custom/game/GameReconnectDialogInfo';
import GameRoomReopenDialogInfo from './custom/game/GameRoomReopenDialogInfo';
import RoomNotFoundDialogInfo from './custom/RoomNotFoundDialogInfo';
import GameNEMDialogInfo from './custom/game/GameNEMDialogInfo';
import RedirectionDialogInfo from './custom/RedirectionDialogInfo';
import GameBuyAmmoFailedDialogInfo from './custom/game/GameBuyAmmoFailedDialogInfo';
import ReturnToGameDialogInfo from './custom/ReturnToGameDialogInfo';
import GameForceSitOutDialogInfo from './custom/game/GameForceSitOutDialogInfo';
import GameMidCompensateSWDialogInfo from './custom/game/GameMidCompensateSWDialogInfo';
import GamePicksUpSpecialWeaponsFirstTimeDialogInfo from './custom/game/GamePicksUpSpecialWeaponsFirstTimeDialogInfo';
import GameMidRoundExitDialogInfo from './custom/game/GameMidRoundExitDialogInfo';
import RuntimeErrorDialogInfo from './custom/RuntimeErrorDialogInfo';
import BonusDialogInfo from './custom/BonusDialogInfo';
import FRBDialogInfo from './custom/FRBDialogInfo';
import TournamentStateDialogInfo from './custom/TournamentStateDialogInfo';
import GameRebuyDialogInfo from './custom/game/GameRebuyDialogInfo';
import GameNEMForRoomDialogInfo from './custom/game/GameNEMForRoomDialogInfo';
import LobbyRebuyDialogInfo from './custom/LobbyRebuyDialogInfo';
import LobbyNEMDialogInfo from './custom/LobbyNEMDialogInfo';
import LobbyRebuyFailedDialogInfo from './custom/LobbyRebuyFailedDialogInfo';
import GameSWPurchaseLimitExceededDialogInfo from './custom/game/GameSWPurchaseLimitExceededDialogInfo';
import LobbyInsufficientFundsDialogInfo from './custom/LobbyInsufficientFundsDialogInfo';
import LobbyBattlegroundNotEnoughPlayersDialogInfo from './custom/LobbyBattlegroundNotEnoughPlayersDialogInfo';
import BattlegroundBuyInConfirmationDialogInfo from './custom/BattlegroundBuyInConfirmationDialogInfo';
import BattlegroundRulesDialogInfo from './custom/BattlegroundRulesDialogInfo';
import GameBattlegroundNoWeaponsFiredDialogInfo from './custom/game/GameBattlegroundNoWeaponsFiredDialogInfo';
import GameBattlegroundContinueReadingDialogInfo from './custom/game/GameBattlegroundContinueReadingDialogInfo';
import GamePendingOperationFailedDialogInfo from './custom/game/GamePendingOperationFailedDialogInfo';
import RoundAlreadyFinishedDialogInfo from './custom/RoundAlreadyFinishedDialogInfo';
import ServerRebootDialogInfo from './custom/ServerRebootDialogInfo';
import GameServerRebootDialogInfo from './custom/game/GameServerRebootDialogInfo';
import WaitPendingOperationDialogInfo from './custom/WaitPendingOperationDialogInfo';
import BattlegroundCafRoomManagerDialogInfo from './custom/BattlegroundCafRoomManagerDialogInfo';
import BattlegroundBuyInConfirmationDialogInfoCAF from './custom/BattlegroundBuyInConfirmationDialogInfoCAF';

import
{ 
	DIALOG_ID_NETWORK_ERROR,
	DIALOG_ID_GAME_NETWORK_ERROR,
	DIALOG_ID_CRITICAL_ERROR,
	DIALOG_ID_GAME_CRITICAL_ERROR,
	DIALOG_ID_RECONNECT,
	DIALOG_ID_GAME_RECONNECT,
	DIALOG_ID_GAME_ROOM_REOPEN,
	DIALOG_ID_ROOM_NOT_FOUND,
	DIALOG_ID_GAME_NEM,
	DIALOG_ID_REDIRECTION,
	DIALOG_ID_RETURN_TO_GAME,
	DIALOG_ID_FORCE_SIT_OUT,
	DIALOG_ID_MID_ROUND_COMPENSATE_SW,
	DIALOG_ID_MID_ROUND_EXIT,
	DIALOG_ID_GAME_BUY_AMMO_FAILED,
	DIALOG_ID_WEBGL_CONTEXT_LOST,
	DIALOG_ID_RUNTIME_ERROR,
	DIALOG_ID_BONUS,
	DIALOG_ID_FRB,
	DIALOG_ID_TOURNAMENT_STATE,
	DIALOG_ID_GAME_REBUY,
	DIALOG_ID_GAME_NEM_FOR_ROOM,
	DIALOG_ID_LOBBY_REBUY,
	DIALOG_ID_LOBBY_NEM,
	DIALOG_ID_LOBBY_REBUY_FAILED,
	DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED,
	DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION,

	DIALOG_ID_INSUFFICIENT_FUNDS,
	DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS,
	DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION,
	DIALOG_ID_BATTLEGROUND_RULES,
	DIALOG_ID_NO_WEAPONS_FIRED,
	DIALOG_ID_BATTLEGROUND_CONTINUE_READING,
	DIALOG_ID_PENDING_OPERATION_FAILED,
	DIALOG_ID_ROUND_ALREADY_FINISHED,
	DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED,
	DIALOG_ID_PLEASE_WAIT,
	DIALOG_ID_WAIT_PENDING_OPERATION,
	DIALOG_ID_CAF_PLAYER_KICKED,
	DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED,
	DIALOG_ID_SERVER_REBOOT,
	DIALOG_ID_GAME_SERVER_REBOOT,
	DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER,
	DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF,
	DIALOGS_IDS_COUNT
} from '../../../../../../shared/src/CommonConstants';
import RoomMovedErrorRequestsLimitDialogInfo from './custom/RoomMovedErrorRequestsLimitDialogInfo';
import PleaseWaitDialogInfo from './custom/PleaseWaitDialogInfo';
/*

*/
let DIALOGS_AMOUNT = DIALOGS_IDS_COUNT;

class DialogsInfo extends SimpleUIInfo
{
	static get DIALOG_ID_NETWORK_ERROR() 							{ return DIALOG_ID_NETWORK_ERROR;}
	static get DIALOG_ID_GAME_NETWORK_ERROR() 						{ return DIALOG_ID_GAME_NETWORK_ERROR;}
	static get DIALOG_ID_CRITICAL_ERROR() 							{ return DIALOG_ID_CRITICAL_ERROR;}
	static get DIALOG_ID_GAME_CRITICAL_ERROR() 						{ return DIALOG_ID_GAME_CRITICAL_ERROR;}
	static get DIALOG_ID_RECONNECT() 								{ return DIALOG_ID_RECONNECT;}
	static get DIALOG_ID_GAME_RECONNECT() 							{ return DIALOG_ID_GAME_RECONNECT;}
	static get DIALOG_ID_GAME_ROOM_REOPEN() 						{ return DIALOG_ID_GAME_ROOM_REOPEN;}
	static get DIALOG_ID_ROOM_NOT_FOUND() 							{ return DIALOG_ID_ROOM_NOT_FOUND;}
	static get DIALOG_ID_GAME_NEM() 								{ return DIALOG_ID_GAME_NEM;}
	static get DIALOG_ID_REDIRECTION() 								{ return DIALOG_ID_REDIRECTION;}
	static get DIALOG_ID_RETURN_TO_GAME() 							{ return DIALOG_ID_RETURN_TO_GAME;}
	static get DIALOG_ID_FORCE_SIT_OUT() 							{ return DIALOG_ID_FORCE_SIT_OUT;}
	static get DIALOG_ID_MID_ROUND_COMPENSATE_SW() 					{ return DIALOG_ID_MID_ROUND_COMPENSATE_SW;}
	static get DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME() 		{ return DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME;}
	static get DIALOG_ID_MID_ROUND_EXIT() 							{ return DIALOG_ID_MID_ROUND_EXIT;}
	static get DIALOG_ID_GAME_BUY_AMMO_FAILED() 					{ return DIALOG_ID_GAME_BUY_AMMO_FAILED;}
	static get DIALOG_ID_WEBGL_CONTEXT_LOST() 						{ return DIALOG_ID_WEBGL_CONTEXT_LOST;}
	static get DIALOG_ID_RUNTIME_ERROR() 							{ return DIALOG_ID_RUNTIME_ERROR;}
	static get DIALOG_ID_BONUS() 								 	{ return DIALOG_ID_BONUS;}
	static get DIALOG_ID_FRB()										{ return DIALOG_ID_FRB;}
	static get DIALOG_ID_TOURNAMENT_STATE() 						{ return DIALOG_ID_TOURNAMENT_STATE;}
	static get DIALOG_ID_GAME_REBUY() 								{ return DIALOG_ID_GAME_REBUY;}	
	static get DIALOG_ID_GAME_NEM_FOR_ROOM() 						{ return DIALOG_ID_GAME_NEM_FOR_ROOM;}	
	static get DIALOG_ID_LOBBY_REBUY() 								{ return DIALOG_ID_LOBBY_REBUY;}
	static get DIALOG_ID_LOBBY_NEM() 								{ return DIALOG_ID_LOBBY_NEM;}
	static get DIALOG_ID_LOBBY_REBUY_FAILED() 						{ return DIALOG_ID_LOBBY_REBUY_FAILED;}
	static get DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED() 			{ return DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED; }
	static get DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION() 		{ return DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION; }
	static get DIALOG_ID_INSUFFICIENT_FUNDS() 						{ return DIALOG_ID_INSUFFICIENT_FUNDS; }
	static get DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS() 			{ return DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS; }
	static get DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION() 		{ return DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION; }
	static get DIALOG_ID_BATTLEGROUND_RULES() 						{ return DIALOG_ID_BATTLEGROUND_RULES; }
	static get DIALOG_ID_NO_WEAPONS_FIRED() 						{ return DIALOG_ID_NO_WEAPONS_FIRED; }
	static get DIALOG_ID_BATTLEGROUND_CONTINUE_READING() 			{ return DIALOG_ID_BATTLEGROUND_CONTINUE_READING; }
	static get DIALOG_ID_PENDING_OPERATION_FAILED() 				{ return DIALOG_ID_PENDING_OPERATION_FAILED; }
	static get DIALOG_ID_ROUND_ALREADY_FINISHED()					{ return DIALOG_ID_ROUND_ALREADY_FINISHED; }
	static get DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED()	{ return DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED; }
	
	static get DIALOG_ID_PLEASE_WAIT()								{ return DIALOG_ID_PLEASE_WAIT; }
	static get DIALOG_ID_WAIT_PENDING_OPERATION() 					{ return DIALOG_ID_WAIT_PENDING_OPERATION;}

	static get DIALOG_ID_SERVER_REBOOT()							{ return DIALOG_ID_SERVER_REBOOT; }
	static get DIALOG_ID_GAME_SERVER_REBOOT()						{ return DIALOG_ID_GAME_SERVER_REBOOT; }

	static get DIALOG_ID_CAF_PLAYER_KICKED()						{ return DIALOG_ID_CAF_PLAYER_KICKED; }
	static get DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER()			{ return DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER; }
	static get DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED()			{ return DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED; }	
	static get DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF() {return DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF;}
	
	static get CRITICAL_DIALOGS_IDS() 								{ return [
																			DialogsInfo.DIALOG_ID_NETWORK_ERROR,
																			DialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR,
																			DialogsInfo.DIALOG_ID_CRITICAL_ERROR,
																			DialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR,
																			DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST,
																			DialogsInfo.DIALOG_ID_RUNTIME_ERROR,
																			DialogsInfo.DIALOG_ID_TOURNAMENT_STATE,
																			DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED
																		];
																	}

	constructor()
	{
		super();

		this._dialogsInfos = null;
		this._dialogIdForPresentation = undefined;

		this._initDialogsInfo();
	}

	destroy()
	{
		this._dialogsInfos = null;

		super.destroy();
	}

	getDialogInfo (dialogId)
	{
		return this._getDialogInfo(dialogId);
	}

	get dialogsCount ()
	{
		return DIALOGS_AMOUNT;
	}

	get dialogIdForPresentation ()
	{
		return this._dialogIdForPresentation;
	}

	set dialogIdForPresentation (dialogId)
	{
		this._dialogIdForPresentation = dialogId;
	}

	hasActiveDialogWithId(aDialogId_int)
	{
		var dialogsAmount = this.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			if (
				this._getDialogInfo(i).isActive &&
				aDialogId_int === this._getDialogInfo(i).dialogId
				)
			{
				return true;
			}
		}
		return false;
	}


	get hasActiveDialog ()
	{
		var dialogsAmount = this.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			if (this._getDialogInfo(i).isActive)
			{
				return true;
			}
		}
		return false;
	}

	get hasActiveCriticalDialog ()
	{
		var dialogsAmount = this.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			if (this._getDialogInfo(i).isActive)
			{
				const dialogId = this._getDialogInfo(i).dialogId;
				if (~DialogsInfo.CRITICAL_DIALOGS_IDS.indexOf(dialogId))
				{
					return true;
				}
				return false;
			}
		}
		return false;
	}

	_initDialogsInfo()
	{
		this._dialogsInfos = [];
	}

	_getDialogInfo (dialogId)
	{
		return this._dialogsInfos[dialogId] || this._initDialogInfo(dialogId);
	}

	_initDialogInfo (dialogId)
	{
		var dialogInfo = this.__generateDialogInfo(dialogId);

		this._dialogsInfos[dialogId] = dialogInfo;

		return dialogInfo;
	}

	__generateDialogInfo (dialogId)
	{
		var dialogInfo = null;
		switch (dialogId)
		{
			case DIALOG_ID_NETWORK_ERROR:
				dialogInfo = new NetworkErrorDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_GAME_NETWORK_ERROR:
				dialogInfo = new GameNetworkErrorDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_CRITICAL_ERROR:
				dialogInfo = new CriticalErrorDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_GAME_CRITICAL_ERROR:
				dialogInfo = new GameCriticalErrorDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_RECONNECT:
				dialogInfo = new ReconnectDialogInfo(dialogId, 0.9);
				break;
			case DIALOG_ID_GAME_RECONNECT:
				dialogInfo = new GameReconnectDialogInfo(dialogId, 0.8);
				break;
			case DIALOG_ID_GAME_ROOM_REOPEN:
				dialogInfo = new GameRoomReopenDialogInfo(dialogId, 0.1);
				break;
			case DIALOG_ID_ROOM_NOT_FOUND:
				dialogInfo = new RoomNotFoundDialogInfo(dialogId, 0.1);
				break;
			case DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED:
				dialogInfo = new RoomMovedErrorRequestsLimitDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_GAME_NEM:
				dialogInfo = new GameNEMDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_REDIRECTION:
				dialogInfo = new RedirectionDialogInfo(dialogId, 4);
				break;
			case DIALOG_ID_GAME_BUY_AMMO_FAILED:
				dialogInfo = new GameBuyAmmoFailedDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_RETURN_TO_GAME:
				dialogInfo = new ReturnToGameDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_FORCE_SIT_OUT:
				dialogInfo = new GameForceSitOutDialogInfo(dialogId, 0.15);
				break;
			case DIALOG_ID_MID_ROUND_COMPENSATE_SW:
				dialogInfo = new GameMidCompensateSWDialogInfo(dialogId, 0.15);
				break;
			case DIALOG_ID_MID_ROUND_EXIT:
				dialogInfo = new GameMidRoundExitDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_WEBGL_CONTEXT_LOST:
				dialogInfo = new DialogInfo(dialogId, 5); // the highest priority
				break;
			case DIALOG_ID_RUNTIME_ERROR:
				dialogInfo = new RuntimeErrorDialogInfo(dialogId, 6); //the highest priority
				break;
			case DIALOG_ID_BONUS:
				dialogInfo = new BonusDialogInfo(dialogId, 0.1);
				break;
			case DIALOG_ID_FRB:
				dialogInfo = new FRBDialogInfo(dialogId, 0.1);
				break;
			case DIALOG_ID_TOURNAMENT_STATE:
				dialogInfo = new TournamentStateDialogInfo(dialogId, 1); // the same priority as for critical error dialogs
				break;
			case DIALOG_ID_GAME_REBUY:
				dialogInfo = new GameRebuyDialogInfo(dialogId, 0); // the same priority as for NEM dlg
				break;
			case DIALOG_ID_LOBBY_REBUY:
				dialogInfo = new LobbyRebuyDialogInfo(dialogId, 0.01); // priority higher than for GAME_REBUY dlg
				break;
			case DIALOG_ID_GAME_NEM_FOR_ROOM:
				dialogInfo = new GameNEMForRoomDialogInfo(dialogId, 0); // the same priority as for REBUY dlg
				break;
			case DIALOG_ID_LOBBY_NEM:
				dialogInfo = new LobbyNEMDialogInfo(dialogId, 0.01); // priority higher than for GAME_REBUY dlg
				break;
			case DIALOG_ID_LOBBY_REBUY_FAILED:
				dialogInfo = new LobbyRebuyFailedDialogInfo(dialogId, 0.01);
				break;
			case DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED:
				dialogInfo = new GameSWPurchaseLimitExceededDialogInfo(dialogId, 0); // the same priority as for NEM dlg
				break;
			case DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION:
				dialogInfo = new GameSWPurchaseLimitExceededDialogInfo(dialogId, -1); // the same priority as for NEM dlg
				break;
			case DIALOG_ID_INSUFFICIENT_FUNDS:
				dialogInfo = new LobbyInsufficientFundsDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogInfo = new LobbyBattlegroundNotEnoughPlayersDialogInfo(dialogId, -1);
				break;
			case DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
				dialogInfo = new BattlegroundBuyInConfirmationDialogInfo(dialogId, -1);
				break;
			case DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF:
				dialogInfo = new BattlegroundBuyInConfirmationDialogInfoCAF(dialogId, -1);
				break;
			case DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER:
				dialogInfo = new BattlegroundCafRoomManagerDialogInfo(dialogId, -1);
				break;
			case DIALOG_ID_BATTLEGROUND_RULES:
				dialogInfo = new BattlegroundRulesDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_NO_WEAPONS_FIRED:
				dialogInfo = new GameBattlegroundNoWeaponsFiredDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_BATTLEGROUND_CONTINUE_READING:
				dialogInfo = new GameBattlegroundContinueReadingDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_PENDING_OPERATION_FAILED:
				dialogInfo = new GamePendingOperationFailedDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogInfo = new RoundAlreadyFinishedDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_PLEASE_WAIT:
				dialogInfo = new PleaseWaitDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_SERVER_REBOOT:
				dialogInfo = new ServerRebootDialogInfo(dialogId, 1.1);
				break;
			case DIALOG_ID_GAME_SERVER_REBOOT:
				dialogInfo = new GameServerRebootDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogInfo = new WaitPendingOperationDialogInfo(dialogId, 0.7); // priority should be less then for dialogs that terminate gameplay (CriticalError, redirection etc.) and reconnect dialogs, but higher then for others
				break;
			case DIALOG_ID_CAF_PLAYER_KICKED:
				dialogInfo = new DialogInfo(dialogId, 0.7); // priority should be less then for dialogs that terminate gameplay (CriticalError, redirection etc.) and reconnect dialogs, but higher then for others
				break;
			case DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED:
				dialogInfo = new DialogInfo(dialogId, 1); 
				break;
			default:
				throw new Error (`Unsupported dialog id: ${dialogId}`);
		}
		return dialogInfo;
	}
}

export default DialogsInfo