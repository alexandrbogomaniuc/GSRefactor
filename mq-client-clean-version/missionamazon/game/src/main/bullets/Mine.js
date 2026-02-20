import Bomb from './Bomb';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import PlayerSpot from '../playerSpots/PlayerSpot';


class Mine extends Bomb {

	constructor(params, points, callback)
	{
		super(params, points, callback);

		//offset startPoint
		this._fWeaponScale = params.weaponScale ? params.weaponScale: 1;
		let angle = Math.atan2(this.endPos.x - this.startPos.x, this.endPos.y - this.startPos.y) + Math.PI/2;
		let offset = 54 - 104 * this._fWeaponScale;
		this.startPos.x += Math.cos(angle) * offset;
		this.startPos.y -= Math.sin(angle) * offset;
	}

	_initFireRotation()
	{
		//nothing to do
	}

	_finalizeFireRotation(aTime_num)
	{
		let angle = -Math.PI;
		if (Math.abs(this.fire.rotation - (-Math.PI)) < 0.7 )
		{
			angle = this.fire.rotation + Math.PI*2;
		}

		this.fire.rotateTo(angle , aTime_num, Easing.quadratic.easeIn);
	}

	get _finalScale()
	{
		return 1;
	}

	addFire(){
		this.fire = this.addChild(new Sprite);
		this.bombThrown = this._bombThrownSprite;
		
		this.fire.addChild(this.bombThrown);
		this.fire.zIndex = 2;

		this._createShadow();

		this.fire.rotation = this.fireRotation;
		this.fire.scale.set(PlayerSpot.WEAPON_SCALE * this._fWeaponScale);
	}

	_createShadow()
	{
		this.shadow = this.addChild(new Sprite());
		this.shadow.view = this.shadow.addChild(APP.library.getSprite('shadow'));
		this.shadow.view.anchor.set(103/235, 67/136);
		this.shadow.view.alpha = 0.8;
		this.shadow.view.scale.x = 0.38 * this._fWeaponScale;
		this.shadow.view.scale.y = 0.48 * this._fWeaponScale;
		this.shadow.zIndex = 1;
	}

	get _bombThrownSprite()
	{
		return APP.library.getSprite('weapons/MineLauncher/mine_flying');
	}

	destroy(){
		this._fWeaponScale = null;
		super.destroy();
	}
}

export default Mine;