import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import TorchFxAnimation from '../../TorchFxAnimation';
import CommonEffectsManager from '../../../CommonEffectsManager';

const FX_BASE_POSITION = new PIXI.Point(0, 40);
const TRANSITION_SMOKES_DESCR = [
	{// TransitionSmoke layer 40
		position: new PIXI.Point(FX_BASE_POSITION.x+11, FX_BASE_POSITION.y-16.5),
		moveDelta: {position: new PIXI.Point(0, -35), duration: 12*2*16.7},
		scale: new PIXI.Point(4*0.21, 4*0.21),
		tint: 0x111111,
		alpha: 0,
		rotation: Utils.gradToRad(-92),
		blendMode: PIXI.BLEND_MODES.MULTIPLY,
		delay: 5*2*16.7,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 4*0.4 }, { prop: 'scale.y', to: 4*0.4 } ], duration: 30*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(-132) } ], duration: 30*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'alpha', to: 0.3*3 } ], duration: 13*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 17*2*16.7 }
						]
					]
	},
	{// TransitionSmoke layer 23
		position: new PIXI.Point(FX_BASE_POSITION.x+9, FX_BASE_POSITION.y-66),
		moveDelta: {position: new PIXI.Point(-1, -35), duration: 12*2*16.7},
		scale: new PIXI.Point(4*0.22, 4*0.22),
		tint: 0x111111,
		alpha: 0.02,
		rotation: Utils.gradToRad(-86),
		blendMode: PIXI.BLEND_MODES.MULTIPLY,
		delay: 0,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 4*0.4 }, { prop: 'scale.y', to: 4*0.4 } ], duration: 11*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(-102) } ], duration: 12*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'alpha', to: 0.1*3 } ], duration: 5*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 7*2*16.7 }
						]
					]
	},
	{// TransitionSmoke layer 22
		position: new PIXI.Point(FX_BASE_POSITION.x+9, FX_BASE_POSITION.y-13),
		moveDelta: {position: new PIXI.Point(-1.5, -48), duration: 17*2*16.7},
		scale: new PIXI.Point(4*0.15, 4*0.15),
		tint: 0x111111,
		alpha: 0,
		rotation: Utils.gradToRad(-92),
		blendMode: PIXI.BLEND_MODES.MULTIPLY,
		delay: 5*2*16.7,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 4*0.4 }, { prop: 'scale.y', to: 4*0.4 } ], duration: 16*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(-114) } ], duration: 17*2*16.7 }
						],
						[
							{ tweens: [], duration: 4*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0.1*3 } ], duration: 5*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 8*2*16.7 }
						]
					]
	},
	{// TransitionSmoke layer 21
		position: new PIXI.Point(FX_BASE_POSITION.x+9, FX_BASE_POSITION.y-23),
		moveDelta: {position: new PIXI.Point(-1.5, -48), duration: 17*2*16.7},
		scale: new PIXI.Point(4*0.15, 4*0.15),
		tint: 0x111111,
		alpha: 0,
		rotation: Utils.gradToRad(-101),
		blendMode: PIXI.BLEND_MODES.MULTIPLY,
		delay: 11*2*16.7,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 4*0.4 }, { prop: 'scale.y', to: 4*0.4 } ], duration: 16*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(-123) } ], duration: 17*2*16.7 }
						],
						[
							{ tweens: [], duration: 4*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0.1*3 } ], duration: 5*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 8*2*16.7 }
						]
					]
	},
	{// TransitionSmoke layer 20
		position: new PIXI.Point(FX_BASE_POSITION.x+9, FX_BASE_POSITION.y-13),
		scale: new PIXI.Point(4*0.475, 4*0.475),
		alpha: 0.18,
		rotation: Utils.gradToRad(-86),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 0,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 4*0.6 }, { prop: 'scale.y', to: 4*0.6 } ], duration: 5*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(-92) } ], duration: 5*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'alpha', to: 1 } ], duration: 5*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 8*2*16.7 }
						]
					]
	}
];

class FlameThrowerHitFXs extends Sprite 
{
	static get EVENT_ON_ANIMATION_COMPLETED()		{ return 'EVENT_ON_ANIMATION_COMPLETED'; }

	startAnimation()
	{
		this._startAnimation();
	}

