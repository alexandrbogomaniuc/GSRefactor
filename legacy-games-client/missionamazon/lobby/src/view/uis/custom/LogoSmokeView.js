import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class LogoSmokeView extends Sprite
{
	constructor()
	{
		super();

		this._smokeContainer = this.addChild(new Sprite());

		this._addSmoke();
		this._startAnimation();
	}

	_addSmoke()
	{
		let smoke = this._smokeContainer.addChild(APP.library.getSprite("preloader/logo/logo_smoke"));
		smoke.blendMode = PIXI.BLEND_MODES.ADD;

		this._smokeContainer.alpha = 0.3;
	}

	_startAnimation()
	{
		this._smokeContainer.rotateTo(Utils.gradToRad(60), 149*FRAME_RATE);
		this._smokeContainer.fadeTo(0.5, 57*FRAME_RATE, undefined, () => {
																			this._smokeContainer.fadeTo(0, 92*FRAME_RATE)}
									);

		this._smokeContainer.moveTo(0, -600, 149*FRAME_RATE, undefined, () => {this._onAnimationFinished();});
	}

	_onAnimationFinished()
	{
		this.destroy();
	}

	destroy()
	{
		this._smokeContainer = null;
		
		super.destroy();
	}
}

export default LogoSmokeView;