import WeaponBeam from "../WeaponBeam";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import EternalCryogunSmoke from '../cryogun/EternalCryogunSmoke';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const ETERNAL_SMOKE_PARAMS = [	{startFrame: 1, x: 30},
								{startFrame: 2, x: 50},
								{startFrame: 3, x: 70},
								{startFrame: 4, x: 90}
							];

class CryogunBeam extends WeaponBeam
{
	static get EVENT_ON_TARGET_ACHIEVED() 		{ return WeaponBeam.EVENT_ON_TARGET_ACHIEVED }
	static get EVENT_ON_ANIMATION_COMPLETED() 	{ return WeaponBeam.EVENT_ON_ANIMATION_COMPLETED }

	constructor(aShotData_obj, callback)
	{
		super(aShotData_obj);

		this._fShotData_obj = aShotData_obj;
		this._fTargetAchievingCallback_func = callback;
		this._fScaleableBase_sprt = null;

		this._fPlasmaBeam_sprt = null;
		this._fBeamDimmer_sprt = null;
		this._fPlazmaSmokes_eps_arr = [];

		this._fParticlesBase_sprt = null;
		this._fParticlesPieces_sprt_arr = [];

		this._fScaleableBase_sprt = this.addChild(new Sprite);
	}

	get _baseBeamLength()
	{
		return 450;
	}

	get _minimumBeamLength()
	{
		return 150;
	}

	//override
	__shoot(aStartPoint_pt, aEndPoint_pt)
	{
		super.__shoot(aStartPoint_pt, aEndPoint_pt);

		if(APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			let lBeamDimmer_sprt = APP.library.getSprite('weapons/Cryogun/beam_dimmer');
			lBeamDimmer_sprt.anchor.set(98/708, 112/206);
			lBeamDimmer_sprt.scale.set(2);
			this._fBeamDimmer_sprt = this._fScaleableBase_sprt.addChild(lBeamDimmer_sprt);
			let lDimmerAlphaSequence_seq = [
				{
					tweens: [
						{ prop: 'alpha', to: 0.8}
					],
					duration: 4*2*16.7
				},
				{
					tweens: [],
					duration: 6*2*16.7
				},
				{
					tweens: [
						{ prop: 'alpha', to: 0}
					],
					duration: 11*2*16.7
				}
			];
			Sequence.start(this._fBeamDimmer_sprt, lDimmerAlphaSequence_seq);
		}

		let lPlasmaBeam_sprt = APP.library.getSprite('weapons/Cryogun/plasmabeam');
		lPlasmaBeam_sprt.anchor.set(98/708, 112/206);
		lPlasmaBeam_sprt.scale.set(2);
		lPlasmaBeam_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fPlasmaBeam_sprt = this._fScaleableBase_sprt.addChild(lPlasmaBeam_sprt);

		this._fPlasmaBeam_sprt.scale.set(2 * 0, 2 * 0.19);
		this._fPlasmaBeam_sprt.alpha = 1;

		let lBeamScaleSequence_seq = [
			{
				tweens: [
					{prop: 'scale.x', to: 2 * 0.56},
					{prop: 'scale.y', to: 2 * 0.29}
				],
				duration: 3*2*16.7
			},
			{
				tweens: [
					{prop: 'scale.x', to: 2 * 0.96},
					{prop: 'scale.y', to: 2 * 0.59}
				],
				duration: 9*2*16.7,
				onfinish: () => {
					this._fTargetAchievingCallback_func && this._fTargetAchievingCallback_func.call(null, null, 0, this._fShotData_obj);
				}
			}
		];

		let lBeamAlphaSequence_seq = [
			{
				tweens: [],
				duration: 10*2*16.7
			},
			{
				tweens: [
					{prop: 'alpha', to: 0},
				],
				duration: 7*2*16.7,
				onfinish: () => {
					this._onAnimationCompleted();
				}
			}
		];

		let lBeamAchievementSequence_seq = [
			{
				tweens: [],
				duration: 3*2*16.7,
				onfinish: () => {
					this.emit(CryogunBeam.EVENT_ON_TARGET_ACHIEVED, {x: this._fEndPoint_pt.x, y: this._fEndPoint_pt.y});
				}
			}
		]

		Sequence.start(this._fPlasmaBeam_sprt, lBeamScaleSequence_seq);
		Sequence.start(this._fPlasmaBeam_sprt, lBeamAlphaSequence_seq);
		Sequence.start(this._fPlasmaBeam_sprt, lBeamAchievementSequence_seq);

		if(APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			//eternal smokes...
			for (let lParams_obj of ETERNAL_SMOKE_PARAMS)
			{
				let seq = [
					{
						tweens: [],
						duration: lParams_obj.startFrame*2*16.7,
						onfinish: () => {
							this._addAnotherPlasmaSmoke(lParams_obj);
						}
					}
				];
				Sequence.start(this, seq);
			}
			//...eternal smokes

			this._createParticles();
		}

		//DEBUG...
		// let gr = new PIXI.Graphics();
		// gr.beginFill(0x00ff00);
		// gr.drawCircle(0, 0, 10);
		// this.addChild(gr);
		//...DEBUG
	}