	constructor()
	{
		super();

		this._fTransSmokeSequences_arr = null;
		this._fTorchesSequences_arr = null;
		this._fDieSmokesAmount_num = 0;

		TorchFxAnimation.initTextures();
	}

	_startAnimation()
	{
		this._fTransSmokeSequences_arr = [];
		this._fTorchesSequences_arr = [];
		this._fDieSmokesAmount_num = 0;

		this._addBlackSmoke(new PIXI.Point(FX_BASE_POSITION.x+10, FX_BASE_POSITION.y+26), new PIXI.Point(2, 1.34), 1); // layer 115
		
		this._addDieSmoke(new PIXI.Point(FX_BASE_POSITION.x+12, FX_BASE_POSITION.y-10), new PIXI.Point(1.58, 1.58), PIXI.BLEND_MODES.SCREEN, -14); //layer 112
		this._addDieSmoke(new PIXI.Point(FX_BASE_POSITION.x+12, FX_BASE_POSITION.y-10), new PIXI.Point(1.58, 1.58), PIXI.BLEND_MODES.SCREEN, -6); //layer 111
		this._addDieSmoke(new PIXI.Point(FX_BASE_POSITION.x+12, FX_BASE_POSITION.y-10), new PIXI.Point(1.38, 1.38), PIXI.BLEND_MODES.SCREEN, 1); //layer 110

		this._addTransitionSmoke(TRANSITION_SMOKES_DESCR[0]); // layer 40
		
		this._addBlackSmoke(new PIXI.Point(FX_BASE_POSITION.x+16, FX_BASE_POSITION.y-35), new PIXI.Point(0.72, 0.5), 11); // layer 34

		this._addTorch(new PIXI.Point(FX_BASE_POSITION.x+9.5, FX_BASE_POSITION.y-20), 0); // MQ_TorchLoop layer 33
		this._addTorch(new PIXI.Point(FX_BASE_POSITION.x+9.5, FX_BASE_POSITION.y-20), 50); // MQ_TorchLoop layer 32
		this._addTorch(new PIXI.Point(FX_BASE_POSITION.x+9.5, FX_BASE_POSITION.y-20), -57); // MQ_TorchLoop layer 31

		this._addBlackSmoke(new PIXI.Point(FX_BASE_POSITION.x+3.5, FX_BASE_POSITION.y-35), new PIXI.Point(1.12, 1.34), 1); // layer 25
		
		for (let i=1; i<TRANSITION_SMOKES_DESCR.length; i++)
		{
			this._addTransitionSmoke(TRANSITION_SMOKES_DESCR[i]); // layers 23-20
		}

		//DEBUG...
		// let gr = this.addChild(new PIXI.Graphics());
		// gr.beginFill(0x00ffff);
		// gr.drawRect(FX_BASE_POSITION.x, FX_BASE_POSITION.y, 100, 5);
		// gr.endFill();
		//...DEBUG
	}

	_addBlackSmoke(position, scaleMultiplier, startFrame=0, totalFrames=undefined)
	{
		let lSmoke_sprt = this._addDieSmoke(position, scaleMultiplier, PIXI.BLEND_MODES.MULTIPLY, startFrame, totalFrames);
		lSmoke_sprt.tint = 0x111111;
		lSmoke_sprt.alpha = 0.7;
		lSmoke_sprt.rotation = Utils.gradToRad(-4);

		return lSmoke_sprt;
	}

	_addDieSmoke(position, scaleMultiplier, blendMode = PIXI.BLEND_MODES.NORMAL, startFrame=0, totalFrames=undefined)
	{
		let lSmoke_sprt = Sprite.createMultiframesSprite(CommonEffectsManager.getDieSmokeUnmultTextures(), startFrame, totalFrames);
		this.addChild(lSmoke_sprt);

		lSmoke_sprt.animationSpeed = 30/60;
		lSmoke_sprt.blendMode = blendMode;

		lSmoke_sprt.anchor.set(0.57, 0.81);
		lSmoke_sprt.scale.set(2 * scaleMultiplier.x, 2 * scaleMultiplier.y);
		if (position !== undefined)
		{
			lSmoke_sprt.position.set(position.x, position.y);
		}

		lSmoke_sprt.on('animationend', () => {
			lSmoke_sprt.destroy();
			
			this._fDieSmokesAmount_num --;
			this._onAnimationCompleteSuspicion();
		});

		lSmoke_sprt.play();
		this._fDieSmokesAmount_num ++;

		return lSmoke_sprt;
	}

