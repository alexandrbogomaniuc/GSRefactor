import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MissEffect from '../../../../main/missEffects/MissEffect';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import EternalCryogunSmoke from '../../../../main/animation/cryogun/EternalCryogunSmoke';

const Z_INDEXES = {
	BURST_SMOKES: 			100 - 5,
	PLASMA_ENERGY_MARK:		100 - 8,
	CENTER_PLASMA_SMOKE: 	100 - 12,
	INDIGO_SMOKE: 			100 - 13,
	BLUE_GLOW: 				100 - 21,
	BLUE_FLARE: 			100 - 22,
	TRANSITION_SMOKE: 		100 - 23
}

class CryogunsEffectView extends Sprite {

	static get EVENT_ON_ANIMATION_COMPLETED() 	{ return 'EVENT_ON_ANIMATION_COMPLETED'};

	constructor()
	{
		super();

		this._fDelayTimers_tmr_arr = [];
		this._fSequences_seq_arr = [];

		this._startEffect();
	}

	_startEffect()
	{
		this._addBurstSmokes();
		//this._addTimer(this._addPlasmaEnergyMark.bind(this), 4);
		this._addTimer(this._addWave.bind(this), 5);

		if(APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			this._addTimer(this._addIndigoSmoke.bind(this), 3);
			this._addTimer(this._addBlueGlow.bind(this), 3);
			this._addTimer(this._addBlueFlare.bind(this), 11);
			this._addTimer(this._addTransitionSmoke.bind(this), 5);
			this._addTimer(this._addCenterPlasmaSmoke.bind(this), 2);
		}
	}

	_addTimer(aFunctionCallback_func, aDelayedFrames_int)
	{
		let lTimer_tmr = new Timer(aFunctionCallback_func, aDelayedFrames_int*2*16.7);
		this._fDelayTimers_tmr_arr.push(lTimer_tmr);
	}

	_addBurstSmokes()
	{
		let lBurstSmokesBase_sprt = this.addChild(new Sprite);
		lBurstSmokesBase_sprt.zIndex = Z_INDEXES.BURST_SMOKES;

		for (let i=0; i<1; i++)
		{
			let lSmokeBurst_sprt = new Sprite;
			lSmokeBurst_sprt.textures = MissEffect.getSmokeTextures();
			lSmokeBurst_sprt.anchor.set(0.5, 0.7);
			lSmokeBurst_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
			lSmokeBurst_sprt.scale.set(4.34);
			lSmokeBurst_sprt.once('animationend', (e) => {
				lSmokeBurst_sprt.destroy();
				if (i == 0)
				{
					this._onAnimationCompleted();
				}
			})
			lSmokeBurst_sprt.play();
			lBurstSmokesBase_sprt.addChild(lSmokeBurst_sprt);
		}
	}

	_addPlasmaEnergyMark()
	{
		let lPlasmaEnergyMark_sprt = APP.library.getSprite('weapons/Cryogun/plasmaenergymark_blue');
		lPlasmaEnergyMark_sprt.anchor.set(108/292, 163/260);
		lPlasmaEnergyMark_sprt.scale.set(2*1.18);
		lPlasmaEnergyMark_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		lPlasmaEnergyMark_sprt.scaleTo(2*1.58, 9*2*16.7);
		lPlasmaEnergyMark_sprt.fadeTo(0, 26*2*16.7, null, () => {
			lPlasmaEnergyMark_sprt.destroy();
		});

		this.addChild(lPlasmaEnergyMark_sprt);
		lPlasmaEnergyMark_sprt.zIndex = Z_INDEXES.PLASMA_ENERGY_MARK;
	}

