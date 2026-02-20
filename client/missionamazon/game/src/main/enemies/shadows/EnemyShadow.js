import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class EnemyShadow extends Sprite {

constructor(aEnemyName_str)
	{
		super();
		this._fEnemyName_str = aEnemyName_str;
		this.view = null;
		this._createView();
	}

	_createView()
	{
		this.view = this.addChild(this._generateShadowView());
		this.view.anchor.set(103/235, 67/136);
		this.view.alpha = 0.5;
	}

	_generateShadowView()
	{
		return APP.library.getSprite('shadow');
	}
}

export default EnemyShadow;