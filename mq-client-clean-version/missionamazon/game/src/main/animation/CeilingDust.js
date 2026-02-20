import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CommonEffectsManager from '../CommonEffectsManager';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const CEILING_DUST_PARAMS =[
	{ scaleX: 3.21, scaleY: 1.26, rotation: 88, x: 471, y: -65, startDelay: 12},
	{ scaleX: 4.69, scaleY: 1.84, rotation: 87, x: 1541, y: -88, startDelay: 0},	
	{ scaleX: 4.69, scaleY: 1.84, rotation: 87, x: 469.3, y: -88, startDelay: 0},
	{ scaleX: 2.85, scaleY: 2.49, rotation: 87, x: 45, y: -46, startDelay: 9},
	{ scaleX: 2.85, scaleY: 2.49, rotation: 87, x: 45, y: -46, startDelay: 9}
];

class CeilingDust extends Sprite {

	constructor(aWithDebris_bl = true) {
		super();

		this._fWithDebris_bl = aWithDebris_bl;
		this._timers = [];

		this._start();
	}

	_start() {
		CommonEffectsManager.getGroundSmokeTextures();

		this.randomOffset = 300 - Utils.random(0, 600);

		for (let params of CEILING_DUST_PARAMS) {
			let timer = new Timer(() => this._createDust(params), params.startDelay * 2 * 16.6);
			this._addTimer(timer);
		}

		if (this._fWithDebris_bl)
		{
			this._createDebris();
		}
	}

	_createDust(params) {
		let textures = CommonEffectsManager.textures['groundSmoke'];
		let groundSmoke = this.addChild(new Sprite);
		groundSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		groundSmoke.textures = textures;

		groundSmoke.rotation = Utils.gradToRad(params.rotation);
		groundSmoke.position.set((params.x + this.randomOffset - 960)/2, (params.y - 540)/2);
		groundSmoke.scale.x = params.scaleX*2;
		groundSmoke.scale.y = params.scaleY*2;

		groundSmoke.play();
		groundSmoke.once('animationend', (e) =>{			
			groundSmoke.destroy();
			if (this.children.length == 0) {
				this.emit('animationFinish');
				this.destroy();
			}
		});	
	}

	_createDebris(){
		let n = Utils.random(10, 15);
		for (let i=0; i<n; i++) {
			let params = {};
			params.scale = Utils.random(0.29, 0.67, true);
			params.rotation = Utils.random(0, 360);
			params.y = -540;
			params.x = (960/2 - Utils.random(0, 960));
			params.startDelay = Utils.random(0, 10);
			params.duration = 21 + Utils.random(0, 50);
			
			let timer = new Timer(() => this._createOnePieceOfDebris(params), params.startDelay * 2 * 16.6);
			this._addTimer(timer);
		}
	}

	_createOnePieceOfDebris(params){
		let debris = this.addChild(APP.library.getSpriteFromAtlas('weapons/GrenadeGun/debris/debris_0'));
		debris.scale.set(params.scale);		
		debris.position.set(params.x, params.y);

		debris.moveTo(params.x, 540, 21*2*16.6, Easing.sine.sineIn, (e) => {
			debris.destroy();
		});

		debris.rotation = Utils.gradToRad(params.rotation);
		let sign = Math.random() > 0.5 ? 1 : -1;
		debris.rotateBy(debris.rotation + Utils.gradToRad(360) * sign, params.duration*2*16.6);
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

	destroy()
	{
		this._fWithDebris_bl = undefined;
		this.randomOffset = undefined;

		this._removeTimers();
		this._timers = null;

		super.destroy();
	}

}

export default CeilingDust;