import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import DialogsInfo from '../../../../model/uis/custom/dialogs/DialogsInfo';
import DialogView from './DialogView';
import RedirectionDialogView from './custom/RedirectionDialogView';
import NEMDialogView from './custom/game/NEMDialogView';
import CriticalErrorDialogView from './custom/CriticalErrorDialogView';
import ReturnToGameDialogView from './custom/ReturnToGameDialogView';
import LobbySoundButtonView from '../secondary/LobbySoundButtonView';
import GameForceSitOutDialogView from './custom/game/GameForceSitOutDialogView';
import GameMidCompensateSWView from './custom/game/GameMidCompensateSWView';
import GamePicksUpSpecialWeaponsFirstTimeDialogView from './custom/game/GamePicksUpSpecialWeaponsFirstTimeDialogView';
import GameMidRoundExitDialogView from './custom/game/GameMidRoundExitDialogView';
import RuntimeErrorDialogView from './custom/RuntimeErrorDialogView';
import BonusDialogView from './custom/BonusDialogView';
import FRBDialogView from './custom/FRBDialogView';
import GameRebuyDialogView from './custom/game/GameRebuyDialogView';
import GameRoundTransitionSWCompensationDialogView from './custom/game/GameRoundTransitionSWCompensationDialogView';
import LobbyBattlegroundNotEnoughPlayersDialogView from './custom/LobbyBattlegroundNotEnoughPlayersDialogView';
import BattlegroundBuyInConfirmationDialogView from './custom/BattlegroundBuyInConfirmationDialogView';
import BattlegroundBuyInConfirmationDialogViewCAF from './custom/BattlegroundBuyInConfirmationDialogViewCAF';
import BattlegroundCafRoomManagerDialogView from './custom/BattlegroundCafRoomManagerDialogView';
import BattlegroundRulesDialogView from './custom/BattlegroundRulesDialogView';
import GameBattlegroundNoWeaponsFiredDialogView from './custom/game/GameBattlegroundNoWeaponsFiredDialogView';
import GameBattlegroundContinueReadingDialogView from './custom/game/GameBattlegroundContinueReadingDialogView';
import WaitPendingOperationDialogView from './custom/WaitPendingOperationDialogView';
import BattlegroundCAFPlayerKickedDialogView from './custom/BattlegroundCAFPlayerKickedDialogView';
import BattlegroundCafRoomWasDeactivatedDialogView from './custom/BattlegroundCafRoomWasDeactivatedDialogView';
import PleaseWaitDialogView from './custom/PleaseWaitDialogView';

class DialogsView extends SimpleUIView
{
	static isDialogViewBlurForbidden(aDialogId_int)
	{
		switch(aDialogId_int)
		{
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER:
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN:
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
			case DialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				return true;
		}

		return false;
	}

	get soundButtonView()
	{
		return this._soundButtonView;
	}

	constructor()
	{
		super();

		this.dialogsViews = null;

		this._initDialogsView();
	}

	destroy()
	{
		this.dialogsViews = null;

		super.destroy();
	}

	get networkErrorDialogView ()
	{
		return this._networkErrorDialogView;
	}

	get gameNetworkErrorDialogView ()
	{
		return this._gameNetworkErrorDialogView;
	}

	get criticalErrorDialogView ()
	{
		return this._criticalErrorDialogView;
	}

	get gameCriticalErrorDialogView ()
	{
		return this._gameCriticalErrorDialogView;
	}

	get reconnectDialogView ()
	{
		return this._reconnectDialogView;
	}

	get gameReconnectDialogView ()
	{
		return this._gameReconnectDialogView;
	}

	get serverRebootDialogView()
	{
		return this._serverRebootDialogView;
	}

	get gameServerRebootDialogView()
	{
		return this._gameServerRebootDialogView;
	}

	get gameRoomReopenDialogView ()
	{
		return this._gameRoomReopenDialogView;
	}

	get roomNotFoundDialogView ()
	{
		return this._roomNotFoundDialogView;
	}

	get roomMovedErrorRequestsLimitDialogView ()
	{
		return this._roomMovedErrorRequestsLimitDialogView;
	}

	get gameNEMDialogView ()
	{
		return this._gameNEMDialogView;
	}

	get redirectionDialogView ()
	{
		return this._redirectionDialogView;
	}

	get gameBuyAmmoFailedDialogView ()
	{
		return this._gameBuyAmmoFailedDialogView;
	}

	get gameForceSitOutDialogView()
	{
		return this._gameForceSitOutDialogView;
	}

	get returnToGameDialogView()
	{
		return this._returnToGameDialogView;
	}

	get roundTransitionSWCompensationDialogView()
	{
		return this._roundTransitionSWCompensationDialogView;
	}

	get insufficientFundsDialogView()
	{
		return this._insufficientFundsDialogView;
	}

	get battlegroundNotEnoughPlayersDialogView()
	{
		return this._battlegroundNotEnoughPlayersDialogView;
	}

