import AppearanceView from './AppearanceView';
import { ENEMIES, FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import * as Easing from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import CommonEffectsManager from '../../../../../main/CommonEffectsManager';
import DeathFxAnimation from '../../../../../main/animation/death/DeathFxAnimation';
import RockFlyingDebris from '../../../../../main/bullets/RockFlyingDebris';

const CRACKS = [
	{id: 67, maskDirection: 0, maskStartFrame: 3, 	maskEndFrame: 25, x: 992, y: 598, 	scaleX: 1, 		scaleY: 1, 		angle: 0, 	fadeOutStartFrame: 97, fadeOutEndFrame: 101},
	{id: 0, maskDirection: 0, maskStartFrame: 49, 	maskEndFrame: 62, x: 1092, y: 572, 	scaleX: 0.55, 	scaleY: 0.55, 	angle: -10, fadeOutStartFrame: 66, fadeOutEndFrame: 70},
	{id: 0, maskDirection: 0, maskStartFrame: 55, 	maskEndFrame: 62, x: 1098, y: 622, 	scaleX: -0.61, 	scaleY: 1, 		angle: 192, fadeOutStartFrame: 75, fadeOutEndFrame: 79},
	{id: 0, maskDirection: 1, maskStartFrame: 25,	maskEndFrame: 30, x: 964, y: 578,	scaleX: -0.61,	scaleY: 1, 		angle: 10, 	fadeOutStartFrame: 43, fadeOutEndFrame: 47}
]

const BEAMS = [
	{	id: 87, x: 893, y: 624, angle: 143, alpha: 0, scaleX: 1.98 * 2, scaleY: 1.646 * 2,
		scaleSeq:
		[
			{
				tweens: [],
				duration: 10 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 2.165 * 2},
					{prop: "scale.y", to: 1.658 * 2}
				],
				duration: 4 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 1.765 * 2},
					{prop: "scale.y", to: 3.361 * 2}
				],
				duration: 42 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 1.764 * 2},
					{prop: "scale.y", to: 2.135 * 2}
				],
				duration: 8 * FRAME_RATE
			}
		],
		alphaSeq:
		[
			{
				tweens: [],
				duration: 10 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.68 }],
				duration: 2 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.69 }],
				duration: 66 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0 }],
				duration: 8 * FRAME_RATE,
				onfinish: (e) => {
					e.target.obj.destroy();
				}
			}
		]
	},
	{	id: 86, x: 1032, y: 587, angle: 173, alpha: 0, scaleX: 1.98 * 2, scaleY: 1.64 * 2,
		scaleSeq:
		[
			{
				tweens: [],
				duration: 20 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 2.165 * 2},
					{prop: "scale.y", to: 1.658 * 2}
				],
				duration: 4 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 1.83 * 2},
					{prop: "scale.y", to: 3.05 * 2}
				],
				duration: 63 * FRAME_RATE
			}
		],
		alphaSeq:
		[
			{
				tweens: [],
				duration: 20 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.97 }],
				duration: 2 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.99 }],
				duration: 57 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0 }],
				duration: 8 * FRAME_RATE,
				onfinish: (e) => {
					e.target.obj.destroy();
				}
			}
		]
	},
	{	id: 85, x: 1161, y: 591, angle: 223, alpha: 0, scaleX: 1.98 * 2, scaleY: 1.64 * 2,
		scaleSeq:
		[
			{
				tweens: [],
				duration: 31 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 2.165 * 2},
					{prop: "scale.y", to: 1.658 * 2}
				],
				duration: 4 * FRAME_RATE
			},
			{
				tweens: [
					{prop: "scale.x", to: 1.88},
					{prop: "scale.y", to: 2.84}
				],
				duration: 53 * FRAME_RATE
			}
		],
		alphaSeq:
		[
			{
				tweens: [],
				duration: 31 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.68 }],
				duration: 2 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0.69 }],
				duration: 47 * FRAME_RATE
			},
			{
				tweens: [ {prop: "alpha", to: 0 }],
				duration: 8 * FRAME_RATE,
				onfinish: (e) => {
					e.target.obj.destroy();
				}
			}
		]
	}
]

