import MultiStateButtonController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/MultiStateButtonController';
import LobbyPreloaderSoundButtonView from '../../../../view/uis/custom/preloader/LobbyPreloaderSoundButtonView';
import SoundSettingsController from '../../../sounds/SoundSettingsController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BrowserSupportController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/preloading/BrowserSupportController';
import DialogsController from '../dialogs/DialogsController';

class LobbyPreloaderSoundButtonController extends MultiStateButtonController
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return LobbyPreloaderSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED;}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED() 	{return LobbyPreloaderSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED;}

	//INIT...
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

		this._fSoundSettingsController_ssc.on(SoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED, this._updateSoundButtonState, this);

		let dialogsController = APP.dialogsController;
		dialogsController.on(DialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogsController.on(DialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_sbv = this.view;
		lView_sbv.on(LobbyPreloaderSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		lView_sbv.on(LobbyPreloaderSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		this._updateSoundButtonState();
	}
	//...INIT

	_onDialogActivated(event)
	{
		this.view && this.view.hide();
	}

	_onDialogDeactivated(event)
	{
		this.view && this.view.show();
	}

	_updateSoundButtonState()
	{
		let lView_sbv = this.view;
		if (lView_sbv)
		{
			lView_sbv.buttonState = this._fSoundSettingsInfo_ssi.isSoundsVolumeOn 
									? LobbyPreloaderSoundButtonView.STATE_SOUND_ON 
									: LobbyPreloaderSoundButtonView.STATE_SOUND_OFF;
		}
	}

	_onSoundOnButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = LobbyPreloaderSoundButtonView.STATE_SOUND_OFF;

		this.emit(event);
	}

	_onSoundOffButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = LobbyPreloaderSoundButtonView.STATE_SOUND_ON;

		this.emit(event);
	}

	destroy()
	{
		this._fSoundSettingsController_ssc && this._fSoundSettingsController_ssc.off(SoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED, this._updateSoundButtonState, this);
		this._fSoundSettingsController_ssc = null;

		this._fSoundSettingsInfo_ssi = null;

		APP.browserSupportController && APP.browserSupportController.off(BrowserSupportController.EVENT_AUDIO_CONTEXT_STATE_CHANGED, this._updateSoundButtonState, this);

		let dialogsController = APP.dialogsController;
		if (!!dialogsController)
		{
			dialogsController.off(DialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
			dialogsController.off(DialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
		}		

		super.destroy();
	}
}
export default LobbyPreloaderSoundButtonController