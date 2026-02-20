import SimpleUIView from '../../../../unified/view/base/SimpleUIView';
import GUDialogsInfo from '../../../model/uis/custom/dialogs/GUDialogsInfo';
import GUDialogView from './GUDialogView';
import GUCriticalErrorDialogView from './custom/GUCriticalErrorDialogView';
import GUNEMDialogView from './custom/game/GUNEMDialogView';
import GURedirectionDialogView from './custom/GURedirectionDialogView';
import GURuntimeErrorDialogView from './custom/GURuntimeErrorDialogView';

class GUDialogsView extends SimpleUIView
{
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

	get networkErrorDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_NETWORK_ERROR);
	}

	get criticalErrorDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR);
	}

	get reconnectDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_RECONNECT);
	}

	get serverRebootDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_SERVER_REBOOT);
	}

	get roomNotFoundDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_ROOM_NOT_FOUND);
	}

	get redirectionDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_REDIRECTION);
	}

	get gameNEMDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_GAME_NEM);
	}

	get webglContextLostDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST);
	}

	get runtimeErrorDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR);
	}

	get roundAlreadyFinishedDialogView()
	{
		return this.__getDialogView(GUDialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED);
	}
	
	destroy()
	{
		this.dialogsViews = null;

		super.destroy();
	}

	getDialogView(dialogId)
	{
		return this.__getDialogView(dialogId);
	}

	_initDialogsView()
	{
		this.dialogsViews = [];
	}

	__getDialogView(dialogId)
	{
		return this.dialogsViews[dialogId] || this._initDialogView(dialogId);
	}

	_initDialogView(dialogId)
	{
		var dialogView = this.__generateDialogView(dialogId);

		this.dialogsViews[dialogId] = dialogView;
		this.addChild(dialogView);

		return dialogView;
	}

	__generateDialogView(dialogId)
	{
		var dialogView;
		switch (dialogId)
		{
			case GUDialogsInfo.DIALOG_ID_NETWORK_ERROR:
			case GUDialogsInfo.DIALOG_ID_RECONNECT:
			case GUDialogsInfo.DIALOG_ID_ROOM_NOT_FOUND:
			case GUDialogsInfo.DIALOG_ID_WEBGL_CONTEXT_LOST:
			case GUDialogsInfo.DIALOG_ID_SERVER_REBOOT:
			case GUDialogsInfo.DIALOG_ID_ROUND_ALREADY_FINISHED:
				dialogView = this.__generateStandardDialogViewInstance();
				break;
			case GUDialogsInfo.DIALOG_ID_CRITICAL_ERROR:
				dialogView = this.__generateCriticalErrorDialogViewInstance();
				break;
			case GUDialogsInfo.DIALOG_ID_GAME_NEM:
				dialogView = this.__generateNEMDialogViewInstance();
				break;
			case GUDialogsInfo.DIALOG_ID_REDIRECTION:
				dialogView = this.__generateRedirectionDialogViewInstance();
				break;
			case GUDialogsInfo.DIALOG_ID_RUNTIME_ERROR:
				dialogView = this.__generateRuntimeErrorDialogViewInstance();
				break;
			default:
				new Error(`Unsupported dialog id: ${dialogId}`);
		}

		return dialogView;
	}

	__generateStandardDialogViewInstance()
	{
		return new GUDialogView();
	}

	__generateCriticalErrorDialogViewInstance()
	{
		return new GUCriticalErrorDialogView();
	}

	__generateNEMDialogViewInstance()
	{
		return new GUNEMDialogView()
	}

	__generateRedirectionDialogViewInstance()
	{
		return new GURedirectionDialogView();
	}

	__generateRuntimeErrorDialogViewInstance()
	{
		return new GURuntimeErrorDialogView();
	}

	//SOUND_BUTTON...
	get _soundButtonView()
	{
		return this._fSoundButtonView_sbv || (this._fSoundButtonView_sbv = this._initSoundButtonView());
	}

	_initSoundButtonView()
	{
		let l_sbv = this.__provideSoundButtonViewInstance();

		return l_sbv;
	}

	__provideSoundButtonViewInstance()
	{
		return new SimpleUIView();
	}
	//...SOUND_BUTTON
}

export default GUDialogsView;