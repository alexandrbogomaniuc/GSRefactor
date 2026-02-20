import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class BurningRings extends Sprite {

	i_animate()
	{
		this._animate();
	}

	i_reset()
	{
		this._reset();
	}

	constructor()
	{
		super();

		this._fInnerRings_sprt_arr = [];
		this._fOuterRings_sprt_arr = [];
		this._initView();
	}

	_initView()
	{
		this._fInnerRings_sprt_arr.push(this._createBurningRing(0.535 * 2 * 0.53));
		this._fInnerRings_sprt_arr.push(this._createBurningRing(0.535 * 2 * 0.53));

		this._fOuterRings_sprt_arr.push(this._createBurningRing(0.575 * 2 * 0.53));
		this._fOuterRings_sprt_arr.push(this._createBurningRing(0.575 * 2 * 0.53));
	}

	_createBurningRing(aScale_num)
	{
		let lBurningRing_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/burning_ring"));
		lBurningRing_sprt.anchor.set(191/392, 161/342);
		lBurningRing_sprt.scale.set(aScale_num)
		lBurningRing_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		return lBurningRing_sprt;
	}

	_rotateInnerBurningRing(aRing_sprt)
	{
		let rotationSeq = [
			{
				tweens: [ { prop: "rotation", to: aRing_sprt.rotation - Utils.gradToRad(24 / 2) } ],
				duration: 12 * FRAME_RATE,
				onfinish: () => {
					this._rotateInnerBurningRing(aRing_sprt);
				}
			}
		];
		Sequence.start(aRing_sprt, rotationSeq);
	}

	_rotateOuterBurningRing(aRing_sprt)
	{
		let rotationSeq = [
			{
				tweens: [ { prop: "rotation", to: aRing_sprt.rotation + Utils.gradToRad(24 * 2) } ],
				duration: 12 * FRAME_RATE,
				onfinish: () => {
					this._rotateOuterBurningRing(aRing_sprt);
				}
			}
		];
		Sequence.start(aRing_sprt, rotationSeq);
	}

	_fadeInOutRing(aRing_sprt)
	{
		let fadeInOutSeq = [
			{
				tweens: [],
				duration: 5 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 1 } ],
				duration: 4 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.5 } ],
				duration: 4 * FRAME_RATE,
			},
			{
				tweens: [],
				duration: 5 * FRAME_RATE,
				onfinish: () => {
					this._fadeInOutRing(aRing_sprt);
				}
			}
		];
		Sequence.start(aRing_sprt, fadeInOutSeq);
	}

	_animate()
	{
		this._reset();

		this._rotateInnerBurningRing(this._fInnerRings_sprt_arr[0]);
		this._rotateOuterBurningRing(this._fOuterRings_sprt_arr[0]);

		this._rotateInnerBurningRing(this._fInnerRings_sprt_arr[1]);
		this._rotateOuterBurningRing(this._fOuterRings_sprt_arr[1]);

		this._fadeInOutRing(this._fInnerRings_sprt_arr[0]);
		this._fadeInOutRing(this._fOuterRings_sprt_arr[0]);
	}

	_reset()
	{
		this._resetSequences();

		this._fInnerRings_sprt_arr[0].alpha = 0.5;
		this._fOuterRings_sprt_arr[0].alpha = 0.5;

		this._fInnerRings_sprt_arr[1].alpha = 0.5;
		this._fOuterRings_sprt_arr[1].alpha = 0.5;

		this._fInnerRings_sprt_arr[1].rotation = Math.random() * Math.PI;
		this._fOuterRings_sprt_arr[1].rotation = Math.random() * Math.PI;
	}

	_resetSequences()
	{
		Sequence.destroy(Sequence.findByTarget(this._fInnerRings_sprt_arr[0]));
		Sequence.destroy(Sequence.findByTarget(this._fInnerRings_sprt_arr[1]));

		Sequence.destroy(Sequence.findByTarget(this._fOuterRings_sprt_arr[0]));
		Sequence.destroy(Sequence.findByTarget(this._fOuterRings_sprt_arr[1]));
	}

	destroy()
	{
		this._resetSequences();
		super.destroy();
	}

}

export default BurningRings;