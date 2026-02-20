import GUDialogsInfo, { __generateNextDialogId } from './GUDialogsInfo';
import GUGameNetworkErrorDialogInfo from './custom/game/GUGameNetworkErrorDialogInfo';
import GUGameCriticalErrorDialogInfo from './custom/game/GUGameCriticalErrorDialogInfo';
import GUGameReconnectDialogInfo from './custom/game/GUGameReconnectDialogInfo';
import GUGameRoomReopenDialogInfo from './custom/game/GUGameRoomReopenDialogInfo';
import GUGameBuyAmmoFailedDialogInfo from './custom/game/GUGameBuyAmmoFailedDialogInfo';
import GUReturnToGameDialogInfo from './custom/GUReturnToGameDialogInfo';
import GUGameForceSitOutDialogInfo from './custom/game/GUGameForceSitOutDialogInfo';
import GUGameMidCompensateSWDialogInfo from './custom/game/GUGameMidCompensateSWDialogInfo';
import GUGamePicksUpSpecialWeaponsFirstTimeDialogInfo from './custom/game/GUGamePicksUpSpecialWeaponsFirstTimeDialogInfo';
import GUGameMidRoundExitDialogInfo from './custom/game/GUGameMidRoundExitDialogInfo';
import GUBonusDialogInfo from './custom/GUBonusDialogInfo';
import GUFRBDialogInfo from './custom/GUFRBDialogInfo';
import GUTournamentStateDialogInfo from './custom/GUTournamentStateDialogInfo';
import GUGameRebuyDialogInfo from './custom/game/GUGameRebuyDialogInfo';
import GUGameNEMForRoomDialogInfo from './custom/game/GUGameNEMForRoomDialogInfo';
import GULobbyRebuyDialogInfo from './custom/GULobbyRebuyDialogInfo';
import GULobbyNEMDialogInfo from './custom/GULobbyNEMDialogInfo';
import GULobbyRebuyFailedDialogInfo from './custom/GULobbyRebuyFailedDialogInfo';
import GUGameSWPurchaseLimitExceededDialogInfo from './custom/game/GUGameSWPurchaseLimitExceededDialogInfo';
import GULobbyInsufficientFundsDialogInfo from './custom/GULobbyInsufficientFundsDialogInfo';
import GUSGameBattlegroundNoWeaponsFiredDialogInfo from './custom/game/GUSGameBattlegroundNoWeaponsFiredDialogInfo';
import GUSBattlegroundRulesDialogInfo from './custom/GUSBattlegroundRulesDialogInfo';
import GUSBattlegroundBuyInConfirmationDialogInfo from './custom/GUSBattlegroundBuyInConfirmationDialogInfo';
import GUSLobbyBattlegroundNotEnoughPlayersDialogInfo from './custom/GUSLobbyBattlegroundNotEnoughPlayersDialogInfo';
import GUGameServerRebootDialogInfo from './custom/game/GUGameServerRebootDialogInfo';
import GUGamePendingOperationFailedDialogInfo from './custom/game/GUGamePendingOperationFailedDialogInfo';
import GUWaitPendingOperationDialogInfo from './custom/GUWaitPendingOperationDialogInfo';
import GUGamePleaseWaitDialogInfo from './custom/game/GUGamePleaseWaitDialogInfo';
import GUSGameBattlegroundContinueReadingDialogInfo from './custom/game/GUSGameBattlegroundContinueReadingDialogInfo';
import GUSRoomMovedErrorRequestsLimitDialogInfo from './custom/GUSRoomMovedErrorRequestsLimitDialogInfo';


