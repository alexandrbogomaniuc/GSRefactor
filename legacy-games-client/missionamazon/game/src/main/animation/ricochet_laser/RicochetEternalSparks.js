import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const SPARKS_TRAIL_ETERNAL = {
	"alpha": {
		"start": 1,
		"end": 0
	},
	"scale": {
		"start": 1,
		"end": 0.7,
		"minimumScaleMultiplier": 1
	},
	"color": {
		"start": "#ffffff",
		"end": "#ffffff"
	},
	"speed": {
		"start": 300,
		"end": 200,
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
		"max": 0
	},
	"lifetime": {
		"min": 0.2,
		"max": 0.4
	},
	"blendMode": "add",
	"frequency": 0.1,
	"emitterLifetime": -1,
	"maxParticles": 8,
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

class RicochetEternalSparks extends Sprite {
	constructor(){
		super();
		let texture = APP.library.getSpriteFromAtlas('common/spark').textures[0];
		this.emitterContainer = this.addChild(new Sprite);

		this.eternalEmitter = new PIXI.particles.Emitter(this.emitterContainer, [texture], SPARKS_TRAIL_ETERNAL);
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

export default RicochetEternalSparks;