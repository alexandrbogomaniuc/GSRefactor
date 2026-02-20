import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Grenade from '../../Grenade';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const GROUND_SMOKES_PARAMS = [
	{ // 2
		scaleX: 0.67,
		scaleY: 0.67,
		rotation: 88,
		x: -3,
		y: 86,
		startDelay: 0,
		zIndex: 2
	},
	{ // 28
		scaleX: 1.95 * 2,
		scaleY: 1.95 * 2,
		rotation: 0,
		x: -32,
		y: -1,
		startDelay: 1,
		zIndex: 28,
		isLast: true
	}
];

const BURST_SMOKES_PARAMS = [
	{ // 29
		scaleX: 0.842,
		scaleY: 2.478,
		rotation: 241,
		x: 55,
		y: 7,
		startDelay: 0,
		zIndex: 29
	},
	{ // 30
		scaleX: 1.137,
		scaleY: 3.348,
		rotation: 178,
		x: 21,
		y: -73,
		startDelay: 4,
		zIndex: 30
	},
	{ // 31
		scaleX: 0.972,
		scaleY: 2.679,
		rotation: 165,
		x: -41,
		y: -19,
		startDelay: 2,
		zIndex: 31
	},
	{ // 53
		scaleX: 6.74,
		scaleY: 2.12,
		rotation: 180,
		x: 8,
		y: 97,
		startDelay: 3,
		zIndex: 53
	},
	{ // 54
		scaleX: 7.38,
		scaleY: 8.02,
		rotation: -1,
		x: -67,
		y: -292,
		startDelay: 3,
		zIndex: 54
	},
	{ // 55
		scaleX: 4.03,
		scaleY: 3.15,
		rotation: -213,
		x: 48,
		y: 82,
		startDelay: 3,
		zIndex: 55,
		anchor: {
			x: 0.6,
			y: 0.7
		}
	},
	{ // 56
		scaleX: 3.49,
		scaleY: 3.43,
		rotation: -114,
		x: 39,
		y: 69,
		startDelay: 3,
		zIndex: 56,
		anchor: {
			x: 0.6,
			y: 0.8
		}
	},
	{ // 57
		scaleX: 3.9,
		scaleY: 6.76,
		rotation: -83,
		x: -345,
		y: 41,
		startDelay: 3,
		zIndex: 57
	},
	{ // 58
		scaleX: 3.9,
		scaleY: 6.76,
		rotation: 89,
		x: 318,
		y: 0,
		startDelay: 3,
		zIndex: 58
	}
]

const PILLARS_PARAMS = [
	{
		rotation: 95, startDelay: 4, deltaX: 25, deltaY: 0, scaleX: 0.48, scaleY: 0.62
	},
	{
		rotation: -91, startDelay: 3, deltaX: -25, deltaY: 0, scaleX: 0.48, scaleY: 0.62
	},
	{
		rotation: 0, startDelay: 4, deltaX: 0, deltaY: -25, scaleX: 0.84, scaleY: 0.8
	}
];

const SPARKS_EMITTER = {
	"alpha": {
		"start": 1,
		"end": 0
	},
	"scale": {
		"start": 1,
		"end": 1,
		"minimumScaleMultiplier": 1
	},
	"color": {
		"start": "#ffffff",
		"end": "#ffffff"
	},
	"speed": {
		"start": 600,
		"end": 100,
		"minimumSpeedMultiplier": 1
	},
	"acceleration": {
		"x": 0,
		"y": 0
	},
	"maxSpeed": 0,
	"startRotation": {
		"min": -140,
		"max": -40
	},
	"noRotation": false,
	"rotationSpeed": {
		"min": 0,
		"max": 0
	},
	"lifetime": {
		"min": 0.3,
		"max": 0.5
	},
	"blendMode": "add",
	"frequency": 0.03,
	"emitterLifetime": 0.45,
	"maxParticles": 60,
	"pos": {
		"x": 0,
		"y": 0
	},
	"addAtBack": false,
	"spawnType": "circle",
	"spawnCircle": {
		"x": 0,
		"y": 0,
		"r": 0
	}
}

