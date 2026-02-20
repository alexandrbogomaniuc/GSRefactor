import SimpleUIController from '../../../../../unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../unified/controller/main/globals';
import GUDialogController from './GUDialogController';
import GUDialogsInfo from '../../../../model/uis/custom/dialogs/GUDialogsInfo';
import GUDialogsView from '../../../../view/uis/dialogs/GUDialogsView';
import GUWebGLContextLostDialogController from './custom/GUWebGLContextLostDialogController';
import GURuntimeErrorDialogController from './custom/GURuntimeErrorDialogController';
import GURoomNotFoundDialogController from './custom/GURoomNotFoundDialogController';
import GUGameNEMDialogController from './custom/game/GUGameNEMDialogController';
import GURedirectionDialogController from './custom/GURedirectionDialogController';
import GUReconnectDialogController from './custom/GUReconnectDialogController';
import GUCriticalErrorDialogController from './custom/GUCriticalErrorDialogController';
import GUNetworkErrorDialogController from './custom/GUNetworkErrorDialogController';
import GUServerRebootDialogController from './custom/GUServerRebootDialogController';
import GURoundAlreadyFinishedDialogController from './custom/GURoundAlreadyFinishedDialogController';

class GUDialogsController extends SimpleUIController
{
	static get EVENT_DIALOG_ACTIVATED()		{ return GUDialogController.EVENT_DIALOG_ACTIVATED }
	static get EVENT_DIALOG_DEACTIVATED()	{ return GUDialogController.EVENT_DIALOG_DEACTIVATED }

	get soundButtonController()
	{
		return this._soundButtonController;
	}

	static _sortDialogsByPresentationPriority(dialog1, dialog2)
	{
		let firstDialogInfo = dialog1.info;
		let secondDialogInfo = dialog2.info;

		let firstDialogPriority = firstDialogInfo.priority;
		let secondDialogPriority = secondDialogInfo.priority;
		let lRet_num = secondDialogPriority - firstDialogPriority;
		if (!lRet_num)
		{
			let firstDialogActivationTime = firstDialogInfo.activationTime;
			let secondDialogActivationTime = secondDialogInfo.activationTime;

			lRet_num = firstDialogActivationTime - secondDialogActivationTime;
		}
		return lRet_num;
	}

	constructor(aOptDialogsInfo)
	{
		super(aOptDialogsInfo || new GUDialogsInfo());

		this._dialogsControllers = null;
		this._fViewContainer_sprt = null;

		this._initDialogsController();

		this._soundButtonController.init();
	}

