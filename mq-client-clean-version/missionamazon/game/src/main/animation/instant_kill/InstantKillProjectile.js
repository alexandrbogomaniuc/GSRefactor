import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Lightning from './Lightning';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import EternalPlazmaSmoke from './EternalPlazmaSmoke';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class InstantKillProjectile extends Sprite
{
	static get SCALE_COMPLETED() { return "scaleCompleted"; }
	static get ANIMATION_COMPLETED() { return "animationCompleted"; }

	constructor()
	{
		super();		
		
		this.smokes = null;
		this.distance = undefined;
		this.underSmoke = null;
		this.beamPlasma = null;
		this.beamWhite = null;
		this.finalScaleX = undefined;
		this.lightning = null;
		this.beamLightning = null;

		this.createView();
	}
	
	createView()
	{		
		this.smokes = [];
	}

	shoot(distance)
	{
		this.distance = distance;

		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.underSmoke = this.addChild(new EternalPlazmaSmoke(true));
			this.underSmoke.position.set(this.distance/16, 0);
			this.underSmoke.scale.y = 2.73*2;
			this.underSmoke.scale.x = 4.1*2;
			this.smokes.push(this.underSmoke);
		}

		this.beamPlasma = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/plasmabeam"));
		this.beamPlasmaInitialWidth = this.beamPlasma.width;
		this.beamPlasma.blendMode = PIXI.BLEND_MODES.ADD;
		this.beamPlasma.anchor.set(0.1, 0.56);		
		this.beamPlasma.scale.x = 0;
		this.beamPlasma.scale.y = 0.59*2;
		this.beamPlasma.scaleXTo(this.distance/this.beamPlasmaInitialWidth, 3*16.6, null, this.onScaleCompleted.bind(this), this.onScaleChange.bind(this));

		this.beamWhite = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/plasmabeam_white"));
		this.beamWhiteInitialWidth = this.beamWhite.width;
		this.beamWhite.blendMode = PIXI.BLEND_MODES.ADD;
		this.beamWhite.anchor.set(0, 0.5);		
		this.beamWhite.scale.x = 0;
		this.beamWhite.scale.y = 2;
		this.beamWhite.scaleXTo(this.distance/this.beamWhiteInitialWidth, 3*16.6);

		this.finalScaleX = this.distance/this.beamWhiteInitialWidth;

		this.beamLightning = new Lightning();
		this.beamLightning.scale.set(3, 1);
		this.beamLightning.position.set(-1598, 0);
		this.beamLightning.updatePivot();
		this.beamWhite.addChild(this.beamLightning);
	}

	onScaleChange(data)
	{
		var pt = {x: this.distance/4 * data.position, y: 0};
		let smoke = new EternalPlazmaSmoke();
		smoke.scale.set(1.41*2);
		smoke.position.set(pt.x, pt.y);
		this.addChildAt(smoke, 0);
		this.smokes.push(smoke);
	}

	onScaleCompleted()
	{
		this.emit(InstantKillProjectile.SCALE_COMPLETED);
		this.showLightnings();

		var sequence = [
			{	tweens: [],
				duration: 5*16.6	
			},
			{	tweens: [	{prop: "scale.y", to: 1.61*2},
					  		{prop: "alpha", to: 0.5}	],
				duration: 1.6*16.6,
				onfinish: ()=>{this.startFadeOut();}
			},
			{	tweens: [	{prop: "scale.y", to: 3.06*2},
					  		{prop: "alpha", to: 0}	],
				duration: 1.6*16.6				
			}
		]

		Sequence.start(this.beamPlasma, sequence);
	}

	startFadeOut()
	{		
		this.lightning.destroy();
		this.beamWhite.destroy();

		var sequence = [			
			{	tweens: [{prop: "alpha", to: 0}	],
				duration: 1.6*2*16.6,
				onfinish: ()=>{ this.complete();}
			}
		]
		while (this.smokes.length > 0)
		{
			Sequence.start(this.smokes.pop(), sequence);
		}
	}	

	showLightnings()
	{
		this.lightning = new Lightning();
		this.lightning.scale.x = this.finalScaleX;
		this.lightning.updatePivot();
		this.addChild(this.lightning);
	}

	complete()
	{
		this.emit(InstantKillProjectile.ANIMATION_COMPLETED);
		this.destroy();
	}

	destroy()
	{
		if (this.beamPlasma)
		{
			Sequence.destroy(Sequence.findByTarget(this.beamPlasma));
		}

		if (this.smokes)
		{
			while (this.smokes.length > 0)
			{
				Sequence.destroy(Sequence.findByTarget(this.smokes.pop()));
			}
		}

		this.beamLightning = null;
		this.smokes = null;
		this.distance = undefined;
		this.underSmoke = null;
		this.beamPlasma = null;
		this.beamWhite = null;
		this.finalScaleX = undefined;
		this.lightning = null;

		super.destroy();
	}
}

export default InstantKillProjectile;