const SMOKES = [
	{id: 48, x: 771, y: 609},
	{id: 47, x: 827, y: 618},
	{id: 46, x: 850, y: 620},
	{id: 45, x: 902, y: 610},
	{id: 44, x: 939, y: 606},
	{id: 43, x: 974, y: 595},
	{id: 42, x: 1018, y: 584},
	{id: 41, x: 1076, y: 600},
	{id: 40, x: 1119, y: 598},
	{id: 39, x: 1163, y: 597},
	{id: 38, x: 1182, y: 588},
	{id: 37, x: 1223, y: 589},

	{id: 36, x: 771, y: 609},
	{id: 35, x: 843, y: 627},
	{id: 34, x: 899, y: 615},
	{id: 33, x: 947, y: 599},
	{id: 32, x: 1013, y: 589},
	{id: 31, x: 1159, y: 599},
	{id: 30, x: 997, y: 561, scale: 2.83},
	{id: 29, x: 771, y: 609},
	{id: 28, x: 843, y: 627},
	{id: 27, x: 899, y: 615},
	{id: 26, x: 947, y: 599},
	{id: 25, x: 1013, y: 589},
	{id: 24, x: 1159, y: 599},
	{id: 23, x: 997, y: 561, scale: 2.83}
];

const GROUND_HITS = [
	{x: 836, y: 594, scale: -1.52, angle: 90},
	{x: 1197, y: 594, scale: -1.52, angle: 90},
	{x: 1273, y: 475, scale: -1.52, angle: 44},
	{x: 693, y: 559, scale: -1.52, angle: -64},
	{x: 727, y: 457, scale: -2.12, angle: -38},
	{x: 971, y: 419, scale: -1.64, angle: -5},
	{x: 1059, y: 373, scale: -2.04, angle: 10},
	{x: 1163, y: 315, scale: -2.34, angle: 20},
	{x: 871, y: 419, scale: 1.64, angle: -25},
	{x: 959, y: 373, scale: 2.04, angle: -10},
	{x: 1063, y: 315, scale: 2.34, angle: 0}

];

const PUFFS = [
	{id: 89, startFrame: 65, endFrame: 117, startScale: 0.9, endScale: 2, x: 1036, y: 505},
	{id: 68, startFrame: 68, endFrame: 97, startScaleX: 0.783, endScaleX: 1.18, startScaleY: -0.913, endScaleY: -1.379}
];

const ROCKS = [
	{startFrame: 3, x: 882, y: 482},
	{startFrame: 5, x: 1486, y: 488},
	{startFrame: 8, x: 782, y: 501},
	{startFrame: 10, x: 733, y: 492},
	{startFrame: 12, x: 663, y: 490},
	{startFrame: 14, x: 627,  y: 495},
	{startFrame: 17, x: 1154, y: 515},
	{startFrame: 19, x: 1103, y: 506},

	{startFrame: 6, x: 586, y: 503},
	{startFrame: 9, x: 537, y: 515},

	{startFrame: 40, x: 900, y: 558},
	{startFrame: 41, x: 851,  y: 538},
	{startFrame: 42, x: 773, y: 538},
	{startFrame: 44, x: 882, y: 482},
	{startFrame: 46, x: 1486,  y: 488},
	{startFrame: 48, x: 782, y: 501},
	{startFrame: 50, x: 733, y: 492},
	{startFrame: 52, x: 663,  y: 490},
	{startFrame: 54, x: 627, y: 495},
	{startFrame: 56, x: 586, y: 503},
	{startFrame: 58, x: 537, y: 515},
	{startFrame: 60, x: 1154, y: 515},
	{startFrame: 61, x: 1103, y: 506}
];

const BIG_ROCKS = [
	{startX: 1044, startY: 603, endX: 2000, endY: 734},
	{startX: 1044, startY: 603, endX: 2000, endY: 174},
	{startX: 1044, startY: 603, endX: -186, endY: 174}
]


class GolemAppearanceView extends AppearanceView {

	constructor(aViewContainerInfo_obj)
	{
		super();

		this._fViewContainerInfo_obj = aViewContainerInfo_obj;
		this._fCracks_sprt_arr = [];
		this._fBeams_sprt_arr = [];
		this._fSmokes_sprt_arr = [];
		this._fShakeTimer_tmr = null;
		this._fStartExplosionsTimer_tmr = null;
		this._fPuffs_sprt_arr = [];
		this._fRockTimers_arr = [];
		this._fSpecialChildren_sprt_arr = [];
		this._fSoundsTimers_tmr_arr = [];
	}

	//INIT...
	get _captionPosition()
	{
		return { x:0, y:50 };
	}

