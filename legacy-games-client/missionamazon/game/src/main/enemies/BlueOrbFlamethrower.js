import BlueOrb from "./BlueOrb";
import {ENEMIES} from '../../../../shared/src/CommonConstants';

class BlueOrbFlamethrower extends BlueOrb
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
			this.emit(BlueOrb.EVENT_ORB_CALLOUT_CREATED, {data : ENEMIES.BlueOrbFlamethrower});
		}
		super.tick();
	}

	getImageName()
	{
		return 'enemies/blue_orbs/flamethrower/Flamethrower';
	}
}

export default BlueOrbFlamethrower;