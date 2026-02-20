import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Sequence from '../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as Easing from '../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import PathTween from '../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import * as FEATURES from '../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

class AmmoCounter extends Sprite {

	constructor() {
		super();

		this.name = 'AmmoCounter';

		this._fAmmoLeftText_tf = null;
		this._fBase_sprt = null;

		this._createView();
	}

	i_setShots(aShots_int)
	{
		this._fAmmoLeftText_tf.text = aShots_int;
	}

	destroy()
	{
		PathTween.destroy(PathTween.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this));

		this._fAmmoLeftText_tf = null;
		this._fBase_sprt = null;

		super.destroy();
	}

	get _animationSpeed()
	{
		return 1.14;
	}

	i_updateShots(aNewShots_int, aCallback_func)
	{
		let seqGlowDown =  [{
				tweens: [{prop:"alpha", to:0}],
				duration: 200 * this._animationSpeed,
				ease: Easing.quadratic.easeInOut,
				onfinish: () => {
					if (aNewShots_int <= 0)
					{
						let seqHide =  [{
								tweens: [{prop:"scale.x", to:0},{prop:"scale.y", to:0}, {prop:"rotation", to:Math.PI}],
								duration: 200 * this._animationSpeed,
								ease: Easing.quadratic.easeIn,
								onfinish: () => {
									this.destroy();
									if (aCallback_func)
									{
										aCallback_func();
									}
								}
						}];

						Sequence.start(this, seqHide);
					}
				}
		}];

		let seqGlowUp =  [{
				tweens: [{prop:"alpha", to:0.8}],
				duration: 100 * this._animationSpeed,
				ease: Easing.quadratic.easeOut,
				onfinish: () => {
					this.i_setShots(aNewShots_int);
				}
		}];
	}

	i_startFlying(aStartPos_pt, aEndPos_pt, aCallback_func)
	{
		this.position.set(aStartPos_pt.x, aStartPos_pt.y);

		this.rotation = -Math.PI;
		this.skew.x = -0.2;
		this.scale.set(0);

		let formSeq = [
			{
				tweens: [
					{prop:"scale.x", to:1.1, duration: 280 * this._animationSpeed, ease: Easing.quadratic.easeIn},
					{prop:"scale.y", to:1.1, duration: 280 * this._animationSpeed, ease: Easing.quadratic.easeIn},
					{prop:"rotation", to:0, duration: 280 * this._animationSpeed, ease: Easing.quadratic.easeIn},
				],
				duration: 300 * this._animationSpeed,
			},
			{
				tweens: [
					{prop:"scale.x", to:1},
					{prop:"scale.y", to:1},
					{prop:"skew.x", to:0}
				],
				duration: 300 * this._animationSpeed,
				ease: Easing.quadratic.easeIn
			},
			{
				tweens: [
					{prop:"scale.x", to:1.1},
					{prop:"scale.y", to:1.1},
					{prop:"rotation", to:-0.1}
				],
				duration: 480 * this._animationSpeed,
				ease: Easing.quadratic.easeIn
			}
		];
		Sequence.start(this, formSeq);

		let point = this.position;

		let path = [{x: point.x, y: point.y}, {x: point.x + 10, y: point.y - 25}, {x: point.x + 60, y: point.y - 80}];
		let pathTween = new PathTween(this, path, true);

		pathTween.start(300 * this._animationSpeed, Easing.linear.easeOut, () => {
			this.rotation = 0;

			path = [{x: point.x, y: point.y}, {x: point.x - 20, y: point.y - 25}, {x: point.x - 40, y: point.y}];
			let pathTween = new PathTween(this, path, true);
			pathTween.start(300 * this._animationSpeed, Easing.linear.easeIn, () => {
				this.scale.set(1);
				this.skew.x = 0;

				path = [{x: point.x, y: point.y}, {x: point.x + 10, y: point.y - 8}, {x: point.x + 20, y: point.y - 20}];
				let pathTween = new PathTween(this, path, true);
				pathTween.start(480 * this._animationSpeed, Easing.cubic.easeIn, () => {
					path = [{x: point.x, y: point.y}, {x: point.x + 5, y: point.y - 10}, {x: point.x, y: point.y - 20}];
					let pathTween = new PathTween(this, path, true);
					pathTween.start(100 * this._animationSpeed, Easing.linear.easeOut, () => {

					});
				});
			});
		});
	}

	i_startFinalMove(aDuration_num, aFinalPos_pt, aEasing_obj)
	{
		PathTween.destroy(PathTween.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this));

		let finalSeq = [
			{
				tweens: [
					{prop:"scale.x", to:0},
					{prop:"scale.y", to:0},
					{prop:"rotation", to: Math.PI}
				],
				duration: aDuration_num,
				ease: Easing.quadratic.easeIn
			}
		];
		Sequence.start(this, finalSeq);

		let path = [{x: this.position.x, y: this.position.y}, {x: aFinalPos_pt.x, y: aFinalPos_pt.y}]
		let pathTween = new PathTween(this, path, true);
		pathTween.start(aDuration_num, aEasing_obj, () => {
			this.scale.set(0);
			this.rotation = 0;
			this.destroy();
		})
	}

	_createView() {

		this._fBase_sprt = this.addChild(APP.library.getSprite("weapons/ammo_left_back"));

		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 34,
			align: "center",
			letterSpacing: 0.5,
			padding: 5,
			fill: 0x000000
		};

		let lAmmoLeftText_tf = new TextField(lStyle_obj);
		lAmmoLeftText_tf.maxWidth = 29;
		lAmmoLeftText_tf.anchor.set(0.5, 0.5);
		lAmmoLeftText_tf.position.set(0.5, 0.5);

		if (FEATURES.IE)
		{
			lAmmoLeftText_tf.position.set(0, 0);
			lAmmoLeftText_tf.pivot.set(1, -3);
		}

		this.addChild(lAmmoLeftText_tf);

		this._fAmmoLeftText_tf = lAmmoLeftText_tf;
	}
}

export default AmmoCounter;