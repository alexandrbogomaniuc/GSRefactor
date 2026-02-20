import SimpleUIController from '../../../../../../unified/controller/uis/base/SimpleUIController';
import GUSettingsScreenView from '../../../../../view/uis/secondary/settings/GUSettingsScreenView';
import { APP } from '../../../../../../unified/controller/main/globals';

class GUSettingsScreenController extends SimpleUIController
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()					{ return GUSettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED; }
	static get EVENT_ON_SOUND_VOLUME_CHANGED()				{ return GUSettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED; }
	static get EVENT_ON_MUSIC_VOLUME_CHANGED()				{ return GUSettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED; }
	static get EVENT_ON_OK_BUTTON_CLICKED()					{ return GUSettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED; }
	static get EVENT_SCREEN_ACTIVATED()						{ return "onSettingsScreenActivated"; }
	static get EVENT_SCREEN_DEACTIVATED()					{ return "onSettingsScreenDeactivated"; }

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideSceeen();
	}

	cancelChanges()
	{
		this._restoreVolumeSettings();
	}

	getVolumeSettings()
	{
		return this._fVolumeSettings_obj;
	}

	constructor()
	{
		super ();
	}

	__init()
	{
		this._fVolumeSettings_obj = {};

		super.__init();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;
		lView_ssv.on(GUSettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED, this._onClose, this);
		lView_ssv.on(GUSettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED, this._onOkClicked, this);
		lView_ssv.on(GUSettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, this.emit, this);
		lView_ssv.on(GUSettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, this.emit, this);
	}

	_showScreen()
	{
		let lView_ssv = this.view;
		lView_ssv.visible = true;

		this._updateVolumeSettings();

		this.emit(GUSettingsScreenController.EVENT_SCREEN_ACTIVATED);
	}

	_updateUnMuteSettings()
    {
        let lView_ssv = this.view;
        if (lView_ssv && lView_ssv.visible)
        {
            let lSoundSettingsInfo_ssi = APP.soundSettingsController.info;

            let muteVolumeSettings = {musicVolume:lSoundSettingsInfo_ssi.i_getBgSoundsVolume(), fxVolume:lSoundSettingsInfo_ssi.i_getFxSoundsVolume()}
            lView_ssv.setSettings({ soundVolumes: muteVolumeSettings });
        }
    }

    _updateMuteSettings()
    {
        let lView_ssv = this.view;
        if (lView_ssv && lView_ssv.visible)
        {
            let muteVolumeSettings = {musicVolume:0, fxVolume:0}
            lView_ssv.setSettings({ soundVolumes: muteVolumeSettings });
        }
    }

	_hideSceeen()
	{
		let lView_ssv = this.view;
		if (lView_ssv)
		{
			lView_ssv.visible = false;
			this.emit(GUSettingsScreenController.EVENT_SCREEN_DEACTIVATED);
		}
	}

	_onClose()
	{
		this.emit(GUSettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_onOkClicked()
	{
		this.emit(GUSettingsScreenController.EVENT_ON_OK_BUTTON_CLICKED);
	}

	_saveVolumeSettings(musicVolume, fxVolume)
	{
		let lSoundSettingsInfo_ssi = APP.soundSettingsController.info;
		this._fVolumeSettings_obj.musicVolume = !!musicVolume||musicVolume===0?musicVolume:lSoundSettingsInfo_ssi.i_getBgSoundsVolume();
		this._fVolumeSettings_obj.fxVolume = !!fxVolume||fxVolume===0?fxVolume:lSoundSettingsInfo_ssi.i_getFxSoundsVolume();
	}

	_updateVolumeSettings()
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			this._saveVolumeSettings();
			lView_ssv.setSettings({ soundVolumes: this._fVolumeSettings_obj });
		}
	}

	_restoreVolumeSettings()
	{
		this.emit(GUSettingsScreenController.EVENT_ON_SOUND_VOLUME_CHANGED, { value: this._fVolumeSettings_obj.fxVolume });
		this.emit(GUSettingsScreenController.EVENT_ON_MUSIC_VOLUME_CHANGED, { value: this._fVolumeSettings_obj.musicVolume });
	}

	destroy()
	{
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			lView_ssv.off(GUSettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED, this.emit, this);
			lView_ssv.off(GUSettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED, this.emit, this);
			lView_ssv.off(GUSettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, this.emit, this);
			lView_ssv.off(GUSettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, this.emit, this);
		}

		this._fVolumeSettings_obj = null;

		super.destroy();
	}
}

export default GUSettingsScreenController