import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE, WEAPONS } from '../../../../../../shared/src/CommonConstants';
import BurningRings from './BurningRings';

class EmblemRim extends Sprite {

	i_activate()
	{
		this._animate();
	}

	i_deactivate()
	{
		this._reset();
	}

	get flareRing()
	{
		return this._fFlareRing_sprt;
	}

	get burningRings()
	{
		return this._fBurningRings_brs;
	}

	get weaponId()
	{
		return this._fWeaponId_int;
	}

	constructor(aWeaponId_int)
	{
		super();

		this._fWeaponId_int = aWeaponId_int;

		this._fFlareRing_sprt = null;
		this._fBurningRings_brs = null;

		this._initView();
	}

	_initView()
	{
		this._fFlareRing_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/flare_ring"));
		this._fFlareRing_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		if (this.weaponId === WEAPONS.FLAMETHROWER)
		{
			this._initBurningRings();
		}
	}

	_initBurningRings()
	{
		this._fBurningRings_brs = this.addChild(new BurningRings());
	}

	_animate()
	{
		this._reset();
		this.visible = true;

		this._rotateFlareRing();

		this.flareRing.alpha = 1;

		this.alpha = 0;
		let fadeInSeq = [
			{
				tweens: [ { prop: "alpha", to: 1 }],
				duration: 14 * FRAME_RATE,
				onfinish: () => {
					this._fadeInOutFlareRing();
				}
			}
		];
		Sequence.start(this, fadeInSeq);

		this.burningRings && this.burningRings.i_animate();

	}

	_rotateFlareRing()
	{
		let rotationSeq = [
			{
				tweens: [ { prop: "rotation", to: this.flareRing.rotation + Utils.gradToRad(360) } ],
				duration: 100 * FRAME_RATE,
				onfinish: () => {
					this._rotateFlareRing();
				}
			}
		];
		Sequence.start(this.flareRing, rotationSeq);

	}

	_fadeInOutFlareRing()
	{
		let fadeInOutSeq = [
			{
				tweens: [],
				duration: 20 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.5 } ],
				duration: 18 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 1 } ],
				duration: 18 * FRAME_RATE,
				onfinish: () => {
					this._fadeInOutFlareRing();
				}
			},
		];
		Sequence.start(this.flareRing, fadeInOutSeq);
	}

	_reset()
	{
		this.visible = false;
		this._resetSequences();
		if (this.flareRing)
		{
			this.flareRing.rotation = 0;
			this.flareRing.alpha = 1;
		}
	}

	_resetSequences() {
		Sequence.destroy(Sequence.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this.flareRing));

		this.burningRings && this.burningRings.i_reset();
	}

	destroy()
	{
		this._resetSequences();
		super.destroy();
	}


}

export default EmblemRim;