	get _appearingCulminationTime()
	{
		return 14*FRAME_RATE;
	}

	get _completionDelay()
	{
		return 185*FRAME_RATE;
	}

	get _bossAppearanceSequences()
	{
		return [
			[
				{ tweens:[{prop:"y", to: 0}], duration:65*FRAME_RATE, ease:Easing.sine.easeInOut },
				{ tweens:[], duration:20*FRAME_RATE} //to prevent mask disappearing earlier than expected
			]
		];
	}

	get _bossAppearanceInit()
	{
		return {x: 0, y: 225, rotation: 0};
	}

	get _bossType()
	{
		return ENEMIES.GolemBoss;
	}
	//...INIT

	//ANIMATION...
	_onTimeToStartCaptionAnimation()
	{
		this.emit(AppearanceView.EVENT_ON_TIME_TO_START_CAPTION_ANIMATION, {captionPosition: this._captionPosition, startDelay: 61*FRAME_RATE});
	}

	//override
	_playAppearingAnimation()
	{
		super._playAppearingAnimation();

		this._startCracksAnimation();
		this._startBeamsAnimation();
		this._startSmokesAnimation();
		this._prepareShakeTheGround();

		this._prepareExplosions();
		this._showPuffs();
		this._startFlyingSmallRocks();
		this._prepareFlyingBigRocks();

		this._prepareSounds();
	}

	_startCracksAnimation()
	{
		for (let lCrackParams_obj of CRACKS)
		{
			this._createCrack(lCrackParams_obj);
		}
	}

	_createCrack(aCrackParams_obj)
	{
		let lCrack_sprt = this._addChildSpecial(new Crack(aCrackParams_obj));
		lCrack_sprt.zIndex = aCrackParams_obj.id === 0 ? 0 : lCrack_sprt.y;//lCrack_sprt.y; // aCrackParams_obj.id; //1000 - aCrackParams_obj.id;

		/*let lParentPosition_pt = this.parent.position;
		lCrack_sprt.x -= lParentPosition_pt.x;
		lCrack_sprt.y -= lParentPosition_pt.y;*/

		this._fCracks_sprt_arr.push(lCrack_sprt);
	}

	_startBeamsAnimation()
	{
		let lParentPosition_pt = this.parent.position;
		for (let lBeamParams_obj of BEAMS)
		{
			let lBeam_sprt = this.addChild(this._createBeam(lBeamParams_obj));
			lBeam_sprt.x -= lParentPosition_pt.x;
			lBeam_sprt.y -= lParentPosition_pt.y;

			this._fBeams_sprt_arr.push(lBeam_sprt);
		}
	}

	_createBeam(aBeamParams_obj)
	{
		let lBeam_sprt = APP.library.getSpriteFromAtlas('boss_mode/golem/appearance_beam');

		lBeam_sprt.anchor.set(0.5, 0.05);
		lBeam_sprt.position.set(aBeamParams_obj.x/2, aBeamParams_obj.y/2);
		lBeam_sprt.rotation = Utils.gradToRad(aBeamParams_obj.angle);
		lBeam_sprt.alpha = aBeamParams_obj.alpha;
		lBeam_sprt.scale.set(aBeamParams_obj.scaleX, aBeamParams_obj.scaleY);

		lBeam_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		Sequence.start(lBeam_sprt, aBeamParams_obj.scaleSeq);
		Sequence.start(lBeam_sprt, aBeamParams_obj.alphaSeq);

		lBeam_sprt.zIndex = 1000 - aBeamParams_obj.id;

		return lBeam_sprt;
	}

