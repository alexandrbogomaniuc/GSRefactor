import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

//import DefaultWeaponsShotEffects from '../../DefaultWeaponsShotEffects';

class Turret1FireEffect extends Sprite
{
	static get EVENT_ON_ANIMATION_COMPLETED() {return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor()
	{
		super();

		this._fTimeLine_mtl = null;
		this._fDustHorisontal_s = null;
		this._fDustVertical_s = null;
		this._fCircle_s = null;
		this._fFlash_s = null;

		//DUST HORISONTAL...
		let l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_1/fx/dust");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustHorisontal_s = this.addChild(l_s);
		//...DUST HORISONTAL

		//DUST VERTICAL...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_1/fx/dust");
		l_s.anchor.set(0.1, 0.55);
		l_s.rotation = -Math.PI / 2;
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustVertical_s = this.addChild(l_s);
		//...DUST VERTICAL

		//CIRCLE...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_1/fx/circle");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		this._fCircle_s = this.addChild(l_s);
		//...CIRCLE

		//FLASH...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_1/fx/flash");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		this._fFlash_s = this.addChild(l_s);
		//...FLASH

		//ANIMATION...
		let l_mtl = new MTimeLine();

		//DUST HORISONTAL...
		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_SCALE_X,
			0.31 * 2,
			[
				[0.91 * 2, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_SCALE_Y,
			0.31 * 2,
			[
				[0.64 * 2, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				7,
				[0, 6],
			]);


		//...DUST HORISONTAL

		//DUST VERTICAL...

		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_SCALE_X,
			0.31 * 2,
			[
				[0.91 * 2, 13],
			]);

		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_SCALE_Y,
			0.31 * 2,
			[
				[3, 13],
			]);

		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_Y,
			0,
			[
				[-10, 13],
			]);

		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				7,
				[0, 6],
			]);
		//...DUST VERTICAL

		//CIRCLE...
		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_SCALE_Y,
			0.9 * 0.6 * 0.8,
			[
				[2 * 0.4 * 0.8, 10],
			]);

		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_SCALE_X,
			0.9 * 0.8,
			[
				[2 * 0.8, 10],
			]);

		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_ALPHA,
			0.7,
			[
				[0.7, 3],
				[0.28, 4],
				[0, 3],
			]);

		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_Y,
			0,
			[
				[-70, 13],
			]);
		//...CIRCLE

		//FLASH...
		l_mtl.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_SCALE,
			0,
			[
				[2, 5],
				[0, 5],
			]);

		l_mtl.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_Y,
			0,
			[
				[-30, 5],
			]);
		//...FLASH

		l_mtl.callFunctionOnFinish(
			this._onAnimationCompleted,
			this);

		l_mtl.play();

		this._fTimeLine_mtl = l_mtl;
		//...ANIMATION


		this.scale.set(0.5);
	}

	_onAnimationCompleted()
	{
		this.emit(Turret1FireEffect.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		if(!this._fDustVertical_s)
		{
			return;
		}
		
		this._fTimeLine_mtl.destroy();
		this._fDustVertical_s.destroy();
		this._fDustHorisontal_s.destroy();
		super.destroy();
	}
}

export default Turret1FireEffect 