	_addCenterPlasmaSmoke()
	{
		let lCenterPlasmaSmoke_sprt = APP.library.getSprite('weapons/Cryogun/center_plasmasmoke');
		this.addChild(lCenterPlasmaSmoke_sprt)
		lCenterPlasmaSmoke_sprt.zIndex = Z_INDEXES.CENTER_PLASMA_SMOKE;
		lCenterPlasmaSmoke_sprt.anchor.set(196/373, 81/180);

		lCenterPlasmaSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lCenterPlasmaSmoke_sprt.scale.set(2);
		lCenterPlasmaSmoke_sprt.alpha = 0;
		lCenterPlasmaSmoke_sprt.scaleTo(2*0.835, 37*2*16.7);

		let sequence = [
			{
				//157
				tweens: [],
				duration: 2*2*16.7
			},
			{
				//159
				tweens: [
					{ prop: 'alpha', to: 1 }
				],
				duration: 11*2*16.7
			},
			{
				//170
				tweens: [],
				duration: 4*2*16.7
			},
			{
				//174
				tweens: [
					{ prop: 'alpha', to: 0 }
				],
				duration: 10*2*16.7,
				onfinish: () => {
					lCenterPlasmaSmoke_sprt.destroy();
				}
			}
		];
		Sequence.start(lCenterPlasmaSmoke_sprt, sequence);
	}

