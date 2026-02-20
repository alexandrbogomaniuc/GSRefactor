import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MissEffect from './MissEffect';
import MTimeLine from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import MAnimation from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';

class MissEffectLevel2 extends MissEffect
{
	constructor()
	{
		super()

		this._fFlash_s = null;
		this._fCircle_s = null;
		this._fRay_s = null;
		this._fExplosion_s = null;
		this._fExplosion2_s = null;
		this._fVerticalSmokes_s_arr = null;
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
		l_s = APP.library.getSprite("enemy_impact/circle_blast/circle_blast_2");
		l_s.blendMode = PIXI.BLEND_MODES.SCREEN;
		l_s.scale.set(0);
		this._fCircle_s = this.addChild(l_s);
		//...CIRCLE

		//RAY...
		l_s = APP.library.getSprite("enemy_impact/circle_blast/sparkle_violet");
		l_s.blendMode = PIXI.BLEND_MODES.SCREEN;
		l_s.scale.set(0);
		l_s.position.set(-12, 7);
		this._fRay_s = this.addChild(l_s);
		//...RAY

		//EXPLOSION...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_2");
		l_s.scale.set(0);
		l_s.position.set(-7, 7);
		this._fExplosion_s = this.addChild(l_s);
		//...EXPLOSION

		//EXPLOSION 2...
		l_s = APP.library.getSprite("enemy_impact/explosion/explosion_2");
		l_s.scale.set(0);
		l_s.position.set(14, -31);
		this._fExplosion2_s = this.addChild(l_s);
		//...EXPLOSION 2

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
				[0.44 * 3, 8],
			]);

		l_mt.addAnimation(
			this._fCircle_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				2,
				[0, 6],
			]);
		//...CIRCLE

		//RAY...
		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_SCALE,
			0,
			[
				4,
				[0.48 * 12, 14, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT],
			]);

		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[15.4, 21],
			]);

		l_mt.addAnimation(
			this._fRay_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				4,
				[1, 17],
				[0, 4],
			]);
		//...RAY

		//EXPLOSION...
		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_SCALE,
			0,
			[
				2,
				[0.24 * 5, 10],
				[0.48 * 5, 10],
			]);

		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				2,
				[26, 20],
			]);

		l_mt.addAnimation(
			this._fExplosion_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				2,
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
				6,
				[0.24 * 3, 10],
				[0.48 * 3, 10],
			]);

		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				6,
				[-26, 20],
			]);

		l_mt.addAnimation(
			this._fExplosion2_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				8,
				[1, 1],
				8,
				[0, 10],
			]);
		//...EXPLOSION 2

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

export default MissEffectLevel2;