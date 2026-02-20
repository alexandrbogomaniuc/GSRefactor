import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const SCALES = [	{x: 1, y: 1.2},
					{x: -1, y: -1.2},
					{x: -1, y: 1.2},
					{x: 1, y: -1.2}];

class Lightning extends Sprite {

	constructor(){
		super();		
		this.assetSprite = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/plasma_arcs"));
		this.assetSprite.blendMode = PIXI.BLEND_MODES.ADD;
		this.tickerCounter = 0;
		this.randomNumber = Utils.random(2, 4);
		this.currentScaleIndex = 0;
		APP.on("tick", this._onTick, this);
	}

	updatePivot(){
		this.pivot.set((-192)*this.scale.x, 0);
	}

	destroy(){
		APP.off("tick", this._onTick, this);

		this.assetSprite = null;
		this.tickerCounter = undefined;
		this.randomNumber = undefined;
		this.currentScaleIndex = undefined;

		super.destroy();
	}

	_onTick(delta){
		this.tickerCounter++;
		this.updatePivot();
		if (this.tickerCounter%this.randomNumber === 0)
		{
			this.currentScaleIndex++;
			let index = this.currentScaleIndex%4;
			let scaleObj = SCALES[index];
			this.assetSprite.scale.x = scaleObj.x;
			this.assetSprite.scale.y = scaleObj.y;
		}

	}

}

export default Lightning;