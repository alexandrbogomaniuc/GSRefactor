import GUDialogsController from './GUDialogsController';
import GUSDialogsInfo from '../../../../model/uis/custom/dialogs/GUSDialogsInfo';
import GUSLobbyDialogsView from '../../../../view/uis/dialogs/GUSLobbyDialogsView';
import { APP } from '../../../../../unified/controller/main/globals';
import GUGameNetworkErrorDialogController from './custom/game/GUGameNetworkErrorDialogController';
import GUGameCriticalErrorDialogController from './custom/game/GUGameCriticalErrorDialogController';
import GUGameReconnectDialogController from './custom/game/GUGameReconnectDialogController';
import GUGameRoomReopenDialogController from './custom/game/GUGameRoomReopenDialogController';
import GUGameBuyAmmoFailedDialogController from './custom/game/GUGameBuyAmmoFailedDialogController';
import GUReturnToGameDialogController from './custom/GUReturnToGameDialogController';
import GUGameForceSitOutDialogController from './custom/game/GUGameForceSitOutDialogController';
import GUGameMidCompensateSWController from './custom/game/GUGameMidCompensateSWController';
import GUGamePicksUpSpecialWeaponsFirstTimeDialogController from './custom/game/GUGamePicksUpSpecialWeaponsFirstTimeDialogController';
import GUGameMidRoundExitDialogController from './custom/game/GUGameMidRoundExitDialogController';
import GUBonusDialogController from './custom/GUBonusDialogController';
import GUFRBDialogController from './custom/GUFRBDialogController';
import GUTournamentStateDialogController from './custom/GUTournamentStateDialogController';
import GUGameRebuyDialogController from './custom/game/GUGameRebuyDialogController';
import GUGameNEMForRoomDialogController from './custom/game/GUGameNEMForRoomDialogController';
import GULobbyRebuyDialogController from './custom/GULobbyRebuyDialogController';
import GULobbyNEMDialogController from './custom/GULobbyNEMDialogController';
import GULobbyRebuyFailedDialogController from './custom/GULobbyRebuyFailedDialogController';
import GUGameSWPurchaseLimitExceededDialogController from './custom/game/GUGameSWPurchaseLimitExceededDialogController';
import GULobbyInsufficientFundsDialogController from './custom/GULobbyInsufficientFundsDialogController';
import GUSGameBattlegroundNoWeaponsFiredDialogController from './custom/game/GUSGameBattlegroundNoWeaponsFiredDialogController';
import GUSBattlegroundRulesDialogController from './custom/GUSBattlegroundRulesDialogController';
import GUSBattlegroundBuyInConfirmationDialogController from './custom/GUSBattlegroundBuyInConfirmationDialogController';
import GUSLobbyBattlegroundNotEnoughPlayersDialogController from './custom/GUSLobbyBattlegroundNotEnoughPlayersDialogController';
import GUSGameRoundTransitionSWCompesationDialogController from './custom/game/GUSGameRoundTransitionSWCompesationDialogController';
import GUGameServerRebootDialogController from './custom/game/GUGameServerRebootDialogController';
import GUGamePendingOperationFailedDialogController from './custom/game/GUGamePendingOperationFailedDialogController';
import GUSWaitPendingOperationDialogController from './custom/GUSWaitPendingOperationDialogController';
import GUGamePleaseWaitDialogController from './custom/game/GUGamePleaseWaitDialogController';
import GUSGameBattlegroundContinueReadingDialogController from './custom/game/GUSGameBattlegroundContinueReadingDialogController';
import GUSRoomMovedErrorRequestsLimitDialogController from './custom/GUSRoomMovedErrorRequestsLimitDialogController'; 

class GUSLobbyDialogsController extends GUDialogsController
{
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING() {return GUSLobbyBattlegroundNotEnoughPlayersDialogController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING};
	static get EVENT_BATTLEGROUND_RULES_SHOW_REQUIRED() {return "EVENT_BATTLEGROUND_RULES_SHOW_REQUIRED"};
	
	constructor(aOptDialogsInfo)
	{
		super(aOptDialogsInfo || new GUSDialogsInfo());
	}

