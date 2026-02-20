import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MissEffect from './MissEffect';
import MTimeLine from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import MAnimation from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';
import { Sprite } from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';

class MissEffectLevel3 extends MissEffect
{
	constructor()
	{
		super()

		this._fFlash_s = null;
		this._fSmoke_s = null;
		this._fCircle_s = null;
		this._fRay_s = null;
		this._fRay2_s = null;
		this._fExplosion_s = null;
		this._fExplosion2_s = null;
		this._fExplosion3_s = null;
		this._fExplosion4_s = null;
		this._fVerticalSmokes_s_arr = null;
		this.scale.set(0.75);
	}

	//override
	init()
	{
		//FLASH...
		let l_s = APP.library.getSprite("hitflash");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0.19);
		this._fFlash_s = this.addChild(l_s);
		//...FLASH

		//CIRCLE...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/circle_blast_3");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		this._fCircle_s = this.addChild(l_s);
		//...CIRCLE

		//RAY...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/sparkle_blue");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(10, 20);
		this._fRay_s = this.addChild(l_s);
		//...RAY

		//RAY 2...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/sparkle_blue");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(-15, -15);
		this._fRay2_s = this.addChild(l_s);
		//...RAY 2

		//EXPLOSION...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_3");
		l_s.scale.set(0);
		l_s.position.set(10, 20);
		this._fExplosion_s = this.addChild(l_s);
		//...EXPLOSION

		//EXPLOSION 2...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_3");
		l_s.scale.set(0);
		l_s.position.set(-25, 25);
		this._fExplosion2_s = this.addChild(l_s);
		//...EXPLOSION 2

		//EXPLOSION 3...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_3");
		l_s.scale.set(0);
		l_s.position.set(-25, -35);
		this._fExplosion3_s = this.addChild(l_s);
		//...EXPLOSION 3


		//EXPLOSION 4...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_3");
		l_s.scale.set(0);
		l_s.position.set(20, -25);
		this._fExplosion4_s = this.addChild(l_s);
		//...EXPLOSION 4

		if (MissEffect.IS_SMOKE_REQUIRED)
		{
			//VERTICAL SMOKES...
			this._fVerticalSmokes_s_arr = [];

			for (let i = 0; i < 2; i++)
			{
				l_s = APP.library.getSprite("enemy_impact/smoke");
				l_s.anchor.set(0.5, 0.85);
				l_s.scale.set(0);

				this._fVerticalSmokes_s_arr[i] = this.addChild(l_s);
			}
			//...VERTICAL SMOKES
		}


		//ANIMATION...
		let l_mt = new MTimeLine();

		//FLASH...
		l_mt.addAnimation(
			this._fFlash_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				1,
				[0, 2],
			]);
		//...FLASH

		//CIRCLE...
		l_mt.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_SCALE,
			0,
			[
				1,
				[0.31 * 7.5, 8]
			]);

		l_mt.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				1,
				[0, 6],
			]);
		//...CIRCLE

		//RAY...
		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_SCALE,
			0,
			[
				1,
				[0.37 * 6, 14, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT],
			]);

		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				1,
				[22, 30],
			]);

		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				1,
				[1, 1],
				21,
				[0, 1],
			]);
		//...RAY


		//RAY 2...
		l_mt.addAnimation(
			this._fRay2_s,
			MTimeLine.SET_SCALE,
			0,
			[
				14,
				[0.37 * 6, 14, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT],
			]);

		l_mt.addAnimation(
			this._fRay2_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[22, 30],
			]);
		l_mt.addAnimation(
			this._fRay2_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				14,
				[1, 1],
				21,
				[0, 1],
			]);
		//...RAY 2

		//EXPLOSION...
		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_SCALE,
			0,
			[
				3,
				[0.48 * 2.5, 20],
				[0, 2],
			]);

		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				3,
				[16, 24],
			]);

		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				3,
				[1, 1],
				8,
				[0, 10],
			]);
		//...EXPLOSION

		//EXPLOSION 2...
		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_SCALE,
			0,
			[
				8,
				[0.48 * 2.5, 17],
				[0, 3],
			]);

		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				8,
				[16, 24],
			]);

		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				8,
				[1, 1],
				9,
				[0, 10],
			]);
		//...EXPLOSION 2

		//EXPLOSION 3...
		l_mt.addAnimation(
			this._fExplosion3_s,
			MTimeLine.SET_SCALE,
			0,
			[
				12,
				[0.30 * 2, 13],
				[0.48 * 2, 7],
			]);

		l_mt.addAnimation(
			this._fExplosion3_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				12,
				[16, 24],
			]);

		l_mt.addAnimation(
			this._fExplosion3_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				12,
				[1, 1],
				9,
				[0, 10],
			]);
		//...EXPLOSION 3


		//EXPLOSION 4...
		l_mt.addAnimation(
			this._fExplosion4_s,
			MTimeLine.SET_SCALE,
			0,
			[
				7,
				[0.42 * 1.5, 18],
				[0.48 * 1.5, 2],
			]);

		l_mt.addAnimation(
			this._fExplosion4_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				7,
				[16, 24],
			]);

		l_mt.addAnimation(
			this._fExplosion4_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				5,
				[1, 1],
				9,
				[0, 10],
			]);
		//...EXPLOSION 4

		if (MissEffect.IS_SMOKE_REQUIRED)
		{
			//VERTICAL SMOKES...
			for (let i = 0; i < this._fVerticalSmokes_s_arr.length; i++)
			{
				let l_s = this._fVerticalSmokes_s_arr[i];

				l_mt.addAnimation(
					l_s,
					MTimeLine.SET_ALPHA,
					0,
					[
						3 + 7 * i,
						[0.75, 5],
						10,
						[0, 5],
					]);

				l_mt.addAnimation(
					l_s,
					MTimeLine.SET_SCALE,
					0,
					[
						3 + 7 * i,
						[2.5, 20],
					]);

				l_mt.addAnimation(
					l_s,
					MTimeLine.SET_X,
					0,
					[
						3 + 7 * i,
						[-20, 20],
					]);

				l_mt.addAnimation(
					l_s,
					MTimeLine.SET_Y,
					0,
					[
						3 + 7 * i,
						[-50, 20],
					]);

			}
			//...VERTICAL SMOKES
		}

		this.__fTimeLine_mt = l_mt;
		//...ANIMATION
	}
}

export default MissEffectLevel3;