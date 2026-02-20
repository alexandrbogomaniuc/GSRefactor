import SimpleUIInfo from '../../../../../unified/model/uis/SimpleUIInfo';
import GUDialogInfo from './GUDialogInfo';
import GURuntimeErrorDialogInfo from './custom/GURuntimeErrorDialogInfo';
import GURoomNotFoundDialogInfo from './custom/GURoomNotFoundDialogInfo';
import GUGameNEMDialogInfo from './custom/game/GUGameNEMDialogInfo';
import GURedirectionDialogInfo from './custom/GURedirectionDialogInfo';
import GUReconnectDialogInfo from './custom/GUReconnectDialogInfo';
import GUCriticalErrorDialogInfo from './custom/GUCriticalErrorDialogInfo';
import GUNetworkErrorDialogInfo from './custom/GUNetworkErrorDialogInfo';
import GUServerRebootDialogInfo from './custom/GUServerRebootDialogInfo';
import GURoundAlreadyFinishedDialogInfo from './custom/GURoundAlreadyFinishedDialogInfo';

var nextDialogId_int = 0;

export function __generateNextDialogId()
{
	let lNextId_int = nextDialogId_int++;

	return lNextId_int;
};

const DIALOG_ID_NETWORK_ERROR = __generateNextDialogId();
const DIALOG_ID_CRITICAL_ERROR = __generateNextDialogId();
const DIALOG_ID_RECONNECT = __generateNextDialogId();
const DIALOG_ID_ROOM_NOT_FOUND = __generateNextDialogId();
const DIALOG_ID_GAME_NEM = __generateNextDialogId();
const DIALOG_ID_REDIRECTION = __generateNextDialogId();
const DIALOG_ID_WEBGL_CONTEXT_LOST = __generateNextDialogId();
const DIALOG_ID_RUNTIME_ERROR = __generateNextDialogId();
const DIALOG_ID_SERVER_REBOOT = __generateNextDialogId();
const DIALOG_ID_ROUND_ALREADY_FINISHED = __generateNextDialogId();

class GUDialogsInfo extends SimpleUIInfo
{
	static get DIALOG_ID_NETWORK_ERROR() 			{ return DIALOG_ID_NETWORK_ERROR; }
	static get DIALOG_ID_CRITICAL_ERROR() 			{ return DIALOG_ID_CRITICAL_ERROR; }
	static get DIALOG_ID_RECONNECT() 				{ return DIALOG_ID_RECONNECT; }
	static get DIALOG_ID_ROOM_NOT_FOUND() 			{ return DIALOG_ID_ROOM_NOT_FOUND; }
	static get DIALOG_ID_GAME_NEM() 				{ return DIALOG_ID_GAME_NEM; }
	static get DIALOG_ID_REDIRECTION() 				{ return DIALOG_ID_REDIRECTION; }
	static get DIALOG_ID_WEBGL_CONTEXT_LOST() 		{ return DIALOG_ID_WEBGL_CONTEXT_LOST; }
	static get DIALOG_ID_RUNTIME_ERROR() 			{ return DIALOG_ID_RUNTIME_ERROR; }
	static get DIALOG_ID_SERVER_REBOOT() 			{ return DIALOG_ID_SERVER_REBOOT; }
	static get DIALOG_ID_ROUND_ALREADY_FINISHED() 	{ return DIALOG_ID_ROUND_ALREADY_FINISHED; }

	static get CRITICAL_DIALOGS_IDS()
	{
		return [
			GUDialogsInfo.DIALOG_ID_NETWORK_ERROR,
			GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR,
			GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST,
			GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR
		];
	}

	constructor()
	{
		super();

		this._fRegisteredDialogsIds_int_arr = null;
		this._dialogsInfos = null;
		this._dialogIdForPresentation = undefined;

		this._initDialogsInfo();
	}

	destroy()
	{
		this._dialogsInfos = null;

		super.destroy();
	}

	getDialogInfo(dialogId)
	{
		return this._getDialogInfo(dialogId);
	}

	get dialogsCount()
	{
		return this._fRegisteredDialogsIds_int_arr.length;
	}

	get dialogsIds()
	{
		return this._fRegisteredDialogsIds_int_arr;
	}

	get dialogIdForPresentation()
	{
		return this._dialogIdForPresentation;
	}

	set dialogIdForPresentation(dialogId)
	{
		this._dialogIdForPresentation = dialogId;
	}

	get hasActiveDialog()
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

