import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MissEffect from './MissEffect';
import MTimeLine from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import MAnimation from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';

class MissEffectLevel5 extends MissEffect
{
	constructor()
	{
		super()

		this._fFlash_s = null;
		this._fCircle_s = null;
		this._fCircleBottom_s = null;
		this._fRay_s = null;
		this._fRay2_s = null;
		this._fRay3_s = null;
		this._fExplosionBig_s = null;
		this._fExplosion_s = null;
		this._fExplosion2_s = null;
		this._fExplosion3_s = null;
		this._fExplosion4_s = null;
		this._fExplosion5_s = null;
		this._fExplosion6_s = null;
		this._fVerticalSmokes_s_arr = null;
		this.scale.set(1.125);
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

		//VERTICAL SMOKES...
		if (MissEffect.IS_SMOKE_REQUIRED)
		{
			this._fVerticalSmokes_s_arr = [];

			for (let i = 0; i < 2; i++)
			{
				l_s = APP.library.getSprite("enemy_impact/smoke");
				l_s.anchor.set(0.5, 0.85);
				l_s.scale.set(0);

				this._fVerticalSmokes_s_arr[i] = this.addChild(l_s);
			}
		}
		//...VERTICAL SMOKES

		//CIRCLE...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/circle_blast_5");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(0, -25);
		this._fCircle_s = this.addChild(l_s);
		//...CIRCLE

		//CIRCLE BOTTOM...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/circle_blast_bottom_5");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(0, 70);
		this._fCircleBottom_s = this.addChild(l_s);
		//...CIRCLE BOTTOM

		//EXPLOSION 1 BIG...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(0, 24);
		this._fExplosionBig_s = this.addChild(l_s);
		//...EXPLOSION 1 BIG

		//RAY...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/sparkle_red");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(-1.5, 24);
		this._fRay_s = this.addChild(l_s);
		//...RAY

		//RAY 2...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/sparkle_red");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(-17.1, 12.3)
		this._fRay2_s = this.addChild(l_s);
		//...RAY 2

		//RAY 3...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/sparkle_red");
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		l_s.scale.set(0);
		l_s.position.set(-10.8, -3.6);
		this._fRay3_s = this.addChild(l_s);
		//...RAY 3

		//EXPLOSION...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(0, 24);
		this._fExplosion_s = this.addChild(l_s);
		//...EXPLOSION

		//EXPLOSION 2...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(-20.64, 3.3);
		this._fExplosion2_s = this.addChild(l_s);
		//...EXPLOSION 2

		//EXPLOSION 3...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(-19.8, 25.2);
		this._fExplosion3_s = this.addChild(l_s);
		//...EXPLOSION 3

		//EXPLOSION 4...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(8.4, 1.2);
		this._fExplosion4_s = this.addChild(l_s);
		//...EXPLOSION 4

		//EXPLOSION 5...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(10.8, 11.4);
		this._fExplosion5_s = this.addChild(l_s);
		//...EXPLOSION 5

		//EXPLOSION 6...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_4_5");
		l_s.scale.set(0);
		l_s.position.set(4.8, 28);
		this._fExplosion6_s = this.addChild(l_s);
		//...EXPLOSION 6

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
				[2.42, 9],
			]);

		l_mt.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_ALPHA,
			0.8,
			[
				[0, 7],
			]);
		//...CIRCLE

		//CIRCLE BOTTOM...
		l_mt.addAnimation(
			this._fCircleBottom_s,
			MTimeLine.SET_SCALE,
			0,
			[
				[2.2, 11],
			]);

		l_mt.addAnimation(
			this._fCircleBottom_s,
			MTimeLine.SET_ALPHA,
			0.8,
			[
				[0, 7],
			]);
		//...CIRCLE BOTTOM

		//RAY...
		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_SCALE,
			0,
			[
				3,
				[2.1, 14, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT],
			]);

		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				3,
				[21, 28],
			]);

		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				3,
				[1, 1],
				19,
				[0, 1],
			]);
		//...RAY

		//RAY 2...
		l_mt.addAnimation(
			this._fRay2_s,
			MTimeLine.SET_SCALE,
			0,
			[
				9,
				[2.1, 16, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT],
			]);

		l_mt.addAnimation(
			this._fRay2_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				9,
				[21, 28],
			]);

		l_mt.addAnimation(
			this._fRay2_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				9,
				[1, 1],
				21,
				[0, 1],
			]);
		//...RAY 2

		//RAY 3...
		l_mt.addAnimation(
			this._fRay3_s,
			MTimeLine.SET_SCALE,
			0,
			[
				14,
				[1.68, 14, MAnimation.EASE_IN],
				[0, 9, MAnimation.EASE_OUT],
			]);

		l_mt.addAnimation(
			this._fRay3_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				14,
				[21, 28],
			]);

		l_mt.addAnimation(
			this._fRay3_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				14,
				[1, 1],
				21,
				[0, 1],
			]);
		//...RAY 3

		//EXPLOSION 1 BIG...
		l_mt.addAnimation(
			this._fExplosionBig_s,
			MTimeLine.SET_SCALE,
			0,
			[
				4,
				[1.47, 20]
			]);

		l_mt.addAnimation(
			this._fExplosionBig_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				4,
				[1, 1],
				8,
				[0, 10],
			]);

		l_mt.addAnimation(
			this._fExplosionBig_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[20, 30],
			]);
		//...EXPLOSION 1 BIG

		//EXPLOSION...
		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_SCALE,
			0,
			[
				5,
				[0.72, 20]
			]);

		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[20, 30],
			]);

		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				5,
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
				7,
				[0.72, 20]
			]);

		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[20, 30],
			]);

		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				7,
				[1, 1],
				8,
				[0, 10],
			]);
		//...EXPLOSION 2

		//EXPLOSION 3...
		l_mt.addAnimation(
			this._fExplosion3_s,
			MTimeLine.SET_SCALE,
			0,
			[
				9,
				[0.72, 20]
			]);

		l_mt.addAnimation(
			this._fExplosion3_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[-24, 36],
			]);

		l_mt.addAnimation(
			this._fExplosion3_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				9,
				[1, 1],
				8,
				[0, 10],
			]);
		//...EXPLOSION 3

		//EXPLOSION 4...
		l_mt.addAnimation(
			this._fExplosion4_s,
			MTimeLine.SET_SCALE,
			0,
			[
				10,
				[0.72, 20]
			]);

		l_mt.addAnimation(
			this._fExplosion4_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[-24, 36],
			]);

		l_mt.addAnimation(
			this._fExplosion4_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				10,
				[1, 1],
				8,
				[0, 10],
			]);
		//...EXPLOSION 4

		//EXPLOSION 5...
		l_mt.addAnimation(
			this._fExplosion5_s,
			MTimeLine.SET_SCALE,
			0,
			[
				11,
				[0.72, 20]
			]);

		l_mt.addAnimation(
			this._fExplosion5_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				11,
				[1, 1],
				8,
				[0, 10],
			]);

		l_mt.addAnimation(
			this._fExplosion5_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[24, 36],
			]);
		//...EXPLOSION 5

		//EXPLOSION 6...
		l_mt.addAnimation(
			this._fExplosion6_s,
			MTimeLine.SET_SCALE,
			0,
			[
				14,
				[0.72, 20]
			]);

		l_mt.addAnimation(
			this._fExplosion6_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				14,
				[1, 1],
				8,
				[0, 10],
			]);

		l_mt.addAnimation(
			this._fExplosion6_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[-24, 36],
			]);
		//...EXPLOSION 6

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

export default MissEffectLevel5;