	_startSmokesAnimation()
	{
		let lParentPosition_pt = this.parent.position;
		for (let lSmokeParams_obj of SMOKES)
		{
			let lSmoke_sprt = this.addChild(this._createSmoke(lSmokeParams_obj));
			lSmoke_sprt.x -= lParentPosition_pt.x;
			lSmoke_sprt.y -= lParentPosition_pt.y;

			this._fSmokes_sprt_arr.push(lSmoke_sprt);
		}

		//special smoke...
		let lSpecialSmoke_sprt = this.addChild(APP.library.getSpriteFromAtlas('common/transition_smoke_fx_unmult'));
		lSpecialSmoke_sprt.position.set(1090/2 - lParentPosition_pt.x, 514/2 - lParentPosition_pt.y);
		lSpecialSmoke_sprt.alpha = 0;
		lSpecialSmoke_sprt.scale.set(3.93 * 0.195);
		lSpecialSmoke_sprt.rotation = Utils.gradToRad(-60);

		let seq = [
			{
				tweens: [],
				duration: 60 * FRAME_RATE,
				onfinish: () => {
					lSpecialSmoke_sprt.alpha = 1;
				}
			},
			{
				tweens: [
					{ prop: "scale.x", to: 3.93 * 0.352 },
					{ prop: "scale.y", to: 3.93 * 0.352 },
					{ prop: "rotation", to: Utils.gradToRad(-82)}
				],
				duration: 22 * FRAME_RATE,
				onfinish: (e) => {
					e.target.obj.destroy();
				}
			}
		];
		Sequence.start(lSpecialSmoke_sprt, seq);

		lSpecialSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fSmokes_sprt_arr.push(lSpecialSmoke_sprt);
		//...special smoke
	}

	_createSmoke(aSmokeParams_obj)
	{
		let lSmoke_sprt = APP.library.getSpriteFromAtlas('common/transition_smoke_fx_unmult');

		lSmoke_sprt.position.set(aSmokeParams_obj.x/2, aSmokeParams_obj.y/2);

		lSmoke_sprt.alpha = 0;
		lSmoke_sprt.rotation = Utils.gradToRad(-4);

		let lScaleMultiplier_num = aSmokeParams_obj.scale || 1;
		lSmoke_sprt.scale.set(3.93 * 0.18 * lScaleMultiplier_num);

		let scaleSeq = [
			{
				tweens: [
					{ prop: "scale.x", to: 3.93 * 0.36 * lScaleMultiplier_num },
					{ prop: "scale.y", to: 3.93 * 0.36 * lScaleMultiplier_num},
					{ prop: "rotation", to: Utils.gradToRad(-29)}
				],
				duration: 24 * FRAME_RATE
			}
		];

		let alphaSeq = [
			{
				tweens: [ { prop: "alpha", to: 1 } ],
				duration: 2 * FRAME_RATE
			},
			{
				tweens: [ { prop: "alpha", to: 0 } ],
				duration: 22 * FRAME_RATE,
				onfinish: (e) => {
					e.target.obj.destroy();
				}
			}
		];

		Sequence.start(lSmoke_sprt, scaleSeq, (4 + 48 - aSmokeParams_obj.id) * FRAME_RATE);
		Sequence.start(lSmoke_sprt, alphaSeq, (4 + 48 - aSmokeParams_obj.id) * FRAME_RATE);

		lSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

		return lSmoke_sprt;
	}

	_prepareShakeTheGround()
	{
		this._fShakeTimer_tmr = new Timer(this._shakeTheGround.bind(this), 10 * FRAME_RATE);
	}

	_shakeTheGround()
	{
		this.emit(AppearanceView.EVENT_SHAKE_THE_GROUND_REQUIRED);
	}

	_prepareSounds()
	{
		this._fSoundsTimers_tmr_arr.push(new Timer( () => { APP.soundsController.play("mq_boss_golem_cry") }, 50 * FRAME_RATE));
	}

	_prepareExplosions()
	{
		this._fStartExplosionsTimer_tmr = new Timer(this._startExplosions.bind(this), 62 * FRAME_RATE);
	}

	_startExplosions()
	{
		APP.soundsController.play('mq_boss_golem_explosion');

		this._fStartExplosionsTimer_tmr && this._fStartExplosionsTimer_tmr.destructor();
		this._fStartExplosionsTimer_tmr = null;

		let lParentPosition_pt = this.parent.position;

		this._startExplosion(new PIXI.Point(-lParentPosition_pt.x, -lParentPosition_pt.y + 50), 0.8);
		this._startExplosion(new PIXI.Point(-lParentPosition_pt.x + 50, -lParentPosition_pt.y + 50), 1);
	}

