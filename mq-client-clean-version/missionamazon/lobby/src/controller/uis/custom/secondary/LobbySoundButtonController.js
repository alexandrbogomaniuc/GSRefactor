import MultiStateButtonController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/MultiStateButtonController';
import LobbySoundButtonView from '../../../../view/uis/custom/secondary/LobbySoundButtonView';
import SoundSettingsController from '../../../sounds/SoundSettingsController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class LobbySoundButtonController extends MultiStateButtonController
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return LobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED;}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED() 	{return LobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED;}

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
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_sbv = this.view;
		lView_sbv.on(LobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		lView_sbv.on(LobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		this._updateSoundButtonState();
	}
	//...INIT

	_updateSoundButtonState()
	{
		let lView_sbv = this.view;
		if (lView_sbv)
		{
			lView_sbv.buttonState = this._fSoundSettingsInfo_ssi.isSoundsVolumeOn 
									? LobbySoundButtonView.STATE_SOUND_ON 
									: LobbySoundButtonView.STATE_SOUND_OFF;
		}
	}

	_onSoundOnButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = LobbySoundButtonView.STATE_SOUND_OFF;

		this.emit(event);
	}

	_onSoundOffButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = LobbySoundButtonView.STATE_SOUND_ON;

		this.emit(event);
	}
}
export default LobbySoundButtonController