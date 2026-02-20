import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const SMOKE_TRAIL_ETERNAL = {
	"alpha": {
		"start": 1,
		"end": 0
	},
	"scale": {
		"start": 0.01,
		"end": 2,
		"minimumScaleMultiplier": 1
	},
	"speed": {
		"start": 0,
		"end": 0,
		"minimumSpeedMultiplier": 1
	},
	"acceleration": {
		"x": 0,
		"y": 0
	},
	"maxSpeed": 0,
	"startRotation": {
		"min": 0,
		"max": 360
	},
	"noRotation": false,
	"rotationSpeed": {
		"min": 0,
		"max": 360
	},
	"lifetime": {
		"min": 1,
		"max": 1
	},
	"blendMode": "add",
	"frequency": 0.2,
	"emitterLifetime": -1,
	"maxParticles": 20,
	"pos": {
		"x": 0,
		"y": 0
	},
	"addAtBack": false,
	"spawnType": "circle",
	"spawnCircle": {
		"x": 0,
		"y": 0,
		"r": 0
	}
}

class RicochetEternalLaserFlare extends Sprite 
{
	constructor()
	{
		super();

		let texture = APP.library.getSprite('weapons/RicochetGun/laser_flare').textures[0];
		this.emitterContainer = this.addChild(new Sprite);

		this.eternalEmitter = new PIXI.particles.Emitter(this.emitterContainer, [texture], SMOKE_TRAIL_ETERNAL);
		this.eternalEmitter.updateOwnerPos(this.x, this.y);
		
		APP.on("tick", this._onTick, this);
	}

	destroy(){
		APP.off("tick", this._onTick, this);
		
		if(this.eternalEmitter){
			this.eternalEmitter.destroy();
			this.eternalEmitter = null;
			this.emitterContainer = null;
		}
		super.destroy();
	}

	updateEternalEmitterPosition(){
		if(this.eternalEmitter && this.parent){
			this.eternalEmitter.updateOwnerPos(this.x, this.y);
		}
	}

	_onTick(e){
		if (this.eternalEmitter)
		{
			this.updateEternalEmitterPosition();		
			this.eternalEmitter.update(e.delta/1000);		
		}
	}
}

export default RicochetEternalLaserFlare;