import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import RicochetEternalLaserFlare from './RicochetEternalLaserFlare';

class RicochetFireEffect extends Sprite 
{
	constructor()
	{
		super();

		this.once('added', (e) => {this._onAdded();});
	}

	_onAdded()
	{
		this._showEffect();
	}

	_showEffect()
	{
		let laserFlare = APP.library.getSprite('weapons/RicochetGun/laser_flare');
		laserFlare.scale.x = 4*0.633;
		laserFlare.scale.y = 4*0.423;		
		laserFlare.blendMode = PIXI.BLEND_MODES.ADD;
		this.addChild(laserFlare);

		let gunLaserFlare = APP.library.getSprite('weapons/RicochetGun/gun_laser_flare');
		gunLaserFlare.scale.x = 1.55;
		gunLaserFlare.scale.y = 2.09;
		gunLaserFlare.blendMode = PIXI.BLEND_MODES.ADD;
		this.addChild(gunLaserFlare);

		let eternalLaserFlare = this.addChild(new RicochetEternalLaserFlare());
		eternalLaserFlare.scale.x = 1.2;
		eternalLaserFlare.scale.y = 2.302;
	}

	destroy()
	{
		super.destroy();
	}
}

export default RicochetFireEffect;