import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

const FLAME_BASE_POSITION = new PIXI.Point(0, 40);
const FLAME_BASE_SEQUENCES = [
								[
									{ tweens: [ { prop: 'scale.x', to: 2 }, { prop: 'scale.y', to: 2 } ], duration: 16*2*16.7 }
								],
								[
									{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(80) } ], duration: 16*2*16.7 }
								],
								[
									{ tweens: [], duration: 9*2*16.7 },
									{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 7*2*16.7 }
								]
							];

const FLAME_LONG_SEQUENCES = [
								[
									{ tweens: [ { prop: 'scale.x', to: 2 }, { prop: 'scale.y', to: 2 } ], duration: 21*2*16.7 }
								],
								[
									{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(80) } ], duration: 21*2*16.7 }
								],
								[
									{ tweens: [], duration: 14*2*16.7 },
									{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 7*2*16.7 }
								]
							];

const FLAMES_DESCR = [
	{// flame layer 19
		position: new PIXI.Point(FLAME_BASE_POSITION.x, FLAME_BASE_POSITION.y),
		scale: new PIXI.Point(0.3, 0.3),
		alpha: 1,
		rotation: Utils.gradToRad(11),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 0,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 2 }, { prop: 'scale.y', to: 2 } ], duration: 13*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(80) } ], duration: 18*2*16.7 }
						],
						[
							{ tweens: [], duration: 6*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 7*2*16.7 }
						]
					]
	},
	{// flame layer 18
		position: new PIXI.Point(FLAME_BASE_POSITION.x+38, FLAME_BASE_POSITION.y-13.5),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 0,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 17
		position: new PIXI.Point(FLAME_BASE_POSITION.x-17.5, FLAME_BASE_POSITION.y-19),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 2*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 16
		position: new PIXI.Point(FLAME_BASE_POSITION.x-1.5, FLAME_BASE_POSITION.y-35),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 5*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 15
		position: new PIXI.Point(FLAME_BASE_POSITION.x+25, FLAME_BASE_POSITION.y+5),
		scale: new PIXI.Point(0.10, 0.10),
		alpha: 1,
		rotation: Utils.gradToRad(4),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 0,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 2 }, { prop: 'scale.y', to: 2 } ], duration: 15*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(80) } ], duration: 15*2*16.7 }
						],
						[
							{ tweens: [], duration: 8*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 7*2*16.7 }
						]
					]
	},
	{// flame layer 14
		position: new PIXI.Point(FLAME_BASE_POSITION.x-3, FLAME_BASE_POSITION.y-50),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 2*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 13
		position: new PIXI.Point(FLAME_BASE_POSITION.x-2, FLAME_BASE_POSITION.y-5),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 3*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 12
		position: new PIXI.Point(FLAME_BASE_POSITION.x+32, FLAME_BASE_POSITION.y-35.5),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 5*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 11
		position: new PIXI.Point(FLAME_BASE_POSITION.x-10, FLAME_BASE_POSITION.y-40),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 5*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 10
		position: new PIXI.Point(FLAME_BASE_POSITION.x+33, FLAME_BASE_POSITION.y-13.5),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 7*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 9
		position: new PIXI.Point(FLAME_BASE_POSITION.x-17.5, FLAME_BASE_POSITION.y-19),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 9*2*16.7,
		sequences: FLAME_BASE_SEQUENCES
	},
	{// flame layer 8
		position: new PIXI.Point(FLAME_BASE_POSITION.x-1, FLAME_BASE_POSITION.y-35),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 11*2*16.7,
		sequences: FLAME_LONG_SEQUENCES
	},
	{// flame layer 7
		position: new PIXI.Point(FLAME_BASE_POSITION.x+45, FLAME_BASE_POSITION.y),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 6*2*16.7,
		sequences: FLAME_LONG_SEQUENCES
	},
	{// flame layer 6
		position: new PIXI.Point(FLAME_BASE_POSITION.x+3, FLAME_BASE_POSITION.y-40),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 8*2*16.7,
		sequences: FLAME_LONG_SEQUENCES
	},
	{// flame layer 5
		position: new PIXI.Point(FLAME_BASE_POSITION.x-2, FLAME_BASE_POSITION.y),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 10*2*16.7,
		sequences: FLAME_LONG_SEQUENCES
	},
	{// flame layer 4
		position: new PIXI.Point(FLAME_BASE_POSITION.x+37, FLAME_BASE_POSITION.y-40),
		scale: new PIXI.Point(0, 0),
		alpha: 1,
		rotation: Utils.gradToRad(0),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 11*2*16.7,
		sequences: FLAME_LONG_SEQUENCES
	},
	{// flame layer 3
		position: new PIXI.Point(FLAME_BASE_POSITION.x+19, FLAME_BASE_POSITION.y-18),
		scale: new PIXI.Point(0.1, 0.1),
		alpha: 0.7,
		rotation: Utils.gradToRad(15),
		blendMode: PIXI.BLEND_MODES.SCREEN,
		delay: 1*2*16.7,
		sequences: [
						[
							{ tweens: [ { prop: 'scale.x', to: 1 }, { prop: 'scale.y', to: 1 } ], duration: 13*2*16.7 }
						],
						[
							{ tweens: [ { prop: 'rotation', to: Utils.gradToRad(80) } ], duration: 13*2*16.7 }
						],
						[
							{ tweens: [], duration: 6*2*16.7 },
							{ tweens: [ { prop: 'alpha', to: 0 } ], duration: 7*2*16.7 }
						]
					]
	}
];

