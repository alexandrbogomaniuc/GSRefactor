import GUDialogsView from "./GUDialogsView";
import GUSDialogsInfo from '../../../model/uis/custom/dialogs/GUSDialogsInfo';
import GUReturnToGameDialogView from "./custom/GUReturnToGameDialogView";
import GUGameForceSitOutDialogView from "./custom/game/GUGameForceSitOutDialogView";
import GUGameMidCompensateSWView from "./custom/game/GUGameMidCompensateSWView";
import GUGamePicksUpSpecialWeaponsFirstTimeDialogView from "./custom/game/GUGamePicksUpSpecialWeaponsFirstTimeDialogView";
import GUGameMidRoundExitDialogView from "./custom/game/GUGameMidRoundExitDialogView";
import GUBonusDialogView from "./custom/GUBonusDialogView";
import GUFRBDialogView from "./custom/GUFRBDialogView";
import GUGameRebuyDialogView from "./custom/game/GUGameRebuyDialogView";
import GUDialogView from "./GUDialogView";
import GUSLobbySoundButtonView from '../GUSLobbySoundButtonView';
import GUNEMDialogView from './custom/game/GUNEMDialogView';
import GUSGameBattlegroundNoWeaponsFiredDialogView from './custom/game/GUSGameBattlegroundNoWeaponsFiredDialogView';
import GUSBattlegroundRulesDialogView from './custom/GUSBattlegroundRulesDialogView';
import GUSBattlegroundBuyInConfirmationDialogView from './custom/GUSBattlegroundBuyInConfirmationDialogView';
import GUSLobbyBattlegroundNotEnoughPlayersDialogView from './custom/GUSLobbyBattlegroundNotEnoughPlayersDialogView';
import GUSGameRoundTransitionSWCompensationDialogView from './custom/game/GUSGameRoundTransitionSWCompensationDialogView';
import GUWaitPendingOperationDialogView from './custom/GUWaitPendingOperationDialogView';
import GUGamePleaseWaitDialogView from './custom/game/GUGamePleaseWaitDialogView';
import GUSGameBattlegroundContinueReadingDialogView from './custom/game/GUSGameBattlegroundContinueReadingDialogView';

class GUSLobbyDialogsView extends GUDialogsView
{
	static isDialogViewBlurForbidden(aDialogId_int)
	{
		switch(aDialogId_int)
		{
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN:
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
			case GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				return true;
		}

		return false;
	}
	
    constructor()
    {
        super();
    }

