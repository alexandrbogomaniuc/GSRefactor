import BlueOrb from "./BlueOrb";
import {ENEMIES} from '../../../../shared/src/CommonConstants';

class BlueOrbAtrillery extends BlueOrb
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
			this.emit(BlueOrb.EVENT_ORB_CALLOUT_CREATED, {data : ENEMIES.BlueOrbArtillerystrike});
		}
		super.tick();
	}

	getImageName()
	{
		return 'enemies/blue_orbs/atrillery/Atrillery';
	}
}

export default BlueOrbAtrillery;