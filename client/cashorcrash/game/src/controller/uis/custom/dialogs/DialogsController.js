import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DialogController from './DialogController';
import DialogsInfo from '../../../../model/uis/custom/dialogs/DialogsInfo';
import DialogsView from '../../../../view/uis/custom/dialogs/DialogsView';
import NetworkErrorDialogController from './custom/NetworkErrorDialogController';
import CriticalErrorDialogController from './custom/CriticalErrorDialogController';
import ReconnectDialogController from './custom/ReconnectDialogController';
import RedirectionDialogController from './custom/RedirectionDialogController';
import GameSoundButtonController from '../secondary/GameSoundButtonController';
import WebGLContextLostDialogController from './custom/WebGLContextLostDialogController';
import RuntimeErrorDialogController from './custom/RuntimeErrorDialogController';
import GameNEMDialogController from './custom/GameNEMDialogController';
import TooLateEjectDialogController from './custom/TooLateEjectDialogController';
import RoomNotFoundDialogController from './custom/RoomNotFoundDialogController';
import BetFailedDialogController from './custom/BetFailedDialogController';
import LeaveRoomDialogController from './custom/LeaveRoomDialogController';
import BattlegroundNotEnoughPlayersDialogController from './custom/BattlegroundNotEnoughPlayersDialogController';
import WebSocketReconnectionAttemptDialogController from './custom/WebSocketReconnectionAttemptDialogController';
import DOMLayout from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import CrashAPP from '../../../../CrashAPP';
import RoundAlreadyFinishedDialogController from './custom/RoundAlreadyFinishedDialogController';
import WaitPendingOperationDialogController from './custom/WaitPendingOperationDialogController';
import BattlegroundRejoinDialogController from './custom/BattlegroundRejoinDialogController';
import BattlegroundCafRoomManagerDialogController from '../battleground/caf/BattlegroundCafRoomManagerDialogController';
import BattlegroundCafRoomGuestDialogController from '../battleground/caf/BattlegroundCafRoomGuestDialogController';
import BattlegroundCafRoomWasDeactivatedDialogController from '../battleground/caf/BattlegroundCafRoomWasDeactivatedDialogController';
import BattlegroundCAFPlayerKickedDialogController from '../battleground/caf/BattlegroundCAFPlayerKickedDialogController';
import BattlegroundCAFRoundAlreadyStartedDialogController from  '../battleground/caf/BattlegroundCAFRoundAlreadyStartedDialogController';
import BattlegroundCAFRoundSummaryDialogController from '../battleground/caf/BattlegroundCAFRoundSummaryDialogController';

let CUR_DEBUG_DLG_INDEX = 0;

class DialogsController extends SimpleUIController
{
	static get EVENT_DIALOG_ACTIVATED() {return DialogController.EVENT_DIALOG_ACTIVATED};
	static get EVENT_DIALOG_DEACTIVATED() {return DialogController.EVENT_DIALOG_DEACTIVATED};

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

		super.initView(view);

		this._soundButtonController.initView(view.soundButtonView);

		this.view.updateArea();