	_startExplosion(aPosition_pt, aScale_num)
	{

		let lExplosionContainer_sprt = this.addChild(new Sprite());
		lExplosionContainer_sprt.position.set(aPosition_pt.x, aPosition_pt.y);
		lExplosionContainer_sprt.scale.set(aScale_num);


		CommonEffectsManager.getGroundSmokeTextures();
		let lGroundSmoke_sprt = lExplosionContainer_sprt.addChild(new Sprite);
		lGroundSmoke_sprt.textures = CommonEffectsManager.textures['groundSmoke'];
		lGroundSmoke_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lGroundSmoke_sprt.scale.set(4 * 1.61);
		lGroundSmoke_sprt.position.set(1018/2, 591/2 - 40);
		lGroundSmoke_sprt.tint = 0x55402f;
		lGroundSmoke_sprt.once("animationend", () => {
			lGroundSmoke_sprt.destroy();
		});
		lGroundSmoke_sprt.play();

		DeathFxAnimation.initTextures();
		let lGroundHitTextures_arr = DeathFxAnimation.getGroundHitFxTextures();
		for (let lGroundHitParams_obj of GROUND_HITS)
		{
			let lGroundHit_sprt = new Sprite();
			lGroundHit_sprt.textures = lGroundHitTextures_arr;
			lGroundHit_sprt.once("animationend", () => {
				lGroundHit_sprt.destroy();
			});

			lGroundHit_sprt.position.set(lGroundHitParams_obj.x/2, lGroundHitParams_obj.y/2);
			lGroundHit_sprt.scale.set(lGroundHitParams_obj.scale * 2);
			lGroundHit_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
			lGroundHit_sprt.rotation = Utils.gradToRad(lGroundHitParams_obj.angle);
			lGroundHit_sprt.play();

			lExplosionContainer_sprt.addChild(lGroundHit_sprt);
		}

	}

	_showPuffs()
	{
		DeathFxAnimation.initSmokePuffTextures();

		for (let lPuffParams_obj of PUFFS)
		{
			this._fPuffs_sprt_arr.push(this._showPuff(lPuffParams_obj));
		}
	}

	_showPuff(aPuffParams_obj)
	{
		let lParentPosition_pt = this.parent.position;
		let textures = DeathFxAnimation.getSmokePuffTextures();

		let lPuff_sprt = new Sprite;
		lPuff_sprt.textures = textures;
		lPuff_sprt.position.set(aPuffParams_obj.x/2 - lParentPosition_pt.x, aPuffParams_obj.y/2 - lParentPosition_pt.y);

		let seq = [{tweens:[], duration: aPuffParams_obj.startFrame * FRAME_RATE, onfinish: () => {
			this.addChild(lPuff_sprt);
			lPuff_sprt.play();
			lPuff_sprt.once('animationend', () => {
				Sequence.destroy(Sequence.findByTarget(lPuff_sprt));
				lPuff_sprt.destroy();
			})
		}}];

		if (aPuffParams_obj.startScale)
		{
			lPuff_sprt.scale.set(aPuffParams_obj.startScale * 2);
			seq.push({tweens:[
				{ prop: "scale.x", to: aPuffParams_obj.endScale * 2 },
				{ prop: "scale.y", to: aPuffParams_obj.endScale * 2 }
			],
			duration: (aPuffParams_obj.endFrame - aPuffParams_obj.startFrame)*FRAME_RATE
			})
		}
		else
		{
			lPuff_sprt.scale.set(aPuffParams_obj.startScaleX * 2, aPuffParams_obj.startScaleY * 2);
			seq.push(
				{ tweens: [
					{ prop: "scale.x", to: aPuffParams_obj.endScaleX * 2 },
					{ prop: "scale.y", to: aPuffParams_obj.endScaleY * 2 }
				],
				duration: (aPuffParams_obj.endFrame - aPuffParams_obj.startFrame)*FRAME_RATE
				});
		}

		Sequence.start(lPuff_sprt, seq);

		lPuff_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		return lPuff_sprt;
	}

