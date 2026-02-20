import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const SMOKE_TRAIL_ETERNAL = {
	"alpha": {
		"start": 1,
		"end": 0
	},
	"scale": {
		"start": 0.001,
		"end": 1,
		"minimumScaleMultiplier": 1
	},
	"color": {
		"start": "#ffffff",
		"end": "#ffffff"
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
		"min": -90,
		"max": -90
	},
	"noRotation": true,
	"rotationSpeed": {
		"min": 0,
		"max": 0
	},
	"lifetime": {
		"min": 1,
		"max": 1
	},
	"blendMode": "add",
	"frequency": 0.3,
	"emitterLifetime": -1,
	"maxParticles": 4,
	"pos": {
		"x": 0,
		"y": 0
	},
	"addAtBack": true,
	"spawnType": "point"
}

class EternalSmoke extends Sprite {
	constructor(indigoSmoke = false, indigoBlendMode = "normal"){
		super();

		let assetName = this.__getSmokeAssetName(indigoSmoke);
		let texture = null;
		texture = APP.library.getSpriteFromAtlas(assetName).textures[0];
		this.emitterContainer = this.addChild(new Sprite);

		var params = Object.assign(SMOKE_TRAIL_ETERNAL, indigoSmoke ? {"blendMode":indigoBlendMode} : {"blendMode": "add"});		

		this.eternalEmitter = new PIXI.particles.Emitter(this.emitterContainer, [texture], params);
		this.eternalEmitter.updateOwnerPos(this.x, this.y);

		this._tickFunc = this.tick.bind(this);

		APP.on("tick", this._tickFunc);
	}

	__getSmokeAssetName()
	{
		throw("The __getSmokeAssetName method must be overridden.")
	}

	destroy()
	{
		this._tickFunc && APP.off ("tick", this._tickFunc);
		this._tickFunc = null;

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

	tick(e){
		if (this.eternalEmitter)
		{
			this.updateEternalEmitterPosition();		
			this.eternalEmitter.update(e.delta/1000);		
		}
	}
}

export default EternalSmoke;