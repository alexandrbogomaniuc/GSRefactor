import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import DialogInfo from './DialogInfo';
import NetworkErrorDialogInfo from './custom/NetworkErrorDialogInfo';
import CriticalErrorDialogInfo from './custom/CriticalErrorDialogInfo';
import ReconnectDialogInfo from './custom/ReconnectDialogInfo';
import RedirectionDialogInfo from './custom/RedirectionDialogInfo';
import RuntimeErrorDialogInfo from './custom/RuntimeErrorDialogInfo';
import GameNEMDialogInfo from './custom/GameNEMDialogInfo';
import TooLateEjectDialogInfo from './custom/TooLateEjectDialogInfo';
import RoomNotFoundDialogInfo from './custom/RoomNotFoundDialogInfo';
import BetFailedDialogInfo from './custom/BetFailedDialogInfo';
import LeaveRoomDialogInfo from './custom/LeaveRoomDialogInfo';
import BattlegroundNotEnoughPlayersDialogInfo from './custom/BattlegroundNotEnoughPlayersDialogInfo';
import WebSocketReconnectionAttemptDialogInfo from './custom/WebSocketReconnectionAttemptDialogInfo';
import RoundAlreadyFinishedDialogInfo from './custom/RoundAlreadyFinishedDialogInfo';
import WaitPendingOperationDialogInfo from './custom/WaitPendingOperationDialogInfo';

import
{ 
	DIALOG_ID_NETWORK_ERROR,
	DIALOG_ID_CRITICAL_ERROR,
	DIALOG_ID_RECONNECT,
	DIALOG_ID_REDIRECTION,
	DIALOG_ID_WEBGL_CONTEXT_LOST,
	DIALOG_ID_RUNTIME_ERROR,
	DIALOG_ID_GAME_NEM,
	DIALOG_ID_TOO_LATE_EJECT,
	DIALOG_ID_ROOM_NOT_FOUND,
	DIALOG_ID_BET_FAILED,
	DIALOG_ID_LEAVE_ROOM,
	DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS,
	DIALOG_ID_WEB_SOCKET_RECONNECT,
	DIALOG_ID_ROUND_ALREADY_FINISHED,
	DIALOG_ID_WAIT_PENDING_OPERATION,
	DIALOGS_IDS_COUNT,
	DIALOG_ID_BATTLEGROUND_REJOIN,
	DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER,
	DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST,
	DIALOG_ID_CAF_PLAYER_KICKED,
	DIALOG_ID_ROUND_ALREADY_STARTED,
	DIALOG_ID_CAF_ROUND_SUMMARY, 
	DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED
} from '../../../../config/Constants';
import BattlegroundRejoinDialogInfo from './custom/BattlegroundRejoinDialogInfo';
import BattlegroundCafRoomManagerDialogInfo from './custom/BattlegroundCafRoomManagerDialogInfo';
import BattlegroundCafRoomGuestDialogInfo from './custom/BattlegroundCafRoomGuestDialogInfo';
/*

*/
let DIALOGS_AMOUNT = DIALOGS_IDS_COUNT;

class DialogsInfo extends SimpleUIInfo
{
	static get DIALOG_ID_NETWORK_ERROR() 							{ return DIALOG_ID_NETWORK_ERROR;}
	static get DIALOG_ID_CRITICAL_ERROR() 							{ return DIALOG_ID_CRITICAL_ERROR;}
	static get DIALOG_ID_RECONNECT() 								{ return DIALOG_ID_RECONNECT;}
	static get DIALOG_ID_REDIRECTION() 								{ return DIALOG_ID_REDIRECTION;}
	static get DIALOG_ID_WEBGL_CONTEXT_LOST() 						{ return DIALOG_ID_WEBGL_CONTEXT_LOST;}
	static get DIALOG_ID_RUNTIME_ERROR() 							{ return DIALOG_ID_RUNTIME_ERROR;}
	static get DIALOG_ID_GAME_NEM() 								{ return DIALOG_ID_GAME_NEM;}
	static get DIALOG_ID_TOO_LATE_EJECT() 							{ return DIALOG_ID_TOO_LATE_EJECT;}
	static get DIALOG_ID_ROOM_NOT_FOUND() 							{ return DIALOG_ID_ROOM_NOT_FOUND;}
	static get DIALOG_ID_BET_FAILED() 								{ return DIALOG_ID_BET_FAILED;}
	static get DIALOG_ID_LEAVE_ROOM() 								{ return DIALOG_ID_LEAVE_ROOM;}
	static get DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS() 			{ return DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS;}
	static get DIALOG_ID_BATTLEGROUND_REJOIN() 						{ return DIALOG_ID_BATTLEGROUND_REJOIN;}
	static get DIALOG_ID_WEB_SOCKET_RECONNECT() 					{ return DIALOG_ID_WEB_SOCKET_RECONNECT;}
	static get DIALOG_ID_ROUND_ALREADY_FINISHED()					{ return DIALOG_ID_ROUND_ALREADY_FINISHED; }
	static get DIALOG_ID_WAIT_PENDING_OPERATION()					{ return DIALOG_ID_WAIT_PENDING_OPERATION; }
	static get DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER()			{ return DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER;}
	static get DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST()				{ return DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST}
	static get DIALOG_ID_CAF_PLAYER_KICKED()						{ return DIALOG_ID_CAF_PLAYER_KICKED}
	static get DIALOG_ID_ROUND_ALREADY_STARTED()					{ return DIALOG_ID_ROUND_ALREADY_STARTED;}
	static get DIALOG_ID_CAF_ROUND_SUMMARY()						{ return DIALOG_ID_CAF_ROUND_SUMMARY;}
	static get DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED()					{ return DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED;}
	
