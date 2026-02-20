import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import EternalPlazmaSmoke from './EternalPlazmaSmoke';
import Lightning from './Lightning';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import InstantKillSmokeLoop from './InstantKillSmokeLoop';

const PARAMS =	[	{x: -31/2, y: -4/2, rotation: 10*0.017, scale: 0.12},
					{x: 31/2, y: -4/2, rotation: 70*0.017 - Math.PI, scale: 0.12},
					{x: 0, y: -92/2, rotation: -70*0.017 - Math.PI, scale: 0.26}];

const BOUNCE_BALL_ROTATION = - 30 * Math.PI / 180;

class InstantKillGunFlare extends Sprite 
{
	constructor(id)
	{
		super();

		this.id = id;
		this.lensFlare = null;
		this.plasmaSmoke = null;
		this.lightning = null;
		this.bouncingBalls = null;
		this._fSequenceBouncingBalls_s = null;

		let params = PARAMS[this.id];

		this.lensFlare = APP.library.getSpriteFromAtlas("common/lensflare");
		this.lensFlare.scale.x = 0.795*0.127;
		this.lensFlare.scale.y = 0.795*0.127;
		this.lensFlare.blendMode = PIXI.BLEND_MODES.ADD;
		this.addChild(this.lensFlare);

		this.plasmaSmoke = this.addChild(new EternalPlazmaSmoke());		
		this.plasmaSmoke.scale.set(params.scale);

		this.position.set(params.x, params.y);
		this.rotation = params.rotation;

		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.alpha = 0;
			let sequence = [
							{
								tweens: [
									{prop: "alpha", to: 1}
								],
								duration: 13.6*16.6,
								onfinish: () => {
									this.showBouncingBalls();
									this.showLightnings();
									APP.profilingController.info.isVfxProfileValueMediumOrGreater && this.showSmoke();
								}
							}
						];
			var delay = this.id * 16.6 * 8;
			Sequence.start(this, sequence, delay);
		}
	}

	showLightnings()
	{
		this.lightning = this.addChild(new Lightning());
		this.lightning.alpha = 0;
		this.lightning.fadeTo(1, 16.6*4);
		this.lightning.scale.set(0.26);
	}

	showBouncingBalls()
	{
		this.bouncingBalls = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/bounce_ball"));
		this.bouncingBalls.scale.set(0);

		var delay = this.id * 16.6 * 8;

		this._startNextSequenceBouncingBalls(delay);
	}

	showSmoke()
	{
		this.smoke = this.addChild(new InstantKillSmokeLoop(0.36, 0.36));
	}

	_getSequenceBouncingBalls()
	{
		if (this._fSequenceBouncingBalls_s)
		{
			return this._fSequenceBouncingBalls_s;
		}

		let lSequenceBouncingBalls_s = [
			{
				tweens: [{prop: "scale.x", to: 0.5}, {prop: "scale.y", to: 0.5}, {prop: "rotation", to: BOUNCE_BALL_ROTATION }],

				duration: 8 * FRAME_RATE
			},
			{
				tweens:[ {prop: "scale.x", to: 0}, {prop: "scale.y", to: 0}, {prop: "rotation", to: BOUNCE_BALL_ROTATION * 2 }],
				duration: 8 * FRAME_RATE ,
				onfinish: () => {
					this._startNextSequenceBouncingBalls();
				}
			}
		];

		this._fSequenceBouncingBalls_s = lSequenceBouncingBalls_s;

		return lSequenceBouncingBalls_s;
	}

	_startNextSequenceBouncingBalls(delay = 0)
	{
		this.bouncingBalls.scale.set(0);
		this.bouncingBalls.rotation = 0;

		Sequence.start(this.bouncingBalls, this._getSequenceBouncingBalls(), delay);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this.bouncingBalls));

		this.id = undefined;
		this.lensFlare = null;
		this.plasmaSmoke = null;
		this.lightning = null;
		this.bouncingBalls = null;
		this._fSequenceBouncingBalls_s = null;

		this.smoke && this.smoke.destroy();
		this.smoke = null;

		super.destroy();
	}
}



export default InstantKillGunFlare;