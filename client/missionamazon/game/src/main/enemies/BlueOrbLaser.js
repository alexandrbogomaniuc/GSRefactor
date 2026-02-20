import BlueOrb from "./BlueOrb";
import {ENEMIES} from '../../../../shared/src/CommonConstants';

class BlueOrbLaser extends BlueOrb
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
			this.emit(BlueOrb.EVENT_ORB_CALLOUT_CREATED, {data : ENEMIES.BlueOrbLaser});
		}
		super.tick();
	}

	getImageName()
	{
		return 'enemies/blue_orbs/laser/Laser';
	}
}

export default BlueOrbLaser;