	_startFlyingSmallRocks()
	{
		//let lParentPosition_pt = this.parent.position;
		for (let i=0; i < ROCKS.length; i++)
		{
			if (!APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
			{
				if (i % 3) continue;
			}
			let lRockParams_obj = ROCKS[i];
			let lStartFrame_int = lRockParams_obj.startFrame;
			let lRockTimer_tmr = new Timer( () => {
				let lStartPoint_pt = new PIXI.Point( lRockParams_obj.x / 4 + 220, lRockParams_obj.y / 2 + 50 + Utils.random(-5, 5, true) );
				//let lStartPoint_pt = new PIXI.Point(lRockParams_obj.x/4 + 250, lRockParams_obj.y/4 + 180);
				// lStartPoint_pt.x -= lParentPosition_pt.x;
				// lStartPoint_pt.y -= lParentPosition_pt.y;
				let lDirection_int = Math.random() > 0.5 ? 1 : -1; //lStartPoint_pt.x > 960 ? 1 : -1;
				for (let j=0; j<3; j++)
				{
					let lDeltaX_num = Utils.random(1, 50, true) * lDirection_int;
					let lEndPoint_pt = new PIXI.Point( lStartPoint_pt.x + lDeltaX_num,  lStartPoint_pt.y + Utils.random(-5, 5, true));
					let points = [lStartPoint_pt, lEndPoint_pt];
					let lRockFlyingDebris_rfd = new RockFlyingDebris(
						{maxHeight: 120, minReboundHeight: 5, maxReboundHeight: 20, lastOffsetDistanceMult: 0.5, lastRotationDelta: 0},
						points,
						null); //TODO to remove debris?
					this._addChildSpecial(lRockFlyingDebris_rfd);
				}

				//smoke 1...
				let lSmoke_sprt = this._addChildSpecial(APP.library.getSpriteFromAtlas('common/transition_smoke_fx_unmult'));
				lSmoke_sprt.position.set(lStartPoint_pt.x, lStartPoint_pt.y);
				lSmoke_sprt.alpha = 0;
				lSmoke_sprt.scale.set(3.93 * 0.181);

				let seq = [
					{ tweens: [{prop: "alpha", to: 1}], duration: 2*FRAME_RATE },
					{ tweens: [{prop: "alpha", to: 0}], duration: 22*FRAME_RATE }
				];
				Sequence.start(lSmoke_sprt, seq);

				lSmoke_sprt.rotateTo(Utils.gradToRad(-25), 24 * FRAME_RATE);
				lSmoke_sprt.scaleTo(3.93 * 0.352, 24 * FRAME_RATE);
				lSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

				this._fSmokes_sprt_arr.push(lSmoke_sprt);
				//...smoke 1

				//smoke 2...
				CommonEffectsManager.getGroundSmokeTextures();
				let lGroundSmoke_sprt = this._addChildSpecial(new Sprite);
				lGroundSmoke_sprt.position.set(lStartPoint_pt.x, lStartPoint_pt.y);
				lGroundSmoke_sprt.textures = CommonEffectsManager.textures['groundSmoke'];
				lGroundSmoke_sprt.tint = 0x55402f;
				lGroundSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
				lGroundSmoke_sprt.scale.set(4 * 0.45);
				lGroundSmoke_sprt.once("animationend", () => {
					lGroundSmoke_sprt.destroy();
				});
				lGroundSmoke_sprt.play();
				//...smoke 2

			}, lStartFrame_int * FRAME_RATE);
			this._fRockTimers_arr.push(lRockTimer_tmr);
		}
	}

	_prepareFlyingBigRocks()
	{
		let lBigRocksTimer_tmr = new Timer(this._startFlyingBigRocks.bind(this), 63 * FRAME_RATE);
		this._fRockTimers_arr.push(lBigRocksTimer_tmr);
	}

	_startFlyingBigRocks()
	{
		//let lParentPosition_pt = this.parent.position;
		for (let lBigRockParams_obj of BIG_ROCKS)
		{
			let lStartPoint_pt = new PIXI.Point(lBigRockParams_obj.startX/2, lBigRockParams_obj.startY/2);
			let lEndPoint_pt = new PIXI.Point(lBigRockParams_obj.endX/2, lBigRockParams_obj.endY/2);
			// lStartPoint_pt.x -= lParentPosition_pt.x;
			// lStartPoint_pt.y -= lParentPosition_pt.y;
			// lEndPoint_pt.x -= lParentPosition_pt.x;
			// lEndPoint_pt.y -= lParentPosition_pt.y;
			let lBigRock_sprt = APP.library.getSpriteFromAtlas('boss_mode/golem/rocks/rock_0');
			lBigRock_sprt.scale.set(0.494 * 0.26, 1 * 0.33);
			lBigRock_sprt.rotation = Utils.gradToRad(87 - 92.5);
			lBigRock_sprt.position.set(lStartPoint_pt.x, lStartPoint_pt.y);
			lBigRock_sprt.moveTo(lEndPoint_pt.x, lEndPoint_pt.y, 23 * FRAME_RATE, Easing.cubic.easeOut);
			lBigRock_sprt.rotateTo(Utils.gradToRad(2*275.2), 23 * FRAME_RATE);
			lBigRock_sprt.scaleXTo(lBigRock_sprt.scale.x * 1.1, 23 * FRAME_RATE);
			lBigRock_sprt.scaleXTo(lBigRock_sprt.scale.y * 1.1, 23 * FRAME_RATE);

			this._addChildSpecial(lBigRock_sprt);
		}

		for (let i=0; i<4; i++)
		{
			let lStartPoint_pt = new PIXI.Point(1044/2, 603/2);
			lStartPoint_pt.x += Utils.random(-30, 30, true);
			let lEndPoint_pt = Utils.clone(lStartPoint_pt);
			let lDeltaX_num = 100 + Utils.random(0, 30, true);
			lDeltaX_num *= Math.random() > 0.5 ? 1 : -1;
			lEndPoint_pt.x += lDeltaX_num;

			// lStartPoint_pt.x -= lParentPosition_pt.x;
			// lStartPoint_pt.y -= lParentPosition_pt.y;
			// lEndPoint_pt.x -= lParentPosition_pt.x;
			// lEndPoint_pt.y -= lParentPosition_pt.y;

			let points = [lStartPoint_pt, lEndPoint_pt];

			let lRockIndex_int = i === 0 ? 0 : 2;
			let lAdditionalScale_obj = i === 0 ? {x: 0.395, y: 0.5} : null;

			let lBigRock_rfd = new RockFlyingDebris({index: lRockIndex_int, additionalScale: lAdditionalScale_obj}, points, null);
			this._addChildSpecial(lBigRock_rfd);
		}
	}
	//...ANIMATION

	_addChildSpecial(aChild_obj)
	{
		let lSpecialChild_sprt = this.parent.parent.addChild(aChild_obj);
		this._fSpecialChildren_sprt_arr.push(lSpecialChild_sprt);
		return lSpecialChild_sprt;
	}

	destroy()
	{
		for (let lCrack_sprt of this._fCracks_sprt_arr)
		{
			Sequence.destroy(Sequence.findByTarget(lCrack_sprt));
		}
		for (let lBeam_sprt of this._fBeams_sprt_arr)
		{
			Sequence.destroy(Sequence.findByTarget(lBeam_sprt));
		}
		for (let lSmoke_sprt of this._fSmokes_sprt_arr)
		{
			Sequence.destroy(Sequence.findByTarget(lSmoke_sprt));
		}
		for (let lPuff_sprt of this._fPuffs_sprt_arr)
		{
			Sequence.destroy(Sequence.findByTarget(lPuff_sprt));
		}
		for (let lRockTimer_tmr of this._fRockTimers_arr)
		{
			lRockTimer_tmr && lRockTimer_tmr.destructor();
			lRockTimer_tmr = null;
		}
		for (let lSoundTimer_tmr of this._fSoundsTimers_tmr_arr)
		{
			lSoundTimer_tmr && lSoundTimer_tmr.destructor();
			lSoundTimer_tmr = null;
		}

		while (this._fSpecialChildren_sprt_arr.length > 0)
		{
			let lSpecialChild_sprt = this._fSpecialChildren_sprt_arr.pop();
			lSpecialChild_sprt && lSpecialChild_sprt.destroy();
		}
		this._fCracks_sprt_arr = null;
		this._fBeams_sprt_arr = null;
		this._fSmokes_sprt_arr = null;
		this._fRockTimers_arr = null;

		this._fShakeTimer_tmr && this._fShakeTimer_tmr.destructor();
		this._fShakeTimer_tmr = null;

		this._fStartExplosionsTimer_tmr && this._fStartExplosionsTimer_tmr.destructor();
		this._fStartExplosionsTimer_tmr = null;

		super.destroy();
	}
}

class Crack extends Sprite {