	_addTorch(aPosition_pt, aRotationInGrad_num)
	{
		let lTorch_sprt = this.addChild(new Sprite);
		lTorch_sprt.textures = TorchFxAnimation.textures.torch;
		lTorch_sprt.anchor.set(0.5, 0.7);
		lTorch_sprt.position.set(aPosition_pt.x, aPosition_pt.y);
		lTorch_sprt.rotation = Utils.gradToRad(aRotationInGrad_num);
		lTorch_sprt.scale.set(1.53);
		lTorch_sprt.alpha = 0.33;
		
		lTorch_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

		lTorch_sprt.play();

		let seq = [
						{ tweens: [ { prop: 'alpha', to: 1 } ], duration: 3*2*16.7 },
						{ tweens: [], duration: 17*2*16.7 },
						{ 
							tweens: [ { prop: 'alpha', to: 0 } ], 
							duration: 6*2*16.7, 
							onfinish: () => { 
												lTorch_sprt.stop();
											}
						}
					]

		let torchSequence = Sequence.start(lTorch_sprt, seq);
		this._fTorchesSequences_arr.push(torchSequence);
	}

	_addTransitionSmoke(flameDescription)
	{
		let lTransitionSmoke_sprt = this.addChild(APP.library.getSpriteFromAtlas('common/transition_smoke_fx_unmult'));
		lTransitionSmoke_sprt.blendMode = flameDescription.blendMode;
		lTransitionSmoke_sprt.scale.set(flameDescription.scale.x, flameDescription.scale.y);
		lTransitionSmoke_sprt.position.set(flameDescription.position.x, flameDescription.position.y);
		lTransitionSmoke_sprt.alpha = flameDescription.alpha;
		lTransitionSmoke_sprt.rotation = flameDescription.rotation;
		if (flameDescription.tint)
		{
			lTransitionSmoke_sprt.tint = flameDescription.tint;
		}

		for (let i=0; i<flameDescription.sequences.length; i++)
		{
			let seq = Sequence.start(lTransitionSmoke_sprt, flameDescription.sequences[i], flameDescription.delay);
			seq.once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._onTransitionSmokeSequenceCompleted, this);
			this._fTransSmokeSequences_arr.push(seq);
		}

		if (flameDescription.moveDelta)
		{
			let moveDeltaDescr = flameDescription.moveDelta;
			let newX = this.x + moveDeltaDescr.position.x;
			let newY = this.y + moveDeltaDescr.position.y;
			lTransitionSmoke_sprt.moveTo(newX, newY, moveDeltaDescr.duration);
		}
	}

	_onTransitionSmokeSequenceCompleted(aEvent_obj)
	{
		let seq = aEvent_obj.target;
		let lIndex_int = this._fTransSmokeSequences_arr.indexOf(seq);
		if (~lIndex_int)
		{
			this._fTransSmokeSequences_arr.splice(lIndex_int, 1);
			seq.destructor();
		}

		this._onAnimationCompleteSuspicion();
	}

	_onAnimationCompleteSuspicion()
	{
		if (
				(this._fDieSmokesAmount_num <= 0) 
				&& (!this._fTransSmokeSequences_arr || !this._fTransSmokeSequences_arr.length)
			)
		{
			this._onAnimationCompleted();
		}
	}

	_onAnimationCompleted()
	{
		this.emit(FlameThrowerHitFXs.EVENT_ON_ANIMATION_COMPLETED);
		this.destroy();
	}

	destroy()
	{	
		while (this._fTorchesSequences_arr && this._fTorchesSequences_arr.length)
		{
			let torchSeq = this._fTorchesSequences_arr.pop();
			torchSeq.destructor();
		}
		this._fTorchesSequences_arr = null;	

		while (this._fTransSmokeSequences_arr && this._fTransSmokeSequences_arr.length)
		{
			let seq = this._fTransSmokeSequences_arr.pop();
			seq.destructor();
		}
		this._fTransSmokeSequences_arr = null;

		super.destroy();
	}

}

export default FlameThrowerHitFXs;