	_addIndigoSmoke()
	{
		let lIndigoSmoke_eps = this.addChild(new EternalCryogunSmoke(true, 'screen'));
		lIndigoSmoke_eps.zIndex = Z_INDEXES.INDIGO_SMOKE;
		lIndigoSmoke_eps.blendMode = PIXI.BLEND_MODES.SCREEN;
		lIndigoSmoke_eps.scale.set(0);

		let lScaleSequence_seq = [
			{
				tweens: [
					{ prop: 'scale.x', to: 2*2.837 },
					{ prop: 'scale.y', to: 2*1.317 }
				],
				duration: 10*2*16.7
			},
			{
				tweens: [
					{ prop: 'scale.x', to: 2*3.214 },
					{ prop: 'scale.y', to: 2*1.884}
				],
				duration: 17*2*16.7
			}
		];
		Sequence.start(lIndigoSmoke_eps, lScaleSequence_seq);

		let lAlphaSequence_seq = [
			{
				tweens: [],
				duration: 15*2*16.7
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0 }
				],
				duration: 12*2*16.7,
				onfinish: () => {
					lIndigoSmoke_eps.destroy();
				}
			}
		];
		Sequence.start(lIndigoSmoke_eps, lAlphaSequence_seq);

		this._fSequences_seq_arr.push(...Sequence.findByTarget(lIndigoSmoke_eps));

	}

	_addBlueGlow()
	{
		let lBlueGlow_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/glow"));
		lBlueGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lBlueGlow_sprt.alpha = 0;
		lBlueGlow_sprt.scale.set(0.6, 1);
		lBlueGlow_sprt.zIndex = Z_INDEXES.BLUE_GLOW;

		let lAlphaSequence_seq = [
			{
				tweens: [
					{ prop: 'alpha', to: 1}
				],
				duration: 3*2*16.7
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0}
				],
				duration: 8*2*16.7,
				onfinish: () => {
					lBlueGlow_sprt.destroy();
				}
			}
		];

		Sequence.start(lBlueGlow_sprt, lAlphaSequence_seq);

		this._fSequences_seq_arr.push(...Sequence.findByTarget(lBlueGlow_sprt));
	}

	_addBlueFlare()
	{
		let lBlueFlare_sprt = this.addChild(APP.library.getSpriteFromAtlas('common/blue_flare'));
		lBlueFlare_sprt.anchor.set(213/437, 135/271);
		lBlueFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lBlueFlare_sprt.zIndex = Z_INDEXES.BLUE_FLARE;
		lBlueFlare_sprt.scale.set(4*1.6);

		let lScaleSequence_seq = [
			{
				tweens: [
					{ prop: 'scale.x', to: 4*0.54 },
					{ prop: 'scale.y', to: 4*0.54 }
				],
				duration: 3*2*16.7
			},
			{
				tweens: [
					{ prop: 'scale.x', to: 0},
					{ prop: 'scale.y', to: 0}
				],
				duration: 11*2*16.7,
				onfinish: () => {
					lBlueFlare_sprt.destroy();
				}
			}
		];

		Sequence.start(lBlueFlare_sprt, lScaleSequence_seq);

		this._fSequences_seq_arr.push(...Sequence.findByTarget(lBlueFlare_sprt));
	}

	_addTransitionSmoke()
	{
		let lTransitionSmoke_sprt = this.addChild(APP.library.getSpriteFromAtlas('common/transition_smoke_fx_unmult'));
		lTransitionSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lTransitionSmoke_sprt.zIndex = Z_INDEXES.TRANSITION_SMOKE;
		lTransitionSmoke_sprt.scale.set(4*0.198);
		lTransitionSmoke_sprt.rotation = Utils.gradToRad(-57.7);
		lTransitionSmoke_sprt.alpha = 0.55;

		let lScaleSequence_seq = [
			{
				tweens: [
					{ prop: 'scale.x', to: 4*0.34 },
					{ prop: 'scale.y', to: 4*0.34},
					{ prop: 'rotation', to: Utils.gradToRad(-70.1) }
				],
				duration: 13*2*16.7
			}
		]
		Sequence.start(lTransitionSmoke_sprt, lScaleSequence_seq);

		let lAlphaSequence_seq = [
			{
				tweens: [
					{ prop: 'alpha', to: 1}
				],
				duration: 4*2*16.7
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0 }
				],
				duration: 23*2*16.7,
				onfinish: () => {
					lTransitionSmoke_sprt.destroy();
				}
			}
		]

		Sequence.start(lTransitionSmoke_sprt, lAlphaSequence_seq);

		this._fSequences_seq_arr.push(...Sequence.findByTarget(lTransitionSmoke_sprt));

	}

	_addWave()
	{
		if (!APP.isPixiHeavenLibrarySupported) return;

		let lBigFrost_sprt = this.addChild(APP.library.getSprite('weapons/Cryogun/Freeze'));
		lBigFrost_sprt.scale.set(1.96);
		lBigFrost_sprt.rotation = Math.PI/2;

		let lWhiteCircle_sprt = this._createWhiteCircle( PIXI.BLEND_MODES.ADD );
		let lWhiteCircleMask_sprt = this._createWhiteCircle( null, () => { lBigFrost_sprt.destroy(); });

		lBigFrost_sprt.mask = lWhiteCircleMask_sprt;
	}

	_createWhiteCircle(aBlendMode_str = null, aDestroyCallback_func = null)
	{
		let lWhiteCircle_sprt = this.addChild(APP.library.getSprite('weapons/Cryogun/white_circle'));

		if (aBlendMode_str != null)
		{
			lWhiteCircle_sprt.blendMode = aBlendMode_str;
		}

		lWhiteCircle_sprt.scale.set(0);

		let sequence = [
			{
				tweens: [
					{ prop: 'scale.x', to: 2*0.48 },
					{ prop: 'scale.y', to: 2*0.48 }
				],
				duration: 2*2*16.7,
				ease: Easing.sine.easeIn
			},
			{
				tweens: [
					{ prop: 'scale.x', to: 2 },
					{ prop: 'scale.y', to: 2 },
					{ prop: 'alpha', to: 0}
				],
				duration: 9*2*16.7,
				ease: Easing.sine.easeOut,
				onfinish: () => {
					if (aDestroyCallback_func)
					{
						aDestroyCallback_func();
					}
					lWhiteCircle_sprt.destroy();
				}
			}
		];

		Sequence.start(lWhiteCircle_sprt, sequence);
		this._fSequences_seq_arr.push(...Sequence.findByTarget(lWhiteCircle_sprt));

		return lWhiteCircle_sprt;
	}

	_onAnimationCompleted()
	{
		this.emit(CryogunsEffectView.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		this.removeAllListeners();
		while (this._fDelayTimers_tmr_arr && this._fDelayTimers_tmr_arr.length > 0)
		{
			let lTimer_tmr = this._fDelayTimers_tmr_arr.pop();
			lTimer_tmr && lTimer_tmr.destructor();
		}
		Sequence.destroy(this._fSequences_seq_arr);
		this._fSequences_seq_arr = [];
		super.destroy();
	}
}

export default CryogunsEffectView