	static get CRITICAL_DIALOGS_IDS() 								{ return [
																			DialogsInfo.DIALOG_ID_NETWORK_ERROR,
																			DialogsInfo.DIALOG_ID_CRITICAL_ERROR,
																			DialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST,
																			DialogsInfo.DIALOG_ID_RUNTIME_ERROR
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
		let di = null; 
		

		di = this._dialogsInfos[dialogId] || this._initDialogInfo(dialogId);

		return di;
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
			case DIALOG_ID_CRITICAL_ERROR:
				dialogInfo = new CriticalErrorDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_RECONNECT:
				dialogInfo = new ReconnectDialogInfo(dialogId, 0.9);
				break;
			case DIALOG_ID_REDIRECTION:
				dialogInfo = new RedirectionDialogInfo(dialogId, 4);
				break;
			case DIALOG_ID_WEBGL_CONTEXT_LOST:
				dialogInfo = new DialogInfo(dialogId, 5); // the highest priority
				break;
			case DIALOG_ID_RUNTIME_ERROR:
				dialogInfo = new RuntimeErrorDialogInfo(dialogId, 6); //the highest priority
				break;
			case DIALOG_ID_GAME_NEM:
				dialogInfo = new GameNEMDialogInfo(dialogId, 0.01);
				break;
			case DIALOG_ID_TOO_LATE_EJECT:
				dialogInfo = new TooLateEjectDialogInfo(dialogId, 0.02);
				break;
			case DIALOG_ID_ROOM_NOT_FOUND:
				dialogInfo = new RoomNotFoundDialogInfo(dialogId, 0.1);
				break;
			case DIALOG_ID_BET_FAILED:
				dialogInfo = new BetFailedDialogInfo(dialogId, 0.02);
				break;
			case DIALOG_ID_LEAVE_ROOM:
				dialogInfo = new LeaveRoomDialogInfo(dialogId, 0.95);
				break;
			case DIALOG_ID_WEB_SOCKET_RECONNECT:
				dialogInfo = new WebSocketReconnectionAttemptDialogInfo(dialogId, 1);
				break;
			case DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
				dialogInfo = new BattlegroundNotEnoughPlayersDialogInfo(dialogId, 0.88);
				break;
			case DIALOG_ID_BATTLEGROUND_REJOIN:
				dialogInfo = new BattlegroundRejoinDialogInfo(dialogId, 0.88);
				break;
			case DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogInfo = new RoundAlreadyFinishedDialogInfo(dialogId, 0);
				break;
			case DIALOG_ID_WAIT_PENDING_OPERATION:
				dialogInfo = new WaitPendingOperationDialogInfo(dialogId, 0.89); 
				break;
			case DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER:
				console.log("dialog info created")
				dialogInfo = new BattlegroundCafRoomManagerDialogInfo(dialogId, 0.91);
				break;
			case DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST:
				console.log("dialog info created")
				dialogInfo = new BattlegroundCafRoomGuestDialogInfo(dialogId, 0.92);
				break;
			case DIALOG_ID_CAF_PLAYER_KICKED:
				dialogInfo = new DialogInfo(dialogId, 0.93); 
				break;
			case DIALOG_ID_ROUND_ALREADY_STARTED:
				dialogInfo = new DialogInfo(dialogId, 0.94); 
				break;
			case DIALOG_ID_CAF_ROUND_SUMMARY:
				dialogInfo = new DialogInfo(dialogId, 0.96); 
				break;
			case DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED:
				dialogInfo = new DialogInfo(dialogId, 0.97); 
				break;
			default:
				throw new Error (`Unsupported dialog id: ${dialogId}`);
		}
		return dialogInfo;
	}
}

export default DialogsInfo