	_addAnotherPlasmaSmoke(aParams_obj)
	{
		let lSmoke_eps = new EternalCryogunSmoke();
		lSmoke_eps.scale.set(2*1.41, 2*0.11);
		lSmoke_eps.x = aParams_obj.x;
		this._fScaleableBase_sprt.addChild(lSmoke_eps);

		let sequence = [
			{
				tweens: [],
				duration: 10*2*16.7
			},
			{
				tweens: [
					{prop: 'alpha', to: '0'}
				],
				duration: 2*2*16.7
			}
		]
		Sequence.start(lSmoke_eps, sequence);
		this._fPlazmaSmokes_eps_arr.push(lSmoke_eps);
	}

	_createParticles()
	{		
		/*let lParticlesMask_gr = new PIXI.Graphics();
		lParticlesMask_gr.beginFill(0xff0000);
		lParticlesMask_gr.drawRect(0, -50, this._beamLength, 100);
		lParticlesMask_gr.endFill();*/

		this._fParticlesBase_sprt = this.addChild(new Sprite);
		/*this._fParticlesBase_sprt.addChild(lParticlesMask_gr);
		this._fParticlesBase_sprt.mask = lParticlesMask_gr;*/

		this._onNextParticlesIteration();
	}

	_onNextParticlesIteration()
	{
		this._addAnotherPieceOfParticles();
		let lFinishCallback_func = this._onNextParticlesIteration.bind(this);
		for (let i=0; i<this._fParticlesPieces_sprt_arr.length; i++)
		{
			let lParticlesPiece_sprt = this._fParticlesPieces_sprt_arr[i];
			if (lParticlesPiece_sprt.x >= (this._beamLength - 50))
			{
				this._fParticlesPieces_sprt_arr.splice(i, 1);
				lParticlesPiece_sprt.destroy();
				continue;
			}
			let lFinalX_num = lParticlesPiece_sprt.x + 100;
			lParticlesPiece_sprt.moveTo(lFinalX_num, lParticlesPiece_sprt.y, 3*2*16.7, null, lFinishCallback_func);
			lFinishCallback_func = null;
		}
	}

	_addAnotherPieceOfParticles()
	{
		let lParticlesPiece_sprt = APP.library.getSprite('weapons/Cryogun/particles');
		lParticlesPiece_sprt.scale.set(2);
		lParticlesPiece_sprt.anchor.set(0, 0.5);
		lParticlesPiece_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		lParticlesPiece_sprt.alpha = 0;
		lParticlesPiece_sprt.fadeTo(1, 3*2*16.7, null, () => {
			lParticlesPiece_sprt.fadeTo(0, 3*2*16.7);
		});

		this._fParticlesBase_sprt.addChild(lParticlesPiece_sprt);
		this._fParticlesPieces_sprt_arr.push(lParticlesPiece_sprt);
	}

	_onAnimationCompleted()
	{
		this.emit(WeaponBeam.EVENT_ON_ANIMATION_COMPLETED);
		this.destroy();
	}

	//override
	_updateScale()
	{
		this._fScaleableBase_sprt.scale.x = this._beamLength / this._baseBeamLength;
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fPlasmaBeam_sprt));
		Sequence.destroy(Sequence.findByTarget(this._fBeamDimmer_sprt));
		Sequence.destroy(Sequence.findByTarget(this));

		this._fPlasmaBeam_sprt && this._fPlasmaBeam_sprt.destroy();
		this._fPlasmaBeam_sprt = null;

		this._fBeamDimmer_sprt && this._fBeamDimmer_sprt.destroy();
		this._fBeamDimmer_sprt = null;

		this._fParticlesPieces_sprt_arr = [];
		this._fParticlesBase_sprt && this._fParticlesBase_sprt.destroy();

		this._fTargetAchievingCallback_func = null;

		while (this._fPlazmaSmokes_eps_arr && this._fPlazmaSmokes_eps_arr.length > 0)
		{
			let lSmoke_eps = this._fPlazmaSmokes_eps_arr.pop();
			Sequence.destroy(Sequence.findByTarget(lSmoke_eps));
		}
		this._fPlazmaSmokes_eps_arr = null;
		this._fShotData_obj = null;

		super.destroy();
	}

}

export default CryogunBeam;