class FlameThrowerHitFlame extends Sprite 
{
	static get EVENT_ON_ANIMATION_COMPLETED()		{ return 'EVENT_ON_ANIMATION_COMPLETED'; }

	startAnimation()
	{
		this._startAnimation();
	}

	constructor()
	{
		super();

		this._fSequences = null;
	}

	_startAnimation()
	{
		this._fSequences = [];
		for (let i=0; i<FLAMES_DESCR.length; i++)
		{
			this._addFlame(FLAMES_DESCR[i]);
		}

		//DEBUG...
		// let gr = this.addChild(new PIXI.Graphics());
		// gr.beginFill(0x00ff00);
		// gr.drawRect(-10, -50, 20, 100);
		// gr.endFill();

		// gr = this.addChild(new PIXI.Graphics());
		// gr.beginFill(0x0000ff);
		// gr.drawRect(-10, -50, 20, 20);
		// gr.endFill();
		//...DEBUG
	}

	_addFlame(flameDescription)
	{
		let lBaseFlame_sprt = this.addChild(new Sprite());
		
		let lFlamePart1_sprt = lBaseFlame_sprt.addChild(APP.library.getSpriteFromAtlas('weapons/FlameThrower/hit_flame'));
		lFlamePart1_sprt.alpha = 0.1;
		let lFlamePart2_sprt = lBaseFlame_sprt.addChild(APP.library.getSpriteFromAtlas('weapons/FlameThrower/hit_flame'));
		lFlamePart2_sprt.blendMode = flameDescription.blendMode;

		lBaseFlame_sprt.scale.set(flameDescription.scale.x, flameDescription.scale.y);
		lBaseFlame_sprt.position.set(flameDescription.position.x, flameDescription.position.y);
		lBaseFlame_sprt.alpha = flameDescription.alpha;
		lBaseFlame_sprt.rotation = flameDescription.rotation;

		for (let i=0; i<flameDescription.sequences.length; i++)
		{
			let seq = Sequence.start(lBaseFlame_sprt, flameDescription.sequences[i], flameDescription.delay);
			seq.once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._onFlameSequenceCompleted, this);
			this._fSequences.push(seq);
		}
	}

	_onFlameSequenceCompleted(aEvent_obj)
	{
		let seq = aEvent_obj.target;
		let lIndex_int = this._fSequences.indexOf(seq);
		if (~lIndex_int)
		{
			this._fSequences.splice(lIndex_int, 1);
			seq.destructor();
		}

		this._onAnimationCompleteSuspicion();
	}

	_onAnimationCompleteSuspicion()
	{
		if (!this._fSequences || !this._fSequences.length)
		{
			this._onAnimationCompleted();
		}
	}

	_onAnimationCompleted()
	{
		this.emit(FlameThrowerHitFlame.EVENT_ON_ANIMATION_COMPLETED);
		this.destroy();
	}

	destroy()
	{
		while (this._fSequences && this._fSequences.length)
		{
			let seq = this._fSequences.pop();
			seq.destructor();
		}
		this._fSequences = null;
		
		super.destroy();
	}

}

export default FlameThrowerHitFlame;