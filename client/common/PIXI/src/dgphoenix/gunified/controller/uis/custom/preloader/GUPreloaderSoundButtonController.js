import GUMultiStateSoundButtonController from './GUMultiStateSoundButtonController';
import GUPreloaderSoundButtonView from '../../../../view/uis/preloader/GUPreloaderSoundButtonView';

class GUPreloaderSoundButtonController extends GUMultiStateSoundButtonController
{
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
	}
	
	_updateSoundButtonState()
	{
		let lView_sbv = this.view;
		if (lView_sbv)
		{
			lView_sbv.buttonState = this.__isSoundOn
				? GUPreloaderSoundButtonView.STATE_SOUND_ON
				: GUPreloaderSoundButtonView.STATE_SOUND_OFF;
		}
	}

	get __isSoundOn()
	{
		return undefined;
	}

	destroy()
	{
		super.destroy();
	}
}
export default GUPreloaderSoundButtonController