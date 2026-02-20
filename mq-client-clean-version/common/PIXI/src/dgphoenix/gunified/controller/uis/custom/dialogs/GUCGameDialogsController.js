import GUDialogsController from './GUDialogsController';
import GUCGameDialogsInfo from '../../../../model/uis/custom/dialogs/GUCGameDialogsInfo';

class GUCGameDialogsController extends GUDialogsController
{
	constructor(aOptDialogsInfo)
	{
		super(aOptDialogsInfo || new GUCGameDialogsInfo());
	}

	_updateSoundButtonPosition()
	{
		let lSoundButtonView_sbv = this._soundButtonController.view;
		lSoundButtonView_sbv.position.set(-445, -205);

		if (APP.isMobile)
		{
			lSoundButtonView_sbv.scale.set(1.8);
			lSoundButtonView_sbv.position.x += 4;
		}
	}
}

export default GUCGameDialogsController