const DIALOG_ID_GAME_NETWORK_ERROR 						= __generateNextDialogId();
const DIALOG_ID_GAME_CRITICAL_ERROR 					= __generateNextDialogId();
const DIALOG_ID_GAME_RECONNECT 							= __generateNextDialogId();
const DIALOG_ID_GAME_ROOM_REOPEN 						= __generateNextDialogId();
const DIALOG_ID_RETURN_TO_GAME 							= __generateNextDialogId();
const DIALOG_ID_FORCE_SIT_OUT							= __generateNextDialogId();
const DIALOG_ID_MID_ROUND_COMPENSATE_SW 				= __generateNextDialogId();
const DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME 	= __generateNextDialogId();
const DIALOG_ID_MID_ROUND_EXIT 							= __generateNextDialogId();
const DIALOG_ID_GAME_BUY_AMMO_FAILED	 				= __generateNextDialogId();
const DIALOG_ID_BONUS 									= __generateNextDialogId();
const DIALOG_ID_FRB										= __generateNextDialogId();
const DIALOG_ID_TOURNAMENT_STATE						= __generateNextDialogId();
const DIALOG_ID_GAME_REBUY								= __generateNextDialogId();
const DIALOG_ID_GAME_NEM_FOR_ROOM						= __generateNextDialogId();
const DIALOG_ID_LOBBY_REBUY								= __generateNextDialogId();
const DIALOG_ID_LOBBY_NEM								= __generateNextDialogId();
const DIALOG_ID_LOBBY_REBUY_FAILED						= __generateNextDialogId();
const DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED			= __generateNextDialogId();
const DIALOG_ID_INSUFFICIENT_FUNDS						= __generateNextDialogId();
const DIALOG_ID_NO_WEAPONS_FIRED						= __generateNextDialogId();
const DIALOG_ID_BATTLEGROUND_RULES						= __generateNextDialogId();
const DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION		= __generateNextDialogId();
const DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS			= __generateNextDialogId();
const DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION		= __generateNextDialogId();
const DIALOG_ID_GAME_SERVER_REBOOT						= __generateNextDialogId();
const DIALOG_ID_PENDING_OPERATION_FAILED				= __generateNextDialogId();
const DIALOG_ID_WAIT_PENDING_OPERATION					= __generateNextDialogId();
const DIALOG_ID_PLEASE_WAIT								= __generateNextDialogId();
const DIALOG_ID_BATTLEGROUND_CONTINUE_READING			= __generateNextDialogId();
const DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED			= __generateNextDialogId();


class GUSDialogsInfo extends GUDialogsInfo
{
	static get DIALOG_ID_GAME_NETWORK_ERROR() 						{ return DIALOG_ID_GAME_NETWORK_ERROR; }
	static get DIALOG_ID_GAME_CRITICAL_ERROR()						{ return DIALOG_ID_GAME_CRITICAL_ERROR; }
	static get DIALOG_ID_GAME_RECONNECT() 							{ return DIALOG_ID_GAME_RECONNECT; }
	static get DIALOG_ID_GAME_ROOM_REOPEN() 						{ return DIALOG_ID_GAME_ROOM_REOPEN; }
	static get DIALOG_ID_RETURN_TO_GAME() 							{ return DIALOG_ID_RETURN_TO_GAME; }
	static get DIALOG_ID_FORCE_SIT_OUT() 							{ return DIALOG_ID_FORCE_SIT_OUT; }
	static get DIALOG_ID_MID_ROUND_COMPENSATE_SW() 					{ return DIALOG_ID_MID_ROUND_COMPENSATE_SW; }
	static get DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME() 		{ return DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME; }
	static get DIALOG_ID_MID_ROUND_EXIT() 							{ return DIALOG_ID_MID_ROUND_EXIT; }
	static get DIALOG_ID_GAME_BUY_AMMO_FAILED() 					{ return DIALOG_ID_GAME_BUY_AMMO_FAILED; }
	static get DIALOG_ID_BONUS() 									{ return DIALOG_ID_BONUS; }
	static get DIALOG_ID_FRB() 										{ return DIALOG_ID_FRB; }
	static get DIALOG_ID_TOURNAMENT_STATE() 						{ return DIALOG_ID_TOURNAMENT_STATE; }
	static get DIALOG_ID_GAME_REBUY() 								{ return DIALOG_ID_GAME_REBUY; }
	static get DIALOG_ID_GAME_NEM_FOR_ROOM() 						{ return DIALOG_ID_GAME_NEM_FOR_ROOM; }
	static get DIALOG_ID_LOBBY_REBUY() 								{ return DIALOG_ID_LOBBY_REBUY; }
	static get DIALOG_ID_LOBBY_NEM() 								{ return DIALOG_ID_LOBBY_NEM; }
	static get DIALOG_ID_LOBBY_REBUY_FAILED() 						{ return DIALOG_ID_LOBBY_REBUY_FAILED; }
	static get DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED() 			{ return DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED; }
	static get DIALOG_ID_INSUFFICIENT_FUNDS() 						{ return DIALOG_ID_INSUFFICIENT_FUNDS; }
	static get DIALOG_ID_NO_WEAPONS_FIRED() 						{ return DIALOG_ID_NO_WEAPONS_FIRED; }
	static get DIALOG_ID_BATTLEGROUND_RULES() 						{ return DIALOG_ID_BATTLEGROUND_RULES; }
	static get DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION() 		{ return DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION; }
	static get DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS() 			{ return DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS; }
	static get DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION() 		{ return DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION; }
	static get DIALOG_ID_GAME_SERVER_REBOOT() 						{ return DIALOG_ID_GAME_SERVER_REBOOT; }
	static get DIALOG_ID_PENDING_OPERATION_FAILED() 				{ return DIALOG_ID_PENDING_OPERATION_FAILED; }
	static get DIALOG_ID_WAIT_PENDING_OPERATION() 					{ return DIALOG_ID_WAIT_PENDING_OPERATION; }
	static get DIALOG_ID_PLEASE_WAIT() 								{ return DIALOG_ID_PLEASE_WAIT; }
	static get DIALOG_ID_BATTLEGROUND_CONTINUE_READING() 			{ return DIALOG_ID_BATTLEGROUND_CONTINUE_READING; }
	static get DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED() 	{ return DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED; }
	

