import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DialogController from './DialogController';
import DialogsInfo from '../../../../model/uis/custom/dialogs/DialogsInfo';
import DialogsView from '../../../../view/uis/custom/dialogs/DialogsView';
import NetworkErrorDialogController from './custom/NetworkErrorDialogController';
import GameNetworkErrorDialogController from './custom/game/GameNetworkErrorDialogController';
import CriticalErrorDialogController from './custom/CriticalErrorDialogController';
import GameCriticalErrorDialogController from './custom/game/GameCriticalErrorDialogController';
import ReconnectDialogController from './custom/ReconnectDialogController';
import GameReconnectDialogController from './custom/game/GameReconnectDialogController';
import GameRoomReopenDialogController from './custom/game/GameRoomReopenDialogController';
import GameNEMDialogController from './custom/game/GameNEMDialogController';
import RedirectionDialogController from './custom/RedirectionDialogController';
import RoomNotFoundDialogController from './custom/RoomNotFoundDialogController';
import ReturnToGameDialogController from './custom/ReturnToGameDialogController';
import LobbySoundButtonController from '../secondary/LobbySoundButtonController';
import GameForceSitOutDialogController from './custom/game/GameForceSitOutDialogController';
import GameMidCompensateSWController from './custom/game/GameMidCompensateSWController';
import GameMidRoundExitDialogController from './custom/game/GameMidRoundExitDialogController';
import GameBuyAmmoFailedDialogController from './custom/game/GameBuyAmmoFailedDialogController';
import WebGLContextLostDialogController from './custom/WebGLContextLostDialogController';
import RuntimeErrorDialogController from './custom/RuntimeErrorDialogController';
import BonusDialogController from './custom/BonusDialogController';
import FRBDialogController from './custom/FRBDialogController';
import TournamentStateDialogController from './custom/TournamentStateDialogController';
import GameRebuyDialogController from './custom/game/GameRebuyDialogController';
import GameNEMForRoomDialogController from './custom/game/GameNEMForRoomDialogController';
import LobbyRebuyDialogController from './custom/LobbyRebuyDialogController';
import LobbyNEMDialogController from './custom/LobbyNEMDialogController';
import LobbyRebuyFailedDialogController from './custom/LobbyRebuyFailedDialogController';
import GameSWPurchaseLimitExceededDialogController from './custom/game/GameSWPurchaseLimitExceededDialogController';
import GameRoundTransitionSWCompesationDialogController from './custom/game/GameRoundTransitionSWCompesationDialogController';
import LobbyInsufficientFundsDialogController from './custom/LobbyInsufficientFundsDialogController';
import LobbyBattlegroundNotEnoughPlayersDialogController from './custom/LobbyBattlegroundNotEnoughPlayersDialogController';
import BattlegroundBuyInConfirmationDialogController from './custom/BattlegroundBuyInConfirmationDialogController';
import BattlegroundCafRoomManagerDialogController from './custom/BattlegroundCafRoomManagerDialogController';
import BattlegroundRulesDialogController from './custom/BattlegroundRulesDialogController';
import GameBattlegroundNoWeaponsFiredDialogController from './custom/game/GameBattlegroundNoWeaponsFiredDialogController';
import GameBattlegroundContinueReadingDialogController from './custom/game/GameBattlegroundContinueReadingDialogController';
import GamePendingOperationFailedDialogController from './custom/game/GamePendingOperationFailedDialogController';
import RoundAlreadyFinishedDialogController from './custom/RoundAlreadyFinishedDialogController';
import RoomMovedErrorRequestsLimitDialogController from './custom/RoomMovedErrorRequestsLimitDialogController';
import PleaseWaitDialogController from './custom/PleaseWaitDialogController';
import ServerRebootDialogController from './custom/ServerRebootDialogController';
import GameServerRebootDialogController from './custom/game/GameServerRebootDialogController';
import WaitPendingOperationDialogController from './custom/WaitPendingOperationDialogController';
import BattlegroundCAFPlayerKickedDialogController from './custom/BattlegroundCAFPlayerKickedDialogController';
import BattlegroundCafRoomWasDeactivatedDialogController from './custom/BattlegroundCafRoomWasDeactivatedDialogController';
import BattlegroundBuyInConfirmationDialogControllerCAF from './custom/BattlegroundBuyInConfirmationDialogControllerCAF';

