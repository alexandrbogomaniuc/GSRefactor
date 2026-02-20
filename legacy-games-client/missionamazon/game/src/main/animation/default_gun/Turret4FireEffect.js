import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class Turret4FireEffect extends Sprite
{
	static get EVENT_ON_ANIMATION_COMPLETED() {return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor()
	{
		super();

		this._fTimeLine_mtl = null;
		this._fDustHorisontal_s = null;
		this._fDustHorisontal2_s = null;
		this._fDustVertical_s = null;
		this._fFlash_s = null;
		this._fSparkles_s = null;

		//DUST HORISONTAL...
		let l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/fx/dust");
		l_s.anchor.set(0.5, 0.65);
		//l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustHorisontal_s = this.addChild(l_s);
		//...DUST HORISONTAL

		//DUST HORISONTAL 2...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/fx/dust");
		l_s.anchor.set(0.5, 0.65);
		l_s.rotation = -Math.PI;
		l_s.position.set(-1.5, -3.5);
		//l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustHorisontal2_s = this.addChild(l_s);
		//...DUST HORISONTAL 2

		//DUST VERTICAL...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/fx/dust");
		l_s.anchor.set(0.1, 0.55);
		l_s.rotation = -Math.PI / 2;
		//l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustVertical_s = this.addChild(l_s);
		//...DUST VERTICAL

		//FLASH...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/fx/flash");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.alpha = 0;
		l_s.position.set(0, -3.5);
		l_s.scale.set(1.5);
		this._fFlash_s = this.addChild(l_s);
		//...FLASH

		//SPARKLES...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/fx/sparkles");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.alpha = 0;
		l_s.position.set(0, 0);
		l_s.scale.set(0);
		this._fSparkles_s = this.addChild(l_s);
		//...SPARKLES

		//ANIMATION...
		let l_mtl = new MTimeLine();

		//DUST HORISONTAL...
		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_SCALE_X,
			0.31 * 1.75 * 1.5 * 0.6,
			[
				[0.75 * 1.75 * 1.5 * 0.6, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_SCALE_Y,
			0.31 * 1.75 * 2 * 0.6,
			[
				[0.64 * 1.75 * 1.5 * 0.6, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_ALPHA,
			0.85,
			[
				7,
				[0, 6],
			]);
		//...DUST HORISONTAL

		//DUST HORISONTAL...
		l_mtl.addAnimation(
			this._fDustHorisontal2_s,
			MTimeLine.SET_SCALE_X,
			0.31 * 1.75 * 1.5 * 0.6,
			[
				[0.75 * 1.75 * 1.5 * 0.6, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal2_s,
			MTimeLine.SET_SCALE_Y,
			0.31 * 1.75 * 2 * 0.6,
			[
				[0.64 * 1.75 * 1.5 * 0.6, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal2_s,
			MTimeLine.SET_ALPHA,
			0.85,
			[
				7,
				[0, 6],
			]);
		//...DUST HORISONTAL

		//DUST VERTICAL...
		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_SCALE_X,
			0.31  * 1.5 * 1.25 * 0.6,
			[
				[0.91 * 0.6, 13],
			]);

		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_SCALE_Y,
			0.31  * 1.5 * 0.6,
			[
				[1.5 * 0.6, 13],
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
			0.85,
			[
				7,
				[0, 6],
			]);
		//...DUST VERTICAL

		//FLASH...
		l_mtl.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				[0.85, 3],
				[0, 8],
			]);

		l_mtl.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_SCALE,
			1 * 0.6,
			[
				[1.5 * 0.6, 5],
				[1, 8],
			]);

		l_mtl.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			1,
			[
				[15, 13],
			]);
		//...FLASH

		//SPARKLES...
		l_mtl.addAnimation(
			this._fSparkles_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 2],
				2,
				[0, 2],
			]);

		l_mtl.addAnimation(
			this._fSparkles_s,
			MTimeLine.SET_SCALE,
			1 * 0.6,
			[
				[2.2 * 0.6, 5],
			]);

		l_mtl.addAnimation(
			this._fSparkles_s,
			MTimeLine.SET_Y,
			0,
			[
				[-160, 5],
			]);
		//...SPARKLES

		l_mtl.callFunctionOnFinish(
			this._onAnimationCompleted,
			this);

		l_mtl.play();

		this._fTimeLine_mtl = l_mtl;
		//...ANIMATION

		this.scale.set(0.6);
	}

	_onAnimationCompleted()
	{
		this.emit(Turret4FireEffect.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		super.destroy();

		this._fCasingsAnimation_ca = null;
		this._fFire_spr = null;
		this._fBullets_spr = null;
		this._fWeaponOffsetYFiringContinuously_int = null;

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

export default Turret4FireEffect 