	get battlegroundBuyInConfirmationDialogView()
	{
		return this._battlegroundBuyInConfirmationDialogView;
	}

	get battlegroundBuyInConfirmationDialogViewCAF()
    {
        return  this._battlegroundBuyInConfirmationDialogViewCAF;
    }

	get battlegroundCafRoomManagerDialogView()
	{
		return this._battlegroundCafRoomManagerDialogView;
	}

	get battlegroundRulesDialogView()
	{
		return this._battlegroundRulesDialogView;
	}

	get midCompensateSWDialogView()
	{
		return this._midCompensateSWDialogView;
	}

	get picksUpSpecialWeaponsFirstTimeDialogView()
	{
		return this._picksUpSpecialWeaponsFirstTimeDialogView;
	}

	get midRoundExitDialogView()
	{
		return this._midRoundExitDialogView;
	}

	get webglContextLostDialogView ()
	{
		return this._webglContextLostDialogView;
	}

	get bonusDialogView()
	{
		return this._bonusDialogView;
	}

	get FRBDialogView ()
	{
		return this._FRBDialogView;
	}

	get tournamentStateDialogView ()
	{
		return this._tournamentStateDialogView;
	}

	get gameRebuyDialogView ()
	{
		return this._gameRebuyDialogView;
	}

	get gameNEMForRoomDialogView()
	{
		return this._gameNEMForRoomDialogView;
	}

	get lobbyRebuyDialogView ()
	{
		return this._lobbyRebuyDialogView;
	}

	get lobbyNEMDialogView ()
	{
		return this._lobbyNEMDialogView;
	}

	get lobbyRebuyFailedDialogView ()
	{
		return this._lobbyRebuyFailedDialogView;
	}

	get gameSWPurchaseLimitExceededDialogView()
	{
		return this._gameSWPurchaseLimitExceededDialogView;
	}

	get gameGameBattlegroundNoWeaponsFiredDialogView()
	{
		return this._gameGameBattlegroundNoWeaponsFiredDialogView;
	}

	get gameBattlegroundContinueReadingDialogView()
	{
		return this._gameBattlegroundContinueReadingDialogView;
	}

	get gamePendingOperationFailedDialogView()
	{
		return this._gamePendingOperationFailedDialogView;
	}

	get roundAlreadyFinishedDialogView()
	{
		return this._roundAlreadyFinishedDialogView;
	}

	get gameCafRoomManagerTransferredDialogView()
	{
		return this._gameCafRoomManagerTransferredDialogView;
	}

	get pleaseWaitDialogView()
	{
		return this._pleaseWaitDialogView;
	}

	get waitPendingOperationDialogView ()
	{
		return this._waitPendingOperationDialogView;
	}

	get cafPlayerKickedDialogView ()
	{
		return this._cafPlayerKickedDialogView;
	}

	get battlegroundCafRoomWasDeactivatedDialogView()
	{
		return this._battlegroundCafRoomWasDeactivatedDialogView;
	}

	getDialogView (dialogId)
	{
		return this.__getDialogView(dialogId);
	}

	_initDialogsView()
	{
		this.dialogsViews = [];
	}

	__getDialogView (dialogId)
	{
		return this.dialogsViews[dialogId] || this._initDialogView(dialogId);
	}

	_initDialogView (dialogId)
	{
		var dialogView = this.__generateDialogView(dialogId);

		this.dialogsViews[dialogId] = dialogView;
		this.addChild(dialogView);

		return dialogView;
	}