	constructor(aCrackParams_obj)
	{
		super();

		this._fParams_obj = aCrackParams_obj;
		this._fCrack_sprt = null;
		this._fMask_sprt = null;

		this._initView();
	}

	_initView()
	{
		if (APP.isPixiHeavenLibrarySupported)
		{
			let lCrack_sprt = new PIXI.heaven.Sprite(APP.library.getSpriteFromAtlas('boss_mode/golem/crack').textures[0]);
			lCrack_sprt.anchor.set(0.5, 0.5);
			lCrack_sprt.scale.set(2 * this._fParams_obj.scaleX, 2 * this._fParams_obj.scaleY);

			this.position.set(this._fParams_obj.x/2, this._fParams_obj.y/2);

			lCrack_sprt.rotation = Utils.gradToRad(this._fParams_obj.angle);

			this._fCrack_sprt = lCrack_sprt;

			let lBounds_rect = lCrack_sprt.getBounds();

			let lMask_sprt = new Sprite();

			let lGraphics_gr = new PIXI.Graphics();
			lGraphics_gr.beginFill(0xffffff);
			lGraphics_gr.drawRect(lBounds_rect.x, lBounds_rect.y, lBounds_rect.width, lBounds_rect.height);
			lGraphics_gr.endFill();
			lMask_sprt.addChild(lGraphics_gr);

			let l_txtr = APP.stage.renderer.generateTexture(lMask_sprt, PIXI.SCALE_MODES.LINEAR, 2, lBounds_rect);
			lMask_sprt = new PIXI.Sprite(l_txtr);
			lMask_sprt.convertToHeaven();

			if (this._fParams_obj.maskDirection === 1)
			{
				lMask_sprt.anchor.set(0.5, 0.5);
			}
			else
			{
				lMask_sprt.anchor.set(0, 0.5);
				lMask_sprt.position.x = -lBounds_rect.width/2;
			}

			this._fMask_sprt = lMask_sprt;
			this._fMask_sprt.alpha = 0;

			this.addChild(this._fCrack_sprt);
			this.addChild(this._fMask_sprt);

			this._fCrack_sprt.maskSprite = this._fMask_sprt;
			this._fCrack_sprt.pluginName = 'batchMasked';
			this._fMask_sprt.renderable = false;

			this._fMask_sprt.scale.x = 0;
			let seq = [
				{
					tweens: [],
					duration: this._fParams_obj.maskStartFrame * FRAME_RATE
				},
				{
					tweens: [
						{prop: 'scale.x', to: 1}
					],
					duration: (this._fParams_obj.maskEndFrame - this._fParams_obj.maskStartFrame) * FRAME_RATE,
					ease:Easing.sine.easeOut,
					onfinish: () => {
						this._fCrack_sprt.maskSprite = null;
						this._fMask_sprt.destroy({children: true, texture: true, baseTexture: true});
						this._fMask_sprt = null;
					}
				}
			];
			Sequence.start(this._fMask_sprt, seq);
		}
		else
		{
			let lCrack_sprt = APP.library.getSpriteFromAtlas('boss_mode/golem/crack');
			lCrack_sprt.anchor.set(0.5, 0.5);
			lCrack_sprt.scale.set(2 * this._fParams_obj.scaleX, 2 * this._fParams_obj.scaleY);

			this.position.set(this._fParams_obj.x/2, this._fParams_obj.y/2);

			lCrack_sprt.rotation = Utils.gradToRad(this._fParams_obj.angle);

			this._fCrack_sprt = lCrack_sprt;
			this.addChild(this._fCrack_sprt);

			this._fCrack_sprt.alpha = 0;

			let seq = [
				{
					tweens: [],
					duration: this._fParams_obj.maskStartFrame * FRAME_RATE
				},
				{
					tweens: [
						{prop: 'alpha', to: 1}
					],
					duration: (this._fParams_obj.maskEndFrame - this._fParams_obj.maskStartFrame) * FRAME_RATE
				}
			];
			Sequence.start(this._fCrack_sprt, seq);
		}

		let fadeOutSeq = [
			{
				tweens: [],
				duration: this._fParams_obj.fadeOutStartFrame * FRAME_RATE
			},
			{
				tweens: [
					{ prop: 'alpha', to: 0 }
				],
				duration: (this._fParams_obj.fadeOutEndFrame - this._fParams_obj.fadeOutStartFrame) * FRAME_RATE,
				onfinish: () => {
					this.destroy();
				}
			}
		]
		Sequence.start(this._fCrack_sprt, fadeOutSeq);

		this._fCrack_sprt.blendMode = PIXI.BLEND_MODES.ADD;

	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fCrack_sprt));
		Sequence.destroy(Sequence.findByTarget(this._fMask_sprt));
		Sequence.destroy(Sequence.findByTarget(this));

		this._fParams_obj = null
		this._fMask_sprt = null;
		this._fCrack_sprt = null;

		super.destroy();
	}
}

export default GolemAppearanceView;