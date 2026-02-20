import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../config/AtlasConfig';

class RicochetEffect extends Sprite{
	constructor(x, y){
		super();

		this.smoke = null;
		this.createView(x, y);
	}

	createView(x, y){
		this.smoke = this.addChild(new Sprite);
		this.smoke.textures = RicochetEffect.getSmokeTextures();
		this.smoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		this.smoke.position.set(x, y - 14);
		this.smoke.animationSpeed = 1;
		this.zIndex = 9000;

		this.smoke.play();
		this.smoke.once('animationend', (e) => {
			this.emit("animationFinish");
			this.destroy();
		});
	}
	
	destroy()
	{
		this.smoke = null;

		super.destroy();
	}
}

RicochetEffect.getSmokeTextures = function(){
	if(!RicochetEffect.smokeTextures){
		RicochetEffect.setSmokeTextures();
	}
	return RicochetEffect.smokeTextures;
}

RicochetEffect.setSmokeTextures = function (){
	var asset = APP.library.getAsset("blend/smoke_fx/u_smoke_screenmode");
	var config = AtlasConfig.SmokeScreenmode[0];
	var pathName = "SmokeScreenmode";

	RicochetEffect.smokeTextures = AtlasSprite.getFrames([asset], [config], pathName);
	RicochetEffect.smokeTextures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
}

export default RicochetEffect;