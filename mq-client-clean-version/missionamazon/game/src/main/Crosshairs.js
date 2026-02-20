import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class Crosshairs extends Sprite {

	constructor()
	{
		super();
		this.createView();
	}

	createView() {
		this.addChild(APP.library.getSprite('TargetCrosshair_RED'));
	}
}

export default Crosshairs;