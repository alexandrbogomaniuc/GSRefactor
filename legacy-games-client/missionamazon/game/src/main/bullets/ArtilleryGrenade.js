import Mine from './Mine';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class ArtilleryGrenade extends Mine {

	get _bombThrownSprite()
	{
		return APP.library.getSpriteFromAtlas('weapons/ArtilleryStrike/artillery_grenade_tossed');
	}

	disappear()
	{
		//we'll not make it disappear, it should stay at the place untill artillery strikes completed
		let lMask_gr = new PIXI.Graphics();
		lMask_gr.beginFill(0x00ff00);

		let lBounds_obj = this.bombThrown.getLocalBounds();
		lMask_gr.drawRect(lBounds_obj.x, lBounds_obj.y, lBounds_obj.width, lBounds_obj.height - 8);
		lMask_gr.endFill();
		
		this.addChild(lMask_gr);
		this.bombThrown.mask = lMask_gr;

		this.zIndex = this.y + 13;
	}

	_finalizeFireRotation(aTime_num)
	{
		let angle = -Math.PI;
		let sign = Math.abs(this.fire.rotation) > Math.PI ? 1 : -1;
		angle += sign * Math.PI + Utils.gradToRad(4);

		this.fire.rotateTo(angle , aTime_num, Easing.quadratic.easeIn);
	}

	_createShadow()
	{
		this.shadow = this.addChild(new Sprite());
		this.shadow.view = this.shadow.addChild(APP.library.getSprite('shadow'));
		this.shadow.view.anchor.set(103/235, 67/136);
		this.shadow.view.alpha = 0.98;
		this.shadow.view.scale.x = 2*0.38;
		this.shadow.view.scale.y = 2*0.48;	
		this.shadow.zIndex = 1;

		this.shadow.view.y = 13; //offset
		this.shadow.view.x = -3;
	}

	get _finalScale()
	{
		return 0.6;
	}

	destroy()
	{
		super.destroy();
	}
}

export default ArtilleryGrenade;