	static get CRITICAL_DIALOGS_IDS()
	{
		return super.CRITICAL_DIALOGS_IDS.concat(
													[
														GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR,
														GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR,
														GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE
													]
												);
	}

	constructor()
	{
		super();
	}

	__initSupportedDialogsIds()
	{
		super.__initSupportedDialogsIds();

		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_RECONNECT);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_RETURN_TO_GAME);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_FORCE_SIT_OUT);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_MID_ROUND_EXIT);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_BONUS);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_FRB);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_REBUY);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_LOBBY_NEM);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_PLEASE_WAIT);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING);
		this.__registerDialogId(GUSDialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED);
	}

	__generateDialogInfo(dialogId)
	{
		let dialogInfo = null;

		switch (dialogId)
		{
			case GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR:
				dialogInfo = this.__gameNetworkErrorDialogInfo(dialogId, 1);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR:
				dialogInfo = this.__gameCriticalErrorDialogInfo(dialogId, 1);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_RECONNECT:
				dialogInfo = this.__gameReconnectDialogInfo(dialogId, 0.8);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT:
				dialogInfo = this.__gameServerRebootDialogInfo(dialogId, 1);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN:
				dialogInfo = this.__gameRoomReopenDialogInfo(dialogId, 0.1);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED:
				dialogInfo = this.__gameBuyAmmoFailedDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_RETURN_TO_GAME:
				dialogInfo = this.__returnToGameDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_FORCE_SIT_OUT:
				dialogInfo = this.__gameForceSitOutDialogInfo(dialogId, 0.15);
				break;
			case GUSDialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW:
				dialogInfo = this.__gameMidCompensateSWDialogInfo(dialogId, 0.15);
				break;
			case GUSDialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME:
				dialogInfo = this.__gamePicksUpSpecialWeaponsFirstTimeDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_MID_ROUND_EXIT:
				dialogInfo = this.__gameMidRoundExitDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_BONUS:
				dialogInfo = this.__bonusDialogInfo(dialogId, 0.1);
				break;
			case GUSDialogsInfo.DIALOG_ID_FRB:
				dialogInfo = this.__FRBDialogInfo(dialogId, 0.1);
				break;
			case GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE:
				dialogInfo = this.__tournamentStateDialogInfo(dialogId, 1);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_REBUY:
				dialogInfo = this.__gameRebuyDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM:
				dialogInfo = this.__gameNEMForRoomDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY:
				dialogInfo = this.__lobbyRebuyDialogInfo(dialogId, 0.01);
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_NEM:
				dialogInfo = this.__lobbyNEMDialogInfo(dialogId, 0.01);
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED:
				dialogInfo = this.__lobbyRebuyFailedDialogInfo(dialogId, 0.01);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED:
				dialogInfo = this.__gameSWPurchaseLimitExceededDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS:
				dialogInfo = this.__lobbyInsufficientFundsDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				dialogInfo = this.__generateGameBattlegroundNoWeaponsFiredDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
				dialogInfo = this.__generateBattlegroundRulesDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
				dialogInfo = this.__generateBattlegroundBuyInCnfirmationDialogInfo(dialogId, -1);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogInfo = this.__generateBattlegroundNotEnoughPlayersDialogInfo(dialogId, -1);
				break;
			case GUSDialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION:
				dialogInfo = this.__generateRoundTransitionSWCompensationDialogInfo(dialogId, -1);
				break;
			case GUSDialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED:
				dialogInfo = this.__generateGamePendingOperationFailedDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogInfo = this.__generateWaitPendingOperationDialogInfo(dialogId, 0.7);
				break;
			case GUSDialogsInfo.DIALOG_ID_PLEASE_WAIT:
				dialogInfo = this.__generatePleaseWaitDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING:
				dialogInfo = this.__generateBattlegroundContinueReadingDialogInfo(dialogId, 0);
				break;
			case GUSDialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED:
				dialogInfo = this.__generateRoomMovedErrorRequestLimitReachedDialogInfo(dialogId, 1);
				break;
			default:
				dialogInfo = super.__generateDialogInfo(dialogId);
		}

		return dialogInfo;
	}

	__gameNetworkErrorDialogInfo(dialogId, priority)
	{
		return new GUGameNetworkErrorDialogInfo(dialogId, priority);
	}

	__gameCriticalErrorDialogInfo(dialogId, priority)
	{
		return new GUGameCriticalErrorDialogInfo(dialogId, priority);
	}

	__gameReconnectDialogInfo(dialogId, priority)
	{
		return new GUGameReconnectDialogInfo(dialogId, priority);
	}

	__gameServerRebootDialogInfo(dialogId, priority)
	{
		return new GUGameServerRebootDialogInfo(dialogId, priority);
	}

	__gameRoomReopenDialogInfo(dialogId, priority)
	{
		return new GUGameRoomReopenDialogInfo(dialogId, priority);
	}

	__gameBuyAmmoFailedDialogInfo(dialogId, priority)
	{
		return new GUGameBuyAmmoFailedDialogInfo(dialogId, priority);
	}

	__returnToGameDialogInfo(dialogId, priority)
	{
		return new GUReturnToGameDialogInfo(dialogId, priority);
	}

	__gameForceSitOutDialogInfo(dialogId, priority)
	{
		return new GUGameForceSitOutDialogInfo(dialogId, priority);
	}

	__gameMidCompensateSWDialogInfo(dialogId, priority)
	{
		return new GUGameMidCompensateSWDialogInfo(dialogId, priority);
	}

	__gamePicksUpSpecialWeaponsFirstTimeDialogInfo(dialogId, priority)
	{
		return new GUGamePicksUpSpecialWeaponsFirstTimeDialogInfo(dialogId, priority);
	}

	__gameMidRoundExitDialogInfo(dialogId, priority)
	{
		return new GUGameMidRoundExitDialogInfo(dialogId, priority);
	}

	__bonusDialogInfo(dialogId, priority)
	{
		return new GUBonusDialogInfo(dialogId, priority);
	}

	__FRBDialogInfo(dialogId, priority)
	{
		return new GUFRBDialogInfo(dialogId, priority);
	}

	__tournamentStateDialogInfo(dialogId, priority)
	{
		return new GUTournamentStateDialogInfo(dialogId, priority);
	}

	__gameRebuyDialogInfo(dialogId, priority)
	{
		return new GUGameRebuyDialogInfo(dialogId, priority);
	}

	__gameNEMForRoomDialogInfo(dialogId, priority)
	{
		return new GUGameNEMForRoomDialogInfo(dialogId, priority);
	}

	__lobbyRebuyDialogInfo(dialogId, priority)
	{
		return new GULobbyRebuyDialogInfo(dialogId, priority);
	}

	__lobbyNEMDialogInfo(dialogId, priority)
	{
		return new GULobbyNEMDialogInfo(dialogId, priority);
	}

	__lobbyRebuyFailedDialogInfo(dialogId, priority)
	{
		return new GULobbyRebuyFailedDialogInfo(dialogId, priority);
	}

	__gameSWPurchaseLimitExceededDialogInfo(dialogId, priority)
	{
		return new GUGameSWPurchaseLimitExceededDialogInfo(dialogId, priority);
	}

	__lobbyInsufficientFundsDialogInfo(dialogId, priority)
	{
		return new GULobbyInsufficientFundsDialogInfo(dialogId, priority);
	}

	__generateGameBattlegroundNoWeaponsFiredDialogInfo(dialogId, priority)
	{
		return new GUSGameBattlegroundNoWeaponsFiredDialogInfo(dialogId, priority);
	}

	__generateBattlegroundRulesDialogInfo(dialogId, priority)
	{
		return new GUSBattlegroundRulesDialogInfo(dialogId, priority);
	}
	
	__generateBattlegroundBuyInCnfirmationDialogInfo(dialogId, priority)
	{
		return new GUSBattlegroundBuyInConfirmationDialogInfo(dialogId, priority);
	}

	__generateBattlegroundNotEnoughPlayersDialogInfo(dialogId, priority)
	{
		return new GUSLobbyBattlegroundNotEnoughPlayersDialogInfo(dialogId, priority);
	}

	__generateRoundTransitionSWCompensationDialogInfo(dialogId, priority)
	{
		return new GUGameSWPurchaseLimitExceededDialogInfo(dialogId, priority);
	}

	__generateGamePendingOperationFailedDialogInfo(dialogId, priority)
	{
		return new GUGamePendingOperationFailedDialogInfo(dialogId, priority);
	}

	__generateWaitPendingOperationDialogInfo(dialogId, priority)
	{
		return new GUWaitPendingOperationDialogInfo(dialogId, priority);
	}

	__generatePleaseWaitDialogInfo(dialogId, priority)
	{
		return new GUGamePleaseWaitDialogInfo(dialogId, priority);
	}

	__generateBattlegroundContinueReadingDialogInfo(dialogId, priority)
	{
		return new GUSGameBattlegroundContinueReadingDialogInfo(dialogId, priority);
	}

	__generateRoomMovedErrorRequestLimitReachedDialogInfo(dialogId, priority)
	{
		return new GUSRoomMovedErrorRequestsLimitDialogInfo(dialogId, priority);
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSDialogsInfo