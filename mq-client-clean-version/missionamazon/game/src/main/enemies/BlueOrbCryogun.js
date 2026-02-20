import BlueOrb from "./BlueOrb";
import {ENEMIES} from '../../../../shared/src/CommonConstants';

class BlueOrbCryogun extends BlueOrb
{
	constructor(params)
	{
		super(params);
	}

	tick()
	{
		if (this._fIsCalloutAwaiting_bl)
		{
			this._fIsCalloutAwaiting_bl = false;
			this.emit(BlueOrb.EVENT_ORB_CALLOUT_CREATED, {data : ENEMIES.BlueOrbCryogun});
		}
		super.tick();
	}

	getImageName()
	{
		return 'enemies/blue_orbs/cryogun/Cryogun';
	}
}

export default BlueOrbCryogun;