	get hasActiveCriticalDialog()
	{
		this.dialogsIds.forEach(i =>
		{
			if (this._getDialogInfo(i).isActive)
			{
				const dialogId = this._getDialogInfo(i).dialogId;
				if (~GUDialogsInfo.CRITICAL_DIALOGS_IDS.indexOf(dialogId))
				{
					return true;
				}
				return false;
			}
		})
		return false;
	}

	_initDialogsInfo()
	{
		this._dialogsInfos = [];

		this._fRegisteredDialogsIds_int_arr = [];
		this.__initSupportedDialogsIds();
	}

	__initSupportedDialogsIds()
	{
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_NETWORK_ERROR);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_RECONNECT);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_GAME_NEM);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_REDIRECTION);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR);
		this.__registerDialogId(GUDialogsInfo.DIALOG_ID_SERVER_REBOOT);
	}

	__registerDialogId (aDialogId_int)
	{
		if (this._fRegisteredDialogsIds_int_arr.indexOf(aDialogId_int) >= 0)
		{
			throw new Error(`Dialog with id ${aDialogId_int} already exists!`);
		}
		
		this._fRegisteredDialogsIds_int_arr.push(aDialogId_int);
	}

	_getDialogInfo(dialogId)
	{
		return this._dialogsInfos[dialogId] || this._initDialogInfo(dialogId);
	}

	_initDialogInfo(dialogId)
	{
		var dialogInfo = this.__generateDialogInfo(dialogId);

		this._dialogsInfos[dialogId] = dialogInfo;

		return dialogInfo;
	}

	__generateDialogInfo(dialogId)
	{
		var dialogInfo = null;
		switch (dialogId)
		{
			case GUDialogsInfo.DIALOG_ID_NETWORK_ERROR:
				dialogInfo = this.__networkErrorDialogInfo(dialogId, 1);
				break;
			case GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR:
				dialogInfo = this.__criticalErrorDialogInfo(dialogId, 1);
				break;
			case GUDialogsInfo.DIALOG_ID_RECONNECT:
				dialogInfo = this.__reconnectDialogInfo(dialogId, 0.9);
				break;
			case GUDialogsInfo.DIALOG_ID_SERVER_REBOOT:
				dialogInfo = this.__serverRebootDialogInfo(dialogId, 1.1);
				break;
			case GUDialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
				dialogInfo = this.__roomNotFoundDialogInfo(dialogId, 0.1);
				break;
			case GUDialogsInfo.DIALOG_ID_GAME_NEM:
				dialogInfo = this.__gameNEMDialogInfo(dialogId, 0);
				break;
			case GUDialogsInfo.DIALOG_ID_REDIRECTION:
				dialogInfo = this.__redirectionDialogInfo(dialogId, 4);
				break;
			case GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
				dialogInfo = this.__webGLContextLostDialogInfo(dialogId, 5);
				break;
			case GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogInfo = this.__runtimeErrorDialogInfo(dialogId, 6);
				break;
			case GUDialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogInfo = this.__roundAlreadyFinishedDialogInfo(dialogId, 0);
				break;
			default:
				new Error(`Unsupported dialog id: ${dialogId}`);
		}

		return dialogInfo;
	}

	__networkErrorDialogInfo(dialogId, priority)
	{
		return new GUNetworkErrorDialogInfo(dialogId, priority);
	}

	__criticalErrorDialogInfo(dialogId, priority)
	{
		return new GUCriticalErrorDialogInfo(dialogId, priority);
	}

	__reconnectDialogInfo(dialogId, priority)
	{
		return new GUReconnectDialogInfo(dialogId, priority);
	}

	__serverRebootDialogInfo(dialogId, priority)
	{
		return new GUServerRebootDialogInfo(dialogId, priority);
	}
	
	__roomNotFoundDialogInfo(dialogId, priority)
	{
		return new GURoomNotFoundDialogInfo(dialogId, priority);
	}

	__gameNEMDialogInfo(dialogId, priority)
	{
		return new GUGameNEMDialogInfo(dialogId, priority)
	}

	__redirectionDialogInfo(dialogId, priority)
	{
		return new GURedirectionDialogInfo(dialogId, priority);
	}

	__webGLContextLostDialogInfo(dialogId, priority)
	{
		return new GUDialogInfo(dialogId, priority);
	}

	__runtimeErrorDialogInfo(dialogId, priority)
	{
		return new GURuntimeErrorDialogInfo(dialogId, priority);
	}

	__roundAlreadyFinishedDialogInfo(dialogId, priority)
	{
		return new GURoundAlreadyFinishedDialogInfo(dialogId, priority);
	}
}

export default GUDialogsInfo