class DialogsController extends SimpleUIController
{
	static get EVENT_DIALOG_ACTIVATED() {return DialogController.EVENT_DIALOG_ACTIVATED};
	static get EVENT_DIALOG_DEACTIVATED() {return DialogController.EVENT_DIALOG_DEACTIVATED};
	static get EVENT_ON_DIALOG_CHANGE_WORLD_BUY_IN_TRIGGERED() {return DialogController.EVENT_ON_DIALOG_CHANGE_WORLD_BUY_IN_TRIGGERED};
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING() {return LobbyBattlegroundNotEnoughPlayersDialogController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING};
	static get EVENT_BATTLEGROUND_RULES_SHOW_REQUIRED() {return "EVENT_BATTLEGROUND_RULES_SHOW_REQUIRED"};

	get soundButtonController()
	{
		return this._soundButtonController;
	}

	static _sortDialogsByPresentationPriority (dialog1, dialog2)
	{
		var firstDialogInfo = dialog1.info;
		var secondDialogInfo = dialog2.info;

		var firstDialogPriority = firstDialogInfo.priority;
		var secondDialogPriority = secondDialogInfo.priority;
		var lRet_num = secondDialogPriority - firstDialogPriority;
		if (!lRet_num)
		{
			var firstDialogActivationTime = firstDialogInfo.activationTime;
			var secondDialogActivationTime = secondDialogInfo.activationTime;

			lRet_num = firstDialogActivationTime - secondDialogActivationTime;
		}
		return lRet_num;
	}

	constructor(optInfo)
	{
		super(new DialogsInfo());

		this._dialogsControllers = null;
		this._fViewContainer_sprt = null;

		this._initDialogsController();

		this._soundButtonController.init();
	}

	initView(viewContainer)
	{
		this._fViewContainer_sprt = viewContainer;

		let view = new DialogsView();
		this._fViewContainer_sprt.addChild(view);

		let lSoundButtonView_sbv = this._fViewContainer_sprt.addChild(view.soundButtonView);
		lSoundButtonView_sbv.position.set(-432, -199);

		if (APP.isMobile)
		{
			lSoundButtonView_sbv.scale.set(1.8);
			lSoundButtonView_sbv.position.y += 14;
			lSoundButtonView_sbv.position.x += 4;
		}

		this._soundButtonController.initView(view.soundButtonView);

		super.initView(view);
	}
	
	get battlegroundBuyInConfirmationDialogController()
	{
		return this._battlegroundBuyInConfirmationDialogController;
	}
	
	get battlegroundBuyInConfirmationDialogControllerCAF()
    {
        return this._battlegroundBuyInConfirmationDialogControllerCAF;
    }

	get _soundButtonController()
	{
		return this._fSoundButtonController_sbc || (this._fSoundButtonController_sbc = new LobbySoundButtonController());
	}

	get battlegroundCafRoomManagerDialogController()
	{
		return this._battlegroundCafRoomManagerDialogController;
	}

	get viewContainer()
	{
		return this._fViewContainer_sprt;
	}

	destroy()
	{
		this._dialogsControllers = null;

		super.destroy();
	}

	get networkErrorDialogController()
	{
		return this._networkErrorDialogController;
	}

	get gameNetworkErrorDialogController()
	{
		return this._gameNetworkErrorDialogController;
	}

	get criticalErrorDialogController()
	{
		return this._criticalErrorDialogController;
	}

	get gameCriticalErrorDialogController()
	{
		return this._gameCriticalErrorDialogController;
	}

	get reconnectDialogController()
	{
		return this._reconnectDialogController;
	}

	get gameReconnectDialogController()
	{
		return this._gameReconnectDialogController;
	}