	get gameNetworkErrorDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR);
	}

	get gameCriticalErrorDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR);
	}

	get gameReconnectDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_RECONNECT);
	}
	
	get gameServerRebootDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT);
	}

	get gameRoomReopenDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN);
	}

	get gameBuyAmmoFailedDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED);
	}

	get bonusDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_BONUS);
	}

	get forceSitOutDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_FORCE_SIT_OUT);
	}

	get returnToGameDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_RETURN_TO_GAME);
	}

	get midRoundCompensateSWExitDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW);
	}

	get picksUpSpecialWeaponsFirstTimeDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME);
	}

	get midRoundExitDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_MID_ROUND_EXIT);
	}

	get FRBDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_FRB);
	}

	get tournamentStateDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE);
	}

	get gameRebuyDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_REBUY);
	}

	get gameNEMForRoomDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM);
	}

	get lobbyRebuyDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY);
	}

	get lobbyNEMDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_LOBBY_NEM);
	}

	get lobbyRebuyFailedDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED);
	}

	get gameSWPurchaseLimitExceededDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED);
	}

	get battlegroundBuyInConfirmationDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION);
	}

	get gameGameBattlegroundNoWeaponsFiredDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED);
	}

	get lobbyBattlegroundNotEnoughPlayersDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
	}

	get battlegroundRulesDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES);
	}

	get gamePendingOperationFailedDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED);
	}

	get waitPendingOperationDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
	}

	get gamePleaseWaitDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_PLEASE_WAIT);
	}

	get gameBattlegroundContinueReadingDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING);
	}

	get roomMovedErrorRequestsLimitDialogController()
	{
		return this.__getDialogController(GUSDialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED);
	}

	__updateSoundButtonPosition()
	{
		let lSoundButtonView_sbv = this._soundButtonController.view;
		lSoundButtonView_sbv.position.set(-445, -205);

		if (APP.isMobile)
		{
			lSoundButtonView_sbv.scale.set(1.8);
			lSoundButtonView_sbv.position.x += 4;
		}
	}

	__generateDialogController(dialogInfo)
	{
		let dialogController;
		let dialogId = dialogInfo.dialogId;
		var dialogInfo = dialogInfo;

		switch (dialogId)
		{
			case GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR:
				dialogController = this.__gameNetworkErrorDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR:
				dialogController = this.__gameCriticalErrorDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_RECONNECT:
				dialogController = this.__gameReconnectDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT:
				dialogController = this.__gameServerRebootDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN:
				dialogController = this.__gameRoomReopenDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED:
				dialogController = this.__gameBuyAmmoFailedDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_RETURN_TO_GAME:
				dialogController = this.__returnToGameDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_FORCE_SIT_OUT:
				dialogController = this.__gameForceSitOutDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW:
				dialogController = this.__gameMidCompensateSWController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME:
				dialogController = this.__gamePicksUpSpecialWeaponsFirstTimeDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_MID_ROUND_EXIT:
				dialogController = this.__gameMidRoundExitDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_BONUS:
				dialogController = this.__bonusDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_FRB:
				dialogController = this.__FRBDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE:
				dialogController = this.__tournamentStateDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_REBUY:
				dialogController = this.__gameRebuyDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM:
				dialogController = this.__gameNEMForRoomDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY:
				dialogController = this.__lobbyRebuyDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_NEM:
				dialogController = this.__lobbyNEMDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED:
				dialogController = this.__lobbyRebuyFailedDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED:
				dialogController = this.__gameSWPurchaseLimitExceededDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS:
				dialogController = this.__lobbyInsufficientFundsDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION:
				dialogController = this.__generateRoundTransitionSWCompensationDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogController = this.__generateBattlegroundNotEnoughPlayersDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
				dialogController = this.__generateBattlegroundBuyInConfirmationDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
				dialogController = this.__generateBattlegroundRulesDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				dialogController = this.__gameBattlegroundNoWeaponsFiredDialogController(dialogInfo);
				break;
			case GUSDialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED:
				dialogController = this.__gamePendingOperationFailedDialogController(dialogInfo, this);
				break;
			case GUSDialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogController = this.__generateWaitPendingOperationDialogController(dialogInfo, this);
				break;
			case GUSDialogsInfo.DIALOG_ID_PLEASE_WAIT:
				dialogController = this.__gamePleaseWaitDialogController(dialogInfo, this);
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING:
				dialogController = this.__generateGameBattlegroundContinueReadingDialogController(dialogInfo, this);
				break;
			case GUSDialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED:
				dialogController = this.__generateRoomMovedErrorRequestsLimitDialogController(dialogInfo, this);
				break;
			default:
				dialogController = super.__generateDialogController(dialogInfo);
		}

		return dialogController;
	}

	__gameNetworkErrorDialogController(dialogInfo)
	{
		return new GUGameNetworkErrorDialogController(dialogInfo, this);
	}

	__gameCriticalErrorDialogController(dialogInfo)
	{
		return new GUGameCriticalErrorDialogController(dialogInfo, this);
	}

	__gameReconnectDialogController(dialogInfo)
	{
		return new GUGameReconnectDialogController(dialogInfo, this);
	}

	__gameServerRebootDialogController(dialogInfo)
	{
		return new GUGameServerRebootDialogController(dialogInfo, this);
	}

	__gameRoomReopenDialogController(dialogInfo)
	{
		return new GUGameRoomReopenDialogController(dialogInfo, this);
	}

	__gameBuyAmmoFailedDialogController(dialogInfo)
	{
		return new GUGameBuyAmmoFailedDialogController(dialogInfo, this);
	}

	__returnToGameDialogController(dialogInfo)
	{
		return new GUReturnToGameDialogController(dialogInfo, this);
	}

	__gameForceSitOutDialogController(dialogInfo)
	{
		return new GUGameForceSitOutDialogController(dialogInfo, this);
	}

	__gameMidCompensateSWController(dialogInfo)
	{
		return new GUGameMidCompensateSWController(dialogInfo, this);
	}

	__gamePicksUpSpecialWeaponsFirstTimeDialogController(dialogInfo)
	{
		return new GUGamePicksUpSpecialWeaponsFirstTimeDialogController(dialogInfo, this);
	}

	__gameMidRoundExitDialogController(dialogInfo)
	{
		return new GUGameMidRoundExitDialogController(dialogInfo, this);
	}

	__bonusDialogController(dialogInfo)
	{
		return new GUBonusDialogController(dialogInfo, this);
	}

	__FRBDialogController(dialogInfo)
	{
		return new GUFRBDialogController(dialogInfo, this);
	}

	__tournamentStateDialogController(dialogInfo)
	{
		return new GUTournamentStateDialogController(dialogInfo, this);
	}

	__gameRebuyDialogController(dialogInfo)
	{
		return new GUGameRebuyDialogController(dialogInfo, this);
	}

	__gameNEMForRoomDialogController(dialogInfo)
	{
		return new GUGameNEMForRoomDialogController(dialogInfo, this);
	}

	__lobbyRebuyDialogController(dialogInfo)
	{
		return new GULobbyRebuyDialogController(dialogInfo, this);
	}

	__lobbyNEMDialogController(dialogInfo)
	{
		return new GULobbyNEMDialogController(dialogInfo, this);
	}

	__lobbyRebuyFailedDialogController(dialogInfo)
	{
		return new GULobbyRebuyFailedDialogController(dialogInfo, this);
	}

	__gameSWPurchaseLimitExceededDialogController(dialogInfo)
	{
		return new GUGameSWPurchaseLimitExceededDialogController(dialogInfo, this);
	}

	__lobbyInsufficientFundsDialogController(dialogInfo)
	{
		return new GULobbyInsufficientFundsDialogController(dialogInfo, this);
	}

	__gameBattlegroundNoWeaponsFiredDialogController(dialogInfo)
	{
		return new GUSGameBattlegroundNoWeaponsFiredDialogController(dialogInfo, this);
	}

	__generateBattlegroundRulesDialogController(dialogInfo)
	{
		return new GUSBattlegroundRulesDialogController(dialogInfo, this);
	}

	__generateBattlegroundBuyInConfirmationDialogController(dialogInfo)
	{
		return new GUSBattlegroundBuyInConfirmationDialogController(dialogInfo, this);
	}

	__generateBattlegroundNotEnoughPlayersDialogController(dialogInfo)
	{
		let l_lbnepsdc = new GUSLobbyBattlegroundNotEnoughPlayersDialogController(dialogInfo, this);
		l_lbnepsdc.on(GUSLobbyBattlegroundNotEnoughPlayersDialogController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING, this.emit, this);

		return l_lbnepsdc;
	}

	__generateRoundTransitionSWCompensationDialogController(dialogInfo)
	{
		return new GUSGameRoundTransitionSWCompesationDialogController(dialogInfo, this);
	}

	__gamePendingOperationFailedDialogController(dialogInfo)
	{
		return new GUGamePendingOperationFailedDialogController(dialogInfo, this);
	}

	__generateWaitPendingOperationDialogController(dialogInfo)
	{
		return new GUSWaitPendingOperationDialogController(dialogInfo, this);
	}

	__gamePleaseWaitDialogController(dialogInfo)
	{
		return new GUGamePleaseWaitDialogController(dialogInfo, this);
	}

	__generateGameBattlegroundContinueReadingDialogController(dialogInfo)
	{
		return new GUSGameBattlegroundContinueReadingDialogController(dialogInfo, this);
	}

	__generateRoomMovedErrorRequestsLimitDialogController(dialogInfo)
	{
		return new GUSRoomMovedErrorRequestsLimitDialogController(dialogInfo, this);
	}

}

export default GUSLobbyDialogsController