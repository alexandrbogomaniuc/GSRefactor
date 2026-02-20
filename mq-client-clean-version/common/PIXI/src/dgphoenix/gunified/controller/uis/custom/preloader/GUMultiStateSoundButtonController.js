import MultiStateButtonController from '../../../../../unified/controller/uis/base/MultiStateButtonController';
import GUMultiStateSoundButtonView from '../../../../view/uis/preloader/GUMultiStateSoundButtonView';

class GUMultiStateSoundButtonController extends MultiStateButtonController
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED()	{ return GUMultiStateSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED; }
	static get EVENT_SOUND_OFF_BUTTON_CLICKED()	{ return GUMultiStateSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED; }

	//INIT...
	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_sbv = this.view;
		lView_sbv.on(GUMultiStateSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		lView_sbv.on(GUMultiStateSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		this._updateSoundButtonState();
	}
	//...INIT

	_updateSoundButtonState()
	{
		// must be overridden
	}

	_onSoundOnButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = GUMultiStateSoundButtonView.STATE_SOUND_OFF;

		this.emit(event);
	}

	_onSoundOffButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = GUMultiStateSoundButtonView.STATE_SOUND_ON;

		this.emit(event);
	}

	destroy()
	{
		super.destroy();
	}
}
export default GUMultiStateSoundButtonController