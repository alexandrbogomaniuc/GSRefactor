import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class Turret3FireEffect extends Sprite
{
	static get EVENT_ON_ANIMATION_COMPLETED() {return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor()
	{
		super();

		this._fTimeLine_mtl = null;
		this._fDustHorisontal_s = null;
		this._fDustVertical_s = null;
		this._fSparkles_s = null;
		this._fCircle_s = null;
		this._fCircle2_s = null;
		this._fFlash_s = null;

		//DUST HORISONTAL...
		let l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_3/fx/dust");
		l_s.anchor.set(0.5, 0.65);
		//l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustHorisontal_s = this.addChild(l_s);
		//...DUST HORISONTAL

		//DUST VERTICAL...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_3/fx/dust");
		l_s.anchor.set(0.1, 0.55);
		l_s.rotation = -Math.PI / 2;
		//l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fDustVertical_s = this.addChild(l_s);
		//...DUST VERTICAL

		//SPARKLES...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_3/fx/sparkles");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.alpha = 0;
		l_s.position.set(0, 0);
		l_s.scale.set(0);
		this._fSparkles_s = this.addChild(l_s);
		//...SPARKLES

		//CIRCLE...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_3/fx/circle");
		l_s.anchor.set(0.5, 0.5);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		this._fCircle_s = this.addChild(l_s);
		//...CIRCLE

		//CIRCLE 2...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_3/fx/circle");
		l_s.anchor.set(0.5, 0.5);
		//l_s.position.set(0, 20);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		this._fCircle2_s = this.addChild(l_s);
		//...CIRCLE 2

		//FLASH...
		l_s = APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_3/fx/flash");
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
			0.31 * 1.75 * 2 * 0.65,
			[
				[0.75 * 1.75 * 2 * 0.65, 13],
			]);

		l_mtl.addAnimation(
			this._fDustHorisontal_s,
			MTimeLine.SET_SCALE_Y,
			0.31 * 1.75 * 2 * 0.65,
			[
				[0.64 * 1.75 * 2 * 0.65, 13],
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
			0.31 * 2 * 1.25 * 0.65,
			[
				[0.91 * 2 * 1.25 * 0.65, 13],
			]);

		l_mtl.addAnimation(
			this._fDustVertical_s,
			MTimeLine.SET_SCALE_Y,
			0.31 * 2 * 1.25 * 0.65,
			[
				[3 * 1.25 * 0.65, 13],
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

		//SPARKLES...
		l_mtl.addAnimation(
			this._fSparkles_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 3],
				3,
				[0, 3],
			]);

		l_mtl.addAnimation(
			this._fSparkles_s,
			MTimeLine.SET_SCALE,
			1 * 0.65,
			[
				[1.5 * 0.65, 5],
			]);

		l_mtl.addAnimation(
			this._fSparkles_s,
			MTimeLine.SET_Y,
			0,
			[
				[-100, 8],
			]);
		//...SPARKLES

		//CIRCLE...
		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_SCALE_Y,
			0.9 * 0.6 * 0.8 * 0.65,
			[
				[2 * 0.4 * 0.8 * 0.65, 10],
			]);

		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_SCALE_X,
			0.54 * 0.65,
			[
				[1.2 * 0.65, 10],
			]);

		l_mtl.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_ALPHA,
			0.7,
			[
				3,
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

		//CIRCLE 2...
		l_mtl.addAnimation(
			this._fCircle2_s,
			MTimeLine.SET_SCALE,
			0.2365 * 0.65,
			[
				[0.91 * 0.65, 10],
			]);

		l_mtl.addAnimation(
			this._fCircle2_s,
			MTimeLine.SET_ALPHA,
			0.7 * 0.65,
			[
				3,
				[0.28 * 0.65, 4],
				[0, 3],
			]);

		l_mtl.addAnimation(
			this._fCircle2_s,
			MTimeLine.SET_Y,
			10,
			[
				[-60, 13],
			]);
		//...CIRCLE 2

		//FLASH...
		l_mtl.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_SCALE,
			0,
			[
				[2 * 0.65, 5],
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

		this.scale.set(0.65);
	}

	_onAnimationCompleted()
	{
		this.emit(Turret3FireEffect.EVENT_ON_ANIMATION_COMPLETED);
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

export default Turret3FireEffect 