	get networkErrorDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_NETWORK_ERROR);
	}

	get criticalErrorDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR);
	}

	get reconnectDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_RECONNECT);
	}

	get serverRebootDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_SERVER_REBOOT);
	}

	get roomNotFoundDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
	}

	get redirectionDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_REDIRECTION);
	}

	get gameNEMDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_GAME_NEM);
	}

	get webglContextLostDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
	}

	get runtimeErrorDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR);
	}

	get roundAlreadyFinishedDialogController()
	{
		return this.__getDialogController(GUDialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED);
	}

	initView(viewContainer)
	{
		this._fViewContainer_sprt = viewContainer;

		let view = this.__provideDialogsViewInstance();
		this._fViewContainer_sprt.addChild(view);

		let lSoundButtonView_sbv = this._fViewContainer_sprt.addChild(view.soundButtonView);
		this._updateSoundButtonPosition(lSoundButtonView_sbv);
		
		this._soundButtonController.initView(lSoundButtonView_sbv);


		super.initView(view);
	}

	__provideDialogsViewInstance()
	{
		return new GUDialogsView();
	}

	_updateSoundButtonPosition()
	{
	}

	get _soundButtonController()
	{
		return this._fSoundButtonController_sbc || (this._fSoundButtonController_sbc = this._provideSoundButtonControllerInstance());
	}

	_provideSoundButtonControllerInstance()
	{
		return new SimpleUIController();
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

	_initDialogsController()
	{
		this._dialogsControllers = [];
	}

	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let dialogIds = this.info.dialogsIds;
		dialogIds.forEach(i =>
		{
			let notificaionController = this.__getDialogController(i);
			notificaionController.init();
		})
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let view = this.__fView_uo;
		let dialogIds = this.info.dialogsIds;
		dialogIds.forEach(i =>
			{
				let dialogController = this.__getDialogController(i);
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
			})
	}

	__getDialogController(dialogId)
	{
		return this._dialogsControllers[dialogId] || this._initDialogController(dialogId);
	}

	_initDialogController(dialogId)
	{
		let dialogController = this.__generateDialogController(this.info.getDialogInfo(dialogId));
		this._dialogsControllers[dialogId] = dialogController;

		dialogController.on(GUDialogController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogController.on(GUDialogController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);

		return dialogController;
	}

	__generateDialogController(dialogInfo)
	{
		//must be overridden
		let dialogController;
		let dialogId = dialogInfo.dialogId;

		switch (dialogId)
		{
			case GUDialogsInfo.DIALOG_ID_NETWORK_ERROR:
				dialogController = this.__networkErrorDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR:
				dialogController = this.__criticalErrorDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_RECONNECT:
				dialogController = this.__reconnectDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_SERVER_REBOOT:
				dialogController = this.__serverRebootDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
				dialogController = this.__roomNotFoundDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_GAME_NEM:
				dialogController = this.__gameNEMDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_REDIRECTION:
				dialogController = this.__redirectionDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
				dialogController = this.__webGLContextLostDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogController = this.__runtimeErrorDialogController(dialogInfo);
				break;
			case GUDialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogController = this.__roundAlreadyFinishedDialogController(dialogInfo);
				break;
			default:
				new Error(`Unsupported dialog id: ${dialogId}`);
		}
		return dialogController;
	}

	_onDialogActivated(aEvent_ue)
	{
		this._updateDialogForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_onDialogDeactivated(aEvent_ue)
	{
		this._updateDialogForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_updateDialogForPresentationSettings()
	{
		let sortedActiveDialogs = this._getActiveDialogsWithPresentationPrioritySorting();
		let info = this.info;

		if (!sortedActiveDialogs)
		{
			info.dialogIdForPresentation = undefined;
		}
		else
		{
			info.dialogIdForPresentation = sortedActiveDialogs[0].info.dialogId;
		}
	}

	_getActiveDialogsWithPresentationPrioritySorting()
	{
		let activeDialogs = null;
		let dialogIds = this.info.dialogsIds;
		dialogIds.forEach(i =>
		{
			let dialogController = this.__getDialogController(i);
			if (dialogController.info.isActive)
			{
				activeDialogs = activeDialogs || [];
				activeDialogs.push(dialogController);
			}
		})

		if (activeDialogs)
		{
			activeDialogs.sort(GUDialogsController._sortDialogsByPresentationPriority);
		}

		return activeDialogs;
	}

	__networkErrorDialogController(dialogInfo)
	{
		return new GUNetworkErrorDialogController(dialogInfo, this);
	}

	__criticalErrorDialogController(dialogInfo)
	{
		return new GUCriticalErrorDialogController(dialogInfo, this);
	}

	__reconnectDialogController(dialogInfo)
	{
		return new GUReconnectDialogController(dialogInfo, this);
	}

	__serverRebootDialogController(dialogInfo)
	{
		return new GUServerRebootDialogController(dialogInfo, this);
	}

	__roomNotFoundDialogController(dialogInfo)
	{
		return new GURoomNotFoundDialogController(dialogInfo, this);
	}

	__gameNEMDialogController(dialogInfo)
	{
		return new GUGameNEMDialogController(dialogInfo, this);
	}

	__webGLContextLostDialogController(dialogInfo)
	{
		return new GUWebGLContextLostDialogController(dialogInfo, this);
	}

	__runtimeErrorDialogController(dialogInfo)
	{
		return new GURuntimeErrorDialogController(dialogInfo, this);
	}

	__redirectionDialogController(dialogInfo)
	{
		return new GURedirectionDialogController(dialogInfo, this);
	}

	__roundAlreadyFinishedDialogController(dialogInfo)
	{
		return new GURoundAlreadyFinishedDialogController(dialogInfo, this);
	}
}

export default GUDialogsController