	get serverRebootDialogController()
	{
		return this._serverRebootDialogController;
	}

	get gameServerRebootDialogController()
	{
		return this._gameServerRebootDialogController;
	}

	get gameRoomReopenDialogController()
	{
		return this._gameRoomReopenDialogController;
	}

	get roomNotFoundDialogController()
	{
		return this._roomNotFoundDialogController;
	}

	get roomMovedErrorRequestsLimitDialogController()
	{
		return this._roomMovedErrorRequestsLimitDialogController;
	}

	get gameNEMDialogController()
	{
		return this._gameNEMDialogController;
	}

	get redirectionDialogController()
	{
		return this._redirectionDialogController;
	}

	get gameBuyAmmoFailedDialogController()
	{
		return this._gameBuyAmmoFailedDialogController;
	}

	get bonusDialogController()
	{
		return this._bonusDialogController;
	}

	get forceSitOutDialogController()
	{
		return this._forceSitOutDialogController;
	}

	get returnToGameDialogController()
	{
		return this._returnToGameDialogController;
	}

	get midRoundCompensateSWExitDialogController()
	{
		return this._midRoundCompensateSWExitDialogController;
	}

	get midRoundExitDialogController()
	{
		return this._midRoundExitDialogController;
	}

	get webglContextLostDialogController()
	{
		return this._webglContextLostDialogController;
	}

	get runtimeErrorDialogController()
	{
		return this._runtimeErrorDialogController;
	}

	get FRBDialogController()
	{
		return this._FRBDialogController;
	}

	get tournamentStateDialogController()
	{
		return this._tournamentStateDialogController;
	}

	get gameRebuyDialogController()
	{
		return this._gameRebuyDialogController;
	}

	get gameNEMForRoomDialogController()
	{
		return this._gameNEMForRoomDialogController;
	}

	get lobbyRebuyDialogController()
	{
		return this._lobbyRebuyDialogController;
	}

	get lobbyNEMDialogController()
	{
		return this._lobbyNEMDialogController;
	}

	get lobbyRebuyFailedDialogController()
	{
		return this._lobbyRebuyFailedDialogController;
	}

	get gameSWPurchaseLimitExceededDialogController()
	{
		return this._gameSWPurchaseLimitExceededDialogController;
	}

	get gameGameBattlegroundNoWeaponsFiredDialogController()
	{
		return this._gameGameBattlegroundNoWeaponsFiredDialogController;
	}

	get gameBattlegroundContinueReadingDialogController()
	{
		return this._gameBattlegroundContinueReadingDialogController;
	}

	get lobbyBattlegroundNotEnoughPlayersDialogController()
	{
		return this._lobbyBattlegroundNotEnoughPlayersDialogController;
	}

	get gamePendingOperationFailedDialogController()
	{
		return this._gamePendingOperationFailedDialogController;
	}

	get roundAlreadyFinishedDialogController()
	{
		return this._roundAlreadyFinishedDialogController;
	}


	get pleaseWaitDialogController()
	{
		return this._pleaseWaitDialogController;
	}

	get waitPendingOperationDialogController()
	{
		return this._waitPendingOperationDialogController;
	}

	get cafPlayerKickedDialogController()
	{
		return this._cafPlayerKickedDialogController;
	}

	get battlegroundCafRoomWasDeactivatedDialogController()
	{
		return this._battlegroundCafRoomWasDeactivatedDialogController;
	}

	_initDialogsController()
	{
		this._dialogsControllers = [];
	}