	__generateDialogView (dialogId)
	{
		var dialogView;
		switch (dialogId)
		{
			case DialogsInfo.DIALOG_ID_NETWORK_ERROR:
			case DialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR:
			case DialogsInfo.DIALOG_ID_RECONNECT:
			case DialogsInfo.DIALOG_ID_GAME_RECONNECT:
			case DialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN:
			case DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
			case DialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED:
			case DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
			case DialogsInfo.DIALOG_ID_TOURNAMENT_STATE:
			case DialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM:
			case DialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED:
			case DialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED:
			case DialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED:
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
			case DialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED:
			case DialogsInfo.DIALOG_ID_SERVER_REBOOT:
			case DialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT:
				dialogView = new DialogView();
				break;
			case DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogView = new WaitPendingOperationDialogView();
				break;
			case DialogsInfo.DIALOG_ID_CRITICAL_ERROR:
			case DialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR:
				dialogView = new CriticalErrorDialogView();
				break;
			case DialogsInfo.DIALOG_ID_REDIRECTION:
				dialogView = new RedirectionDialogView();
				break;
			case DialogsInfo.DIALOG_ID_GAME_NEM:
			case DialogsInfo.DIALOG_ID_LOBBY_NEM:
				dialogView = new NEMDialogView();
				break;
			case DialogsInfo.DIALOG_ID_RETURN_TO_GAME:
				dialogView = new ReturnToGameDialogView();
				break;
			case DialogsInfo.DIALOG_ID_FORCE_SIT_OUT:
				dialogView = new GameForceSitOutDialogView();
				break;
			case DialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW:
				dialogView = new GameMidCompensateSWView();
				break;
			case DialogsInfo.DIALOG_ID_MID_ROUND_EXIT:
				dialogView = new GameMidRoundExitDialogView();
				break;
			case DialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogView = new RuntimeErrorDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BONUS:
				dialogView = new BonusDialogView();
				break;
			case DialogsInfo.DIALOG_ID_FRB:
				dialogView = new FRBDialogView();
				break;
			case DialogsInfo.DIALOG_ID_GAME_REBUY:
			case DialogsInfo.DIALOG_ID_LOBBY_REBUY:
				dialogView = new GameRebuyDialogView();
				break;
			case DialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION:
				dialogView = new GameRoundTransitionSWCompensationDialogView();
				break;
			case DialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS:
				dialogView = new NEMDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogView = new LobbyBattlegroundNotEnoughPlayersDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
				dialogView = new BattlegroundBuyInConfirmationDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF:
				dialogView = new BattlegroundBuyInConfirmationDialogViewCAF();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER:
				dialogView = new BattlegroundCafRoomManagerDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
				dialogView = new BattlegroundRulesDialogView();
				break;
			case DialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				dialogView = new GameBattlegroundNoWeaponsFiredDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING:
				dialogView = new GameBattlegroundContinueReadingDialogView();
				break;
			case DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED:
				dialogView = new BattlegroundCAFPlayerKickedDialogView();
				break;
			case DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED:
				dialogView = new BattlegroundCafRoomWasDeactivatedDialogView();
				break;
			case DialogsInfo.DIALOG_ID_PLEASE_WAIT:
				dialogView = new PleaseWaitDialogView();
				break;
			default:
				throw new Error (`Unsupported dialog id: ${dialogId}`);
		}
		return dialogView;
	}

	//SOUND_BUTTON...
	get _soundButtonView()
	{
		return this._fSoundButtonView_sbv || (this._fSoundButtonView_sbv = this._initSoundButtonView());
	}

	_initSoundButtonView()
	{
		let l_sbv = new LobbySoundButtonView(false, 1);

		return l_sbv;
	}
	//...SOUND_BUTTON

	get _battlegroundNotEnoughPlayersDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
	}

	get _insufficientFundsDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS);
	}

	get _battlegroundBuyInConfirmationDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION);
	}
    get _battlegroundBuyInConfirmationDialogViewCAF()
    {
        return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF);
    }
	get _battlegroundCafRoomManagerDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER);
	}

	get _battlegroundRulesDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_RULES);
	}

	get _roundTransitionSWCompensationDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION);
	}

	get _networkErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_NETWORK_ERROR);
	}

	get _gameNetworkErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR);
	}

	get _criticalErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CRITICAL_ERROR);
	}

	get _gameCriticalErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR);
	}

	get _reconnectDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_RECONNECT);
	}

	get _gameReconnectDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_RECONNECT);
	}

	get _serverRebootDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_SERVER_REBOOT);
	}

	get _gameServerRebootDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT);
	}

	get _gameRoomReopenDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN);
	}

	get _roomNotFoundDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
	}

	get _roomMovedErrorRequestsLimitDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED);
	}

	get _gameNEMDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_NEM);
	}

	get _redirectionDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_REDIRECTION);
	}

	get _gameBuyAmmoFailedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED);
	}

	get _gameForceSitOutDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_FORCE_SIT_OUT);
	}

	get _returnToGameDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_RETURN_TO_GAME);
	}

	get _midCompensateSWDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW);
	}

	get _picksUpSpecialWeaponsFirstTimeDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME);
	}

	get _midRoundExitDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_MID_ROUND_EXIT);
	}

	get _webglContextLostDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
	}

	get runtimeErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_RUNTIME_ERROR);
	}

	get bonusDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BONUS);
	}

	get _FRBDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_FRB);
	}

	get _tournamentStateDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_TOURNAMENT_STATE);
	}

	get _gameRebuyDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_REBUY);
	}
	
	get _lobbyRebuyDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_LOBBY_REBUY);
	}

	get _gameNEMForRoomDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM);
	}

	get _lobbyNEMDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_LOBBY_NEM);
	}

	get _lobbyRebuyFailedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED);
	}

	get _gameSWPurchaseLimitExceededDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED);
	}

	get _gameGameBattlegroundNoWeaponsFiredDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED);
	}

	get _gameBattlegroundContinueReadingDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING);
	}

	get _gamePendingOperationFailedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED);
	}

	get _roundAlreadyFinishedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED);
	}

	get _gameCafRoomManagerTransferredDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CAF_MANAGER_TRANSFERRED);
	}

	get _pleaseWaitDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_PLEASE_WAIT);
	}

	get _waitPendingOperationDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
	}

	get _cafPlayerKickedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED);
	}

	get _battlegroundCafRoomWasDeactivatedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED);
	}

}

export default DialogsView;