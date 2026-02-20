import Gun from '../Gun';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../GameField';
import RicochetLasersController from '../../../controller/uis/weapons/ricochet_laser/RicochetLasersController';

class RicochetLaserGun extends Gun
{

	constructor()
	{
		super();

		this._fLaserGun_sprt = null;

		this._initView();
		this.idle();
	}

	//override
	reset()
	{
		super.reset();
		
		if (this.isIdleState)
		{
			return;
		}

		this.idle();
	}

	_initView()
	{
		this._fLaserGun_sprt = APP.library.getSpriteFromAtlas('weapons/RicochetGun/RicocjetGun');
		this.addChild(this._fLaserGun_sprt);

		this._fLaserGun_sprt.anchor = {x: 0.421053, y: 0.433692};
	}

	//override
	_initIdleState()
	{
		// nothing for now
	}

	//override
	_initReloadState()
	{
		// nothing for now
	}

	//override
	_initShotState()
	{
		APP.currentWindow.ricochetLasersController.off(RicochetLasersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._onFinishShot, this, true);
		APP.currentWindow.ricochetLasersController.once(RicochetLasersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._onFinishShot, this);
	}

	_onFinishShot()
	{
		this.emit(Gun.EVENT_ON_SHOT_COMPLETED);
		this.idle();
	}
}

export default RicochetLaserGun;