	__init ()
	{
		super.__init();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		var info = this.info;
		var dialogsAmount = info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var notificaionController = this.__getDialogController(i);
			notificaionController.init();
		}
	}

	__initViewLevel ()
	{
		super.__initViewLevel();

		var view = this.__fView_uo;
		var dialogsAmount = this.info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var dialogController = this.__getDialogController(i);
			if (dialogController.isViewLevelSelfInitializationMode)
			{
				if (!dialogController.hasView)
				{
					dialogController.initViewLevelSelfInitializationViewProvider(view);
				}
			}
			else
			{
				dialogController.initView(view.getDialogView(i));
			}
		}
	}

	__getDialogController (dialogId)
	{
		return this._dialogsControllers[dialogId] || this._initDialogController(dialogId);
	}

	_initDialogController (dialogId)
	{
		var dialogController = this.__generateDialogController(this.info.getDialogInfo(dialogId));
		this._dialogsControllers[dialogId] = dialogController;

		dialogController.on(DialogController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogController.on(DialogController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
		dialogController.on(DialogController.EVENT_ON_DIALOG_CHANGE_WORLD_BUY_IN_TRIGGERED, this.emit, this);

		return dialogController;
	}

	__generateDialogController (dialogInfo)
	{
		var dialogController;
		var dialogId = dialogInfo.dialogId;
		var dialogInfo = dialogInfo;

		switch (dialogId)
		{
			case DialogsInfo.DIALOG_ID_NETWORK_ERROR:
				dialogController = new NetworkErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR:
				dialogController = new GameNetworkErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_CRITICAL_ERROR: 
				dialogController = new CriticalErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR:
				dialogController = new GameCriticalErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_RECONNECT:
				dialogController = new ReconnectDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_RECONNECT:
				dialogController = new GameReconnectDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_SERVER_REBOOT:
				dialogController = new ServerRebootDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT:
				dialogController = new GameServerRebootDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN:
				dialogController = new GameRoomReopenDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
				dialogController = new RoomNotFoundDialogController(dialogInfo, this);
				break;				
			case DialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED:
				dialogController = new RoomMovedErrorRequestsLimitDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_NEM:
				dialogController = new GameNEMDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_REDIRECTION:
				dialogController = new RedirectionDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED:
				dialogController = new GameBuyAmmoFailedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_RETURN_TO_GAME:
				dialogController = new ReturnToGameDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_FORCE_SIT_OUT:
				dialogController = new GameForceSitOutDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW:
				dialogController = new GameMidCompensateSWController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_MID_ROUND_EXIT:
				dialogController = new GameMidRoundExitDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
				dialogController = new WebGLContextLostDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogController = new RuntimeErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BONUS:
				dialogController = new BonusDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_FRB:
				dialogController = new FRBDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_TOURNAMENT_STATE:
				dialogController = new TournamentStateDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_REBUY:
				dialogController = new GameRebuyDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM:
				dialogController = new GameNEMForRoomDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_LOBBY_REBUY:
				dialogController = new LobbyRebuyDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_LOBBY_NEM:
				dialogController = new LobbyNEMDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED:
				dialogController = new LobbyRebuyFailedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED:
				dialogController = new GameSWPurchaseLimitExceededDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION:
				dialogController = new GameRoundTransitionSWCompesationDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogController = new LobbyBattlegroundNotEnoughPlayersDialogController(dialogInfo, this);
				dialogController.on(LobbyBattlegroundNotEnoughPlayersDialogController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING, this.emit, this);
				break;
			case DialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS:
				dialogController = new LobbyInsufficientFundsDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
				dialogController = new BattlegroundBuyInConfirmationDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF:
				dialogController = new BattlegroundBuyInConfirmationDialogControllerCAF(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER:
				dialogController = new BattlegroundCafRoomManagerDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_RULES:
				dialogController = new BattlegroundRulesDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED:
				dialogController = new GameBattlegroundNoWeaponsFiredDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING:
				dialogController = new GameBattlegroundContinueReadingDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED:
				dialogController = new GamePendingOperationFailedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogController = new RoundAlreadyFinishedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_PLEASE_WAIT:
				dialogController = new PleaseWaitDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogController = new WaitPendingOperationDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED:
				dialogController = new BattlegroundCAFPlayerKickedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED:
				dialogController = new BattlegroundCafRoomWasDeactivatedDialogController(dialogInfo, this);
				break;
			default:
				throw new Error(`Unsupported dialog id: ${dialogId}`);
		}

		return dialogController;
	}

	_onDialogActivated (aEvent_ue)
	{
		this._updateDialogForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_onDialogDeactivated (aEvent_ue)
	{
		this._updateDialogForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_updateDialogForPresentationSettings ()
	{
		var sortedActiveDialogs = this._getActiveDialogsWithPresentationPrioritySorting();
		// console.log("sortedActiveDialogs", sortedActiveDialogs);
		var info = this.info;

		if (!sortedActiveDialogs)
		{
			info.dialogIdForPresentation = undefined;
		}
		else
		{
			info.dialogIdForPresentation = sortedActiveDialogs[0].info.dialogId;
		}
	}

	_getActiveDialogsWithPresentationPrioritySorting ()
	{
		var activeDialogs = null;
		var dialogsAmount = this.info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var dialogController = this.__getDialogController(i);
			if (dialogController.info.isActive)
			{
				activeDialogs = activeDialogs || [];
				activeDialogs.push(dialogController);
			}
		}

		if (activeDialogs)
		{
			activeDialogs.sort(DialogsController._sortDialogsByPresentationPriority);
		}

		return activeDialogs;
	}

	get _battlegroundBuyInConfirmationDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION);
	}

	get _battlegroundBuyInConfirmationDialogControllerCAF()
    {
        return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION_CAF);
    }

	get _battlegroundCafRoomManagerDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER);
	}

	get _networkErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_NETWORK_ERROR);
	}

	get _gameNetworkErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_NETWORK_ERROR);
	}

	get _criticalErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CRITICAL_ERROR);
	}

	get _gameCriticalErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_CRITICAL_ERROR);
	}

	get _reconnectDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_RECONNECT);
	}

	get _gameReconnectDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_RECONNECT);
	}

	get _serverRebootDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_SERVER_REBOOT);
	}

	get _gameServerRebootDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_SERVER_REBOOT);
	}

	get _gameRoomReopenDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_ROOM_REOPEN);
	}

	get _roomNotFoundDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
	}

	get _roomMovedErrorRequestsLimitDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED);
	}

	get _gameNEMDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_NEM);
	}

	get _redirectionDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_REDIRECTION);
	}

	get _gameBuyAmmoFailedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_BUY_AMMO_FAILED);
	}

	get _bonusDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BONUS);
	}

	get _forceSitOutDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_FORCE_SIT_OUT);
	}

	get _returnToGameDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_RETURN_TO_GAME);
	}

	get _midRoundCompensateSWExitDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_MID_ROUND_COMPENSATE_SW);
	}

	get _midRoundExitDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_MID_ROUND_EXIT);
	}

	get _webglContextLostDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
	}

	get _runtimeErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_RUNTIME_ERROR);
	}

	get _FRBDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_FRB);
	}

	get _tournamentStateDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_TOURNAMENT_STATE);
	}

	get _gameRebuyDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_REBUY);
	}

	get _gameNEMForRoomDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_NEM_FOR_ROOM);
	}

	get _lobbyRebuyDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_LOBBY_REBUY);
	}

	get _lobbyNEMDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_LOBBY_NEM);
	}

	get _lobbyRebuyFailedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_LOBBY_REBUY_FAILED);
	}

	get _gameSWPurchaseLimitExceededDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED);
	}

	get _gameGameBattlegroundNoWeaponsFiredDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_NO_WEAPONS_FIRED);
	}

	get _gameBattlegroundContinueReadingDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_CONTINUE_READING);
	}

	get _lobbyBattlegroundNotEnoughPlayersDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
	}

	get _gamePendingOperationFailedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_PENDING_OPERATION_FAILED);
	}

	get _roundAlreadyFinishedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED);
	}

	get _pleaseWaitDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_PLEASE_WAIT);
	}

	get _waitPendingOperationDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
	}

	get _cafPlayerKickedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED);
	}

	get _battlegroundCafRoomWasDeactivatedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED);
	}

	get lobbyInsufficientFundsDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_INSUFFICIENT_FUNDS);
	}
}

export default DialogsController