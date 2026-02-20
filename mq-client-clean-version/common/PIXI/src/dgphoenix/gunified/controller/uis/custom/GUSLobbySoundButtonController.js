import MultiStateButtonController from '../../../../unified/controller/uis/base/MultiStateButtonController';
import GUSLobbySoundButtonView from '../../../view/uis/GUSLobbySoundButtonView';
import GUSLobbySoundSettingsController from '../../sounds/GUSLobbySoundSettingsController';
import { APP } from '../../../../unified/controller/main/globals';

class GUSLobbySoundButtonController extends MultiStateButtonController
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return GUSLobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED;}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED() 	{return GUSLobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED;}

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

		this._fSoundSettingsController_ssc.on(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED, this._updateSoundButtonState, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_sbv = this.view;
		lView_sbv.on(GUSLobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		lView_sbv.on(GUSLobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		this._updateSoundButtonState();
	}
	//...INIT

	_updateSoundButtonState()
	{
		let lView_sbv = this.view;
		if (lView_sbv)
		{
			lView_sbv.buttonState = this._fSoundSettingsInfo_ssi.isSoundsVolumeOn
				? GUSLobbySoundButtonView.STATE_SOUND_ON
				: GUSLobbySoundButtonView.STATE_SOUND_OFF;
		}
	}

	_onSoundOnButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = GUSLobbySoundButtonView.STATE_SOUND_OFF;

		this.emit(event);
	}

	_onSoundOffButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = GUSLobbySoundButtonView.STATE_SOUND_ON;

		this.emit(event);
	}
}
export default GUSLobbySoundButtonController