    get gameNetworkErrorDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR);
	}

	get gameCriticalErrorDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR);
	}

	get gameReconnectDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_RECONNECT);
	}

	get gameServerRebootDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT);
	}

	get gameRoomReopenDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN);
	}

	get gameForceSitOutDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_FORCE_SIT_OUT);
	}

	get gameBuyAmmoFailedDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED);
	}

	get bonusDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_BONUS);
	}

	get returnToGameDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_RETURN_TO_GAME);
	}

	get midRoundCompensateSWExitDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW);
	}

	get picksUpSpecialWeaponsFirstTimeDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME);
	}

	get midRoundExitDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_MID_ROUND_EXIT);
	}

	get FRBDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_FRB);
	}

	get tournamentStateDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE);
	}

	get gameRebuyDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_REBUY);
	}

	get gameNEMForRoomDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM);
	}

	get lobbyRebuyDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY);
	}

	get lobbyNEMDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_LOBBY_NEM);
	}

	get lobbyRebuyFailedDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED);
	}

	get gameSWPurchaseLimitExceededDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED);
	}

	get insufficientFundsDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS);
	}

	get gameGameBattlegroundNoWeaponsFiredDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED);
	}

	get battlegroundRulesDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES);
	}
	
	get battlegroundBuyInConfirmationDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION);
	}

	get battlegroundNotEnoughPlayersDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
	}

	get gameRoundTransitionSWCompensationDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION);
	}

	get gamePendingOperationFailedDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED);
	}

	get waitPendingOperationDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
	}

	get gamePleaseWaitDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_PLEASE_WAIT);
	}

	get gameBattlegroundContinueReadingDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING);
	}

	get roomMovedErrorRequestsLimitDialogView()
	{
		return this.__getDialogView(GUSDialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED);
	}

    __generateDialogView(dialogId)
	{
		var dialogView;
		switch (dialogId)
		{
			case GUSDialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR:
            case GUSDialogsInfo.DIALOG_ID_GAME_RECONNECT:
            case GUSDialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN:
            case GUSDialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED:
            case GUSDialogsInfo.DIALOG_ID_TOURNAMENT_STATE:
            case GUSDialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM:
            case GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED:
            case GUSDialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED:
			case GUSDialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT:
			case GUSDialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED:
			case GUSDialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED:
				dialogView = this.__generateStandardDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogView = this.__generateWaitPendingOperationDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR:
				dialogView = this.__generateCriticalErrorDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_RETURN_TO_GAME:
				dialogView = this.__generateReturnToGameDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_FORCE_SIT_OUT:
				dialogView = this.__generateForceSitOutDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW:
				dialogView = this.__generateMidCompensateSWDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME:
				dialogView = this.__generatePicksUpSpecialWeaponsFirstTimeDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_MID_ROUND_EXIT:
				dialogView = this.__generateMidRoundExitDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_BONUS:
				dialogView = this.__generateBonusDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_FRB:
				dialogView = this.__generateFRBDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_GAME_REBUY:
            case GUSDialogsInfo.DIALOG_ID_LOBBY_REBUY:
				dialogView = this.__generateRebuyDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_LOBBY_NEM:
				dialogView = this.__generateNEMDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS:
				dialogView = this.__generateLobbyInsufficientFundsDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				dialogView = this.__generateGameBattlegroundNoWeaponsFiredDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
				dialogView = this.__generateBattlegroundRulesDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
				dialogView = this.__generateBattlegroundBuyInConfirmationDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogView = this.__generateBattlegroundNotEnoughPlayersDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION:
				dialogView = this.__generateRoundTransitionSWCompensationDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_PLEASE_WAIT:
				dialogView = this.__generateGamePleaseWaitDialogViewInstance();
				break;
			case GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING:
				dialogView = this.__generateGameBattlegroundContinueReadingDialogViewInstance();
				break;
			default:
				dialogView = super.__generateDialogView(dialogId);
				break;
		}

		return dialogView;
	}

	get __midRoundExitBackAssetName()
	{
		return undefined;
	}

	get __forceSitOutBackAssetName()
	{
		return undefined;
	}

    __generateReturnToGameDialogViewInstance()
    {
        return new GUReturnToGameDialogView(this.__midRoundExitBackAssetName);
    }

    __generateForceSitOutDialogViewInstance()
    {
        return new GUGameForceSitOutDialogView(this.__forceSitOutBackAssetName);
    }

    __generateMidCompensateSWDialogViewInstance()
    {
        return new GUGameMidCompensateSWView(this.__forceSitOutBackAssetName);
    }

    __generatePicksUpSpecialWeaponsFirstTimeDialogViewInstance()
    {
        return new GUGamePicksUpSpecialWeaponsFirstTimeDialogView(this.__forceSitOutBackAssetName);
    }

    __generateMidRoundExitDialogViewInstance()
    {
        return new GUGameMidRoundExitDialogView(this.__midRoundExitBackAssetName);
    }

    __generateBonusDialogViewInstance()
    {
        return new GUBonusDialogView();
    }

    __generateFRBDialogViewInstance()
    {
        return new GUFRBDialogView();
    }

    __generateRebuyDialogViewInstance()
    {
        return new GUGameRebuyDialogView();
    }

    __generateLobbyInsufficientFundsDialogViewInstance()
    {
        return new GUNEMDialogView();
    }

    __generateGameBattlegroundNoWeaponsFiredDialogViewInstance()
    {
    	return new GUSGameBattlegroundNoWeaponsFiredDialogView();
    }

    __generateBattlegroundRulesDialogViewInstance()
    {
    	return new GUSBattlegroundRulesDialogView();
    }

    __generateBattlegroundBuyInConfirmationDialogViewInstance()
    {
    	return new GUSBattlegroundBuyInConfirmationDialogView();
    }

    __generateBattlegroundNotEnoughPlayersDialogViewInstance()
    {
    	return new GUSLobbyBattlegroundNotEnoughPlayersDialogView();
    }

    __generateRoundTransitionSWCompensationDialogViewInstance()
    {
    	return new GUSGameRoundTransitionSWCompensationDialogView();
    }

    __generateWaitPendingOperationDialogViewInstance()
	{
		return new GUWaitPendingOperationDialogView();
	}

	__generateGamePleaseWaitDialogViewInstance()
	{
		return new GUGamePleaseWaitDialogView();
	}

	__generateGameBattlegroundContinueReadingDialogViewInstance()
	{
		return new GUSGameBattlegroundContinueReadingDialogView();
	}

    //SOUND_BUTTON...
	__provideSoundButtonViewInstance()
	{
		return new GUSLobbySoundButtonView(false, 1);
	}
	//...SOUND_BUTTON
}

export default GUSLobbyDialogsView