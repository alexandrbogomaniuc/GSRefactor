import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { AtlasSprite } from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import AtlasConfig from './../../config/AtlasConfig';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import TorchFxAnimation from './TorchFxAnimation';
import SmokeRisingAnimation from './SmokeRisingAnimation';

const GROUND_BURNS_CONFIG = [
	{
		scale: {x: 4.09, y: 4.09/1.4},
		position: {x: -240, y: 135}
	},
	{
		scale: {x: 4.09, y: 4.09/1.4},
		position: {x: -100, y: 70}
	},
	{
		scale: {x: 2.72, y: 2.72/1.4},
		position: {x: 0, y: 0}
	},
	{
		scale: {x: 2.72, y: 2.72/1.4},
		position: {x: 20, y: 60}
	},
	{
		scale: {x: 2.72, y: 2.72/1.4},
		position: {x: 130, y: -90}
	},
	{
		scale: {x: 2.07, y: 2.07/1.4},
		position: {x: 200, y: -140}
	},
	{
		scale: {x: 2.07, y: 2.07/1.4},
		position: {x: 240, y: -200}
	}
];

const FIRES_CONFIG = [
	{
		scale: {x: 0.7, y: 0.7},
		position: {x: 5, y: 15}
	},
	{
		scale: {x: 0.7, y: 0.72},
		position: {x: 0, y: 14}
	},
	{
		scale: {x: 0.7, y: 0.7},
		position: {x: 140, y: -90}
	},
	{
		scale: {x: 0.7, y: 0.66},
		position: {x: 150, y: -92}
	},
	{
		scale: {x: 0.7, y: 0.67},
		position: {x: 240, y: -170}
	},
	{
		scale: {x: 0.7, y: 0.7},
		position: {x: 245, y: -173}
	}
];

const SMOKES_CONFIG = [
	{
		scale: {x: 4, y: 4},
		position: {x: -310, y: -181}
	},
	{
		scale: {x: 4, y: 4},
		position: {x: 0, y: 30}
	},
	{
		scale: {x: 4, y: 4},
		position: {x: 280, y: 141}
	},
	{
		scale: {x: 4, y: 4},
		position: {x: 0, y: 0}
	},
	{
		scale: {x: 4, y: 4},
		position: {x: -256, y: 155}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: -278, y: 168}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: -166, y: 93}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: -81, y: 57}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: 10, y: 43}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: 87, y: -8}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: 140, y: -90}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: 185, y: -123}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: 239, y: -196}
	},
	{
		scale: {x: 0.6, y: 0.6},
		position: {x: 274, y: -229}
	},
];

class GroundFireTraceAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_FINISHED()			{return "onDragonFireAnimationFinished";}

	startAnimation()
	{
		this._startAnimation();
	}

	constructor()
	{
		super();

		TorchFxAnimation.initTextures();

		this._animsCounter = 0;
		this._animations = [];
		this._smokes = [];
	}

	_startAnimation()
	{
		this._startGroundBurns();
		this._startFires();
		this._startSmokes();
	}

	_startGroundBurns()
	{
		for (let config of GROUND_BURNS_CONFIG)
		{
			this._startGroundBurn(config.position, config.scale);
		}
	}

	_startGroundBurn(position, scale)
	{
		let groundburn = this.addChild(APP.library.getSpriteFromAtlas("common/groundburn"));
		groundburn.blendMode = PIXI.BLEND_MODES.MULTIPLY;
		groundburn.position.set(position.x, position.y);
		groundburn.scale.set(scale.x, scale.y);
		groundburn.rotation = Utils.gradToRad(-40);

		++this._animsCounter;
		let seq = [
			{tweens:[],							duration: 147*FRAME_RATE},
			{tweens:[{ prop: "alpha", to: 0 }],	duration: 12*FRAME_RATE, onfinish: () => {
				groundburn && groundburn.destroy();
				--this._animsCounter;
				this._tryToFinishAnimation();
			}}
		];

		this._animations.push(groundburn);
		Sequence.start(groundburn, seq);
	}

	_startFires()
	{
		for (let config of FIRES_CONFIG)
		{
			this._startFlame(config.position, config.scale);
		}
	}

	_startFlame(position, scale)
	{
		let flame = this.addChild(new Sprite());
		flame.textures = TorchFxAnimation.textures.torch;
		flame.blendMode = PIXI.BLEND_MODES.ADD;
		flame.position.set(position.x, position.y);
		flame.scale.set(scale.x, scale.y);
		flame.anchor.set(0.5, 1);
		++this._animsCounter;
		let iterator = 4;
		flame.on('animationend', () => {
			if (--iterator == 0)
			{
				let seq = [
					{tweens:[{prop: "scale.x", to: 0.2}, {prop: "scale.y", to: 0}],	duration: 10*FRAME_RATE, onfinish: () => {
						flame && flame.destroy();
						--this._animsCounter;
						this._tryToFinishAnimation();
					}}
				];
		
				this._animations.push(flame);
				Sequence.start(flame, seq);
			}
		});
		flame.play();
	}

	_startSmokes()
	{
		for (let config of SMOKES_CONFIG)
		{
			this._startSmoke(config.position, config.scale);
		}
	}

	_startSmoke(position, scale)
	{
		let smoke = this.addChild(new SmokeRisingAnimation());
		smoke.position.set(position.x, position.y);
		smoke.scale.set(scale.x, scale.y);
		this._smokes.push(smoke);
		++this._animsCounter;
		smoke.once(SmokeRisingAnimation.EVENT_ON_ANIMATION_ENDED, this._onSmokeEnded, this);
		smoke.startAnimation();
	}

	_onSmokeEnded(e)
	{
		if (this._smokes)
		{
			let id = this._smokes.indexOf(e.target)
			if (~id)
			{
				this._smokes.splice(id, 1);
				--this._animsCounter;
			}
		}

		this._tryToFinishAnimation();
	}

	_tryToFinishAnimation()
	{
		if (this._animsCounter <= 0)
		{
			this._onAnimationFinished();
		}
	}

	_onAnimationFinished()
	{
		this.emit(GroundFireTraceAnimation.EVENT_ON_ANIMATION_FINISHED);
	}

	destroy()
	{
		if (this._animations)
		{
			for (let anim of this._animations)
			{
				if (anim)
				{
					Sequence.destroy(Sequence.findByTarget(anim));
					anim.destroy();
				}
			}
		}

		if (this._smokes)
		{
			while (this._smokes.length)
			{
				let smoke = this._smokes.pop();
				smoke.off(SmokeRisingAnimation.EVENT_ON_ANIMATION_ENDED, this._onSmokeEnded, this);
				smoke.destroy();
			}
		}

		super.destroy();

		this._animsCounter = null;
		this._animations = null;
		this._smokes = null;
	}
}
export default GroundFireTraceAnimation;