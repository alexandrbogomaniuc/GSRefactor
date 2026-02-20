import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import PathTween from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';

const CASINGS_COUNT = 3;
const CASINGS_SPAWN_DURATION = 4;

class  CasingsAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_COMPLETED() {return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor()
	{
		super();

		this._fCasings_spr_arr = [];

		this._startAnimation();
	}

	_startAnimation()
	{
		for (let i = 0; i < CASINGS_COUNT; i++)
		{
			let lLeftCasing_spr = this.addChild(APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/casing"));
			lLeftCasing_spr.scale.set(1.5);
			lLeftCasing_spr.alpha = 0;
			this._fCasings_spr_arr.push(lLeftCasing_spr);
			let lLeftCasingAnimation_arr = [
				{tweens: [],							duration: (0 + CASINGS_SPAWN_DURATION * i) * FRAME_RATE, onfinish: () => {
					let lLeftPathTween_ptw = new PathTween(lLeftCasing_spr, [{x: -3, y: 0}, {x: -3 - 8, y: 0 - 10}, {x: -3 - 14, y: 0 + 6}], true, false);
					lLeftPathTween_ptw.start(14 * FRAME_RATE);

					lLeftCasing_spr.alpha = 1;

					let lLeftAngle_arr = [
						{tweens: [{prop: 'rotation', to: -Math.PI * 2 * 2}],		duration: 14 * FRAME_RATE},
					];
					Sequence.start(lLeftCasing_spr, lLeftAngle_arr);

					let lLeftScale_arr = [
						{tweens: [{prop: 'scale.x', to: 0},			{prop: 'scale.y', to: 0}],		duration: 14 * FRAME_RATE},
					];
					Sequence.start(lLeftCasing_spr, lLeftScale_arr);
				}},
				{tweens: [{prop: 'alpha', to: 0}],		duration: 14 * FRAME_RATE, onfinish: () => { this._destroyCasingAnimation(lLeftCasing_spr) }},
			];
			Sequence.start(lLeftCasing_spr, lLeftCasingAnimation_arr)

			let lRightCasing_spr = this.addChild(APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/casing"));
			lRightCasing_spr.scale.set(1.5);
			lRightCasing_spr.alpha = 0;
			this._fCasings_spr_arr.push(lRightCasing_spr);
			let lRightCasingAnimation_arr = [
				{tweens: [],							duration: (0 + CASINGS_SPAWN_DURATION * i) * FRAME_RATE, onfinish: () => {
					let lRightPathTween_ptw = new PathTween(lRightCasing_spr, [{x: 3, y: 0}, {x: 3 + 8, y: 0 - 10}, {x: 3 + 14, y: 0 + 6}], true, false);
					lRightPathTween_ptw.start(14 * FRAME_RATE);
					
					lRightCasing_spr.alpha = 1;

					let lRightAngle_arr = [
						{tweens: [{prop: 'rotation', to: Math.PI * 2 * 2}],		duration: 14 * FRAME_RATE},
					];
					Sequence.start(lRightCasing_spr, lRightAngle_arr);

					let lRightScale_arr = [
						{tweens: [{prop: 'scale.x', to: 0},			{prop: 'scale.y', to: 0}],		duration: 14 * FRAME_RATE},
					];
					Sequence.start(lRightCasing_spr, lRightScale_arr);
				}},
				{tweens: [{prop: 'alpha', to: 0}],		duration: 14 * FRAME_RATE, onfinish: () => { this._destroyCasingAnimation(lRightCasing_spr) }},
			];
			Sequence.start(lRightCasing_spr, lRightCasingAnimation_arr)
		}
	}

	_destroyCasingAnimation(aCasing_spr)
	{
		let l_num = this._fCasings_spr_arr.indexOf(aCasing_spr);
		if (l_num != -1)
		{
			let l_spr_arr = this._fCasings_spr_arr.splice(l_num, 1);
			let l_spr = l_spr_arr[0];
			PathTween.destroy(PathTween.findByTarget(l_spr));
			Sequence.destroy(Sequence.findByTarget(l_spr));
			l_spr && l_spr.destroy();
			l_spr = null;
		}
		
		if(this._fCasings_spr_arr.length == 0)
		{
			this._fCasings_spr_arr = null;
			this.emit(CasingsAnimation.EVENT_ON_ANIMATION_COMPLETED);
		}
	}

	destroy()
	{
		super.destroy();

		if (this._fCasings_spr_arr)
		{
			for (let l_spr of this._fCasings_spr_arr)
			{
				PathTween.destroy(PathTween.findByTarget(l_spr));
				Sequence.destroy(Sequence.findByTarget(l_spr));
				l_spr && l_spr.destroy();
				l_spr = null;
			}

			this._fCasings_spr_arr = [];
			this._fCasings_spr_arr = null;
		}
	}
}

export default CasingsAnimation;