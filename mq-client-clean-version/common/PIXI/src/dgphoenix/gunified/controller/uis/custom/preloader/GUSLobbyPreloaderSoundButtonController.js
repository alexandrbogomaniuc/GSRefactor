import GUPreloaderSoundButtonController from './GUPreloaderSoundButtonController';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbySoundSettingsController from '../../../sounds/GUSLobbySoundSettingsController';
import GUSLobbyDialogsController from '../dialogs/GUSLobbyDialogsController';
import GUPreloaderSoundButtonView from '../../../../view/uis/preloader/GUPreloaderSoundButtonView';

class GUSLobbyPreloaderSoundButtonController extends GUPreloaderSoundButtonController
{
	__init()
	{
		this._fSoundSettingsController_ssc = null;
		this._fSoundSettingsInfo_ssi = null;

		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._fSoundSettingsController_ssc = APP.soundSettingsController;
		this._fSoundSettingsInfo_ssi = this._fSoundSettingsController_ssc.info;

		this._fSoundSettingsController_ssc.on(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED, this._updateSoundButtonState, this);

		let dialogsController = APP.dialogsController;
		dialogsController.on(GUSLobbyDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogsController.on(GUSLobbyDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}
	
	_onDialogActivated()
	{
		this.view && this.view.hide();
	}

	_onDialogDeactivated()
	{
		this.view && this.view.show();
	}

	get __isSoundOn()
	{
		return this._fSoundSettingsInfo_ssi.isSoundsVolumeOn;
	}

	destroy()
	{
		this._fSoundSettingsController_ssc && this._fSoundSettingsController_ssc.off(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED, this._updateSoundButtonState, this);
		this._fSoundSettingsController_ssc = null;

		this._fSoundSettingsInfo_ssi = null;

		let dialogsController = APP.dialogsController;
		if (dialogsController)
		{
			dialogsController.off(GUSLobbyDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
			dialogsController.off(GUSLobbyDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
		}

		super.destroy();
	}
}
export default GUSLobbyPreloaderSoundButtonController