import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import EternalPlazmaSmoke from './EternalPlazmaSmoke';
import InstantKillSpinner from './InstantKillSpinner';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Lightning from './Lightning';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const LIGHTNING_ROTATIONS = [-54, -107, -77];

class InstantKillAtomizer extends Sprite 
{
	static get EVENT_ON_ANIMATION_COMPLETED()						{return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor()
	{
		super();

		this.plasmaSmoke = null;
		this.spinner = null;
		this.spinnerRepeatCounter = undefined;

		this.createView();
	}

	createView()
	{
		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			//indigo smoke...
			this.plasmaSmoke = this.addChild(new EternalPlazmaSmoke(true /*indigo style*/));
			this.plasmaSmoke.rotation = Utils.gradToRad(18.4);
			this.plasmaSmoke.scale.set();
			//...indigo smoke
			
			//common smokes...
			let smoke1 = new EternalPlazmaSmoke(true /*indigo style*/, "add");
			smoke1.scale.set(1.58*0.56, 2.36*0.56);
			this.addChild(smoke1);

			let smoke2 = new EternalPlazmaSmoke(true);
			smoke2.scale.set(2*0.594, 2*0.889);
			smoke2.rotation = Utils.gradToRad(11.7);
			smoke2.y = -30;
			this.addChild(smoke2);
			//...common smokes
		}
		
		//spinner...
		this.spinner = this.addChild(new InstantKillSpinner);
		this.spinner.y = -100;
		this.spinner.rotation = Utils.gradToRad(-20);
		this.spinner.scale.set(0);
		this.spinner.scaleYTo(0.774, 40*16.6);
		this.spinner.scaleXTo(0.545, 40*16.6);
		this.spinnerRepeatCounter = 0;
		//...spinner

		var sequence = [
			{	tweens: [],
				duration: 13*16.6,
				onfinish: () => {
					APP.profilingController.info.isVfxProfileValueMediumOrGreater && this.showLigntnings();
				}
			},
			{
				tweens: [],
				duration: 16*16.6,
				onfinish: () => {
					this._onAnimationCompleted();
				}
			}
		]
		Sequence.start(this, sequence);
	}

	_onAnimationCompleted()
	{
		this.emit(InstantKillAtomizer.EVENT_ON_ANIMATION_COMPLETED);
	}

	showLigntnings()
	{
		for (let i=0; i<3; i++)
		{
			let lightning = this.addChild(new Lightning());
			lightning.y = 10 - 60; 
			lightning.scale.set(0.574);
			lightning.rotation = Utils.gradToRad(LIGHTNING_ROTATIONS[i]);
		}
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		
		this.plasmaSmoke = null;
		this.spinner = null;
		this.spinnerRepeatCounter = undefined;

		super.destroy();
	}
}

export default InstantKillAtomizer;