		this.view.hide();
	}



	get _soundButtonController()
	{
		return this._fSoundButtonController_sbc || (this._fSoundButtonController_sbc = new GameSoundButtonController());
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

	get criticalErrorDialogController()
	{
		return this._criticalErrorDialogController;
	}

	get reconnectDialogController()
	{
		return this._reconnectDialogController;
	}

	get redirectionDialogController()
	{
		return this._redirectionDialogController;
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

	get gameNEMDialogController()
	{
		return this._gameNEMDialogController;
	}

	get tooLateEjectDialogController()
	{
		return this._tooLateEjectDialogController;
	}

	get roomNotFoundDialogController()
	{
		return this._roomNotFoundDialogController;
	}

	get betFailedDialogController()
	{
		return this._betFailedDialogController;
	}

	get leaveRoomDialogController()
	{
		return this._leaveRoomDialogController;
	}

	get webSocketReconnectionAttemptDialogController()
	{
		return this._webSocketReconnectionAttemptDialogController;
	}

	get battlegroundNotEnoughPlayersDialogController()
	{
		return this._battlegroundNotEnoughPlayersDialogController;
	}
	
	get battlegroundRejoinDialogController()
	{
		return this._battlegroundRejoinDialogController;
	}

	get roundAlreadyFinishedDialogController()
	{
		return this._roundAlreadyFinishedDialogController;
	}

	get waitPendingOperationDialogController()
	{
		return this._waitPendingOperationDialogController;
	}

	get battlegroundCAFRoundAlreadyStartedDialogController()
	{
		return this._battlegroundCAFRoundAlreadyStartedDialogController;
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

		APP.once(CrashAPP.EVENT_ON_GAME_PRELOADER_REMOVED, this._onGamePreloaderRemoved, this);
		APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);
		
		var info = this.info;
		var dialogsAmount = info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var notificaionController = this.__getDialogController(i);
			notificaionController.init();
		}

		// DEBUG...
		 // hotkeys("d,o", this.onDlgHotkeyPressed.bind(this));
		// ...DEBUG
	}

	// DEBUG...
	onDlgHotkeyPressed(event, handler) 
	{
		if (hotkeys.isPressed('d'))
		{
			var dialogsAmount = this.info.dialogsCount;
			for (var i = 0; i < dialogsAmount; i++)
			{
				var notificaionController = this.__getDialogController(i);
				notificaionController.debugDeactivateDialog();
			}

			if (hotkeys.isPressed('o'))
			{
				if (this.__getDialogController(CUR_DEBUG_DLG_INDEX).isDlgDebugCycleCompleted)
				{
					this.__getDialogController(CUR_DEBUG_DLG_INDEX).resetDebugMessage();

					for (let i=1; i<dialogsAmount; i++)
					{
						CUR_DEBUG_DLG_INDEX++;

						if (CUR_DEBUG_DLG_INDEX >= dialogsAmount)
						{
							CUR_DEBUG_DLG_INDEX = 0;
						}
						
						if (this.__getDialogController(CUR_DEBUG_DLG_INDEX).isDebugMessageRequired)
						{
							break;
						}
					}
					
				}

				this.__getDialogController(CUR_DEBUG_DLG_INDEX).debugActivateDialog();
			}
		}
	}
	// ...DEBUG

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

	_onGamePreloaderRemoved(event)
	{
		this._fViewContainer_sprt && this._fViewContainer_sprt.addChild(this.view);
		this.view.soundButtonView.hide();
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
			case DialogsInfo.DIALOG_ID_CRITICAL_ERROR: 
				dialogController = new CriticalErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_RECONNECT:
				dialogController = new ReconnectDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_REDIRECTION:
				dialogController = new RedirectionDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
				dialogController = new WebGLContextLostDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogController = new RuntimeErrorDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_GAME_NEM:
				dialogController = new GameNEMDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_TOO_LATE_EJECT:
				dialogController = new TooLateEjectDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
				dialogController = new RoomNotFoundDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BET_FAILED:
				dialogController = new BetFailedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_LEAVE_ROOM:
				dialogController = new LeaveRoomDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_WEB_SOCKET_RECONNECT:
				dialogController = new WebSocketReconnectionAttemptDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogController = new BattlegroundNotEnoughPlayersDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_REJOIN:
				dialogController = new BattlegroundRejoinDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogController = new RoundAlreadyFinishedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogController = new WaitPendingOperationDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER:
				dialogController = new BattlegroundCafRoomManagerDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST:
				dialogController = new BattlegroundCafRoomGuestDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED:
				dialogController = new BattlegroundCAFPlayerKickedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_STARTED:
				dialogController = new BattlegroundCAFRoundAlreadyStartedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_STARTED:
				dialogController = new BattlegroundCAFRoundAlreadyStartedDialogController(dialogInfo, this);
				break;
			case DialogsInfo.DIALOG_ID_CAF_ROUND_SUMMARY:
				dialogController = new BattlegroundCAFRoundSummaryDialogController(dialogInfo, this);
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
		this.view && this.view.show();
		this.emit(aEvent_ue);
	}

	_onDialogDeactivated (aEvent_ue)
	{
		this._updateDialogForPresentationSettings();

		if (!this.info.hasActiveDialog)
		{
			this.view.hide();
		}

		this.emit(aEvent_ue);
	}

	_updateDialogForPresentationSettings ()
	{
		var sortedActiveDialogs = this._getActiveDialogsWithPresentationPrioritySorting();
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

	get _networkErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_NETWORK_ERROR);
	}


	get _cafRoundSummaryDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CAF_ROUND_SUMMARY);
	}

	get _criticalErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CRITICAL_ERROR);
	}

	get _reconnectDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_RECONNECT);
	}

	get _redirectionDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_REDIRECTION);
	}

	get _cafRoomManagerController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER);
	}

	
	get _cafRoomKickedController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED);
	}

	
	get _cafGuestController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST);
	}


	get _webglContextLostDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
	}

	get _runtimeErrorDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_RUNTIME_ERROR);
	}

	get _gameNEMDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_GAME_NEM);
	}

	get _tooLateEjectDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_TOO_LATE_EJECT);
	}

	get _roomNotFoundDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
	}

	get _betFailedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BET_FAILED);
	}

	get _leaveRoomDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_LEAVE_ROOM);
	}

	get _webSocketReconnectionAttemptDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_WEB_SOCKET_RECONNECT);
	}

	get _battlegroundNotEnoughPlayersDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
	}	
	
	get _battlegroundRejoinDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_BATTLEGROUND_REJOIN);
	}

	get _roundAlreadyFinishedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED);
	}

	get _waitPendingOperationDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
	}

	get _battlegroundCAFRoundAlreadyStartedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_ROUND_ALREADY_STARTED);
	}

	get _battlegroundCafRoomWasDeactivatedDialogController()
	{
		return this.__getDialogController(DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED);
	}
	

	

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		this.view && this.view.updateArea();
	}
	//...ORIENTATION
}

export default DialogsController