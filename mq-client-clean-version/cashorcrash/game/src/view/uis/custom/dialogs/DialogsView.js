import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import DialogsInfo from '../../../../model/uis/custom/dialogs/DialogsInfo';
import DialogView from './DialogView';
import NEMDialogView from './custom/game/NEMDialogView';
import CriticalErrorDialogView from './custom/CriticalErrorDialogView';
import GameSoundButtonView from '../secondary/GameSoundButtonView';
import RuntimeErrorDialogView from './custom/RuntimeErrorDialogView';
import RedirectionDialogView from './custom/RedirectionDialogView';
import WebSocketReconnectionAttemptDialogView from './custom/WebSocketReconnectionAttemptDialogView';
import GameView from '../../../main/GameView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BattlegroundNotEnoughPlayersDialogView from './custom/BattlegroundNotEnoughPlayersDialogView';
import BattlegroundRejoinDialogView from './custom/BattlegroundRejoinDialogView';
import BattlegroundCafRoomManagerDialogView from './custom/BattlegroundCafRoomManagerDialogView';
import BattlegroundCafRoomGuestDialogView from './custom/BattlegroundCafRoomGuestDialogView';
import BattlegroundCAFPlayerKickedDialogView from './custom/BattlegroundCAFPlayerKickedDialogView';
import BattlegroundCAFRoundAlreadyStartedDialogView from './custom/BattlegroundCAFRoundAlreadyStartedDialogView';
import BattlegroundCAFRoundSummaryDialogView from './custom/BattlegroundCAFRoundSummaryDialogView';
import BattlegroundCafRoomWasDeactivatedDialogView from './custom/BattlegroundCafRoomWasDeactivatedDialogView';