class GrenadeEffectsAnimation extends Sprite {
	constructor() {
		super();

		this._timers = [];
		this._sequences = [];
		this.eternalEmitter = null;
		this.emitterContainer = null;
		this._tickFunc = null;
	}

	start() {
		Grenade.getTextures();

		let groundSmokesContainer = this.addChild(new Sprite);
		groundSmokesContainer.zIndex = 100-37;

		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			for (let params of GROUND_SMOKES_PARAMS) {
				let timer = new Timer(() => this._addGroundSmoke(params, /*groundSmokesContainer*/this), params.startDelay*2*16.6);
				this._addTimer(timer);
			}

			for (let params of BURST_SMOKES_PARAMS) {
				let timer = new Timer(() => this._addBurstSmoke(params, /*groundSmokesContainer*/this), params.startDelay*2*16.6);
				this._addTimer(timer);
			}
		}

		this._addGrenadeBlast();
		this._addGrenadeBlast2();

		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._addGlow();
			this._addGlow2();

			this._addFireblast();

			this._addDimmer();

			let pillarsContainer = this.addChild(new Sprite);
			pillarsContainer.zIndex = 100-50;
			for (let params of PILLARS_PARAMS)
			{
				let timer = new Timer(() => this._addPillar(params, pillarsContainer), params.startDelay*2*16.6);
				this._addTimer(timer);
			}
		}

		let sparksContainer = this.addChild(new Sprite);
		this._addSparks(sparksContainer);
	}

	_addGroundSmoke(params, container) {
		let groundSmoke = this._addEffect('groundSmoke', container, PIXI.BLEND_MODES.SCREEN);
		groundSmoke.anchor.set(125/250, 114/165);
		groundSmoke.rotation = Utils.gradToRad(params.rotation);
		groundSmoke.position.set(params.x/8, params.y/8);
		groundSmoke.scale.x = params.scaleX * 2;
		groundSmoke.scale.y = params.scaleY * 2;
		groundSmoke.zIndex = 100 - params.zIndex;
		if (params.isLast)
		{
			groundSmoke.once('animationend', (e) => {
				this.emit('animationFinish');
				this.destroy();
			})
		};

	}

	_addBurstSmoke(params, container) {
		let burstSmoke = this._addEffect('burstSmoke', container, PIXI.BLEND_MODES.SCREEN);
		burstSmoke.rotation = Utils.gradToRad(params.rotation);
		burstSmoke.position.set(params.x/2, params.y/2 - 40);
		if (params.anchor)
		{
			burstSmoke.anchor.set(params.anchor.x, params.anchor.y)
		}
		burstSmoke.scale.x = params.scaleX;
		burstSmoke.scale.y = params.scaleY;

		if (params.zIndex === 29)
		{
			burstSmoke.alpha = 0;
			let sequence = [
				{
					tweens: [],
					duration: 20*2*16.6
				},
				{
					tweens: [
						{ prop: 'alpha', to: 1}
					],
					duration: 8*2*16.6
				}
			];
			this._addSequence(Sequence.start(burstSmoke, sequence));
		}

	}

	_addGrenadeBlast() {
		let grenadeBlast = this._addEffect('grenadeBlast', this, PIXI.BLEND_MODES.ADD);
		grenadeBlast.zIndex = 100-49;
		grenadeBlast.anchor.set(0.51, 0.64);
		grenadeBlast.scale.x = 1*4;
		grenadeBlast.scale.y = 0.3*4;

		grenadeBlast.stop();
		grenadeBlast.visible = false;

		let sequence = [
			{
				tweens: [],
				duration: 1*2*16.6,
				onfinish: () => {
					grenadeBlast.visible = true;
					grenadeBlast.play();
				}
			},
			{
				tweens: [
					{prop: 'scale.x', to: 0.53*4},
					{prop: 'scale.y', to: 0.53*4},
					{prop: 'position.y', to: -20 }
				],
				duration: (3)*2*16.6,
			}
		]

		let seq = Sequence.start(grenadeBlast, sequence);
		this._addSequence(seq);

		return grenadeBlast;
	}

	_addGrenadeBlast2() {
		let grenadeBlast2 = this._addEffect('grenadeBlast', this, PIXI.BLEND_MODES.ADD);
		grenadeBlast2.stop();
		grenadeBlast2.visible = false;

		grenadeBlast2.zIndex = 100 - 16;
		grenadeBlast2.anchor.set(0.51, 0.64);
		grenadeBlast2.scale.x = 1*4;
		grenadeBlast2.scale.y = 0.3*4;

		if(!APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			grenadeBlast2.once('animationend', (e) => {
				this.emit('animationFinish');
				this.destroy();
			})
		}

		let sequence = [
			{
				tweens: [],
				duration: 1*2*16.6,
				onfinish: () => {
					grenadeBlast2.visible = true;
					grenadeBlast2.play();
				}
			},
			{
				tweens: [
					{prop: 'scale.x', to: 0.53*4},
					{prop: 'scale.y', to: 0.33*4},
					{prop: 'position.y', to: -10 }
				],
				duration: (3)*2*16.6
			}
		]

		let seq = Sequence.start(grenadeBlast2, sequence);
		this._addSequence(seq);

		return grenadeBlast2;

	}

	_addGlow() {
		let glow = this.addChild(APP.library.getSprite('common/crate_glow'));
		glow.zIndex = 100-7;
		glow.blendMode = PIXI.BLEND_MODES.ADD;
		glow.scale.set(0);

		let sequenceScale = [
			{
				tweens: [
					{prop: 'scale.x', to: 1.54*2},
					{prop: 'scale.y', to: 1.54*2}
				],
				duration: 2*2*16.6
			},
			{
				tweens: [
					{prop: 'scale.x', to: 6*2},
					{prop: 'scale.y', to: 6*2}
				],
				duration: 6*2*16.6,
				onfinish: (e) => {
					glow.destroy();
				}
			}
		]

		let seqScale = Sequence.start(glow, sequenceScale, 1*2*16.6);
		this._addSequence(seqScale);

		let sequenceAlpha = [
			{
				tweens: [
					{prop: 'alpha', to: 0}
				],
				duration: 5*2*16.6
			},
		]

		let seqAlpha = Sequence.start(glow, sequenceAlpha, 6*2*16.6);
		this._addSequence(seqAlpha);
	}

	_addGlow2() {
		let glow2 = this.addChild(APP.library.getSpriteFromAtlas('weapons/GrenadeGun/crate_glow_ring'));
		glow2.zIndex = 100-6;
		glow2.blendMode = PIXI.BLEND_MODES.ADD;
		glow2.scale.set(0);
		glow2.alpha = 0.95;

		glow2.scaleXTo(13.2*2, 20*2*16.6, Easing.sine.easeInOut);
		glow2.scaleYTo(9.69*2, 20*2*16.6, Easing.sine.easeInOut);

		let sequenceAlpha = [
			{
				tweens: [],
				duration: 1*2*16.6
			},
			{
				tweens: [
					{prop: 'alpha', to: 0}
				],
				duration: 18*2*16.6,
				onfinish: (e) => {
					glow2.destroy();
				}
			}
		]

		let seqAlpha = Sequence.start(glow2, sequenceAlpha, 6*2*16.6);
		this._addSequence(seqAlpha);
	}

	_addFireblast() {

		let fireblast = this.addChild(APP.library.getSpriteFromAtlas('weapons/GrenadeGun/MQ_Grenade_Fireblast_ADD'));
		fireblast.blendMode = PIXI.BLEND_MODES.ADD;
		fireblast.anchor.set(0.5, 0.55);
		fireblast.scale.set(0);
		fireblast.zIndex = 100-48;

		let sequence = [
			{
				tweens: [],
				duration: 4*2*16.6,
			},
			{
				tweens: [
					{ prop: 'scale.x', to: 1.42*2 },
					{ prop: 'scale.y', to: 1.42*2 }
				],
				duration: 1*2*16.6
			},
			{
				tweens: [
					{prop: 'scale.x', to: 0},
					{prop: 'scale.y', to: 0}
				],
				duration: 5*2*16.6,
				onfinish: (e) => {
					fireblast.destroy();
				}
			}
		]

		let seq = Sequence.start(fireblast, sequence);
		this._addSequence(seq);
	}

	_addDimmer() {
		let dimmer = this.addChild(APP.library.getSpriteFromAtlas('weapons/GrenadeGun/dimmer'));
		dimmer.zIndex = 100-78;
		dimmer.alpha = 0;
		dimmer.scale.set(4);

		let sequence = [
			{
				tweens: [],
				duration: 3*2*16.6
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0.2 }
				],
				duration: 2*2*16.6
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0.21 }
				],
				duration: 1*2*16.6
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0 }
				],
				duration: 5*2*16.6
			}
		]

		let seq = Sequence.start(dimmer, sequence);
		this._addSequence(seq);
	}

	_addPillar(params, container) {
		let pillar = container.addChild(APP.library.getSpriteFromAtlas('weapons/GrenadeGun/MQ_Grenade_BlackSmokePillar'));
		pillar.anchor.set(0.5, 364/426);
		pillar.scale.x = 1.25*params.scaleX;
		pillar.scale.y = 1.25*params.scaleY;
		pillar.rotation = Utils.gradToRad(params.rotation);

		pillar.scaleXTo(1.68*params.scaleX, 10*2*16.6, Easing.sine.easeOut);
		pillar.scaleYTo(1.68*params.scaleY, 10*2*16.6, Easing.sine.easeOut);

		pillar.moveTo(params.deltaX, params.deltaY, 10*2*16.6);

		let sequence = [
			{
				tweens: [],
				duration: 3*2*16.6
			},
			{
				tweens: [
					{prop: 'alpha', to: 0},
				],
				duration: 7*2*16.6,
				onfinish: (e) => {
					pillar.destroy();
				}
			}
		]

		let seq = Sequence.start(pillar, sequence);
		this._addSequence(seq);
	}

	_addEffect(name, container, blendMode){

		let grenadeEffect = container.addChild(new Sprite);
		grenadeEffect.textures = Grenade.textures[name];

		grenadeEffect.blendMode = blendMode ? blendMode : PIXI.BLEND_MODES.NORMAL;

		grenadeEffect.play();

		grenadeEffect.once("animationend", (e) => {
			e.target.destroy();
		});

		return grenadeEffect;
	}

	_addSparks(container) {
		this.emitterContainer = container;

		let texture = APP.library.getSpriteFromAtlas('common/spark').textures[0];
		this.eternalEmitter = new PIXI.particles.Emitter(this.emitterContainer, [texture], SPARKS_EMITTER);
		this.eternalEmitter.updateOwnerPos(this.emitterContainer.x, this.emitterContainer.y);

		this._tickFunc = this.tick.bind(this);
		APP.on("tick", this._tickFunc);
	}

	_addTimer(timer)
	{
		this._timers.push(timer);
	}

	_removeTimers()
	{
		if (!this._timers)
		{
			return;
		}

		while (this._timers.length)
		{
			this._timers.pop().destructor();
		}
	}

	_addSequence(seq)
	{
		this._sequences.push(seq);
	}

	_removeSequences()
	{
		if (!this._sequences)
		{
			return;
		}

		while (this._sequences.length)
		{
			this._sequences.pop().destructor();
		}
	}

	destroy()
	{
		this._tickFunc && APP.off("tick", this._tickFunc);
		this._tickFunc = null;

		if(this.eternalEmitter)
		{
			this.eternalEmitter.destroy();
			this.eternalEmitter = null;
			this.emitterContainer = null;
		}

		this._removeTimers();
		this._timers = null;

		this._removeSequences();
		this._sequences = null;

		super.destroy();
	}

	updateEternalEmitterPosition(){
		if(this.eternalEmitter && this.emitterContainer){
			this.eternalEmitter.updateOwnerPos(this.emitterContainer.x, this.emitterContainer.y);
		}
	}

	tick(e){
		if (this.eternalEmitter)
		{
			this.updateEternalEmitterPosition();
			this.eternalEmitter.update(e.delta/1000);
		}
	}
}

export default GrenadeEffectsAnimation;