class DialogsView extends SimpleUIView
{
	static isDialogViewBlurForbidden(aDialogId_int)
	{
		switch(aDialogId_int)
		{
			case DialogsInfo.DIALOG_ID_REDIRECTION:
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

	get criticalErrorDialogView ()
	{
		return this._criticalErrorDialogView;
	}

	get reconnectDialogView ()
	{
		return this._reconnectDialogView;
	}

	get redirectionDialogView ()
	{
		return this._redirectionDialogView;
	}

	get webglContextLostDialogView ()
	{
		return this._webglContextLostDialogView;
	}

	get runtimeErrorDialogView ()
	{
		return this._runtimeErrorDialogView;
	}

	get gameNEMDialogView ()
	{
		return this._gameNEMDialogView;
	}

	get tooLateEjectDialogView ()
	{
		return this._tooLateEjectDialogView;
	}

	get roomNotFoundDialogView ()
	{
		return this._roomNotFoundDialogView;
	}

	get betFailedDialogView ()
	{
		return this._betFailedDialogView;
	}

	get leaveRoomDialogView ()
	{
		return this._leaveRoomDialogView;
	}

	get battlegroundNotEnoughPlayersDialogView ()
	{
		return this._battlegroundNotEnoughPlayersDialogView;
	}

	get battlegroundRejoinDialogView ()
	{
		return this._battlegroundRejoinDialogView;
	}

	get webSocketReconnectionAttemptDialogView ()
	{
		return this._webSocketReconnectionAttemptDialogView;
	}

	get roundAlreadyFinishedDialogView()
	{
		return this._roundAlreadyFinishedDialogView;
	}

	get waitPendingOperationDialogView ()
	{
		return this._waitPendingOperationDialogView;
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
		dialogView.zIndex = dialogId;
		this.addChild(dialogView);

		return dialogView;
	}

	__generateDialogView (dialogId)
	{
		var dialogView;
		switch (dialogId)
		{
			case DialogsInfo.DIALOG_ID_NETWORK_ERROR:
			case DialogsInfo.DIALOG_ID_RECONNECT:
			case DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
			case DialogsInfo.DIALOG_ID_TOO_LATE_EJECT:
			case DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
			case DialogsInfo.DIALOG_ID_BET_FAILED:
			case DialogsInfo.DIALOG_ID_LEAVE_ROOM:
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
			case DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogView = new DialogView();
				break;
			case DialogsInfo.DIALOG_ID_CRITICAL_ERROR:
				dialogView = new CriticalErrorDialogView();
				break;
			case DialogsInfo.DIALOG_ID_GAME_NEM:
				dialogView = new NEMDialogView();
				break;
			case DialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogView = new RuntimeErrorDialogView();
				break;
			case DialogsInfo.DIALOG_ID_REDIRECTION:
				dialogView = new RedirectionDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogView = new BattlegroundNotEnoughPlayersDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_REJOIN:
				dialogView = new BattlegroundRejoinDialogView();
				break;
			case DialogsInfo.DIALOG_ID_WEB_SOCKET_RECONNECT:
				dialogView = new WebSocketReconnectionAttemptDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER:
				dialogView = new BattlegroundCafRoomManagerDialogView();
				break;
			case DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST:
				dialogView = new BattlegroundCafRoomGuestDialogView();
				break;
			case DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED:
				dialogView = new BattlegroundCAFPlayerKickedDialogView();
				break;
			case DialogsInfo.DIALOG_ID_ROUND_ALREADY_STARTED:
				dialogView = new BattlegroundCAFRoundAlreadyStartedDialogView();
				break;
			case DialogsInfo.DIALOG_ID_CAF_ROUND_SUMMARY:
				dialogView = new BattlegroundCAFRoundSummaryDialogView();
				break;
			case DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED:
				dialogView = new BattlegroundCafRoomWasDeactivatedDialogView();
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
		let l_sbv = new GameSoundButtonView(1);
		l_sbv.zIndex = this.uiInfo.dialogsCount+10;
		this.addChild(l_sbv);

		return l_sbv;
	}

	_updateSoundButtonPosition()
	{
		let lScreenWidth_num = APP.screenWidth;
		let lScreenHeight_num = APP.screenHeight;

		let lSoundButtonView_sbv = this.soundButtonView;
		lSoundButtonView_sbv.position.set(-lScreenWidth_num/2+33, -lScreenHeight_num/2+66);

		if (APP.isMobile)
		{
			lSoundButtonView_sbv.scale.set(1.8);
			lSoundButtonView_sbv.position.y += 14;
			lSoundButtonView_sbv.position.x += 4;
		}
	}
	//...SOUND_BUTTON

	get _networkErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_NETWORK_ERROR);
	}

	get _criticalErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CRITICAL_ERROR);
	}

	get _reconnectDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_RECONNECT);
	}

	get _webglContextLostDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
	}

	get _runtimeErrorDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_RUNTIME_ERROR);
	}

	get _gameNEMDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_GAME_NEM);
	}

	get _redirectionDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_REDIRECTION);
	}

	get _tooLateEjectDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_TOO_LATE_EJECT);
	}

	get _roomNotFoundDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
	}

	get _betFailedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BET_FAILED);
	}

	get _leaveRoomDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_LEAVE_ROOM);
	}

	get _webSocketReconnectionAttemptDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_WEB_SOCKET_RECONNECT);
	}

	get _battlegroundRejoinDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_REJOIN);
	}

	get _battlegroundNotEnoughPlayersDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS);
	}	updateArea()
	{
		this._updateSoundButtonPosition();
	}

	get _roundAlreadyFinishedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED);
	}


	get _roundAlreadyStartedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_ROUND_ALREADY_STARTED);
	}

	
	get _cafRoundSummaryDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CAF_ROUND_SUMMARY);
	}

	get _waitPendingOperationDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_WAIT_PENDING_OPERATION);
	}

	get cafPlayerKickedDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CAF_PLAYER_KICKED);
	}


	get battlegroundCafRoomGuestDialogView()
	{
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST);
	}

	get battlegroundCafRoomManagerDialogView(){
		return this.__getDialogView(DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER);
	}

	get battlegroundCafRoomWasDeactivatedDialogView(){
		return this.__getDialogView(DialogsInfo.DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED);
	}


}

export default DialogsView;